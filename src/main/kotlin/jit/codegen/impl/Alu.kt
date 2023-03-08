package io.github.vbe0201.jiffy.jit.codegen.impl

import io.github.vbe0201.jiffy.jit.codegen.InstructionMeta
import io.github.vbe0201.jiffy.jit.codegen.Status
import io.github.vbe0201.jiffy.jit.codegen.jvm.BytecodeEmitter
import io.github.vbe0201.jiffy.jit.codegen.jvm.Condition
import io.github.vbe0201.jiffy.jit.codegen.jvm.JvmType
import io.github.vbe0201.jiffy.jit.codegen.jvm.Operand
import io.github.vbe0201.jiffy.jit.decoder.Register
import io.github.vbe0201.jiffy.utils.*

// Local variable slots in the generated function; temporarily
// occupied for implementation details of individual instructions.
private const val TEMP_MUL_SLOT = 4
private const val CHECKED_RES_SLOT = 5

private inline fun checkedAdd(
    meta: InstructionMeta,
    emitter: BytecodeEmitter,
    out: Register,
    a: BytecodeEmitter.() -> Operand,
    b: BytecodeEmitter.() -> Operand,
) {
    emitter.run {
        // Evaluate the result of the addition and back it up.
        val res = a().add { b() }.storeLocal(CHECKED_RES_SLOT)

        // Check if an overflow has occurred. This is the case when
        // both operands have a different sign than the result.
        res.loadLocal(CHECKED_RES_SLOT).xor { a() }
            .and {
                res.loadLocal(CHECKED_RES_SLOT).xor { b() }
            }

        conditional(Condition.INT_SMALLER_THAN_ZERO) {
            then = {
                // Generate an overflow CPU exception.
                exception(meta.exceptionPc(), meta.branchDelaySlot, "OVERFLOW")
            }

            orElse = {
                // When we were successful, write the result to output.
                setGpr(out) {
                    res.loadLocal(CHECKED_RES_SLOT)
                }
            }
        }

    }
}

private inline fun checkedSub(
    meta: InstructionMeta,
    emitter: BytecodeEmitter,
    out: Register,
    a: BytecodeEmitter.() -> Operand,
    b: BytecodeEmitter.() -> Operand
) {
    emitter.run {
        // Evaluate the result of the subtraction and back it up.
        val res = a().sub { b() }.storeLocal(CHECKED_RES_SLOT)

        // Check if an overflow has occurred. This is the case when both
        // operands have different signs and the result has a different
        // sign than `a`.
        a().xor { b() }
            .and { a().xor { res.loadLocal(CHECKED_RES_SLOT) } }

        conditional(Condition.INT_SMALLER_THAN_ZERO) {
            then = {
                // Generate an overflow CPU exception.
                exception(meta.exceptionPc(), meta.branchDelaySlot, "OVERFLOW")
            }

            orElse = {
                // When we were successful, write the result to output.
                setGpr(out) {
                    res.loadLocal(CHECKED_RES_SLOT)
                }
            }
        }
    }
}

/**
 * Generates the Add Immediate Unsigned (ADDIU) instruction to
 * the code buffer.
 */
fun addiu(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    emitter.setGpr(meta.insn.rt()) {
        // NOTE: The "Unsigned" part in the instruction name is
        // misleading. The sign extension is intentional.
        getGpr(meta.insn.rs())
            .add { place(meta.insn.imm().signExtend32().toInt()) }
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the Add instruction to the code buffer.
 */
fun add(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    checkedAdd(
        meta,
        emitter,
        meta.insn.rd(),
        { getGpr(meta.insn.rs()) },
        { getGpr(meta.insn.rt()) }
    )

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the Add Immediate (ADDI) instruction to the code buffer.
 */
fun addi(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    checkedAdd(
        meta,
        emitter,
        meta.insn.rt(),
        { getGpr(meta.insn.rs()) },
        { place(meta.insn.imm().signExtend32().toInt()) }
    )

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the Add Unsigned (ADDU) instruction to the
 * code buffer.
 */
fun addu(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    emitter.setGpr(meta.insn.rd()) {
        getGpr(meta.insn.rs())
            .add { getGpr(meta.insn.rt()) }
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the Subtract (SUB) instruction to the code buffer.
 */
fun sub(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    checkedSub(
        meta,
        emitter,
        meta.insn.rd(),
        { getGpr(meta.insn.rs()) },
        { getGpr(meta.insn.rt()) }
    )

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the Subtract Unsigned (SUBU) instruction to the
 * code buffer.
 */
fun subu(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    emitter.setGpr(meta.insn.rd()) {
        getGpr(meta.insn.rs())
            .sub { getGpr(meta.insn.rt()) }
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the Load Unsigned Immediate (LUI) instruction to
 * the code buffer.
 */
fun lui(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    emitter.setGpr(meta.insn.rt()) {
        place((meta.insn.imm().zeroExtend32() shl 16).toInt())
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the AND Immediate (ANDI) instruction to the
 * code buffer.
 */
fun andi(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    emitter.setGpr(meta.insn.rt()) {
        getGpr(meta.insn.rs())
            .and { place(meta.insn.imm().zeroExtend32().toInt()) }
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the OR Immediate (ORI) instruction to the code buffer.
 */
fun ori(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    emitter.setGpr(meta.insn.rt()) {
        getGpr(meta.insn.rs())
            .or { place(meta.insn.imm().zeroExtend32().toInt()) }
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the XOR Immediate (XORI) instruction to the code buffer.
 */
fun xori(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    emitter.setGpr(meta.insn.rt()) {
        getGpr(meta.insn.rs())
            .xor { place(meta.insn.imm().zeroExtend32().toInt()) }
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the AND instruction to the code buffer.
 */
fun and(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    emitter.setGpr(meta.insn.rd()) {
        getGpr(meta.insn.rs())
            .and { getGpr(meta.insn.rt()) }
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the XOR instruction to the code buffer.
 */
fun or(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    emitter.setGpr(meta.insn.rd()) {
        getGpr(meta.insn.rs())
            .or { getGpr(meta.insn.rt()) }
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the XOR instruction to the code buffer.
 */
fun xor(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    emitter.setGpr(meta.insn.rd()) {
        getGpr(meta.insn.rs())
            .xor { getGpr(meta.insn.rt()) }
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the NOT OR (NOR) instruction to the code buffer.
 */
fun nor(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    emitter.setGpr(meta.insn.rd()) {
        // Bitwise OR both registers together and compute
        // the complement of the resulting value.
        getGpr(meta.insn.rs())
            .or { getGpr(meta.insn.rd()) }
            .xor { place(-1) }
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the Shift Left Logical (SLL) instruction to the
 * code buffer.
 */
fun sll(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    emitter.setGpr(meta.insn.rd()) {
        getGpr(meta.insn.rt())
            .shl { place(meta.insn.shamt().toInt()) }
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the Shift Left Logical Variable (SLLV) instruction
 * to the code buffer.
 */
fun sllv(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    emitter.setGpr(meta.insn.rd()) {
        // NOTE: The ISHL bytecode instruction already truncates the
        // S register to 5 bits, as defined by the MIPS architecture.
        getGpr(meta.insn.rt())
            .shl { getGpr(meta.insn.rs()) }
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the Shift Right Logical (SRL) instruction to the
 * code buffer.
 */
fun srl(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    emitter.setGpr(meta.insn.rd()) {
        getGpr(meta.insn.rt())
            .ushr { place(meta.insn.shamt().toInt()) }
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the Shift Right Logical Variable (SRLV) instruction
 * to the code buffer.
 */
fun srlv(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    emitter.setGpr(meta.insn.rd()) {
        // NOTE: The IUSHR bytecode instruction already truncates the
        // S register to 5 bits, as defined by the MIPS architecture.
        getGpr(meta.insn.rt())
            .ushr { getGpr(meta.insn.rs()) }
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the Shift Right Arithmetic (SRA) instruction to the
 * code buffer.
 */
fun sra(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    emitter.setGpr(meta.insn.rd()) {
        getGpr(meta.insn.rt())
            .shr { place(meta.insn.shamt().toInt()) }
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the Shift Right Arithmetic Variable (SRAV) instruction
 * to the code buffer.
 */
fun srav(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    emitter.setGpr(meta.insn.rd()) {
        // NOTE: The ISHR bytecode instruction already truncates the
        // S register to 5 bits, as defined by the MIPS architecture.
        getGpr(meta.insn.rt())
            .shr { getGpr(meta.insn.rs()) }
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the Set if Less Than Immediate (SLTI) instruction to
 * the code buffer.
 */
fun slti(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    emitter.setGpr(meta.insn.rt()) {
        getGpr(meta.insn.rs())
        place(meta.insn.imm().signExtend32().toInt())

        conditional(Condition.COMPARE_SMALLER_THAN) {
            then = { place(1) }
            orElse = { place(0) }
        }
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the Set if Less Than Immediate Unsigned (SLTIU)
 * instruction to the code buffer.
 */
fun sltiu(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    emitter.setGpr(meta.insn.rt()) {
        getGpr(meta.insn.rs())
        place(meta.insn.imm().signExtend32().toInt())

        conditional(Condition.COMPARE_UNSIGNED_SMALLER_THAN) {
            then = { place(1) }
            orElse = { place(0) }
        }
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the Set on Less Than (SLT) instruction to the
 * code buffer.
 */
fun slt(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    emitter.setGpr(meta.insn.rd()) {
        getGpr(meta.insn.rs())
        getGpr(meta.insn.rt())

        conditional(Condition.COMPARE_SMALLER_THAN) {
            then = { place(1) }
            orElse = { place(0) }
        }
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the Set on Less Than Unsigned (SLTU) instruction to
 * the code buffer.
 */
fun sltu(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    emitter.setGpr(meta.insn.rd()) {
        getGpr(meta.insn.rs())
        getGpr(meta.insn.rt())

        conditional(Condition.COMPARE_UNSIGNED_SMALLER_THAN) {
            then = { place(1) }
            orElse = { place(0) }
        }
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the Multiply (MULT) instruction to the code buffer.
 */
fun mult(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    emitter.run {
        // Perform signed multiplication of input registers.
        val result = getGpr(meta.insn.rs()).signExtend(JvmType.LONG)
            .mult { getGpr(meta.insn.rt()).signExtend(JvmType.LONG) }
            .storeLocal(TEMP_MUL_SLOT)

        // Set the HI register to the high 32 bits of the result.
        setHigh {
            result.loadLocal(TEMP_MUL_SLOT)
                .ushr { place(Int.SIZE_BITS) }
                .truncate(JvmType.INT)
        }

        // Set the LO register to the lower 32 bits of the result.
        setLow {
            result.loadLocal(TEMP_MUL_SLOT).truncate(JvmType.INT)
        }
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the Multiply Unsigned (MULTU) instruction to the
 * code buffer.
 */
fun multu(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    emitter.run {
        // Perform unsigned multiplication of input registers.
        val result = getGpr(meta.insn.rs()).zeroExtend(JvmType.LONG)
            .mult { getGpr(meta.insn.rt()).zeroExtend(JvmType.LONG) }
            .storeLocal(TEMP_MUL_SLOT)

        // Set the HI register to the high 32 bits of the result.
        setHigh {
            result.loadLocal(TEMP_MUL_SLOT)
                .ushr { place(Int.SIZE_BITS) }
                .truncate(JvmType.INT)
        }

        // Set the LO register to the lower 32 bits of the result.
        setLow {
            result.loadLocal(TEMP_MUL_SLOT).truncate(JvmType.INT)
        }
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the Divide (DIV) instruction to the code buffer.
 */
fun div(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    val rs = meta.insn.rs()
    val rt = meta.insn.rt()

    emitter.run {
        // If we're dividing by zero, the results are undefined.
        getGpr(rt)
        conditional(Condition.INT_ZERO) {
            then = {
                setHigh { getGpr(rs) }
                setLow {
                    getGpr(rs)
                    conditional(Condition.INT_SMALLER_THAN_ZERO) {
                        then = { place(1) }
                        orElse = { place(-1) }
                    }
                }
            }

            orElse = {
                // The JVM bytecode instructions conveniently already produce
                // matching results for `Int.MIN_VALUE / -1`, so no need to
                // special case it in the implementation.
                setHigh { getGpr(rs).rem { getGpr(rt) } }
                setLow { getGpr(rs).div { getGpr(rt) } }
            }
        }
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the Divide Unsigned (DIVU) instruction to the
 * code buffer.
 */
fun divu(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    val rs = meta.insn.rs()
    val rt = meta.insn.rt()

    emitter.run {
        // If we're dividing by zero, the results are undefined.
        getGpr(rt)
        conditional(Condition.INT_ZERO) {
            then = {
                setHigh { getGpr(rs) }
                setLow { place(-1) }
            }

            orElse = {
                setHigh { getGpr(rs).rem { getGpr(rt) } }
                setLow { getGpr(rs).div { getGpr(rt) } }
            }
        }
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates Move From LO (MFLO) instruction to the code buffer.
 */
fun mflo(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    emitter.setGpr(meta.insn.rd()) {
        getLow()
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the Move From HI (MFHI) instruction to the code buffer.
 */
fun mfhi(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    emitter.setGpr(meta.insn.rd()) {
        getHigh()
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the Move To LO (MTLO) instruction to the code buffer.
 */
fun mtlo(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    emitter.setLow {
        getGpr(meta.insn.rs())
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the Move To HI (MTHI) instruction to the code buffer.
 */
fun mthi(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    emitter.setHigh {
        getGpr(meta.insn.rs())
    }

    return Status.CONTINUE_BLOCK
}
