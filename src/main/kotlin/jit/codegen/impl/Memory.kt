package io.github.vbe0201.jiffy.jit.codegen.impl

import io.github.vbe0201.jiffy.jit.codegen.InstructionMeta
import io.github.vbe0201.jiffy.jit.codegen.Status
import io.github.vbe0201.jiffy.jit.codegen.jvm.BytecodeEmitter
import io.github.vbe0201.jiffy.jit.codegen.jvm.JvmType
import io.github.vbe0201.jiffy.jit.codegen.jvm.Operand
import io.github.vbe0201.jiffy.jit.decoder.Instruction
import io.github.vbe0201.jiffy.utils.*

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
    meta: InstructionMeta,
    emitter: BytecodeEmitter
): Operand {
    return emitter.loadBus(meta.exceptionPc(), meta.branchDelaySlot, ty) {
        computeLoadAddress(meta.insn, this)
    }
}

private inline fun storeMemory(
    meta: InstructionMeta,
    emitter: BytecodeEmitter,
    crossinline op: BytecodeEmitter.() -> Operand,
) {
    return emitter.writeBus(meta.exceptionPc(), meta.branchDelaySlot) {
        computeLoadAddress(meta.insn, this)
        op()
    }
}

/** Generates the Load Byte (LB) instruction to the code buffer. */
fun lb(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    emitter.run {
        configureDelayedLoad(
            meta.insn.rt(),
            loadMemory(JvmType.BYTE, meta, emitter).signExtend(JvmType.INT)
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
            loadMemory(JvmType.BYTE, meta, emitter).zeroExtend(JvmType.INT)
        )
    }

    return Status.FILL_LOAD_DELAY_SLOT
}

/** Generates the Load Halfword (LH) instruction to the code buffer. */
fun lh(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    emitter.run {
        configureDelayedLoad(
            meta.insn.rt(),
            loadMemory(JvmType.SHORT, meta, emitter).signExtend(JvmType.INT)
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
            loadMemory(JvmType.SHORT, meta, emitter).zeroExtend(JvmType.INT)
        )
    }

    return Status.FILL_LOAD_DELAY_SLOT
}

/** Generates the Load Word (LW) instruction to the code buffer. */
fun lw(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    emitter.configureDelayedLoad(
        meta.insn.rt(),
        loadMemory(JvmType.INT, meta, emitter)
    )

    return Status.FILL_LOAD_DELAY_SLOT
}

/** Generates the Store Byte (SB) instruction to the code buffer. */
fun sb(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    storeMemory(meta, emitter) {
        getGpr(meta.insn.rt()).truncate(JvmType.BYTE)
    }

    return Status.CONTINUE_BLOCK
}

/** Generates the Store Halfword (SH) instruction to the code buffer. */
fun sh(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    storeMemory(meta, emitter) {
        getGpr(meta.insn.rt()).truncate(JvmType.SHORT)
    }

    return Status.CONTINUE_BLOCK
}

/** Generates the Store Word (SW) instruction to the code buffer. */
fun sw(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    storeMemory(meta, emitter) {
        getGpr(meta.insn.rt())
    }

    return Status.CONTINUE_BLOCK
}
