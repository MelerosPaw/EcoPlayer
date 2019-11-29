package com.example.playercrop

class VideoConfigBO private constructor() {

  var autoPlay = false
  var loop = false

  class Builder {

    private val videoConfig = VideoConfigBO()

    fun build() = videoConfig

    fun autoPlay(isAutoPlay: Boolean) = apply {
      videoConfig.autoPlay = isAutoPlay
    }

    fun loop(isLoop: Boolean) = apply {
      videoConfig.loop = isLoop
    }
  }
}