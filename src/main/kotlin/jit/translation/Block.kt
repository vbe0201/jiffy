package io.github.vbe0201.jiffy.jit.translation

import io.github.vbe0201.jiffy.jit.state.ExecutionContext

/**
 * A compiled JIT block with metadata.
 */
data class Block(
    /**
     * The [ExecutionContext] of the JIT.
     *
     * Shared amongst all [Block]s.
     */
    val ctx: ExecutionContext,
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
) {
    /**
     * Executes the code of this [Block].
     */
    fun run() = this.code.execute(this.ctx)
}
