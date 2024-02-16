package app.vatov.idserver.routes.admin

import app.vatov.idserver.IDServer
import app.vatov.idserver.model.PublicKeyInfo
import app.vatov.idserver.repository.TenantRSAKeyPairRepository
import app.vatov.idserver.repository.TenantRepository
import app.vatov.idserver.request.admin.CreateTenantRequest
import app.vatov.idserver.request.admin.DeleteKeyRequest
import app.vatov.idserver.request.admin.DeleteTenantRequest
import app.vatov.idserver.request.admin.RotateKeyRequest
import app.vatov.idserver.response.CurrentKeyResponse
import app.vatov.idserver.response.ErrorResponse
import app.vatov.idserver.response.ResultResponse
import app.vatov.idserver.routes.getIntParam
import app.vatov.idserver.routes.respondNotFound
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.tenantRoutes() {

    route("admin/tenant") {

        post("create") {

            val request = call.receive<CreateTenantRequest>()

            if (request.name.isBlank() || request.host.isBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest, ErrorResponse.BAD_REQUEST
                )
                return@post
            }

            val tenant = TenantRepository.create(request.name, request.host)

            call.respond(tenant)
        }


        get("list") {

            call.respond(IDServer.getAllTenants())
        }

        post("delete") {
            val request = call.receive<DeleteTenantRequest>()

            call.respond(ResultResponse(TenantRepository.remove(request.tenantId)))
        }
    }


    route("admin/tenant/keys") {

        get {
            val tenantId = getIntParam("tenantId") ?: return@get


            val tenant = IDServer.getTenant(tenantId)

            tenant?.let {
                call.respond(
                    CurrentKeyResponse(
                        it.getCurrentPublicKey(), it.getValidPublicKeys().reversed()
                    )
                )
                return@get

            }
            respondNotFound()
        }


        post {
            val request = call.receive<RotateKeyRequest>()

            IDServer.getTenant(request.tenantId)?.let {
                val newKey = TenantRSAKeyPairRepository.create(it)

                call.respond(
                    PublicKeyInfo(
                        newKey.id, newKey.createdAt, newKey.publicKey
                    )
                )
                return@post
            }

            respondNotFound()
        }

        post("delete") {
            val request = call.receive<DeleteKeyRequest>()

            val result = TenantRSAKeyPairRepository.delete(request.tenantId, request.keyId)

            call.respond(ResultResponse(result))
        }
    }
}