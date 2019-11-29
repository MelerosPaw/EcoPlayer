package com.example.playercrop

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.offline.DownloadService
import com.google.android.exoplayer2.offline.DownloaderConstructorHelper
import com.google.android.exoplayer2.offline.ProgressiveDownloadAction
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util
import okhttp3.OkHttpClient
import java.io.File

fun getCachedMediaSource(context: Context, videoUrl: String): MediaSource? {

    // Data source
    val okHttpDataSourceFactory = OkHttpDataSourceFactory(getOkHttpClient(),
        Util.getUserAgent(context, null), null
    )

    // Offline cache
    val parsedUrl = Uri.parse(videoUrl)
    val progressiveDownloadAction = ProgressiveDownloadAction(parsedUrl, false, null, null)

    DownloadService.startWithAction(context, ExoPlayerDownloadService::class.java,
        progressiveDownloadAction,true)

    // Playback
    val cacheDataSourceFactory = CacheDataSourceFactory(getDownloadCache(context), okHttpDataSourceFactory)
    return ExtractorMediaSource.Factory(cacheDataSourceFactory).createMediaSource(parsedUrl)
}

fun getMediaSource(context: Context, videoUrl: String): MediaSource {
    val okHttpDataSourceFactory = OkHttpDataSourceFactory(getOkHttpClient(),
          Util.getUserAgent(context, null), null)

    return ExtractorMediaSource(Uri.parse(videoUrl), okHttpDataSourceFactory,
        DefaultExtractorsFactory(), null, null)
//        player.addListener(new ComponentListener(mVideoTextureView, image));
}

private fun getOkHttpClient(): OkHttpClient {
    return ApiModule.getUnsafeOkHttpClient(null).build()

}

fun createPlayer(context: Context): SimpleExoPlayer {
    val player: SimpleExoPlayer
    val adaptiveTrackSelectionFactory = AdaptiveTrackSelection.Factory(DefaultBandwidthMeter())
    val trackSelector = DefaultTrackSelector(adaptiveTrackSelectionFactory)
    player = ExoPlayerFactory.newSimpleInstance(context, trackSelector)
    return player
}

fun getDownloadManager(context: Context): DownloadManager? {
    initDownloadManager(context)
    return downloadManager
}

@Synchronized
private fun initDownloadManager(context: Context) {
    if (downloadManager == null) {
        val downloaderConstructorHelper = DownloaderConstructorHelper(getDownloadCache(context), buildHttpDataSourceFactory(context))
        downloadManager = DownloadManager(
            downloaderConstructorHelper,
            MAX_SIMULTANEOUS_DOWNLOADS,
            DownloadManager.DEFAULT_MIN_RETRY_COUNT,
            File(getDownloadDirectory(context), DOWNLOAD_ACTION_FILE)
        )
    }
}

private var downloadManager: DownloadManager? = null
private var downloadDirectory: File? = null
private var downloadCache: Cache? = null
private var playerMediaSource: MediaSource? = null

private const val DOWNLOAD_ACTION_FILE = "actions"
private const val DOWNLOAD_CONTENT_DIRECTORY = "downloads"
private const val MAX_SIMULTANEOUS_DOWNLOADS = 2

private fun getDownloadDirectory(context: Context): File? {
    if (downloadDirectory == null) {
        downloadDirectory = context.getExternalFilesDir(null)
        if (downloadDirectory == null) {
            downloadDirectory = context.filesDir
        }
    }
    return downloadDirectory
}

@Synchronized
fun getDownloadCache(context: Context): Cache? {
    if (downloadCache == null) {
        val downloadContentDirectory = File(getDownloadDirectory(context), DOWNLOAD_CONTENT_DIRECTORY)
        downloadCache = SimpleCache(downloadContentDirectory, NoOpCacheEvictor())
    }
    return downloadCache
}

fun buildHttpDataSourceFactory(context: Context): HttpDataSource.Factory {
    val userAgent = Util.getUserAgent(context, null)
    return DefaultHttpDataSourceFactory(userAgent)
}

fun releaseExoPlayer(player: SimpleExoPlayer?) {
    if (player != null) {
        player.stop()
        player.release()
        player.setVideoTextureView(null)
        player.setVideoSurfaceView(null)
        playerMediaSource = null
    }
}