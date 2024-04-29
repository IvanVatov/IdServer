package app.vatov.idserver.routes.admin

import app.vatov.idserver.Const
import app.vatov.idserver.IDServer
import app.vatov.idserver.exception.IdServerException
import app.vatov.idserver.ext.checkAuthorizedAdmin
import app.vatov.idserver.ext.getOwnedTenantIds
import app.vatov.idserver.model.PublicKeyInfo
import app.vatov.idserver.repository.TenantRSAKeyPairRepository
import app.vatov.idserver.repository.TenantRepository
import app.vatov.idserver.request.admin.CreateTenantRequest
import app.vatov.idserver.request.admin.DeleteKeyRequest
import app.vatov.idserver.request.admin.DeleteTenantRequest
import app.vatov.idserver.request.admin.RotateKeyRequest
import app.vatov.idserver.response.CurrentKeyResponse
import app.vatov.idserver.response.ResultResponse
import app.vatov.idserver.ext.getIntParam
import app.vatov.idserver.ext.getUserPrincipal
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.tenantRoutes() {

    route("tenant") {

        post("create") {

            val user = getUserPrincipal()

            if (!user.roles.contains(Const.Administration.SUPER_ADMIN_ROLE)) {
                throw IdServerException.FORBIDDEN
            }

            val request = call.receive<CreateTenantRequest>()

            if (request.name.isBlank() || request.host.isBlank()) {
                throw IdServerException.BAD_REQUEST
            }

            val tenant = TenantRepository.create(request.name, request.host)

            call.respond(tenant)
        }

        get("list") {

            val user = getUserPrincipal()

            if (user.roles.contains(Const.Administration.SUPER_ADMIN_ROLE)) {
                call.respond(IDServer.getAllTenants())
                return@get
            }

            call.respond(IDServer.getTenants(user.getOwnedTenantIds()))
        }

        post("delete") {

            val user = getUserPrincipal()

            if (!user.roles.contains(Const.Administration.SUPER_ADMIN_ROLE)) {
                throw IdServerException.FORBIDDEN
            }

            val request = call.receive<DeleteTenantRequest>()

            call.respond(ResultResponse(TenantRepository.remove(request.tenantId)))
        }

        route("keys") {

            get {

                val user = getUserPrincipal()

                val tenantId = getIntParam("tenantId")

                user.checkAuthorizedAdmin(tenantId)

                val tenant = IDServer.getTenant(tenantId)

                call.respond(
                    CurrentKeyResponse(
                        tenant.getCurrentPublicKey(), tenant.getValidPublicKeys().reversed()
                    )
                )
            }

            post {

                val user = getUserPrincipal()

                val request = call.receive<RotateKeyRequest>()

                user.checkAuthorizedAdmin(request.tenantId)

                val tenant = IDServer.getTenant(request.tenantId)
                val newKey = TenantRSAKeyPairRepository.create(tenant)

                call.respond(
                    PublicKeyInfo(
                        newKey.id, newKey.createdAt, newKey.publicKey
                    )
                )
            }

            post("delete") {

                val user = getUserPrincipal()

                val request = call.receive<DeleteKeyRequest>()

                user.checkAuthorizedAdmin(request.tenantId)

                val result = TenantRSAKeyPairRepository.delete(request.tenantId, request.keyId)

                call.respond(ResultResponse(result))
            }
        }
    }
}