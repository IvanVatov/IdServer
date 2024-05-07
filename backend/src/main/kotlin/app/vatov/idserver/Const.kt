package app.vatov.idserver

object Const {

    object Endpoint {
        const val TOKEN = "token"
        const val AUTHORIZE = "authorize"
        const val JWKS_JSON = "jwks.json"
        const val OPEN_ID_CONFIGURATION = ".well-known/openid-configuration"
    }

    object AuthName {
        const val ADMINISTRATION_BASIC = "administration-basic"
        const val ADMINISTRATION_BEARER = "administration-bearer"
        const val CLIENT_BASIC = "client-basic"
    }

    object OAuth {
        const val USERNAME = "username"
        const val PASSWORD = "password"
        const val GRANT_TYPE = "grant_type"
        const val SCOPE = "scope"
        const val CLIENT_CREDENTIALS = "client_credentials"
        const val REFRESH_TOKEN = "refresh_token"
        const val ID_TOKEN = "id_token"
        const val AUTHORIZATION_CODE = "authorization_code"
        const val CODE = "code"

        const val ACCESS_TOKEN = "access_token"
        const val TOKEN_TYPE = "token_type"
        const val EXPIRES_IN = "expires_in"

        const val BASIC = "Basic"
        const val BEARER = "Bearer"

        const val RESPONSE_TYPE = "response_type"
        const val REDIRECT_URI = "redirect_uri"
        const val STATE = "state"
        const val CLIENT_ID = "client_id"
    }


    object OpenIdScope {
        const val OPENID = "openid"
        const val PROFILE = "profile"
        const val EMAIL = "email"
        const val ADDRESS = "address"
        const val PHONE = "phone"
        const val OFFLINE_ACCESS = "offline_access"
        const val ROLES = "roles"
    }

    object OpenIdClaim {
        const val SUB = "sub"
        const val NAME = "name"
        const val GIVEN_NAME = "given_name"
        const val FAMILY_NAME = "family_name"
        const val MIDDLE_NAME = "middle_name"
        const val NICKNAME = "nickname"
        const val PREFERRED_USERNAME = "preferred_username"
        const val PROFILE = "profile"
        const val PICTURE = "picture"
        const val WEBSITE = "website"
        const val EMAIL = "email"
        const val EMAIL_VERIFIED = "email_verified" // boolean
        const val GENDER = "gender"
        const val BIRTH_DATE = "birthdate"
        const val ZONE_INFO = "zoneinfo"
        const val LOCALE = "locale"
        const val PHONE_NUMBER = "phone_number"
        const val PHONE_NUMBER_VERIFIED = "phone_number_verified" // boolean
        const val ADDRESS = "address" // JSON Object
        const val UPDATED_AT = "updated_at"

        const val NONCE = "nonce"

        // Other
        const val CLIENT_ID = "client_id"
        const val APPLICATION = "application"

        const val USER_DATA = "user_data"
        const val SERVER_DATA = "server_data"
    }

    object OpenIdRole {
        const val USER = "user"
    }

    object Error {
        const val INVALID_REQUEST = "invalid_request"
        const val INVALID_CLIENT = "invalid_client"
        const val INVALID_GRANT = "invalid_grant"
        const val UNAUTHORIZED_CLIENT = "unauthorized_client"
        const val UNSUPPORTED_GRANT_TYPE = "unsupported_grant_type"
        const val INVALID_SCOPE = "invalid_scope"
    }

    object Administration {
        const val TENANT_ID = 1
        const val CLIENT_ID = "adminClient"
        const val CLIENT_SECRET = "adminSecret"
        const val SUPER_ADMIN_ROLE = "super-admin"
        const val TENANT_ADMIN_ROLE_PREFIX = "admin-"
    }
}