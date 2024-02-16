package app.vatov.idserver.repository

import app.vatov.idserver.IDServer
import app.vatov.idserver.database.TenantTable
import app.vatov.idserver.model.Tenant

object TenantRepository {

    fun create(name: String, host: String): Tenant {

        val tenant = TenantTable.createTenantTransaction(name, host) ?: throw Exception("Couldn't create tenant")

        IDServer.updateTenant(tenant)

        return tenant
    }

    fun getAll(): List<Tenant> {
        return TenantTable.getAll()
    }

    fun getById(tenantId: Int): Tenant? {
        return TenantTable.getById(tenantId)
    }

    fun remove(id: Int): Boolean {
        val result = TenantTable.deleteTenant(id)
        if (result) {
            IDServer.removeTenant(id)
        }
        return result
    }
}