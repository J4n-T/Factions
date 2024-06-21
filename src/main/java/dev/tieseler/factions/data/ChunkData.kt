package dev.tieseler.factions.data

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "chunks", schema = "public")
class ChunkData {

    @Id
    var id: UUID = UUID.randomUUID()

    var x: Long = 0
    var z: Long = 0

    var state: ChunkState = ChunkState.WILDERNESS

    @ManyToOne
    var faction: Faction? = null

    @ManyToOne
    var roleClaims: RoleClaims? = null
}