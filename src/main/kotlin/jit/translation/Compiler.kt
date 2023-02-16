package io.github.vbe0201.jiffy.jit.translation

import java.lang.invoke.MethodHandles

/**
 * Compiles Java bytecode to callable instances of [Compiled].
 *
 * Objects which result from [Compiler.compile] may be thrown out
 * by the GC without having to worry about the underlying [ClassLoader].
 */
class Compiler {
    private val lookup = MethodHandles.lookup()

    /**
     * Compiles a given buffer of Java bytecode into a class instance.
     *
     * The compiled bytecode must be a class that implements [Compiled]
     * and has a callable constructor that does not take any arguments.
     */
    fun compile(code: ByteArray): Compiled {
        val cls = this.lookup.defineHiddenClass(code, true).lookupClass()
        return cls.getConstructor().newInstance() as Compiled
    }
}
