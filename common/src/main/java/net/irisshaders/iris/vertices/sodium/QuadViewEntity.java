package net.irisshaders.iris.vertices.sodium;

import net.irisshaders.iris.vertices.views.QuadView;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Pointer;

public class QuadViewEntity implements QuadView {
    private long writePointer;
    private int stride;

    public void setup(long writePointer, int stride) {
        // Ensure pointer is aligned according to platform requirements
        this.writePointer = writePointer & ~((1L << Pointer.POINTER_SHIFT) - 1);
        this.stride = (stride + (1 << Pointer.POINTER_SHIFT) - 1) & ~((1 << Pointer.POINTER_SHIFT) - 1);
    }

    @Override
    public float x(int index) {
        return MemoryUtil.memGetFloat(getOffset(index));
    }

    @Override
    public float y(int index) {
        return MemoryUtil.memGetFloat(getOffset(index) + 4);
    }

    @Override
    public float z(int index) {
        return MemoryUtil.memGetFloat(getOffset(index) + 8);
    }

    @Override
    public float u(int index) {
        return MemoryUtil.memGetFloat(getOffset(index) + 16);
    }

    @Override
    public float v(int index) {
        return MemoryUtil.memGetFloat(getOffset(index) + 20);
    }

    private long getOffset(int index) {
        // Ensure index arithmetic is platform-aware
        return writePointer + ((long) stride * index);
    }
}
