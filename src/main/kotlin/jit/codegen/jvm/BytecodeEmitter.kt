package io.github.vbe0201.jiffy.jit.codegen.jvm

import io.github.vbe0201.jiffy.cpu.ExceptionKind
import io.github.vbe0201.jiffy.jit.decoder.Register
import io.github.vbe0201.jiffy.jit.state.ExecutionContext
import io.github.vbe0201.jiffy.jit.translation.Compiled
import io.github.vbe0201.jiffy.jit.translation.Compiler
import io.github.vbe0201.jiffy.utils.toInt
import org.objectweb.asm.commons.InstructionAdapter
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type

private const val WRITER_FLAGS =
    ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS

// The only officially supported JDK by jiffy is 19.
// When bumping to a newer version, also change this constant.
private const val CLASS_VERSION = V19

// Symbols for miscellaneous classes used during code generation.
private val compiledInterface = Type.getInternalName(Compiled::class.java)
private val compilerClass = Type.getInternalName(Compiler::class.java)
private val contextClass = Type.getInternalName(ExecutionContext::class.java)
private val exceptionKindEnum = Type.getInternalName(ExceptionKind::class.java)

// The local variable slot for a value from a delayed memory load.
private const val DELAYED_LOAD_SLOT = 2
private const val TEMP_WRITE_BACKUP_SLOT = 3

private fun makeWriterWithPrologue(): ClassWriter {
    val writer = ClassWriter(WRITER_FLAGS)

    // Start the new class to generate.
    writer.visit(
        CLASS_VERSION,
        ACC_PUBLIC + ACC_SUPER,
        "$compilerClass\$BlockImpl",
        null,
        "java/lang/Object",
        arrayOf(compiledInterface)
    )

    // Generate the default constructor for every generated class.
    // This must be kept in sync with the requirements of the
    // `Compiler` class for instantiating the generated types.
    writer.visitMethod(
        ACC_PUBLIC,
        "<init>",
        "()V",
        null,
        null
    )
        .run {
            visitVarInsn(ALOAD, 0)
            visitMethodInsn(
                INVOKESPECIAL,
                "java/lang/Object",
                "<init>",
                "()V",
                false
            )
            visitInsn(RETURN)

            visitMaxs(0, 0)
            visitEnd()
        }

    return writer
}

class BytecodeEmitter {
    // An `ClassWriter` which is configured to write a new class
    // implementing the `Compiled` interface with empty constructor.
    private val writer = makeWriterWithPrologue()

    // Since Kotlin guarantees initialization order of properties,
    // the very next thing for us to do is to start the generation
    // of the `Compiled.execute` implementation.
    var raw = InstructionAdapter(
        this.writer.visitMethod(
            ACC_PUBLIC,
            "execute",
            "(L$contextClass;)V",
            null,
            null
        )
    )

    // The target register of a pending memory load. Loads to
    // `$zero` are always ignored, so that is the default value.
    private var pendingLoad = Register.ZERO

    /**
     * Finishes the generation of a [Compiled] object and returns
     * the assembled class bytes.
     */
    fun finish(): ByteArray {
        this.raw.run {
            visitInsn(RETURN)
            visitMaxs(0, 0)
            visitEnd()
        }

        return this.writer.run {
            visitEnd()
            toByteArray()
        }
    }

    private inline fun contextCall(name: String, descriptor: String) {
        this.raw.invokevirtual(contextClass, name, descriptor, false)
    }

    /**
     * Configures a delayed load from memory, to be finished at a
     * later time.
     *
     * This finishes any pending memory loads first, then sets
     * itself up to load [Operand] into the given destination register
     * on the next call to [BytecodeEmitter.finishDelayedLoad].
     */
    fun configureDelayedLoad(reg: Register, op: Operand) {
        this.run {
            // Finish pending memory loads to prevent the last cached value
            // from being overwritten in consecutive load sequences.
            finishDelayedLoad()

            // Store local state for `finishDelayedLoad`.
            pendingLoad = reg
            op.storeLocal(DELAYED_LOAD_SLOT)
        }
        finishDelayedLoad()
    }

    /**
     * Completes a delayed load from memory, if any are pending.
     *
     * This will set the destination register the load was initiated
     * with to the pre-computed value.
     */
    fun finishDelayedLoad() {
        val reg = this.pendingLoad
        if (reg != Register.ZERO) {
            this.pendingLoad = Register.ZERO
            setGpr(reg) {
                this.raw.visitVarInsn(ILOAD, DELAYED_LOAD_SLOT)
            }
        }
    }

    /** Places an integer value on top of the JVM operand stack. */
    inline fun place(value: Int): Operand {
        this.raw.iconst(value)
        return Operand(JvmType.INT)
    }

    /**
     * Loads the integer value of a given general-purpose register
     * and returns the [Operand] descriptor for it.
     */
    fun getGpr(reg: Register): Operand {
        this.raw.run {
            visitVarInsn(ALOAD, 1)
            contextCall("getGprs", "()[I")

            iconst(reg.index)
            visitInsn(IALOAD)
        }

        return Operand(JvmType.INT)
    }

    /**
     * Sets a general-purpose register to a given integer value
     * placed on the operand stack by [op].
     */
    fun setGpr(reg: Register, op: BytecodeEmitter.() -> Unit) {
        this.raw.run {
            visitVarInsn(ALOAD, 1)
            contextCall("getGprs", "()[I")

            iconst(reg.index)
            op()

            // Finish a delayed load now that all inputs have been read,
            // and before the output register is written below.
            finishDelayedLoad()

            visitInsn(IASTORE)
        }
    }

    /**
     * Gets the integer value of a given COP0 register and returns
     * the [Operand] descriptor for it.
     */
    fun getCop0Register(reg: Register): Operand {
        this.raw.run {
            visitVarInsn(ALOAD, 1)
            iconst(reg.index)
            contextCall("getCop0Register", "(I)I")
        }

        return Operand(JvmType.INT)
    }

    /**
     * Sets a given COP0 register to a new value.
     *
     * The given operation is responsible for placing an integer
     * value to be written on the operand stack.
     */
    fun setCop0Register(reg: Register, op: BytecodeEmitter.() -> Unit) {
        this.raw.run {
            visitVarInsn(ALOAD, 1)
            iconst(reg.index)
            op()

            // Finish a delayed load now that all inputs have been read,
            // and before the output register is written below.
            finishDelayedLoad()

            contextCall("setCop0Register", "(II)V")
        }
    }

    /**
     * Loads a value of any supported result type from an address
     * through the CPU bus.
     *
     * The given operation is responsible for placing the integer
     * address to read from on the operand stack.
     *
     * The user must handle load delay slots, when appropriate.
     * See [BytecodeEmitter.configureDelayedLoad].
     */
    fun loadBus(
        pc: UInt,
        delayed: Boolean,
        res: JvmType,
        op: BytecodeEmitter.() -> Unit,
    ): Operand {
        this.raw.run {
            visitVarInsn(ALOAD, 1)
            op()

            // Check if the read address is correctly aligned.
            // Since Longs are unsupported and addresses are expected
            // to be Ints, it is fine to hardcode the instructions.
            visitInsn(DUP)
            visitInsn(ICONST_0 + res.align - 1)
            visitInsn(IAND)
            conditional(Condition.INT_NOT_ZERO) {
                then = {
                    pop()
                    exception(pc, delayed, ExceptionKind.UNALIGNED_LOAD)
                }
            }

            when (res) {
                JvmType.BYTE -> contextCall("read8", "(I)B")
                JvmType.SHORT -> contextCall("read16", "(I)S")
                JvmType.INT -> contextCall("read32", "(I)I")
                JvmType.LONG -> throw AssertionError()
            }
        }

        return Operand(res)
    }

    /**
     * Writes a value of any supported type to an address through
     * the CPU bus.
     *
     * The given operation is responsible for placing two integer
     * values addr, value on the stack in this order.
     *
     * The [Operand] for the value to be written should be returned.
     * The user is responsible for casting it to the desired type.
     */
    fun writeBus(
        pc: UInt,
        delayed: Boolean,
        op: BytecodeEmitter.() -> Operand,
    ) {
        this.raw.run {
            visitVarInsn(ALOAD, 1)
            val value = op().storeLocal(TEMP_WRITE_BACKUP_SLOT)

            // Check if the write address is correctly aligned.
            // Since Longs are unsupported and addresses are expected
            // to be Ints, it is fine to hardcode the instructions.
            visitInsn(DUP)
            visitInsn(ICONST_0 + value.type.align - 1)
            visitInsn(IAND)
            conditional(Condition.INT_NOT_ZERO) {
                then = {
                    pop()
                    exception(pc, delayed, ExceptionKind.UNALIGNED_STORE)
                }
            }

            value.loadLocal(TEMP_WRITE_BACKUP_SLOT)
            when (value.type) {
                JvmType.BYTE -> contextCall("write8", "(IB)V")
                JvmType.SHORT -> contextCall("write16", "(IS)V")
                JvmType.INT -> contextCall("write32", "(II)V")
                JvmType.LONG -> throw AssertionError()
            }
        }
    }

    /**
     * Emits a jump in the emulated software, which sets the program
     * counter register to a new value.
     *
     * The given operation is responsible for placing the destination
     * address on the operand stack as an integer.
     */
    fun jump(op: BytecodeEmitter.() -> Unit) {
        this.raw.run {
            visitVarInsn(ALOAD, 1)
            op()
            contextCall("setPc", "(I)V")
        }
    }

    /**
     * Gets the integer value of the special-purpose LO register
     * and returns its [Operand] descriptor.
     */
    fun getLow(): Operand {
        this.raw.run {
            visitVarInsn(ALOAD, 1)
            contextCall("getLo", "()I")
        }

        return Operand(JvmType.INT)
    }

    /**
     * Sets the special-purpose LO register to a new value.
     *
     * The given operation is responsible for placing the new value
     * on top of the operand stack as an integer.
     */
    fun setLow(op: BytecodeEmitter.() -> Unit) {
        this.raw.run {
            visitVarInsn(ALOAD, 1)
            op()
            contextCall("setLo", "(I)V")
        }
    }

    /**
     * Gets the integer value of the special-purpose HI register
     * and returns its [Operand] descriptor.
     */
    fun getHigh(): Operand {
        this.raw.run {
            visitVarInsn(ALOAD, 1)
            contextCall("getHi", "()I")
        }

        return Operand(JvmType.INT)
    }

    /**
     * Sets the special-purpose HI register to a new value.
     *
     * The given operation is responsible for placing the new value
     * on top of the operand stack as an integer.
     */
    fun setHigh(op: BytecodeEmitter.() -> Unit) {
        this.raw.run {
            visitVarInsn(ALOAD, 1)
            op()
            contextCall("setHi", "(I)V")
        }
    }

    /**
     * Emits conditionally-executed code based on a [Condition] and
     * appropriate behavior for either outcome.
     */
    fun conditional(cond: Condition, action: Conditional.() -> Unit) {
        val conditional = Conditional(cond).apply(action)
        this.raw.run {
            val elseLabel = Label()
            val endLabel = Label()

            // First, emit the condition to not check for.
            // We want to hit the else label in that case.
            when (conditional.cond) {
                Condition.COMPARE_EQUAL -> ificmpne(elseLabel)
                Condition.COMPARE_NOT_EQUAL -> ificmpeq(elseLabel)

                Condition.COMPARE_SMALLER_THAN -> ificmpge(elseLabel)
                Condition.COMPARE_UNSIGNED_SMALLER_THAN -> {
                    invokestatic(
                        "java/lang/Integer",
                        "compareUnsigned",
                        "(II)I",
                        false
                    )
                    ifge(elseLabel)
                }

                Condition.INT_SMALLER_THAN_ZERO -> ifge(elseLabel)
                Condition.INT_SMALLER_OR_EQUAL_ZERO -> ifgt(elseLabel)
                Condition.INT_ZERO -> ifne(elseLabel)
                Condition.INT_NOT_ZERO -> ifeq(elseLabel)
                Condition.INT_GREATER_THAN_ZERO -> ifle(elseLabel)
            }

            val orElse = conditional.orElse

            // When the condition is fulfilled, the `then` code will run.
            // If `orElse` is given, we need to skip it after this.
            (conditional.then!!)()
            if (orElse != null) {
                goTo(endLabel)
            }

            // Bind the label for skipping the `then` block and run the
            // `orElse` logic. Also bind the label for skipping that.
            visitLabel(elseLabel)
            if (orElse != null) {
                orElse()
                visitLabel(endLabel)
            }
        }
    }

    /** Emits a call to [ExecutionContext.unimplemented]. */
    fun unimplemented() {
        this.raw.run {
            visitVarInsn(ALOAD, 1)
            contextCall("unimplemented", "()V")
        }
    }

    /**
     * Emits a call to [ExecutionContext.raiseException] and
     * prematurely returns from the current block.
     */
    fun exception(pc: UInt, delayed: Boolean, kind: ExceptionKind) {
        this.raw.run {
            visitVarInsn(ALOAD, 1)
            iconst(pc.toInt())
            visitInsn(ICONST_0 + delayed.toInt())
            getstatic(exceptionKindEnum, kind.name, "L$exceptionKindEnum;")
            contextCall("raiseException", "(IZL$exceptionKindEnum;)V")

            // After an exception was raised, return from the current block
            // to run exception handler code immediately in the next block.
            visitInsn(RETURN)
        }
    }

    /**
     * Emits a call to [ExecutionContext.restoreAfterException]
     * to leave exceptional state.
     */
    fun leaveException() {
        this.raw.run {
            visitVarInsn(ALOAD, 1)
            contextCall("leaveException", "()V")
        }
    }
}
