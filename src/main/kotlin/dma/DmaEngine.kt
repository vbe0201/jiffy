package io.github.vbe0201.jiffy.dma

import io.github.vbe0201.jiffy.memory.MemoryMap
import io.github.vbe0201.jiffy.memory.MemoryRange
import io.github.vbe0201.jiffy.memory.Peripheral
import io.github.vbe0201.jiffy.utils.alignDown
import io.github.vbe0201.jiffy.utils.putUInt
import java.nio.ByteBuffer

// The size of a DMA transfer word in bytes.
private const val WORD = UInt.SIZE_BYTES

// XXX: Taken from Nocash PSXSPX PlayStation specifications.
private const val DPCR_RESET_VALUE = 0x0765_4321U

/**
 * Implementation of the PSX Direct Memory Access Engine.
 *
 * The DMA Engine is a peripheral which sits on the bus and is
 * programmed through its hardware registers.
 *
 * It manages 7 DMA channels between processor RAM and various
 * peripherals, allowing for efficient transfers of data between
 * them without wasting CPU time on the task.
 */
class DmaEngine(private val ram: ByteBuffer) : Peripheral {
    /** Internal DMA [Channel] configurations for each port. */
    private val channels = arrayOf(
        Channel(Port.MdecIn),
        Channel(Port.MdecOut),
        Channel(Port.Gpu),
        Channel(Port.CdRom),
        Channel(Port.Spu),
        Channel(Port.Pio),
        Channel(Port.Otc),
    ).also { check(it.size == 7) }

    /** The DMA Control Register (DPCR) value. */
    private var dpcr = 0U

    /** The DMA interrupt register (DICR) value. */
    private var dicr = 0U

    init {
        // Put the DMA engine into proper reset state when creating it.
        this.reset()
    }

    /** Puts the DMA engine back into reset state. */
    fun reset() {
        // Reset the DMA channels.
        for (channel in this.channels) {
            channel.reset()
        }

        // Reset the engine block.
        this.dpcr = DPCR_RESET_VALUE
        this.dicr = 0U
    }

    private inline fun maskAddress(addr: UInt): UInt {
        // Ensure an address wraps around in MIPS RAM (which starts
        // at address 0) and is also word-aligned for 32-bit access.
        val mask = MemoryMap.RAM.size - 1U
        return addr.and(mask).alignDown(WORD.toUInt())
    }

    private fun transferPeripheralToMemory(
        channel: Channel,
        address: UInt,
        step: Int,
        words: Int,
    ) {
        var address = address

        // Clear the ordering table, if requested.
        if (channel.port == Port.Otc) {
            // Write pointers to previous entry until we reach the last one.
            for (i in 0..words - 2) {
                val previous = this.maskAddress(address - WORD.toUInt())
                this.ram.putUInt(address.toInt(), previous)
                address = previous
            }

            // For the last entry, we put a special termination marker.
            val terminator = 0x00FF_FFFFU
            this.ram.putUInt(address.toInt(), terminator)

            return
        }

        TODO("other peripherals not yet implemented")
    }

    private fun performBlockTransfer(channel: Channel) {
        var address = channel.baseAddress.toInt()
        var count = channel.transferWordCount().toInt()
        val step = when (channel.step()) {
            AddressStep.INCREMENT -> WORD
            AddressStep.DECREMENT -> -WORD
        }

        while (count > 0) {
            val maskedAddr = this.maskAddress(address.toUInt())

            when (channel.direction()) {
                TransferDirection.FROM_RAM -> TODO()
                TransferDirection.TO_RAM -> this.transferPeripheralToMemory(
                    channel,
                    maskedAddr,
                    step,
                    count
                )
            }

            address += step
            --count
        }
    }

    private fun performTransfer(channel: Channel) {
        when (channel.sync()) {
            SyncMode.MANUAL -> this.performBlockTransfer(channel)

            else -> TODO("other DMA modes not yet implemented")
        }

        channel.finishTransfer()
    }

    override fun mapping(): MemoryRange {
        return MemoryMap.DMA
    }

    override fun read8(register: Int): UByte {
        TODO("Not yet implemented")
    }

    override fun read16(register: Int): UShort {
        TODO("Not yet implemented")
    }

    override fun read32(register: Int): UInt {
        val major = register.ushr(4).and(0b111)
        val minor = register.and(0b1111)

        return when (major) {
            in 0..6 -> {
                val channel = this.channels[major]
                when (minor) {
                    0 -> channel.baseAddress
                    4 -> channel.blockControl
                    8 -> channel.channelControl

                    // FIXME: Any registers mapped?
                    else -> 0xFFFF_FFFFU
                }
            }

            else -> when (minor) {
                0 -> this.dpcr
                4 -> this.dicr

                // FIXME: Any registers mapped?
                else -> 0xFFFF_FFFFU
            }
        }
    }

    override fun write8(register: Int, value: UByte) {
        TODO("Not yet implemented")
    }

    override fun write16(register: Int, value: UShort) {
        TODO("Not yet implemented")
    }

    override fun write32(register: Int, value: UInt) {
        val major = register.ushr(4).and(0b111)
        val minor = register.and(0b1111)

        when (major) {
            in 0..6 -> {
                val channel = this.channels[major]
                when (minor) {
                    0 -> channel.baseAddress = value
                    4 -> channel.blockControl = value
                    8 -> channel.channelControl = value
                }

                if (channel.active()) {
                    this.performTransfer(channel)
                }
            }

            else -> when (minor) {
                0 -> this.dpcr = value
                4 -> this.dicr = value
            }
        }
    }
}
