package com.appknot.akvideoview.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.annotation.MainThread
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

/**
 *
 * @author Jin on 2020/09/10
 */


private const val ARG_URI = "uri_string"
private const val ARG_TITLE = "title"
private const val ARG_START_POSITION = "start_position"

private val PLAYBACK_CHANNEL_ID = "playback_channel"
private val PLAYBACK_NOTIFICATION_ID = 1

class AudioService : LifecycleService() {

    companion object    {
        @MainThread
        fun newIntent(context: Context, title: String, uriString: String, startPosition: Long) =
            Intent(context, AudioService::class.java).apply {
                putExtra(ARG_TITLE, title)
                putExtra(ARG_URI, Uri.parse(uriString))
                    .putExtra(ARG_START_POSITION, startPosition)
            }
    }


    private lateinit var exoPlayer: SimpleExoPlayer
    private var playerNotificationManager: PlayerNotificationManager? = null

    var title: String? = null
    var subText: String? = null
    var iconResId: Int? = null
    var currentContentIntent: Intent? = null

    override fun onCreate() {
        super.onCreate()

        exoPlayer = SimpleExoPlayer.Builder(applicationContext).build()
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.CONTENT_TYPE_SPEECH)
            .build()
        exoPlayer.setAudioAttributes(audioAttributes, true)

        playerNotificationManager = PlayerNotificationManager.Builder(
            applicationContext,
            PLAYBACK_NOTIFICATION_ID,
            PLAYBACK_CHANNEL_ID,
            object : PlayerNotificationManager.MediaDescriptionAdapter  {
                override fun getCurrentContentTitle(player: Player): String =
                    title.toString()

                override fun createCurrentContentIntent(player: Player): PendingIntent? = null

                override fun getCurrentContentText(player: Player): String? =
                    subText

                override fun getCurrentLargeIcon(
                    player: Player,
                    callback: PlayerNotificationManager.BitmapCallback
                ): Bitmap? = null
            }
        ).build()
    }

    @MainThread
    private fun handleIntent(intent: Intent?) {
        intent?.let {
            intent.getParcelableExtra<Uri>(ARG_URI)?.also { uri ->
                title = intent.getStringExtra(ARG_TITLE)
                val startPosition =
                    intent.getLongExtra(ARG_START_POSITION, C.POSITION_UNSET.toLong())
                val playbackSpeed: Float? = null

                play(uri, startPosition, playbackSpeed)
            } ?: Log.w("AudioService", "Playback uri was not set")
        }
    }

    @MainThread
    fun play(uri: Uri, startPosition: Long, playbackSpeed: Float? = null) {
        val userAgent = Util.getUserAgent(applicationContext, BuildConfig.LIBRARY_PACKAGE_NAME)
        val mediaSource = ProgressiveMediaSource.Factory(
            DefaultDataSourceFactory(applicationContext, userAgent),
            DefaultExtractorsFactory()
        ).createMediaSource(MediaItem.fromUri(uri))

        val haveStartPosition = startPosition != C.POSITION_UNSET.toLong()
        if (haveStartPosition) {
            exoPlayer.seekTo(startPosition)
        }

        playbackSpeed?.let { changePlaybackSpeed(playbackSpeed) }

        exoPlayer.prepare(mediaSource, !haveStartPosition, false)
        exoPlayer.playWhenReady = true
    }

    @MainThread
    fun resume() {
        exoPlayer.playWhenReady = true
    }

    @MainThread
    fun pause() {
        exoPlayer.playWhenReady = false
    }

    @MainThread
    fun changePlaybackSpeed(playbackSpeed: Float) {
        exoPlayer.playbackParameters = PlaybackParameters(playbackSpeed)
    }
}