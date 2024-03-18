package app.vatov.idserver

import app.vatov.idserver.model.Tenant
import app.vatov.idserver.repository.TenantRepository
import java.util.TreeMap


object IDServer {

    private var TENANT_MAP_HOST = HashMap<String, Tenant>()
    private var TENANT_MAP_ID = TreeMap<Int, Tenant>()

    init {
        loadTenantMap()
    }

    private fun loadTenantMap() {

        TENANT_MAP_HOST.clear()
        TENANT_MAP_ID.clear()

        val tenants = TenantRepository.getAll()

        tenants.forEach { tenant ->
            updateTenant(tenant)
        }
    }

    fun updateTenant(tenant: Tenant) {
        TENANT_MAP_HOST[tenant.host] = tenant
        TENANT_MAP_ID[tenant.id] = tenant
    }

    fun removeTenant(tenantId: Int) {
        TENANT_MAP_ID[tenantId]?.let {
            TENANT_MAP_ID.remove(it.id)
            TENANT_MAP_HOST.remove(it.host)
        }
    }

    fun getTenant(id: Int): Tenant? {
        return TENANT_MAP_ID[id]
    }

    fun getTenant(host: String): Tenant? {
        return TENANT_MAP_HOST[host]
    }

    fun getAllTenants(): List<Tenant> {
        return TENANT_MAP_ID.values.toList()
    }

    fun getTenants(ids: List<Int>): List<Tenant> {
        return TENANT_MAP_ID.values.filter { it.id in ids }
    }
}