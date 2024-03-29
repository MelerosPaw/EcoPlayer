package com.example.playercrop

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val url = "https://static.e-stradivarius.net/5/photos3//2019/I/0/1/p/5935/188/220//03/5935188220_1_1_4.mp4?t=1573727419337"

        val imageVideo = ImageVideoWidgetBO()
        imageVideo.isMatchScreen = true

        val videoConfig = VideoConfigBO.Builder()
            .autoPlay(false)
            .loop(false)
            .build()
        val video = VideoWBO(url, videoConfig, ScreenUtils.width(this).toInt(),
            ScreenUtils.heightProv(this).toInt())
        imageVideo.video = video
        imageVideo.position = 0

        val viewConfig = VideoWidgetView.PlayerConfig.Builder()
            .hasImage(true)
            .usesCache(true)
            .centerCrop(true)
            .hasPlayImage(true)
            .playsOnImageClick(true)
            .showPlayOnVideoEnd(true)
            .showImageOnVideoEnd(true)
            .showPlayOnVideoPaused(true)
            .showImageOnVideoPaused(false)
            .underTheHood(VideoWidgetView.UnderTheHood.TEXTURE_VIEW)
            .build()
        player.drawVideo(imageVideo, viewConfig)
    }

//    private fun oldWay() {
//        videoPlayImg.setImageResource(R.drawable.video_play)
//        videoView.setVideoURI(Uri.parse(url))
//        videoView.setOnErrorListener { _, _, _ ->
//            setErrorVideoState()
//            true
//        }
//        videoView.setOnPreparedListener { mediaPlayer ->
//            ViewUtils.setVisible(true, videoContainer, videoView)
//            showPlayView(true)
//            try {
//                mediaPlayer.isLooping = getBoolean(R.bool.loop_video_product_detail)
//                if (InditexApplication.get().isWifiConnected) {
//                    videoView.start()
//                    videoView.pause()
//                }
//            } catch (e: Exception) {
//                setErrorVideoState()
//            }
//            resizeVideoView(mediaPlayer)
//        }
//        videoView.setOnCompletionListener {
//            showPlayView(true)
//        }
//    }
//
//    private fun resizeVideoView(mediaPlayer: MediaPlayer) {
//        val videoWidth: Int = mediaPlayer.videoWidth
//        val videoHeight: Int = mediaPlayer.videoHeight
//
//        val videoViewWidth = videoView.width
//        val videoViewHeight = videoView.height
//
//        val xScale = videoViewWidth.toFloat() / videoWidth
//        val yScale = videoViewHeight.toFloat() / videoHeight
//        val scale = max(xScale, yScale)
//
//        val scaledWidth = scale * videoWidth
//        val scaledHeight = scale * videoHeight
//
//        val layoutParams = videoView.layoutParams
//        layoutParams.width = scaledWidth.toInt()
//        layoutParams.height = scaledHeight.toInt()
//        videoView.layoutParams = layoutParams
//    }
}
