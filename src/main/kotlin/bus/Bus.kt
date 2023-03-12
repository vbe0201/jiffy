package io.github.vbe0201.jiffy.bus

import io.github.vbe0201.jiffy.dma.DmaEngine
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
class Bus(
    /** The read-only BIOS image buffer. */
    val bios: ByteBuffer,
) {
    /** The Random Access Memory buffer. */
    val ram = makeArrayBuffer(MemoryMap.RAM.size, 0xCA.toByte())

    /** The [DmaEngine] peripheral. */
    val dma = DmaEngine(this.ram)

    // Configure all memory accesses to be in little-endian byte order
    // and make sure that we get a BIOS image of the expected size.
    init {
        require(bios.size() == MemoryMap.BIOS.size) { "Invalid BIOS image supplied!" }
        bios.order(ByteOrder.LITTLE_ENDIAN)

        ram.order(ByteOrder.LITTLE_ENDIAN)
    }

    @PublishedApi
    internal inline fun <reified T> read(
        addr: UInt,
        op: ByteBuffer.(Int) -> T,
        default: T,
    ): T {
        val masked = maskSegment(addr)

        if (MemoryMap.RAM.contains(masked)) {
            return this.ram.op(MemoryMap.RAM.makeOffset(masked))
        } else if (MemoryMap.BIOS.contains(masked)) {
            return this.bios.op(MemoryMap.BIOS.makeOffset(masked))
        } else {
            println("read(0x${addr.toString(16)})")
            return default
        }
    }

    @PublishedApi
    internal inline fun <reified T> write(
        addr: UInt,
        op: ByteBuffer.(Int, T) -> Unit,
        value: T,
    ) {
        val masked = maskSegment(addr)

        if (MemoryMap.RAM.contains(masked)) {
            this.ram.op(MemoryMap.RAM.makeOffset(masked), value)
        } else {
            println("write(0x${addr.toString(16)})")
        }
    }

    /** Reads a value from the given address through the bus. */
    inline fun read8(addr: UInt): UByte {
        return this.read(addr, ByteBuffer::uget, 0U)
    }

    /** Reads a value from the given address through the bus. */
    inline fun read16(addr: UInt): UShort {
        return this.read(addr, ByteBuffer::getUShort, 0U)
    }

    /** Reads a value from the given address through the bus. */
    fun read32(addr: UInt): UInt {
        if (MemoryMap.DMA.contains(addr)) {
            return this.dma.read32(MemoryMap.DMA.makeOffset(addr))
        } else if (MemoryMap.GPU.contains(addr)) {
            return when (MemoryMap.GPU.makeOffset(addr)) {
                // HACK: Set bits 26, 27, 28 to signal that the GPU
                // is ready for DMA and CPU access. This avoids the
                // BIOS deadlocks when polling on this register.
                4 -> 0x1C00_0000U
                else -> 0U
            }
        } else {
            return this.read(addr, ByteBuffer::getUInt, 0U)
        }
    }

    /** Writes a [value] to the given address through the bus. */
    fun write8(addr: UInt, value: UByte) {
        this.write(addr, ByteBuffer::uput, value)
    }

    /** Writes a [value] to the given address through the bus. */
    fun write16(addr: UInt, value: UShort) {
        this.write(addr, ByteBuffer::putUShort, value)
    }

    /** Writes a [value] to the given address through the bus. */
    fun write32(addr: UInt, value: UInt) {
        if (MemoryMap.DMA.contains(addr)) {
            this.dma.write32(MemoryMap.DMA.makeOffset(addr), value)
        } else {
            this.write(addr, ByteBuffer::putUInt, value)
        }
    }

    /** Reads an [Instruction] from the given memory address. */
    fun readInstruction(addr: UInt): Instruction {
        return Instruction(this.read32(addr))
    }
}
