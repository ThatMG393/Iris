package net.irisshaders.iris.vertices.sodium;

import net.irisshaders.iris.vertices.views.QuadView;
import org.lwjgl.system.MemoryUtil;

public class QuadViewEntity implements QuadView {
    private long writePointer;
    private int stride;

    public void setup(long writePointer, int stride) {
        this.writePointer = writePointer;
        this.stride = stride;
    }

    @Override
    public float x(int index) {
        return MemoryUtil.memGetFloat(writePointer + (long)(stride * (3 - index)));
    }

    @Override
    public float y(int index) {
        return MemoryUtil.memGetFloat(writePointer + (long)(stride * (3 - index)) + 4);
    }

    @Override
    public float z(int index) {
        return MemoryUtil.memGetFloat(writePointer + (long)(stride * (3 - index)) + 8);
    }

    @Override
    public float u(int index) {
        return MemoryUtil.memGetFloat(writePointer + (long)(stride * (3 - index)) + 16);
    }

    @Override
    public float v(int index) {
        return MemoryUtil.memGetFloat(writePointer + (long)(stride * (3 - index)) + 20);
    }
}
