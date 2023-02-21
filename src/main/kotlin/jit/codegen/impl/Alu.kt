package io.github.vbe0201.jiffy.jit.codegen.impl

import io.github.vbe0201.jiffy.jit.codegen.BytecodeEmitter
import io.github.vbe0201.jiffy.jit.codegen.Condition
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
 * Generates the Add Immediate (ADDI) instruction to the code buffer.
 */
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
        conditional(Condition.SMALLER_THAN_ZERO) {
            then = {
                // TODO: Raise an exception for the overflow.
                generateUnimplementedStub()
            }

            orElse = {
                // When we were successful, write the sum to the register.
                setGpr(insn.rt()) {
                    loadLocal(sumSlot)
                }
            }
        }
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the Add Unsigned (ADDU) instruction to the code buffer.
 */
@Suppress("UNUSED_PARAMETER")
fun addu(pc: UInt, insn: Instruction, emitter: BytecodeEmitter): Status {
    emitter.setGpr(insn.rd()) {
        getGpr(insn.rs())
        getGpr(insn.rt())
        iadd(null)
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
 * Generates the AND Immediate (ANDI) instruction to the code buffer.
 */
@Suppress("UNUSED_PARAMETER")
fun andi(pc: UInt, insn: Instruction, emitter: BytecodeEmitter): Status {
    emitter.setGpr(insn.rt()) {
        getGpr(insn.rs())
        iand(insn.imm().zeroExtend32())
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

/**
 * Generates the Set on Less Than Unsigned (SLTU) instruction to
 * the code buffer.
 */
@Suppress("UNUSED_PARAMETER")
fun sltu(pc: UInt, insn: Instruction, emitter: BytecodeEmitter): Status {
    emitter.setGpr(insn.rd()) {
        getGpr(insn.rs())
        getGpr(insn.rt())

        conditional(Condition.UNSIGNED_INT_SMALLER_THAN) {
            then = { push(1U) }
            orElse = { push(0U) }
        }
    }

    return Status.CONTINUE_BLOCK
}
