package hr.vgabriel.skillconnect.definitions.entities

import hr.vgabriel.skillconnect.definitions.vm.UserVM
import hr.vgabriel.skillconnect.helpers.removeSpaces
import hr.vgabriel.skillconnect.helpers.splitStringBySpaces

open class UserEntity(
    var name: String,
    var surname: String,
    var email: String,
    var communicationUserId: String,
    var communicationAccessToken: String,
    var tags: List<String>,
    var query: List<String>
) {

    init {
        query = generateQuery(this)
    }

    companion object {
        fun generateQuery(userEntity: UserEntity): List<String> {
            val lowercaseSubstrings = HashSet<String>()

            var properties =
                listOf<String>(userEntity.name, userEntity.surname).map {
                    it.removeSpaces.lowercase()
                }

            properties.forEach { str ->
                for (i in str.indices) {
                    for (j in i + 1..str.length) {
                        lowercaseSubstrings.add(str.substring(i, j))
                    }
                }
            }

            return lowercaseSubstrings.toList()
        }

        fun getQueryStrings(queryString: String): List<String> {
            return queryString.splitStringBySpaces.map { it.lowercase().removeSpaces }
        }

        fun createUserVMMap(userVM: UserVM): Map<String, Any> {
            return mutableMapOf<String, Any>().apply {
                userVM.nameState.value.takeIf { it.isNotBlank() }
                    ?.let { put(UserEntity::name.name, it) }
                userVM.surnameState.value.takeIf { it.isNotBlank() }
                    ?.let { put(UserEntity::surname.name, it) }
                userVM.emailState.value.takeIf { it.isNotBlank() }
                    ?.let { put(UserEntity::email.name, it) }
                userVM.userTagsVM.items.takeIf { it.isNotEmpty() }
                    ?.let { it ->
                        put(
                            UserEntity::tags.name,
                            it.toList().map { tag -> tag.lowercase().removeSpaces })
                    }
            }
        }
    }
}