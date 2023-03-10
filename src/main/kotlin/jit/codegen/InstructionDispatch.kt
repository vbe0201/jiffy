package io.github.vbe0201.jiffy.jit.codegen

import io.github.vbe0201.jiffy.jit.codegen.impl.*
import io.github.vbe0201.jiffy.jit.codegen.jvm.BytecodeEmitter

/**
 * Dispatches an [InstructionMeta] to its translation routine.
 *
 * The resulting bytecode will be written to [BytecodeEmitter].
 */
fun dispatch(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    // Identify the next instruction or generate an exception.
    val kind = meta.insn.kind() ?: return invalid(meta, emitter)

    // Find the handler for the instruction and invoke it.
    val handler = instructionTable[kind.opcode.toInt()]
    return handler(meta, emitter)
}

private fun dispatchFunction(
    meta: InstructionMeta,
    emitter: BytecodeEmitter,
): Status {
    // Identify the next instruction or generate an exception.
    val func = meta.insn.function() ?: return invalid(meta, emitter)

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
    ::cop,
    ::unimplemented,
    ::cop,
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

    ::lwc,
    ::lwc,
    ::unimplemented,
    ::lwc,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,

    ::swc,
    ::swc,
    ::unimplemented,
    ::swc,
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
