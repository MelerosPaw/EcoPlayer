package com.example.playercrop

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
            .centerCrop(true)
            .build()
        val video = VideoWBO(url, videoConfig, ScreenUtils.width(this).toInt(),
            ScreenUtils.heightProv(this).toInt())
        imageVideo.video = video
        imageVideo.position = 0

        val viewConfig = VideoWidgetView.PlayerConfig.Builder()
            .hasImage(false)
            .hasPlayImage(true)
            .playOnImageClick(false)
            .showPlayOnVideoEnd(true)
            .showImageOnVideoEnd(false)
            .showPlayOnVideoPaused(true)
            .showImageOnVideoPaused(false)
            .build()
        player.drawVideo(imageVideo, viewConfig)
    }
}
