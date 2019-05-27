package org.m0skit0.android.nomoredoli.data

import org.m0skit0.android.nomoredoli.data.http.HTTPResponse
import org.m0skit0.android.nomoredoli.util.getGroupOrThrow

internal data class PunchParameters(
    val idUser: Pair<String, String>,
    val action: Pair<String, String>,
    val bouton: Pair<String, String>,
    val comment: Pair<String, String>
) {
    companion object {
        fun fromResponse(response: HTTPResponse) = run {
            val idUser = "idUser" to punchUserIdRegex.find(response.body).getGroupOrThrow(1, "Cannot find idUser in body")
            val action = "action" to  punchActionRegex.find(response.body).getGroupOrThrow(1, "Cannot find action in body")
            val bouton = punchBoutonRegex.find(response.body).run {
                val key = getGroupOrThrow(1, "Cannot find bouton key in body")
                val value = getGroupOrThrow(2, "Cannot find bouton value in body")
                key to value
            }
            val comment = "comment" to ""
            PunchParameters(idUser, action, bouton, comment)
        }
    }

    fun toMap() = mapOf(idUser, action, bouton, comment)

    fun isPunchIn() = bouton.first == BOUTON_PUNCH_IN
    fun isPunchOut() = bouton.first == BOUTON_PUNCH_OUT
}