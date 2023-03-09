package io.github.vbe0201.jiffy.dma

/** The step direction for memory addresses during a DMA transfer. */
enum class AddressStep {
    /** Memory addresses are incremented during a transfer. */
    INCREMENT,

    /** Memory addresses are decremented during a transfer. */
    DECREMENT,
}
