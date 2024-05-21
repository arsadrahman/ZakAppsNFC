package com.arsad.zakappsnfc.data.model

enum class ActionCodes(val code: String) {
    ClockIn("001"), ClockOut("000"), TakeABreak("002"), EndBreak(
        "003"
    )
}