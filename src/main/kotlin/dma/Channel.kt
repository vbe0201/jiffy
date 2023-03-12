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
    /** The DMA [Port] associated with the channel. */
    val port: Port,
    /** The channel control register of the [Channel]. */
    var channelControl: UInt,
    /** The block control register of the [Channel]. */
    var blockControl: UInt,
) {
    /** Constructs a DMA channel in its empty state. */
    constructor(port: Port) : this(port, 0U, 0U)

    /** The 24-bit base address this DMA channel is configured to. */
    var baseAddress = 0U
        set(addr) {
            // Truncate address to reachable range.
            field = addr and 0x00FF_FFFFU
        }

    /** Puts the channel back into reset state. */
    fun reset() {
        this.channelControl = 0U
        this.blockControl = 0U
        this.baseAddress = 0U
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
        return when (this.channelControl.shr(9).and(0x3U)) {
            0U -> SyncMode.MANUAL
            1U -> SyncMode.REQUEST
            2U -> SyncMode.LINKED_LIST

            else -> throw AssertionError("unknown DMA sync mode")
        }
    }

    /** Indicates whether a DMA transfer is currently active. */
    fun active(): Boolean {
        val enable = this.channelControl.shr(24).and(1U)

        return when (this.sync()) {
            SyncMode.MANUAL -> {
                val trigger = this.channelControl.shr(28).and(1U)
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

    /** Updates the state of this channel when a DMA transfer is finished. */
    fun finishTransfer() {
        // Clear 'enable' and 'trigger' bits on channel control.
        val mask = (1U shl 24) or (1U shl 28)
        this.channelControl = this.channelControl.and(mask.inv())
    }
}
