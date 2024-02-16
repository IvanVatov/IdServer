package app.vatov.idserver.repository

import app.vatov.idserver.IDServer
import app.vatov.idserver.database.ClientTable
import app.vatov.idserver.model.ClientPrincipal
import app.vatov.idserver.util.createShortUUID

object ClientRepository {

    fun getByClientIdAndSecret(
        tenantId: Int,
        clientId: String,
        clientSecret: String
    ): ClientPrincipal? {

        return ClientTable.getByClientIdAndSecret(tenantId, clientId, clientSecret)
    }

    fun create(tenantId: Int, clientId: String, application: String): ClientPrincipal {
        val generatedSecret = createShortUUID()

        val result = ClientTable.create(tenantId, clientId, application, generatedSecret)

        return if (result) {
            val client = ClientPrincipal(tenantId, clientId, application, generatedSecret)

            IDServer.getTenant(tenantId)?.apply {
                setClient(client)
            } ?: throw Exception("Tenant $tenantId do not exist")

            client
        } else throw Exception("Failed to create client")
    }

    fun delete(tenantId: Int, clientId: String): Boolean {
        val result = ClientTable.delete(tenantId, clientId)

        if (!result) throw Exception("Failed to delete client")

        IDServer.getTenant(tenantId)?.apply {
            removeClientId(clientId)
        }  ?: throw Exception("Tenant $tenantId do not exist")

        return result
    }

    fun update(
        client: ClientPrincipal
    ): Boolean {
        val result = ClientTable.update(client)

        if (!result) throw Exception("Failed to update client")

        IDServer.getTenant(client.tenantId)?.apply {

            val old = getClient(clientId = client.clientId)
                ?: throw Exception("ClientId ${client.clientId} do not exist")

            client.codes.putAll(old.codes)

            if (!ClientTable.update(client)) throw Exception("Failed to update client")

            setClient(client)

        } ?: throw Exception("Tenant ${client.tenantId} do not exist")

        return true
    }

    fun getAllForTenantId(tenantId: Int): List<ClientPrincipal> {

        return ClientTable.getAllByTenantId(tenantId)
    }

    fun getClientIdForTenantId(tenantId: Int, clientId: String): ClientPrincipal? {

        return ClientTable.getClientIdByTenantId(tenantId, clientId)
    }
}