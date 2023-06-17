package hr.vgabriel.skillconnect.helpers

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.math.BigInteger
import java.security.MessageDigest

val Int.percent: Dp
    get() = (this / 100f).dp

val String.removeSpaces: String
    get() = this.replace("\\s".toRegex(), "")

val String.splitStringBySpaces: List<String>
    get() = this.split("\\s+".toRegex())

val String.getId: Int
    get() {
        val md = MessageDigest.getInstance("MD5")
        val messageDigest = md.digest(this.toByteArray())
        val uniqueIdBytes = messageDigest.copyOfRange(0, 4)
        return BigInteger(1, uniqueIdBytes).toInt()
    }