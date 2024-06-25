package dev.tieseler.factions

import dev.tieseler.factions.commands.ChunkCommand
import dev.tieseler.factions.commands.FactionCommand
import dev.tieseler.factions.commands.InviteCommand
import dev.tieseler.factions.commands.PepoSitCommand
import dev.tieseler.factions.data.ChunkData
import dev.tieseler.factions.language.German
import dev.tieseler.factions.language.Messages
import dev.tieseler.factions.listeners.ChatListener
import dev.tieseler.factions.listeners.ChunkListener
import dev.tieseler.factions.listeners.PigListener
import dev.tieseler.factions.listeners.PlayerListener
import dev.tieseler.factions.papi.FactionsExpansion
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.Bukkit
import org.bukkit.entity.Pig
import org.bukkit.plugin.java.JavaPlugin
import org.hibernate.Session
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class Factions : JavaPlugin() {

    lateinit var prefix: TextComponent
    lateinit var messages: Messages

    var databaseConnector: DatabaseConnector? = null
    val pigs = ConcurrentHashMap<UUID, Pig>()
    var readSession: Session? = null
    val chunks = ConcurrentHashMap<UUID, ChunkData>()

    //TODO: Message of the day
    //TODO: Disable Crystal PvP

    override fun onEnable() {
        instance = this
        saveDefaultConfig()

        prefix = Component.text(config.getString("prefix")!!)
        messages = German()

        databaseConnector = DatabaseConnector(
            config.getString("hostname")!!,
            config.getInt("port"),
            config.getString("database")!!,
            config.getString("username")!!,
            config.getString("password")!!
        )

        databaseConnector?.connect()
        readSession = databaseConnector!!.sessionFactory!!.openSession()

        FactionsExpansion().register()

        server.pluginManager.registerEvents(ChunkListener(), this)
        server.pluginManager.registerEvents(PigListener(), this)
        server.pluginManager.registerEvents(PlayerListener(), this)
        server.pluginManager.registerEvents(ChatListener(), this)

        getCommand("chunk")?.setExecutor(ChunkCommand())
        getCommand("peposit")?.setExecutor(PepoSitCommand())
        getCommand("faction")?.setExecutor(FactionCommand())
        getCommand("invite")?.setExecutor(InviteCommand())

        Bukkit.getGlobalRegionScheduler().runAtFixedRate(this, {
            pigs.forEach(100) { id, pig ->
                val fetchedEntity = Bukkit.getServer().getEntity(id)
                if (fetchedEntity == null) {
                    pigs.remove(id)
                    return@forEach
                }
                //TODO: Check if the pig is flying by half a block -.-
                val scheduler = fetchedEntity.scheduler
                scheduler.run(instance, {
                    pig.setRotation(pig.passengers.first().yaw, pig.pitch)
                    /*
                    Pig location offset = 0.8
                    Player location offset = 0.54
                     */
                    //val block = pig.passengers.first().location.add(0.0, 0.5, 0.0).block
                    val block = pig.location.add(0.0, 0.8, 0.0).block
                    if (block.type.isAir) {
                        pig.remove()
                        pigs.remove(id)
                    }
                }, null)
            }
        }, 20, 1)
    }

    override fun onDisable() {
        pigs.forEach(100) { id, pig ->
            val fetchedEntity = Bukkit.getServer().getEntity(id)
            if (fetchedEntity == null) {
                pigs.remove(id)
                return@forEach
            }
            val scheduler = fetchedEntity.scheduler
            scheduler.run(instance, {
                pig.remove()
                pigs.remove(id)
            }, null)
        }
        readSession?.close()
        databaseConnector?.sessionFactory!!.close()
        logger.info("Factions plugin disabled")
    }

    companion object {
        lateinit var instance: Factions
    }

}
