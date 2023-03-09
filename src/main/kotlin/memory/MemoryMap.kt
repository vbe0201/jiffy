package io.github.vbe0201.jiffy.memory

private const val KIB = 1 shl 10
private const val MIB = 1 shl 20

/** The static memory map of the MIPS processor. */
object MemoryMap {
    /** The memory range of Random Access Memory. */
    val RAM = MemoryRange(0x0000_0000U, (2 * MIB).toUInt())

    /** The memory range of Expansion Region 1. */
    val EXPANSION_1 = MemoryRange(0x1F00_0000U, MIB.toUInt())

    /** The D-Cache region used as Fast RAM ("Scratchpad"). */
    val SCRATCHPAD = MemoryRange(0x1F80_0000U, KIB.toUInt())

    /** The memory range of memory latency and expansion mapping. */
    val MEM_CONTROL = MemoryRange(0x1F80_1000U, 0x24U)

    /** The memory range of the gamepad and memory card controller. */
    val PAD_MEMCARD = MemoryRange(0x1F80_1040U, 0x20U)

    /** The memory range of the RAM size control register. */
    val RAM_SIZE = MemoryRange(0x1F80_1060U, 0x4U)

    /** The memory range of Interrupt Control registers. */
    val IRQ_CONTROL = MemoryRange(0x1F80_1070U, 0x8U)

    /** The memory range of Direct Memory Access registers. */
    val DMA = MemoryRange(0x1F80_1080U, 0x80U)

    /** The memory range of hardware timer registers. */
    val TIMERS = MemoryRange(0x1F80_1100U, 0x30U)

    /** The memory range of CD-ROM control registers. */
    val CDROM = MemoryRange(0x1F80_1800U, 0x4U)

    /** The memory range of GPU control registers. */
    val GPU = MemoryRange(0x1F80_1810U, 0x8U)

    /** The memory range of MDEC control registers. */
    val MDEC = MemoryRange(0x1F80_1820U, 0x8U)

    /** The memory range of Sound Processing Unit registers. */
    val SPU = MemoryRange(0x1F80_1C00U, 0x280U)

    /** The memory range of Expansion Region 2. */
    val EXPANSION_2 = MemoryRange(0x1F80_2000U, 0x42U)

    /** The memory range of static BIOS ROM. */
    val BIOS = MemoryRange(0x1FC0_0000U, (512 * KIB).toUInt())

    /** The memory range of the Cache Control register in KSEG2. */
    val CACHE_CONTROL = MemoryRange(0xFFFE_0130U, 0x4U)
}
