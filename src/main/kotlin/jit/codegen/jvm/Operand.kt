package io.github.vbe0201.jiffy.jit.codegen.jvm

import org.objectweb.asm.Opcodes

/**
 * Description of a value on the operand stack of the JVM.
 *
 * This provides common helpers for manipulating data in a
 * type-agnostic fashion.
 *
 * Special care must be applied when using these methods
 * since they are only valid as long as the described
 * operand is on top of the stack.
 */
@JvmInline
value class Operand(val type: JvmType) {
    /**
     * A stacked operation, performed by a raw VM opcode on this [Operand]
     * and an additional [Operand] placed on the stack by [op].
     *
     * The result will be left on top of the stack and its [Operand] will
     * be returned.
     */
    context(BytecodeEmitter)
    inline fun stackedOp(raw: Int, op: BytecodeEmitter.() -> Unit): Operand {
        this@BytecodeEmitter.op()
        this@BytecodeEmitter.raw.visitInsn(raw)

        return this
    }

    context(BytecodeEmitter)
    inline fun add(op: BytecodeEmitter.() -> Unit): Operand {
        return stackedOp(this.type.add, op)
    }

    context(BytecodeEmitter)
    inline fun sub(op: BytecodeEmitter.() -> Unit): Operand {
        return stackedOp(this.type.sub, op)
    }

    context(BytecodeEmitter)
    inline fun mult(op: BytecodeEmitter.() -> Unit): Operand {
        return stackedOp(this.type.mult, op)
    }

    context(BytecodeEmitter)
    inline fun rem(op: BytecodeEmitter.() -> Unit): Operand {
        return stackedOp(this.type.rem, op)
    }

    context(BytecodeEmitter)
    inline fun div(op: BytecodeEmitter.() -> Unit): Operand {
        return stackedOp(this.type.div, op)
    }

    context(BytecodeEmitter)
    inline fun and(op: BytecodeEmitter.() -> Unit): Operand {
        return stackedOp(this.type.and, op)
    }

    context(BytecodeEmitter)
    inline fun or(op: BytecodeEmitter.() -> Unit): Operand {
        return stackedOp(this.type.or, op)
    }

    context(BytecodeEmitter)
    inline fun xor(op: BytecodeEmitter.() -> Unit): Operand {
        return stackedOp(this.type.xor, op)
    }

    context(BytecodeEmitter)
    inline fun shl(op: BytecodeEmitter.() -> Unit): Operand {
        return stackedOp(this.type.shl, op)
    }

    context(BytecodeEmitter)
    inline fun shr(op: BytecodeEmitter.() -> Unit): Operand {
        return stackedOp(this.type.shr, op)
    }

    context(BytecodeEmitter)
    inline fun ushr(op: BytecodeEmitter.() -> Unit): Operand {
        return stackedOp(this.type.ushr, op)
    }

    context(BytecodeEmitter)
    inline fun loadLocal(slot: Int): Operand {
        this@BytecodeEmitter.raw.visitVarInsn(this.type.loadLocal, slot)
        return this
    }

    context(BytecodeEmitter)
    inline fun storeLocal(slot: Int): Operand {
        this@BytecodeEmitter.raw.visitVarInsn(this.type.storeLocal, slot)
        return this
    }

    /**
     * Truncates the value of the operand to the provided [JvmType].
     *
     * Do not call this method with a type larger than the operand
     * already is!
     */
    context(BytecodeEmitter)
    fun truncate(new: JvmType): Operand {
        // When this value is of type `Long`, truncate to `Int` first.
        if (this.type === JvmType.LONG) {
            this@BytecodeEmitter.raw.visitInsn(Opcodes.L2I)
        }

        // Then do the remaining conversion to `Byte` or `Short`.
        if (new === JvmType.BYTE) {
            this@BytecodeEmitter.raw.visitInsn(Opcodes.I2B)
        } else if (new === JvmType.SHORT) {
            this@BytecodeEmitter.raw.visitInsn(Opcodes.I2S)
        }

        return Operand(new)
    }

    /**
     * Zero-extends the value to the provided [JvmType] and returns
     * the [Operand] for the new value.
     *
     * Do not call this method with a type smaller than the operand
     * already is!
     */
    context(BytecodeEmitter)
    fun zeroExtend(new: JvmType): Operand {
        val mask = this.type.mask.toInt()

        // If we want a Long, we have to convert from Int first.
        // Otherwise, AND will promote any type to Int.
        return if (new === JvmType.LONG) {
            this@BytecodeEmitter.raw.visitInsn(Opcodes.I2L)

            Operand(new).and {
                place(mask)
                raw.visitInsn(Opcodes.I2L)
            }
        } else {
            Operand(new).and { place(mask) }
        }
    }

    /**
     * Sign-extends the value to the provided [JvmType] and returns
     * the [Operand] for the new value.
     *
     * Do not call this method with a type smaller than the operand
     * already is!
     */
    context(BytecodeEmitter)
    fun signExtend(new: JvmType): Operand {
        // If we want a Long, we have to do the explicit cast.
        // Everything else is already an Int of sorts.
        if (new === JvmType.LONG) {
            this@BytecodeEmitter.raw.visitInsn(Opcodes.I2L)
        }

        return Operand(new)
    }
}
