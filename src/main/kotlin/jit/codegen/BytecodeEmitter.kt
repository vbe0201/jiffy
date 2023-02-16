package io.github.vbe0201.jiffy.jit.codegen

import io.github.vbe0201.jiffy.jit.state.ExecutionContext
import io.github.vbe0201.jiffy.jit.translation.Compiled
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

// The only officially supported JDK by jiffy is 19.
// When bumping to a newer version, also change this constant.
private const val CLASS_VERSION = 52 + 19 - 18

// The name of every generated class. Since we're loading classes
// anonymously into the JVM at runtime, we're not concerned about
// their names and just go with something that saves us cost.
private const val GENERATED_CLASS =
    "io/github/vbe0201/jiffy/jit/translation/EmittedBlockImpl"

// Symbols for miscellaneous classes used during code generation.
private val compiledInterface = Type.getInternalName(Compiled::class.java)
private val contextClass = Type.getInternalName(ExecutionContext::class.java)

private fun makeWriterWithProlog(): ClassWriter {
    val writer = ClassWriter(ClassWriter.COMPUTE_MAXS)

    // Start the new class to generate.
    writer.visit(
        CLASS_VERSION,
        Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER,
        GENERATED_CLASS,
        null,
        "java/lang/Object",
        arrayOf(compiledInterface)
    )

    // Generate the default constructor for every generated class.
    // This must be kept in sync with the requirements of the
    // `Compiler` class for instantiating the generated types.
    val visitor = writer.visitMethod(
        Opcodes.ACC_PUBLIC,
        "<init>",
        "()V",
        null,
        null
    )

    visitor.visitVarInsn(Opcodes.ALOAD, 0)
    visitor.visitMethodInsn(
        Opcodes.INVOKESPECIAL,
        "java/lang/Object",
        "<init>",
        "()V",
        false
    )
    visitor.visitInsn(Opcodes.RETURN)

    visitor.visitMaxs(0, 0)
    visitor.visitEnd()

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
    private val writer = makeWriterWithProlog()

    // Since Kotlin guarantees initialization order of properties,
    // the very next thing for us to do is to start the generation
    // of the `Compiled.execute` implementation.
    private var visitor = this.writer.visitMethod(
        Opcodes.ACC_PUBLIC,
        "execute",
        "(L$contextClass;)V",
        null,
        null
    )

    /**
     * Completes the generation of a class and returns the generated code.
     *
     * The resulting bytes define a class implementing [Compiled], as per
     * [BytecodeEmitter] docs.
     */
    fun finish(): ByteArray {
        // Finish the implementation of `Compiled.execute`.
        this.visitor.visitMaxs(0, 0)
        this.visitor.visitEnd()

        // Finish the implementation of the generated class.
        this.writer.visitEnd()

        return this.writer.toByteArray()
    }

    /**
     * Emits a call to [ExecutionContext.unimplemented] into the
     * implementation of the generated class.
     */
    fun generateUnimplementedStub() {
        visitor.visitVarInsn(Opcodes.ALOAD, 1)
        visitor.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            contextClass,
            "unimplemented",
            "()V",
            false
        )
        visitor.visitInsn(Opcodes.RETURN)
    }

    // Generates a default constructor for the object which basically
    // does nothing. Note that the `Compiler` expects things to work
    // this way, so don't make changes here without adapting there.
    private fun generateConstructor() {
        val visitor = this.writer.visitMethod(
            Opcodes.ACC_PUBLIC,
            "<init>",
            "()V",
            null,
            null
        )

        visitor.visitVarInsn(Opcodes.ALOAD, 0)
        visitor.visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            "java/lang/Object",
            "<init>",
            "()V",
            false
        )
        visitor.visitInsn(Opcodes.RETURN)

        visitor.visitMaxs(0, 0)
        visitor.visitEnd()
    }
}
