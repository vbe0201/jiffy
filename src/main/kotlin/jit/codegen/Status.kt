package io.github.vbe0201.jiffy.jit.codegen

/**
 * The status of code translation; polled after every processed
 * instruction.
 *
 * [Status]es provide a way for instructions to provide additional
 * context to the [BlockBuilder] on their own.
 */
enum class Status(private val mask: Int) {
    /**
     * The block will consume more instructions until a different
     * [Status] occurs.
     */
    CONTINUE_BLOCK(0b0001),

    /**
     * The block will consume more instructions, but the next
     * instruction needs to fill the load delay slot.
     */
    FILL_LOAD_DELAY_SLOT(0b0010),

    /**
     * The block ends, but the delay slot needs to be filled with
     * the next instruction.
     */
    FILL_BRANCH_DELAY_SLOT(0b0100),

    /**
     * Terminates a block without any additional side effects.
     */
    TERMINATE_BLOCK(0b1000);

    /**
     * Indicates whether the block is still taking more instructions in.
     */
    fun blockOpen(): Boolean = (this.mask and 0b0011) != 0

    /**
     * Indicates whether an instruction caused a delay slot.
     */
    fun delaySlot(): Boolean = (this.mask and 0b0110) != 0
}
