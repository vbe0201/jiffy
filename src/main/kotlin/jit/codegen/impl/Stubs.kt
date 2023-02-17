package io.github.vbe0201.jiffy.jit.codegen.impl

import io.github.vbe0201.jiffy.jit.codegen.BytecodeEmitter
import io.github.vbe0201.jiffy.jit.decoder.Instruction

/**
 * Emits a handler stub for unimplemented instructions.
 *
 * This should only be used during prototyping.
 */
@Suppress("UNUSED_PARAMETER")
fun unimplemented(
    pc: UInt,
    insn: Instruction,
    emitter: BytecodeEmitter
): Boolean {
    println("Unimplemented instruction: ${insn.kind()}")

    emitter.generateUnimplementedStub()
    return false
}
