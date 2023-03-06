package io.github.vbe0201.jiffy.jit.codegen.impl

import io.github.vbe0201.jiffy.jit.codegen.InstructionMeta
import io.github.vbe0201.jiffy.jit.codegen.Status
import io.github.vbe0201.jiffy.jit.codegen.jvm.BytecodeEmitter
import io.github.vbe0201.jiffy.jit.codegen.jvm.Condition
import io.github.vbe0201.jiffy.jit.decoder.INSTRUCTION_SIZE
import io.github.vbe0201.jiffy.jit.decoder.RA
import io.github.vbe0201.jiffy.utils.signExtend32

private inline fun computeBranchTarget(pc: UInt, target: UShort): Int {
    val offset = target.signExtend32() shl 2
    return ((pc + INSTRUCTION_SIZE) + offset).toInt()
}

private inline fun branchConditional(
    cond: Condition,
    meta: InstructionMeta,
    emitter: BytecodeEmitter
) {
    emitter.conditional(cond) {
        // When the condition is fulfilled, branch to the target.
        then = {
            jump {
                place(computeBranchTarget(meta.pc, meta.insn.imm()))
            }
        }

        // Otherwise, adjust the PC past the branch and its delay slot.
        orElse = {
            jump { place((meta.pc + INSTRUCTION_SIZE * 2U).toInt()) }
        }
    }
}

/**
 * Generates the Jump (J) instruction to the code buffer.
 */
fun j(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    emitter.jump {
        val offset = meta.insn.target() shl 2
        val target = (meta.pc and 0xF000_0000U) or offset

        place(target.toInt())
    }

    return Status.FILL_BRANCH_DELAY_SLOT
}

/**
 * Generates the Jump And Link (JAL) instruction to the code buffer.
 */
fun jal(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    // Store the return address in the `$ra` register.
    emitter.setGpr(RA) {
        place((meta.pc + INSTRUCTION_SIZE * 2U).toInt())
    }

    // Jump to the destination.
    return j(meta, emitter)
}

/**
 * Generates the Jump and Link Register (JALR) instruction to the
 * code buffer.
 */
fun jalr(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    emitter.run {
        // Store the return address in the selected register.
        setGpr(meta.insn.rd()) {
            place((meta.pc + INSTRUCTION_SIZE * 2U).toInt())
        }

        // Jump to the destination register.
        jump {
            getGpr(meta.insn.rs())
        }
    }

    return Status.FILL_BRANCH_DELAY_SLOT
}

/**
 * Generates the Jump Register (JR) instruction to the code buffer.
 */
fun jr(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    // Jump to the destination register.
    emitter.jump {
        getGpr(meta.insn.rs())
    }

    return Status.FILL_BRANCH_DELAY_SLOT
}

/**
 * Generates the Branch Equal (BEQ) instruction to the code buffer.
 */
fun beq(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    emitter.run {
        getGpr(meta.insn.rs())
        getGpr(meta.insn.rt())
        branchConditional(Condition.COMPARE_EQUAL, meta, this)
    }

    return Status.FILL_BRANCH_DELAY_SLOT
}

/**
 * Generates the Branch Not Equal (BNE) instruction to the code buffer.
 */
fun bne(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    emitter.run {
        getGpr(meta.insn.rs())
        getGpr(meta.insn.rt())
        branchConditional(Condition.COMPARE_NOT_EQUAL, meta, this)
    }

    return Status.FILL_BRANCH_DELAY_SLOT
}

/**
 * Generates the Branch if Greater Than Zero (BGTZ) instruction to
 * the code buffer.
 */
fun bgtz(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    emitter.run {
        getGpr(meta.insn.rs())
        branchConditional(Condition.INT_GREATER_THAN_ZERO, meta, this)
    }

    return Status.FILL_BRANCH_DELAY_SLOT
}

/**
 * Generates the Branch if Less or Equal Zero (BLEZ) instruction to
 * the code buffer.
 */
fun blez(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    emitter.run {
        getGpr(meta.insn.rs())
        branchConditional(Condition.INT_SMALLER_OR_EQUAL_ZERO, meta, this)
    }

    return Status.FILL_BRANCH_DELAY_SLOT
}
