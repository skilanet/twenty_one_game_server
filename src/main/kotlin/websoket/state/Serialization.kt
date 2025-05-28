package websoket.state

import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

val gameModule = SerializersModule {
    polymorphic(GameCommand::class) {
        subclass(GameCommand.Join::class, GameCommand.Join.serializer())
        subclass(GameCommand.Hit::class, GameCommand.Hit.serializer())
        subclass(GameCommand.StartGame::class, GameCommand.StartGame.serializer())
        subclass(GameCommand.Stand::class, GameCommand.Stand.serializer())
    }
}