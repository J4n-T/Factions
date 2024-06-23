package dev.tieseler.factions.util

import java.util.UUID

class UUIDUtil {

    fun parse(uuid: String?): UUID? {
        return if (uuid == null) null else try {
            UUID.fromString(uuid)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

}