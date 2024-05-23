package com.arsad.zakappsnfc.presentation.clock

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
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
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.arsad.zakappsnfc.R
import com.arsad.zakappsnfc.presentation.clock.viewmodel.NFCEntryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class EntryScreen : AppCompatActivity() {

    private val viewModel by viewModels<NFCEntryViewModel>()
    private var nfcAdapter: NfcAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        setContentView(R.layout.entryscreen_activity)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        viewModel.getUserDetails()
        setupObservers()
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.userID.collect { userID ->
                findViewById<TextView>(R.id.userid_tv).apply { setText(userID) }
            }
        }
        lifecycleScope.launch {
            viewModel.userName.collect { userName ->
                findViewById<TextView>(R.id.username_tv).apply { setText(userName) }
            }
        }
    }

    fun onClickClockIn(view: View) {
        viewModel.writeClockInToNFC()
    }

    fun onClickClockOut(view: View) {
        viewModel.writeClockOutToNFC()
    }

    fun onClickTakeABreak(view: View) {
        viewModel.writeTakeABreakToNFC()
    }

    fun onClickEndBreak(view: View) {
        viewModel.writeEndBreakToNFC()
    }

    override fun onResume() {
        super.onResume()
        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE
        )
        val intentFilters = arrayOf<IntentFilter>(
            IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED),
            IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED),
            IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
        )
        val techLists = arrayOf(
            arrayOf(
                NfcA::class.java.name
            ),
            arrayOf(NfcB::class.java.name),
            arrayOf(NfcF::class.java.name),
            arrayOf(NfcV::class.java.name),
            arrayOf(IsoDep::class.java.name),
            arrayOf(MifareClassic::class.java.name),
            arrayOf(MifareUltralight::class.java.name),
            arrayOf(Ndef::class.java.name),
            arrayOf(NdefFormatable::class.java.name)
        )
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilters, techLists)
    }

    override fun onPause() {
        super.onPause()
        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        nfcAdapter.disableForegroundDispatch(this)
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent?.action == NfcAdapter.ACTION_TAG_DISCOVERED) {
            val tag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
            } else {
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            }
            tag?.id?.let {
                tag?.let { viewModel.onTagDiscovered(it) } ?: kotlin.run {
                    Log.e("NO Tags", "NULL")
                }
                val tagValue = it.toHexString()
                Toast.makeText(this, "NFC tag detected: $tagValue", Toast.LENGTH_SHORT).show()
            } ?: kotlin.run {
                Log.e("NO Tags", "NULL")
            }
        }
    }

}

