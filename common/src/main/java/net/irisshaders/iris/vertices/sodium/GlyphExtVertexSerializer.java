package net.irisshaders.iris.vertices.sodium;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.caffeinemc.mods.sodium.api.vertex.serializer.VertexSerializer;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.vertices.NormI8;
import net.irisshaders.iris.vertices.NormalHelper;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

public class GlyphExtVertexSerializer implements VertexSerializer {
    private static final int VANILLA_STRIDE = DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP.getVertexSize();
    private static final int STRIDE = 56; // Total format size with padding
    
    private static final int OFFSET_POSITION = 0;  // vec3f
    private static final int OFFSET_COLOR = 12;    // uint32
    private static final int OFFSET_UV0 = 16;      // vec2f
    private static final int OFFSET_UV2 = 24;      // vec2f
    private static final int OFFSET_NORMAL = 32;   // vec4b
    // padding(1) at 36
    private static final int OFFSET_ENTITY = 37;   // 3x short
    private static final int OFFSET_MIDTEX = 43;   // vec2f
    private static final int OFFSET_TANGENT = 51;  // vec4b
    // padding(1) at 55

    private static final Vector3f NORMAL = new Vector3f();
    private static final QuadViewEntity QUAD = new QuadViewEntity();

    @Override
    public void serialize(long src, long dst, int vertexCount) {
        float uSum = 0, vSum = 0;
        CapturedRenderingState state = CapturedRenderingState.INSTANCE;
        
        for (int i = 0; i < vertexCount; i++) {
            long srcPos = src + (i * VANILLA_STRIDE);
            long dstPos = dst + (i * STRIDE);
            
            // Copy position, color, UV0, UV2 (28 bytes)
            MemoryUtil.memCopy(srcPos, dstPos, 28);
            
            uSum += MemoryUtil.memGetFloat(srcPos + 16);
            vSum += MemoryUtil.memGetFloat(srcPos + 20);
            
            // Write entity IDs
            MemoryUtil.memPutShort(dstPos + OFFSET_ENTITY, (short)state.getCurrentRenderedEntity());
            MemoryUtil.memPutShort(dstPos + OFFSET_ENTITY + 2, (short)state.getCurrentRenderedBlockEntity());
            MemoryUtil.memPutShort(dstPos + OFFSET_ENTITY + 4, (short)state.getCurrentRenderedItem());
        }

        QUAD.setup(dst, STRIDE);
        NormalHelper.computeFaceNormal(NORMAL, QUAD);
        int normal = NormI8.pack(NORMAL);
        int tangent = NormalHelper.computeTangent(NORMAL.x, NORMAL.y, NORMAL.z, QUAD);
        
        float avgU = uSum / vertexCount;
        float avgV = vSum / vertexCount;

        for (int i = 0; i < vertexCount; i++) {
            long dstPos = dst + (i * STRIDE);
            MemoryUtil.memPutInt(dstPos + OFFSET_NORMAL, normal);
            MemoryUtil.memPutFloat(dstPos + OFFSET_MIDTEX, avgU);
            MemoryUtil.memPutFloat(dstPos + OFFSET_MIDTEX + 4, avgV);
            MemoryUtil.memPutInt(dstPos + OFFSET_TANGENT, tangent);
        }
    }
}
