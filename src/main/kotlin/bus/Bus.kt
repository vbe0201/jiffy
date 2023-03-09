package io.github.vbe0201.jiffy.bus

import io.github.vbe0201.jiffy.jit.decoder.Instruction
import io.github.vbe0201.jiffy.memory.MemoryMap
import io.github.vbe0201.jiffy.memory.maskSegment
import io.github.vbe0201.jiffy.utils.*
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * The memory bus for the PSX CPU block.
 *
 * The bus interface is responsible for handling reads and writes
 * to memory or I/O devices.
 */
class Bus(private val bios: ByteBuffer) {
    private val ram = makeArrayBuffer(MemoryMap.RAM.size, 0xCA.toByte())

    // Configure all memory accesses to be in little-endian byte order
    // and make sure that we get a BIOS image of the expected size.
    init {
        require(bios.size() == MemoryMap.BIOS.size) { "Invalid BIOS image supplied!" }
        bios.order(ByteOrder.LITTLE_ENDIAN)

        ram.order(ByteOrder.LITTLE_ENDIAN)
    }

    /** Reads a value from the given address through the bus. */
    fun read8(addr: UInt): UByte {
        val masked = maskSegment(addr)

        return if (MemoryMap.RAM.contains(masked)) {
            this.ram.uget(MemoryMap.RAM.makeOffset(masked))
        } else if (MemoryMap.BIOS.contains(masked)) {
            this.bios.uget(MemoryMap.BIOS.makeOffset(masked))
        } else {
            println("read(0x${masked.toString(16)})")
            return 0U
        }
    }

    /** Reads a value from the given address through the bus. */
    fun read16(addr: UInt): UShort {
        val masked = maskSegment(addr)

        return if (MemoryMap.RAM.contains(masked)) {
            this.ram.getUShort(MemoryMap.RAM.makeOffset(masked))
        } else if (MemoryMap.BIOS.contains(masked)) {
            this.bios.getUShort(MemoryMap.BIOS.makeOffset(masked))
        } else {
            println("read(0x${masked.toString(16)})")
            return 0U
        }
    }

    /** Reads a value from the given address through the bus. */
    fun read32(addr: UInt): UInt {
        val masked = maskSegment(addr)

        return if (MemoryMap.RAM.contains(masked)) {
            this.ram.getUInt(MemoryMap.RAM.makeOffset(masked))
        } else if (MemoryMap.BIOS.contains(masked)) {
            this.bios.getUInt(MemoryMap.BIOS.makeOffset(masked))
        } else {
            println("read(0x${masked.toString(16)})")
            return 0U
        }
    }

    /** Writes a [value] to the given address through the bus. */
    fun write8(addr: UInt, value: UByte) {
        val masked = maskSegment(addr)

        if (MemoryMap.RAM.contains(masked)) {
            this.ram.uput(MemoryMap.RAM.makeOffset(masked), value)
        } else {
            println("write(0x${addr.toString(16)}, ..)")
        }
    }

    /** Writes a [value] to the given address through the bus. */
    fun write16(addr: UInt, value: UShort) {
        val masked = maskSegment(addr)

        if (MemoryMap.RAM.contains(masked)) {
            this.ram.putUShort(MemoryMap.RAM.makeOffset(masked), value)
        } else {
            println("write(0x${addr.toString(16)}, ..)")
        }
    }

    /** Writes a [value] to the given address through the bus. */
    fun write32(addr: UInt, value: UInt) {
        val masked = maskSegment(addr)

        if (MemoryMap.RAM.contains(masked)) {
            this.ram.putUInt(MemoryMap.RAM.makeOffset(masked), value)
        } else {
            println("write(0x${addr.toString(16)}, ..)")
        }
    }

    /** Reads an [Instruction] from the given memory address. */
    fun readInstruction(addr: UInt): Instruction {
        return Instruction(this.read32(addr))
    }
}
