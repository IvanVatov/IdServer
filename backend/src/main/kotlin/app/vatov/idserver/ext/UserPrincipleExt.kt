package app.vatov.idserver.ext

import app.vatov.idserver.Const
import app.vatov.idserver.exception.IdServerException
import app.vatov.idserver.model.UserPrincipal

@Throws(IdServerException::class)
fun UserPrincipal.checkAuthorizedAdmin(tenantId: Int) {
    if (roles.contains(Const.Administration.SUPER_ADMIN_ROLE)) {
        return
    }
    roles.forEach {
        if (it.startsWith(Const.Administration.TENANT_ADMIN_ROLE_PREFIX)) {

            if (it.substring(Const.Administration.TENANT_ADMIN_ROLE_PREFIX.length, it.length)
                    .toInt() == tenantId
            ) {
                return
            }
        }
    }
    throw IdServerException.FORBIDDEN
}

fun UserPrincipal.getOwnedTenantIds(): List<Int> {

    val result = ArrayList<Int>()

    roles.forEach {
        if (it.startsWith(Const.Administration.TENANT_ADMIN_ROLE_PREFIX)) {
            result.add(
                it.substring(Const.Administration.TENANT_ADMIN_ROLE_PREFIX.length, it.length)
                    .toInt()
            )
        }
    }
    return result
}