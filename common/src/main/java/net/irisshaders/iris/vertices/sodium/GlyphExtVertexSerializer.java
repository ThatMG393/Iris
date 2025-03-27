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
    private static final long OFFSET_MASK = 0xFFFFFFFFL;
    private static final int POINTER_ALIGNMENT = 1 << Pointer.POINTER_SHIFT;

    private static final QuadViewEntity quad = new QuadViewEntity();
    private static final int STRIDE;

    static {
        // Align stride to pointer boundaries
        int baseStride = IrisVertexFormats.GLYPH.getVertexSize();
        STRIDE = (baseStride + POINTER_ALIGNMENT - 1) & ~(POINTER_ALIGNMENT - 1);
    }

    private void endQuad(float uSum, float vSum, long src, long dst) {
        uSum *= 0.25f;
        vSum *= 0.25f;

        // Align memory pointers
        long alignedDst = dst & ~((1L << Pointer.POINTER_SHIFT) - 1);
        quad.setup(alignedDst, STRIDE);

        Vector3f faceNormal = new Vector3f();
        NormalHelper.computeFaceNormal(faceNormal, quad);
        
        int packedNormal = NormI8.pack(faceNormal);
        int packedTangent = NormalHelper.computeTangent(
            faceNormal.x, faceNormal.y, faceNormal.z, quad
        );

        for (int vertex = 0; vertex < 4; vertex++) {
            long vertexOffset = alignedDst - ((long) STRIDE * vertex & OFFSET_MASK);
            
            MemoryUtil.memPutInt(vertexOffset + 16, Float.floatToIntBits(uSum));
            MemoryUtil.memPutInt(vertexOffset + 20, Float.floatToIntBits(vSum));
            MemoryUtil.memPutInt(vertexOffset + 32, packedNormal);
            MemoryUtil.memPutInt(vertexOffset + 36, packedTangent);
        }
    }

    @Override
    public void serialize(long src, long dst, int vertexCount) {
        // Align source and destination pointers
        long alignedSrc = src & ~((1L << Pointer.POINTER_SHIFT) - 1);
        long alignedDst = dst & ~((1L << Pointer.POINTER_SHIFT) - 1);

        float uSum = 0.0f, vSum = 0.0f;

        for (int i = 0; i < vertexCount; i++) {
            // Use bit manipulation for safe memory access
            long texOffset = alignedSrc + 16L & OFFSET_MASK;
            float u = Float.intBitsToFloat(MemoryUtil.memGetInt(texOffset));
            float v = Float.intBitsToFloat(MemoryUtil.memGetInt(texOffset + 4L));

            uSum += u;
            vSum += v;

            // Aligned memory copy
            MemoryIntrinsics.copyMemory(alignedSrc, alignedDst, 28L);

            // Safe short conversion with explicit bounds
            short entityId = (short) Math.min(
                Short.MAX_VALUE, 
                CapturedRenderingState.INSTANCE.getCurrentRenderedEntity()
            );
            short blockEntityId = (short) Math.min(
                Short.MAX_VALUE, 
                CapturedRenderingState.INSTANCE.getCurrentRenderedBlockEntity()
            );
            short itemId = (short) Math.min(
                Short.MAX_VALUE, 
                CapturedRenderingState.INSTANCE.getCurrentRenderedItem()
            );

            // Write aligned memory
            MemoryUtil.memPutShort(alignedDst + 32L, entityId);
            MemoryUtil.memPutShort(alignedDst + 34L, blockEntityId);
            MemoryUtil.memPutShort(alignedDst + 36L, itemId);

            if (i != 3) {
                alignedSrc += DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP.getVertexSize();
                alignedDst += STRIDE;
            }
        }

        endQuad(uSum, vSum, src, dst);
    }
}
