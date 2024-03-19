package app.vatov.idserver.routes.admin

import app.vatov.idserver.IDServer
import app.vatov.idserver.ext.isAuthorizedAdmin
import app.vatov.idserver.model.ClientPrincipal
import app.vatov.idserver.repository.ClientRepository
import app.vatov.idserver.request.admin.CreateClientRequest
import app.vatov.idserver.request.admin.DeleteClientRequest
import app.vatov.idserver.response.ResultResponse
import app.vatov.idserver.routes.getIntParam
import app.vatov.idserver.routes.getStringParam
import app.vatov.idserver.routes.getUserOrRespondError
import app.vatov.idserver.routes.respondBadRequest
import app.vatov.idserver.routes.respondNotFound
import app.vatov.idserver.routes.respondForbidden
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.clientRoutes() {

    route("admin/client") {

        get {
            val user = getUserOrRespondError() ?: return@get

            val tenantId = getIntParam("tenantId") ?: return@get

            if (!user.isAuthorizedAdmin(tenantId)){
                respondForbidden()
                return@get
            }

            val clientId = getStringParam("clientId") ?: return@get

            ClientRepository.getClientIdForTenantId(tenantId, clientId)?.let {
                call.respond(it)
                return@get
            }

            respondNotFound()
        }

        post("put") {

            val user = getUserOrRespondError() ?: return@post

            val client = call.receive<ClientPrincipal>()

            if (!user.isAuthorizedAdmin(client.tenantId)){
                respondForbidden()
                return@post
            }

            call.respond(ResultResponse(ClientRepository.update(client)))
        }

        post("delete") {
            val user = getUserOrRespondError() ?: return@post

            val request = call.receive<DeleteClientRequest>()

            if (!user.isAuthorizedAdmin(request.tenantId)){
                respondForbidden()
                return@post
            }

            val result = ClientRepository.delete(request.tenantId, request.clientId)

            if (result) {
                IDServer.getTenant(request.tenantId)?.apply {
                    removeClientId(request.clientId)
                }
            }

            call.respond(ResultResponse(result))
        }

    }

    route("admin/client/list") {

        get {
            val user = getUserOrRespondError() ?: return@get

            val tenantId = getIntParam("tenantId") ?: return@get

            if (!user.isAuthorizedAdmin(tenantId)){
                respondForbidden()
                return@get
            }

            call.respond(ClientRepository.getAllForTenantId(tenantId))
        }
    }

    route("admin/client/create") {

        post {
            val user = getUserOrRespondError() ?: return@post

            val request = call.receive<CreateClientRequest>()

            if (!user.isAuthorizedAdmin(request.tenantId)){
                respondForbidden()
                return@post
            }

            if (request.clientId.isBlank() || request.application.isBlank()) {
                respondBadRequest()
                return@post
            }

            val client =
                ClientRepository.create(request.tenantId, request.clientId, request.application)

            call.respond(client)
        }
    }
}