package dev.tieseler.factions

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.tieseler.factions.data.*
import org.hibernate.SessionFactory
import org.hibernate.Transaction
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.hibernate.cfg.Configuration
import java.sql.Connection
import java.sql.ResultSet
import java.util.*

import org.hibernate.cfg.Environment.*
import org.hibernate.service.ServiceRegistry

class DatabaseConnector(
    private val host: String,
    private val port: Int,
    private val database: String,
    private val username: String,
    private val password: String
) {

    var sessionFactory: SessionFactory? = null

    fun connect() {
        // DO NOT REMOVE
        Class.forName("org.postgresql.Driver");

        val configuration = Configuration()
        val properties = Properties()
        properties[JAKARTA_JDBC_URL] = "jdbc:postgresql://$host:$port/$database"
        properties[JAKARTA_JDBC_USER] = username
        properties[JAKARTA_JDBC_PASSWORD] = password
        properties[JAKARTA_JDBC_DRIVER] = "org.postgresql.Driver"
        properties[HBM2DDL_AUTO] = "update"
        properties[CONNECTION_PROVIDER] = "org.hibernate.hikaricp.internal.HikariCPConnectionProvider"
        properties[CURRENT_SESSION_CONTEXT_CLASS] = "thread"
        properties[SHOW_SQL] = "false"


        configuration.setProperties(properties)
        configuration.addAnnotatedClass(ChunkData::class.java)
        configuration.addAnnotatedClass(Faction::class.java)
        configuration.addAnnotatedClass(FactionInvite::class.java)
        configuration.addAnnotatedClass(FactionPlayer::class.java)
        configuration.addAnnotatedClass(Role::class.java)
        configuration.addAnnotatedClass(RoleClaims::class.java)

        val serviceRegistry: ServiceRegistry = StandardServiceRegistryBuilder().applySettings(configuration.properties).build()
        sessionFactory = configuration.buildSessionFactory(serviceRegistry)
    }

    fun saveFactionPlayer(player: FactionPlayer): FactionPlayer {
        return save(player) as FactionPlayer
    }

    fun saveFaction(faction: Faction): Faction {
        return save(faction) as Faction
    }

    private fun save(entity: Any): Any {
        val session = sessionFactory!!.openSession()
        session.beginTransaction()
        session.persist(entity)
        session.transaction.commit()
        session.close()
        return entity
    }

    private fun get(entity: Any, id: UUID): Any {
        val session = sessionFactory!!.openSession()
        val transaction: Transaction = session.beginTransaction()
        val result = session.get(entity::class.java, id)
        transaction.commit()
        session.close()
        return result
    }



}