package com.xva.musiclife.models

class Song(
    var path: String,
    var name: String,
    var artist: String,
    var duration: String,
    var image: ByteArray?,
    var isPlaying: Boolean
) {
}