package com.example.playercrop

class VideoWBO(val path: String?,
               val videoConfig: VideoConfigBO?,
               val height: Int? = null,
               val width: Int? = null,
               val onVideoClickedListener: OnVideoClickedListener? = null) {


  interface OnVideoClickedListener {
    fun onVideoClicked(isVideoReady: Boolean, isPlaying: Boolean)
  }

}