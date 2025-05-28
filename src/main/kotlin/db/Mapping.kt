package db

import kotlinx.coroutines.Dispatchers
import model.game.User
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
    newSuspendedTransaction(context = Dispatchers.IO, statement = block)


@OptIn(ExperimentalUuidApi::class)
fun Uuid.toUUID(): UUID = UUID.fromString(this.toString())