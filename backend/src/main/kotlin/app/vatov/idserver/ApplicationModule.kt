package app.vatov.idserver

import com.auth0.jwt.JWT
import app.vatov.idserver.model.UserPrincipal
import app.vatov.idserver.response.ErrorResponse
import app.vatov.idserver.routes.applicationRoute
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.basic
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.host
import io.ktor.server.response.respond
import io.ktor.server.velocity.Velocity
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import org.slf4j.LoggerFactory
import java.net.URL


private val _LOG = LoggerFactory.getLogger(Application::class.java)

fun Application.webServerModule(testing: Boolean = false) {

    install(ContentNegotiation) {
        json(jsonInstance)
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->

            _LOG.error("webServerModule", cause)

            when (cause) {
                is BadRequestException ->
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse.BAD_REQUEST
                    )

                else ->
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse.INTERNAL_SERVER_ERROR
                    )
            }
        }
    }

    install(Authentication) {
        basic(Const.AuthName.CLIENT_BASIC) {
            validate { credentials ->

                val host = this.request.host()

                val tenant = IDServer.getTenant(host)

                if (tenant == null) {

                    this.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse.NOT_FOUND
                    )
                    return@validate null
                }

                val client = tenant.getClient(credentials.name)

                if (client == null || client.clientSecret != credentials.password) {
                    this.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse.INVALID_CLIENT
                    )
                    return@validate null
                }

                client
            }
        }

        basic(Const.AuthName.ADMINISTRATION_BASIC) {
            validate { credentials ->

                if (Configuration.ADMINISTRATION_HOST.isNotEmpty() && Configuration.ADMINISTRATION_HOST != this.request.host()) {
                    this.respond(
                        HttpStatusCode.NotFound,
                        String()
                    )
                    return@validate null
                }

                val tenant = IDServer.getTenant(Const.Administration.TENANT_ID)

                if (tenant == null) {

                    this.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse.NOT_FOUND
                    )
                    return@validate null
                }

                val client = tenant.getClient(credentials.name)

                if (client == null || client.clientSecret != credentials.password) {
                    this.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse.INVALID_CLIENT
                    )
                    return@validate null
                }

                client
            }
        }

        jwt(Const.AuthName.ADMINISTRATION_BEARER) {
            verifier {
                IDServer.getTenant(Const.Administration.TENANT_ID)?.jwtVerifier
            }

            validate { credential ->
                UserPrincipal(
                    credential.payload.subject,
                    credential[Const.OAuth.SCOPE]?.let {
                        jsonInstance.decodeFromString(
                            ListSerializer(String.serializer()), it
                        )
                    } ?: emptyList(),
                    credential[Const.OpenIdScope.ROLES]?.let {
                        jsonInstance.decodeFromString(
                            ListSerializer(String.serializer()), it
                        )
                    } ?: emptyList()
                )
            }
        }

        jwt {
            verifier {
                val token = JWT.decode((it as? HttpAuthHeader.Single)?.blob)
                val host = URL(token.issuer).host
                IDServer.getTenant(host)?.jwtVerifier
            }

            validate { credential ->
                UserPrincipal(
                    credential.payload.subject,
                    credential[Const.OAuth.SCOPE]?.let {
                        jsonInstance.decodeFromString(
                            ListSerializer(String.serializer()), it
                        )
                    } ?: emptyList(),
                    credential[Const.OpenIdScope.ROLES]?.let {
                        jsonInstance.decodeFromString(
                            ListSerializer(String.serializer()), it
                        )
                    } ?: emptyList()
                )
            }
        }
    }

    install(Velocity) {
        setProperty("resource.loader.file.path", "./templates")
    }

    install(CORS) {
        this.allowCredentials = true

        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.Origin)
        allowHeader(HttpHeaders.Host)
        allowHeader(HttpHeaders.AccessControlAllowOrigin)

        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Get)

        allowHost(host = "*", schemes = listOf("https"))
    }

    applicationRoute()
}
