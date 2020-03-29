package com.cdsap.talaiot.publisher

import java.util.concurrent.Executor

class TestExecutor : Executor {
    override fun execute(command: Runnable?) {
        System.out.println("assss")
        command?.run()
        println("@22222")
    }

}