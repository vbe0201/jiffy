package io.github.vbe0201.jiffy.jit.decoder

/**
 * Representation of a 32-bit MIPS instruction.
 *
 * This type wraps the raw data and provides various convenience methods
 * for parsing and interacting with encoded bit fields.
 */
data class Instruction(val insn: UInt) {
    /**
     * Gets the [InstructionKind] for this instruction.
     *
     * @return The associated kind, or null for invalid data.
     */
    fun kind(): InstructionKind? = decodeKind(insn)

    /**
     * Gets the [FunctionKind] for this instruction.
     *
     * @return The associated kind, or null for invalid data.
     */
    fun function(): FunctionKind? = decodeFunction(insn)

    /**
     * Extracts the rs operand of this instruction.
     *
     * The resulting value is only valid for I and R form instructions.
     */
    fun rs(): UInt = (insn shr 21) and 0x1FU

    /**
     * Extracts the rt operand of this instruction.
     *
     * The resulting value is only valid for I and R form instructions.
     */
    fun rt(): UInt = (insn shr 16) and 0x1FU

    /**
     * Gets the imm operand of this instruction.
     *
     * The resulting value is only valid for I form instructions and may be
     * sign or zero extended to 32-bits, depending on the instruction.
     */
    fun imm(): UShort = (insn and 0xFFFFU).toUShort()

    /**
     * Extracts the rd operand of this instruction.
     *
     * The resulting value is only valid for R form instructions.
     */
    fun rd(): UInt = (insn shr 11) and 0x1FU

    /**
     * Extracts the shamt operand of this instruction.
     *
     * The resulting value is only valid for R form instructions.
     */
    fun shamt(): UInt = (insn shr 6) and 0x1FU

    /**
     * Extracts the target operand of this instruction.
     *
     * The resulting value is only valid for J form instructions.
     */
    fun target(): UInt = insn and 0x3FFFFFFU
}
