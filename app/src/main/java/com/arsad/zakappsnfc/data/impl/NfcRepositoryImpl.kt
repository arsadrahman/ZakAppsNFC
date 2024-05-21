package com.arsad.zakappsnfc.data.impl

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NdefRecord.createTextRecord
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.nfc.tech.MifareClassic
import android.nfc.tech.MifareUltralight
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.nfc.tech.NfcA
import android.nfc.tech.NfcB
import android.nfc.tech.NfcF
import android.nfc.tech.NfcV
import android.util.Log
import com.arsad.zakappsnfc.domain.NfcRepository
import java.nio.charset.Charset
import java.util.*
import javax.inject.Inject

class NfcRepositoryImpl @Inject constructor() : NfcRepository {

    override fun writeNfcTag(tag: Tag, ndefMessage: NdefMessage): Boolean {
        val ndef = Ndef.get(tag)
        if (ndef != null) {
            ndef.connect()
            return if (ndef.maxSize < ndefMessage.toByteArray().size) {
                ndef.close()
                false
            } else {
                ndef.writeNdefMessage(ndefMessage)
                ndef.close()
                true
            }
        }

        val ndefFormatable = NdefFormatable.get(tag)
        if (ndefFormatable != null) {
            return try {
                ndefFormatable.connect()
                ndefFormatable.format(ndefMessage)
                ndefFormatable.close()
                true
            } catch (e: Exception) {
                false
            }
        }

        val mifareClassic = MifareClassic.get(tag)
        if (mifareClassic != null) {
            mifareClassic.connect()
            mifareClassic.authenticateSectorWithKeyA(1, MifareClassic.KEY_DEFAULT)
            val data = ByteArray(16) { 0x00.toByte() }
            mifareClassic.writeBlock(4, data)
            mifareClassic.close()
            return true
        }

        return false
    }

    override fun readNfcTag(tag: Tag): String? {
        val ndef = Ndef.get(tag) ?: return null
        val ndefMessage = ndef.cachedNdefMessage
        return ndefMessage?.records?.mapNotNull { it.payload.decodeToString() }?.joinToString()
    }

}
