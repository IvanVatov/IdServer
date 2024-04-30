package app.vatov.idserver.util

val emailPattern = Regex("^[A-Za-z0-9+_.-]+@(.+)$")

fun isEmailValid(email: String): Boolean {
    return emailPattern.matches(email)
}

val passwordPattern = Regex("^(?=.*[0-9])(?=.*[A-Za-z]).{6,}\$")

fun isPasswordValid(password: String): Boolean {
    return passwordPattern.matches(password)
}