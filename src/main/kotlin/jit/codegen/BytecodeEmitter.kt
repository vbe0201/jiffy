package io.github.vbe0201.jiffy.jit.codegen

import io.github.vbe0201.jiffy.jit.state.ExecutionContext
import io.github.vbe0201.jiffy.jit.translation.Compiled
import io.github.vbe0201.jiffy.jit.translation.Compiler
import org.objectweb.asm.commons.InstructionAdapter
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type

// The flags to configure the writer with. For the sake of
// simplicity, we want it to do as much work for us as possible.
private const val WRITER_FLAGS =
    ClassWriter.COMPUTE_FRAMES + ClassWriter.COMPUTE_MAXS

// The only officially supported JDK by jiffy is 19.
// When bumping to a newer version, also change this constant.
private const val CLASS_VERSION = V19

// Symbols for miscellaneous classes used during code generation.
private val compiledInterface = Type.getInternalName(Compiled::class.java)
private val compilerClass = Type.getInternalName(Compiler::class.java)
private val contextClass = Type.getInternalName(ExecutionContext::class.java)

// The local variable slot for a value from a delayed memory load.
private const val DELAYED_LOAD_SLOT = 2

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
        .apply {
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

/**
 * Code emitter for dynamically composing implementations of the
 * [Compiled] interface in Java bytecode.
 *
 * After an emitter has been created, it accepts arbitrary bytecode
 * to form the [Compiled.execute] implementation. Finally, the
 * [BytecodeEmitter.finish] method retrieves the class bytes.
 *
 * Emitter objects cannot be reused; a new instance should be created
 * when a new [Compiled] implementation needs to be generated.
 */
class BytecodeEmitter {
    // An `ClassWriter` which is configured to write a new class
    // implementing the `Compiled` interface with empty constructor.
    private val writer = makeWriterWithPrologue()

    // Since Kotlin guarantees initialization order of properties,
    // the very next thing for us to do is to start the generation
    // of the `Compiled.execute` implementation.
    private var visitor = InstructionAdapter(
        this.writer.visitMethod(
            ACC_PUBLIC,
            "execute",
            "(L$contextClass;)V",
            null,
            null
        )
    )

    // The target register of a pending memory load. By default, this is
    // set to zero since loads to `$zero` would be ignored anyway.
    private var pendingLoad = 0U

    /**
     * Completes the generation of a class and returns the generated code.
     *
     * The resulting bytes define a class implementing [Compiled], as per
     * [BytecodeEmitter] docs.
     */
    fun finish(): ByteArray {
        // Finish the implementation of `Compiled.execute`.
        this.visitor.run {
            visitInsn(RETURN)
            visitMaxs(0, 0)
            visitEnd()
        }

        // Finish the implementation of the generated class.
        return this.writer.run {
            visitEnd()
            toByteArray()
        }
    }

    /**
     * Stores the value on top of the operand stack as a local variable.
     *
     * Returns the slot index to load the value at a later time.
     */
    fun storeLocal(slot: Int) {
        this.visitor.visitVarInsn(ISTORE, slot)
    }

    /**
     * Loads a local variable at a given slot to the top of the
     * operand stack.
     */
    fun loadLocal(slot: Int) {
        this.visitor.visitVarInsn(ILOAD, slot)
    }

    /**
     * Builds conditional if/else constructs based on a [Condition].
     *
     * The [Conditional.orElse] logic is optional.
     */
    fun conditional(cond: Condition, action: Conditional.() -> Unit) {
        val conditional = Conditional(cond).apply(action)
        this.visitor.run {
            val elseLabel = Label()
            val endLabel = Label()

            // First, emit the condition to check for.
            when (conditional.cond) {
                Condition.INTS_EQUAL -> ificmpne(elseLabel)
                Condition.INTS_NOT_EQUAL -> ificmpeq(elseLabel)
                Condition.INT_SMALLER_THAN -> ificmpge(elseLabel)
                Condition.UNSIGNED_INT_SMALLER_THAN -> {
                    invokestatic(
                        "java/lang/Integer",
                        "compareUnsigned",
                        "(II)I",
                        false
                    )
                    ifge(elseLabel)
                }
                Condition.SMALLER_THAN_ZERO -> ifge(elseLabel)
                Condition.SMALLER_OR_EQUAL_ZERO -> ifgt(elseLabel)
                Condition.GREATER_THAN_ZERO -> ifle(elseLabel)
            }

            val orElse = conditional.orElse

            // When the condition is fulfilled, the `then` code will run.
            // If else is given, we need to skip it after this.
            (conditional.then!!)()
            if (orElse != null) {
                goTo(endLabel)
            }

            // Bind the label for skipping the `then` block and place the
            // `orElse` logic behind it.
            visitLabel(elseLabel)
            if (orElse != null) {
                orElse()
                visitLabel(endLabel)
            }
        }
    }

    /**
     * Reads the value of a general-purpose register from the given
     * index and leaves it on top of the stack.
     */
    fun getGpr(index: UInt) {
        this.visitor.run {
            visitVarInsn(ALOAD, 1)
            invokevirtual(contextClass, "getGprs", "()[I", false)

            iconst(index.toInt())
            visitInsn(IALOAD)
        }
    }

    /**
     * Sets the general-purpose register for the given index to
     * a new value.
     *
     * The given operation is responsible for placing an integer
     * value to be written on the stack.
     */
    fun setGpr(index: UInt, op: BytecodeEmitter.() -> Unit) {
        this.visitor.run {
            visitVarInsn(ALOAD, 1)
            invokevirtual(contextClass, "getGprs", "()[I", false)

            iconst(index.toInt())
            op()

            // Finish a delayed load now that all inputs have been read.
            finishDelayedLoad()

            visitInsn(IASTORE)
        }
    }

    /**
     * Gets the value of a given COP0 register.
     *
     * The given operation is responsible for placing an integer
     * register index on the stack.
     *
     * NOTE: The value will be written to the given destination
     * register after a load delay slot.
     */
    fun loadCop0RegisterDelayed(reg: UInt, op: BytecodeEmitter.() -> Unit) {
        this.visitor.run {
            visitVarInsn(ALOAD, 1)
            op()
            invokevirtual(contextClass, "getCop0Register", "(I)I", false)

            // Finish pending memory loads to prevent the last cached value
            // from being overwritten in consecutive load sequences.
            finishDelayedLoad()

            visitVarInsn(ISTORE, DELAYED_LOAD_SLOT)
        }
        this.pendingLoad = reg
    }

    /**
     * Sets the COP0 status register to a new value.
     *
     * The given operation is responsible for placing an integer
     * value to be written on the stack.
     */
    fun setCop0Register(op: BytecodeEmitter.() -> Unit) {
        this.visitor.run {
            visitVarInsn(ALOAD, 1)
            op()
            invokevirtual(contextClass, "setCop0Register", "(II)V", false)
        }
    }

    /**
     * Completes a pending delayed load, if any.
     *
     * This will set the destination register to the loaded value.
     */
    fun finishDelayedLoad() {
        val loadReg = this.pendingLoad
        if (loadReg != 0U) {
            this.pendingLoad = 0U
            setGpr(loadReg) {
                loadLocal(DELAYED_LOAD_SLOT)
            }
        }
    }

    /**
     * Loads an 8-bit value from a given address through the CPU bus.
     *
     * This takes the destination register of the load and stores the
     * value of the load without applying it to the register yet.
     *
     * Since this handles the delay slot, the result will not be
     * visible before the next instruction has finished executing.
     */
    fun loadBusDelayed8(reg: UInt, op: BytecodeEmitter.() -> Unit) {
        this.visitor.run {
            visitVarInsn(ALOAD, 1)
            op()
            invokevirtual(contextClass, "read8", "(I)B", false)

            // Finish pending memory loads to prevent the last cached value
            // from being overwritten in consecutive load sequences.
            finishDelayedLoad()

            visitVarInsn(ISTORE, DELAYED_LOAD_SLOT)
        }
        this.pendingLoad = reg
    }

    /**
     * Loads an 16-bit value from a given address through the CPU bus.
     *
     * This takes the destination register of the load and stores the
     * value of the load without applying it to the register yet.
     *
     * Since this handles the delay slot, the result will not be
     * visible before the next instruction has finished executing.
     */
    fun loadBusDelayed16(reg: UInt, op: BytecodeEmitter.() -> Unit) {
        this.visitor.run {
            visitVarInsn(ALOAD, 1)
            op()
            invokevirtual(contextClass, "read16", "(I)S", false)

            // Finish pending memory loads to prevent the last cached value
            // from being overwritten in consecutive load sequences.
            finishDelayedLoad()

            visitVarInsn(ISTORE, DELAYED_LOAD_SLOT)
        }
        this.pendingLoad = reg
    }

    /**
     * Loads a 32-bit value from a given address through the CPU bus.
     *
     * This takes the destination register of the load and stores the
     * value of the load without applying it to the register yet.
     *
     * Since this handles the delay slot, the result will not be
     * visible before the next instruction has finished executing.
     */
    fun loadBusDelayed32(reg: UInt, op: BytecodeEmitter.() -> Unit) {
        this.visitor.run {
            visitVarInsn(ALOAD, 1)
            op()
            invokevirtual(contextClass, "read32", "(I)I", false)

            // Finish pending memory loads to prevent the last cached value
            // from being overwritten in consecutive load sequences.
            finishDelayedLoad()

            visitVarInsn(ISTORE, DELAYED_LOAD_SLOT)
        }
        this.pendingLoad = reg
    }

    /**
     * Writes an 8-bit value to a given address through the CPU bus.
     *
     * The given operation is responsible for placing two integer
     * values addr, value on the stack in this order.
     *
     * The conversion of value to [UByte] along with a call to
     * [ExecutionContext.write8] will then be emitted.
     */
    fun writeBus8(op: BytecodeEmitter.() -> Unit) {
        this.visitor.run {
            visitVarInsn(ALOAD, 1)
            op()
            visitInsn(I2B)
            invokevirtual(contextClass, "write8", "(IB)V", false)
        }
    }

    /**
     * Writes a 16-bit value to a given address through the CPU bus.
     *
     * The given operation is responsible for placing two integer
     * values addr, value on the stack in this order.
     *
     * The conversion of value to [UShort] along with a call to
     * [ExecutionContext.write16] will then be emitted.
     */
    fun writeBus16(op: BytecodeEmitter.() -> Unit) {
        this.visitor.run {
            visitVarInsn(ALOAD, 1)
            op()
            visitInsn(I2S)
            invokevirtual(contextClass, "write16", "(IS)V", false)
        }
    }

    /**
     * Writes a 32-bit value to a given address through the CPU bus.
     *
     * The given operation is responsible for placing two integer
     * values addr, value on the stack in this order.
     *
     * A call to [ExecutionContext.write32] will then be emitted.
     */
    fun writeBus32(op: BytecodeEmitter.() -> Unit) {
        this.visitor.run {
            visitVarInsn(ALOAD, 1)
            op()
            invokevirtual(contextClass, "write32", "(II)V", false)
        }
    }

    /**
     * Emits a branch in the emulated software, which sets the program
     * counter to a new location.
     *
     * The given operation is responsible for placing the new program
     * counter value on the operand stack.
     */
    fun jump(op: BytecodeEmitter.() -> Unit) {
        this.visitor.run {
            visitVarInsn(ALOAD, 1)
            op()
            invokevirtual(contextClass, "setPc", "(I)V", false)
        }
    }

    /**
     * Pushes a given value on the operand stack.
     */
    fun push(value: UInt) {
        this.visitor.iconst(value.toInt())
    }

    /**
     * Adds two values on the operand stack and leaves the result
     * on top of the stack.
     *
     * An optional immediate value may be pushed to the stack
     * before the operation, when supplied.
     */
    fun iadd(value: UInt?) {
        this.visitor.run {
            if (value != null) {
                iconst(value.toInt())
            }
            visitInsn(IADD)
        }
    }

    /**
     * Bitwise ANDs two values on the operand stack and leaves the
     * result on top of the stack.
     *
     * An optional immediate value may be pushed to the stack
     * before the operation, when supplied.
     */
    fun iand(value: UInt?) {
        this.visitor.run {
            if (value != null) {
                iconst(value.toInt())
            }
            visitInsn(IAND)
        }
    }

    /**
     * Bitwise ORs two values on the operand stack and leaves the
     * result on top of the stack.
     *
     * An optional immediate value may be pushed to the stack
     * before the operation, when supplied.
     */
    fun ior(value: UInt?) {
        this.visitor.run {
            if (value != null) {
                iconst(value.toInt())
            }
            visitInsn(IOR)
        }
    }

    /**
     * Bitwise XORs two values on the operand stack and leaves the
     * result on top of the stack.
     *
     * An optional immediate value may be pushed to the stack
     * before the operation, when supplied.
     */
    fun ixor(value: UInt?) {
        this.visitor.run {
            if (value != null) {
                iconst(value.toInt())
            }
            visitInsn(IXOR)
        }
    }

    /**
     * Shifts the value on the operand stack to the left by a
     * given amount of bits.
     *
     * An optional immediate value may be pushed to the stack
     * as the bits to shift, when supplied.
     */
    fun ishl(value: UInt?) {
        this.visitor.run {
            if (value != null) {
                iconst(value.toInt())
            }
            visitInsn(ISHL)
        }
    }

    /**
     * Shifts the value on the operand stack to the right by a
     * given amount of bits.
     *
     * An optional immediate value may be pushed to the stack
     * as the bits to shift, when supplied.
     */
    fun ishr(value: UInt?) {
        this.visitor.run {
            if (value != null) {
                iconst(value.toInt())
            }
            visitInsn(ISHR)
        }
    }

    /**
     * Emits a call to [ExecutionContext.unimplemented].
     */
    fun generateUnimplementedStub() {
        this.visitor.run {
            visitVarInsn(ALOAD, 1)
            invokevirtual(contextClass, "unimplemented", "()V", false)
        }
    }
}
