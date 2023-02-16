package io.github.vbe0201.jiffy.jit.translation

import io.github.vbe0201.jiffy.jit.state.ExecutionContext

/**
 * Represents a block of callable code translated by the JIT.
 *
 * Implementors of [Compiled] are usually generated from raw Java Bytecode
 * built by the translator and loaded as an accessible class at runtime.
 */
interface Compiled {
    /**
     * Executes this code block on the given [ExecutionContext].
     */
    fun execute(context: ExecutionContext)
}
