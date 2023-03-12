package io.github.vbe0201.jiffy.jit.translation

import io.github.oshai.KotlinLogging
import io.github.vbe0201.jiffy.cpu.ExceptionKind
import io.github.vbe0201.jiffy.jit.codegen.BlockBuilder
import io.github.vbe0201.jiffy.jit.codegen.jvm.BytecodeEmitter
import io.github.vbe0201.jiffy.jit.decoder.INSTRUCTION_SIZE
import io.github.vbe0201.jiffy.jit.jitLogger
import io.github.vbe0201.jiffy.jit.state.ExecutionContext
import io.github.vbe0201.jiffy.jit.state.State
import io.github.vbe0201.jiffy.utils.isAligned

/**
 * The runtime code translator.
 *
 * This is the heart of the JIT; it is responsible for efficient translation
 * of MIPS instructions to [Block]s of executable Java Bytecode.
 *
 * The translator also manages code blocks and recompiles them on demand.
 */
class Translator {
    private val cache = BlockCache()

    private val compiler = Compiler()

    /**
     * Runs the translator until the program has finished or an exception
     * occurs, whichever happens first.
     *
     * Takes a configured [ExecutionContext] on which the translated
     * code will operate on.
     */
    fun execute(context: ExecutionContext) {
        context.state = State.RUNNING

        while (context.state == State.RUNNING) {
            // Make sure the MIPS program counter is aligned to full
            // instruction boundaries or generate an exception.
            if (!context.pc.isAligned(INSTRUCTION_SIZE)) {
                context.raiseException(
                    context.pc,
                    false,
                    ExceptionKind.UNALIGNED_LOAD
                )
            }

            jitLogger.debug {
                "Executing next block from 0x${context.pc.toString(16)}"
            }

            // Find or translate the next code block and execute it.
            val block = findOrCompileBlock(context, context.pc)
            block.code.execute(context)
        }
    }

    private fun findOrCompileBlock(ctx: ExecutionContext, addr: UInt): Block {
        var block = this.cache.get(addr)

        if (block == null) {
            jitLogger.debug {
                "Cache miss; recompiling block at 0x${addr.toString(16)}"
            }

            block = compileBlock(ctx, addr)
            this.cache.insert(block)
        }

        return block
    }

    private fun compileBlock(ctx: ExecutionContext, addr: UInt): Block {
        val builder = BlockBuilder(BytecodeEmitter())

        val len = builder.build(ctx, addr)
        return Block(
            this.compiler.compile(builder.emitter.finish()),
            addr,
            len,
        )
    }
}
