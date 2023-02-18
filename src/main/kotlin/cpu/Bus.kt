package io.github.vbe0201.jiffy.cpu

import io.github.vbe0201.jiffy.jit.decoder.Instruction
import java.nio.ByteBuffer
import java.nio.ByteOrder

private const val KIB = 1 shl 10

private const val BIOS_SIZE = 512 * KIB

const val BIOS_START = 0xBFC0_0000U
const val BIOS_END = 0xBFC8_0000U

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
        assert(bios.remaining() == BIOS_SIZE) { "Invalid BIOS image supplied!" }
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
        return when (addr) {
            in BIOS_START..BIOS_END -> readBios(addr)

            else -> TODO("[$addr] Memory read not yet implemented")
        }
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
