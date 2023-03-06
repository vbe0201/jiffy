package io.github.vbe0201.jiffy.jit.codegen.impl

import io.github.vbe0201.jiffy.jit.codegen.InstructionMeta
import io.github.vbe0201.jiffy.jit.codegen.Status
import io.github.vbe0201.jiffy.jit.codegen.jvm.BytecodeEmitter
import io.github.vbe0201.jiffy.jit.codegen.jvm.JvmType
import io.github.vbe0201.jiffy.jit.codegen.jvm.Operand
import io.github.vbe0201.jiffy.jit.decoder.Instruction
import io.github.vbe0201.jiffy.utils.signExtend32

private inline fun computeLoadAddress(
    insn: Instruction,
    emitter: BytecodeEmitter
) {
    emitter.run {
        getGpr(insn.rs())
            .add { place(insn.imm().signExtend32().toInt()) }
    }
}

private inline fun loadMemory(
    ty: JvmType,
    insn: Instruction,
    emitter: BytecodeEmitter
): Operand {
    return emitter.loadBus(ty) {
        computeLoadAddress(insn, this)
    }
}

private inline fun storeMemory(
    insn: Instruction,
    emitter: BytecodeEmitter,
    crossinline op: BytecodeEmitter.() -> Operand,
) {
    return emitter.writeBus {
        computeLoadAddress(insn, this)
        op()
    }
}

/**
 * Generates the Load Byte (LB) instruction to the code buffer.
 */
fun lb(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    emitter.run {
        configureDelayedLoad(
            meta.insn.rt(),
            loadMemory(JvmType.BYTE, meta.insn, emitter).signExtend(JvmType.INT)
        )
    }

    return Status.FILL_LOAD_DELAY_SLOT
}

/**
 * Generates the Load Byte Unsigned (LBU) instruction to the
 * code buffer.
 */
fun lbu(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    emitter.run {
        configureDelayedLoad(
            meta.insn.rt(),
            loadMemory(JvmType.BYTE, meta.insn, emitter).zeroExtend(JvmType.INT)
        )
    }

    return Status.FILL_LOAD_DELAY_SLOT
}

/**
 * Generates the Load Halfword (LH) instruction to the code buffer.
 */
fun lh(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    emitter.run {
        configureDelayedLoad(
            meta.insn.rt(),
            loadMemory(
                JvmType.SHORT,
                meta.insn,
                emitter
            ).signExtend(JvmType.INT)
        )
    }

    return Status.FILL_LOAD_DELAY_SLOT
}

/**
 * Generates the Load Halfword Unsigned (LHU) instruction to the
 * code buffer.
 */
fun lhu(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    emitter.run {
        configureDelayedLoad(
            meta.insn.rt(),
            loadMemory(
                JvmType.SHORT,
                meta.insn,
                emitter
            ).zeroExtend(JvmType.INT)
        )
    }

    return Status.FILL_LOAD_DELAY_SLOT
}

/**
 * Generates the Load Word (LW) instruction to the code buffer.
 */
fun lw(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    emitter.configureDelayedLoad(
        meta.insn.rt(),
        loadMemory(JvmType.INT, meta.insn, emitter)
    )

    return Status.FILL_LOAD_DELAY_SLOT
}

/**
 * Generates the Store Byte (SB) instruction to the code buffer.
 */
fun sb(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    storeMemory(meta.insn, emitter) {
        getGpr(meta.insn.rt()).truncate(JvmType.BYTE)
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the Store Halfword (SH) instruction to the code buffer.
 */
fun sh(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    storeMemory(meta.insn, emitter) {
        getGpr(meta.insn.rt()).truncate(JvmType.SHORT)
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the Store Word (SW) instruction to the code buffer.
 */
fun sw(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    storeMemory(meta.insn, emitter) {
        getGpr(meta.insn.rt())
    }

    return Status.CONTINUE_BLOCK
}
