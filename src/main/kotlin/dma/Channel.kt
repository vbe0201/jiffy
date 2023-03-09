package io.github.vbe0201.jiffy.dma

/**
 * Representation of a DMA channel configuration.
 *
 * The PSX features 7 DMA ports, each with an individual [Channel]
 * configuration managed by this type. A DMA channel is always set
 * up between a peripheral and the RAM. There's no direct path to
 * transfer between peripherals.
 */
class Channel(
    private val channelControl: UInt,
    private val blockControl: UInt,
) {
    /** The 24-bit base address this DMA channel is configured to. */
    var baseAddress = 0U
        set(addr) {
            // Truncate address to reachable range.
            field = addr and 0x00FF_FFFFU
        }

    /** Gets the [TransferDirection] configured on this channel. */
    fun direction(): TransferDirection {
        if (this.channelControl.and(1U) != 0U) {
            return TransferDirection.FROM_RAM
        } else {
            return TransferDirection.TO_RAM
        }
    }

    /** Gets the [AddressStep] configured on this channel. */
    fun step(): AddressStep {
        if (this.channelControl.and(1U shl 1) != 0U) {
            return AddressStep.DECREMENT
        } else {
            return AddressStep.INCREMENT
        }
    }

    /** Gets the [SyncMode] configured on this channel. */
    fun sync(): SyncMode {
        return when (this.channelControl.and(3U shl 9)) {
            0U -> SyncMode.MANUAL
            1U -> SyncMode.REQUEST
            2U -> SyncMode.LINKED_LIST

            else -> throw AssertionError("unknown DMA sync mode")
        }
    }

    /** Indicates whether a DMA transfer is currently active. */
    fun active(): Boolean {
        val enable = this.channelControl.and(1U shl 24)

        return when (this.sync()) {
            SyncMode.MANUAL -> {
                val trigger = this.channelControl.and(1U shl 28)
                enable.and(trigger) != 0U
            }

            else -> enable != 0U
        }
    }

    /** Gets the number of words being transferred on this channel. */
    fun transferWordCount(): UInt {
        return when (this.sync()) {
            SyncMode.MANUAL -> {
                val wordCount = this.blockControl and 0xFFFFU

                wordCount
            }

            SyncMode.REQUEST -> {
                val blockSize = this.blockControl and 0xFFFFU
                val blockCount = this.blockControl shr 16

                blockSize * blockCount
            }

            else -> throw AssertionError("invalid DMA sync mode")
        }
    }
}
