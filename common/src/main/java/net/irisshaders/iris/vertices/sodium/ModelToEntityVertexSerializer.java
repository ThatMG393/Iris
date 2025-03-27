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

    @Override
    public void serialize(long src, long dst, int vertexCount) {
        int quadCount = vertexCount / 4;
        // Branch on architecture: use int arithmetic if running on 32-bit.
        if (Pointer.BITS32) {
            // Cast src and dst pointers to int once.
            int src32 = (int) src;
            int dst32 = (int) dst;
            for (int i = 0; i < quadCount; i++) {
                int normal = MemoryUtil.memGetInt(src32 + 32);
                int tangent = NormalHelper.computeTangent(
                    null,
                    NormI8.unpackX(normal),
                    NormI8.unpackY(normal),
                    NormI8.unpackZ(normal),
                    MemoryUtil.memGetFloat(src32),
                    MemoryUtil.memGetFloat(src32 + 4),
                    MemoryUtil.memGetFloat(src32 + 8),
                    MemoryUtil.memGetFloat(src32 + 16),
                    MemoryUtil.memGetFloat(src32 + 20),
                    MemoryUtil.memGetFloat(src32 + EntityVertex.STRIDE),
                    MemoryUtil.memGetFloat(src32 + 4 + EntityVertex.STRIDE),
                    MemoryUtil.memGetFloat(src32 + 8 + EntityVertex.STRIDE),
                    MemoryUtil.memGetFloat(src32 + 16 + EntityVertex.STRIDE),
                    MemoryUtil.memGetFloat(src32 + 20 + EntityVertex.STRIDE),
                    MemoryUtil.memGetFloat(src32 + EntityVertex.STRIDE * 2),
                    MemoryUtil.memGetFloat(src32 + 4 + EntityVertex.STRIDE * 2),
                    MemoryUtil.memGetFloat(src32 + 8 + EntityVertex.STRIDE * 2),
                    MemoryUtil.memGetFloat(src32 + 16 + EntityVertex.STRIDE * 2),
                    MemoryUtil.memGetFloat(src32 + 20 + EntityVertex.STRIDE * 2)
                );

                float midU = 0, midV = 0;
                for (int vertex = 0; vertex < 4; vertex++) {
                    midU += MemoryUtil.memGetFloat(src32 + 16 + (EntityVertex.STRIDE * vertex));
                    midV += MemoryUtil.memGetFloat(src32 + 20 + (EntityVertex.STRIDE * vertex));
                }
                midU /= 4;
                midV /= 4;

                for (int j = 0; j < 4; j++) {
                    MemoryIntrinsics.copyMemory(src32, dst32, 36);
                    MemoryUtil.memPutShort(dst32 + 36, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedEntity());
                    MemoryUtil.memPutShort(dst32 + 38, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedBlockEntity());
                    MemoryUtil.memPutShort(dst32 + 40, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedItem());
                    MemoryUtil.memPutFloat(dst32 + 42, midU);
                    MemoryUtil.memPutFloat(dst32 + 46, midV);
                    MemoryUtil.memPutInt(dst32 + 50, tangent);

                    src32 += EntityVertex.STRIDE;
                    dst32 += IrisVertexFormats.ENTITY.getVertexSize();
                }
            }
        } else {
            // 64-bit: use long arithmetic directly.
            for (int i = 0; i < quadCount; i++) {
                int normal = MemoryUtil.memGetInt(src + 32);
                int tangent = NormalHelper.computeTangent(
                    null,
                    NormI8.unpackX(normal),
                    NormI8.unpackY(normal),
                    NormI8.unpackZ(normal),
                    MemoryUtil.memGetFloat(src),
                    MemoryUtil.memGetFloat(src + 4),
                    MemoryUtil.memGetFloat(src + 8),
                    MemoryUtil.memGetFloat(src + 16),
                    MemoryUtil.memGetFloat(src + 20),
                    MemoryUtil.memGetFloat(src + EntityVertex.STRIDE),
                    MemoryUtil.memGetFloat(src + 4 + EntityVertex.STRIDE),
                    MemoryUtil.memGetFloat(src + 8 + EntityVertex.STRIDE),
                    MemoryUtil.memGetFloat(src + 16 + EntityVertex.STRIDE),
                    MemoryUtil.memGetFloat(src + 20 + EntityVertex.STRIDE),
                    MemoryUtil.memGetFloat(src + EntityVertex.STRIDE * 2),
                    MemoryUtil.memGetFloat(src + 4 + EntityVertex.STRIDE * 2),
                    MemoryUtil.memGetFloat(src + 8 + EntityVertex.STRIDE * 2),
                    MemoryUtil.memGetFloat(src + 16 + EntityVertex.STRIDE * 2),
                    MemoryUtil.memGetFloat(src + 20 + EntityVertex.STRIDE * 2)
                );

                float midU = 0, midV = 0;
                for (int vertex = 0; vertex < 4; vertex++) {
                    midU += MemoryUtil.memGetFloat(src + 16 + (EntityVertex.STRIDE * vertex));
                    midV += MemoryUtil.memGetFloat(src + 20 + (EntityVertex.STRIDE * vertex));
                }
                midU /= 4;
                midV /= 4;

                for (int j = 0; j < 4; j++) {
                    MemoryIntrinsics.copyMemory(src, dst, 36);
                    MemoryUtil.memPutShort(dst + 36, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedEntity());
                    MemoryUtil.memPutShort(dst + 38, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedBlockEntity());
                    MemoryUtil.memPutShort(dst + 40, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedItem());
                    MemoryUtil.memPutFloat(dst + 42, midU);
                    MemoryUtil.memPutFloat(dst + 46, midV);
                    MemoryUtil.memPutInt(dst + 50, tangent);

                    src += EntityVertex.STRIDE;
                    dst += IrisVertexFormats.ENTITY.getVertexSize();
                }
            }
        }
    }
}
