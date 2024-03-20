package app.vatov.idserver.repository

import app.vatov.idserver.Configuration
import app.vatov.idserver.IDServer
import app.vatov.idserver.database.TenantRSAKeyPairTable
import app.vatov.idserver.model.RSAKeyPair
import app.vatov.idserver.model.Tenant
import app.vatov.idserver.model.TenantRSAKeyPair
import app.vatov.idserver.model.encodeToString
import app.vatov.idserver.util.createShortUUID
import java.time.Instant

object TenantRSAKeyPairRepository {

    fun create(tenant: Tenant): TenantRSAKeyPair {

        val id = createShortUUID()
        val rsaKeyPair = RSAKeyPair.generate(Configuration.JWT_SIGNING_KEY_SIZE)

        val tenantRSAKeyPair = TenantRSAKeyPair(
            id,
            tenant.id,
            rsaKeyPair.publicKey.encodeToString(),
            rsaKeyPair.privateKey.encodeToString(),
            Instant.now()
        )

        if (!TenantRSAKeyPairTable.insert(tenantRSAKeyPair)) throw Exception()

        tenant.addRSAKeyPair(tenantRSAKeyPair)


        return tenantRSAKeyPair
    }

    fun delete(tenantId: Int, keyId: String): Boolean {

        val tenant = IDServer.getTenant(tenantId)

        val key = tenant.getValidPublicKeys().find { it.id == keyId }
            ?: throw Exception("Key Id: $keyId not found")

        if (TenantRSAKeyPairTable.delete(tenantId, keyId)) {
            tenant.removeRSAKeyPair(key.id)
            return true
        }

        return false
    }

    fun getAllForTenant(tenantId: Int): List<TenantRSAKeyPair> {

        return TenantRSAKeyPairTable.getByTenantId(tenantId)
    }

}