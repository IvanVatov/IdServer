package app.vatov.idserver

import app.vatov.idserver.exception.IdServerException
import app.vatov.idserver.ext.respondException
import app.vatov.idserver.model.UserPrincipal
import app.vatov.idserver.routes.applicationRoute
import com.auth0.jwt.JWT
import io.ktor.http.auth.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.velocity.*
import org.apache.velocity.exception.ResourceNotFoundException
import org.slf4j.LoggerFactory
import java.net.URL


private val _LOG = LoggerFactory.getLogger(Application::class.java)

fun Application.webServerModule(testing: Boolean = false) {

    install(ContentNegotiation) {
        json(jsonInstance)
    }

    install(StatusPages) {
        exception<IdServerException> { call, cause ->
            call.respond(cause.statusCode, cause.errorResponse)
        }
        exception<Throwable> { call, cause ->
            when (cause) {
                is BadRequestException ->
                    call.respondException(IdServerException.BAD_REQUEST)

                is ResourceNotFoundException ->
                    call.respondException(IdServerException.NOT_FOUND)

                else -> {
                    _LOG.error("webServerModule", cause)
                    call.respondException(IdServerException.INTERNAL_SERVER_ERROR)
                }
            }
        }
    }

    install(Authentication) {
        basic(Const.AuthName.CLIENT_BASIC) {
            validate { credentials ->

                val host = this.request.host()

                val tenant = IDServer.getTenant(host)

                val client = tenant.getClient(credentials.name)

                if (client.clientSecret != credentials.password) {
                    throw IdServerException.INVALID_CLIENT
                }

                client
            }
        }

        basic(Const.AuthName.ADMINISTRATION_BASIC) {
            validate { credentials ->

                if (Configuration.ADMINISTRATION_HOST.isNotEmpty() && Configuration.ADMINISTRATION_HOST != this.request.host()) {
                    throw IdServerException.NOT_FOUND
                }

                val tenant = IDServer.getTenant(Const.Administration.TENANT_ID)

                val client = tenant.getClient(credentials.name)

                if (client.clientSecret != credentials.password) {
                    throw IdServerException.INVALID_CLIENT
                }

                client
            }
        }

        jwt(Const.AuthName.ADMINISTRATION_BEARER) {
            verifier {
                IDServer.getTenant(Const.Administration.TENANT_ID).jwtVerifier
            }

            validate { credential ->

                credential.payload
                UserPrincipal(
                    credential.payload.subject,
                    credential.payload.getClaim(Const.OAuth.SCOPE)?.asList(String::class.java)
                        ?: emptyList(),
                    credential.payload.getClaim(Const.OpenIdScope.ROLES)?.asList(String::class.java)
                        ?: emptyList()
                )
            }
        }

        jwt {
            verifier {
                val token = JWT.decode((it as? HttpAuthHeader.Single)?.blob)
                val host = URL(token.issuer).host
                IDServer.getTenant(host).jwtVerifier
            }

            validate { credential ->
                UserPrincipal(
                    credential.payload.subject,
                    credential.payload.getClaim(Const.OAuth.SCOPE)?.asList(String::class.java)
                        ?: emptyList(),
                    credential.payload.getClaim(Const.OpenIdScope.ROLES)?.asList(String::class.java)
                        ?: emptyList()
                )
            }
        }
    }

    install(Velocity) {
        setProperty("resource.loader.file.path", "./templates")
    }

//    install(CORS) {
//        this.allowCredentials = true
//
//        allowHeader(HttpHeaders.ContentType)
//        allowHeader(HttpHeaders.Authorization)
//        allowHeader(HttpHeaders.Origin)
//        allowHeader(HttpHeaders.Host)
//        allowHeader(HttpHeaders.AccessControlAllowOrigin)
//
//        allowMethod(HttpMethod.Options)
//        allowMethod(HttpMethod.Post)
//        allowMethod(HttpMethod.Get)
//
//        allowHost(host = "*", schemes = listOf("https"))
//    }

    applicationRoute()
}
