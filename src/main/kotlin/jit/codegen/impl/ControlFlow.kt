package io.github.vbe0201.jiffy.jit.codegen.impl

import io.github.vbe0201.jiffy.jit.codegen.InstructionMeta
import io.github.vbe0201.jiffy.jit.codegen.Status
import io.github.vbe0201.jiffy.jit.codegen.jvm.BytecodeEmitter
import io.github.vbe0201.jiffy.jit.codegen.jvm.Condition
import io.github.vbe0201.jiffy.jit.decoder.INSTRUCTION_SIZE
import io.github.vbe0201.jiffy.jit.decoder.RA
import io.github.vbe0201.jiffy.utils.*

// Local variable slots in the generated function; temporarily
// occupied for implementation details of individual instructions.
private const val TEMP_BRANCH_SLOT = 4

private inline fun computeBranchTarget(pc: UInt, target: UShort): Int {
    val offset = target.signExtend32() shl 2
    return (pc + offset).toInt()
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
            jump { place((meta.pc + INSTRUCTION_SIZE).toInt()) }
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
 * Emits a conditional branch instruction (Bxx) to the code buffer.
 */
fun b(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    val cond = (meta.insn.raw shr 16) and 1U
    val link = (meta.insn.raw shr 17) and 0xFU == 8U

    emitter.run {
        // First, check if the source register is "less than zero".
        // This can be conveniently read from the sign bit value.
        //
        // If the condition is "BGEZ", we need to negate the comparison
        // since ("a >= 0" <=> "!(a < 0)"). XOR takes care of that.
        val test = getGpr(meta.insn.rs())
            .ushr { place(Int.SIZE_BITS - 1) }
            .xor { place(cond.toInt()) }
            .storeLocal(TEMP_BRANCH_SLOT)

        // Store the return address in `$ra`, if requested.
        if (link) {
            setGpr(RA) { place((meta.pc + INSTRUCTION_SIZE).toInt()) }
        }

        // Branch when requested.
        test.loadLocal(TEMP_BRANCH_SLOT)
        branchConditional(Condition.INT_NOT_ZERO, meta, emitter)
    }

    return Status.FILL_BRANCH_DELAY_SLOT
}

/**
 * Generates the Jump And Link (JAL) instruction to the code buffer.
 */
fun jal(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    // Store the return address in the `$ra` register.
    emitter.setGpr(RA) {
        place((meta.pc + INSTRUCTION_SIZE).toInt())
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
        // Jump to the destination register.
        jump {
            getGpr(meta.insn.rs())
        }

        // Store the return address in the selected register.
        setGpr(meta.insn.rd()) {
            place((meta.pc + INSTRUCTION_SIZE).toInt())
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
