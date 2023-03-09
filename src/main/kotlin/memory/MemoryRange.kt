package io.github.vbe0201.jiffy.memory

/**
 * Represents a span of byte-addressable memory in the MIPS
 * address space.
 *
 * [MemoryRange]s represent distinct memory spaces reachable
 * through the bus and help with relative address-to-offset
 * conversions for accessing them.
 */
data class MemoryRange(
    /** The start address of this range in memory. */
    val start: UInt,
    /** The size of this memory range in bytes. */
    val size: UInt,
) {
    /** Returns the last address that is part of this range. */
    inline fun lastAddress(): UInt {
        return this.start + this.size - 1U
    }

    /** Checks if an address is contained in the memory range. */
    inline fun contains(addr: UInt): Boolean {
        return this.start <= addr && addr <= this.lastAddress()
    }

    /**
     * Converts a given address to a relative offset from the
     * start of the memory range.
     */
    inline fun makeOffset(addr: UInt): Int {
        return (addr - this.start).toInt()
    }
}
