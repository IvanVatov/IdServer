package app.vatov.idserver.exception

class IdServerException(message: String) : Exception(message) {

    companion object {
        val ACCOUNT_EXIST = IdServerException("Account is already in use")
    }
}