package io.github.vbe0201.jiffy.dma

/** The synchronization mode of a DMA transfer. */
enum class SyncMode {
    /** Transfer all at once when CPU writes the Trigger bit. */
    MANUAL,

    /** Synchronize blocks to DMA requests. */
    REQUEST,

    /** Transfer linked GPU commands list. */
    LINKED_LIST,
}
