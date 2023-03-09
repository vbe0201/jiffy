package io.github.vbe0201.jiffy.jit.decoder

/** Representation of the `$zero` register. */
val ZERO = Register(0)

/** Representation of the `$ra` register. */
val RA = Register(31)

/** Wrapper around a register index for type safety. */
@JvmInline
value class Register(val index: Int)
