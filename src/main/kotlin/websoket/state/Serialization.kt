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

    polymorphic(GameEvent::class) {
        subclass(GameEvent.PlayerJoined::class, GameEvent.PlayerJoined.serializer())
        subclass(GameEvent.GameStarted::class, GameEvent.GameStarted.serializer())
        subclass(GameEvent.CardDealt::class, GameEvent.CardDealt.serializer())
        subclass(GameEvent.PlayerTurn::class, GameEvent.PlayerTurn.serializer())
        subclass(GameEvent.PlayerStood::class, GameEvent.PlayerStood.serializer())
        subclass(GameEvent.GameOver::class, GameEvent.GameOver.serializer())
        subclass(GameEvent.Error::class, GameEvent.Error.serializer())
        subclass(GameEvent.Info::class, GameEvent.Info.serializer())
        subclass(GameEvent.AnotherPlayerTookCard::class, GameEvent.AnotherPlayerTookCard.serializer())
        subclass(GameEvent.OpponentCards::class, GameEvent.OpponentCards.serializer())
    }
}