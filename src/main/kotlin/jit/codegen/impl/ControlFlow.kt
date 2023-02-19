package io.github.vbe0201.jiffy.jit.codegen.impl

import io.github.vbe0201.jiffy.jit.codegen.BytecodeEmitter
import io.github.vbe0201.jiffy.jit.codegen.Condition
import io.github.vbe0201.jiffy.jit.codegen.Status
import io.github.vbe0201.jiffy.jit.decoder.INSTRUCTION_SIZE
import io.github.vbe0201.jiffy.jit.decoder.Instruction
import io.github.vbe0201.jiffy.utils.signExtend32

private fun computeBranchTarget(pc: UInt, target: UShort): UInt {
    val offset = target.signExtend32() shl 2
    return pc + offset
}

/**
 * Generates the Jump (J) instruction to the code buffer.
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
 * Generates the Branch Not Equal (BNE) instruction to the code buffer.
 */
fun bne(pc: UInt, insn: Instruction, emitter: BytecodeEmitter): Status {
    emitter.run {
        getGpr(insn.rs())
        getGpr(insn.rt())

        // Check the operand registers for equality.
        conditional(Condition.INTS_NOT_EQUAL) {
            // When the registers are not equal, branch to the target.
            then = {
                jump {
                    push(computeBranchTarget(pc, insn.imm()))
                }
            }

            // Otherwise, adjust the PC past the branch and its delay slot.
            orElse = {
                jump {
                    push(pc + (INSTRUCTION_SIZE * 2U))
                }
            }
        }
    }

    return Status.FILL_DELAY_SLOT
}
