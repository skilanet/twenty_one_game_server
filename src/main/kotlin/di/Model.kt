package di

import model.game.GameManager
import org.koin.dsl.module

val modelModule = module(createdAtStart = true) {
    single<GameManager> { GameManager() }
}