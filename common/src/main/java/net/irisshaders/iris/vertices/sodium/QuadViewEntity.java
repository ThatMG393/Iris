package net.irisshaders.iris.vertices.sodium;

import net.irisshaders.iris.vertices.views.QuadView;
import org.lwjgl.system.MemoryUtil;

public class QuadViewEntity implements QuadView {
    private long baseX, baseY, baseZ, baseU, baseV;
    private int stride;

    public void setup(long writePointer, int stride) {
        this.stride = stride;
        // Precompute the constant offsets based on the write pointer and stride.
        this.baseX = writePointer - stride * 3L;
        this.baseY = writePointer + 4 - stride * 3L;
        this.baseZ = writePointer + 8 - stride * 3L;
        this.baseU = writePointer + 16 - stride * 3L;
        this.baseV = writePointer + 20 - stride * 3L;
    }

    @Override
    public float x(int index) {
        return MemoryUtil.memGetFloat(baseX + stride * index);
    }

    @Override
    public float y(int index) {
        return MemoryUtil.memGetFloat(baseY + stride * index);
    }

    @Override
    public float z(int index) {
        return MemoryUtil.memGetFloat(baseZ + stride * index);
    }

    @Override
    public float u(int index) {
        return MemoryUtil.memGetFloat(baseU + stride * index);
    }

    @Override
    public float v(int index) {
        return MemoryUtil.memGetFloat(baseV + stride * index);
    }
}
