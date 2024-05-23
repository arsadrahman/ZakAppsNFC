package com.arsad.zakappsnfc.data.impl

import android.nfc.NdefMessage
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.nfc.tech.MifareClassic
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.nfc.tech.NfcA
import android.os.Build
import android.util.Log
import com.arsad.zakappsnfc.data.model.Commands
import com.arsad.zakappsnfc.domain.NfcRepository
import com.arsad.zakappsnfc.misc.util.NdefRecordGenerator.toNdefTextRecord
import java.io.IOException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import javax.inject.Inject


class NfcRepositoryImpl @Inject constructor() : NfcRepository {

    override fun writeNfcTag(tag: Tag, message: String): Boolean {
        val ndef = Ndef.get(tag)
        if (ndef != null) {
            val ndefMessage = NdefMessage(arrayOf(message.toNdefTextRecord()))
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
            val ndefMessage = NdefMessage(arrayOf(message.toNdefTextRecord()))
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

        val nfcA = NfcA.get(tag)
        if(nfcA != null){
            try {
                nfcA.connect()
                val command: ByteArray = prepareWriteCommand(message)
                val response = nfcA.transceive(command)
                val responseMessage = String(response, Charset.forName("UTF-8"))
                Log.e("Received response",responseMessage)
                nfcA.close()
                return response != null && response.isNotEmpty() && response[0] == Commands.ResponseSuccess.command
            } catch (e: IOException) {
                e.printStackTrace()
                return false
            }
        }

        val isoDep = IsoDep.get(tag)
        if(isoDep != null){
            try {
                isoDep.connect()
                val command = createApduCommand(message)
                val response = isoDep.transceive(command)
                val responseMessage = String(response, Charset.forName("UTF-8"))
                Log.e("Received response",responseMessage)
                isoDep.close()
                return response != null && response.isNotEmpty() && response[0] == Commands.ResponseSuccess.command
            } catch (e: IOException) {
                e.printStackTrace()
                return false
            }
        }

        return false
    }

    private fun prepareWriteCommand(message: String): ByteArray {

        val messageBytes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            message.toByteArray(StandardCharsets.US_ASCII)
        } else {
           message.toByteArray(Charset.forName("UTF-8"))
        }
        val command = ByteArray(4 + messageBytes.size)
        command[0] = Commands.WriteNfcA.command // Write command code for some NfcA tags
        command[1] = Commands.BlockAddress.command // Block address to write to
        System.arraycopy(messageBytes, 0, command, 2, messageBytes.size)
        return command
    }

    override fun readNfcTag(tag: Tag): String? {
        val ndef = Ndef.get(tag) ?: return null
        val ndefMessage = ndef.cachedNdefMessage
        return ndefMessage?.records?.mapNotNull { it.payload.decodeToString() }?.joinToString()
    }

    private fun createApduCommand(message: String): ByteArray {
        val messageBytes = message.toByteArray(Charset.forName("UTF-8"))
        val command = ByteArray(5 + messageBytes.size)
        command[0] = Commands.CLA.command
        command[1] = Commands.INS.command
        command[2] = Commands.P1.command
        command[3] = Commands.P2.command
        command[4] = messageBytes.size.toByte()
        System.arraycopy(messageBytes, 0, command, 5, messageBytes.size)
        return command
    }

}
