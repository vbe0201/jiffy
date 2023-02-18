package io.github.vbe0201.jiffy.jit.codegen

/**
 * The status of code translation; polled after every processed
 * instruction.
 *
 * [Status]es provide a way for instructions to provide additional
 * context to the [BlockBuilder] on their own.
 */
enum class Status {
    /**
     * The block will consume more instructions until a different
     * [Status] occurs.
     */
    CONTINUE_BLOCK,

    /**
     * The block ends, but the delay slot needs to be filled with
     * the next instruction.
     */
    FILL_DELAY_SLOT,

    /**
     * Terminates a block without any additional side effects.
     */
    TERMINATE_BLOCK,
}
