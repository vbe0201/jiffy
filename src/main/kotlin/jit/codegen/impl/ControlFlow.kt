package io.github.vbe0201.jiffy.jit.codegen.impl

import io.github.vbe0201.jiffy.jit.codegen.BytecodeEmitter
import io.github.vbe0201.jiffy.jit.codegen.Status
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
        getGpr(insn.rs())
        getGpr(insn.rt())

        ifNotEqual {
            jump {
                push(computeBranchTarget(pc, insn.imm()))
            }
        }
    }

    return Status.FILL_DELAY_SLOT
}
