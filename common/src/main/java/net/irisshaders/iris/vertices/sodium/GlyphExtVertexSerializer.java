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
    private static final int POINTER_SIZE = Pointer.POINTER_SIZE;
    private static final int STRIDE = align(IrisVertexFormats.GLYPH.getVertexSize());
    private static final int VANILLA_STRIDE = align(DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP.getVertexSize());
    
    private static final int OFFSET_MID_TEXTURE = align(IrisVertexFormats.GLYPH.getOffset(IrisVertexFormats.MID_TEXTURE_ELEMENT));
    private static final int OFFSET_NORMAL = align(IrisVertexFormats.GLYPH.getOffset(VertexFormatElement.NORMAL));
    private static final int OFFSET_TANGENT = align(IrisVertexFormats.GLYPH.getOffset(IrisVertexFormats.TANGENT_ELEMENT));

    private static final Vector3f NORMAL = new Vector3f();
    private static final QuadViewEntity QUAD = new QuadViewEntity();

    private static int align(int value) {
        return (value + (POINTER_SIZE - 1)) & ~(POINTER_SIZE - 1);
    }

    private static long alignPtr(long ptr) {
        return (ptr + (POINTER_SIZE - 1)) & ~(POINTER_SIZE - 1);
    }

    private void endQuad(float u, float v, long dst) {
        dst = alignPtr(dst);
        QUAD.setup(dst, STRIDE);
        NormalHelper.computeFaceNormal(NORMAL, QUAD);
        
        int normal = NormI8.pack(NORMAL);
        int tangent = NormalHelper.computeTangent(NORMAL.x, NORMAL.y, NORMAL.z, QUAD);

        u *= 0.25f;
        v *= 0.25f;

        long baseOffset = dst & ~3; // Align to 4-byte boundary
        for (int i = 0; i < 4; i++) {
            long vertexOffset = baseOffset - ((long) STRIDE * i);
            long midTexOffset = alignPtr(vertexOffset + OFFSET_MID_TEXTURE);
            long normalOffset = alignPtr(vertexOffset + OFFSET_NORMAL);
            long tangentOffset = alignPtr(vertexOffset + OFFSET_TANGENT);

            MemoryUtil.memPutFloat(midTexOffset, u);
            MemoryUtil.memPutFloat(midTexOffset + 4, v);
            MemoryUtil.memPutInt(normalOffset, normal);
            MemoryUtil.memPutInt(tangentOffset, tangent);
        }
    }

    @Override
    public void serialize(long src, long dst, int vertexCount) {
        src = alignPtr(src);
        dst = alignPtr(dst);
        
        float uSum = 0, vSum = 0;
        
        CapturedRenderingState state = CapturedRenderingState.INSTANCE;
        short entity = (short) state.getCurrentRenderedEntity();
        short blockEntity = (short) state.getCurrentRenderedBlockEntity();
        short item = (short) state.getCurrentRenderedItem();

        for (int i = 0; i < vertexCount; i++) {
            uSum += MemoryUtil.memGetFloat(alignPtr(src + 16));
            vSum += MemoryUtil.memGetFloat(alignPtr(src + 20));

            MemoryIntrinsics.copyMemory(src, dst, 28);
            
            MemoryUtil.memPutShort(alignPtr(dst + 32), entity);
            MemoryUtil.memPutShort(alignPtr(dst + 34), blockEntity);
            MemoryUtil.memPutShort(alignPtr(dst + 36), item);

            if (i != 3) {
                src += VANILLA_STRIDE;
                dst += STRIDE;
            }
        }

        endQuad(uSum, vSum, dst);
    }
}
