package net.irisshaders.iris.vertices.sodium;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.caffeinemc.mods.sodium.api.memory.MemoryIntrinsics;
import net.caffeinemc.mods.sodium.api.vertex.serializer.VertexSerializer;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import net.irisshaders.iris.vertices.NormI8;
import net.irisshaders.iris.vertices.NormalHelper;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

public class GlyphExtVertexSerializer implements VertexSerializer {
    private static final int OFFSET_POSITION = 0;
    private static final int OFFSET_COLOR = 12;
    private static final int OFFSET_TEXTURE = 16;
    private static final int OFFSET_MID_TEXTURE = IrisVertexFormats.GLYPH.getOffset(IrisVertexFormats.MID_TEXTURE_ELEMENT);
    private static final int OFFSET_LIGHT = 24;
    private static final int OFFSET_NORMAL = IrisVertexFormats.GLYPH.getOffset(VertexFormatElement.NORMAL);
    private static final int OFFSET_TANGENT = IrisVertexFormats.GLYPH.getOffset(IrisVertexFormats.TANGENT_ELEMENT);

    // A helper quad view object used for computing normals.
    private static final QuadViewEntity quad = new QuadViewEntity();
    private static final Vector3f saveNormal = new Vector3f();
    private static final int DST_STRIDE = IrisVertexFormats.GLYPH.getVertexSize();

    /**
     * Write the averaged texture coordinates, computed normal, and tangent for the quad.
     * Here we compute a base pointer for the quad vertices and then iterate over them.
     */
    private static void endQuad(float uSum, float vSum, long dstLastVertex) {
        // Compute the average UV values.
        float midU = uSum * 0.25f;
        float midV = vSum * 0.25f;

        // Compute the pointer to the first vertex of the quad.
        long quadBaseDst = dstLastVertex - DST_STRIDE * 3;
        // Setup quad view using the first vertex pointer.
        quad.setup(quadBaseDst, DST_STRIDE);

        // Compute the face normal using the quad view.
        NormalHelper.computeFaceNormal(saveNormal, quad);
        int normal = NormI8.pack(saveNormal);
        int tangent = NormalHelper.computeTangent(saveNormal.x, saveNormal.y, saveNormal.z, quad);

        // Now update each vertex in the quad with mid texture coordinates, normal, and tangent.
        for (int i = 0; i < 4; i++) {
            long vertexDst = quadBaseDst + (long) DST_STRIDE * i;
            MemoryUtil.memPutFloat(vertexDst + OFFSET_MID_TEXTURE, midU);
            MemoryUtil.memPutFloat(vertexDst + OFFSET_MID_TEXTURE + 4, midV);
            MemoryUtil.memPutInt(vertexDst + OFFSET_NORMAL, normal);
            MemoryUtil.memPutInt(vertexDst + OFFSET_TANGENT, tangent);
        }
    }

    @Override
    public void serialize(long src, long dst, int vertexCount) {
        // Cache source and destination vertex sizes.
        final int SRC_STRIDE = DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP.getVertexSize();
        final int quadVertexCount = vertexCount; // Typically 4 for a quad.
        float uSum = 0.0f;
        float vSum = 0.0f;

        // Process each vertex in the quad.
        // We use an index variable and local copies of src and dst pointers for clarity.
        long currentSrc = src;
        long currentDst = dst;
        for (int i = 0; i < quadVertexCount; i++) {
            // Read the texture coordinates from the source.
            float u = MemoryUtil.memGetFloat(currentSrc + OFFSET_TEXTURE);
            float v = MemoryUtil.memGetFloat(currentSrc + OFFSET_TEXTURE + 4);
            uSum += u;
            vSum += v;

            // Copy the first 28 bytes of the vertex data.
            MemoryIntrinsics.copyMemory(currentSrc, currentDst, 28);

            // Write the extra per-vertex rendering state.
            MemoryUtil.memPutShort(currentDst + 32, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedEntity());
            MemoryUtil.memPutShort(currentDst + 34, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedBlockEntity());
            MemoryUtil.memPutShort(currentDst + 36, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedItem());

            // Advance to the next vertex except after the last one.
            if (i != quadVertexCount - 1) {
                currentSrc += SRC_STRIDE;
                currentDst += DST_STRIDE;
            }
        }

        // At this point, currentSrc and currentDst point to the last vertex of the quad.
        endQuad(uSum, vSum, currentDst);
    }
}
