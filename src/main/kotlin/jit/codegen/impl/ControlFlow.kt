package io.github.vbe0201.jiffy.jit.codegen.impl

import io.github.vbe0201.jiffy.jit.codegen.BytecodeEmitter
import io.github.vbe0201.jiffy.jit.codegen.Status
import io.github.vbe0201.jiffy.jit.decoder.Instruction

/**
 * Emits a Jump (J) instruction to the code buffer.
 */
fun j(pc: UInt, insn: Instruction, emitter: BytecodeEmitter): Status {
    emitter.branch {
        // Mask the current program counter value.
        push(pc)
        iand(0xF000_0000U)

        // Compute the destination for the branch.
        ior(insn.target() shl 2)
    }

    return Status.FILL_DELAY_SLOT
}
