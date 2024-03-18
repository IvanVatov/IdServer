package app.vatov.idserver.ext

import app.vatov.idserver.Const
import app.vatov.idserver.model.UserPrincipal

fun UserPrincipal.isAuthorizedAdmin(tenantId: Int): Boolean {
    if (role.contains(Const.Administration.SUPER_ADMIN_ROLE)) {
        return true
    }
    role.forEach {
        if (it.startsWith(Const.Administration.TENANT_ADMIN_ROLE_PREFIX)) {

            if (it.substring(Const.Administration.TENANT_ADMIN_ROLE_PREFIX.length, it.length)
                    .toInt() == tenantId
            ) {
                return true
            }
        }
    }
    return false
}

fun UserPrincipal.getOwnedTenantIds(): List<Int> {

    val result = ArrayList<Int>()

    role.forEach {
        if (it.startsWith(Const.Administration.TENANT_ADMIN_ROLE_PREFIX)) {
            result.add(
                it.substring(Const.Administration.TENANT_ADMIN_ROLE_PREFIX.length, it.length)
                    .toInt()
            )
        }
    }
    return result
}