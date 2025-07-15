import java.util.*

val jkey = CharArray(64)
val fullKey = CharArray(64)
val configVal = CharArray(64)
val offKey = CharArray(7)

// Get a consecutive day from 1/Jan/2010
fun getDiaAbs(day: Int, month: Int, year: Int): Int {
    var diaAbs = 0
    for (cnt in 2010 until year) {
        diaAbs += 365
        if ((cnt % 4 == 0 && cnt % 100 != 0) || (cnt % 400 == 0)) diaAbs++ // Leap year
    }
    for (cnt in 1 until month) {
        diaAbs += when (cnt) {
            4, 6, 9, 11 -> 30
            2 -> {
                var feb = 28
                if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) feb++
                feb
            }
            else -> 31
        }
    }
    diaAbs += day
    return diaAbs
}

//Convert consecutive day to date
fun getFechaAbs(diaAbs: Int): Triple<Int, Int, Int> {
    var year = 2010
    var testDay = 0
    var cntDia: Int

    while (true) {
        cntDia = testDay
        testDay += 365
        if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) testDay++
        if (testDay >= diaAbs) break else year++
    }

    var month = 1
    testDay = cntDia
    while (true) {
        cntDia = testDay
        testDay += when (month) {
            4, 6, 9, 11 -> 30
            2 -> {
                var feb = 28
                if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) feb++
                feb
            }
            else -> 31
        }
        if (testDay >= diaAbs) break else month++
    }

    val day = diaAbs - cntDia
    return Triple(day, month, year)
}

// Create local key
fun JDroid_EncriptCodeKey(idDisp: String, idApp: Int, jkey: String): String {
    val codIdDisp = CharArray(5)
    val jkeyApp = CharArray(4)
    var tmpIdApp = idApp.coerceIn(0, 999)
    var posjkey = 0

    while (tmpIdApp > 0) {
        jkeyApp[posjkey++] = (tmpIdApp % 10 + 48).toChar()
        tmpIdApp /= 10
    }
    while (posjkey < 3) jkeyApp[posjkey++] = '0'
    jkeyApp[posjkey] = '\u0000'

    for (cnt in 0 until 4) {
        codIdDisp[cnt] = when (val c = idDisp[cnt]) {
            in '0'..'9' -> (57 - c.code + 48).toChar()
            in 'A'..'Z' -> (90 - c.code + 65).toChar()
            in 'a'..'z' -> (122 - c.code + 97).toChar()
            else -> c
        }
    }
    codIdDisp[4] = '\u0000'

    val sb = StringBuilder()
    if (jkey.length > 42) sb.append("http://www.puisori.com/jorgesys/IOANICAs")
    sb.append(jkeyApp.concatToString().trim())
    sb.append(codIdDisp.concatToString().trim())
    if (jkey.length > 42) sb.append(jkey.substring(42))
    return sb.toString()
}

// Validate key
fun JDroid_AskCodeKey(idDisp: String, idApp: Int, jkeyConf: String, day: Int, month: Int, year: Int): Int {
    if (jkeyConf.length < 58 || jkeyConf.length > 59) return 1

    val tmpIdDisp = idDisp.substring(0, 4)
    val codIdAppDisp = JDroid_EncriptCodeKey(tmpIdDisp, idApp, "")

    for (cnt in 0 until 7) {
        if (codIdAppDisp[cnt] != jkeyConf[cnt + 42]) return 2
    }

    var tmpKey = jkeyConf.substring(49)
    var lastChar = tmpKey.length - 4
    tmpKey = tmpKey.substring(0, lastChar)

    var crc = 0
    for (cnt in 0 until lastChar - 1) {
        crc += tmpKey[cnt].code
    }
    if (tmpKey[--lastChar] != ((crc % 27) + 65).toChar()) return 3

    val daysLapseOK = tmpKey[--lastChar].code - 65
    if (daysLapseOK !in 0..25) return 4

    val decrypted = tmpKey.substring(0, lastChar).mapIndexed { idx, c ->
        (((c.code - 65 - (idx % 2)) / 2) + 48).toChar()
    }.joinToString("")
    if (decrypted.any { it !in '0'..'9' }) return 5

    val diaConf = decrypted.reversed().toInt()
    val diaDisp = getDiaAbs(day, month, year)

    if (diaDisp < diaConf || diaDisp > diaConf + daysLapseOK) return 6

    return 0
}

//Generate key to shut down "system"
fun JDroid_GetOffKey(day: Int, month: Int, year: Int, lapseDays: Int): String {
    var diaConf = getDiaAbs(day, month, year)
    if (diaConf > 9999 || lapseDays !in 0..25) return ""

    val tmpKey = StringBuilder()
    while (diaConf > 0) {
        val ch = (diaConf % 10 + 48).toChar()
        tmpKey.append(((ch.code - 48) * 2 + 65 + (tmpKey.length % 2)).toChar())
        diaConf /= 10
    }
    tmpKey.append((lapseDays + 65).toChar())

    var crc = 0
    for (c in tmpKey) crc += c.code
    tmpKey.append(((crc % 27) + 65).toChar())

    return "http://www.puisori.com/jorgesys/IOANICAs${tmpKey}.zip"
}

fun main() {
    val strUDID = "A6E9"
    val idApp = 443
    var enabledKey = JDroid_GetOffKey(22, 5, 2025, 1)
    println(enabledKey)

    val result = if (enabledKey.isNotEmpty()) {
        val secondKey = JDroid_EncriptCodeKey(strUDID, idApp, enabledKey)
        JDroid_AskCodeKey(strUDID, idApp, secondKey, 22, 5, 2025)
    } else {
        7
    }
    // The resulting string of this example must be: http://www.puisori.com/jorgesys/IOANICAsCFMLBF.zip
    println("RESULT=$result (The correct value must be 1!)")
}
