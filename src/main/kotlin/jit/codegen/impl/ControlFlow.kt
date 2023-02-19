package io.github.vbe0201.jiffy.jit.codegen.impl

import io.github.vbe0201.jiffy.jit.codegen.BytecodeEmitter
import io.github.vbe0201.jiffy.jit.codegen.Status
import io.github.vbe0201.jiffy.jit.decoder.INSTRUCTION_SIZE
import io.github.vbe0201.jiffy.jit.decoder.Instruction
import io.github.vbe0201.jiffy.utils.signExtend32

private fun computeBranchTarget(pc: UInt, target: UShort): UInt {
    val offset = target.signExtend32() shl 2
    return pc + offset
}

/**
 * Emits a Jump (J) instruction to the code buffer.
 */
fun j(pc: UInt, insn: Instruction, emitter: BytecodeEmitter): Status {
    emitter.jump {
        // Mask the current program counter value.
        push(pc)
        iand(0xF000_0000U)

        // Compute the destination for the branch.
        ior(insn.target() shl 2)
    }

    return Status.FILL_DELAY_SLOT
}

/**
 * Emits a Branch Not Equal (BNE) instruction to the code buffer.
 */
fun bne(pc: UInt, insn: Instruction, emitter: BytecodeEmitter): Status {
    emitter.run {
        // Adjust the program counter past the branch and its delay slot.
        // This is so we don't get stuck on a block when branch is not taken.
        jump {
            push(pc + (INSTRUCTION_SIZE * 2U))
        }

        getGpr(insn.rs())
        getGpr(insn.rt())

        // When the registers are not equal, branch to the target.
        ifNotEqual {
            jump {
                push(computeBranchTarget(pc, insn.imm()))
            }
        }
    }

    return Status.FILL_DELAY_SLOT
}
