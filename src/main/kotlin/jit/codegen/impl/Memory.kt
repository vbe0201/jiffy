package io.github.vbe0201.jiffy.jit.codegen.impl

import io.github.vbe0201.jiffy.jit.codegen.BytecodeEmitter
import io.github.vbe0201.jiffy.jit.decoder.Instruction
import io.github.vbe0201.jiffy.utils.signExtend32

/**
 * Generates the Store Word (SW) instruction to the code buffer.
 */
@Suppress("UNUSED_PARAMETER")
fun sw(pc: UInt, insn: Instruction, emitter: BytecodeEmitter): Boolean {
    emitter.writeBus32 {
        // Compute the memory address to write to.
        getGpr(insn.rs())
        iadd(insn.imm().signExtend32())

        // Prepare the value to write.
        getGpr(insn.rt())
    }

    return true
}
