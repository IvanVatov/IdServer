package app.vatov.idserver.request.user

import kotlinx.serialization.Serializable

@Serializable
class UserChangePasswordRequest(val oldPassword: String, val newPassword: String)