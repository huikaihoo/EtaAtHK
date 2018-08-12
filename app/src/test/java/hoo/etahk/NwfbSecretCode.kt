package hoo.etahk

import android.util.Base64
import com.mcxiaoke.koi.HASH
import hoo.etahk.common.Utils
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object NwfbSecretCode {

    val code: String
        get() {
            // Get Random String
            var random = Random().nextInt(10000).toString()
            random += "0".repeat(5 - random.length)

            val timestamp = Utils.getCurrentTimestamp().toString()
            var timestampStr = (timestamp.substring(2, 3) + timestamp.substring(9, 10)
                    + timestamp.substring(4, 5) + timestamp.substring(6, 7)
                    + timestamp.substring(3, 4) + timestamp.substring(0, 1)
                    + timestamp.substring(8, 9) + timestamp.substring(7, 8)
                    + timestamp.substring(5, 6) + timestamp.substring(1, 2))

            random = timestampStr + HASH.sha256((timestampStr + "siwmytnw" + random).toByteArray()).toLowerCase() + random

            return Base64.encodeToString(encryptedStrToByteArray(encrypt(random, "siwmytnwinfomwyy", "a20330efd3f6060e")), Base64.NO_WRAP)
                    .replace("=".toRegex(), "")
        }

    private fun passwordToByteArray(password: String): ByteArray {
        return when {
            (password.length < 16) -> password + "0".repeat(16 - password.length)
            (password.length > 16) -> password.substring(0, 16)
            else -> password
        }.toByteArray()
    }

    private fun encrypt(data: String, passwordForKey: String, passwordForIvParam: String): String {
        val keySpec = SecretKeySpec(passwordToByteArray(passwordForKey), "AES")
        val ivParameterSpec = IvParameterSpec(passwordToByteArray(passwordForIvParam))
        val instance = Cipher.getInstance("AES/CBC/PKCS5Padding")

        instance.init(Cipher.ENCRYPT_MODE, keySpec, ivParameterSpec)

        return String(HASH.encodeHex(instance.doFinal(data.toByteArray()), false))
    }

    private fun encryptedStrToByteArray(str: String): ByteArray {
        val bArr = ByteArray(str.length / 2)
        var i = 0
        while (i < str.length) {
            bArr[i / 2] = ((Character.digit(str[i], 16) shl 4) + Character.digit(str[i + 1], 16)).toByte()
            i += 2
        }
        return bArr
    }
}
