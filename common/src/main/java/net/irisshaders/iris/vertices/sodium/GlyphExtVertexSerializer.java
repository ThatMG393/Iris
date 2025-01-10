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

public class GlyphExtVertexSerializer implements VertexSerializer {
    private static final int VANILLA_STRIDE = DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP.getVertexSize();
    private static final int EXTENDED_STRIDE = IrisVertexFormats.GLYPH.getVertexSize();
    
    private static final int POSITION_OFFSET = 0;
    private static final int COLOR_OFFSET = 12;
    private static final int TEXCOORD_OFFSET = 16;
    private static final int LIGHTMAP_OFFSET = 24;
    private static final int MIDTEX_OFFSET = 28;
    private static final int NORMAL_OFFSET = 36;
    private static final int TANGENT_OFFSET = 40;
    private static final int ENTITY_OFFSET = 44;
    
    private static final Vector3f NORMAL = new Vector3f();
    private static final QuadViewEntity QUAD = new QuadViewEntity();

    @Override
    public void serialize(long src, long dst, int vertexCount) {
        if (vertexCount != 4) return;
        
        // Copy base vertex data
        for (int i = 0; i < vertexCount; i++) {
            long srcVertex = src + (i * VANILLA_STRIDE);
            long dstVertex = dst + (i * EXTENDED_STRIDE);
            
            // Position, color, UV, lightmap
            MemoryIntrinsics.copyMemory(srcVertex, dstVertex, VANILLA_STRIDE);
            
            // Additional attributes
            CapturedRenderingState state = CapturedRenderingState.INSTANCE;
            MemoryUtil.memPutShort(dstVertex + ENTITY_OFFSET, (short) state.getCurrentRenderedEntity());
            MemoryUtil.memPutShort(dstVertex + ENTITY_OFFSET + 2, (short) state.getCurrentRenderedBlockEntity());
            MemoryUtil.memPutShort(dstVertex + ENTITY_OFFSET + 4, (short) state.getCurrentRenderedItem());
        }

        // Compute normal and tangent for the quad
        QUAD.setup(dst, EXTENDED_STRIDE);
        NormalHelper.computeFaceNormal(NORMAL, QUAD);
        int normal = NormI8.pack(NORMAL);
        int tangent = NormalHelper.computeTangent(NORMAL.x, NORMAL.y, NORMAL.z, QUAD);

        // Write normal and tangent to all vertices
        for (int i = 0; i < vertexCount; i++) {
            long vertex = dst + (i * EXTENDED_STRIDE);
            MemoryUtil.memPutInt(vertex + NORMAL_OFFSET, normal);
            MemoryUtil.memPutInt(vertex + TANGENT_OFFSET, tangent);
            
            // Preserve original UV coordinates
            float u = MemoryUtil.memGetFloat(src + (i * VANILLA_STRIDE) + TEXCOORD_OFFSET);
            float v = MemoryUtil.memGetFloat(src + (i * VANILLA_STRIDE) + TEXCOORD_OFFSET + 4);
            MemoryUtil.memPutFloat(vertex + MIDTEX_OFFSET, u);
            MemoryUtil.memPutFloat(vertex + MIDTEX_OFFSET + 4, v);
        }
    }
}
