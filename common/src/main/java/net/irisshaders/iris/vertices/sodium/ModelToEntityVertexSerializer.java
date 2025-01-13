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
        int quadCount = vertexCount / 4;

        for (int i = 0; i < quadCount; i++) {
            int srcOffset = 0;
            int dstOffset = 0;
            
            int normal = getInt(src, srcOffset + 32);
            int tangent = NormalHelper.computeTangent(
                null,
                NormI8.unpackX(normal),
                NormI8.unpackY(normal),
                NormI8.unpackZ(normal),
                intBitsToFloat(getInt(src, srcOffset)),
                intBitsToFloat(getInt(src, srcOffset + 4)),
                intBitsToFloat(getInt(src, srcOffset + 8)),
                intBitsToFloat(getInt(src, srcOffset + 16)),
                intBitsToFloat(getInt(src, srcOffset + 20)),
                intBitsToFloat(getInt(src, srcOffset + EntityVertex.STRIDE)),
                intBitsToFloat(getInt(src, srcOffset + 4 + EntityVertex.STRIDE)),
                intBitsToFloat(getInt(src, srcOffset + 8 + EntityVertex.STRIDE)),
                intBitsToFloat(getInt(src, srcOffset + 16 + EntityVertex.STRIDE)),
                intBitsToFloat(getInt(src, srcOffset + 20 + EntityVertex.STRIDE)),
                intBitsToFloat(getInt(src, srcOffset + 2 * EntityVertex.STRIDE)),
                intBitsToFloat(getInt(src, srcOffset + 4 + 2 * EntityVertex.STRIDE)),
                intBitsToFloat(getInt(src, srcOffset + 8 + 2 * EntityVertex.STRIDE)),
                intBitsToFloat(getInt(src, srcOffset + 16 + 2 * EntityVertex.STRIDE)),
                intBitsToFloat(getInt(src, srcOffset + 20 + 2 * EntityVertex.STRIDE))
            );

            float midU = 0, midV = 0;
            for (int vertex = 0; vertex < 4; vertex++) {
                midU += intBitsToFloat(getInt(src, srcOffset + 16 + (EntityVertex.STRIDE * vertex)));
                midV += intBitsToFloat(getInt(src, srcOffset + 20 + (EntityVertex.STRIDE * vertex)));
            }

            midU /= 4;
            midV /= 4;

            for (int j = 0; j < 4; j++) {
                MemoryIntrinsics.copyMemory(src + srcOffset, dst + dstOffset, 36);
                putShort(dst, dstOffset + 36, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedEntity());
                putShort(dst, dstOffset + 38, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedBlockEntity());
                putShort(dst, dstOffset + 40, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedItem());
                putInt(dst, dstOffset + 42, Float.floatToRawIntBits(midU));
                putInt(dst, dstOffset + 46, Float.floatToRawIntBits(midV));
                putInt(dst, dstOffset + 50, tangent);

                srcOffset += EntityVertex.STRIDE;
                dstOffset += IrisVertexFormats.ENTITY.getVertexSize();
            }
        }
    }

    private static int getInt(long base, int offset) {
        return MemoryUtil.memGetInt(base + (offset & 0xFFFFFFFFL));
    }

    private static float intBitsToFloat(int bits) {
        return Float.intBitsToFloat(bits);
    }

    private static void putShort(long base, int offset, short value) {
        MemoryUtil.memPutShort(base + (offset & 0xFFFFFFFFL), value);
    }

    private static void putInt(long base, int offset, int value) {
        MemoryUtil.memPutInt(base + (offset & 0xFFFFFFFFL), value);
    }
}
