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
    private static final boolean is32bit = (MemoryUtil.POINTER_SIZE == 4);
    
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

    // Helper method to ensure that pointers are properly cast on 32-bit systems.
    private static long ptr(long address) {
        return is32bit ? (int) address : address;
    }

    private static void endQuad(float uSum, float vSum, long src, long dst) {
        uSum *= 0.25f;
        vSum *= 0.25f;

        quad.setup(dst, IrisVertexFormats.GLYPH.getVertexSize());

        NormalHelper.computeFaceNormal(saveNormal, quad);
        int normal = NormI8.pack(saveNormal);
        int tangent = NormalHelper.computeTangent(saveNormal.x, saveNormal.y, saveNormal.z, quad);

        for (long vertex = 0; vertex < 4; vertex++) {
            MemoryUtil.memPutFloat(ptr(dst + OFFSET_MID_TEXTURE - STRIDE * vertex), uSum);
            MemoryUtil.memPutFloat(ptr(dst + (OFFSET_MID_TEXTURE + 4) - STRIDE * vertex), vSum);
            MemoryUtil.memPutInt(ptr(dst + OFFSET_NORMAL - STRIDE * vertex), normal);
            MemoryUtil.memPutInt(ptr(dst + OFFSET_TANGENT - STRIDE * vertex), tangent);
        }
    }

    @Override
    public void serialize(long src, long dst, int vertexCount) {
        float uSum = 0.0f, vSum = 0.0f;

        for (int i = 0; i < vertexCount; i++) {
            float u = MemoryUtil.memGetFloat(ptr(src + OFFSET_TEXTURE));
            float v = MemoryUtil.memGetFloat(ptr(src + OFFSET_TEXTURE + 4));

            uSum += u;
            vSum += v;

            MemoryIntrinsics.copyMemory(ptr(src), ptr(dst), 28);

            MemoryUtil.memPutShort(ptr(dst + 32), (short) CapturedRenderingState.INSTANCE.getCurrentRenderedEntity());
            MemoryUtil.memPutShort(ptr(dst + 34), (short) CapturedRenderingState.INSTANCE.getCurrentRenderedBlockEntity());
            MemoryUtil.memPutShort(ptr(dst + 36), (short) CapturedRenderingState.INSTANCE.getCurrentRenderedItem());

            if (i != 3) {
                src += DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP.getVertexSize();
                dst += IrisVertexFormats.GLYPH.getVertexSize();
            }
        }

        endQuad(uSum, vSum, src, dst);
    }
}
