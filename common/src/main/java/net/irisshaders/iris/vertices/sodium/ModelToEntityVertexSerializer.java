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
        int entityStride = EntityVertex.STRIDE;
        int irisStride = IrisVertexFormats.ENTITY.getVertexSize();

        for (int i = 0; i < quadCount; i++) {
            int normal = getInt(src, 32);
            int tangent = NormalHelper.computeTangent(
                null,
                NormI8.unpackX(normal),
                NormI8.unpackY(normal),
                NormI8.unpackZ(normal),
                intBitsToFloat(getInt(src, 0)),
                intBitsToFloat(getInt(src, 4)),
                intBitsToFloat(getInt(src, 8)),
                intBitsToFloat(getInt(src, 16)),
                intBitsToFloat(getInt(src, 20)),
                intBitsToFloat(getInt(src, entityStride)),
                intBitsToFloat(getInt(src, entityStride + 4)),
                intBitsToFloat(getInt(src, entityStride + 8)),
                intBitsToFloat(getInt(src, entityStride + 16)),
                intBitsToFloat(getInt(src, entityStride + 20)),
                intBitsToFloat(getInt(src, 2 * entityStride)),
                intBitsToFloat(getInt(src, 2 * entityStride + 4)),
                intBitsToFloat(getInt(src, 2 * entityStride + 8)),
                intBitsToFloat(getInt(src, 2 * entityStride + 16)),
                intBitsToFloat(getInt(src, 2 * entityStride + 20))
            );

            float midU = 0, midV = 0;
            for (int vertex = 0; vertex < 4; vertex++) {
                midU += intBitsToFloat(getInt(src, vertex * entityStride + 16));
                midV += intBitsToFloat(getInt(src, vertex * entityStride + 20));
            }

            midU /= 4;
            midV /= 4;

            for (int j = 0; j < 4; j++) {
                MemoryIntrinsics.copyMemory(src, dst, 36);
                putShort(dst, 36, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedEntity());
                putShort(dst, 38, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedBlockEntity());
                putShort(dst, 40, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedItem());
                putInt(dst, 42, Float.floatToRawIntBits(midU));
                putInt(dst, 46, Float.floatToRawIntBits(midV));
                putInt(dst, 50, tangent);

                src += entityStride;
                dst += irisStride;
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
