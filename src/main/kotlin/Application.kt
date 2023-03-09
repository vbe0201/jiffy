package io.github.vbe0201.jiffy

import io.github.vbe0201.jiffy.bus.Bus
import io.github.vbe0201.jiffy.cpu.Cop0
import io.github.vbe0201.jiffy.jit.state.ExecutionContext
import io.github.vbe0201.jiffy.jit.translation.Translator
import java.io.File
import java.nio.ByteBuffer

fun main(args: Array<String>) {
    // Read the BIOS image and construct the CPU bus from it.
    val bios = ByteBuffer.wrap(File("assets/scph1001.bin").readBytes())
    val bus = Bus(bios)

    // Construct the JIT state.
    val context = ExecutionContext(bus, Cop0())
    val translator = Translator()

    // Run the JIT until the program is done.
    translator.execute(context)
}
