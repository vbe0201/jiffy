package io.github.vbe0201.jiffy.cpu

import io.github.vbe0201.jiffy.jit.decoder.Instruction
import io.github.vbe0201.jiffy.utils.maskSegment
import java.nio.ByteBuffer
import java.nio.ByteOrder

private const val KIB = 1 shl 10

private const val RAM_SIZE = 2048 * KIB
private const val BIOS_SIZE = 512 * KIB

const val RAM_START = 0x0000_0000U
const val RAM_END = 0x0020_0000U

const val BIOS_START = 0x1FC0_0000U
const val BIOS_END = 0x1FC8_0000U

/**
 * The memory bus for the PSX CPU.
 *
 * The bus interface is responsible for handling reads and writes
 * to memory or I/O devices.
 */
class Bus(private val bios: ByteBuffer) {
    private val ram = ByteBuffer.wrap(ByteArray(RAM_SIZE) { 0xCA.toByte() })

    // Configure memory accesses to be in little-endian byte ordering
    // and make sure that we get a BIOS image of the expected size.
    init {
        bios.order(ByteOrder.LITTLE_ENDIAN)
        require(bios.remaining() == BIOS_SIZE) { "Invalid BIOS image supplied!" }

        ram.order(ByteOrder.LITTLE_ENDIAN)
    }

    private fun readBuf8(buf: ByteBuffer, addr: UInt, base: UInt): UByte {
        val offset = (addr - base).toInt()
        return buf.get(offset).toUByte()
    }

    private fun readBuf16(buf: ByteBuffer, addr: UInt, base: UInt): UShort {
        val offset = (addr - base).toInt()
        return buf.getShort(offset).toUShort()
    }

    private fun readBuf32(buf: ByteBuffer, addr: UInt, base: UInt): UInt {
        val offset = (addr - base).toInt()
        return buf.getInt(offset).toUInt()
    }

    private fun writeBuf8(
        buf: ByteBuffer,
        addr: UInt,
        base: UInt,
        value: UByte
    ) {
        val offset = (addr - base).toInt()
        buf.put(offset, value.toByte())
    }

    private fun writeBuf16(
        buf: ByteBuffer,
        addr: UInt,
        base: UInt,
        value: UShort
    ) {
        val offset = (addr - base).toInt()
        buf.putShort(offset, value.toShort())
    }

    private fun writeBuf32(
        buf: ByteBuffer,
        addr: UInt,
        base: UInt,
        value: UInt
    ) {
        val offset = (addr - base).toInt()
        buf.putInt(offset, value.toInt())
    }

    /**
     * Reads an [Instruction] from the given memory address.
     */
    fun readInstruction(addr: UInt): Instruction {
        return Instruction(read32(addr))
    }

    /**
     * Reads an 8-bit value from the given memory address.
     */
    fun read8(addr: UInt): UByte {
        return when (val masked = maskSegment(addr)) {
            in RAM_START..RAM_END -> readBuf8(this.ram, masked, RAM_START)
            in BIOS_START..BIOS_END -> readBuf8(this.bios, masked, BIOS_START)

            else -> {
                println("read8(0x${masked.toString(16)})")
                0U
            }
        }
    }

    /**
     * Reads a 32-bit value from the given memory address in little-endian
     * byte ordering.
     */
    fun read16(addr: UInt): UShort {
        return when (val masked = maskSegment(addr)) {
            in RAM_START..RAM_END -> readBuf16(this.ram, masked, RAM_START)
            in BIOS_START..BIOS_END -> readBuf16(this.bios, masked, BIOS_START)

            else -> {
                println("read16(0x${masked.toString(16)})")
                0U
            }
        }
    }

    /**
     * Reads a 32-bit value from the given memory address in little-endian
     * byte ordering.
     */
    fun read32(addr: UInt): UInt {
        return when (val masked = maskSegment(addr)) {
            in RAM_START..RAM_END -> readBuf32(this.ram, masked, RAM_START)
            in BIOS_START..BIOS_END -> readBuf32(this.bios, masked, BIOS_START)

            else -> {
                println("read32(0x${masked.toString(16)})")
                0U
            }
        }
    }

    /**
     * Writes an 8-bit value to the given memory address.
     */
    fun write8(addr: UInt, value: UByte) {
        when (val masked = maskSegment(addr)) {
            in RAM_START..RAM_END -> writeBuf8(
                this.ram,
                masked,
                RAM_START,
                value
            )

            else -> {
                println("write8(0x${addr.toString(16)}, 0x${value.toString(16)})")
            }
        }
    }

    /**
     * Writes a 16-bit value to a given memory address in little-endian
     * byte ordering.
     */
    fun write16(addr: UInt, value: UShort) {
        when (val masked = maskSegment(addr)) {
            in RAM_START..RAM_END -> writeBuf16(
                this.ram,
                masked,
                RAM_START,
                value
            )

            else -> {
                println("write16(0x${addr.toString(16)}, 0x${value.toString(16)})")
            }
        }
    }

    /**
     * Writes a 32-bit value to a given memory address in little-endian
     * byte ordering.
     */
    fun write32(addr: UInt, value: UInt) {
        when (val masked = maskSegment(addr)) {
            in RAM_START..RAM_END -> writeBuf32(
                this.ram,
                masked,
                RAM_START,
                value
            )

            else -> {
                println("write32(0x${addr.toString(16)}, 0x${value.toString(16)})")
            }
        }
    }
}
