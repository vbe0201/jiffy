package io.github.vbe0201.jiffy.jit.codegen.jvm

/**
 * Condition codes a [Conditional] reacts on.
 */
enum class Condition {
    /**
     * Compares two integer operands on the stack and runs a block
     * only when first and second are equal.
     */
    INTS_EQUAL,

    /**
     * Compares two integer operands on the stack and runs a block
     * only when first and second are not equal.
     */
    INTS_NOT_EQUAL,

    /**
     * Compares two integer operands on the stack and runs a block
     * only when first is smaller than second.
     */
    COMPARE_SMALLER_THAN,

    /**
     * Compares two integer operands on the stack and runs a block
     * only when first is smaller than second.
     *
     * The integers are treated as unsigned values.
     */
    COMPARE_UNSIGNED_SMALLER_THAN,

    /**
     * Tests an integer operand for smaller than zero and
     * runs a block only when that is the case.
     */
    INT_SMALLER_THAN_ZERO,

    /**
     * Tests an integer operand for smaller or equal to zero and
     * runs a block only when that is the case.
     */
    INT_SMALLER_OR_EQUAL_ZERO,

    /**
     * Tests an integer operand for greater than zero and
     * runs a block only when that is the case.
     */
    INT_GREATER_THAN_ZERO,
}

/**
 * Emits conditional code based on a given [Condition] code.
 */
data class Conditional(val cond: Condition) {
    /**
     * Codegen callback for when a [Condition] is met.
     */
    val then: (BytecodeEmitter.() -> Unit)? = null

    /**
     * Optional codegen callback for when a [Condition] failed.
     */
    val orElse: (BytecodeEmitter.() -> Unit)? = null
}