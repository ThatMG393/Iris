package net.irisshaders.iris.vertices.sodium;

import net.irisshaders.iris.vertices.views.QuadView;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Pointer;

public class QuadViewEntity implements QuadView {
    // Aligned memory pointer to ensure architectural consistency
    private long writePointer;
    // Stride aligned to pointer boundaries
    private int stride;

    /**
     * Configures vertex view with memory-aligned pointer and stride.
     * 
     * @param writePointer Base memory address for vertex data
     * @param stride Vertex data stride, aligned to pointer boundaries
     */
    public void setup(long writePointer, int stride) {
        // Mask pointer to align with system pointer boundaries
        this.writePointer = writePointer & ~((1L << Pointer.POINTER_SHIFT) - 1);
        
        // Adjust stride to pointer-aligned boundary, preventing misalignment
        this.stride = (stride + (1 << Pointer.POINTER_SHIFT) - 1) 
                      & ~((1 << Pointer.POINTER_SHIFT) - 1);
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

    /**
     * Retrieves a 32-bit integer from vertex memory with safe addressing.
     * 
     * @param offset Byte offset within vertex data
     * @param index Vertex index
     * @return 32-bit integer value
     */
    private int getInt(int offset, int index) {
        // Use explicit 32-bit unsigned long masking to prevent overflow
        // Compute memory address with safe index and offset calculation
        return MemoryUtil.memGetInt(
            writePointer + 
            ((long) stride * index & 0xFFFFFFFFL) + 
            (offset & 0xFFFFFFFFL)
        );
    }
}
