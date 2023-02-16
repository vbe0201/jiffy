package io.github.vbe0201.jiffy.jit.translation

/**
 * A compiled JIT block with metadata.
 */
data class Block(
    /**
     * The [Compiled] code for this block.
     */
    val code: Compiled,
    /**
     * The start address of this block in MIPS memory.
     */
    val start: UInt,
    /**
     * The code length of this block in MIPS memory, in bytes.
     */
    val len: UInt
)
