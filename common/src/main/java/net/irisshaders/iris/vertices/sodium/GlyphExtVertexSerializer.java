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
    private static final int STRIDE = align(IrisVertexFormats.GLYPH.getVertexSize(), POINTER_SIZE);
    private static final int VANILLA_STRIDE = align(DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP.getVertexSize(), POINTER_SIZE);
    
    private static final Vector3f NORMAL = new Vector3f();
    private static final QuadViewEntity QUAD = new QuadViewEntity();

    private static int align(int value, int alignment) {
        return (value + alignment - 1) & ~(alignment - 1);
    }

    @Override
    public void serialize(long src, long dst, int vertexCount) {
        float uSum = 0, vSum = 0;
        
        CapturedRenderingState state = CapturedRenderingState.INSTANCE;
        short entity = (short) state.getCurrentRenderedEntity();
        short blockEntity = (short) state.getCurrentRenderedBlockEntity();
        short item = (short) state.getCurrentRenderedItem();

        for (int i = 0; i < vertexCount; i++) {
            long srcPos = src + (i * VANILLA_STRIDE);
            long dstPos = dst + (i * STRIDE);
            
            uSum += MemoryUtil.memGetFloat(srcPos + 16);
            vSum += MemoryUtil.memGetFloat(srcPos + 20);
            
            // Copy position + color + tex + lightmap
            MemoryIntrinsics.copyMemory(srcPos, dstPos, 28);
            
            // Write entity data at aligned offset
            long entityOffset = align(dstPos + 28, 2);
            MemoryUtil.memPutShort(entityOffset, entity);
            MemoryUtil.memPutShort(entityOffset + 2, blockEntity);
            MemoryUtil.memPutShort(entityOffset + 4, item);
        }

        QUAD.setup(dst, STRIDE);
        NormalHelper.computeFaceNormal(NORMAL, QUAD);
        
        int normal = NormI8.pack(NORMAL);
        int tangent = NormalHelper.computeTangent(NORMAL.x, NORMAL.y, NORMAL.z, QUAD);

        float avgU = uSum / vertexCount;
        float avgV = vSum / vertexCount;

        for (int i = 0; i < vertexCount; i++) {
            long vertexBase = dst + (i * STRIDE);
            // Align offsets for each field
            long midTexOffset = align(vertexBase + 28, 4);
            long normalOffset = align(midTexOffset + 8, 4);
            long tangentOffset = align(normalOffset + 4, 4);
            
            MemoryUtil.memPutFloat(midTexOffset, avgU);
            MemoryUtil.memPutFloat(midTexOffset + 4, avgV);
            MemoryUtil.memPutInt(normalOffset, normal);
            MemoryUtil.memPutInt(tangentOffset, tangent);
        }
    }
}
