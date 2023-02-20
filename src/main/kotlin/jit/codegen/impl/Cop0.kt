package io.github.vbe0201.jiffy.jit.codegen.impl

import io.github.vbe0201.jiffy.jit.codegen.BytecodeEmitter
import io.github.vbe0201.jiffy.jit.codegen.Status
import io.github.vbe0201.jiffy.jit.decoder.Instruction
import kotlin.system.exitProcess

internal fun cop0(
    pc: UInt,
    insn: Instruction,
    emitter: BytecodeEmitter
): Status {
    return when (insn.copOpcode()) {
        0b0100U -> mtc0(pc, insn, emitter)

        // TODO: Figure out a better way to handle invalid instructions.
        else -> exitProcess(1)
    }
}

/**
 * Generates the Move To Coprocessor 0 (MTC0) instruction to the
 * code buffer.
 */
@Suppress("UNUSED_PARAMETER")
fun mtc0(pc: UInt, insn: Instruction, emitter: BytecodeEmitter): Status {
    emitter.setCop0Register {
        push(insn.rd())
        getGpr(insn.rt())
    }

    return Status.CONTINUE_BLOCK
}
