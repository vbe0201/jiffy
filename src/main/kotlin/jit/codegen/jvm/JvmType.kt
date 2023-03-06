package io.github.vbe0201.jiffy.jit.codegen.jvm

import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

/**
 * Represents a data type on the JVM with associated opcode values
 * for common arithmetic instructions.
 */
enum class JvmType(val mask: UInt, private val ty: Type) {
    /**
     * The [Byte] (or [UByte]) type.
     */
    BYTE(0xFFU, Type.BYTE_TYPE),

    /**
     * The [Short] (or [UShort]) type.
     */
    SHORT(0xFFFFU, Type.SHORT_TYPE),

    /**
     * The [Int] (or [UInt]) type.
     */
    INT(0xFFFF_FFFFU, Type.INT_TYPE),

    /**
     * The [Long] (or [ULong]) type.
     */
    LONG(0U, Type.LONG_TYPE);

    // Common arithmetic opcodes for every value type.
    val add: Int = this.ty.getOpcode(Opcodes.IADD)
    val sub: Int = this.ty.getOpcode(Opcodes.ISUB)
    val mult: Int = this.ty.getOpcode(Opcodes.IMUL)
    val div: Int = this.ty.getOpcode(Opcodes.IDIV)
    val and: Int = this.ty.getOpcode(Opcodes.IAND)
    val or: Int = this.ty.getOpcode(Opcodes.IOR)
    val xor: Int = this.ty.getOpcode(Opcodes.IXOR)
    val shl: Int = this.ty.getOpcode(Opcodes.ISHL)
    val shr: Int = this.ty.getOpcode(Opcodes.ISHR)
    val ushr: Int = this.ty.getOpcode(Opcodes.IUSHR)

    // Local variable access for every value type.
    val loadLocal: Int = this.ty.getOpcode(Opcodes.ILOAD)
    val storeLocal: Int = this.ty.getOpcode(Opcodes.ISTORE)
}