package com.addx.ai.demo

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test

class test1 {

    @Test
    fun test111(){
        System.out.println("----------------------adf")
    }

    @Test
    fun fff() =  runBlocking { // this: CoroutineScope
        repeat(1000) { i ->
            println("I'm sleeping $i ...")
            delay(500L)
        }

        println("Coroutine scope is over") // 这一行在内嵌 launch 执行完毕后才输出
    }
}