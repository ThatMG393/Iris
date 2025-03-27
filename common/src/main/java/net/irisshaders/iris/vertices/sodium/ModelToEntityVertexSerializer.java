package net.irisshaders.iris.vertices.sodium;

import net.caffeinemc.mods.sodium.api.memory.MemoryIntrinsics;
import net.caffeinemc.mods.sodium.api.util.NormI8;
import net.caffeinemc.mods.sodium.api.vertex.format.common.EntityVertex;
import net.caffeinemc.mods.sodium.api.vertex.serializer.VertexSerializer;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import net.irisshaders.iris.vertices.NormalHelper;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Pointer;

public class ModelToEntityVertexSerializer implements VertexSerializer {
    private static final long OFFSET_MASK = 0xFFFFFFFFL;
    private static final int POINTER_ALIGNMENT = 1 << Pointer.POINTER_SHIFT;
    private static final int ENTITY_STRIDE;

    static {
        // Align stride to pointer boundaries
        int baseStride = IrisVertexFormats.ENTITY.getVertexSize();
        ENTITY_STRIDE = (baseStride + POINTER_ALIGNMENT - 1) & ~(POINTER_ALIGNMENT - 1);
    }

    @Override
    public void serialize(long src, long dst, int vertexCount) {
        // Align pointers to system boundaries
        long alignedSrc = src & ~((1L << Pointer.POINTER_SHIFT) - 1);
        long alignedDst = dst & ~((1L << Pointer.POINTER_SHIFT) - 1);

        int quadCount = vertexCount / 4;
        for (int i = 0; i < quadCount; i++) {
            // Safe normal unpacking with bit manipulation
            int normalPacked = MemoryUtil.memGetInt(alignedSrc + 32L);
            float normalX = NormI8.unpackX(normalPacked);
            float normalY = NormI8.unpackY(normalPacked);
            float normalZ = NormI8.unpackZ(normalPacked);

            // Compute tangent with explicit coordinate retrieval
            int tangent = NormalHelper.computeTangent(null, 
                normalX, normalY, normalZ, 
                Float.intBitsToFloat(MemoryUtil.memGetInt(alignedSrc)),
                Float.intBitsToFloat(MemoryUtil.memGetInt(alignedSrc + 4L)),
                Float.intBitsToFloat(MemoryUtil.memGetInt(alignedSrc + 8L)),
                Float.intBitsToFloat(MemoryUtil.memGetInt(alignedSrc + 16L)),
                Float.intBitsToFloat(MemoryUtil.memGetInt(alignedSrc + 20L)),
                Float.intBitsToFloat(MemoryUtil.memGetInt(alignedSrc + EntityVertex.STRIDE)),
                Float.intBitsToFloat(MemoryUtil.memGetInt(alignedSrc + 4L + EntityVertex.STRIDE)),
                Float.intBitsToFloat(MemoryUtil.memGetInt(alignedSrc + 8L + EntityVertex.STRIDE)),
                Float.intBitsToFloat(MemoryUtil.memGetInt(alignedSrc + 16L + EntityVertex.STRIDE)),
                Float.intBitsToFloat(MemoryUtil.memGetInt(alignedSrc + 20L + EntityVertex.STRIDE)),
                Float.intBitsToFloat(MemoryUtil.memGetInt(alignedSrc + EntityVertex.STRIDE + EntityVertex.STRIDE)),
                Float.intBitsToFloat(MemoryUtil.memGetInt(alignedSrc + 4L + EntityVertex.STRIDE + EntityVertex.STRIDE)),
                Float.intBitsToFloat(MemoryUtil.memGetInt(alignedSrc + 8L + EntityVertex.STRIDE + EntityVertex.STRIDE)),
                Float.intBitsToFloat(MemoryUtil.memGetInt(alignedSrc + 16L + EntityVertex.STRIDE + EntityVertex.STRIDE)),
                Float.intBitsToFloat(MemoryUtil.memGetInt(alignedSrc + 20L + EntityVertex.STRIDE + EntityVertex.STRIDE))
            );

            // Mid-texture coordinate computation
            float midU = 0, midV = 0;
            for (int vertex = 0; vertex < 4; vertex++) {
                midU += Float.intBitsToFloat(
                    MemoryUtil.memGetInt(alignedSrc + 16L + (EntityVertex.STRIDE * vertex))
                );
                midV += Float.intBitsToFloat(
                    MemoryUtil.memGetInt(alignedSrc + 20L + (EntityVertex.STRIDE * vertex))
                );
            }
            midU /= 4;
            midV /= 4;

            // Vertex processing with aligned memory
            for (int j = 0; j < 4; j++) {
                MemoryIntrinsics.copyMemory(alignedSrc, alignedDst, 36);
                
                // Safe short conversion
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

                MemoryUtil.memPutShort(alignedDst + 36L, entityId);
                MemoryUtil.memPutShort(alignedDst + 38L, blockEntityId);
                MemoryUtil.memPutShort(alignedDst + 40L, itemId);
                
                MemoryUtil.memPutInt(alignedDst + 42L, Float.floatToIntBits(midU));
                MemoryUtil.memPutInt(alignedDst + 46L, Float.floatToIntBits(midV));
                MemoryUtil.memPutInt(alignedDst + 50L, tangent);

                alignedSrc += EntityVertex.STRIDE;
                alignedDst += ENTITY_STRIDE;
            }
        }
    }
}
