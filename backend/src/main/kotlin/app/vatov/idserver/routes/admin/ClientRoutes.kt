package app.vatov.idserver.routes.admin

import app.vatov.idserver.IDServer
import app.vatov.idserver.exception.IdServerException
import app.vatov.idserver.ext.checkAuthorizedAdmin
import app.vatov.idserver.model.ClientPrincipal
import app.vatov.idserver.repository.ClientRepository
import app.vatov.idserver.request.admin.CreateClientRequest
import app.vatov.idserver.request.admin.DeleteClientRequest
import app.vatov.idserver.response.ResultResponse
import app.vatov.idserver.routes.getIntParam
import app.vatov.idserver.routes.getStringParam
import app.vatov.idserver.routes.getUserPrincipal
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
            val user = getUserPrincipal()

            val tenantId = getIntParam("tenantId")

            user.checkAuthorizedAdmin(tenantId)

            val clientId = getStringParam("clientId")

            ClientRepository.getClientIdForTenantId(tenantId, clientId)?.let {
                call.respond(it)
                return@get
            }

            throw IdServerException.NOT_FOUND
        }

        post("put") {

            val user = getUserPrincipal()

            val client = call.receive<ClientPrincipal>()

            user.checkAuthorizedAdmin(client.tenantId)

            call.respond(ResultResponse(ClientRepository.update(client)))
        }

        post("delete") {
            val user = getUserPrincipal() ?: return@post

            val request = call.receive<DeleteClientRequest>()

            user.checkAuthorizedAdmin(request.tenantId)

            val result = ClientRepository.delete(request.tenantId, request.clientId)

            if (result) {
                IDServer.getTenant(request.tenantId).removeClientId(request.clientId)
            }

            call.respond(ResultResponse(result))
        }
    }

    route("admin/client/list") {

        get {
            val user = getUserPrincipal()

            val tenantId = getIntParam("tenantId")

            user.checkAuthorizedAdmin(tenantId)

            call.respond(ClientRepository.getAllForTenantId(tenantId))
        }
    }

    route("admin/client/create") {

        post {
            val user = getUserPrincipal() ?: return@post

            val request = call.receive<CreateClientRequest>()

            user.checkAuthorizedAdmin(request.tenantId)

            if (request.clientId.isBlank() || request.application.isBlank()) {
                throw IdServerException.BAD_REQUEST
            }

            val client =
                ClientRepository.create(request.tenantId, request.clientId, request.application)

            call.respond(client)
        }
    }
}