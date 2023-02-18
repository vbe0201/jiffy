package io.github.vbe0201.jiffy.jit.codegen

import io.github.vbe0201.jiffy.jit.state.ExecutionContext
import io.github.vbe0201.jiffy.jit.translation.Compiled
import io.github.vbe0201.jiffy.jit.translation.Compiler
import org.objectweb.asm.commons.InstructionAdapter
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type

// The only officially supported JDK by jiffy is 19.
// When bumping to a newer version, also change this constant.
private const val CLASS_VERSION = V19

// Symbols for miscellaneous classes used during code generation.
private val compiledInterface = Type.getInternalName(Compiled::class.java)
private val compilerClass = Type.getInternalName(Compiler::class.java)
private val contextClass = Type.getInternalName(ExecutionContext::class.java)

private fun makeWriterWithPrologue(): ClassWriter {
    val writer = ClassWriter(ClassWriter.COMPUTE_MAXS)

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
            visitInsn(IASTORE)
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
     * Pushes a given value on the operand stack.
     */
    fun push(value: UInt) {
        this.visitor.iconst(value.toInt())
    }

    /**
     * Adds a given value to an operand on the stack and leaves the
     * result on the stack.
     */
    fun iadd(value: UInt) {
        this.visitor.run {
            iconst(value.toInt())
            visitInsn(IADD)
        }
    }

    /**
     * Bitwise ORs a value on the stack with a given value and leaves
     * the result on the stack.
     */
    fun ior(value: UInt) {
        this.visitor.run {
            iconst(value.toInt())
            visitInsn(IOR)
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
