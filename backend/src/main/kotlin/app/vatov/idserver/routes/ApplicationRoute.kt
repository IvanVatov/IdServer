package app.vatov.idserver.routes

import app.vatov.idserver.Configuration
import app.vatov.idserver.Const
import app.vatov.idserver.routes.admin.adminToken
import app.vatov.idserver.routes.admin.adminUsers
import app.vatov.idserver.routes.admin.adminWhoAmI
import app.vatov.idserver.routes.admin.clientRoutes
import app.vatov.idserver.routes.admin.serverConfiguration
import app.vatov.idserver.routes.admin.tenantRoutes
import app.vatov.idserver.routes.oauth.authorize
import app.vatov.idserver.routes.oauth.jwksJson
import app.vatov.idserver.routes.oauth.login
import app.vatov.idserver.routes.oauth.openIdConfiguration
import app.vatov.idserver.routes.oauth.token
import app.vatov.idserver.routes.user.userChangePassword
import app.vatov.idserver.routes.user.userRegister
import app.vatov.idserver.routes.user.userUpdate
import app.vatov.idserver.routes.user.userWhoAmI
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.http.content.staticFiles
import io.ktor.server.routing.routing
import java.io.File

fun Application.applicationRoute() {

    routing {
        // public
        openIdConfiguration()
        jwksJson()
        userRegister()

        authorize()
        login()

        authenticate(Const.AuthName.CLIENT_BASIC) {
            token()
        }

        authenticate(Const.AuthName.ADMINISTRATION_BASIC) {
            adminToken()
        }

        authenticate(Const.AuthName.ADMINISTRATION_BEARER) {
            adminWhoAmI()
            tenantRoutes()
            clientRoutes()
            serverConfiguration()
            adminUsers()
        }

        // protected
        authenticate {
            userWhoAmI()
            userChangePassword()
            userUpdate()
        }

        staticFiles(
            "/admin", File(
                Configuration.FLUTTER_ADMINISTRATION_FILES_LOCATION
            ), index = "index.html"
        )

        staticFiles("/static", File("./static"))
    }
}
