package dev.tieseler.factions.language

import net.kyori.adventure.text.Component

interface Messages {

    fun failedToFetchPlayerData(): Component
    fun failedToFetchPlayerData(playerName: Component): Component
    fun failedToFetchFactionData(): Component
    fun failedToFetchFactionData(factionName: String): Component
    fun failedToFetchChunkData(): Component

    fun alreadyInFaction(): Component
    fun missingFactionName(): Component
    fun selectFactionMode(): Component
    fun missingFactionDescription(): Component
    fun factionAlreadyExists(factionName: String): Component
    fun factionCreated(): Component
    fun factionDisbanded(): Component
    fun factionNameChanged(): Component
    fun factionDescriptionChanged(): Component

    fun targetNotSameFaction(targetName: Component): Component

    fun playerNotFactionLeader(): Component
    fun playerNotInFaction(): Component

    fun chunkAlreadyClaimed(): Component

}
