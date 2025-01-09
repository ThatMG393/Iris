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
import org.lwjgl.system.Pointer;

public class GlyphExtVertexSerializer implements VertexSerializer {
    private static final int OFFSET_POSITION = 0;
    private static final int OFFSET_COLOR = 12;
    private static final int OFFSET_TEXTURE = 16;
    private static final int OFFSET_MID_TEXTURE = IrisVertexFormats.GLYPH.getOffset(IrisVertexFormats.MID_TEXTURE_ELEMENT);
    private static final int OFFSET_LIGHT = 24;
    private static final int OFFSET_NORMAL = IrisVertexFormats.GLYPH.getOffset(VertexFormatElement.NORMAL);
    private static final int OFFSET_TANGENT = IrisVertexFormats.GLYPH.getOffset(IrisVertexFormats.TANGENT_ELEMENT);
    private static final QuadViewEntity quad = new QuadViewEntity();
    private static final Vector3f saveNormal = new Vector3f();
    private static final int STRIDE = IrisVertexFormats.GLYPH.getVertexSize();

    private static void endQuad(float uSum, float vSum, long src, long dst) {
        uSum *= 0.25f;
        vSum *= 0.25f;

        quad.setup(dst, STRIDE);

        float normalX, normalY, normalZ;

        NormalHelper.computeFaceNormal(saveNormal, quad);
        normalX = saveNormal.x;
        normalY = saveNormal.y;
        normalZ = saveNormal.z;
        int normal = NormI8.pack(saveNormal);

        int tangent = NormalHelper.computeTangent(normalX, normalY, normalZ, quad);

        // Align vertex writes to pointer size
        long alignedDst = dst & ~((1L << Pointer.POINTER_SHIFT) - 1);
        
        for (int vertex = 0; vertex < 4; vertex++) {
            long vertexOffset = alignedDst + (STRIDE * vertex);
            MemoryUtil.memPutFloat(vertexOffset + OFFSET_MID_TEXTURE, uSum);
            MemoryUtil.memPutFloat(vertexOffset + OFFSET_MID_TEXTURE + 4, vSum);
            MemoryUtil.memPutInt(vertexOffset + OFFSET_NORMAL, normal);
            MemoryUtil.memPutInt(vertexOffset + OFFSET_TANGENT, tangent);
        }
    }

    @Override
    public void serialize(long src, long dst, int vertexCount) {
        float uSum = 0.0f, vSum = 0.0f;

        for (int i = 0; i < vertexCount; i++) {
            float u = MemoryUtil.memGetFloat(src + OFFSET_TEXTURE);
            float v = MemoryUtil.memGetFloat(src + OFFSET_TEXTURE + 4);

            uSum += u;
            vSum += v;

            MemoryIntrinsics.copyMemory(src, dst, 28);

            MemoryUtil.memPutShort(dst + 32, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedEntity());
            MemoryUtil.memPutShort(dst + 34, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedBlockEntity());
            MemoryUtil.memPutShort(dst + 36, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedItem());

            if (i != 3) {
                src += DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP.getVertexSize();
                dst += STRIDE;
            }
        }

        endQuad(uSum, vSum, src, dst);
    }
}
