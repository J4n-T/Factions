package dev.tieseler.factions.data

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "role_claims", schema = "public")
class RoleClaims {

    @Id
    var id: UUID = UUID.randomUUID()

    @ManyToOne
    var role: Role? = null

    var claim: Claim? = null
    var value: Boolean = false

    @ManyToOne
    var chunkData: ChunkData? = null
}