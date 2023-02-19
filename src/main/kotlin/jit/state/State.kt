package io.github.vbe0201.jiffy.jit.state

/**
 * The state of an [ExecutionContext], which will be modified over
 * the course of running an application.
 */
enum class State {
    /**
     * The initial state in which the context was created.
     */
    INITIAL,

    /**
     * Translation has been started and the context is actively
     * being operated on.
     */
    RUNNING,

    /**
     * The program has finished or translation has decided to
     * stop the program.
     */
    CLOSED
}
