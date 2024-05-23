package com.arsad.zakappsnfc.data.model

enum class Commands(val command: Byte) {
    WriteNfcA(0xA2.toByte()), BlockAddress(0x04.toByte()), ResponseSuccess(0x0A.toByte()),
    CLA(0x00.toByte()), INS(0xA4.toByte()), P1(0x04.toByte()), P2(0x00.toByte())
}