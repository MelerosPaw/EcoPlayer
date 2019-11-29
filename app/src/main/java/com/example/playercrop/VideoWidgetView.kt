package com.example.playercrop

//import androidx.lifecycle.ViewModelProviders
//import com.example.inditexextensions.view.getActivity
//import sdosproject.sdos.es.imageloader.ImageLoader
//import sdosproject.sdos.es.imageloader.extensions.loadImage
//import javax.inject.Inject
import android.content.Context
import android.graphics.SurfaceTexture
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.TextureView
import android.view.View
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import kotlin.math.roundToInt

class VideoWidgetView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
  : ConstraintLayout(context, attrs, defStyleAttr), LifecycleObserver {

  companion object {
    private const val NO_HORIZONTAL_SLIDER = -1
    private const val EXTENSION_JPG = ".jpg"
    private const val EXTENSION_MP4 = ".mp4"
  }

  interface OnErrorListener {
    fun onError()
  }

  /*region Properties*/
//  @Inject
//  lateinit var dashboardManager: DashboardManager

  @BindView(R.id.video__container__parent)
  lateinit var container: ConstraintLayout
  @BindView(R.id.video__view__texture_video)
  lateinit var textureView: TextureView
  @BindView(R.id.video__view__player)
  lateinit var playerView: PlayerView
  @BindView(R.id.video__img__image_widget)
  lateinit var imageView: ImageView
  @BindView(R.id.video__container__img__play)
  lateinit var playImage: ImageView

  private var isWifiConnection = false
  private var exoPlayer: SimpleExoPlayer? = null
  //  private lateinit var viewModel: SlideViewModel
//  private lateinit var categoryViewModel: CategoryViewModel
  private var widgetPosition: Int = 0
  private lateinit var videoWidget: ImageVideoWidgetBO
  private lateinit var playerConfig: PlayerConfig
  var errorListener: () -> Unit = {}
  var useAutoPlay = false
  var wasPlaying: Boolean = false
  var isHidingImageAndPlayButton = false
  var lastPosition: Long? = null
  var mediaSource: MediaSource? = null

  /*region Observers*/
//  private val sliderPositionObserver = object : ResourceObserver<Int>() {
//    override fun success(data: Int?) {
//
//      if (NO_HORIZONTAL_SLIDER != widgetPosition
//          && data != null
//          && widgetPosition == data
//          && (useAutoPlay || isWifiConnection)) {
//        playVideo()
//      }
//    }
//  }
//
//  private val pauseObserver = object : ResourceObserver<Boolean>() {
//    override fun success(data: Boolean?) {
//      data?.let {
//        if (it) {
//          pauseVideo()
//        } else {
//          if (useAutoPlay || isWifiConnection) {
//            playVideo()
//          }
//        }
//      }
//    }
//  }
//
//  private val autoPlayObserver = object : ResourceObserver<Boolean>() {
//    override fun success(data: Boolean?) {
//      if (data != null && widgetPosition != NO_HORIZONTAL_SLIDER) {
//        useAutoPlay = data
//        showImage(!useAutoPlay)
//        showPlayer(useAutoPlay)
//      }
//    }
//  }

  /*endregion*/

  /*endregion*/

  init {
    LinearLayout.inflate(context, R.layout.widget_video, this)
    ButterKnife.bind(this)
  }

  /*region Public Methods*/

  @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
  private fun onActivityDestroy() {
    releaseExoPlayer(exoPlayer)
    exoPlayer = null
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
  private fun onActivityPause() {
    if (isPlaying()) {
      wasPlaying = true
      pauseVideo()
//      exoPlayer?.stop()
//      lastPosition = exoPlayer?.currentPosition
    }
    releaseExoPlayer(exoPlayer)
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
  private fun onActivityResume() {
//    exoPlayer?.let { player ->
//      videoWidget.video?.path?.let {
//        mediaSource?.let { player.prepare(it) }
//      }
//    }
    doIfUnderTheHood(UnderTheHood.PLAYER_VIEW, {
      showImage(true)
    })
    initializeVideo()

    lastPosition?.let { seekTo(it) }
    if (wasPlaying) { // TODO Melero 26/11/19 Aquí había un if de useAutoPlay
      wasPlaying = false
      playVideo()
    }
  }

  fun drawSpotVideo(imageVideoWidgetBO: ImageVideoWidgetBO?) {
    imageVideoWidgetBO?.let {
      drawVideo(it, isFromSpot = true)
    }
  }

  @JvmOverloads
  fun drawVideo(imageVideoWidget: ImageVideoWidgetBO, playerConfig: PlayerConfig? = null,
                isFromSpot: Boolean = false) {

    this.playerConfig = playerConfig ?: PlayerConfig.Builder().build()

    videoWidget = imageVideoWidget
    widgetPosition = imageVideoWidget.position
    init()

    // Resize de views with the new measures
    val viewDimensions = calculateViewDimensions(imageVideoWidget)
    resizeViews(viewDimensions, container, textureView, imageView)

    observeLifeCycle()
    setUpView(this.playerConfig, useAutoPlay)
    setUpImage(null, false)
//    imageVideoWidget.image?.let { setUpImage(it, isFromSpot) }
    initializeVideo()
  }

  @Suppress("MemberVisibilityCanBePrivate")
  fun pauseVideo() {
    exoPlayer?.playWhenReady = false
    lastPosition = exoPlayer?.currentPosition

    if (playerConfig.showPlayOnVideoPaused) {
      showPlay(true)
    }

    if (playerConfig.showImageOnVideoPaused) {
      showImage(true)
    }
  }

  @Suppress("MemberVisibilityCanBePrivate")
  fun playVideo() {
    if (exoPlayer?.playWhenReady != true) {
      exoPlayer?.playWhenReady = true
    }

    showPlay(false)

    if (!isVideoStarted()) {
      hideImageAndPlayButtonWithAnimation()
    }
  }

  @Suppress("MemberVisibilityCanBePrivate")
  fun seekTo(milliseconds: Long) {
    exoPlayer?.seekTo(milliseconds)
  }

  @Suppress("MemberVisibilityCanBePrivate")
  fun replay() {
    seekTo(0)
    playVideo()
  }

  @Suppress("MemberVisibilityCanBePrivate")
  fun isPlaying() = exoPlayer?.isPlaying() == true

  @Suppress("MemberVisibilityCanBePrivate")
  fun isReady() = exoPlayer?.isReady() == true

  @Suppress("MemberVisibilityCanBePrivate")
  fun isVideoOver() = exoPlayer?.playbackState == Player.STATE_ENDED

  @Suppress("MemberVisibilityCanBePrivate")
  fun isVideoStarted() = exoPlayer?.let { it.currentPosition > 0L } ?: false

  @Suppress("MemberVisibilityCanBePrivate")
  fun setPlayImage(@DrawableRes drawableId: Int) {
    setPlayImage(ContextCompat.getDrawable(context, drawableId))
  }

  @Suppress("MemberVisibilityCanBePrivate")
  fun setPlayImage(drawable: Drawable?) {
    playImage.setImageDrawable(drawable)
  }

  fun setOnErrorListener(errorListener: OnErrorListener) {
    this.errorListener = errorListener::onError
  }

  /*endregion*/

  /*region Private Methods*/

  private fun init() {
//    DIManager.getAppComponent().inject(this)
//    getActivity()?.let { activity ->
//      viewModel = ViewModelProviders.of(activity).get(SlideViewModel::class.java)
//      categoryViewModel = ViewModelProviders.of(activity).get(CategoryViewModel::class.java)
//      viewModel.positionLiveData.observe(activity, sliderPositionObserver)
//      viewModel.pauseVideo.observe(activity, pauseObserver)
//      viewModel.autoPlay.observe(activity, autoPlayObserver)
    isWifiConnection = isWifiConnected()
  }

  private fun releaseExoPlayer(player: SimpleExoPlayer?) {
    if (player != null) {
      player.stop()
      player.release()
      player.setVideoTextureView(null)
      player.setVideoSurfaceView(null)
      mediaSource = null
      exoPlayer = null
    }
  }

  private fun setUpView(playerConfig:PlayerConfig, useAutoPlay: Boolean) {
    if (isWifiConnection || useAutoPlay) {
      hideUnusedPlayerView()
      useDefaultPlayerController(playerConfig.usesDefaultPlayerController)
      changePlayerAlpha(0F)
      showPlayer(true)
      showPlay(false)
      AnimationUtils.animateViewSetWithAlpha(imageView, 0, getUnderTheHood(), 1, 400)
      showImage(!useAutoPlay)
    }
  }

  private fun setUpImage(image: ImageWBO?, isFromSpot: Boolean) {
//    val imageUrl = if (isFromSpot) { formatSpotImageUrl(image) } else { image.path }
//    imageView.loadImage(imageUrl, image.width.toFloat(), image.height.toFloat(), ImageLoader.CropType.Default())
  }

  private fun setUpVideo(video: VideoWBO, playerConfig: PlayerConfig, isFromSpot: Boolean) {
    video.path?.let {
      val videoConfig: VideoConfigBO = video.videoConfig ?: VideoConfigBO.Builder().build()
      loadVideo(it, playerConfig, videoConfig, isFromSpot)
    }
  }

  private fun calculateViewDimensions(imageVideoWidget: ImageVideoWidgetBO) =
    if (imageVideoWidget.isMatchScreen) {
      Dimensions(ScreenUtils.width(context).toInt(), ScreenUtils.heightProv(context).toInt())

      // If not matchScreen, the dimensions are calculated maintaining the image/video proportions
    } else {
      val screenWidth = ScreenUtils.width(context)
      val baseWidth = imageVideoWidget.image?.width ?: imageVideoWidget.video?.width ?: screenWidth.roundToInt()
      val baseHeight = imageVideoWidget.image?.height ?: imageVideoWidget.video?.height ?: ScreenUtils.heightProv(context).roundToInt()
      val imageFactor = screenWidth / baseWidth

      val width = screenWidth.roundToInt()
      val height = (baseHeight * imageFactor).roundToInt()
      Dimensions(width, height)
    }

  private fun resizeViews(dimensions: Dimensions, vararg views: View) {
    views.forEach { it.redimension(dimensions.width, dimensions.height) }
  }

  private fun loadVideo(url: String, playerConfig: PlayerConfig, videoConfig: VideoConfigBO,
                        isFromSpot: Boolean) {
    if (url.isNotEmpty()) {
      exoPlayer = createPlayer(context).apply {
        val finalUrl = formatUrl(isFromSpot, url)
        val mediaSource = getMediaSource(playerConfig.usesCache, finalUrl)

        setUpExoPlayer(this, playerConfig, videoConfig)
        joinVideoAndPlayer(this)
        prepare(mediaSource)
        showPlay(true)
      }
    }
  }

  private fun formatUrl(isFromSpot: Boolean, url: String) =
    if (isFromSpot) { formatSpotVideoPath(url) } else { url }

  private fun setUpExoPlayer(exoPlayer: SimpleExoPlayer, playerConfig: PlayerConfig,
                             videoConfig: VideoConfigBO) {
    useAutoPlay = videoConfig.autoPlay
    exoPlayer.playWhenReady = videoConfig.autoPlay
    exoPlayer.repeatMode = Player.REPEAT_MODE_ONE.takeIf { videoConfig.loop } ?: Player.REPEAT_MODE_OFF
    exoPlayer.addListener(ExoPlayerEventListener(playerConfig, videoConfig))
    if (playerConfig.centerCrop) {
      setCenterCrop()
    }
  }

  private fun joinVideoAndPlayer(exoPlayer: SimpleExoPlayer) {
    doIfUnderTheHood(UnderTheHood.TEXTURE_VIEW,
      { exoPlayer.setVideoTextureView(it as TextureView) },
      { (it as PlayerView).player = exoPlayer })
  }

  private fun getMediaSource(useCache: Boolean, videoUrl: String) : MediaSource? =
    if (useCache) {
      getCachedMediaSource(context, videoUrl)
    } else {
      getMediaSource(context, videoUrl)
    }

  private fun setCenterCrop() {
    doIfUnderTheHood(UnderTheHood.PLAYER_VIEW,
      { (it as PlayerView).resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM },
      { setTextureViewCenterCrop(it as TextureView) })
  }

  private fun setTextureViewCenterCrop(textureView: TextureView) {
    // // TODO Juan Jose Melero 28/11/2019 No implementation!!!
  }

  private fun initializeVideo() {
    videoWidget.video?.let { setUpVideo(it, playerConfig, false) }
  }

  private fun formatSpotImageUrl(image: ImageWBO): String {
////     Endpoint
//    var imageUrl = DIManager.getAppComponent().sessionData.store.staticUrl
//    // Get store timestamp
//    val storeTimestamp = DIManager.getAppComponent().sessionData.store.timeStamp
//    // Path
//    imageUrl += image.path
//    // Extension
//    imageUrl += EXTENSION_JPG
//    // Timestamp
//    if (!TextUtils.isEmpty(image.timestamp)) {
//      imageUrl += "?t=$image.timestamp"
//    } else if (!TextUtils.isEmpty(storeTimestamp)) {
//      imageUrl += "?t=$storeTimestamp"
//    }
//
//    return imageUrl
    return image.path
  }

  private fun formatSpotVideoPath(path: String): String {
//    // Endpoint
//    var url = DIManager.getAppComponent().sessionData.store.staticUrl
//    // Get store timestamp
//    val storeTimestamp = DIManager.getAppComponent().sessionData.store.timeStamp
//    // Path
//    url += path
//    // Extension
//    url += EXTENSION_MP4
//    // Timestamp!
//    if (TextUtils.isEmpty(storeTimestamp)) {
//      url += "?t=$storeTimestamp"
//    }
//
//    return url
    return path
  }

  private fun showPlay(show: Boolean) {
    playImage.setVisible(playerConfig.hasPlayImage && show)
  }

  private fun showPlayer(show: Boolean) {
    getUnderTheHood().setVisible(show)
  }

  private fun showImage(show: Boolean) {
    val willShow = playerConfig.hasImage && show
    imageView.setVisible(willShow)
    if (willShow) { imageView.alpha = 1F }
  }

  private fun hideImageAndPlayButtonWithAnimation() {
    val animationOut = AnimationUtils.fadeOut(500)
    animationOut.setAnimationListener(object : AnimationUtils.AnimationEndListener() {
      override fun onAnimationEnd(animation: Animation) {
        isHidingImageAndPlayButton = false
        showImage(!isPlaying())
        showPlay(!isPlaying())
      }
    })
    imageView.startAnimation(animationOut)
    isHidingImageAndPlayButton = true
  }

  private fun isWifiConnected(): Boolean {
    return ConnectivityManager.isWifiConnected(context)
  }

  private fun observeLifeCycle() {
    val lifeCycle = (context as? AppCompatActivity)?.lifecycle
    lifeCycle?.removeObserver(this)
    lifeCycle?.addObserver(this)
  }

  private fun getUnderTheHood() =
    textureView.takeIf { playerConfig.underTheHood == UnderTheHood.TEXTURE_VIEW } ?: playerView

  private fun doIfUnderTheHood(expectedUnderTheHood: UnderTheHood, what: (view: View) -> Unit,
                               inAnyOtherCase:(otherView: View) -> Unit = {}) {
    val underTheHoodView = getUnderTheHood()
    if (playerConfig.underTheHood == expectedUnderTheHood) {
      what(underTheHoodView)
    } else {
      inAnyOtherCase(underTheHoodView)
    }
  }

  private fun useDefaultPlayerController(use: Boolean) {
    doIfUnderTheHood(UnderTheHood.PLAYER_VIEW, { (it as PlayerView).useController = use })
  }

  private fun hideUnusedPlayerView() {
    doIfUnderTheHood(UnderTheHood.PLAYER_VIEW,
      { textureView.setVisible(false) },
      { playerView.setVisible(false) })
  }

  private fun changePlayerAlpha(alpha: Float) {
    getUnderTheHood().alpha = alpha
  }

  /*endregion*/

  /*region OnClicks*/

  @OnClick(R.id.video__img__image_widget)
  fun onImageClick() {
    if (isHidingImageAndPlayButton) {
      onVideoClick()

    } else if (playerConfig.playsOnImageClick) {
      useAutoPlay = true  // TODO Melero 26/11/19 Debería renombrarse
//      if (widgetPosition != NO_HORIZONTAL_SLIDER) {
//        viewModel.forceAutoPlay()
//      }

      playVideo()
      showPlayer(true)
    }
  }

  @OnClick(R.id.video__container__img__play)
  fun onPlayClicked() {
    if (isVideoOver()) {
      replay()
    } else {
      playVideo()
    }
  }

  @OnClick(R.id.video__container__parent) // TODO Melero 26/11/19 Esto no saltará en cualquier otro momento indeseado?
  fun onVideoClick() {
    videoWidget.video?.onVideoClickedListener?.onVideoClicked(isReady(), isPlaying())
  }

  /*endregion*/

  //region Extensions

  private fun ExoPlayer.isPlaying() = isReady() && playWhenReady

  private fun ExoPlayer.isReady() = playbackState == Player.STATE_READY

  fun View.redimension(width: Int, height: Int) {
    layoutParams.width = width
    layoutParams.height = height
  }

  @JvmOverloads
  fun View?.setVisible(condition: Boolean, doWhenVisible: () -> Unit = {}) {
    this?.visibility = if (condition) {
      doWhenVisible()
      View.VISIBLE
    } else {
      View.GONE
    }
  }

  // endregion

  /*region Inner Classes*/

  private inner class ExoPlayerEventListener(
    private val playerConfig: PlayerConfig,
    private val videoConfig: VideoConfigBO
  ) : Player.DefaultEventListener() {

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
      when (playbackState) {
        Player.STATE_BUFFERING -> {
          if (useAutoPlay || isWifiConnection) {
            showPlayer(true)
            if (playerConfig.hasImage) {
              hideImageAndPlayButtonWithAnimation()
            }
          }
        }
        Player.STATE_READY -> {
          if (useAutoPlay || isWifiConnection) {
//            showPlay(false)   // TODO Melero 27/11/19 Descomentar para Pull. Configurable, ¿Cómo?
          }
        }
        Player.STATE_ENDED -> {

          // Workaround to solve automatic milliseconds playback when leaving an resuming
//          if (!videoConfig.loop) {
//            seekTo(0)
//            pauseVideo()
//          }

          lastPosition = exoPlayer?.currentPosition

          if (playerConfig.showPlayOnVideoEnd) {
            showPlay(true)
          }

          if (playerConfig.showImageOnVideoEnd) {
            showImage(true)
          }
        }
      }
    }

    override fun onPlayerError(error: ExoPlaybackException?) {
      super.onPlayerError(error)
      errorListener()
      releaseExoPlayer(exoPlayer)
    }
  }

  class PlayerConfig private constructor() {

    var hasImage = true
    var usesCache = false
    var centerCrop = false
    var hasPlayImage = true
    var underTheHood: UnderTheHood = UnderTheHood.TEXTURE_VIEW
    var playsOnImageClick = true
    var showPlayOnVideoEnd = false
    var showImageOnVideoEnd = false
    var showImageOnVideoPaused = false
    var showPlayOnVideoPaused = true
    var usesDefaultPlayerController = false

    class Builder {

      private val config = PlayerConfig()

      fun hasImage(hasImage: Boolean) = apply {
        config.hasImage = hasImage
      }

      fun playsOnImageClick(playsOnImageClick: Boolean) = apply {
        config.playsOnImageClick = playsOnImageClick
      }

      fun hasPlayImage(hasPlayImage: Boolean) = apply {
        config.hasPlayImage = hasPlayImage
      }

      fun showPlayOnVideoEnd(showPlayOnVideoEnd: Boolean) = apply {
        config.showPlayOnVideoEnd = showPlayOnVideoEnd
      }

      fun showImageOnVideoEnd(showImageOnVideoEnd: Boolean) = apply {
        config.showImageOnVideoEnd = showImageOnVideoEnd
      }

      fun showImageOnVideoPaused(showImageOnVideoPaused: Boolean) = apply {
        config.showImageOnVideoPaused = showImageOnVideoPaused
      }

      fun showPlayOnVideoPaused(showPlayOnVideoPaused: Boolean) = apply {
        config.showPlayOnVideoPaused = showPlayOnVideoPaused
      }

      fun usesDefaultPlayerControls(usesDefaultPlayerControls: Boolean) = apply {
        config.usesDefaultPlayerController = usesDefaultPlayerControls
      }

      fun underTheHood(underTheHood: UnderTheHood) = apply {
        config.underTheHood = underTheHood
      }

      fun usesCache(usesCache: Boolean) = apply {
        config.usesCache = usesCache
      }

      fun centerCrop(centerCrop: Boolean) = apply {
        config.centerCrop = centerCrop
      }

      fun build() = config
    }
  }

  inner class Dimensions(val width: Int, val height: Int)

  enum class UnderTheHood(private val id: Int) {
    TEXTURE_VIEW(0), PLAYER_VIEW(1)
  }


  /*endregion*/
}