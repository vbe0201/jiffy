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

@Suppress("UNUSED_PARAMETER")
fun addi(pc: UInt, insn: Instruction, emitter: BytecodeEmitter): Status {
    val imm = insn.imm().signExtend32()

    // Compute the sum of both registers and store as a local variable.
    val sumSlot = emitter.run {
        getGpr(insn.rs())
        iadd(imm)
        storeLocal()
    }

    // Check if an overflow has occurred. This is the case when both
    // operands have a different sign than the result.
    emitter.run {
        loadLocal(sumSlot)
        getGpr(insn.rs())
        ixor(null)

        loadLocal(sumSlot)
        ixor(imm)

        iand(null)
        ifSmallerThanZero {
            // TODO: Raise an exception for the overflow.
            generateUnimplementedStub()
        }
    }

    // When we were successful, write the sum to the target register.
    emitter.setGpr(insn.rt()) {
        loadLocal(sumSlot)
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
