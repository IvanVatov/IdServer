package app.vatov.idserver.util

val emailPattern = Regex("^[A-Za-z0-9+_.-]+@(.+)$")

fun isEmailValid(email: String): Boolean {
    return emailPattern.matches(email)
}

