package io.github.vbe0201.jiffy.jit.codegen.impl

import io.github.vbe0201.jiffy.jit.codegen.BytecodeEmitter
import io.github.vbe0201.jiffy.jit.codegen.Status
import io.github.vbe0201.jiffy.jit.decoder.Instruction
import io.github.vbe0201.jiffy.utils.*

/**
 * Generates the Add Immediate Unsigned (ADDIU) instruction to the
 * code buffer.
 */
@Suppress("UNUSED_PARAMETER")
fun addiu(pc: UInt, insn: Instruction, emitter: BytecodeEmitter): Status {
    emitter.setGpr(insn.rt()) {
        // NOTE: The "Unsigned" part in the instruction name is
        // misleading. The sign extension is intentional.
        getGpr(insn.rs())
        iadd(insn.imm().signExtend32())
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the Load Unsigned Immediate (LUI) instruction to the
 * code buffer.
 */
@Suppress("UNUSED_PARAMETER")
fun lui(pc: UInt, insn: Instruction, emitter: BytecodeEmitter): Status {
    emitter.setGpr(insn.rt()) {
        push(insn.imm().zeroExtend32() shl 16)
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the OR Immediate (ORI) instruction to the code buffer.
 */
@Suppress("UNUSED_PARAMETER")
fun ori(pc: UInt, insn: Instruction, emitter: BytecodeEmitter): Status {
    emitter.setGpr(insn.rt()) {
        getGpr(insn.rs())
        ior(insn.imm().zeroExtend32())
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the OR instruction to the code buffer.
 */
@Suppress("UNUSED_PARAMETER")
fun or(pc: UInt, insn: Instruction, emitter: BytecodeEmitter): Status {
    emitter.setGpr(insn.rd()) {
        getGpr(insn.rs())
        getGpr(insn.rt())
        ior(null)
    }

    return Status.CONTINUE_BLOCK
}
