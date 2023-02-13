package io.github.vbe0201.jiffy.jit.decoder

private val instructionTable = arrayOf(
    InstructionKind.SPECIAL,
    InstructionKind.B,
    InstructionKind.J,
    InstructionKind.JAL,
    InstructionKind.BEQ,
    InstructionKind.BNE,
    InstructionKind.BLEZ,
    InstructionKind.BGTZ,

    InstructionKind.ADDI,
    InstructionKind.ADDIU,
    InstructionKind.SLTI,
    InstructionKind.SLTIU,
    InstructionKind.ANDI,
    InstructionKind.ORI,
    InstructionKind.XORI,
    InstructionKind.LUI,

    InstructionKind.COP0,
    InstructionKind.COP1,
    InstructionKind.COP2,
    InstructionKind.COP3,
    null,
    null,
    null,
    null,

    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,

    InstructionKind.LB,
    InstructionKind.LH,
    InstructionKind.LWL,
    InstructionKind.LW,
    InstructionKind.LBU,
    InstructionKind.LHU,
    InstructionKind.LWR,
    null,

    InstructionKind.SB,
    InstructionKind.SH,
    InstructionKind.SWL,
    InstructionKind.SW,
    null,
    null,
    InstructionKind.SWR,
    null,

    InstructionKind.LWC0,
    InstructionKind.LWC1,
    InstructionKind.LWC2,
    InstructionKind.LWC3,
    null,
    null,
    null,
    null,

    InstructionKind.SWC0,
    InstructionKind.SWC1,
    InstructionKind.SWC2,
    InstructionKind.SWC3,
    null,
    null,
    null,
    null,
).also { check(it.size == 64) }

private val functionTable = arrayOf(
    FunctionKind.SLL,
    null,
    FunctionKind.SRL,
    FunctionKind.SRA,
    FunctionKind.SLLV,
    null,
    FunctionKind.SRLV,
    FunctionKind.SRAV,

    FunctionKind.JR,
    FunctionKind.JALR,
    null,
    null,
    FunctionKind.SYSCALL,
    FunctionKind.BREAK,
    null,
    null,

    FunctionKind.MFHI,
    FunctionKind.MTHI,
    FunctionKind.MFLO,
    FunctionKind.MTLO,
    null,
    null,
    null,
    null,

    FunctionKind.MULT,
    FunctionKind.MULTU,
    FunctionKind.DIV,
    FunctionKind.DIVU,
    null,
    null,
    null,
    null,

    FunctionKind.ADD,
    FunctionKind.ADDU,
    FunctionKind.SUB,
    FunctionKind.SUBU,
    FunctionKind.AND,
    FunctionKind.OR,
    FunctionKind.XOR,
    FunctionKind.NOR,

    null,
    null,
    FunctionKind.SLT,
    FunctionKind.SLTU,
    null,
    null,
    null,
    null,

    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,

    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
).also { check(it.size == 64) }

/**
 * Decodes the [InstructionKind] from a given 32-bit instruction.
 *
 * @return The resulting kind, or null for invalid instructions.
 */
internal fun decodeKind(insn: UInt): InstructionKind? {
    val opcode = (insn shr 26).toInt()
    return instructionTable[opcode]
}

/**
 * Decodes the [FunctionKind] from a given 32-bit instruction.
 *
 * The return value will only make sense for [InstructionKind.SPECIAL].
 *
 * @return The resulting kind, or null for invalid instructions.
 */
internal fun decodeFunction(insn: UInt): FunctionKind? {
    val opcode = (insn and 0x3FU).toInt()
    return functionTable[opcode]
}
