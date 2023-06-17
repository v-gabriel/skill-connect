package hr.vgabriel.skillconnect.bll.services

import android.util.Log
import androidx.compose.runtime.staticCompositionLocalOf
import hr.vgabriel.skillconnect.bll.providers.UserProvider
import hr.vgabriel.skillconnect.bll.repositories.FirebaseRepository
import java.util.Date

enum class LogType {
    ERROR, INFO
}

interface ILoggingService {
    fun log(type: LogType, message: String, exception: Exception? = null)
}

val LocalLoggingService =
    staticCompositionLocalOf<LoggingService> { error("No LoggingService provided") }

class LoggingService(
    private val repository: FirebaseRepository,
    private val userProvider: UserProvider
) : ILoggingService {

    override fun log(type: LogType, message: String, exception: Exception?) {
        try {
            val logData: HashMap<String, Any?> = hashMapOf(
                "type" to type.name,
                "timestamp" to Date().toString(),
                "message" to message,
                "exception" to exception?.message,
                //"stackTrace" to exception?.stackTrace?.toList()
            )


            val documentKey = userProvider.user?.id ?: "anonymous"
            val logsCollection =
                repository.dataRepository.collection(repository.LOGGING_REPOSITORY_KEY)

            val logsDocument = logsCollection.document(documentKey)

            logsDocument.get().addOnSuccessListener { documentSnapshot ->
                val logsArray =
                    documentSnapshot.get("logs") as? ArrayList<HashMap<String, Any?>> ?: ArrayList()
                logsArray.add(logData)

                val updatedData = hashMapOf("logs" to logsArray)
                logsDocument.set(updatedData)
            }.addOnFailureListener { e ->
                throw Exception("[LoggingService] Log exception", e)
            }
        } catch (e: Exception) {
            Log.d("DEBUG", "[LoggingService] Log exception", e)
        }
    }

}