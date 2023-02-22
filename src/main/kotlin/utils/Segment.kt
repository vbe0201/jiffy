package io.github.vbe0201.jiffy.utils

// The highest 3 bits of an address encode the address space in
// 512MiB chunks of memory. We use this as a lookup table.
private val segmentMasks = arrayOf(
    // KUSEG: 2048MiB of memory.
    0xFFFF_FFFFU, 0xFFFF_FFFFU, 0xFFFF_FFFFU, 0xFFFF_FFFFU,
    // KSEG0: 512MiB of memory.
    0x7FFF_FFFFU,
    // KSEG1: 512MiB of memory.
    0x1FFF_FFFFU,
    // KSEG2: 1024MiB of memory.
    0xFFFF_FFFFU, 0xFFFF_FFFFU,
).also { check(it.size == 8) }

/**
 * Masks a memory address to remove its segment bits and returns the
 * new address.
 */
fun maskSegment(addr: UInt): UInt {
    val mask = segmentMasks[(addr shr 29).toInt()]
    return addr and mask
}
