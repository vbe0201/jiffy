package io.github.vbe0201.jiffy

import io.github.vbe0201.jiffy.jit.decoder.Instruction

fun main(args: Array<String>) {
    println("${Instruction(0x8FA20010U).kind()}")
    println("${Instruction(0U).kind()}")
    println("${Instruction(0x2442FFFFU).kind()}")
    println("${Instruction(0xAFA20010U).kind()}")
    println("${Instruction(0x1443000BU).kind()}")
}
