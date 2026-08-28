package me.catcoder.sidebar;

import com.google.common.base.Preconditions;

import java.util.BitSet;

/**
 * Hands out the line slots of a sidebar, lowest free slot first.
 * <p>
 * A slot decides both the team name and the invisible team entry of a line, so two live lines
 * must never share one. Not thread safe, {@link Sidebar} holds its lines lock.
 */
final class LineIndexAllocator {

    private final BitSet used;
    private final int capacity;

    LineIndexAllocator(int capacity) {
        Preconditions.checkArgument(capacity > 0, "Capacity must be positive");

        this.capacity = capacity;
        this.used = new BitSet(capacity);
    }

    int allocate() {
        int index = used.nextClearBit(0);

        Preconditions.checkState(index < capacity,
                "No free line slot available, all %s are in use", capacity);

        used.set(index);
        return index;
    }

    void release(int index) {
        Preconditions.checkElementIndex(index, capacity, "Line slot");

        used.clear(index);
    }

    void releaseAll() {
        used.clear();
    }

    int allocatedCount() {
        return used.cardinality();
    }
}
