package io.github.vbe0201.jiffy.jit.codegen

/**
 * The condition a [Conditional] reacts on.
 */
enum class Condition {
    /**
     * Compares two integers on the stack and runs the block
     * only when these are not equal.
     */
    INTS_NOT_EQUAL,

    /**
     * Compares two integers on the stack and runs the block
     * only when the first one is smaller than the second.
     */
    INT_SMALLER_THAN,

    /**
     * Compares two unsigned integers on the stack and runs the
     * block only when the first one is smaller than the second.
     */
    UNSIGNED_INT_SMALLER_THAN,

    /**
     * Checks an operand on the stack for smaller than zero and
     * runs the block only when that is the case.
     */
    SMALLER_THAN_ZERO,
}

/**
 * Composes conditional blocks of logic and emits corresponding code.
 *
 * Based on the checked [Condition], only [Conditional.then] xor
 * [Conditional.orElse] will be executed.
 */
data class Conditional(
    /**
     * The [Condition] type for the if block.
     */
    val cond: Condition
) {
    /**
     * The code that should be executed when the given [Condition]
     * is fulfilled.
     */
    var then: (BytecodeEmitter.() -> Unit)? = null

    /**
     * The code that should be executed when the given [Condition]
     * is not fulfilled.
     */
    var orElse: (BytecodeEmitter.() -> Unit)? = null
}
