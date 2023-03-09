package io.github.vbe0201.jiffy.jit.codegen.jvm

/** Condition codes a [Conditional] reacts on. */
enum class Condition {
    /**
     * Compares two integer operands on the stack and runs a block
     * only when first and second are equal.
     */
    COMPARE_EQUAL,

    /**
     * Compares two integer operands on the stack and runs a block
     * only when first and second are not equal.
     */
    COMPARE_NOT_EQUAL,

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
     * Tests an integer operand for equality to 0 and runs a
     * block only when that is the case.
     */
    INT_ZERO,

    /**
     * Tests an integer operand for equality to 0 and runs a
     * block only when that is not the case.
     */
    INT_NOT_ZERO,

    /**
     * Tests an integer operand for greater than zero and
     * runs a block only when that is the case.
     */
    INT_GREATER_THAN_ZERO,
}

/** Emits conditional code based on a given [Condition] code. */
data class Conditional(val cond: Condition) {
    /** Codegen callback for when a [Condition] is met. */
    var then: (BytecodeEmitter.() -> Unit)? = null

    /** Optional codegen callback for when a [Condition] failed. */
    var orElse: (BytecodeEmitter.() -> Unit)? = null
}
