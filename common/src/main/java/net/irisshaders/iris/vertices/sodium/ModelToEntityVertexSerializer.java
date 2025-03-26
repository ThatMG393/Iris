package net.irisshaders.iris.vertices.sodium;

import net.caffeinemc.mods.sodium.api.memory.MemoryIntrinsics;
import net.caffeinemc.mods.sodium.api.util.NormI8;
import net.caffeinemc.mods.sodium.api.vertex.format.common.EntityVertex;
import net.caffeinemc.mods.sodium.api.vertex.serializer.VertexSerializer;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import net.irisshaders.iris.vertices.NormalHelper;
import org.lwjgl.system.MemoryUtil;

public class ModelToEntityVertexSerializer implements VertexSerializer {
    private final boolean is32bit = (MemoryUtil.POINTER_SIZE == 4);

    @Override
    public void serialize(long src, long dst, int vertexCount) {
        int quadCount = vertexCount / 4;

        for (int i = 0; i < quadCount; i++) {
            int normal = MemoryUtil.memGetInt(ptr(src + 32));
            int tangent = NormalHelper.computeTangent(
                null,
                NormI8.unpackX(normal),
                NormI8.unpackY(normal),
                NormI8.unpackZ(normal),
                MemoryUtil.memGetFloat(ptr(src)),
                MemoryUtil.memGetFloat(ptr(src + 4)),
                MemoryUtil.memGetFloat(ptr(src + 8)),
                MemoryUtil.memGetFloat(ptr(src + 16)),
                MemoryUtil.memGetFloat(ptr(src + 20)),
                MemoryUtil.memGetFloat(ptr(src + EntityVertex.STRIDE)),
                MemoryUtil.memGetFloat(ptr(src + 4 + EntityVertex.STRIDE)),
                MemoryUtil.memGetFloat(ptr(src + 8 + EntityVertex.STRIDE)),
                MemoryUtil.memGetFloat(ptr(src + 16 + EntityVertex.STRIDE)),
                MemoryUtil.memGetFloat(ptr(src + 20 + EntityVertex.STRIDE)),
                MemoryUtil.memGetFloat(ptr(src + EntityVertex.STRIDE * 2)),
                MemoryUtil.memGetFloat(ptr(src + 4 + EntityVertex.STRIDE * 2)),
                MemoryUtil.memGetFloat(ptr(src + 8 + EntityVertex.STRIDE * 2)),
                MemoryUtil.memGetFloat(ptr(src + 16 + EntityVertex.STRIDE * 2)),
                MemoryUtil.memGetFloat(ptr(src + 20 + EntityVertex.STRIDE * 2))
            );

            float midU = 0, midV = 0;
            for (int vertex = 0; vertex < 4; vertex++) {
                midU += MemoryUtil.memGetFloat(ptr(src + 16 + (EntityVertex.STRIDE * vertex)));
                midV += MemoryUtil.memGetFloat(ptr(src + 20 + (EntityVertex.STRIDE * vertex)));
            }

            midU /= 4;
            midV /= 4;

            for (int j = 0; j < 4; j++) {
                MemoryIntrinsics.copyMemory(ptr(src), ptr(dst), 36);
                MemoryUtil.memPutShort(ptr(dst + 36), (short) CapturedRenderingState.INSTANCE.getCurrentRenderedEntity());
                MemoryUtil.memPutShort(ptr(dst + 38), (short) CapturedRenderingState.INSTANCE.getCurrentRenderedBlockEntity());
                MemoryUtil.memPutShort(ptr(dst + 40), (short) CapturedRenderingState.INSTANCE.getCurrentRenderedItem());
                MemoryUtil.memPutFloat(ptr(dst + 42), midU);
                MemoryUtil.memPutFloat(ptr(dst + 46), midV);
                MemoryUtil.memPutInt(ptr(dst + 50), tangent);

                src += EntityVertex.STRIDE;
                dst += IrisVertexFormats.ENTITY.getVertexSize();
            }
        }
    }

    private long ptr(long address) {
        return is32bit ? (int) address : address;
    }
}
