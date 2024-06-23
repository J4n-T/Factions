package dev.tieseler.factions.language

import dev.tieseler.factions.Factions
import net.kyori.adventure.text.Component

import net.kyori.adventure.text.Component.*
import net.kyori.adventure.text.format.NamedTextColor.*

class German : Messages {

    private val prefix = Factions.instance.prefix

    override fun failedToFetchPlayerData(): Component {
        return prefix.append(text(" Dein Profil konnte nicht abgerufen werden").color(RED))
    }

    override fun failedToFetchPlayerData(playerName: Component): Component {
        return prefix.append(text(" Das Profil von ").color(RED).append(playerName).append(text(" konnte nicht abgerufen werden").color(RED)))
    }

    override fun failedToFetchFactionData(): Component {
        return prefix.append(text(" Die Daten deiner Faction konnten nicht abgerufen werden").color(RED))
    }

    override fun failedToFetchFactionData(factionName: String): Component {
        return prefix.append(text(" Die Daten der Faction ").color(RED).append(text(factionName).append(text(" konnten nicht abgerufen werden").color(RED))))
    }

    override fun failedToFetchChunkData(): Component {
        return prefix.append(text(" Die Daten über den Chunk konnten nicht abgerufen werden").color(RED))
    }

    override fun alreadyInFaction(): Component {
        return prefix.append(text(" Du bist bereits in einer Faction").color(RED))
    }

    override fun missingFactionName(): Component {
        return prefix.append(text(" Wir sind hier nicht bei \"No Game No Life\"! Bitte gib einen Namen (ohne Leerzeichen) an!").color(RED))
    }

    override fun selectFactionMode(): Component {
        return prefix.append(text(" Bitte entscheide dich dafür, ob deine Faction neutral oder aggressiv sein soll!").color(RED))
    }

    override fun missingFactionDescription(): Component {
        return prefix.append(text(" Bitte gib eine Beschreibung für deine Faction ein").color(RED))
    }

    override fun factionAlreadyExists(factionName: String): Component {
        return prefix.append(text(" Die Faction ").color(RED).append(text(factionName).color(GREEN).append(text(" existiert bereits").color(RED))))
    }

    override fun playerNotInFaction(): Component {
        return prefix.append(text(" Du bist in keiner Factio").color(RED))
    }

    override fun chunkAlreadyClaimed(): Component {
        return prefix.append(text(" Dieser Chunk wurde bereits geclaimt").color(RED))
    }

    override fun factionCreated(): Component {
        return prefix.append(text(" Deine Faction wurde gegründet").color(GREEN))
    }

    override fun playerNotFactionLeader(): Component {
        return prefix.append(text(" Du bist nicht der Anführer der Faction").color(RED))
    }

    override fun factionDisbanded(): Component {
        return prefix.append(text(" Deine Faction wurde aufgelöst").color(GREEN))
    }

    override fun factionNameChanged(): Component {
        return prefix.append(text(" Der Name deiner Faction wurde geändert").color(GREEN))
    }

    override fun factionDescriptionChanged(): Component {
        return prefix.append(text(" Die Beschreibung deiner Faction wurde geändert").color(GREEN))
    }

    override fun targetNotSameFaction(targetName: Component): Component {
        return prefix.append(text(" Der Spieler ").color(RED).append(targetName).append(text(" ist nicht in deiner Faction").color(RED)))
    }

}