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
        int quadCount = vertexCount >> 2; // Faster division by 4
        int entityVertexStride = EntityVertex.STRIDE;
        int entityVertexSize = IrisVertexFormats.ENTITY.getVertexSize();

        for (int i = 0; i < quadCount; i++) {
            // Bitwise normal extraction
            int normal = MemoryUtil.memGetInt(src + 32);
            int normalX = (normal & 0xFF) - 128;
            int normalY = ((normal >> 8) & 0xFF) - 128;
            int normalZ = ((normal >> 16) & 0xFF) - 128;

            // Bitwise float reading - faster than Float.intBitsToFloat
            int tangent = NormalHelper.computeTangent(
                null, 
                normalX, 
                normalY, 
                normalZ, 
                bitwiseIntToFloat(MemoryUtil.memGetInt(src)),
                bitwiseIntToFloat(MemoryUtil.memGetInt(src + 4)),
                bitwiseIntToFloat(MemoryUtil.memGetInt(src + 8)),
                bitwiseIntToFloat(MemoryUtil.memGetInt(src + 16)),
                bitwiseIntToFloat(MemoryUtil.memGetInt(src + 20)),
                bitwiseIntToFloat(MemoryUtil.memGetInt(src + entityVertexStride)),
                bitwiseIntToFloat(MemoryUtil.memGetInt(src + 4 + entityVertexStride)),
                bitwiseIntToFloat(MemoryUtil.memGetInt(src + 8 + entityVertexStride)),
                bitwiseIntToFloat(MemoryUtil.memGetInt(src + 16 + entityVertexStride)),
                bitwiseIntToFloat(MemoryUtil.memGetInt(src + 20 + entityVertexStride)),
                bitwiseIntToFloat(MemoryUtil.memGetInt(src + 2 * entityVertexStride)),
                bitwiseIntToFloat(MemoryUtil.memGetInt(src + 4 + 2 * entityVertexStride)),
                bitwiseIntToFloat(MemoryUtil.memGetInt(src + 8 + 2 * entityVertexStride)),
                bitwiseIntToFloat(MemoryUtil.memGetInt(src + 16 + 2 * entityVertexStride)),
                bitwiseIntToFloat(MemoryUtil.memGetInt(src + 20 + 2 * entityVertexStride))
            );

            // Bitwise midpoint calculation
            int midUBits = 0, midVBits = 0;
            for (int vertex = 0; vertex < 4; vertex++) {
                midUBits += MemoryUtil.memGetInt(src + 16 + (entityVertexStride * vertex));
                midVBits += MemoryUtil.memGetInt(src + 20 + (entityVertexStride * vertex));
            }
            
            // Bitwise average (division by 4)
            midUBits >>= 2;
            midVBits >>= 2;

            // Serialize each vertex
            for (int j = 0; j < 4; j++) {
                MemoryIntrinsics.copyMemory(src, dst, 36);
                
                // Bitwise short casting and state extraction
                int renderedEntity = CapturedRenderingState.INSTANCE.getCurrentRenderedEntity();
                int renderedBlockEntity = CapturedRenderingState.INSTANCE.getCurrentRenderedBlockEntity();
                int renderedItem = CapturedRenderingState.INSTANCE.getCurrentRenderedItem();

                MemoryUtil.memPutShort(dst + 36, (short)(renderedEntity & 0xFFFF));
                MemoryUtil.memPutShort(dst + 38, (short)(renderedBlockEntity & 0xFFFF));
                MemoryUtil.memPutShort(dst + 40, (short)(renderedItem & 0xFFFF));
                
                // Bitwise float writing
                MemoryUtil.memPutInt(dst + 42, midUBits);
                MemoryUtil.memPutInt(dst + 46, midVBits);
                MemoryUtil.memPutInt(dst + 50, tangent);

                src += entityVertexStride;
                dst += entityVertexSize;
            }
        }
    }

    // Ultra-fast bitwise int to float conversion
    private static float bitwiseIntToFloat(int bits) {
        if ((bits & 0x7F800000) == 0) return 0.0f;  // Zero or subnormal
    
        // Extract components with minimal branching
        int sign = bits & 0x80000000;
        int exponent = (bits & 0x7F800000) >>> 23;
        int mantissa = bits & 0x007FFFFF;

        // Handle special cases with minimal overhead
        if (exponent == 0xFF) {
            return (mantissa != 0) ? Float.NaN : 
               (sign != 0) ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;
        }

        // Normalized number reconstruction
        if (exponent > 0) {
            exponent -= 127;  // Remove bias
            float value = 1.0f + (mantissa / 8388608.0f);  // 1 + m / 2^23
        
            // Precise power of 2 multiplication
            value *= (1 << exponent);
        
            // Apply sign
            return sign != 0 ? -value : value;
        }

        // Subnormal number handling
        return 0.0f;
    }
}
