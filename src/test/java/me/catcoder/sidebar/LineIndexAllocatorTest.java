package me.catcoder.sidebar;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class LineIndexAllocatorTest {

    @Test
    public void testAllocatesSequentially() {
        LineIndexAllocator allocator = new LineIndexAllocator(4);

        assertEquals(0, allocator.allocate());
        assertEquals(1, allocator.allocate());
        assertEquals(2, allocator.allocate());
        assertEquals(3, allocator.allocatedCount());
    }

    @Test
    public void testReusesReleasedSlot() {
        LineIndexAllocator allocator = new LineIndexAllocator(4);

        allocator.allocate(); // 0
        allocator.allocate(); // 1
        allocator.allocate(); // 2

        allocator.release(1);

        assertEquals(1, allocator.allocate());
        assertEquals(3, allocator.allocate());
    }

    @Test
    public void testThrowsWhenExhausted() {
        LineIndexAllocator allocator = new LineIndexAllocator(2);

        allocator.allocate();
        allocator.allocate();

        assertThrows(IllegalStateException.class, allocator::allocate);
    }

    @Test
    public void testReleaseAllFreesEverything() {
        LineIndexAllocator allocator = new LineIndexAllocator(3);

        allocator.allocate();
        allocator.allocate();
        allocator.releaseAll();

        assertEquals(0, allocator.allocatedCount());
        assertEquals(0, allocator.allocate());
    }

    @Test
    public void testRejectsOutOfRangeRelease() {
        LineIndexAllocator allocator = new LineIndexAllocator(2);

        assertThrows(IndexOutOfBoundsException.class, () -> allocator.release(2));
    }

    @Test
    public void testRejectsNonPositiveCapacity() {
        assertThrows(IllegalArgumentException.class, () -> new LineIndexAllocator(0));
    }
}
