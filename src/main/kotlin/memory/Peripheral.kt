package io.github.vbe0201.jiffy.memory

/** Represents a peripheral that is accessible through MMIO. */
interface Peripheral {
    /** Gets the [MemoryRange] this [Peripheral] is mapped to. */
    fun mapping(): MemoryRange

    /** Reads an 8-bit value from the given register offset. */
    fun read8(register: Int): UByte

    /** Reads a 16-bit value from the given register offset. */
    fun read16(register: Int): UShort

    /** Reads a 32-bit value from the given register offset. */
    fun read32(register: Int): UInt

    /** Writes an 8-bit value to the given register offset. */
    fun write8(register: Int, value: UByte)

    /** Writes a 16-bit value to the given register offset. */
    fun write16(register: Int, value: UShort)

    /** Writes a 32-bit value to the given register offset. */
    fun write32(register: Int, value: UInt)
}
