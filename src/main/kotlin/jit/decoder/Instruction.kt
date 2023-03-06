package io.github.vbe0201.jiffy.jit.decoder

/**
 * The amount of bytes an [Instruction] consumes.
 */
const val INSTRUCTION_SIZE = 4U

/**
 * Representation of a 32-bit MIPS instruction.
 *
 * This type wraps the raw data and provides various convenience methods
 * for parsing and interacting with encoded bit fields.
 */
@JvmInline
value class Instruction(val raw: UInt) {
    /**
     * Gets the [InstructionKind] for this instruction.
     */
    fun kind(): InstructionKind? = decodeKind(raw)

    /**
     * Gets the [FunctionKind] for this instruction.
     */
    fun function(): FunctionKind? = decodeFunction(raw)

    /**
     * Extracts the rs operand of this instruction.
     *
     * The resulting value is only valid for I and R form instructions.
     */
    fun rs() = Register(((raw shr 21) and 0x1FU).toInt())

    /**
     * Extracts the rt operand of this instruction.
     *
     * The resulting value is only valid for I and R form instructions.
     */
    fun rt() = Register(((raw shr 16) and 0x1FU).toInt())

    /**
     * Gets the imm operand of this instruction.
     *
     * The resulting value is only valid for I form instructions and may be
     * sign or zero extended to 32 bits, depending on the instruction.
     */
    fun imm() = (raw and 0xFFFFU).toUShort()

    /**
     * Extracts the coprocessor opcode of this instruction.
     *
     * The resulting value is only valid for coprocessor instructions.
     */
    fun copOpcode() = (raw shr 21) and 0x1FU

    /**
     * Gets the imm25 operand of this instruction.
     *
     * The resulting value is only valid for coprocessor instructions.
     */
    fun imm25() = raw and 0x1FF_FFFFU

    /**
     * Extracts the rd operand of this instruction.
     *
     * The resulting value is only valid for R form instructions.
     */
    fun rd() = Register(((raw shr 11) and 0x1FU).toInt())

    /**
     * Extracts the shamt operand of this instruction.
     *
     * The resulting value is only valid for R form instructions.
     */
    fun shamt() = (raw shr 6) and 0x1FU

    /**
     * Extracts the target operand of this instruction.
     *
     * The resulting value is only valid for J form instructions.
     */
    fun target() = raw and 0x3FFFFFFU
}
