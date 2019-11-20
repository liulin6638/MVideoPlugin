package com.kvideo.flutter_kvideo_plugin

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.mvideo.flutter_mvideo_plugin.R
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import com.shuyu.gsyvideoplayer.utils.OrientationUtils
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.platform.PlatformView
//import me.panpf.sketch.SketchImageView

/**
 * PackageName : com.kvideo.flutter_kvideo_plugin <br/>
 *
 * Creator : sun <br/>
 *
 * CreateDate : 2019-07-17 <br/>
 *
 * CreateTime : 15 : 15 <br/>
 *
 * Description : 用于视频播放的View
 */
class VideoPlayerView(var context: Context?, var viewId: Int, var args: Any?,
    private var registrar: PluginRegistry.Registrar) : PlatformView, MethodChannel.MethodCallHandler {

  private val inflater: LayoutInflater = LayoutInflater.from(registrar.activity())
  @SuppressLint("InflateParams")
  private val mVideo = inflater.inflate(R.layout.m_video, null) as StandardGSYVideoPlayer
  private var methodChannel = MethodChannel(registrar.messenger(), "flutter_mvideo_plugin_$viewId")

  private lateinit var orientationUtils: OrientationUtils

  private lateinit var mVideoOptions: GSYVideoOptionBuilder

  private var isPlay: Boolean = false
  private var isPause: Boolean = false
  private var isFullScreen: Boolean = false

  init {
    this.methodChannel.setMethodCallHandler(this)
  }

  override fun getView() = mVideo


  override fun onMethodCall(methodCall: MethodCall, chanel: MethodChannel.Result) {
    when (methodCall.method) {
      "loadUrl" -> {
        ///视频播放地址
        val url = methodCall.argument<String>("videoUrl")
        ///视频封面
        val cover = methodCall.argument<String>("cover")

        initVideo(url!!,cover!!)
      }
      "onPause" ->{
        mVideo.currentPlayer.onVideoPause()
      }
      "onResume" ->{
        mVideo.currentPlayer.onVideoResume(true)
      }

      ///退出全屏
      "quitFullScreen"->{
        val isFullScreen = GSYVideoManager.backFromWindowFull(registrar.activity())
        if(isFullScreen) chanel.success(false) else chanel.success(true)
      }
      else -> {

      }
    }

  }

  override fun dispose() {
    if (isPlay) mVideo.currentPlayer.release()
    orientationUtils.releaseListener()

  }

  //创建播放器相关的参数
  private fun initVideo(url: String,cover:String) {
    //初始化旋转
    orientationUtils = OrientationUtils(registrar.activity(), mVideo)

    //是否旋转
    mVideo.isRotateViewAuto = false

    //是否可以滑动调整
    mVideo.setIsTouchWiget(true)

    //添加封面
    val  imageView= ImageView(registrar.activity())
    imageView.scaleType = ImageView.ScaleType.FIT_XY
    Glide.with(registrar.activity()).load(cover).into(imageView)
//    imageView.displayImage(cover)
//    Toast.makeText(registrar.activity(),cover,0).show()
//    val imageView = ImageView(registrar.activity())
//    Glide.with(registrar.context()).load(cover).into(imageView)

//    Log.d("图片地址",cover)
//    imageView.setImageResource(R.drawable.exo_icon_vr)
//    mVideo.thumbImageView = imageView

    //返回按钮是否可见
    mVideo.backButton.visibility = View.GONE

    //处事换不打开外部旋转
    mVideo.isEnabled = false

    //播放器的参数设置
    mVideoOptions = GSYVideoOptionBuilder()

    mVideoOptions.setThumbImageView(imageView)
        .setIsTouchWiget(true)
        .setRotateViewAuto(false)
        .setLockLand(false)
        .setAutoFullWithSize(true)
        .setShowFullAnimation(true)
        .setNeedLockFull(true)
        .setUrl(url)
        .setCacheWithPlay(true)
        .setLockClickListener { _, lock ->
          orientationUtils.isEnable = !lock
        }
        .setVideoAllCallBack(object : GSYSampleCallBack() {

          //加载完成
          override fun onPrepared(url: String?, vararg objects: Any?) {
            super.onPrepared(url, *objects)
            orientationUtils.isEnable = true
            isPlay = true
          }

          //退出全屏
          override fun onQuitFullscreen(url: String?, vararg objects: Any?) {
            super.onQuitFullscreen(url, *objects)
            orientationUtils.backToProtVideo()
          }
        })
        .build(mVideo)

    //点击全屏按钮
    mVideo.fullscreenButton.setOnClickListener {
      orientationUtils.resolveByClick()
      mVideo.startWindowFullscreen(registrar.activity(), true, true)

    }
    mVideo.startPlayLogic()
  }


}




