package com.arsad.zakappsnfc.presentation.clock.viewmodel


import android.content.Context
import android.nfc.Tag
import android.util.Log
import androidx.lifecycle.ViewModel
import com.arsad.zakappsnfc.domain.usecases.WriteNfcTagUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import com.arsad.zakappsnfc.R
import com.arsad.zakappsnfc.data.model.ActionCodes
import com.arsad.zakappsnfc.data.model.User
import com.arsad.zakappsnfc.domain.usecases.ReadNfcTagUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class NFCEntryViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val writeNfcTagUseCase: WriteNfcTagUseCase,
    private val readNfcTagUseCase: ReadNfcTagUseCase
) : ViewModel() {

    private val _userData = MutableStateFlow(User("21760", "Arsad Rahman"))
    val userData: StateFlow<User> get() = _userData

    private val _userName = MutableStateFlow("Arsad Rahman")
    val userName: StateFlow<String> get() = _userName

    private val _userID = MutableStateFlow("21760")
    val userID: StateFlow<String> get() = _userID


    private val _nfcTagData = MutableStateFlow("")
    val nfcTagData: StateFlow<String> get() = _nfcTagData

    private var _tag: Tag? = null

    fun getUserDetails() {
        viewModelScope.launch {
            _userName.value =
                context.resources.getString(R.string.employee_name_s, _userData.value.name)
            _userID.value = context.resources.getString(R.string.employee_id_s, _userData.value.id)
        }
    }

    fun writeClockInToNFC() {
        writeNfcTag("{${_userData},${ActionCodes.ClockIn.code}}")
    }

    fun writeClockOutToNFC() {
        writeNfcTag("{${_userData},${ActionCodes.ClockOut.code}}")
    }

    fun writeTakeABreakToNFC() {
        writeNfcTag("{${_userData},${ActionCodes.TakeABreak.code}}")
    }

    fun writeEndBreakToNFC() {
        writeNfcTag("{${_userData},${ActionCodes.EndBreak.code}}")
    }


    private fun writeNfcTag(message: String) {
        val tag = _tag ?: return
        viewModelScope.launch {
            val success = writeNfcTagUseCase(tag, message)
            _nfcTagData.value = if (success) "Tag written successfully" else "Failed to write tag"
        }
    }

    fun onTagDiscovered(tag: Tag) {
        _tag = tag
        viewModelScope.launch {
            _nfcTagData.value = readNfcTagUseCase(tag) ?: "No data"
        }
    }
}