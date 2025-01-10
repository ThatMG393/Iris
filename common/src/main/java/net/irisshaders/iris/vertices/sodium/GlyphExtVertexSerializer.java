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
    private static final int OFFSET_POSITION = 0;
    private static final int OFFSET_COLOR = 12;
    private static final int OFFSET_TEXTURE = 16;
    private static final int OFFSET_LIGHT = 24;
    private static final int OFFSET_NORMAL = 28;
    // private static final int OFFSET_PADDING1 = 32;
    private static final int OFFSET_ENTITY = 33;
    private static final int OFFSET_MID_TEXTURE = 39;
    private static final int OFFSET_TANGENT = 47;
    // private static final int OFFSET_PADDING2 = 51;
    private static final QuadViewEntity quad = new QuadViewEntity();
    private static final Vector3f saveNormal = new Vector3f();
    private static final int STRIDE = 52; // Total size after padding

    private static void endQuad(float uSum, float vSum, int src, int dst) {
        uSum *= 0.25f;
        vSum *= 0.25f;

        quad.setup(dst, STRIDE);

        NormalHelper.computeFaceNormal(saveNormal, quad);
        int normal = NormI8.pack(saveNormal);
        int tangent = NormalHelper.computeTangent(saveNormal.x, saveNormal.y, saveNormal.z, quad);

        for (int vertex = 0; vertex < 4; vertex++) {
            int offset = dst - STRIDE * vertex;
            MemoryUtil.memPutFloat(offset + OFFSET_MID_TEXTURE, uSum);
            MemoryUtil.memPutFloat(offset + OFFSET_MID_TEXTURE + 4, vSum);
            MemoryUtil.memPutInt(offset + OFFSET_NORMAL, normal);
            MemoryUtil.memPutInt(offset + OFFSET_TANGENT, tangent);
        }
    }

    @Override
    public void serialize(long src, long dst, int vertexCount) {
        float uSum = 0.0f, vSum = 0.0f;

        for (int i = 0; i < vertexCount; i++) {
            float u = MemoryUtil.memGetFloat(src + OFFSET_TEXTURE);
            float v = MemoryUtil.memGetFloat(src + OFFSET_TEXTURE + 4);

            uSum += u;
            vSum += v;

            MemoryIntrinsics.copyMemory(src, dst, 28);

            int currentDst = dst + OFFSET_ENTITY;
            MemoryUtil.memPutShort(currentDst, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedEntity());
            MemoryUtil.memPutShort(currentDst + 2, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedBlockEntity());
            MemoryUtil.memPutShort(currentDst + 4, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedItem());

            if (i != 3) {
                src += DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP.getVertexSize();
                dst += STRIDE;
            }
        }

        endQuad(uSum, vSum, src, dst);
    }
					}
