package com.addx.ai.demo.videoview

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Looper
import android.os.SystemClock
import android.text.TextUtils
import android.util.AttributeSet
import android.view.*
import android.view.View.OnClickListener
import android.widget.*
import com.addx.common.Const
import com.addx.common.utils.*
import com.ai.addx.model.DeviceBean
import com.ai.addx.model.Ratio
import com.ai.addx.model.SleepPlanData
import com.ai.addx.model.request.ReporLiveCommonEntry
import com.ai.addx.model.request.ReporLiveInterruptEntry
import com.ai.addx.model.request.SerialNoEntry
import com.ai.addx.model.response.GetSingleDeviceResponse
import com.ai.addxbase.*
import com.ai.addxbase.addxmonitor.AddxMonitor
import com.ai.addxbase.addxmonitor.FileLogUpload
import com.ai.addxbase.helper.SharePreManager
import com.ai.addxbase.util.LocalDrawableUtills
import com.ai.addxbase.util.TimeUtils
import com.ai.addxbase.util.ToastUtils
import com.ai.addxbase.view.dialog.CommonCornerDialog
import com.ai.addxnet.ApiClient
import com.ai.addxnet.HttpSubscriber
import com.ai.addxvideo.addxvideoplay.*
import com.ai.addxvideo.addxvideoplay.addxplayer.*
import com.ai.addxvideo.addxvideoplay.addxplayer.addxijkplayer.AddxGLSurfaceView
import com.ai.addxvideo.addxvideoplay.addxplayer.addxijkplayer.AddxVideoIjkPlayer
import com.ai.addxvideo.addxvideoplay.addxplayer.webrtcplayer.*
import com.base.resmodule.view.LoadingDialog
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.File
import java.io.FileNotFoundException
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import com.addx.ai.demo.R

open abstract class DemoBaseVideoView : FrameLayout, IAddxView, IAddxPlayerStateListener, View.OnClickListener, View.OnTouchListener  {

    private var mOldOpt: Int = 0
    private var mOldState: Int = 0
    public var currentOpt: Int = 0
    val TAG = "AddxBaseVideoView" + hashCode()
    open var iAddxPlayer: IVideoPlayer? = null
    var dataSourceBean: DeviceBean? = null
    var mNetWorkDialog: CommonCornerDialog? = null
    var currentState = CURRENT_STATE_NORMAL
    var mIsFullScreen = false
    val activityContext: Activity by lazy { CommonUtil.getActivityContext(context) }
    var mute: Boolean = true
    var mSystemUiVisibility = 0
    open var disableCropFrame = true
    open var mShowing: Boolean = false
    var mVideoCallBack: IAddxViewCallback? = null
    open val DEFAULT_SHOW_TIME = 3500L
    open var mVideoRatio: Ratio = Ratio.P720
    val mFadeOut = Runnable {
        hide()
    }
    var liveStartTime = 0L
    protected var videoSavePath: String? = null

    @Volatile
    protected var isRecording = false
    protected var recordCounterTask: Subscription? = null
    var mSavePlayState: Int = CURRENT_STATE_NORMAL

    @Volatile
    var isChanggingRatio: Boolean = false

    //most common views
    var defaultThumbRid: Int? = R.mipmap.live_default_cover
    var soundBtn: ImageView? = null
    var fullScreenBtn: View? = null
    open var startBtn: ImageView? = null
    var thumbImage: ImageView? = null
    private var renderContainer: ViewGroup? = null
    var renderView: SurfaceView? = null
    open lateinit var mAddxVideoContentView: View
    var ivErrorFlag: ImageView? = null
    private var ivErrorExit: ImageView? = null
    private var ivErrorSetting: ImageView? = null
    protected var tvErrorTips: TextView? = null
    open var tvErrorButton: TextView? = null
    var tvUnderLineErrorBtn: TextView? = null
    private var ivErrorHelp: ImageView? = null
    var errorLayout: ViewGroup? = null
    private var oldLayoutParams: ViewGroup.LayoutParams? = null
    private lateinit var mAddxVideoViewParent: ViewGroup
    var normalLayout: ViewGroup? = null
    var loadingLayout: LinearLayout? = null
    var tvDownloadSpeed: TextView? = null
    var ivErrorThumb: ImageView? = null
    var animShotView: LinearLayout? = null
    var recordIcon: ImageView? = null
    var recordTimeText: TextView? = null

    @Volatile
    var isSavingRecording: Boolean = false
    var playBeginTimeRecord: Long = 0
    var playTimeRecordSpan: Long = 0
    var savingRecordLoading: LinearLayout? = null
    var availableSdcardSize: Float? = null
    open var thumbSaveKeySuffix: String? = null
    var mShowStartTimeSpan: Long = 0
    var startBtnAction = {
        if(isPlaying() && System.currentTimeMillis() - mShowStartTimeSpan >= START_SHOW_SPAN){
            startBtn?.visibility = View.INVISIBLE
        }
    }
    var downloadStringBuilder:StringBuilder=StringBuilder()
    var START_SHOW_SPAN: Long = 3000
    var mBitmap: Bitmap? = null
    var mStopType: String = "endButton"
    var mShowRecordShotToast: Boolean = false
    //    var mUploadlog: ImageView? = null
    var mIsNeedUploadFailLog: Boolean = false
    var mIsUserClick = false
    var mClickId: String? = null
    var mIsSplit: Boolean = false
    var mLocalThumbTime: Long = 0
    var mServerThumbTime: Long = 0
    companion object {
        const val CURRENT_STATE_NORMAL = 0             //正常
        const val CURRENT_STATE_PREPAREING = 1        //准备中
        const val CURRENT_STATE_PLAYING = 2           //播放中
        const val CURRENT_STATE_PAUSE = 3             //暂停
        const val CURRENT_STATE_AUTO_COMPLETE = 4     //自动播放结束
        const val CURRENT_STATE_ERROR = 5             //错误状态

        @JvmStatic
        fun getThumbImagePath(context: Context, sn: String): String?{
            if(sn == null){
                return null
            }
            var localThumbTime = VideoSharePreManager.getInstance(context).getThumbImgLastLocalFreshTime(
                sn
            )/1000
            var serverThumbTime = VideoSharePreManager.getInstance(context).getThumbImgLastServerFreshTime(
                sn
            )
            var imgPath:String? = null
            if(serverThumbTime > localThumbTime){
                imgPath = DownloadUtil.getThumbImgDir(context) + MD5Util.md5(sn)+".jpg"
            }
            if (FileUtils.isFileExists(imgPath)) {
                return imgPath
            }else{
                imgPath = LocalDrawableUtills.instance.getDiskCacheFilePath(sn.plus(null))
                if (FileUtils.isFileExists(imgPath)) {
                    return imgPath
                }
            }
            return null
        }
        @JvmStatic
        var mIsGetNewThumbTime: Long = 0
    }

    constructor(context: Context) : super(context) {
        getDeclaredAttrs(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        getDeclaredAttrs(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        getDeclaredAttrs(context, attrs)
    }

    protected open fun getDeclaredAttrs(context: Context, attrs: AttributeSet?) {
        // do some init things
    }

    open fun init(context: Context?, bean: DeviceBean, iAddxViewCallback: IAddxViewCallback) {
        if (bean == null) {
            throw NullPointerException("$TAG---setDeviceBean----sn is null")
        }
//        LogUtils.w(TAG, "setDeviceBean-------" + bean.serialNumber)
        this.dataSourceBean = bean
        iAddxPlayer = AddxPlayerManager.getInstance().getPlayer(dataSourceBean)
        iAddxPlayer?.setDataSource(dataSourceBean)
//        setBackgroundColor(Color.BLACK)
        setOnClickListener(this)
        mute = AddxAudioSet.getMuteState(dataSourceBean?.serialNumber)
        reloadLayout(context)
        setDeviceState(false, false)
        mVideoCallBack = iAddxViewCallback
    }

    //状态处理
    override fun onPrepared(player: IVideoPlayer) {
        LogUtils.w(
            TAG,
            "---------AddxBaseVideoView-------onPrepared-------sn:${dataSourceBean!!.serialNumber}"
        )
        playTimeRecordSpan= SystemClock.elapsedRealtime()-liveStartTime
        mShowStartTimeSpan = System.currentTimeMillis()
        //暂时通过延时1s，解决串屏问题
        postDelayed({
            if (iAddxPlayer != null && iAddxPlayer?.isPlaying()!!) {
                updateStateAndUI(
                    CURRENT_STATE_PLAYING,
                    if (currentOpt == PlayerErrorState.ERROR_DEVICE_NEED_OTA) PlayerErrorState.ERROR_DEVICE_NEED_OTA else 0
                )
                iAddxPlayer?.muteVoice(mute, true)
                if (mVideoCallBack != null) {
                    mVideoCallBack!!.onStartPlay()
                }
            }
        }, 1000)
    }

    override fun onPreparing(player: IVideoPlayer) {
        LogUtils.w(
            TAG,
            "AddxBaseVideoView---------------AddxBaseVideoView-------onPreparing----sn:${dataSourceBean?.serialNumber}"
        )
        post {
            updateStateAndUI(CURRENT_STATE_PREPAREING, currentOpt)
        }
    }

    override fun onVideoSizeChanged(player: IVideoPlayer, ratio: Ratio?) {
        //设置ratio真正成功后的回调
        LogUtils.d(TAG, dataSourceBean?.serialNumber + "----onVideoSizeChanged success")
    }

    override fun onError(player: IVideoPlayer, what: Int, extra: Int) {
        LogUtils.w(
            TAG,
            "AddxBaseVideoView--------------AddxBaseVideoView--------onError-------sn:${dataSourceBean!!.serialNumber}"
        )
        playTimeRecordSpan = 0
        post {
            stopRecordVideo("error")
            if(what == PlayerErrorState.ERROR_DEVICE_MAX_CONNECT_LIMIT){
                updateStateAndUI(CURRENT_STATE_ERROR, what)
            }else{
                checkDeviceExit()
            }
        }
    }

    override fun onVideoPause(player: IVideoPlayer) {
        LogUtils.w(
            TAG,
            dataSourceBean?.serialNumber + "---------AddxBaseVideoView-------onVideoPause-------sn:${dataSourceBean!!.serialNumber}"
        )
        post {
            activityContext.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            updateStateAndUI(CURRENT_STATE_PAUSE, currentOpt)
        }
        if (mVideoCallBack != null) {
            mVideoCallBack!!.onStopPlay()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onDownloadSpeedUpdate(player: IVideoPlayer, bytes: Long) {
        if (SystemClock.elapsedRealtime()-liveStartTime<=60000){
            if (downloadStringBuilder.isNotEmpty()) {
                downloadStringBuilder.append(",")
            }
            downloadStringBuilder.append(bytes / 1024)
            downloadStringBuilder.append("K/s")
        }

        post {
            val df = DecimalFormat("0.0")
            val M = 1024 * 1024.toLong()
            if (bytes >= M) {
                val v = bytes * 1f / M
                tvDownloadSpeed?.text = df.format(v.toDouble()) + "M/s"
            } else {
                tvDownloadSpeed?.text = df.format(bytes / 1024f.toDouble()) + "K/s"
            }
        }
    }

    override fun onSeekComplete(player: IVideoPlayer) {

    }

    override fun onRetryConnect() {
        post {
//            iAddxPlayer?.playerStatInfo?.misretryconnect = true
            mIsUserClick = false
            stopRecordVideo("onRetryConnect")
            startPlay()
        }
    }

    //===========以上为状态处理
    override fun isPlaying(): Boolean {
        return currentState == CURRENT_STATE_PLAYING
    }

    fun isPrepareing(): Boolean {
        return currentState == CURRENT_STATE_PREPAREING
    }

    override fun startPlay() {
        LogUtils.w(
            TAG,
            "AddxBaseVideoView---------------startPlay--------------sn:${dataSourceBean!!.serialNumber}"
        )
        if (dataSourceBean == null) {
            throw NullPointerException("$TAG---startPlay----sn is null")
        }

        startPlayInternal()
    }

    internal open fun resetRatioInfoForG0() {
        dataSourceBean?.let {
            if (it.deviceModel.isG0) {
                SharePreManager.getInstance(context).setLiveRatio(it, Ratio.P720)
            }
        }
    }

    open fun startPlayInternal() {
        activityContext.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        resetRatioInfoForG0()
        liveStartTime = SystemClock.elapsedRealtime()
        playTimeRecordSpan = 0
        getCurrentErrorCodeForCountly(currentState, currentOpt)
        playBeginTimeRecord = SystemClock.elapsedRealtime()
        downloadStringBuilder.clear()

        AddxAudioSet.setMisicDisable()
        if(iAddxPlayer == null){
            iAddxPlayer = AddxPlayerManager.getInstance().getPlayer(dataSourceBean)
        }
        LogUtils.w(
            TAG,
            "AddxBaseVideoView---------------startPlayInternal------iAddxPlayer is null:${iAddxPlayer == null}-----sn:${dataSourceBean?.serialNumber}"
        )
        iAddxPlayer?.setDataSource(dataSourceBean)
        iAddxPlayer?.setDisplay(renderView)
        if(renderView is CustomSurfaceViewRenderer){
            (renderView as CustomSurfaceViewRenderer).setDisableCropFrame(mIsFullScreen)
        }
        iAddxPlayer?.setListener(this)
        iAddxPlayer?.startLive()
    }

    override fun stopPlay() {
        LogUtils.d(TAG, "AddxBaseVideoView------stopPlay------sn:${dataSourceBean!!.serialNumber}")
        AddxAudioSet.restoreMisic()
//        iAddxPlayer?.playerStatInfo?.misretryconnect = false
        removeCallbacks(mFadeOut)
        mShowing = false
        // 由于截图需要时间，我们需要先操作UI暂停，然后再截图完成后再操作player暂停
        if(currentState == CURRENT_STATE_PLAYING){
            iAddxPlayer?.saveShot { frame ->
                if (frame != null) {
                    mLocalThumbTime = TimeUtils.getUTCTime()
                    VideoSharePreManager.getInstance(A4xContext.getInstance().getmContext()).setThumbImgLocalLastFresh(
                        dataSourceBean!!.serialNumber,
                        TimeUtils.getUTCTime()
                    )
                    LocalDrawableUtills.instance.putLocalCoverBitmap(
                        dataSourceBean!!.serialNumber.plus(
                            thumbSaveKeySuffix
                        ), frame
                    )
                }
            }
        }
        iAddxPlayer?.stop()
        startBtn?.visibility = View.VISIBLE
        removeCallbacks(startBtnAction)
        stopRecordVideo("stopPlay")
    }

//    override fun setDeviceBean(deviceBean: DeviceBean?) {
//        if (deviceBean == null) {
//            throw NullPointerException("$TAG---setDeviceBean----sn is null")
//        }
//        LogUtils.w(TAG, "setDeviceBean-------" + deviceBean.serialNumber)
//        this.dataSourceBean = deviceBean
//        iAddxPlayer = AddxPlayerManager.getInstance().getPlayer(AddxContext.getInstance().getmContext(), deviceBean)
//        iAddxPlayer?.setDataSource(deviceBean)
//    }

    fun setDeviceState(isAutoPlay: Boolean, isTimeout: Boolean) {
        LogUtils.d(
            TAG,
            "AddxBaseVideoView------setDeviceState init server device state---:${dataSourceBean!!.needForceOta()}---needOta:${dataSourceBean!!.needOta()}-----sn:${dataSourceBean!!.serialNumber}"
        )
        //检查设备
        when {
            dataSourceBean!!.isFirmwareUpdateing -> updateStateAndUI(
                CURRENT_STATE_ERROR,
                PlayerErrorState.ERROR_DEVICE_IS_OTA_ING
            )
            dataSourceBean!!.needOta() -> {//需要OTA
                if(isTimeout){
                    updateStateAndUI(CURRENT_STATE_ERROR, PlayerErrorState.ERROR_PLAYER_TIMEOUT)
                }else{
                    updateStateAndUI(CURRENT_STATE_ERROR, PlayerErrorState.ERROR_DEVICE_NEED_OTA)
                }
            }
            dataSourceBean!!.needForceOta() -> {//需要强制OTA
                updateStateAndUI(CURRENT_STATE_ERROR, PlayerErrorState.ERROR_DEVICE_NEED_OTA)
            }
            dataSourceBean!!.isShutDownLowPower -> updateStateAndUI(
                CURRENT_STATE_ERROR,
                PlayerErrorState.ERROR_DEVICE_SHUTDOWN_LOW_POWER
            )
            dataSourceBean!!.isShutDownPressKey -> updateStateAndUI(
                CURRENT_STATE_ERROR,
                PlayerErrorState.ERROR_DEVICE_SHUTDOWN_PRESS_KEY
            )
            dataSourceBean!!.isDeviceOffline -> updateStateAndUI(
                CURRENT_STATE_ERROR,
                PlayerErrorState.ERROR_DEVICE_OFFLINE
            )
            dataSourceBean!!.isDeviceSleep -> updateStateAndUI(
                CURRENT_STATE_ERROR,
                PlayerErrorState.ERROR_DEVICE_SLEEP
            )
            isTimeout -> updateStateAndUI(
                CURRENT_STATE_ERROR,
                PlayerErrorState.ERROR_PLAYER_TIMEOUT
            )
            else -> {
                updateStateAndUI(CURRENT_STATE_NORMAL, currentOpt)
                if(isAutoPlay){
                    startPlay()
                }
            }
        }
    }

//    override fun setUiSplit(split: Boolean) {
//
//    }

    override fun setPlayPosition(position: Long) {
        iAddxPlayer?.SeekTo(position)
    }

    override fun onWebRTCCommandSend(command: DataChannelCommand?) {

    }

    fun saveMuteState(mute: Boolean) {
        val localDevice = DeviceManager.getInstance().get(dataSourceBean!!.serialNumber)
        if (localDevice != null) {
            localDevice.isNeedMute = mute
            DeviceManager.getInstance().update(localDevice)
        }
    }

    fun setMuteState(mute: Boolean) {
        this.mute = mute
        iAddxPlayer?.muteVoice(mute, true)
        //todo
//        reportEnableMicEvent(mute)
        saveMuteState(mute)
        updateSoundIcon(mute)
    }

    fun setMuteState() {
        setMuteState(!mute)
    }
    //
    protected abstract fun fullLayoutId(): Int

    protected abstract fun normalLayoutId(): Int
    override fun onClick(v: View?) {
        LogUtils.d(TAG, "onClick-------" + dataSourceBean!!.serialNumber)
        if (mVideoCallBack != null) {
            if (onViewClick(v)) return
        }

        LogUtils.w(TAG, "onClick----mVideoCallBack == null---")

        when (v?.id) {
            R.id.start -> {
                LogUtils.d(
                    TAG,
                    "AddxBaseVideoView------onClick------start------sn:${dataSourceBean!!.serialNumber}"
                )
                if (isPlaying()) {
                    mStopType = "endButton"
                    stopPlay()
                } else {
                    createRecordClick("start_btn_clickid_")
                    showNetWorkToast()
                    startPlay()
                    //todo
//                    reportLiveClickEvent("normal")
                }
            }
            R.id.back -> {
                LogUtils.d(
                    TAG,
                    "AddxBaseVideoView------onClick------back---sn:" + dataSourceBean!!.serialNumber
                )
                backToNormal()
            }
            R.id.fullscreen -> {
                createRecordClick("fullscreen_btn_clickid_")
                LogUtils.d(
                    TAG,
                    "AddxBaseVideoView------onClick------fullscreen---sn:" + dataSourceBean!!.serialNumber
                )
                if (currentState != CURRENT_STATE_ERROR) {
                    startFullScreen()
                }
            }
            R.id.iv_sound -> {
                LogUtils.d(
                    TAG,
                    "AddxBaseVideoView------onClick------iv_sound---sn:" + dataSourceBean!!.serialNumber
                )
                setMuteState(!mute)
            }
            R.id.iv_record -> {
                LogUtils.d(
                    TAG,
                    "AddxBaseVideoView------onClick------currentState $currentState isRecording $isRecording----iv_record---sn:" + dataSourceBean!!.serialNumber
                )
                if (currentState == CURRENT_STATE_PLAYING) {
                    if (!isRecording) {
                        availableSdcardSize = SDCardUtils.getAvailableSdcardSize(SizeUnit.MB)
                        val sdfVideo = SimpleDateFormat("yyyyMMddhhmmss", Locale.getDefault())
                        videoSavePath =
                            DirManager.getInstance().getRecordVideoPath() + sdfVideo.format(
                                Date()
                            ) + ".mp4"
                        //startRecord(videoSavePath)
                        iAddxPlayer!!.startRecording(
                            videoSavePath!!,
                            object : MP4VideoFileRenderer.VideoRecordCallback {
                                override fun completed() {
                                    LogUtils.e(
                                        TAG,
                                        "AddxBaseVideoView------startRecording-------------- completed---sn:${dataSourceBean!!.serialNumber}"
                                    )
                                }

                                override fun error() {
                                    LogUtils.e(
                                        TAG,
                                        "AddxBaseVideoView------startRecording-------------- error---sn:${dataSourceBean!!.serialNumber}"
                                    )
                                }
                            })
                        startRecordCountTask()
                        recordIcon?.setImageResource(R.mipmap.video_recording)
                        recordTimeText?.visibility = View.VISIBLE
                        isRecording = true
                        hide()
                    } else {
                        stopRecordVideo("stop by user")
                        savingRecordLoading?.visibility = View.VISIBLE

                        iAddxPlayer?.saveShot { frame ->
                            if (frame != null) {
                                renderView?.post {
                                    startBitmapAnim(
                                        frame,
                                        resources.getDimension(R.dimen.dp_180).toInt()
                                    )
                                }
                            }
                        }
                    }

                } else {
                    ToastUtils.showShort(R.string.live_not_start)
                }
            }
            R.id.iv_screen_shot -> {
                LogUtils.d(
                    TAG,
                    "AddxBaseVideoView------onClick------iv_screen_shot---sn:" + dataSourceBean!!.serialNumber
                )
                if (!isRecording && recordIcon?.isEnabled!!) {
                    iAddxPlayer?.saveShot { frame ->
                        if (frame != null) {
                            try {
                                renderView?.post {
                                    startBitmapAnim(
                                        frame,
                                        resources.getDimension(R.dimen.dp_100).toInt()
                                    )
                                }
                                val sdf = SimpleDateFormat("yyyyMMddhhmmss", Locale.getDefault())
                                val savePath =
                                    PathUtils.getExternalDcimPath() + File.separator + A4xContext.getInstance()
                                        .getmTenantId() + sdf.format(
                                        Date()
                                    ) + ".png"
                                BitmapUtils.saveBitmap(frame, savePath)
                                FileUtils.syncImageToAlbum(context, savePath, Date().time)
                                if (mShowRecordShotToast) {
                                    ToastUtils.showShort(R.string.image_save_to_ablum)
                                }
                                //todo
//                                reportScreenShotEvent(true, null)
                            } catch (e: FileNotFoundException) {
                                e.printStackTrace();
                                ToastUtils.showShort(R.string.shot_fail)
                                //todo
//                                reportScreenShotEvent(false, "shot frame=null")
                            }
                        } else {
                            ToastUtils.showShort(R.string.shot_fail)
                        }
                    }
                } else {
                    ToastUtils.showShort(R.string.cannot_take_screenshot)
                    //todo
//                    reportScreenShotEvent(false, "recording can not shot")
                }
            }
        }
    }

    fun createRecordClick(clickType: String){
        mClickId = clickType + System.currentTimeMillis()
        mIsUserClick = true
    }

    fun showNetWorkToast(){
        if (NetworkUtil.isMobileData(A4xContext.getInstance().getmContext()) && !NetworkUtils.isWifiConnected(
                A4xContext.getInstance().getmContext()
            ) && SharePreManager.getInstance(context).showNetworkDialog()) {
            //showNetWorkDialog()
            ToastUtils.showShort(R.string.pay_attention_data)
            SharePreManager.getInstance(context).setShowNetworkDialog(false)
        }
    }

    override fun getScreenShotPicture(callBack: IVideoPlayer.ScreenSpotCallBack) {
        iAddxPlayer?.saveShot(callBack)
    }

    open fun reloadErrorLayout() {
        errorLayout = mAddxVideoContentView.findViewById(R.id.layout_error)
        errorLayout?.removeAllViews()
        errorLayout?.addView(getErrorView())
//        errorLayout?.setOnClickListener {
//            LogUtils.d(TAG, "AddxBaseVideoView------errorLayout?.setOnClickListener-------" + dataSourceBean!!.serialNumber)
//            if(PlayerErrorState.ERROR_DEVICE_IS_OTA_ING != currentOpt){
//                onClickUnderlineErrorButton(null)
//            }
//        }
    }

    open fun setFullScreenRatio(layoutParams: ViewGroup.LayoutParams) {
        if (!mIsFullScreen) {
            return
        }
        val screenHeight = CommonUtil.getScreenHeight(context) + CommonUtil.getStatusBarHeight(
            context
        )
        val screenWidth = CommonUtil.getScreenWidth(context)
        val height = screenWidth.coerceAtMost(screenHeight)
        val width = screenWidth.coerceAtLeast(screenHeight)
        if (renderView != null) {
            if (disableCropFrame) {
                renderView?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
                renderView?.layoutParams?.width = ViewGroup.LayoutParams.MATCH_PARENT
            } else {
                layoutParams.width = width * Const.Screen.RATIO.toInt()
                layoutParams.height = height
            }
        }
        LogUtils.w(
            TAG,
            "AddxBaseVideoView------setFullscreenRatio ,${renderView?.layoutParams?.width} : ${renderView?.layoutParams?.height} ---sn:${dataSourceBean!!.serialNumber}"
        )
    }

    val fullLayoutViewGroup by lazy { View.inflate(context, fullLayoutId(), null) }
    val normalLayoutViewGroup by lazy { View.inflate(context, normalLayoutId(), null) }

    @SuppressLint("ClickableViewAccessibility")
    private fun reloadLayout(context: Context?) {
        removeAllViews()
        LogUtils.w("reloadLayout", mIsFullScreen)
        mAddxVideoContentView = if (mIsFullScreen) fullLayoutViewGroup else normalLayoutViewGroup
        addView(mAddxVideoContentView)
        reloadErrorLayout()
        thumbImage = findViewById(R.id.thumbImage)
        normalLayout = findViewById(R.id.normal_layout)
        loadingLayout = mAddxVideoContentView.findViewById(R.id.loading)
        fullScreenBtn = mAddxVideoContentView.findViewById(R.id.fullscreen)
        fullScreenBtn?.setOnClickListener(this)
        soundBtn = mAddxVideoContentView.findViewById(R.id.iv_sound)
        soundBtn?.setOnClickListener(this)
        startBtn = mAddxVideoContentView.findViewById(R.id.start)
        startBtn?.setOnClickListener(this)
        tvDownloadSpeed = findViewById(R.id.tv_download_speed)
        renderContainer = mAddxVideoContentView.findViewById(R.id.surface_container)
        animShotView = mAddxVideoContentView.findViewById(R.id.screen_shot_anim)
        recordIcon = mAddxVideoContentView.findViewById(R.id.iv_record)
        recordTimeText = mAddxVideoContentView.findViewById(R.id.tv_record_time)
//        savingRecordLoading = contentView.findViewById(R.id.is_saving_record)
        if (mIsFullScreen) {
            renderView?.setZOrderOnTop(true)
            renderView?.setZOrderMediaOverlay(true)
        }else{
            renderView?.setZOrderOnTop(false)
            renderView?.setZOrderMediaOverlay(false)
        }
        if (renderView == null) {
            renderView = if (iAddxPlayer is AddxVideoIjkPlayer) {
                AddxGLSurfaceView(A4xContext.getInstance().getmContext())
            } else {
                CustomSurfaceViewRenderer(A4xContext.getInstance().getmContext())
            }
            renderView?.id = View.generateViewId()
            LogUtils.w(
                TAG,
                "AddxBaseVideoView------onAddRenderView,${Integer.toHexString(renderView.hashCode())}---sn:${dataSourceBean!!.serialNumber}"
            )
            renderContainer?.addView(
                renderView, LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT
                )
            )
            renderView?.setOnTouchListener(this)
        } else {
            LogUtils.w(
                TAG,
                "AddxBaseVideoView------onAddRenderView,${Integer.toHexString(renderView.hashCode())}---sn:${dataSourceBean!!.serialNumber}"
            )
            renderContainer?.addView(
                renderView, 0, LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT
                )
            )
            setFullScreenRatio(renderView?.layoutParams!!)
        }

        onInitOrReloadUi(context)
        updateStateAndUI(currentState, currentOpt)
        updateSoundIcon(mute)
        setThumbImageByPlayState(true)
        if(mIsGetNewThumbTime <= System.currentTimeMillis() - 5 * 60 * 1000){
            mIsGetNewThumbTime = System.currentTimeMillis()
            refreshThumbImg()
        }
    }

    internal open fun onInitOrReloadUi(context: Context?) {}


    open fun backToNormal() {
        LogUtils.w(TAG, "AddxBaseVideoView------backToNormal---sn:${dataSourceBean!!.serialNumber}")
        isChanggingRatio = false
        CommonUtil.showNavKey(activityContext, mSystemUiVisibility)
        CommonUtil.showSupportActionBar(activityContext, true, true)
        CommonUtil.scanForActivity(activityContext).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        //remove self (player) from root
        val contentView = activityContext.findViewById(android.R.id.content) as ViewGroup
        contentView.removeView(this)
        //remove renderView from full layout container
        renderContainer?.removeView(renderView)

        for (index in 0 until contentView.childCount) {
            contentView.getChildAt(index).visibility = View.VISIBLE
        }
        mIsFullScreen = false
        //init normal layout  and add renderView to container
        reloadLayout(context)

        //add self to old parent
        if(parent != mAddxVideoViewParent){
            mAddxVideoViewParent.addView(this, oldLayoutParams)
        }

        if (mShowing) {
            removeCallbacks(mFadeOut)
            mShowing = false
            show(DEFAULT_SHOW_TIME)
        }
        stopRecordVideo("back to normal ")
        if(renderView is CustomSurfaceViewRenderer){
            (renderView as CustomSurfaceViewRenderer).setDisableCropFrame(mIsFullScreen)
        }
        onFullScreenStateChange(false)
    }

    open fun startFullScreen() {
        LogUtils.w(
            TAG,
            "AddxBaseVideoView------startFullScreen---sn:${dataSourceBean!!.serialNumber}"
        )
        mIsFullScreen = true
        mSystemUiVisibility = activityContext.window.decorView.systemUiVisibility
        oldLayoutParams = layoutParams
        mAddxVideoViewParent = parent as ViewGroup
        CommonUtil.hideSupportActionBar(activityContext, true, true)
        hideNavKey()
        CommonUtil.scanForActivity(activityContext).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        //remove self(Player) from parent
        mAddxVideoViewParent.removeView(this)
        // remove renderView from it's current parent  and  prepare add it to full screen layout 's surface container
        renderContainer?.removeView(renderView)
        // init full layout and add it to surfaceContainer
        reloadLayout(context)
        //设置reload 后的UI状态
        val params = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        val contentView = activityContext.findViewById(android.R.id.content) as ViewGroup
        for (index in 0 until contentView.childCount) {
            contentView.getChildAt(index).visibility = View.INVISIBLE
        }
        // now add player to root
        contentView.addView(this, params)
        if (mShowing) {
            removeCallbacks(mFadeOut)
            mShowing = false
            show(DEFAULT_SHOW_TIME)
        }
        autoPlayIfNeed()
        if (isSavingRecording) {
            savingRecordLoading?.visibility = View.VISIBLE
        }
        onFullScreenStateChange(true)
        if(renderView is CustomSurfaceViewRenderer){
            (renderView as CustomSurfaceViewRenderer).setDisableCropFrame(mIsFullScreen)
        }
        onResetRecordUi()
    }

    private fun autoPlayIfNeed() {
        if (!(isPlaying() || isPrepareing())) {
            startBtn?.callOnClick()
            LogUtils.e(
                TAG,
                "AddxBaseVideoView------auto play ,before state=$currentState---sn:${dataSourceBean!!.serialNumber}"
            )
        }
    }

    protected open fun errorLayoutId(): Int {
        return if (mIsFullScreen) {
            R.layout.live_plager_full_error_page
        } else R.layout.live_plager_no_full_error_default_page
    }

    fun onClickErrorRetry() {
        LogUtils.d(
            TAG,
            "AddxBaseVideoView------errorLayout?.setOnClickListener----------sn:${dataSourceBean!!.serialNumber}"
        )
        if (!AccountManager.getInstance().isLogin) return
        when (currentOpt) {
            PlayerErrorState.ERROR_DEVICE_NEED_OTA -> {
                AddxFunJump.toUpdate(activityContext, dataSourceBean!!)
            }
            PlayerErrorState.ERROR_DEVICE_SLEEP -> {
                if (dataSourceBean != null && dataSourceBean!!.isAdmin) {
//                    RxBus.getDefault().post(dataSourceBean?.serialNumber, Const.Rxbus.EVENT_WAKE_UP_SLEEP_DEVICE)
                    wakeDevice(dataSourceBean!!.serialNumber)
                }
            }

            PlayerErrorState.ERROR_DEVICE_NO_ACCESS -> {
                onViewClick(tvUnderLineErrorBtn)
            }
            else -> {
                createRecordClick("retry_btn_clickid_")
                showNetWorkToast()
                if (resources.getString(R.string.reconnect) == tvUnderLineErrorBtn?.text) {
                    //todo
//                    reportLiveReconnectClickEvent()
                } else {
                    //todo
//                    reportLiveClickEvent(PlayerErrorState.getErrorMsg(currentOpt))
                }
                startPlay()
            }
        }
    }

    fun wakeDevice(sn: String) {
        val loadingDialog = LoadingDialog(context)
        loadingDialog.show()
        ApiClient.getInstance().sleepSwitchSetting(SleepPlanData(sn, 0)).flatMap {
            if (it.result == Const.ResponseCode.CODE_OK) {
                return@flatMap ApiClient.getInstance().getSingleDevice(SerialNoEntry(sn))
            } else {
                return@flatMap rx.Observable.error<GetSingleDeviceResponse>(Throwable("sleepSwitchSetting wakeup device failed code=" + it.result))
            }
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : HttpSubscriber<GetSingleDeviceResponse>() {
                override fun doOnNext(baseResponse: GetSingleDeviceResponse) {
                    loadingDialog.dismiss()
                    if (baseResponse.result < Const.ResponseCode.CODE_OK) {
                        ToastUtils.showShort(R.string.open_fail_retry)
                    } else {
                        ToastUtils.showShort(R.string.setup_success)
                        dataSourceBean?.copy(baseResponse.data)
                        setDeviceState(false, false)
                    }
                }

                override fun onError(e: Throwable) {
                    e.printStackTrace()
                    ToastUtils.showShort(R.string.open_fail_retry)
                    loadingDialog.dismiss()
                }
            })
    }

    open fun getErrorView(): View {
        val inflate = View.inflate(context, errorLayoutId(), null)
//        mUploadlog = inflate.findViewById(R.id.uploadlog)
//        mUploadlog?.visibility = if (mIsNeedUploadFailLog) View.VISIBLE else View.GONE
        tvErrorTips = inflate.findViewById(R.id.tv_error_tips)
        ivErrorExit = inflate.findViewById(R.id.iv_error_exit)
        ivErrorSetting = inflate.findViewById(R.id.iv_error_setting)
        ivErrorFlag = inflate.findViewById(R.id.iv_error_flag)
        tvErrorButton = inflate.findViewById(R.id.tv_error_btn)
        tvUnderLineErrorBtn = inflate.findViewById(R.id.tv_underline_error_btn)
        ivErrorHelp = inflate.findViewById(R.id.iv_error_help)
        ivErrorThumb = inflate.findViewById(R.id.iv_error_thumb)
        ivErrorExit?.setOnClickListener { backToNormal() }
        tvErrorButton?.setOnClickListener { onClickErrorRetry() }
        tvErrorTips?.setOnClickListener { onClickErrorTips(tvErrorTips) }
        tvUnderLineErrorBtn?.setOnClickListener {  onClickUnderlineErrorButton(tvUnderLineErrorBtn) }
//        mUploadlog?.setOnClickListener({uploadErrorLog()})
        return inflate
    }

    fun uploadErrorLog() {
        CommonCornerDialog(activityContext)
            .setTitleText(R.string.send_log)
            .setMessage(R.string.send_log_tips)
            .setLeftText(R.string.cancel)
            .setRightText(R.string.ok)
            .setLeftClickListener(OnClickListener {
            })
            .setRightClickListener(OnClickListener {
                AddxMonitor.getInstance(A4xContext.getInstance().getmContext()).uploadLastDayLog(
                    object : FileLogUpload.Callback {
                        override fun invoke(fileName: String?, ret: Boolean) {
                            if (ret) {
                                ToastUtils.showShort(R.string.uploaded_success)
                            } else {
                                ToastUtils.showShort(R.string.uploaded_fail)
                            }
                        }
                    })
            }).show()
    }

    open fun updateSoundIcon(mute: Boolean) {
        LogUtils.w(
            TAG,
            "AddxBaseVideoView------updateSoundIcon----" + (soundBtn == null).toString() + "--" + mute.toString() + "---sn:${dataSourceBean!!.serialNumber}"
        )
        if(mIsFullScreen) {
            soundBtn?.setImageResource(if (mute) R.mipmap.live_last_sound_disable else R.mipmap.live_last_sound_enable)
        }else{
            soundBtn?.setImageResource(if (mute) R.mipmap.voice_black_notalk else R.mipmap.voice_black_talk)
        }
    }

    open fun updateStateAndUI(state: Int, opt: Int) {
        if (!checkPassState(state, opt)) {
            LogUtils.d(
                TAG,
                "AddxBaseVideoView------checkPassState false , state = $state  opt = $opt---sn:${dataSourceBean!!.serialNumber}"
            )
            return
        }
        mOldState = currentState
        mOldOpt = currentOpt
        currentOpt = opt
        currentState = state
        LogUtils.d(
            TAG,
            "AddxBaseVideoView------oldState=$mOldState  currentState=$state oldOpt===$mOldOpt currentOpt===$opt---sn:${dataSourceBean!!.serialNumber}"
        )
        if (Looper.myLooper() != Looper.getMainLooper()) {
            post {
                updateStateAndUI(mOldState, mOldOpt, state, opt)
            }
        } else {
            updateStateAndUI(mOldState, mOldOpt, state, opt)
        }
    }

    private fun checkPassState(state: Int, opt: Int): Boolean {
        if (state == CURRENT_STATE_ERROR && opt != PlayerErrorState.ERROR_PHONE_NO_INTERNET && !NetworkUtils.isConnected(
                A4xContext.getInstance().getmContext()
            )) {//check
            updateStateAndUI(state, PlayerErrorState.ERROR_PHONE_NO_INTERNET)
            return false
        }
        return true
    }

    private fun updateStateAndUI(tempState: Int, tempOpt: Int, state: Int, opt: Int){
        callBackViewState(state, tempState)
        updatePropertyBeforeUiStateChanged(tempState, state, tempOpt, opt)
        hideNavKey()
        updateUiState(tempState, state, tempOpt, opt)

        LogUtils.d(
            TAG,
            "AddxBaseVideoView------onStateAndUiUpdated oldState $tempState  newState : $state---sn:${dataSourceBean!!.serialNumber}"
        )
        //todo
//        reportLiveState(tempState, state)
        if (currentState != CURRENT_STATE_PLAYING && tempState != currentState) {
            setThumbImageByPlayState()
        }
    }

    private fun updateUiState(oldUiState: Int, newUiState: Int, oldOption: Int, newOption: Int) {
        when (newUiState) {
            CURRENT_STATE_NORMAL -> {
                changeUIToIdle()
            }
            CURRENT_STATE_PREPAREING -> {
                changeUIToConnecting()
            }
            CURRENT_STATE_PLAYING -> {
                changeUIToPlaying()
            }
            CURRENT_STATE_PAUSE, CURRENT_STATE_AUTO_COMPLETE -> {
                if (currentOpt == 0) {
                    changeUIToIdle()
                } else {
                    changeUIToError(currentOpt)
                }
            }
            CURRENT_STATE_ERROR -> {
                changeUIToError(newOption)
            }
        }
    }

    private fun updatePropertyBeforeUiStateChanged(
        oldUiState: Int,
        newUiState: Int,
        oldOption: Int,
        newOption: Int
    ) {
        //do noting
    }

    var errorCodeWhenUserClickForCountly = 0 // 仅仅离线和被动的错误算是errorCode
    private var retryErrorCodeOnClickStartByUserForCountly = 0 //从errorCode中筛选出来，属于errorRetry的code
    private fun getCurrentErrorCodeForCountly(state: Int, opt: Int) {
        if (state == CURRENT_STATE_ERROR) {
            errorCodeWhenUserClickForCountly = opt
            when (opt) {
                PlayerErrorState.ERROR_DEVICE_NO_ACCESS,
                PlayerErrorState.ERROR_DEVICE_SHUTDOWN_LOW_POWER,
                PlayerErrorState.ERROR_DEVICE_SHUTDOWN_PRESS_KEY,
                PlayerErrorState.ERROR_DEVICE_OFFLINE,
                PlayerErrorState.ERROR_DEVICE_UNACTIVATED,
                PlayerErrorState.ERROR_DEVICE_AUTH_LIMITATION,
                PlayerErrorState.ERROR_DEVICE_MAX_CONNECT_LIMIT,
                PlayerErrorState.ERROR_PHONE_NO_INTERNET,
                PlayerErrorState.ERROR_UDP_AWAKE_FAILED,
                PlayerErrorState.ERROR_PLAYER_TIMEOUT,
                PlayerErrorState.ERROR_CONNECT_TIMEOUT,
                PlayerErrorState.ERROR_CONNECT_EXCEPTION -> {
                    retryErrorCodeOnClickStartByUserForCountly = opt
                }
                else -> {
                    retryErrorCodeOnClickStartByUserForCountly = 0
                }
            }
        } else {
            errorCodeWhenUserClickForCountly = 0
            retryErrorCodeOnClickStartByUserForCountly = 0
        }
    }

    private fun callBackViewState(currentState: Int, oldState: Int) {
        onPlayStateChanged(currentState, oldState)
        when (currentState) {
            CURRENT_STATE_PREPAREING -> {
            }
            CURRENT_STATE_PLAYING -> {
            }
            CURRENT_STATE_ERROR, CURRENT_STATE_AUTO_COMPLETE, CURRENT_STATE_PAUSE, CURRENT_STATE_NORMAL -> {
                mVideoCallBack?.onStopPlay()
            }
        }
    }

    protected open fun changeUIToPlaying() {
        LogUtils.w(
            TAG,
            dataSourceBean?.serialNumber + "-----changeUIToPlaying, mIsFullScreen= $mIsFullScreen"
        )
        thumbImage?.visibility = View.INVISIBLE
        loadingLayout?.visibility = View.INVISIBLE
        errorLayout?.visibility = View.INVISIBLE
        if((mIsFullScreen && currentState == CURRENT_STATE_NORMAL) || !mIsFullScreen) {
            startBtn?.visibility = View.VISIBLE
        }else{
            startBtn?.visibility = View.INVISIBLE
        }
        removeCallbacks(startBtnAction)
        postDelayed(startBtnAction, START_SHOW_SPAN)
    }

    protected open fun changeUIToConnecting() {
        LogUtils.w(
            TAG,
            dataSourceBean?.serialNumber + "-----changeUIToConnecting, mIsFullScreen= $mIsFullScreen"
        )
        startBtn?.visibility = View.INVISIBLE
        errorLayout?.visibility = View.INVISIBLE
        loadingLayout?.visibility = View.VISIBLE
        thumbImage?.visibility = View.VISIBLE
    }

    protected open fun changeUIToIdle() {
        LogUtils.w(
            TAG,
            dataSourceBean?.serialNumber + "-----changeUIToIdle, mIsFullScreen= $mIsFullScreen"
        )
        thumbImage?.visibility = View.VISIBLE
        errorLayout?.visibility = View.INVISIBLE
        loadingLayout?.visibility = View.INVISIBLE
        if((mIsFullScreen && currentState == CURRENT_STATE_NORMAL) || !mIsFullScreen) {
            startBtn?.visibility = View.VISIBLE
            removeCallbacks(startBtnAction)
        }
    }

    protected open fun changeUIToError(opt: Int?) {
        when (opt) {
            PlayerErrorState.ERROR_DEVICE_UNACTIVATED -> setErrorInfo(
                R.string.camera_not_activated,
                R.mipmap.live_error_unactivated
            )
            PlayerErrorState.ERROR_DEVICE_SLEEP -> {
                setErrorInfo(
                    R.string.camera_sleep,
                    R.mipmap.ic_sleep_main_live,
                    true,
                    R.string.camera_wake_up,
                    dataSourceBean?.isAdmin
                        ?: false,
                    null,
                    false
                )

                dataSourceBean?.sleepMsg?.let {
                    if (it.isNotEmpty() && dataSourceBean?.isAdmin!!) {
                        tvErrorTips?.text = it
                    } else {
                        tvErrorTips?.text =
                            it.plus("\n\n" + context.resources.getString(R.string.admin_wakeup_camera))
                    }
                }
            }
            PlayerErrorState.ERROR_DEVICE_NO_ACCESS -> setErrorInfo(
                R.string.error_2002,
                R.mipmap.live_error__no_access,
                underlineErrorBtnText = R.string.refresh
            )
            PlayerErrorState.ERROR_DEVICE_SHUTDOWN_LOW_POWER -> {
                setErrorInfo(R.string.low_power, R.mipmap.lowpowershutdown)
            }
            PlayerErrorState.ERROR_DEVICE_SHUTDOWN_PRESS_KEY -> {
                setErrorInfo(R.string.turned_off, R.mipmap.shutdown)
            }
            PlayerErrorState.ERROR_DEVICE_OFFLINE -> {
                setErrorInfo(R.string.camera_poor_network, R.mipmap.live_offline)
            }
            PlayerErrorState.ERROR_DEVICE_NEED_OTA -> {
                if (!AccountManager.getInstance().isLogin) {
                    return
                }
                val needForceOta = dataSourceBean?.needForceOta()
                if (dataSourceBean?.adminId == AccountManager.getInstance().userId) {
                    if (!needForceOta!!) {
                        setErrorInfo(
                            R.string.fireware_need_update_tips,
                            R.mipmap.ic_video_device_upgrade,
                            errorBtnText = R.string.update,
                            errorBtnVisible = true,
                            underlineErrorBtnText = R.string.do_not_update,
                            underLineErrorBtnColor = R.color.theme_color
                        )
                    } else {
                        setErrorInfo(
                            R.string.fireware_need_update_tips,
                            R.mipmap.ic_video_device_upgrade,
                            errorBtnText = R.string.update,
                            errorBtnVisible = true,
                            underlineErrorBtnVisible = false
                        )
                    }
                } else {
                    if (!needForceOta!!) {
                        setErrorInfo(
                            R.string.forck_update_share,
                            R.mipmap.ic_video_device_upgrade,
                            errorBtnText = R.string.update,
                            errorBtnVisible = false,
                            underlineErrorBtnText = R.string.do_not_update,
                            underLineErrorBtnColor = R.color.theme_color
                        )
                    } else {
                        setErrorInfo(
                            R.string.forck_update_share,
                            R.mipmap.ic_video_device_upgrade,
                            underlineErrorBtnVisible = false
                        )
                    }

                }
            }
            PlayerErrorState.ERROR_DEVICE_IS_OTA_ING -> setErrorInfo(
                R.string.device_is_updating,
                flagRes = R.mipmap.ic_video_device_upgrade,
                underlineErrorBtnVisible = false
            )


            PlayerErrorState.ERROR_PHONE_NO_INTERNET -> {
                setErrorInfo(R.string.failed_to_get_information_and_try)
            }

            PlayerErrorState.ERROR_PLAYER_TIMEOUT -> {
                setErrorInfo(R.string.live_stream_timeout, R.mipmap.live_timeout)
            }
            PlayerErrorState.ERROR_CONNECT_TIMEOUT -> {
                setErrorInfo(R.string.network_error_our_server, R.mipmap.live_timeout)
            }
            PlayerErrorState.ERROR_UNKNOWN,
            PlayerErrorState.ERROR_CONNECT_EXCEPTION -> {
                setErrorInfo(R.string.server_error, R.mipmap.live_exception)
            }

            PlayerErrorState.ERROR_DEVICE_MAX_CONNECT_LIMIT -> {
                LogUtils.w(
                    TAG,
                    "AddxBaseVideoView------AddxWebRtc------setErrorInfo-----------ERROR_DEVICE_MAX_CONNECT_LIMIT-------sn:${dataSourceBean!!.serialNumber}"
                )
                setErrorInfo(R.string.server_error, R.mipmap.live_exception)
//                changeUIToIdle()
                ToastUtils.showShort(R.string.live_viewers_limit)
            }

            else -> {
                RuntimeException("changeUIToError----未知错误---").printStackTrace()
                setDefaultErrorInfo()
            }
        }
        errorLayout?.visibility = View.VISIBLE
    }

    open fun setDefaultErrorInfo(){
        setErrorInfo(R.string.live_stream_error, R.mipmap.live_exception)
    }

    fun setErrorInfo(
        errorMsg: Int,
        flagRes: Int? = R.mipmap.ic_video_network_device_disconnect,
        flagVisible: Boolean? = true,
        errorBtnText: Int? = R.string.reconnect,
        errorBtnVisible: Boolean? = false,
        underlineErrorBtnText: Int? = R.string.reconnect,
        underlineErrorBtnVisible: Boolean? = true,
        underLineErrorBtnColor: Int? = R.color.theme_color
    ) {
        flagRes?.let { ivErrorFlag?.setImageResource(it) }
        flagVisible?.let { ivErrorFlag?.visibility = if (it) View.VISIBLE else View.GONE }
        if(errorMsg == R.string.fireware_need_update_tips){
            tvErrorTips?.text = activityContext.getString(
                errorMsg,
                dataSourceBean?.newestFirmwareId
            )
        }else if(errorMsg == R.string.forck_update_share){
            if(dataSourceBean?.needForceOta()!!){
                tvErrorTips?.text = activityContext.getString(
                    errorMsg,
                    dataSourceBean?.newestFirmwareId
                ).plus(
                    activityContext.getString(
                        R.string.unavailable_before_upgrade
                    )
                )
            }else{
                tvErrorTips?.text = activityContext.getString(
                    errorMsg,
                    dataSourceBean?.newestFirmwareId
                )
            }
        }else if(errorMsg == R.string.low_power || errorMsg == R.string.turned_off){
            LogUtils.d(
                TAG,
                "dataSourceBean?.getOfflineTime()-----errorMsg == R.string.low_power:${errorMsg == R.string.low_power}----getOfflineTime：${dataSourceBean?.getOfflineTime()}"
            )
            tvErrorTips?.setText(
                resources.getString(errorMsg) + "\n" + resources.getString(
                    R.string.off_time,
                    if (dataSourceBean?.getOfflineTime() == null) "" else TimeUtils.formatYearSecondFriendly(
                        dataSourceBean?.getOfflineTime()!!.toLong() * 1000
                    )
                )
            )
        }else{
            tvErrorTips?.setText(errorMsg)
        }
        errorBtnVisible?.let { tvErrorButton?.visibility = if (errorBtnVisible) View.VISIBLE else View.GONE }
        errorBtnText?.let { tvErrorButton?.setText(it) }

        underlineErrorBtnText?.let { tvUnderLineErrorBtn?.setText(it) }
        underlineErrorBtnVisible?.let { tvUnderLineErrorBtn?.visibility = if (it) View.VISIBLE else View.GONE }
        underLineErrorBtnColor?.let { tvUnderLineErrorBtn?.setTextColor(
            activityContext.resources.getColor(
                underLineErrorBtnColor
            )
        ) }
    }

    fun hideNavKey() {
        if (mIsFullScreen) {
            CommonUtil.hideNavKey(activityContext)
        }
    }

    open fun getThumbPath(sn: String): String {
        return DirManager.getInstance().getCoverPath(sn)
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        hideNavKey()
        when (v?.id) {
            renderView?.id -> {
                if (currentState != CURRENT_STATE_PLAYING) {
                    return false
                }
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (mShowing) {
                            hide()
                        } else {
                            mShowStartTimeSpan = System.currentTimeMillis()
                            show(DEFAULT_SHOW_TIME) // start timeout
                        }
                        return true
                    }
                }
            }
        }
        return false
    }
    protected open fun micTouch(v: View?, event: MotionEvent?): Int{
        return 0
    }
    protected open fun show(timeout: Long) {
        hideNavKey()
    }

    protected open fun hide() {
        hideNavKey()
    }

    override fun onMicFrame(data: ByteArray?) {

    }

    open fun onClickUnderlineErrorButton(tip: TextView?) {
        LogUtils.d(
            TAG,
            "AddxBaseVideoView------onClickUnderlineErrorButton----------sn:${dataSourceBean!!.serialNumber}"
        )
        when (currentOpt) {
            PlayerErrorState.ERROR_DEVICE_AUTH_LIMITATION,
            PlayerErrorState.ERROR_DEVICE_NO_ACCESS -> {
            }//checkDeviceExit(1)
            PlayerErrorState.ERROR_DEVICE_SLEEP -> {
                return
            }//sleep click do noting
            else -> {
                onViewClick(tip)
                createRecordClick("underline_retry_btn_clickid_")
//                mVideoCallBack?.onViewClick(tip)
                showNetWorkToast()
                startPlay()
            }
        }
        if(resources.getString(R.string.reconnect) == tvUnderLineErrorBtn?.text){
            //todo
//            reportLiveReconnectClickEvent()
        }else if(resources.getString(R.string.refresh) == tvUnderLineErrorBtn?.text){
            onViewClick(tip)
        }else{
            //todo
//            reportLiveClickEvent(PlayerErrorState.getErrorMsg(currentOpt))
        }
    }

    open fun onClickErrorTips(tip: TextView?) {
        LogUtils.d(
            TAG,
            "AddxBaseVideoView------onClickErrorTips base----------sn:${dataSourceBean!!.serialNumber}"
        )
        createRecordClick("clicktip_btn_clickid_")
    }

    fun checkDeviceExit() {
        val observable = ApiClient.getInstance().getSingleDevice(SerialNoEntry(dataSourceBean?.serialNumber))
        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : HttpSubscriber<GetSingleDeviceResponse>() {
                override fun doOnNext(t: GetSingleDeviceResponse) {
                    if (t.result == Const.ResponseCode.DEVICE_NO_ACCESS || t.result == Const.ResponseCode.DEVICE_1_NO_ACCESS || t.result == Const.ResponseCode.DEVICE_2_NO_ACCESS) {
                        currentOpt = PlayerErrorState.ERROR_DEVICE_NO_ACCESS
                        updateStateAndUI(CURRENT_STATE_ERROR, currentOpt)
                    } else if (t.result >= Const.ResponseCode.CODE_OK) {
                        dataSourceBean?.copy(t.data)
                        LogUtils.d(
                            TAG,
                            "AddxBaseVideoView------setDeviceState====doOnNext---sn:${dataSourceBean!!.serialNumber}"
                        )
                        setDeviceState(false, true)
                    }
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    LogUtils.d(
                        TAG,
                        "AddxBaseVideoView------setDeviceState====onError=---sn:${dataSourceBean!!.serialNumber}"
                    )
                    LogUtils.d(
                        TAG,
                        "AddxBaseVideoView------getSingleDevice====onError=---sn:${dataSourceBean!!.serialNumber}"
                    )
                    updateStateAndUI(CURRENT_STATE_ERROR, currentOpt)
                }
            })
    }

    override fun getNewLiveReportData(isSeccess: Boolean, en: ReporLiveCommonEntry?): ReporLiveCommonEntry? {
        var entry = en
        if(en == null){
            entry = ReporLiveCommonEntry()
        }
        LiveHelper.getNewLiveReportData(iAddxPlayer, dataSourceBean, isSeccess, entry!!)

        entry?.isClick = mIsUserClick
        entry?.clickid = mClickId
        entry?.live_player_type = if(mIsFullScreen) "full" else{ if(mIsSplit) "quad" else "half"}
//        entry?.wait_time = playTimeRecordSpan
        entry?.download_speeds = downloadStringBuilder.toString()
//        entry?.error_code = errorCodeWhenUserClickForCountly
        entry?.current_is_fullscreen = mIsFullScreen.toString()
        return entry
    }

    override fun getNewLiveInterruptWhenLoaddingReportData(): ReporLiveInterruptEntry {
        var data = ReporLiveInterruptEntry()
        data.endWay = mStopType
        getNewLiveReportData(false, data)
        return data
    }

    fun setThumbImageByPlayState(shouldSkipVisible: Boolean = false) {
        LogUtils.d(
            TAG,
            "AddxBaseVideoView------setThumbImageByPlayState ---sn:${dataSourceBean!!.serialNumber}"
        )
        if (dataSourceBean == null) {
            LogUtils.e(
                TAG,
                "AddxBaseVideoView------sn is null , do not set bg---sn:${dataSourceBean!!.serialNumber}"
            )
            return
        }

        val needBlur = when (currentState) {
            CURRENT_STATE_NORMAL,
            CURRENT_STATE_PAUSE,
            CURRENT_STATE_AUTO_COMPLETE -> false
            else -> true
        }
        thumbImage?.let {
            if (it.visibility == View.VISIBLE || shouldSkipVisible) {
                LogUtils.e(
                    TAG,
                    "AddxBaseVideoView------thumbImage visible = ${it.visibility == View.VISIBLE}---sn:${dataSourceBean!!.serialNumber}"
                )
                setThumbImageInternal(it, needBlur)
            }
        }

        ivErrorThumb?.let {
            if ((it.visibility == View.VISIBLE || shouldSkipVisible)) {
                if (currentState == CURRENT_STATE_ERROR) setThumbImageInternal(it, needBlur)
                else if (currentOpt < 0) setThumbImageInternal(it, true)
            }
        }
    }

    private fun setThumbImageInternal(view: ImageView, needBlur: Boolean) {
        dataSourceBean?.let {
            if (it.isDeviceSleep){
                view.setImageResource(R.drawable.live_sleep_bg)
                return
            }
        }
        LogUtils.d(
            TAG,
            "AddxBaseVideoView------setThumbImageInternal---sn:${dataSourceBean!!.serialNumber}"
        )
//        mLocalThumbTime = SharePreManager.getInstance(context).getThumbImgLastLocalFreshTime(dataSourceBean!!.serialNumber) / 1000
//        mServerThumbTime = SharePreManager.getInstance(context).getThumbImgLastServerFreshTime(dataSourceBean!!.serialNumber)
        var localCoverBitmap:Bitmap? = null
        if(mServerThumbTime > mLocalThumbTime){
            val imgPath = DownloadUtil.getThumbImgDir(activityContext) + MD5Util.md5(dataSourceBean?.serialNumber)+".jpg"
            localCoverBitmap = BitmapUtils.getBitmap(imgPath)
            LogUtils.d(
                TAG,
                "AddxBaseVideoView------toRequestAndRefreshThumbImg=======setThumbImageInternal======code:${hashCode()}======:${(localCoverBitmap == null)}==$imgPath---sn:${dataSourceBean!!.serialNumber}"
            )
        }
        if (localCoverBitmap == null) {
            localCoverBitmap = LocalDrawableUtills.instance.getLocalCoverBitmap(
                dataSourceBean!!.serialNumber.plus(
                    thumbSaveKeySuffix
                )
            )
            if (localCoverBitmap == null) {
                localCoverBitmap = BitmapFactory.decodeResource(resources, defaultThumbRid!!)
            }
        }
        mBitmap = if (needBlur) {
            BitmapUtils.rsBlur(context, localCoverBitmap, 15, 3)
        } else {
            localCoverBitmap
        }
        view.setImageBitmap(mBitmap)
    }

    override fun onWhiteLightResult(isOpen: Boolean) {}

    override fun onTriggerAlarm(isOpen: Boolean) {}

    override fun getPlayPosition(): Long {
        return iAddxPlayer?.currentPosition!!
    }

    override fun onSeekProcessing(player: IVideoPlayer?, playingTime: Long) {

    }

    override fun onDevInitiativeSendMsg(player: IVideoPlayer?, type: Int) {

    }
    /**
     * 开启录像任务
     */
    fun startRecordCountTask() {
        if (recordCounterTask != null) {
            recordCounterTask!!.unsubscribe()
        }
        recordCounterTask = rx.Observable.interval(0, 1, TimeUnit.SECONDS)
            .onBackpressureBuffer()
            .take(Int.MAX_VALUE)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : HttpSubscriber<Long?>() {
                override fun doOnNext(t: Long?) {
                    if (t != null) {
                        val minute = t / 60
                        val sec = t % 60
                        recordTimeText?.text = String.format("%02d:%02d", minute, sec)
                    }
                }
            })
    }


    protected fun stopRecordVideo(stopWay: String) {
        if (!isRecording || (recordIcon != null && !recordIcon?.isEnabled!!) || isSavingRecording) {
            LogUtils.d("stopRecordVideo------------没有正在录制或者保存还没有完成")
            return
        }
        recordIcon?.isEnabled = false
        isSavingRecording = true
        savingRecordLoading?.visibility = View.INVISIBLE
        iAddxPlayer?.stopRecording(object : MP4VideoFileRenderer.VideoRecordCallback {
            override fun error() {
                LogUtils.d("stopRecordVideo------------error")
                ToastUtils.showShort(R.string.record_failed)
                //todo
//                reportRecordEvent(false, "error", stopWay)
            }

            override fun completed() {
                LogUtils.d("stopRecordVideo------------completed")
                try {
                    isRecording = false
                    isSavingRecording = false
                    if (mShowRecordShotToast) {
                    }
                    ToastUtils.showShort(R.string.video_saved_to_ablum)
                    FileUtils.syncVideoToAlbum(
                        A4xContext.getInstance().getmContext(),
                        videoSavePath,
                        Date().time
                    )
                    //todo
//                    reportRecordEvent(true, null, stopWay)
                } catch (e: Exception) {
                    e.printStackTrace()
                    //todo
//                    reportRecordEvent(false, e.message, stopWay)
                }
                post {
                    recordIcon?.isEnabled = true
                    savingRecordLoading?.visibility = View.INVISIBLE
                    onResetRecordUi()
                }
            }
        })
        recordTimeText?.visibility = View.INVISIBLE
    }

    @SuppressLint("SetTextI18n")
    protected fun onResetRecordUi() {
        recordIcon?.setImageResource(R.mipmap.live_last_record)
        recordTimeText?.visibility = View.INVISIBLE
        if (recordCounterTask != null) {
            recordCounterTask!!.unsubscribe()
        }
        recordTimeText?.text = "00:00"
    }


    internal open fun startBitmapAnim(bitmap: Bitmap, toBottom: Int) {

    }

    fun savePlayState() {
        mSavePlayState = currentState
    }

    fun savePlayStatePlaying(): Boolean {
        return mSavePlayState == CURRENT_STATE_PLAYING
    }

    override fun getPlayState(): Int {
        return currentState
    }

    override fun onReveivedVideoFrame(frame: Bitmap?) {
//        post { setThumbImageByPlayState() }
        if (frame != null) {
            LocalDrawableUtills.instance.putLocalCoverBitmap(
                dataSourceBean!!.serialNumber + thumbSaveKeySuffix,
                frame
            )
        }
    }

//    fun releasePlayer() {
//        if (renderView is CustomSurfaceViewRenderer) {
//            LogUtils.e(
//                TAG,
//                "AddxBaseVideoView------releasePlayer----------iAddxPlayer is null: true---sn:${dataSourceBean!!.serialNumber}"
//            )
//            AddxPlayerManager.getInstance().releasePlayer(dataSourceBean)
//            iAddxPlayer = null
//        }
//    }
    internal open fun refreshThumbImg(){
    }

    open fun updateStateAndUIWhenNetChange(isConnected: Boolean) {
        if (isConnected) {
            updateStateAndUI(CURRENT_STATE_NORMAL, currentOpt)
        } else {
            updateStateAndUI(CURRENT_STATE_ERROR, PlayerErrorState.ERROR_PHONE_NO_INTERNET)
        }
    }
    open fun preApplyConnectWhenB(errorCode: Int){
        if(iAddxPlayer is AddxVideoWebRtcPlayer){
            if(dataSourceBean!!.isDeviceOffline || dataSourceBean!!.isDeviceSleep || dataSourceBean!!.isFirmwareUpdateing ||dataSourceBean!!.needForceOta()){
                return
            }
            iAddxPlayer?.setListener(this)
            (iAddxPlayer as AddxVideoWebRtcPlayer)?.preApplyConnectWhenB(errorCode)
        }
    }

    //原來viewcallback方法抽离
    internal open fun onPlayStateChanged(currentState: Int, oldState: Int){

    }
    internal open fun isSupportGuide() {
        mVideoCallBack?.isSupportGuide()
    }
    internal open fun onFullScreenStateChange(fullScreen: Boolean) {
        mVideoCallBack?.onFullScreenStateChange(fullScreen)
    }
    internal open fun onViewClick(v: View?): Boolean{
        return false
    }

    override fun isFullScreen(): Boolean{
        return mIsFullScreen
    }

    override fun getAddxPlayer(): IVideoPlayer?{
        return iAddxPlayer
    }

    override fun isRinging(): Boolean{
        return false
    }
    override fun getWhiteLightOn():Boolean{
        return false
    }

    override  fun hideNav(){
    }
}

