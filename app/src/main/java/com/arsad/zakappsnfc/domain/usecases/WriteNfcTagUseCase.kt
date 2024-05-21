package com.arsad.zakappsnfc.domain.usecases

import android.nfc.NdefMessage
import android.nfc.Tag
import com.arsad.zakappsnfc.domain.NfcRepository
import com.arsad.zakappsnfc.misc.util.NdefRecordGenerator.toNdefTextRecord
import javax.inject.Inject

class WriteNfcTagUseCase @Inject constructor(
    private val repository: NfcRepository
) {
    operator fun invoke(tag: Tag, message: String): Boolean {
        val ndefMessage = NdefMessage(arrayOf(message.toNdefTextRecord()))
        return repository.writeNfcTag(tag, ndefMessage)
    }
}
