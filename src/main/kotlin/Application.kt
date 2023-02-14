package io.github.vbe0201.jiffy

import io.github.vbe0201.jiffy.jit.decoder.Instruction
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

fun main(args: Array<String>) {
    // Read the BIOS image and configure ByteBuffer for reading instructions.
    val bios = ByteBuffer.wrap(File("assets/scph1001.bin").readBytes())
    bios.order(ByteOrder.LITTLE_ENDIAN)

    for (i in 0..10) {
        val raw = bios.getInt(i * 4).toUInt()
        val insn = Instruction(raw)
        println("[${i * 4}] ($raw) ${insn.kind()}")
    }
}
