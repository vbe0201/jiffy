package io.github.vbe0201.jiffy.jit.codegen.impl

import io.github.vbe0201.jiffy.jit.codegen.BytecodeEmitter
import io.github.vbe0201.jiffy.jit.codegen.Status
import io.github.vbe0201.jiffy.jit.decoder.Instruction

/**
 * Emits a handler stub for unimplemented instructions.
 *
 * This should only be used during prototyping.
 */
fun unimplemented(
    pc: UInt,
    insn: Instruction,
    emitter: BytecodeEmitter
): Status {
    println("[0x${pc.toString(16)}]: ${insn.kind()} unimplemented!")

    emitter.generateUnimplementedStub()
    return Status.TERMINATE_BLOCK
}
