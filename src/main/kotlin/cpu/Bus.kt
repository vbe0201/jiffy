package io.github.vbe0201.jiffy.cpu

import io.github.vbe0201.jiffy.jit.decoder.Instruction
import io.github.vbe0201.jiffy.utils.maskSegment
import java.nio.ByteBuffer
import java.nio.ByteOrder

private const val KIB = 1 shl 10

private const val BIOS_SIZE = 512 * KIB

const val BIOS_START = 0x1FC0_0000U
const val BIOS_END = 0x1FC8_0000U

/**
 * The memory bus for the PSX CPU.
 *
 * The bus interface is responsible for handling reads and writes
 * to memory or I/O devices.
 */
class Bus(private val bios: ByteBuffer) {
    // Configure BIOS accesses to be in little-endian byte ordering
    // and make sure that we get an image of the expected size.
    init {
        bios.order(ByteOrder.LITTLE_ENDIAN)
        require(bios.remaining() == BIOS_SIZE) { "Invalid BIOS image supplied!" }
    }

    private fun readBios(addr: UInt): UInt {
        val offset = (addr - BIOS_START).toInt()
        return this.bios.getInt(offset).toUInt()
    }

    /**
     * Reads an [Instruction] from the given memory address.
     */
    fun readInstruction(addr: UInt): Instruction {
        return Instruction(read32(addr))
    }

    /**
     * Reads a 32-bit value from the given memory address in little-endian
     * byte ordering.
     */
    fun read32(addr: UInt): UInt {
        return when (val masked = maskSegment(addr)) {
            in BIOS_START..BIOS_END -> readBios(masked)

            else -> {
                println("read32(0x${masked.toString(16)})")
                0x4CU
            }
        }
    }

    /**
     * Writes an 8-bit value to the given memory address.
     */
    fun write8(addr: UInt, value: UByte) {
        // TODO
        println("write8(0x${addr.toString(16)}, 0x${value.toString(16)})")
    }

    /**
     * Writes a 16-bit value to a given memory address in little-endian
     * byte ordering.
     */
    fun write16(addr: UInt, value: UShort) {
        // TODO
        println("write16(0x${addr.toString(16)}, 0x${value.toString(16)})")
    }

    /**
     * Writes a 32-bit value to a given memory address in little-endian
     * byte ordering.
     */
    fun write32(addr: UInt, value: UInt) {
        // TODO
        println("write32(0x${addr.toString(16)}, 0x${value.toString(16)})")
    }
}
