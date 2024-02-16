package app.vatov.idserver.model

class Code(
    val code: String,
    val clientId: String,
    val state: String?,
    val redirectUrl: String,
    val scope: List<String>
) {
    val generatedAt: Long = System.currentTimeMillis()
}