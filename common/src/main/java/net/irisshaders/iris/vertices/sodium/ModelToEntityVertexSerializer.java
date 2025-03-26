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
        // Only accept quads, to be safe
        int quadCount = vertexCount << 2;
        // Cache stride values
        final int srcVertexSize = EntityVertex.STRIDE;
        final int dstVertexSize = IrisVertexFormats.ENTITY.getVertexSize();

        for (int i = 0; i < quadCount; i++) {
            // For each quad, work with a local pointer to the quad's first vertex
            long quadSrc = src;
            
            // Read the normal from the first vertex of the quad (offset 32)
            int normal = MemoryUtil.memGetInt(quadSrc + 32);
            
            // Compute tangent using values from the quad’s vertices.
            int tangent = NormalHelper.computeTangent(
                null,
                NormI8.unpackX(normal),
                NormI8.unpackY(normal),
                NormI8.unpackZ(normal),
                MemoryUtil.memGetFloat(quadSrc),
                MemoryUtil.memGetFloat(quadSrc + 4),
                MemoryUtil.memGetFloat(quadSrc + 8),
                MemoryUtil.memGetFloat(quadSrc + 16),
                MemoryUtil.memGetFloat(quadSrc + 20),
                MemoryUtil.memGetFloat(quadSrc + srcVertexSize),
                MemoryUtil.memGetFloat(quadSrc + 4 + srcVertexSize),
                MemoryUtil.memGetFloat(quadSrc + 8 + srcVertexSize),
                MemoryUtil.memGetFloat(quadSrc + 16 + srcVertexSize),
                MemoryUtil.memGetFloat(quadSrc + 20 + srcVertexSize),
                MemoryUtil.memGetFloat(quadSrc + srcVertexSize * 2),
                MemoryUtil.memGetFloat(quadSrc + 4 + srcVertexSize * 2),
                MemoryUtil.memGetFloat(quadSrc + 8 + srcVertexSize * 2),
                MemoryUtil.memGetFloat(quadSrc + 16 + srcVertexSize * 2),
                MemoryUtil.memGetFloat(quadSrc + 20 + srcVertexSize * 2)
            );

            // Calculate average (mid) U and V texture coordinates across the quad’s vertices
            float midU = 0, midV = 0;
            for (int vertex = 0; vertex < 4; vertex++) {
                midU += MemoryUtil.memGetFloat(quadSrc + 16 + (srcVertexSize * vertex));
                midV += MemoryUtil.memGetFloat(quadSrc + 20 + (srcVertexSize * vertex));
            }
            midU /= 4;
            midV /= 4;

            // Process each of the quad's 4 vertices.
            for (int j = 0; j < 4; j++) {
                // Copy the first 36 bytes from the current vertex to the destination.
                MemoryIntrinsics.copyMemory(quadSrc + (srcVertexSize * j), dst, 36);
                MemoryUtil.memPutShort(dst + 36, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedEntity());
                MemoryUtil.memPutShort(dst + 38, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedBlockEntity());
                MemoryUtil.memPutShort(dst + 40, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedItem());
                MemoryUtil.memPutFloat(dst + 42, midU);
                MemoryUtil.memPutFloat(dst + 46, midV);
                MemoryUtil.memPutInt(dst + 50, tangent);

                // Increment the destination pointer by the destination vertex size.
                dst += dstVertexSize;
            }
            // Move the source pointer forward by 4 vertices (one quad).
            src += srcVertexSize * 4;
        }
    }
}
