package io.github.vbe0201.jiffy.dma

/** The DMA ports implemented in hardware. */
enum class Port {
    /** Macroblock decoder input. */
    MdecIn,

    /** Macroblock decoder output. */
    MdecOut,

    /** Graphics Processing Unit. */
    Gpu,

    /** CD-ROM drive. */
    CdRom,

    /** Sound Processing Unit. */
    Spu,

    /** Extension port. */
    Pio,

    /** Used to clear the ordering table. */
    Otc,
}
