package io.github.vbe0201.jiffy.jit.translation

import io.github.vbe0201.jiffy.jit.codegen.BlockBuilder
import io.github.vbe0201.jiffy.jit.codegen.BytecodeEmitter
import io.github.vbe0201.jiffy.jit.state.ExecutionContext
import io.github.vbe0201.jiffy.jit.state.State

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
            val block = findOrCompileBlock(context, context.pc)
            block.code.execute(context)
        }
    }

    private fun findOrCompileBlock(ctx: ExecutionContext, addr: UInt): Block {
        var block = this.cache.get(addr)

        if (block == null) {
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