package io.github.vbe0201.jiffy.jit.codegen.impl

import io.github.vbe0201.jiffy.jit.codegen.BytecodeEmitter
import io.github.vbe0201.jiffy.jit.codegen.Status
import io.github.vbe0201.jiffy.jit.decoder.Instruction
import io.github.vbe0201.jiffy.utils.signExtend32

/**
 * Generates the Load Byte (LB) instruction to the code buffer.
 */
@Suppress("UNUSED_PARAMETER")
fun lb(pc: UInt, insn: Instruction, emitter: BytecodeEmitter): Status {
    emitter.loadBusDelayed8(insn.rt()) {
        // Compute the memory address to read from.
        getGpr(insn.rs())
        iadd(insn.imm().signExtend32())
    }

    return Status.FILL_LOAD_DELAY_SLOT
}

/**
 * Generates the Load Word (LW) instruction to the code buffer.
 */
@Suppress("UNUSED_PARAMETER")
fun lw(pc: UInt, insn: Instruction, emitter: BytecodeEmitter): Status {
    emitter.loadBusDelayed32(insn.rt()) {
        // Compute the memory address to read from.
        getGpr(insn.rs())
        iadd(insn.imm().signExtend32())
    }

    return Status.FILL_LOAD_DELAY_SLOT
}

/**
 * Generates the Store Byte (SB) instruction to the code buffer.
 */
@Suppress("UNUSED_PARAMETER")
fun sb(pc: UInt, insn: Instruction, emitter: BytecodeEmitter): Status {
    emitter.writeBus8 {
        // Compute the memory address to write to.
        getGpr(insn.rs())
        iadd(insn.imm().signExtend32())

        // Prepare the value to write.
        getGpr(insn.rt())
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the Store Halfword (SH) instruction to the code buffer.
 */
@Suppress("UNUSED_PARAMETER")
fun sh(pc: UInt, insn: Instruction, emitter: BytecodeEmitter): Status {
    emitter.writeBus16 {
        // Compute the memory address to write to.
        getGpr(insn.rs())
        iadd(insn.imm().signExtend32())

        // Prepare the value to write.
        getGpr(insn.rt())
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the Store Word (SW) instruction to the code buffer.
 */
@Suppress("UNUSED_PARAMETER")
fun sw(pc: UInt, insn: Instruction, emitter: BytecodeEmitter): Status {
    emitter.writeBus32 {
        // Compute the memory address to write to.
        getGpr(insn.rs())
        iadd(insn.imm().signExtend32())

        // Prepare the value to write.
        getGpr(insn.rt())
    }

    return Status.CONTINUE_BLOCK
}
