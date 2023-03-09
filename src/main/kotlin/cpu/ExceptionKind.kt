package io.github.vbe0201.jiffy.cpu

/** Exception types which may occur during CPU execution. */
enum class ExceptionKind(val raw: UInt) {
    /** An interrupt request was issued. */
    INTERRUPT(0x0U),

    /** A load was attempted from an unaligned memory address. */
    UNALIGNED_LOAD(0x4U),

    /** A store was attempted to an unaligned memory address. */
    UNALIGNED_STORE(0x5U),

    /** A system call was triggered by the SYSCALL instruction. */
    SYSCALL(0x8U),

    /** A breakpoint was triggered by the BREAK instruction. */
    BREAK(0x9U),

    /** Execution landed on an illegal instruction. */
    ILLEGAL_INSTRUCTION(0xAU),

    /** An unsupported Coprocessor operation was triggered. */
    COPROCESSOR_ERROR(0xBU),

    /** An arithmetic overflow has occurred. */
    OVERFLOW(0xCU),
}
