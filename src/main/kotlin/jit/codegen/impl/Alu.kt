package io.github.vbe0201.jiffy.jit.codegen.impl

import io.github.vbe0201.jiffy.jit.codegen.InstructionMeta
import io.github.vbe0201.jiffy.jit.codegen.Status
import io.github.vbe0201.jiffy.jit.codegen.jvm.BytecodeEmitter
import io.github.vbe0201.jiffy.jit.codegen.jvm.Condition
import io.github.vbe0201.jiffy.jit.codegen.jvm.JvmType

const val TEMP_MUL_SLOT: Int = 3

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
