package com.arsad.zakappsnfc.misc.util

import android.nfc.NdefRecord
import java.nio.charset.Charset
import java.util.Locale

object NdefRecordGenerator {
    fun String.toNdefTextRecord():NdefRecord{
        val payload = this
        val langBytes = Locale.getDefault().language.toByteArray(Charset.forName("US-ASCII"))
        val textBytes = payload.toByteArray(Charset.forName("UTF-8"))
        val status = (langBytes.size and 0x3F).toByte()
        val data = ByteArray(1 + langBytes.size + textBytes.size)
        data[0] = status
        System.arraycopy(langBytes, 0, data, 1, langBytes.size)
        System.arraycopy(textBytes, 0, data, 1 + langBytes.size, textBytes.size)
        return NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, ByteArray(0), data)
    }
}