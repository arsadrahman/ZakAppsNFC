package com.arsad.zakappsnfc.domain

import android.nfc.NdefMessage
import android.nfc.Tag

interface NfcRepository {
    fun readNfcTag(tag: Tag): String?
    fun writeNfcTag(tag: Tag, message: String): Boolean
}
