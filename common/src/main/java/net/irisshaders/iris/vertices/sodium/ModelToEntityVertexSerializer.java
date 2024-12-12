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
    @Override
    public void serialize(long src, long dst, int vertexCount) {
        // Validate that vertexCount is a multiple of 4
        if ((vertexCount & 3) != 0) {
            throw new IllegalArgumentException("vertexCount must be a multiple of 4");
        }

        // Calculate quad count
        int quadCount = vertexCount >> 2; // Using bitwise shift for division by 4

        for (int i = 0; i < quadCount; i++) {
            // Ensure src is within bounds before accessing memory
            if (src + EntityVertex.STRIDE * 4 >= MemoryUtil.memGetAddress(src + vertexCount * EntityVertex.STRIDE)) {
                throw new IndexOutOfBoundsException("Source address out of bounds");
            }

            int normal = MemoryUtil.memGetInt(src + 32);
            int tangent = NormalHelper.computeTangent(null,
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
                MemoryUtil.memGetFloat(src + 20 + EntityVertex.STRIDE * 2));

            float midU = 0, midV = 0;
            for (int vertex = 0; vertex < 4; vertex++) {
                midU += MemoryUtil.memGetFloat(src + 16 + (EntityVertex.STRIDE * vertex));
                midV += MemoryUtil.memGetFloat(src + 20 + (EntityVertex.STRIDE * vertex));
            }

            midU /= 4;
            midV /= 4;

            for (int j = 0; j < 4; j++) {
                // Ensure dst is within bounds before copying memory
                if (dst >= MemoryUtil.memGetAddress(dst)) {
                    throw new IndexOutOfBoundsException("Destination address out of bounds");
                }

                MemoryIntrinsics.copyMemory(src, dst, EntityVertex.STRIDE); // Copy the vertex data
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
