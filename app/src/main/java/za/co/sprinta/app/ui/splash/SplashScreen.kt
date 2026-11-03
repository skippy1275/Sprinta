package za.co.sprinta.app.ui.splash

import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import za.co.sprinta.app.R
import za.co.sprinta.app.ui.SprintaBlack
import za.co.sprinta.app.ui.SprintaGold

@OptIn(UnstableApi::class)
@Composable
fun VideoSplashScreen(
    onSplashComplete: () -> Unit
) {
    BackHandler(enabled = true) {}

    val context = LocalContext.current
    var isVideoLoaded by remember { mutableStateOf(false) }
    var videoFailed by remember { mutableStateOf(false) }

    val rawResId = remember { getRawVideoResId(context) }

    val exoPlayer = remember(context, rawResId) {
        if (rawResId != 0) {
            try {
                ExoPlayer.Builder(context).build().apply {
                    val videoUri = Uri.parse("android.resource://${context.packageName}/$rawResId")
                    setMediaItem(MediaItem.fromUri(videoUri))
                    volume = 0f
                    repeatMode = Player.REPEAT_MODE_OFF
                    prepare()
                    playWhenReady = true
                }
            } catch (e: Exception) {
                videoFailed = true
                null
            }
        } else {
            videoFailed = true
            null
        }
    }

    LaunchedEffect(Unit) {
        if (exoPlayer != null && !videoFailed) {
            exoPlayer.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        isVideoLoaded = true
                    } else if (playbackState == Player.STATE_ENDED) {
                        onSplashComplete()
                    }
                }

                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    videoFailed = true
                }
            })
            delay(5000L)
            onSplashComplete()
        } else {
            delay(2500L)
            onSplashComplete()
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer?.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SprintaBlack),
        contentAlignment = Alignment.Center
    ) {
        if (exoPlayer != null && !videoFailed) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        if (exoPlayer == null || videoFailed || !isVideoLoaded) {
            val alphaAnim = remember { Animatable(0f) }
            LaunchedEffect(Unit) {
                alphaAnim.animateTo(1f, animationSpec = tween(800))
            }

            Column(
                modifier = Modifier.alpha(alphaAnim.value),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.sprinta_flat_logo_transparent),
                    contentDescription = "Sprinta Logo",
                    modifier = Modifier.size(160.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Sprinta",
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                    color = SprintaGold
                )
            }
        }
    }
}

private fun getRawVideoResId(context: Context): Int {
    val res1 = context.resources.getIdentifier("splash_video", "raw", context.packageName)
    if (res1 != 0) return res1
    val res2 = context.resources.getIdentifier("sprinta_splash", "raw", context.packageName)
    if (res2 != 0) return res2
    return 0
}
