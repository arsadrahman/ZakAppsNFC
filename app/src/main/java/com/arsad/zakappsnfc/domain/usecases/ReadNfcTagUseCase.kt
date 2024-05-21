package com.arsad.zakappsnfc.domain.usecases

import android.nfc.Tag
import com.arsad.zakappsnfc.domain.NfcRepository
import javax.inject.Inject

class ReadNfcTagUseCase @Inject constructor(
    private val repository: NfcRepository
) {
    operator fun invoke(tag: Tag): String? {
        return repository.readNfcTag(tag)
    }
}
