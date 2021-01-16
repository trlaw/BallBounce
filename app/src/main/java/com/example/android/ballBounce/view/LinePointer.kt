package com.example.android.ballBounce.view

import com.example.android.ballBounce.utility.Vector

class LinePointer(val pointerId: Int, var lineStart: Vector) {
    var lineEnd: Vector = lineStart
}