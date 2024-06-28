package dev.tieseler.factions

import co.aikar.commands.PaperCommandManager
import dev.tieseler.factions.commands.*
import dev.tieseler.factions.data.ChunkData
import dev.tieseler.factions.data.FactionInvite
import dev.tieseler.factions.data.FactionPlayer
import dev.tieseler.factions.language.German
import dev.tieseler.factions.language.Messages
import dev.tieseler.factions.listeners.*
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

    //TODO: Rolesystem
    //TODO: Check Tnt explosion in claims

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

        if (config.getBoolean("pvp.crystal")) server.pluginManager.registerEvents(CrystalListener(), this)
        if (config.getBoolean("pvp.tnt_minecart")) server.pluginManager.registerEvents(TntMinecartListener(), this)
        if (config.getBoolean("pvp.respawn_anchor")) server.pluginManager.registerEvents(RespawnAnchorListener(), this)

        /*getCommand("peposit")?.setExecutor(PepoSitCommand())
        getCommand("faction")?.setExecutor(FactionCommand())
        getCommand("invite")?.setExecutor(InviteCommand())*/

        val commandManager = PaperCommandManager(this)
        commandManager.registerCommand(FactionsChunkCommand())
        commandManager.registerCommand(FactionsCreateCommand())
        commandManager.registerCommand(FactionsDisbandCommand())
        commandManager.registerCommand(FactionsInviteCommand())
        commandManager.registerCommand(FactionsKickCommand())
        commandManager.registerCommand(PepoSitCommand())

        commandManager.commandCompletions.registerCompletion("factionsInvites") { context ->
            databaseConnector?.sessionFactory?.openSession()?.createQuery("FROM FactionInvite WHERE target = :target_id", FactionInvite::class.java)?.setParameter("target_id", context.player.uniqueId)?.list()?.map { it.faction!!.name } ?: mutableListOf()
        }
        commandManager.commandCompletions.registerCompletion("factionsMembers") { context ->
            val session = databaseConnector?.sessionFactory?.openSession() ?: return@registerCompletion mutableListOf()
            val factionPlayer = session.get(FactionPlayer::class.java, context.player.uniqueId)
            if (factionPlayer == null) {
                session.close()
                return@registerCompletion mutableListOf()
            }

            val members = factionPlayer.faction?.members?.map { Bukkit.getOfflinePlayer(it.id!!).name } ?: mutableListOf()
            session.close()
            return@registerCompletion members
        }

        Bukkit.getGlobalRegionScheduler().runAtFixedRate(this, {
            pigs.forEach(100) { id, pig ->
                val fetchedEntity = Bukkit.getServer().getEntity(id)
                if (fetchedEntity == null) {
                    pigs.remove(id)
                    return@forEach
                }
                val scheduler = fetchedEntity.scheduler
                scheduler.run(instance, {
                    pig.setRotation(pig.passengers.first().yaw, pig.pitch)
                    /*
                    Pig location offset = 0.8
                    Player location offset = 0.54
                     */
                    val block = pig.location.add(0.0, 0.79, 0.0).block
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
