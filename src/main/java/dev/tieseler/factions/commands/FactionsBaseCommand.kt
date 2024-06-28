package dev.tieseler.factions.commands

import co.aikar.commands.BaseCommand
import dev.tieseler.factions.DatabaseConnector
import dev.tieseler.factions.Factions
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.hibernate.Session
import org.hibernate.Transaction
import java.util.UUID

open class FactionsBaseCommand : BaseCommand() {

    private val plugin = Factions.instance

    var session: Session? = null
    val messages = plugin.messages

    private var databaseConnector: DatabaseConnector? = null
    private var transaction: Transaction? = null

    fun init(db: Boolean, session: Boolean, transaction: Boolean) {
        if (db) databaseConnector = plugin.databaseConnector
        if (session) this.session = databaseConnector!!.sessionFactory!!.openSession()
        if (transaction) this.transaction = this.session!!.beginTransaction()
    }

    fun close() {
        if (transaction != null && transaction!!.isActive) transaction!!.commit()
        session!!.close()
    }

    fun parseUUIDFromPersistentDataContainer(dataContainer: PersistentDataContainer, key: String): UUID? {
        val uuid = dataContainer.get(NamespacedKey.fromString(key)!!, PersistentDataType.STRING)
        return if (uuid == null) null else try {
            UUID.fromString(uuid)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

}