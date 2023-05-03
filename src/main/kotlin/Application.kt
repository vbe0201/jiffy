package io.github.vbe0201.jiffy

import io.github.oshai.KotlinLogging
import io.github.vbe0201.jiffy.bus.Bus
import io.github.vbe0201.jiffy.cpu.Cop0
import io.github.vbe0201.jiffy.gui.EmulatorWindow
import io.github.vbe0201.jiffy.jit.state.ExecutionContext
import io.github.vbe0201.jiffy.jit.translation.Translator
import java.io.File
import java.nio.ByteBuffer
import kotlin.concurrent.thread

private val logger = KotlinLogging.logger("App")

fun main(args: Array<String>) {
    logger.info("Starting jiffy")

    // Read the BIOS image and construct the CPU bus from it.
    val bios = ByteBuffer.wrap(File("assets/scph1001.bin").readBytes())
    val bus = Bus(bios)

    // Construct the JIT state.
    val context = ExecutionContext(bus, Cop0())
    val translator = Translator()

    EmulatorWindow().use {
        // Spawn the JIT execution engine to a different thread.
        val jitThread = thread(name = "JIT Translator") {
            translator.execute(context)
        }

        // Run the window until a shutdown is requested.
        it.run()
    }
}
