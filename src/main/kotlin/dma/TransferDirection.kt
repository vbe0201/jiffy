package io.github.vbe0201.jiffy.dma

/** The direction of a DMA transfer. */
enum class TransferDirection {
    /** Transferring from external peripheral to RAM. */
    TO_RAM,

    /** Transferring from RAM to external peripheral. */
    FROM_RAM,
}
