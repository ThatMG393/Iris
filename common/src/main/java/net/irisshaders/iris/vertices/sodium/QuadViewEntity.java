package net.irisshaders.iris.vertices.sodium;

import net.irisshaders.iris.vertices.views.QuadView;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Pointer;

public class QuadViewEntity implements QuadView {
    private long writePointer;
    private int stride;

    public void setup(long writePointer, int stride) {
        this.writePointer = writePointer & ~((1L << Pointer.POINTER_SHIFT) - 1);
        this.stride = (stride + (1 << Pointer.POINTER_SHIFT) - 1) & ~((1 << Pointer.POINTER_SHIFT) - 1);
    }

    @Override
    public float x(int index) {
        return Float.intBitsToFloat(getInt(0, index));
    }

    @Override
    public float y(int index) {
        return Float.intBitsToFloat(getInt(4, index));
    }

    @Override
    public float z(int index) {
        return Float.intBitsToFloat(getInt(8, index));
    }

    @Override
    public float u(int index) {
        return Float.intBitsToFloat(getInt(16, index));
    }

    @Override
    public float v(int index) {
        return Float.intBitsToFloat(getInt(20, index));
    }

    private int getInt(int offset, int index) {
        return MemoryUtil.memGetInt(writePointer + ((long) stride * index & 0xFFFFFFFFL) + (offset & 0xFFFFFFFFL));
    }
}
