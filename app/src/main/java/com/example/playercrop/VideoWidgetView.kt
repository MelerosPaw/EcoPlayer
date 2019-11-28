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
  lateinit var textureV: TextureView
  //  @BindView(R.id.video__view__player)
//  lateinit var playerView: PlayerView
  @BindView(R.id.video__img__image_widget)
  lateinit var imageView: ImageView
  @BindView(R.id.video__container__img__play)
  lateinit var playImage: ImageView

  private var isWifiConnection = false
  private var exoPlayer: SimpleExoPlayer? = null
  //  private lateinit var viewModel: SlideViewModel
//  private lateinit var categoryViewModel: CategoryViewModel
  private var widgetPosition: Int = 0
  private var path: String? = ""
  private lateinit var videoWidget: ImageVideoWidgetBO
  private lateinit var playerConfig: PlayerConfig
  var errorListener: () -> Unit = {}
  var useAutoPlay = false
  var wasPlaying: Boolean = false
  var isHiddingImageAndPlayButton = false
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
//      pauseVideo()
//      exoPlayer?.stop()
      lastPosition = exoPlayer?.currentPosition
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
    initialize()

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
  fun drawVideo(
    imageVideoWidget: ImageVideoWidgetBO,
    playerConfig: PlayerConfig? = null,
    isFromSpot: Boolean = false
  ) {

    this.playerConfig = playerConfig ?: PlayerConfig.Builder().build()

    videoWidget = imageVideoWidget
    widgetPosition = imageVideoWidget.position
    useDefaultPlayerController(this.playerConfig.usesDefaultPlayerControls)
    init()

    // Resize de views with the new measures
    val viewDimensions = calculateViewDimensions(imageVideoWidget)
    resizeViews(viewDimensions, container, textureV, imageView)

    setUpView(useAutoPlay)
    imageVideoWidget.image?.let { setUpImage(it, isFromSpot ) }// TODO Melero 25/11/19 isFromSpot tiene que calcularse //   fuera de esta vista.
    observeLifeCycle()
    initialize()
  }

  @Suppress("MemberVisibilityCanBePrivate")
  fun setCenterCrop() {
//     setCenterCrop(playerView)
  }

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

  fun playVideo() {
    if (exoPlayer?.playWhenReady != true) {
      exoPlayer?.playWhenReady = true
    }

    showPlay(false)

    if (!isVideoStarted()) {
      hideImageAndPlayButtonWithAnimation()
    }
  }

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

  private fun setUpView(useAutoPlay: Boolean) {
    if (isWifiConnection || useAutoPlay) {
      changeVideoAlpha(0F)
      showPlayer(true)
      showPlay(false)
      AnimationUtils.animateViewSetWithAlpha(imageView, 0, textureV, 1, 400)
      showImage(!useAutoPlay)
    }
  }

  private fun setUpImage(image: ImageWBO, isFromSpot: Boolean) {
    val imageUrl = if (isFromSpot) {
      formatSpotImageUrl(image)
    } else {
      image.path
    }
//    imageView.loadImage(imageUrl, image.width.toFloat(), image.height.toFloat(), ImageLoader.CropType.Default())
  }

  private fun setupVideo(video: VideoWBO, playerConfig: PlayerConfig, isFromSpot: Boolean) {
    video.path?.let {
      path = it
      val videoConfig: VideoConfigBO =
        videoWidget.video?.videoConfig ?: VideoConfigBO.Builder().build()
      loadVideo(it, playerConfig, videoConfig, isFromSpot)
    }
  }

  private fun calculateViewDimensions(imageVideoWidget: ImageVideoWidgetBO) =
    if (imageVideoWidget.isMatchScreen) {
      Dimensions(ScreenUtils.width(context).toInt(), ScreenUtils.heightProv(context).toInt())

      // If not matchScreen, the dimensions are calculated maintaining the image/video proportions
    } else {
      val screenWidth = ScreenUtils.width(context)
      val baseWidth =
        imageVideoWidget.image?.width ?: imageVideoWidget.video?.width ?: screenWidth.roundToInt()
      val baseHeight =
        imageVideoWidget.image?.height ?: imageVideoWidget.video?.height ?: ScreenUtils.heightProv(
          context
        ).roundToInt()
      val imageFactor = screenWidth / baseWidth

      val width = screenWidth.roundToInt()
      val height = (baseHeight * imageFactor).roundToInt()
      Dimensions(width, height)
    }

  private fun resizeViews(dimensions: Dimensions, vararg views: View) {
    views.forEach { it.redimension(dimensions.width, dimensions.height) }
  }

  private fun loadVideo(
    url: String,
    playerConfig: PlayerConfig,
    videoConfig: VideoConfigBO,
    isFromSpot: Boolean
  ) {
    if (url.isNotEmpty()) {
      exoPlayer = createPlayer(context).apply {
        val finalUrl = if (isFromSpot) {
          formatSpotVideoPath(url)
        } else {
          url
        }

        setUpExoPlayer(this, playerConfig, videoConfig)
//        setUpVideoPlayer(this, textureV, videoConfig)
        mediaSource = playVideoWithCache(context, this, finalUrl)
        textureListener(this)
        setVideoTextureView(textureV)
      }
      showPlay(true)
    }
  }

  private fun setUpExoPlayer(
    exoPlayer: SimpleExoPlayer,
    playerConfig: PlayerConfig,
    videoConfig: VideoConfigBO
  ) {
    useAutoPlay = videoConfig.autoPlay
    exoPlayer.playWhenReady = videoConfig.autoPlay
    exoPlayer.repeatMode = if (videoConfig.loop) {
      Player.REPEAT_MODE_ONE
    } else {
      Player.REPEAT_MODE_OFF
    }
    exoPlayer.addListener(ExoPlayerEventListener(playerConfig, videoConfig))
  }

//  private fun setUpVideoPlayer(exoPlayer: SimpleExoPlayer, playerView: PlayerView, config: VideoConfigBO) {
//    playerView.player = exoPlayer
//    if (config.centerCrop) {
//      setCenterCrop(playerView)
//    }
//  }

  private fun setCenterCrop(playerView: PlayerView) {
//    playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
  }

  private fun initialize() {
    videoWidget.video?.let { setupVideo(it, playerConfig, false) }
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
//    playerView.setVisible(show)
    textureV.setVisible(show)
  }

  private fun showImage(show: Boolean) {
    imageView.setVisible(playerConfig.hasImage && show)
  }

  private fun hideImageAndPlayButtonWithAnimation() {
    val animationOut = AnimationUtils.fadeOut(500)
    animationOut.setAnimationListener(object : AnimationUtils.AnimationEndListener() {
      override fun onAnimationEnd(animation: Animation) {
        isHiddingImageAndPlayButton = false
        showImage(!isPlaying())
        showPlay(!isPlaying())
      }
    })
    imageView.startAnimation(animationOut)
    isHiddingImageAndPlayButton = true
  }

  private fun isWifiConnected(): Boolean {
    return ConnectivityManager.isWifiConnected(context)
  }

  private fun ExoPlayer.isPlaying() = isReady() && playWhenReady

  private fun ExoPlayer.isReady() = playbackState == Player.STATE_READY

  private fun observeLifeCycle() {
    val lifeCycle = (context as? AppCompatActivity)?.lifecycle
    lifeCycle?.removeObserver(this)
    lifeCycle?.addObserver(this)
  }

  /*endregion*/

  /*region OnClicks*/

  @OnClick(R.id.video__img__image_widget)
  fun onImageClick() {
    if (isHiddingImageAndPlayButton) {
      onVideoClick()

    } else if (playerConfig.playOnImageClick) {
      useAutoPlay = true  // TODO Melero 26/11/19 Debería renombrarse
//      if (widgetPosition != NO_HORIZONTAL_SLIDER) {
//        viewModel.forceAutoPlay()
//      }

      playVideo()
      changeVideoAlpha(1F)
      showPlayer(true)
      hideImageAndPlayButtonWithAnimation()
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
    var playOnImageClick = true
    var hasPlayImage = true
    var showPlayOnVideoEnd = false
    var showImageOnVideoEnd = false
    var showImageOnVideoPaused = false
    var showPlayOnVideoPaused = true
    var usesDefaultPlayerControls = false

    class Builder {

      private val config = PlayerConfig()

      fun hasImage(hasImage: Boolean) = apply {
        config.hasImage = hasImage
      }

      fun playOnImageClick(playOnImageClick: Boolean) = apply {
        config.playOnImageClick = playOnImageClick
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
        config.usesDefaultPlayerControls = usesDefaultPlayerControls
      }

      fun build() = config
    }
  }

  inner class Dimensions(val width: Int, val height: Int)


  /*endregion*/

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

  private fun useDefaultPlayerController(use: Boolean) {
//    playerView.useController = use
  }

  private fun changeVideoAlpha(alpha: Float) {
//    playerView.alpha = alpha
    textureV.alpha = alpha
  }

  private fun textureListener(exoPlayer: SimpleExoPlayer) {
    textureV.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
      override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
      }

      override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
      }

      override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        releaseExoPlayer(exoPlayer)
        return false
      }

      override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, i: Int, i1: Int) {
//      if (exoPlayer != null) {
//        OkHttpDataSourceFactory okHttpDataSourceFactory = new OkHttpDataSourceFactory(
//          getOkHttpClient(),
//          Util.getUserAgent(mVideoTextureView.getContext(), null),
//          null
//        );
//
//        playerMediaSource = new ExtractorMediaSource(Uri.parse(url),
//        okHttpDataSourceFactory, new DefaultExtractorsFactory(), null,
//        null);
//
//        player.setVideoTextureView(mVideoTextureView);
//        player.addListener(
//          new ComponentListener(mVideoTextureView, image));
        mediaSource?.let { exoPlayer.prepare(it) }
      }
    }
  }
}