package dev.tieseler.factions

import dev.tieseler.factions.data.*
import org.hibernate.SessionFactory
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.hibernate.cfg.Configuration
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

}