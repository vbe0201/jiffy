package io.github.vbe0201.jiffy.jit.codegen

import io.github.vbe0201.jiffy.jit.codegen.impl.*
import io.github.vbe0201.jiffy.jit.codegen.jvm.BytecodeEmitter
import kotlin.system.exitProcess

/**
 * Dispatches an [InstructionMeta] to its translation routine.
 *
 * The resulting bytecode will be written to [BytecodeEmitter].
 */
fun dispatch(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    // TODO: Figure out a nicer way to handle invalid instructions.
    val kind = meta.insn.kind() ?: exitProcess(1)

    // Find the handler for the instruction and invoke it.
    val handler = instructionTable[kind.opcode.toInt()]
    return handler(meta, emitter)
}

private fun dispatchFunction(
    meta: InstructionMeta,
    emitter: BytecodeEmitter
): Status {
    // TODO: Figure out a mroe elegant way to handle invalid instructions.
    val func = meta.insn.function() ?: exitProcess(1)

    // Delegate to the corresponding entry in the functions table.
    val handler = functionTable[func.opcode.toInt()]
    return handler(meta, emitter)
}

private val instructionTable = arrayOf(
    ::dispatchFunction,
    ::b,
    ::j,
    ::jal,
    ::beq,
    ::bne,
    ::blez,
    ::bgtz,

    ::addi,
    ::addiu,
    ::slti,
    ::sltiu,
    ::andi,
    ::ori,
    ::xori,
    ::lui,

    ::cop0,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,

    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,

    ::lb,
    ::lh,
    ::unimplemented,
    ::lw,
    ::lbu,
    ::lhu,
    ::unimplemented,
    ::unimplemented,

    ::sb,
    ::sh,
    ::unimplemented,
    ::sw,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,

    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,

    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
).also { check(it.size == 64) }

private val functionTable = arrayOf(
    ::sll,
    ::unimplemented,
    ::srl,
    ::sra,
    ::sllv,
    ::unimplemented,
    ::srlv,
    ::srav,

    ::jr,
    ::jalr,
    ::unimplemented,
    ::unimplemented,
    ::syscall,
    ::`break`,
    ::unimplemented,
    ::unimplemented,

    ::mfhi,
    ::mthi,
    ::mflo,
    ::mtlo,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,

    ::mult,
    ::multu,
    ::div,
    ::divu,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,

    ::add,
    ::addu,
    ::sub,
    ::subu,
    ::and,
    ::or,
    ::xor,
    ::nor,

    ::unimplemented,
    ::unimplemented,
    ::slt,
    ::sltu,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,

    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,

    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
).also { check(it.size == 64) }
