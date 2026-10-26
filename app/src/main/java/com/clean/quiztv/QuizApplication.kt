package com.clean.quiztv

import android.app.Application
import com.clean.quiztv.data.QuestionPackRepository
import com.clean.quiztv.data.llm.LlmService
import com.clean.quiztv.game.GameStateMachine
import com.clean.quiztv.server.HttpServer

class QuizApplication : Application() {

    lateinit var packRepository: QuestionPackRepository
        private set

    lateinit var stateMachine: GameStateMachine
        private set

    lateinit var llmService: LlmService
        private set

    lateinit var server: HttpServer
        private set

    override fun onCreate() {
        super.onCreate()
        packRepository = QuestionPackRepository(this)
        llmService = LlmService(packRepository)
        stateMachine = GameStateMachine(packRepository)
        server = HttpServer(this, stateMachine, llmService, port = 8080)
        server.start()
    }

    override fun onTerminate() {
        super.onTerminate()
        server.stop()
        llmService.close()
    }
}
