package io.github.vbe0201.jiffy.cpu

/**
 * Exception types which may occur during CPU execution.
 */
enum class ExceptionKind(val type: UInt) {
    /**
     * A load was attempted from an unaligned memory address
     * where not permitted.
     */
    UNALIGNED_LOAD(0x4U),

    /**
     * A store was attempted to an unaligned memory address
     * where not permitted.
     */
    UNALIGNED_STORE(0x5U),

    /**
     * A system call was triggered by the syscall instruction.
     */
    SYSCALL(0x8U),

    /**
     * An arithmetic overflow has occurred.
     */
    OVERFLOW(0xCU),
}
