package com.itech.sleepwell

import java.io.Serializable

data class Music(
    val title: String,
    val artist: String,
    val filePath: String
) : Serializable