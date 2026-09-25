package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableFloatStateOf
import android.widget.Toast
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ScoreEntity
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import java.util.Locale
import androidx.lifecycle.ViewModelProvider
import android.view.KeyEvent
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.zIndex
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebResourceRequest
import coil.compose.AsyncImage
import com.example.SpotifyManager
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.graphics.lerp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.foundation.layout.BoxScope

fun Context.findActivity(): Activity? {
  var currentContext = this
  while (currentContext is ContextWrapper) {
    if (currentContext is Activity) {
      return currentContext
    }
    currentContext = currentContext.baseContext
  }
  return null
}

var mockFoldableDeviceGlobal by androidx.compose.runtime.mutableStateOf(false)
var mockFoldedStateGlobal by androidx.compose.runtime.mutableStateOf(false)

@Composable
fun isFoldableDevice(): Boolean {
  if (mockFoldableDeviceGlobal) return true
  val context = LocalContext.current
  val activity = remember(context) { context.findActivity() } ?: return false
  
  // 1. Check WindowLayoutInfo for folding features
  val layoutInfoState = remember(activity) {
    WindowInfoTracker.getOrCreate(activity).windowLayoutInfo(activity)
  }.collectAsState(initial = null)
  
  val hasFoldingFeature = remember(layoutInfoState.value) {
    layoutInfoState.value?.displayFeatures?.any { it is FoldingFeature } == true
  }
  
  // 2. Check SensorManager for hinge angle sensor
  val hasHingeSensor = remember(context) {
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? android.hardware.SensorManager
    sensorManager?.getDefaultSensor(android.hardware.Sensor.TYPE_HINGE_ANGLE) != null
  }
  
  return hasFoldingFeature || hasHingeSensor
}

@Composable
fun isFlipStyleFoldable(): Boolean {
  if (mockFoldableDeviceGlobal) return true
  val context = LocalContext.current
  val activity = remember(context) { context.findActivity() } ?: return false
  
  val layoutInfoState = remember(activity) {
    WindowInfoTracker.getOrCreate(activity).windowLayoutInfo(activity)
  }.collectAsState(initial = null)
  
  val hasHorizontalFolding = remember(layoutInfoState.value) {
    layoutInfoState.value?.displayFeatures?.any { 
      it is FoldingFeature && it.orientation == FoldingFeature.Orientation.HORIZONTAL 
    } == true
  }
  
  val isFlipModel = remember {
    val model = android.os.Build.MODEL.lowercase(Locale.US)
    val product = android.os.Build.PRODUCT.lowercase(Locale.US)
    val device = android.os.Build.DEVICE.lowercase(Locale.US)
    model.contains("flip") || product.contains("flip") || device.contains("flip")
  }
  
  val hasVerticalFolding = remember(layoutInfoState.value) {
    layoutInfoState.value?.displayFeatures?.any { 
      it is FoldingFeature && it.orientation == FoldingFeature.Orientation.VERTICAL 
    } == true
  }
  
  val isFoldModel = remember {
    val model = android.os.Build.MODEL.lowercase(Locale.US)
    val product = android.os.Build.PRODUCT.lowercase(Locale.US)
    val device = android.os.Build.DEVICE.lowercase(Locale.US)
    (model.contains("fold") || product.contains("fold") || device.contains("fold")) && !model.contains("flip")
  }

  if (hasVerticalFolding || isFoldModel) {
    return false
  }

  return (hasHorizontalFolding || isFlipModel) && isFoldableDevice()
}


@Composable
fun rememberHingeAngle(): Float? {
  val context = LocalContext.current
  var hingeAngle by remember { mutableStateOf<Float?>(null) }
  DisposableEffect(context) {
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? android.hardware.SensorManager
    val hingeSensor = sensorManager?.getDefaultSensor(android.hardware.Sensor.TYPE_HINGE_ANGLE)
    if (hingeSensor == null) {
      onDispose {}
    } else {
      val listener = object : android.hardware.SensorEventListener {
        override fun onSensorChanged(event: android.hardware.SensorEvent?) {
          event?.values?.firstOrNull()?.let {
            hingeAngle = it
          }
        }
        override fun onAccuracyChanged(sensor: android.hardware.Sensor?, accuracy: Int) {}
      }
      sensorManager.registerListener(listener, hingeSensor, android.hardware.SensorManager.SENSOR_DELAY_NORMAL)
      onDispose {
        sensorManager.unregisterListener(listener)
      }
    }
  }
  return hingeAngle
}

@Composable
fun isFoldedState(threshold: Float): Boolean {
  if (mockFoldableDeviceGlobal) {
    return mockFoldedStateGlobal
  }
  val isFoldable = isFoldableDevice()
  if (!isFoldable) return false

  val hingeAngle = rememberHingeAngle()
  if (hingeAngle != null) {
    return hingeAngle <= threshold
  }

  // Fallback: If hinge angle is not available, check window layout HALF_OPENED
  val context = LocalContext.current
  val activity = remember(context) { context.findActivity() } ?: return false
  val layoutInfoState = remember(activity) {
    WindowInfoTracker.getOrCreate(activity).windowLayoutInfo(activity)
  }.collectAsState(initial = null)

  val layoutInfo = layoutInfoState.value ?: return false
  val foldingFeature = layoutInfo.displayFeatures
    .filterIsInstance<FoldingFeature>()
    .firstOrNull() ?: return false

  return foldingFeature.state == FoldingFeature.State.HALF_OPENED &&
      foldingFeature.orientation == FoldingFeature.Orientation.HORIZONTAL
}

class MainActivity : ComponentActivity() {
  private lateinit var viewModel: PingleViewModel
  private val keyBuffer = StringBuilder()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Enable immersive full-screen mode (hide status and navigation bars)
    val windowInsetsController = androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
    windowInsetsController.systemBarsBehavior =
        androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    windowInsetsController.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())

    // Allow the window to draw under display cutouts (notches) for a perfect bleed-edge game screen
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
      window.attributes.layoutInDisplayCutoutMode =
          android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
    }

    viewModel = ViewModelProvider(this).get(PingleViewModel::class.java)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val isDeviceKilled by viewModel.isDeviceKilled.collectAsState()
        if (isDeviceKilled) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(Color.Black),
            contentAlignment = Alignment.Center
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(16.dp),
              modifier = Modifier.padding(32.dp)
            ) {
              PingleText(
                text = "⚠️",
                style = TextStyle(fontSize = 48.sp)
              )
              PingleText(
                text = "DEVICE PERMANENTLY DEACTIVATED",
                style = TextStyle(
                  fontFamily = FontFamily.SansSerif,
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFFFF453A),
                  letterSpacing = 1.sp
                )
              )
              PingleText(
                text = "killsave signature detected.\nthis device has been blacklisted and will no longer boot this application.",
                style = TextStyle(
                  fontFamily = FontFamily.Monospace,
                  fontSize = 11.sp,
                  color = Color.White.copy(alpha = 0.6f),
                  textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                  lineHeight = 18.sp
                )
              )
            }
          }
        } else {
          PingleSpinApp(viewModel)
        }
      }
    }
  }

  override fun onWindowFocusChanged(hasFocus: Boolean) {
    super.onWindowFocusChanged(hasFocus)
    if (hasFocus) {
      val windowInsetsController = androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
      windowInsetsController.systemBarsBehavior =
          androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
      windowInsetsController.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
    }
  }

  override fun dispatchKeyEvent(event: KeyEvent): Boolean {
    if (event.action == KeyEvent.ACTION_DOWN) {
      val unicode = event.unicodeChar
      if (unicode != 0) {
        val character = unicode.toChar()
        if (character.isLetterOrDigit()) {
          keyBuffer.append(character.lowercaseChar())
          if (keyBuffer.length > 50) {
            keyBuffer.delete(0, keyBuffer.length - 20)
          }
          if (keyBuffer.toString().endsWith("imadethegamelilbro")) {
            viewModel.unlockEverything()
            showToast(this, "Cheat Active: Everything Unlocked!", Toast.LENGTH_LONG)
            keyBuffer.setLength(0)
          }
        }
      }
    }
    return super.dispatchKeyEvent(event)
  }
}

data class PingleTintOption(
  val id: String,
  val name: String,
  val color: Color?,
  val labelColor: Color
)

val PINGLE_TINTS = listOf(
  PingleTintOption("none", "Original", null, Color.White),
  PingleTintOption("red", "Spicy Red", Color(0xFFFF3B30), Color(0xFFFF453A)),
  PingleTintOption("green", "Sour Cream Onion", Color(0xFF34C759), Color(0xFF30D158)),
  PingleTintOption("blue", "Salted Blue", Color(0xFF007AFF), Color(0xFF0A84FF)),
  PingleTintOption("purple", "BBQ Purple", Color(0xFFA252FF), Color(0xFFBF5AF2)),
  PingleTintOption("gold", "Premium Cheddar Gold", Color(0xFFFFCC00), Color(0xFFFFD60A))
)

@Composable
fun VideoPingle(
  videoUri: String,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var mediaPlayerRef by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<android.media.MediaPlayer?>(null) }

  androidx.compose.runtime.DisposableEffect(videoUri) {
    onDispose {
      try {
        mediaPlayerRef?.stop()
        mediaPlayerRef?.release()
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  androidx.compose.ui.viewinterop.AndroidView(
    factory = { ctx ->
      val textureView = android.view.TextureView(ctx).apply {
        isOpaque = false
      }
      val mediaPlayer = android.media.MediaPlayer().apply {
        isLooping = true
        setVolume(0f, 0f)
      }
      mediaPlayerRef = mediaPlayer

      textureView.surfaceTextureListener = object : android.view.TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: android.graphics.SurfaceTexture, width: Int, height: Int) {
          try {
            val s = android.view.Surface(surface)
            mediaPlayer.setSurface(s)
            mediaPlayer.setDataSource(ctx, android.net.Uri.parse(videoUri))
            mediaPlayer.prepareAsync()
          } catch (e: Exception) {
            e.printStackTrace()
          }
        }
        override fun onSurfaceTextureSizeChanged(surface: android.graphics.SurfaceTexture, width: Int, height: Int) {}
        override fun onSurfaceTextureDestroyed(surface: android.graphics.SurfaceTexture): Boolean {
          return true
        }
        override fun onSurfaceTextureUpdated(surface: android.graphics.SurfaceTexture) {}
      }

      mediaPlayer.setOnPreparedListener { mp ->
        try {
          mp.start()
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }

      textureView
    },
    modifier = modifier
  )
}

@Composable
fun PingleImage(
  useCustomImage: Boolean,
  customImageUri: String?,
  modifier: Modifier = Modifier,
  contentDescription: String = "Pingle",
  contentScale: ContentScale = ContentScale.Fit,
  colorFilter: ColorFilter? = null
) {
  val viewModel: PingleViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
  val invisiblePingleEnabled by viewModel.invisiblePingleEnabled.collectAsState()
  val loadEverything by viewModel.loadEverything.collectAsState()
  val customMediaType by viewModel.customMediaType.collectAsState()

  if (invisiblePingleEnabled) {
    Box(modifier = modifier)
  } else if (useCustomImage && !customImageUri.isNullOrEmpty()) {
    if (customMediaType == "video") {
      VideoPingle(videoUri = customImageUri, modifier = modifier)
    } else if (customMediaType == "unrecognized") {
      BoxWithConstraints(
        modifier = modifier
          .background(Color(0xFF222222), shape = androidx.compose.foundation.shape.CircleShape)
          .border(4.dp, Color(0xFF0078D7), shape = androidx.compose.foundation.shape.CircleShape),
        contentAlignment = Alignment.Center
      ) {
        val sizeDp = maxWidth
        val fontSizeSp = (sizeDp.value * 0.6f).sp
        PingleText(
          text = "?",
          style = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = fontSizeSp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
          )
        )
      }
    } else {
      val painter = coil.compose.rememberAsyncImagePainter(model = customImageUri)
      Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        colorFilter = colorFilter
      )
    }
  } else if (loadEverything && viewModel.preloadedPurePringle != null) {
    val preloaded = viewModel.preloadedPurePringle!!
    Image(
      bitmap = preloaded,
      contentDescription = contentDescription,
      modifier = modifier,
      contentScale = contentScale,
      colorFilter = colorFilter
    )
  } else {
    Image(
      painter = painterResource(id = R.drawable.img_pure_pringle_1780498841768),
      contentDescription = contentDescription,
      modifier = modifier,
      contentScale = contentScale,
      colorFilter = colorFilter
    )
  }
}

@Composable
fun PingleSpinApp(viewModel: PingleViewModel = viewModel()) {
  KeepScreenOn()
  var hasShownLoadingScreen by androidx.compose.runtime.saveable.rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }
  val screen by viewModel.currentScreen.collectAsState()
  val speed by viewModel.pingleSpeed.collectAsState()
  val pingleFriction by viewModel.pingleFriction.collectAsState()
  val isManualUnlocked by viewModel.isManualUnlocked.collectAsState()
  val topScores by viewModel.topScores.collectAsState()
  val pingleTintId by viewModel.pingleTint.collectAsState()
  val pingleCustomColorInt by viewModel.pingleCustomColor.collectAsState()
  val totalSpinDuration by viewModel.totalSpinDuration.collectAsState()
  val foldAngleThreshold by viewModel.pingleFoldAngleThreshold.collectAsState()
  val pingleCustomImageUri by viewModel.pingleCustomImageUri.collectAsState()
  val useCustomImage by viewModel.useCustomImage.collectAsState()

  val isDebugUnlocked by viewModel.isDebugUnlocked.collectAsState()
  val easterRainbowNeon by viewModel.easterRainbowNeon.collectAsState()
  val easterMatrixBg by viewModel.easterMatrixBg.collectAsState()
  val easterReverseSpin by viewModel.easterReverseSpin.collectAsState()
  val easterSpaceStars by viewModel.easterSpaceStars.collectAsState()

  val isRainbowNeonUnlocked by viewModel.isRainbowNeonUnlocked.collectAsState()
  val isMatrixBgUnlocked by viewModel.isMatrixBgUnlocked.collectAsState()
  val isReverseSpinUnlocked by viewModel.isReverseSpinUnlocked.collectAsState()
  val isSpaceStarsUnlocked by viewModel.isSpaceStarsUnlocked.collectAsState()
  val isChineseCrashed by viewModel.isChineseCrashed.collectAsState()

  PingleBackgroundWrapper(
    easterSpaceStars = easterSpaceStars,
    easterMatrixBg = easterMatrixBg
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      val currentNonOptionsScreen = remember(screen) {
        if (screen != Screen.OPTIONS) screen else Screen.HOME
      }

      AnimatedContent(
        targetState = currentNonOptionsScreen,
        transitionSpec = {
          val rand = kotlin.random.Random.nextInt(5)
          when (rand) {
            0 -> {
              // Vertical slide up + fade in
              (slideInVertically(initialOffsetY = { it }, animationSpec = tween(500, easing = FastOutSlowInEasing)) + fadeIn(animationSpec = tween(500)))
                .togetherWith(slideOutVertically(targetOffsetY = { -it }, animationSpec = tween(500, easing = FastOutSlowInEasing)) + fadeOut(animationSpec = tween(500)))
            }
            1 -> {
              // Horizontal slide from right + fade in
              (slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(500, easing = FastOutSlowInEasing)) + fadeIn(animationSpec = tween(500)))
                .togetherWith(slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(500, easing = FastOutSlowInEasing)) + fadeOut(animationSpec = tween(500)))
            }
            2 -> {
              // Scale up / zoom in + fade in
              (scaleIn(initialScale = 0.8f, animationSpec = tween(500, easing = FastOutSlowInEasing)) + fadeIn(animationSpec = tween(500)))
                .togetherWith(scaleOut(targetScale = 1.1f, animationSpec = tween(500, easing = FastOutSlowInEasing)) + fadeOut(animationSpec = tween(500)))
            }
            3 -> {
              // Slide down from top + fade in
              (slideInVertically(initialOffsetY = { -it }, animationSpec = tween(500, easing = FastOutSlowInEasing)) + fadeIn(animationSpec = tween(500)))
                .togetherWith(slideOutVertically(targetOffsetY = { it }, animationSpec = tween(500, easing = FastOutSlowInEasing)) + fadeOut(animationSpec = tween(500)))
            }
            else -> {
              // Slide from left, scale down slightly
              (slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(500, easing = FastOutSlowInEasing)) + scaleIn(initialScale = 0.9f, animationSpec = tween(500)) + fadeIn(tween(500)))
                .togetherWith(slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(500, easing = FastOutSlowInEasing)) + scaleOut(targetScale = 0.9f, animationSpec = tween(500)) + fadeOut(tween(500)))
            }
          }
        },
        modifier = Modifier.fillMaxSize(),
        label = "ScreenTransition"
      ) { targetScreen ->
        when (targetScreen) {
          Screen.HOME -> HomeScreen(
            isManualUnlocked = isManualUnlocked,
            useCustomImage = useCustomImage,
            pingleCustomImageUri = pingleCustomImageUri,
            showLoadingSequence = !hasShownLoadingScreen,
            onLoadingFinished = { hasShownLoadingScreen = true },
            onUnlockManual = { viewModel.unlockManual() },
            onPlayClicked = { viewModel.setScreen(Screen.PLAY) },
            onManualSpinClicked = { viewModel.setScreen(Screen.MANUAL) },
            onHighScoresClicked = { viewModel.setScreen(Screen.HIGH_SCORES) },
            onOptionsClicked = { viewModel.setScreen(Screen.OPTIONS) }
          )
          Screen.PLAY -> PlayScreen(
            speed = speed,
            pingleTintId = pingleTintId,
            pingleCustomColorInt = pingleCustomColorInt,
            foldAngleThreshold = foldAngleThreshold,
            useCustomImage = useCustomImage,
            pingleCustomImageUri = pingleCustomImageUri,
            onBackToHomeWithScore = { score ->
              viewModel.saveScore(score)
              viewModel.setScreen(Screen.HIGH_SCORES)
            },
            onElapsedClickToHighScores = { score ->
              viewModel.saveScore(score)
              viewModel.setScreen(Screen.HIGH_SCORES)
            }
          )
          Screen.HIGH_SCORES -> HighScoresScreen(
            scores = topScores,
            onBackClicked = { viewModel.setScreen(Screen.HOME) },
            onClearAll = { viewModel.clearAllScores() }
          )
          Screen.MANUAL -> ManualPlayScreen(
            pingleFriction = pingleFriction,
            pingleTintId = pingleTintId,
            pingleCustomColorInt = pingleCustomColorInt,
            foldAngleThreshold = foldAngleThreshold,
            useCustomImage = useCustomImage,
            pingleCustomImageUri = pingleCustomImageUri,
            onBackToHomeWithScore = { score ->
              viewModel.saveScore(score, isManual = true)
              viewModel.setScreen(Screen.HIGH_SCORES)
            },
            onElapsedClickToHighScores = { score ->
              viewModel.saveScore(score, isManual = true)
              viewModel.setScreen(Screen.HIGH_SCORES)
            }
          )
          Screen.OPTIONS -> {}
          Screen.PINGUI_SETUP -> PinguiSetupScreen(
            onBackClicked = { viewModel.setScreen(Screen.OPTIONS) },
            onSetUpNowClicked = { viewModel.setScreen(Screen.PINGUI_GAME_EDIT) }
          )
          Screen.PINGUI_GAME_EDIT -> PinguiGameEditScreen(
            onBackClicked = { viewModel.setScreen(Screen.OPTIONS) }
          )
        }
      }

      AnimatedVisibility(
        visible = screen == Screen.OPTIONS,
        enter = slideInVertically(
          initialOffsetY = { it },
          animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
        ),
        exit = slideOutVertically(
          targetOffsetY = { it },
          animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
        ),
        modifier = Modifier.fillMaxSize()
      ) {
        OptionsScreen(
          speed = speed,
          pingleFriction = pingleFriction,
          isManualUnlocked = isManualUnlocked,
          pingleTintId = pingleTintId,
          pingleCustomColorInt = pingleCustomColorInt,
          totalSpinDuration = totalSpinDuration,
          foldAngleThreshold = foldAngleThreshold,
          useCustomImage = useCustomImage,
          pingleCustomImageUri = pingleCustomImageUri,
          isDebugUnlocked = isDebugUnlocked,
          easterRainbowNeon = easterRainbowNeon,
          easterMatrixBg = easterMatrixBg,
          easterReverseSpin = easterReverseSpin,
          easterSpaceStars = easterSpaceStars,
          isRainbowNeonUnlocked = isRainbowNeonUnlocked,
          isMatrixBgUnlocked = isMatrixBgUnlocked,
          isReverseSpinUnlocked = isReverseSpinUnlocked,
          isSpaceStarsUnlocked = isSpaceStarsUnlocked,
          onSpeedChange = { viewModel.setPingleSpeed(it) },
          onFrictionChange = { viewModel.setPingleFriction(it) },
          onTintSelect = { viewModel.setPingleTint(it) },
          onCustomColorChange = { viewModel.setPingleCustomColor(it) },
          onFoldAngleThresholdChange = { viewModel.setPingleFoldAngleThreshold(it) },
          onCustomImageUriChange = { viewModel.setCustomImageUri(it) },
          onUseCustomImageChange = { viewModel.setUseCustomImage(it) },
          onEasterRainbowNeonChange = { viewModel.setEasterRainbowNeon(it) },
          onEasterMatrixBgChange = { viewModel.setEasterMatrixBg(it) },
          onEasterReverseSpinChange = { viewModel.setEasterReverseSpin(it) },
          onEasterSpaceStarsChange = { viewModel.setEasterSpaceStars(it) },
          onCreateDebugScore = { viewModel.saveDebugScore(it) },
          onBackClicked = { viewModel.setScreen(Screen.HOME) }
        )
      }

      if (isDebugUnlocked) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp),
          contentAlignment = Alignment.TopEnd
        ) {
          var showExpandedControls by remember { mutableStateOf(false) }
          
          if (showExpandedControls) {
            Column(
              modifier = Modifier
                .background(Color(0xE6121212), RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFF00FFCC).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                .padding(12.dp)
                .width(160.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              PingleText(
                text = "DEV FOLD MODE",
                style = TextStyle(
                  color = Color(0xFF00FFCC),
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.sp
                )
              )
              
              listOf(
                "STANDARD" to (false to false),
                "OPENED" to (true to false),
                "FOLDED" to (true to true)
              ).forEach { (label, states) ->
                val (mockDevice, mockFolded) = states
                val isSelected = (mockFoldableDeviceGlobal == mockDevice && (!mockDevice || mockFoldedStateGlobal == mockFolded))
                
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .background(
                      if (isSelected) Color(0xFF00FFCC).copy(alpha = 0.2f) else Color.Transparent,
                      RoundedCornerShape(8.dp)
                    )
                    .border(
                      width = 1.dp,
                      color = if (isSelected) Color(0xFF00FFCC) else Color.White.copy(alpha = 0.15f),
                      shape = RoundedCornerShape(8.dp)
                    )
                    .clickable {
                      mockFoldableDeviceGlobal = mockDevice
                      mockFoldedStateGlobal = mockFolded
                    },
                  contentAlignment = Alignment.Center
                ) {
                  PingleText(
                    text = label,
                    style = TextStyle(
                      color = if (isSelected) Color(0xFF00FFCC) else Color.White.copy(alpha = 0.6f),
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      letterSpacing = 0.5.sp
                    )
                  )
                }
              }
              
              // Collapse button
              PingleText(
                text = "MINIMIZE",
                style = TextStyle(
                  color = Color.White.copy(alpha = 0.4f),
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 0.5.sp
                ),
                modifier = Modifier
                  .clickable { showExpandedControls = false }
                  .padding(top = 4.dp)
              )
            }
          } else {
            // Minimized Pill
            val currentModeLabel = when {
              !mockFoldableDeviceGlobal -> "DEV FOLD: OFF"
              !mockFoldedStateGlobal -> "DEV: OPENED"
              else -> "DEV: FOLDED"
            }
            val badgeColor = when {
              !mockFoldableDeviceGlobal -> Color.White.copy(alpha = 0.3f)
              !mockFoldedStateGlobal -> Color(0xFF0078D7)
              else -> Color(0xFF00FFCC)
            }
            
            Row(
              modifier = Modifier
                .background(Color(0xE6121212), RoundedCornerShape(20.dp))
                .border(1.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .clickable { showExpandedControls = true }
                .padding(horizontal = 14.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .background(badgeColor, androidx.compose.foundation.shape.CircleShape)
              )
              PingleText(
                text = currentModeLabel,
                style = TextStyle(
                  color = Color.White,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 0.5.sp
                )
              )
            }
          }
        }


      }
    }
  }
}

@Composable
fun MinimalistButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val viewModel: PingleViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
  val buttonVibrationMode by viewModel.buttonVibrationMode.collectAsState()
  val context = androidx.compose.ui.platform.LocalContext.current

  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(56.dp)
      .border(
        width = 1.dp,
        color = Color.White.copy(alpha = 0.2f),
        shape = RoundedCornerShape(14.dp)
      )
      .background(Color.Black, shape = RoundedCornerShape(14.dp))
      .clickable {
        VibrationHelper.vibrate(context, buttonVibrationMode)
        onClick()
      }
      .padding(horizontal = 16.dp),
    contentAlignment = Alignment.Center
  ) {
    PingleText(
      text = text.uppercase(Locale.US),
      style = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = Color.White,
        letterSpacing = 2.sp
      )
    )
  }
}

@Composable
fun HomeScreen(
  isManualUnlocked: Boolean,
  useCustomImage: Boolean,
  pingleCustomImageUri: String?,
  showLoadingSequence: Boolean,
  onLoadingFinished: () -> Unit,
  onUnlockManual: () -> Unit,
  onPlayClicked: () -> Unit,
  onManualSpinClicked: () -> Unit,
  onHighScoresClicked: () -> Unit,
  onOptionsClicked: () -> Unit
) {
  val context = LocalContext.current
  val viewModel: PingleViewModel = viewModel()
  val easterSpaceStars by viewModel.easterSpaceStars.collectAsState()
  val easterMatrixBg by viewModel.easterMatrixBg.collectAsState()
  val pingleTintId by viewModel.pingleTint.collectAsState()
  val pingleCustomColorInt by viewModel.pingleCustomColor.collectAsState()
  val easterRainbowNeon by viewModel.easterRainbowNeon.collectAsState()

  // Cycle color if easterRainbowNeon is true on the title/home screen
  val rainbowColor by if (easterRainbowNeon) {
    val infiniteTransition = rememberInfiniteTransition(label = "rainbow_home")
    val hue by infiniteTransition.animateFloat(
      initialValue = 0f,
      targetValue = 360f,
      animationSpec = infiniteRepeatable(
        animation = tween(6000, easing = LinearEasing),
        repeatMode = RepeatMode.Restart
      ),
      label = "hue_home"
    )
    remember(hue) {
      mutableStateOf(Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, 1f, 1f))))
    }
  } else {
    remember { mutableStateOf(Color.Transparent) }
  }

  val selectedTintOption = remember(pingleTintId) { PINGLE_TINTS.firstOrNull { it.id == pingleTintId } }
  val tintColor = remember(pingleTintId, pingleCustomColorInt, easterRainbowNeon, rainbowColor) {
    if (easterRainbowNeon) {
      rainbowColor
    } else if (pingleTintId == "custom") {
      Color(pingleCustomColorInt)
    } else {
      selectedTintOption?.color
    }
  }
  val colorFilter = remember(tintColor) { tintColor?.let { ColorFilter.tint(it, BlendMode.Color) } }

  var clickCount by remember { mutableIntStateOf(0) }
  var isPlayExpanded by remember { mutableStateOf(false) }

  var loadingState by remember { mutableStateOf(if (showLoadingSequence) "loading" else "menu") }
  var rotationAngle by remember { mutableFloatStateOf(0f) }
  var menuAlpha by remember { mutableFloatStateOf(if (showLoadingSequence) 0f else 1f) }

  LaunchedEffect(showLoadingSequence) {
    if (showLoadingSequence) {
      loadingState = "loading"
      val spinDuration = 5000L // 5 seconds
      val decelerateDuration = 2000L
      var lastTime = withFrameMillis { it }
      val startTime = lastTime

      while (true) {
        val now = withFrameMillis { it }
        val elapsed = now - startTime
        val dt = (now - lastTime) / 1000f // delta time in seconds
        lastTime = now

        val (targetSpeedDegreesPerSec, currentPhase) = when {
          elapsed < spinDuration -> {
            1440f to "loading" // 4 full rotations per second
          }
          elapsed < spinDuration + decelerateDuration -> {
            val decelElapsed = elapsed - spinDuration
            val fraction = decelElapsed.toFloat() / decelerateDuration
            val easedFraction = 1f - (1f - fraction) * (1f - fraction) // quadratic out
            val s = 1440f * (1f - easedFraction)
            s to "slowing"
          }
          else -> {
            0f to "menu"
          }
        }

        if (currentPhase != loadingState) {
          loadingState = currentPhase
        }

        rotationAngle = (rotationAngle + targetSpeedDegreesPerSec * dt) % 360f

        val alpha = when {
          elapsed < spinDuration -> 0f
          elapsed < spinDuration + decelerateDuration -> {
            val decelElapsed = elapsed - spinDuration
            val fraction = decelElapsed.toFloat() / decelerateDuration
            // Smooth ease-in-out
            if (fraction < 0.5f) {
              2f * fraction * fraction
            } else {
              1f - (-2f * fraction + 2f) * (-2f * fraction + 2f) / 2f
            }
          }
          else -> 1f
        }
        menuAlpha = alpha

        if (currentPhase == "menu") {
          val startAngle = rotationAngle % 360f
          val diff = if (startAngle > 180f) startAngle - 360f else startAngle
          val steps = 30
          for (step in 1..steps) {
            val progress = step.toFloat() / steps
            rotationAngle = startAngle - diff * progress
            kotlinx.coroutines.delay(16)
          }
          rotationAngle = 0f
          onLoadingFinished()
          break
        }
      }
    } else {
      loadingState = "menu"
      menuAlpha = 1f
    }
  }

  if (isPlayExpanded) {
    androidx.activity.compose.BackHandler {
      isPlayExpanded = false
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .pingleBackground(easterSpaceStars, easterMatrixBg)
      .statusBarsPadding()
      .navigationBarsPadding()
      .testTag("home_screen")
  ) {
    // Full screen overlay scrim directly behind the controls to reverse the animation back to play button
    androidx.compose.animation.AnimatedVisibility(
      visible = isPlayExpanded,
      enter = fadeIn(animationSpec = tween(350)),
      exit = fadeOut(animationSpec = tween(300))
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = 0.55f))
          .clickable(
            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
            indication = null
          ) {
            isPlayExpanded = false
          }
      )
    }

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 32.dp, vertical = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Header logo description matching exactly "made by pingleTEK ©2025-inf"
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .padding(top = 16.dp)
          .graphicsLayer { alpha = menuAlpha }
          .clickable(
            indication = null,
            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
          ) {
            if (loadingState == "menu" && !isManualUnlocked) {
              clickCount++
              if (clickCount >= 8) {
                onUnlockManual()
                showToast(context, "MANUAL SPIN UNLOCKED!", Toast.LENGTH_SHORT)
              }
            }
          }
          .testTag("pinglespin_title")
      ) {
        val isV2xUnlocked by viewModel.isV2xUnlocked.collectAsState()
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          PingleText(
            text = "pinglespin".uppercase(Locale.US),
            style = TextStyle(
              fontFamily = FontFamily.SansSerif,
              fontSize = 28.sp,
              fontWeight = FontWeight.ExtraLight,
              color = Color.White,
              letterSpacing = 6.sp
            )
          )
          Spacer(modifier = Modifier.width(8.dp))
          Row(verticalAlignment = Alignment.Top) {
            PingleText(
              text = "V2",
              style = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00FFCC),
                letterSpacing = 2.sp
              )
            )
            if (isV2xUnlocked) {
              PingleText(
                text = "x",
                style = TextStyle(
                  fontFamily = FontFamily.SansSerif,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF00FFCC)
                )
              )
            }
          }
        }
        Spacer(modifier = Modifier.height(6.dp))
        PingleText(
          text = "made by pingleTEK ©2025-inf".uppercase(Locale.US),
          style = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.4f),
            letterSpacing = 2.sp
          )
        )
      }

      val invisiblePingleEnabled by viewModel.invisiblePingleEnabled.collectAsState()

      // Atmospheric centered static pingle
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
          .size(240.dp)
      ) {
        Box(
          modifier = Modifier
            .size(240.dp, 140.dp)
            .background(
              Brush.radialGradient(
                colors = listOf(
                  Color(0x18FFD93D),
                  Color(0x00FFD93D)
                )
              )
            )
        )
        PingleImage(
          useCustomImage = useCustomImage,
          customImageUri = pingleCustomImageUri,
          contentDescription = "Static Pringle",
          modifier = Modifier
            .size(190.dp)
            .graphicsLayer {
              rotationZ = rotationAngle + 180f
            },
          contentScale = ContentScale.Fit,
          colorFilter = colorFilter
        )
      }

      // Elegant minimal control list with smooth size change animation
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 24.dp)
          .graphicsLayer { alpha = menuAlpha },
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(450, easing = FastOutSlowInEasing))
        ) {
          if (!isPlayExpanded) {
            MinimalistButton(
              text = "play",
              onClick = {
                if (loadingState == "menu") {
                  if (isManualUnlocked) {
                    isPlayExpanded = true
                  } else {
                    onPlayClicked()
                  }
                }
              },
              modifier = Modifier.testTag("play_button")
            )
          } else {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .border(
                  width = 1.dp,
                  color = Color.White.copy(alpha = 0.2f),
                  shape = RoundedCornerShape(16.dp)
                )
                .background(Color.Black, shape = RoundedCornerShape(16.dp))
                .padding(20.dp)
                .clickable(
                  interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                  indication = null
                ) {
                  // Absorb clicks inside the container so they don't trigger the scrim click
                },
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
              PingleText(
                text = "SELECT GAMEMODE",
                style = TextStyle(
                  fontFamily = FontFamily.SansSerif,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White.copy(alpha = 0.8f),
                  letterSpacing = 2.sp
                )
              )

              Spacer(modifier = Modifier.height(4.dp))

              MinimalistButton(
                text = "normal",
                onClick = {
                  if (loadingState == "menu") {
                    isPlayExpanded = false
                    onPlayClicked()
                  }
                },
                modifier = Modifier.testTag("gamemode_normal_button")
              )

              MinimalistButton(
                text = "PINGLEMANUALSPIN",
                onClick = {
                  if (loadingState == "menu") {
                    isPlayExpanded = false
                    onManualSpinClicked()
                  }
                },
                modifier = Modifier.testTag("gamemode_manual_button")
              )
            }
          }
        }

        MinimalistButton(
          text = "high scores",
          onClick = {
            if (loadingState == "menu") {
              onHighScoresClicked()
            }
          },
          modifier = Modifier.testTag("high_scores_button")
        )
        MinimalistButton(
          text = "options",
          onClick = {
            if (loadingState == "menu") {
              onOptionsClicked()
            }
          },
          modifier = Modifier.testTag("options_button")
        )
      }
    }
  }
}

@Composable
fun PlayScreen(
  speed: Float,
  pingleTintId: String,
  pingleCustomColorInt: Int,
  foldAngleThreshold: Float,
  useCustomImage: Boolean,
  pingleCustomImageUri: String?,
  onBackToHomeWithScore: (Long) -> Unit,
  onElapsedClickToHighScores: (Long) -> Unit
) {
  var elapsedTimeMs by remember { mutableLongStateOf(0L) }

  val context = LocalContext.current
  DisposableEffect(Unit) {
    onDispose {
      SpotifyManager.getInstance(context).stopPreview()
    }
  }

  val viewModel: PingleViewModel = viewModel()
  val easterRainbowNeon by viewModel.easterRainbowNeon.collectAsState()
  val easterReverseSpin by viewModel.easterReverseSpin.collectAsState()
  val easterSpaceStars by viewModel.easterSpaceStars.collectAsState()
  val easterMatrixBg by viewModel.easterMatrixBg.collectAsState()
  val spinVibrationMode by viewModel.spinVibrationMode.collectAsState()
  val spinVibrationInterval by viewModel.spinVibrationInterval.collectAsState()

  val isFolded = isFoldedState(threshold = foldAngleThreshold)
  val isFlipFoldable = isFlipStyleFoldable()

  val normalLayout by viewModel.normalLayout.collectAsState()
  val unfoldedNormalLayout by viewModel.unfoldedNormalLayout.collectAsState()
  val foldedNormalLayout by viewModel.foldedNormalLayout.collectAsState()

  val activeLayout = remember(isFolded, isFlipFoldable, normalLayout, unfoldedNormalLayout, foldedNormalLayout) {
    when {
      isFlipFoldable && isFolded -> foldedNormalLayout
      isFlipFoldable && !isFolded -> unfoldedNormalLayout
      else -> normalLayout
    }
  }

  val pingleScaleState = activeLayout.pingleScale
  val pingleOffsetXState = activeLayout.pingleOffsetX
  val pingleOffsetYState = activeLayout.pingleOffsetY
  val pingleTiltState = activeLayout.pingleTilt

  val timerScaleState = activeLayout.timerScale
  val timerOffsetXState = activeLayout.timerOffsetX
  val timerOffsetYState = activeLayout.timerOffsetY
  val timerTiltState = activeLayout.timerTilt

  val spotifyScaleState = activeLayout.spotifyScale
  val spotifyOffsetXState = activeLayout.spotifyOffsetX
  val spotifyOffsetYState = activeLayout.spotifyOffsetY
  val spotifyTiltState = activeLayout.spotifyTilt


  // Cycle color if easterRainbowNeon is true
  val rainbowColor by if (easterRainbowNeon) {
    val infiniteTransition = rememberInfiniteTransition(label = "rainbow")
    val hue by infiniteTransition.animateFloat(
      initialValue = 0f,
      targetValue = 360f,
      animationSpec = infiniteRepeatable(
        animation = tween(6000, easing = LinearEasing),
        repeatMode = RepeatMode.Restart
      ),
      label = "hue"
    )
    remember(hue) {
      mutableStateOf(Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, 1f, 1f))))
    }
  } else {
    remember { mutableStateOf(Color.Transparent) }
  }

  val selectedTintOption = remember(pingleTintId) { PINGLE_TINTS.firstOrNull { it.id == pingleTintId } }
  val tintColor = remember(pingleTintId, pingleCustomColorInt, easterRainbowNeon, rainbowColor) {
    if (easterRainbowNeon) {
      rainbowColor
    } else if (pingleTintId == "custom") {
      Color(pingleCustomColorInt)
    } else {
      selectedTintOption?.color
    }
  }
  val colorFilter = remember(tintColor) { tintColor?.let { ColorFilter.tint(it, BlendMode.Color) } }

  val invisiblePingleEnabled by viewModel.invisiblePingleEnabled.collectAsState()

  // High precision timer loop updating roughly at ~60fps
  LaunchedEffect(invisiblePingleEnabled) {
    if (invisiblePingleEnabled) {
      elapsedTimeMs = 0L
    } else {
      val startTime = System.currentTimeMillis()
      while (isActive) {
        androidx.compose.runtime.withFrameMillis {
          elapsedTimeMs = System.currentTimeMillis() - startTime
        }
      }
    }
  }

  // Spin speed ratio: baseline 3 seconds divided by multiplier
  val rotationPeriod = (3000 / speed).toInt().coerceAtLeast(100)

  val infiniteTransition = rememberInfiniteTransition(label = "pingle_spin_transition")
  val rotationRaw by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = rotationPeriod, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "pingle_rotation"
  )
  val rotation = if (easterReverseSpin) -rotationRaw else rotationRaw

  // Calibrate exact millisecond representations
  val totalSeconds = elapsedTimeMs / 1000
  val minutes = (totalSeconds / 60) % 60
  val seconds = totalSeconds % 60
  val centiseconds = (elapsedTimeMs % 1000) / 10

  val formattedTime = if (totalSeconds < 60) {
    String.format(Locale.US, "%d.%02d", seconds, centiseconds)
  } else {
    String.format(Locale.US, "%02d:%02d.%02d", minutes, seconds, centiseconds)
  }

  val isFoldable = isFoldableDevice()

  val transitionProgress by animateFloatAsState(
    targetValue = if (isFolded) 1f else 0f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioLowBouncy,
      stiffness = Spring.StiffnessLow
    ),
    label = "fold_transition"
  )

  Box(
    modifier = Modifier
      .fillMaxSize()
      .pingleBackground(easterSpaceStars, easterMatrixBg)
      .testTag("play_viewport")
  ) {
    // 1. Centered Spinning Pringle unit moving smoothly with transition progress
    Column(
      modifier = Modifier
        .align(BiasAlignment(0f, -0.5f * transitionProgress))
        .offset(x = pingleOffsetXState.dp, y = pingleOffsetYState.dp)
        .graphicsLayer(
          scaleX = pingleScaleState,
          scaleY = pingleScaleState,
          rotationZ = pingleTiltState
        ),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      val pringleSize = (260f - 20f * transitionProgress).dp
      Box(contentAlignment = Alignment.Center) {
        Box(
          modifier = Modifier
            .size(320.dp, 200.dp)
            .background(
              Brush.radialGradient(
                colors = listOf(
                  Color(0x22FFD93D),
                  Color(0x00FFD93D)
                )
              )
            )
        )

        PingleImage(
          useCustomImage = useCustomImage,
          customImageUri = pingleCustomImageUri,
          contentDescription = "Spinning Pringle",
          modifier = Modifier
            .size(pringleSize)
            .graphicsLayer(rotationZ = rotation)
            .testTag("spinning_pringle"),
          contentScale = ContentScale.Fit,
          colorFilter = colorFilter
        )
      }
    }

    // 2. Header Row (Timer, Sensing Active Badge, Check Button) sliding down/up
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val headerTopPadding = (statusBarPadding.value * (1f - transitionProgress)).dp
    val headerHorizontalPadding = (24f - 8f * transitionProgress).dp
    val headerVerticalPadding = (20f - 12f * transitionProgress).dp

    Box(
      modifier = Modifier
        .align(BiasAlignment(0f, -1.0f + 1.5f * transitionProgress))
        .offset(x = timerOffsetXState.dp, y = timerOffsetYState.dp)
        .graphicsLayer(
          scaleX = timerScaleState,
          scaleY = timerScaleState,
          rotationZ = timerTiltState
        )
        .padding(top = headerTopPadding)
        .padding(horizontal = headerHorizontalPadding, vertical = headerVerticalPadding)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Timer
        val timerSize = (24f + 4f * transitionProgress).sp
        val timerWeight = if (transitionProgress > 0.5f) FontWeight.SemiBold else FontWeight.Medium
        val timerAlpha = 0.85f + 0.05f * transitionProgress
        PingleText(
          text = formattedTime,
          style = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontSize = timerSize,
            fontWeight = timerWeight,
            color = Color.White.copy(alpha = timerAlpha),
            letterSpacing = (-0.5).sp,
            fontFeatureSettings = "tnum"
          ),
          modifier = Modifier
            .clickable { onElapsedClickToHighScores(elapsedTimeMs) }
            .burnInProtection(enableShine = true)
            .testTag("elapsed_timer")
        )

        // Stop & Save
        val buttonSize = (48f + 4f * transitionProgress).dp
        val iconSize = (24f + 2f * transitionProgress).dp
        Box(
          modifier = Modifier
            .size(buttonSize)
            .border(
              width = 1.dp,
              color = Color.White.copy(alpha = 0.15f),
              shape = RoundedCornerShape(12.dp)
            )
            .background(Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(12.dp))
            .clickable { onBackToHomeWithScore(elapsedTimeMs) }
            .testTag("stop_save_button"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Stop & Save",
            tint = Color.White,
            modifier = Modifier
              .size(iconSize)
              .burnInProtection(enableShine = true)
          )
        }
      }
    }

    // 3. Spotify Widget remains anchored beautifully at the bottom
    Box(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .navigationBarsPadding()
        .padding(bottom = 16.dp)
        .offset(x = spotifyOffsetXState.dp, y = spotifyOffsetYState.dp)
        .graphicsLayer(
          scaleX = spotifyScaleState,
          scaleY = spotifyScaleState,
          rotationZ = spotifyTiltState
        )
    ) {
      SpotifyPlayerWidget()
    }
  }
}

@Composable
fun HighScoresScreen(
  scores: List<ScoreEntity>,
  onBackClicked: () -> Unit,
  onClearAll: () -> Unit
) {
  val viewModel: PingleViewModel = viewModel()
  val easterSpaceStars by viewModel.easterSpaceStars.collectAsState()
  val easterMatrixBg by viewModel.easterMatrixBg.collectAsState()

  Box(
    modifier = Modifier
      .fillMaxSize()
      .pingleBackground(easterSpaceStars, easterMatrixBg)
      .statusBarsPadding()
      .navigationBarsPadding()
      .testTag("high_scores_screen")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 32.dp, vertical = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      PingleText(
        text = "HIGH SCORES",
        style = TextStyle(
          fontFamily = FontFamily.SansSerif,
          fontSize = 24.sp,
          fontWeight = FontWeight.ExtraLight,
          color = Color.White,
          letterSpacing = 4.sp
        ),
        modifier = Modifier.padding(top = 16.dp)
      )

      Spacer(modifier = Modifier.height(4.dp))

      PingleText(
        text = "MOST TIME PINgLE SPINnED",
        style = TextStyle(
          fontFamily = FontFamily.SansSerif,
          fontSize = 9.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White.copy(alpha = 0.4f),
          letterSpacing = 2.sp
        )
      )

      Spacer(modifier = Modifier.height(32.dp))

      if (scores.isEmpty()) {
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            PingleText(
              text = "NO SPINS YET",
              style = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.3f),
                letterSpacing = 2.sp
              )
            )
            Spacer(modifier = Modifier.height(8.dp))
            PingleText(
              text = "Select play to start spinning",
              style = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.2f)
              )
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          itemsIndexed(scores) { index, score ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .border(
                  width = 1.dp,
                  color = Color.White.copy(alpha = 0.08f),
                  shape = RoundedCornerShape(12.dp)
                )
                .background(
                  Color.White.copy(alpha = 0.02f),
                  shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 20.dp, vertical = 16.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
              ) {
                PingleText(
                  text = "#${index + 1}",
                  style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (index) {
                      0 -> Color(0xFFFFD93D)
                      1 -> Color(0xFFC0C0C0)
                      2 -> Color(0xFFCD7F32)
                      else -> Color.White.copy(alpha = 0.3f)
                    }
                  )
                )

                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  PingleText(
                    text = formatDuration(score.durationMs),
                    style = TextStyle(
                      fontFamily = FontFamily.SansSerif,
                      fontSize = 18.sp,
                      fontWeight = FontWeight.Medium,
                      color = Color.White.copy(alpha = 0.9f),
                      fontFeatureSettings = "tnum"
                    )
                  )
                  if (score.isManual) {
                    Box(
                      modifier = Modifier
                        .background(Color(0xFFFFD93D).copy(alpha = 0.12f), shape = RoundedCornerShape(4.dp))
                        .border(0.5.dp, Color(0xFFFFD93D).copy(alpha = 0.4f), shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                      PingleText(
                        text = "pms",
                        style = TextStyle(
                          fontFamily = FontFamily.SansSerif,
                          fontSize = 8.sp,
                          fontWeight = FontWeight.Bold,
                          color = Color(0xFFFFD93D),
                          letterSpacing = 0.5.sp
                        )
                      )
                    }
                  }
                  if (score.isDebug) {
                    Box(
                      modifier = Modifier
                        .background(Color(0xFFFF453A).copy(alpha = 0.12f), shape = RoundedCornerShape(4.dp))
                        .border(0.5.dp, Color(0xFFFF453A).copy(alpha = 0.4f), shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                      PingleText(
                        text = "db",
                        style = TextStyle(
                          fontFamily = FontFamily.SansSerif,
                          fontSize = 8.sp,
                          fontWeight = FontWeight.Bold,
                          color = Color(0xFFFF453A),
                          letterSpacing = 0.5.sp
                        )
                      )
                    }
                  }
                }
              }

              PingleText(
                text = formatTimestamp(score.timestamp),
                style = TextStyle(
                  fontFamily = FontFamily.SansSerif,
                  fontSize = 11.sp,
                  color = Color.White.copy(alpha = 0.3f)
                )
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      if (scores.isNotEmpty()) {
        PingleText(
          text = "WARNING: DELETING SCORES IS PERMANENT",
          style = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Red.copy(alpha = 0.5f),
            letterSpacing = 1.sp
          ),
          modifier = Modifier.padding(bottom = 12.dp)
        )
      }

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Box(
          modifier = Modifier
            .weight(1.2f)
            .height(56.dp)
            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
            .background(Color.Black, shape = RoundedCornerShape(14.dp))
            .clickable { onBackClicked() },
          contentAlignment = Alignment.Center
        ) {
          PingleText(
            text = "BACK",
            style = TextStyle(
              fontFamily = FontFamily.SansSerif,
              fontSize = 14.sp,
              fontWeight = FontWeight.Medium,
              color = Color.White,
              letterSpacing = 2.sp
            )
          )
        }

        if (scores.isNotEmpty()) {
          Box(
            modifier = Modifier
              .weight(1f)
              .height(56.dp)
              .border(1.dp, Color.Red.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
              .background(Color.Black, shape = RoundedCornerShape(14.dp))
              .clickable { onClearAll() },
            contentAlignment = Alignment.Center
          ) {
            PingleText(
              text = "CLEAR ALL",
              style = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Red.copy(alpha = 0.7f),
                letterSpacing = 1.sp
              )
            )
          }
        }
      }
    }
  }
}

@Composable
fun OptionsScreenOld(
  speed: Float,
  pingleFriction: Float,
  isManualUnlocked: Boolean,
  pingleTintId: String,
  pingleCustomColorInt: Int,
  totalSpinDuration: Long,
  foldAngleThreshold: Float,
  useCustomImage: Boolean,
  pingleCustomImageUri: String?,
  onSpeedChange: (Float) -> Unit,
  onFrictionChange: (Float) -> Unit,
  onTintSelect: (String) -> Unit,
  onCustomColorChange: (Int) -> Unit,
  onFoldAngleThresholdChange: (Float) -> Unit,
  onCustomImageUriChange: (String?) -> Unit,
  onUseCustomImageChange: (Boolean) -> Unit,
  onBackClicked: () -> Unit
) {
  val context = LocalContext.current
  val hours = totalSpinDuration / 3600000L
  val minutes = (totalSpinDuration % 3600000L) / 60000L
  val seconds = (totalSpinDuration % 60000L) / 1000L
  val progressText = String.format(Locale.US, "%d:%02d:%02d / 1:00:00", hours, minutes, seconds)

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black)
      .statusBarsPadding()
      .navigationBarsPadding()
      .testTag("options_screen")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 32.dp, vertical = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .padding(top = 16.dp)
      ) {
        PingleText(
          text = "OPTIONS",
          style = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraLight,
            color = Color.White,
            letterSpacing = 6.sp
          )
        )
        Spacer(modifier = Modifier.height(6.dp))
        PingleText(
          text = "SYSTEM CONFIGURATION",
          style = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.4f),
            letterSpacing = 2.sp
          )
        )

        // SENSATIONAL rotating preview pingle showing what it looks like ingame
        val selectedTintOption = remember(pingleTintId) { PINGLE_TINTS.firstOrNull { it.id == pingleTintId } }
        val tintColor = remember(pingleTintId, pingleCustomColorInt) {
          if (pingleTintId == "custom") Color(pingleCustomColorInt) else selectedTintOption?.color
        }
        val colorFilter = remember(tintColor) { tintColor?.let { ColorFilter.tint(it, BlendMode.Color) } }

        var previewRotation by remember { mutableFloatStateOf(0f) }
        LaunchedEffect(speed) {
          var lastTime = System.nanoTime()
          while (isActive) {
            withFrameMillis {
              val now = System.nanoTime()
              val deltaSeconds = (now - lastTime) / 1_000_000_000f
              lastTime = now
              val speedFactor = speed * 120f // multiplier for rotation speed
              previewRotation = (previewRotation + deltaSeconds * speedFactor) % 360f
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Box(
          modifier = Modifier
            .size(110.dp)
            .border(
              width = 1.dp,
              brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = 0.2f), Color.Transparent)
              ),
              shape = androidx.compose.foundation.shape.CircleShape
            )
            .background(
              brush = Brush.radialGradient(
                colors = listOf(
                  tintColor?.copy(alpha = 0.15f) ?: Color.White.copy(alpha = 0.05f),
                  Color.Transparent
                )
              ),
              shape = androidx.compose.foundation.shape.CircleShape
            ),
          contentAlignment = Alignment.Center
        ) {
          PingleImage(
            useCustomImage = useCustomImage,
            customImageUri = pingleCustomImageUri,
            contentDescription = "Pingle Ingame Preview",
            modifier = Modifier
              .size(76.dp)
              .graphicsLayer(rotationZ = previewRotation)
              .testTag("pingle_preview"),
            contentScale = ContentScale.Fit,
            colorFilter = colorFilter
          )
        }
      }

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .padding(vertical = 12.dp)
          .verticalScroll(androidx.compose.foundation.rememberScrollState())
          .border(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.08f),
            shape = RoundedCornerShape(16.dp)
          )
          .background(Color.White.copy(alpha = 0.01f), shape = RoundedCornerShape(16.dp))
          .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
      ) {
        // Pingle Speed Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          PingleText(
            text = "pinglespeed".uppercase(Locale.US),
            style = TextStyle(
              fontFamily = FontFamily.SansSerif,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White.copy(alpha = 0.4f),
              letterSpacing = 3.sp
            )
          )
          PingleText(
            text = String.format(Locale.US, "%.1fx", speed),
            style = TextStyle(
              fontFamily = FontFamily.SansSerif,
              fontSize = 16.sp,
              fontWeight = FontWeight.Medium,
              color = Color.White,
              fontFeatureSettings = "tnum"
            )
          )
        }

        // Pingle Speed Slider
        Slider(
          value = speed,
          onValueChange = onSpeedChange,
          valueRange = 0.5f..10.0f,
          colors = SliderDefaults.colors(
            thumbColor = Color.White,
            activeTrackColor = Color.White,
            inactiveTrackColor = Color.White.copy(alpha = 0.2f)
          ),
          modifier = Modifier.fillMaxWidth().testTag("pingle_speed_slider")
        )

        // If manual is unlocked, show pingle friction slider!
        if (isManualUnlocked) {
          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            PingleText(
              text = "pinglefriction".uppercase(Locale.US),
              style = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.4f),
                letterSpacing = 3.sp
              )
            )
            PingleText(
              text = String.format(Locale.US, "%.0f%%", pingleFriction),
              style = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                fontFeatureSettings = "tnum"
              )
            )
          }

          Slider(
            value = pingleFriction,
            onValueChange = onFrictionChange,
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(
              thumbColor = Color.White,
              activeTrackColor = Color.White,
              inactiveTrackColor = Color.White.copy(alpha = 0.2f)
            ),
            modifier = Modifier.fillMaxWidth().testTag("pingle_friction_slider")
          )
        }

        // If device is foldable, show fold angle slider!
        val isFoldable = isFoldableDevice()
        if (isFoldable) {
          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            PingleText(
              text = "fold angle".uppercase(Locale.US),
              style = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD93D),
                letterSpacing = 3.sp
              )
            )
            PingleText(
              text = String.format(Locale.US, "%.0f°", foldAngleThreshold),
              style = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                fontFeatureSettings = "tnum"
              )
            )
          }

          Slider(
            value = foldAngleThreshold,
            onValueChange = onFoldAngleThresholdChange,
            valueRange = 0f..180f,
            colors = SliderDefaults.colors(
              thumbColor = Color(0xFFFFD93D),
              activeTrackColor = Color(0xFFFFD93D),
              inactiveTrackColor = Color.White.copy(alpha = 0.2f)
            ),
            modifier = Modifier.fillMaxWidth().testTag("pingle_fold_angle_slider")
          )

          PingleText(
            text = "Triggers tabletop split layout when folded below this angle.",
            style = TextStyle(
              fontFamily = FontFamily.SansSerif,
              fontSize = 10.sp,
              color = Color.White.copy(alpha = 0.5f)
            ),
            modifier = Modifier.padding(top = 4.dp)
          )
        }

        // Tint Selector block (depends on 1-hour total spin duration)
        Spacer(modifier = Modifier.height(12.dp))
        if (totalSpinDuration >= 3600000L) {
          val isCustomUnlocked = totalSpinDuration >= 18000000L // 5 hours in ms
          val isCustomSelected = pingleTintId == "custom"
          Column {
            PingleText(
              text = "PINGLE TINT",
              style = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.4f),
                letterSpacing = 3.sp
              ),
              modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              P_TINTS_TAG@ for (tint in PINGLE_TINTS) {
                val isSelected = pingleTintId == tint.id
                Box(
                  modifier = Modifier
                    .size(width = 44.dp, height = 36.dp)
                    .border(
                      width = if (isSelected) 2.dp else 1.dp,
                      color = if (isSelected) Color.White else Color.White.copy(alpha = 0.15f),
                      shape = RoundedCornerShape(8.dp)
                    )
                    .background(
                      color = tint.color?.copy(alpha = 0.2f) ?: Color.White.copy(alpha = 0.05f),
                      shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onTintSelect(tint.id) }
                    .testTag("tint_${tint.id}"),
                  contentAlignment = Alignment.Center
                ) {
                  Box(
                    modifier = Modifier
                      .size(12.dp)
                      .background(
                        color = tint.color ?: Color.White,
                        shape = androidx.compose.foundation.shape.CircleShape
                      )
                  )
                }
              }

              // Custom Color Circle (unlocked at 5 hours)
              val rainbowBrush = remember {
                Brush.sweepGradient(
                  colors = listOf(
                    Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red
                  )
                )
              }
              Box(
                modifier = Modifier
                  .size(width = 44.dp, height = 36.dp)
                  .border(
                    width = if (isCustomSelected) 2.dp else 1.dp,
                    color = if (isCustomSelected) Color.White else Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                  )
                  .background(
                    if (isCustomUnlocked) Color(pingleCustomColorInt).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.03f),
                    shape = RoundedCornerShape(8.dp)
                  )
                  .clickable {
                    if (isCustomUnlocked) {
                      onTintSelect("custom")
                    } else {
                      val remainingMs = 18000000L - totalSpinDuration
                      val remainingHours = remainingMs / 3600000L
                      val remainingMinutes = (remainingMs % 3600000L) / 60000L
                      showToast(
                        context,
                        String.format(Locale.US, "Custom Colors Lock: Spin for another %d hr %d min to unlock!", remainingHours, remainingMinutes),
                        Toast.LENGTH_SHORT
                      )
                    }
                  }
                  .testTag("tint_custom"),
                contentAlignment = Alignment.Center
              ) {
                if (isCustomUnlocked) {
                  Box(
                    modifier = Modifier
                      .size(14.dp)
                      .border(width = 1.5.dp, brush = rainbowBrush, shape = androidx.compose.foundation.shape.CircleShape)
                      .padding(1.5.dp)
                  ) {
                    Box(
                      modifier = Modifier
                        .fillMaxSize()
                        .background(Color(pingleCustomColorInt), shape = androidx.compose.foundation.shape.CircleShape)
                    )
                  }
                } else {
                  Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Custom Locked",
                    tint = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size(14.dp)
                  )
                }
              }
            }

            if (!isCustomUnlocked) {
              Spacer(modifier = Modifier.height(10.dp))
              val customHours = totalSpinDuration / 3600000L
              val customMinutes = (totalSpinDuration % 3600000L) / 60000L
              val customSeconds = (totalSpinDuration % 60000L) / 1000L
              val progressTextCustom = String.format(Locale.US, "%d:%02d:%02d / 5:00:00", customHours, customMinutes, customSeconds)
              PingleText(
                text = "🔒 Custom Colors Lock in Progress: $progressTextCustom",
                style = TextStyle(
                  fontFamily = FontFamily.SansSerif,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFFFFD93D).copy(alpha = 0.8f),
                  fontFeatureSettings = "tnum"
                )
              )
            } else if (isCustomSelected) {
              Spacer(modifier = Modifier.height(16.dp))
              
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                PingleText(
                  text = "CUSTOM_COLOR".uppercase(Locale.US),
                  style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.4f),
                    letterSpacing = 2.sp
                  )
                )

                // Beautifully designed Hex Code and Colored Swatch
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  PingleText(
                    text = String.format(Locale.US, "#%06X", pingleCustomColorInt and 0xFFFFFF),
                    style = TextStyle(
                      fontFamily = FontFamily.Monospace,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color(pingleCustomColorInt)
                    )
                  )
                  Box(
                    modifier = Modifier
                      .size(24.dp)
                      .background(Color(pingleCustomColorInt), shape = RoundedCornerShape(6.dp))
                      .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                  )
                }
              }

              Spacer(modifier = Modifier.height(12.dp))

              var r by remember(pingleCustomColorInt) { mutableFloatStateOf(((pingleCustomColorInt shr 16) and 0xFF).toFloat()) }
              var g by remember(pingleCustomColorInt) { mutableFloatStateOf(((pingleCustomColorInt shr 8) and 0xFF).toFloat()) }
              var b by remember(pingleCustomColorInt) { mutableFloatStateOf((pingleCustomColorInt and 0xFF).toFloat()) }

              val redGradient = remember(g, b) {
                Brush.horizontalGradient(
                  colors = listOf(Color(0, g.toInt(), b.toInt()), Color(255, g.toInt(), b.toInt()))
                )
              }
              val greenGradient = remember(r, b) {
                Brush.horizontalGradient(
                  colors = listOf(Color(r.toInt(), 0, b.toInt()), Color(r.toInt(), 255, b.toInt()))
                )
              }
              val blueGradient = remember(r, g) {
                Brush.horizontalGradient(
                  colors = listOf(Color(r.toInt(), g.toInt(), 0), Color(r.toInt(), g.toInt(), 255))
                )
              }

              Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                FancyRGBSlider(
                  label = "R",
                  value = r,
                  gradient = redGradient,
                  thumbColor = Color(0xFFFF453A),
                  onValueChange = {
                    r = it
                    val newColor = (0xFF000000L or (r.toLong() shl 16) or (g.toLong() shl 8) or b.toLong()).toInt()
                    onCustomColorChange(newColor)
                  },
                  valueText = String.format(Locale.US, "%d", r.toInt())
                )

                FancyRGBSlider(
                  label = "G",
                  value = g,
                  gradient = greenGradient,
                  thumbColor = Color(0xFF30D158),
                  onValueChange = {
                    g = it
                    val newColor = (0xFF000000L or (r.toLong() shl 16) or (g.toLong() shl 8) or b.toLong()).toInt()
                    onCustomColorChange(newColor)
                  },
                  valueText = String.format(Locale.US, "%d", g.toInt())
                )

                FancyRGBSlider(
                  label = "B",
                  value = b,
                  gradient = blueGradient,
                  thumbColor = Color(0xFF0A84FF),
                  onValueChange = {
                    b = it
                    val newColor = (0xFF000000L or (r.toLong() shl 16) or (g.toLong() shl 8) or b.toLong()).toInt()
                    onCustomColorChange(newColor)
                  },
                  valueText = String.format(Locale.US, "%d", b.toInt())
                )
              }
            }
          }
        } else {
          Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
          ) {
            PingleText(
              text = "🔒 PINGLE TINT (Locked)",
              style = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.4f),
                letterSpacing = 3.sp
              )
            )

            Spacer(modifier = Modifier.height(4.dp))

            PingleText(
              text = "Reach 1 Hour total spin time to unlock dynamic custom tints!",
              style = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.5f)
              )
            )

            Spacer(modifier = Modifier.height(4.dp))

            PingleText(
              text = "Progress: $progressText",
              style = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD93D),
                fontFeatureSettings = "tnum"
              )
            )
          }
        }

        // --- CUSTOM PINGLE IMAGE SECTION ---
        if (totalSpinDuration < 2000L) {
          // Locked State
          Spacer(modifier = Modifier.height(16.dp))
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .border(width = 1.dp, color = Color.White.copy(alpha = 0.08f), shape = RoundedCornerShape(12.dp))
              .background(Color.White.copy(alpha = 0.01f), shape = RoundedCornerShape(12.dp))
              .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(imageVector = Icons.Default.Lock, contentDescription = "Locked", tint = Color(0xFFFFD93D), modifier = Modifier.size(16.dp))
              PingleText(
                text = "CUSTOM PINGLE IMAGE",
                style = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.8f))
              )
            }
            Spacer(modifier = Modifier.height(8.dp))
            PingleText(
              text = "Spin for at least 2 seconds in total to unlock the ability to upload a custom image and replace the Pringle!",
              style = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f)),
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            PingleText(
              text = String.format(Locale.US, "Progress: %.1fs / 2.0s", (totalSpinDuration / 1000f).coerceAtMost(2.0f)),
              style = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD93D))
            )
          }
        } else {
          // Unlocked State
          Spacer(modifier = Modifier.height(16.dp))
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .border(width = 1.dp, color = Color(0xFFFFD93D).copy(alpha = 0.3f), shape = RoundedCornerShape(12.dp))
              .background(Color.White.copy(alpha = 0.02f), shape = RoundedCornerShape(12.dp))
              .padding(16.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Unlocked",
                tint = Color(0xFFFFD93D),
                modifier = Modifier.size(18.dp)
              )
              PingleText(
                text = "CUSTOM PINGLE IMAGE (UNLOCKED!)",
                style = TextStyle(
                  fontFamily = FontFamily.SansSerif,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFFFFD93D)
                )
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Toggle / Switch Row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              PingleText(
                text = "Use Custom Image",
                style = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 12.sp, color = Color.White)
              )
              androidx.compose.material3.Switch(
                checked = useCustomImage,
                onCheckedChange = onUseCustomImageChange,
                colors = androidx.compose.material3.SwitchDefaults.colors(
                  checkedThumbColor = Color(0xFFFFD93D),
                  checkedTrackColor = Color(0xFFFFD93D).copy(alpha = 0.5f),
                  uncheckedThumbColor = Color.White,
                  uncheckedTrackColor = Color.White.copy(alpha = 0.2f)
                )
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Image preview and pick button
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Mini Thumbnail
              Box(
                modifier = Modifier
                  .size(64.dp)
                  .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                  .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
              ) {
                if (pingleCustomImageUri != null) {
                  val previewPainter = coil.compose.rememberAsyncImagePainter(model = pingleCustomImageUri)
                  Image(
                    painter = previewPainter,
                    contentDescription = "Custom Image Thumbnail",
                    modifier = Modifier.size(56.dp),
                    contentScale = ContentScale.Fit
                  )
                } else {
                  Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "No Image",
                    tint = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(24.dp)
                  )
                }
              }

              // Buttons Column
              Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                val launcher = rememberLauncherForActivityResult(
                  contract = ActivityResultContracts.GetContent()
                ) { uri ->
                  if (uri != null) {
                    try {
                      val contentResolver = context.contentResolver
                      val inputStream = contentResolver.openInputStream(uri)
                      if (inputStream != null) {
                        val file = java.io.File(context.filesDir, "custom_pingle_image.png")
                        file.outputStream().use { outputStream ->
                          inputStream.use { it.copyTo(outputStream) }
                        }
                        onCustomImageUriChange(file.absolutePath)
                        onUseCustomImageChange(true)
                        showToast(context, "Custom image updated successfully!")
                      }
                    } catch (e: Exception) {
                      android.util.Log.e("MainActivity", "Error copying image", e)
                      showToast(context, "Error loading image: ${e.message}")
                    }
                  }
                }

                MinimalistButton(
                  text = "SELECT CUSTOM IMAGE",
                  onClick = { launcher.launch("image/*") },
                  modifier = Modifier.fillMaxWidth()
                )

                if (pingleCustomImageUri != null) {
                  MinimalistButton(
                    text = "RESET TO DEFAULT",
                    onClick = {
                      onCustomImageUriChange(null)
                      onUseCustomImageChange(false)
                      showToast(context, "Reset to default Pringle.")
                    },
                    modifier = Modifier.fillMaxWidth()
                  )
                }
              }
            }
          }
        }

        // --- SPOTIFY INTEGRATION SECTION ---
        Spacer(modifier = Modifier.height(16.dp))
        val spotifyManager = remember { SpotifyManager.getInstance(context) }
        val spotifyClientId by spotifyManager.clientId.collectAsState()
        val spotifyClientSecret by spotifyManager.clientSecret.collectAsState()
        val spotifyUsername by spotifyManager.username.collectAsState()
        val spotifyPassword by spotifyManager.password.collectAsState()
        val spotifyIsSimulated by spotifyManager.isSimulated.collectAsState()
        val spotifyIsConnected by spotifyManager.isConnected.collectAsState()
        val spotifyIsLoading by spotifyManager.isLoading.collectAsState()
        val spotifyError by spotifyManager.error.collectAsState()

        var editClientId by remember(spotifyClientId) { mutableStateOf(spotifyClientId) }
        var editClientSecret by remember(spotifyClientSecret) { mutableStateOf(spotifyClientSecret) }
        var editUsername by remember(spotifyUsername) { mutableStateOf(spotifyUsername) }
        var editPassword by remember(spotifyPassword) { mutableStateOf(spotifyPassword) }
        var loginMethod by remember { mutableStateOf(if (spotifyClientId.isNotEmpty()) "developer" else "simple") } // "simple" or "developer"
        var showLoginDialog by remember { mutableStateOf(false) }
        val scope = androidx.compose.runtime.rememberCoroutineScope()

        if (totalSpinDuration < 600000L) {
          // Locked State
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .border(width = 1.dp, color = Color.White.copy(alpha = 0.08f), shape = RoundedCornerShape(12.dp))
              .background(Color.White.copy(alpha = 0.01f), shape = RoundedCornerShape(12.dp))
              .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(imageVector = Icons.Default.Lock, contentDescription = "Locked", tint = Color(0xFFFFD93D), modifier = Modifier.size(16.dp))
              PingleText(
                text = "SPOTIFY INTEGRATION",
                style = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.8f))
              )
            }
            Spacer(modifier = Modifier.height(8.dp))
            PingleText(
              text = "Unlock Spotify integration after 10 minutes of spin! Connect your account and listen to a random song from a random one of your playlists directly inside the app while spinning.",
              style = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f)),
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            val currentMins = (totalSpinDuration / 60000L).coerceAtMost(10L)
            val currentSecs = ((totalSpinDuration % 60000L) / 1000L).coerceAtMost(59L)
            PingleText(
              text = String.format(Locale.US, "Progress: %d:%02d / 10:00", currentMins, currentSecs),
              style = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD93D))
            )
          }
        } else {
          // Unlocked State
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .border(width = 1.dp, color = Color(0xFF1DB954).copy(alpha = 0.3f), shape = RoundedCornerShape(12.dp))
              .background(Color.White.copy(alpha = 0.02f), shape = RoundedCornerShape(12.dp))
              .padding(16.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = "Spotify",
                tint = Color(0xFF1DB954),
                modifier = Modifier.size(18.dp)
              )
              PingleText(
                text = "SPOTIFY INTEGRATION (UNLOCKED!)",
                style = TextStyle(
                  fontFamily = FontFamily.SansSerif,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF1DB954)
                )
              )
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            if (!spotifyIsConnected) {
              // Toggle tabs
              Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .clickable { loginMethod = "simple" }
                    .border(
                      width = 1.dp,
                      color = if (loginMethod == "simple") Color(0xFF1DB954) else Color.White.copy(alpha = 0.15f),
                      shape = RoundedCornerShape(8.dp)
                    )
                    .background(
                      if (loginMethod == "simple") Color(0xFF1DB954).copy(alpha = 0.1f) else Color.Transparent,
                      shape = RoundedCornerShape(8.dp)
                    )
                    .padding(vertical = 8.dp),
                  contentAlignment = Alignment.Center
                ) {
                  PingleText(
                    text = "Standard Login",
                    style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (loginMethod == "simple") Color(0xFF1DB954) else Color.White.copy(alpha = 0.6f))
                  )
                }

                Box(
                  modifier = Modifier
                    .weight(1f)
                    .clickable { loginMethod = "developer" }
                    .border(
                      width = 1.dp,
                      color = if (loginMethod == "developer") Color(0xFF1DB954) else Color.White.copy(alpha = 0.15f),
                      shape = RoundedCornerShape(8.dp)
                    )
                    .background(
                      if (loginMethod == "developer") Color(0xFF1DB954).copy(alpha = 0.1f) else Color.Transparent,
                      shape = RoundedCornerShape(8.dp)
                    )
                    .padding(vertical = 8.dp),
                  contentAlignment = Alignment.Center
                ) {
                  PingleText(
                    text = "Developer Login",
                    style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (loginMethod == "developer") Color(0xFF1DB954) else Color.White.copy(alpha = 0.6f))
                  )
                }
              }

              Spacer(modifier = Modifier.height(8.dp))

              if (loginMethod == "simple") {
                PingleText(
                  text = "Login with your Spotify account credentials:",
                  style = TextStyle(fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                  value = editUsername,
                  onValueChange = { editUsername = it },
                  label = { PingleText("Spotify Username or Email", color = Color.White.copy(alpha = 0.5f)) },
                  textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                  modifier = Modifier.fillMaxWidth(),
                  colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Black,
                    unfocusedContainerColor = Color.Black,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                  )
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                OutlinedTextField(
                  value = editPassword,
                  onValueChange = { editPassword = it },
                  label = { PingleText("Spotify Password", color = Color.White.copy(alpha = 0.5f)) },
                  visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                  textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                  modifier = Modifier.fillMaxWidth(),
                  colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Black,
                    unfocusedContainerColor = Color.Black,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                  )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                if (spotifyIsLoading) {
                  PingleText(
                    text = "Connecting & Authenticating...",
                    style = TextStyle(fontSize = 11.sp, color = Color(0xFFFFD93D))
                  )
                } else {
                  MinimalistButton(
                    text = "CONNECT SPOTIFY ACCOUNT",
                    onClick = {
                      if (editUsername.isBlank() || editPassword.isBlank()) {
                        showToast(context, "Please enter both Username and Password!")
                      } else {
                        spotifyManager.loginWithUsernamePassword(editUsername, editPassword)
                        showToast(context, "Successfully authenticated with Spotify!")
                      }
                    },
                    modifier = Modifier.fillMaxWidth()
                  )
                }
              } else {
                PingleText(
                  text = "Setup your Spotify Developer Credentials to connect your account:",
                  style = TextStyle(fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                  value = editClientId,
                  onValueChange = { editClientId = it },
                  label = { PingleText("Spotify Client ID", color = Color.White.copy(alpha = 0.5f)) },
                  textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                  modifier = Modifier.fillMaxWidth(),
                  colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Black,
                    unfocusedContainerColor = Color.Black,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                  )
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                OutlinedTextField(
                  value = editClientSecret,
                  onValueChange = { editClientSecret = it },
                  label = { PingleText("Spotify Client Secret", color = Color.White.copy(alpha = 0.5f)) },
                  textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                  modifier = Modifier.fillMaxWidth(),
                  colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Black,
                    unfocusedContainerColor = Color.Black,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                  )
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                PingleText(
                  text = "How to connect:\n1. Visit developer.spotify.com & log in\n2. Create an App (name: PingleSpin)\n3. Edit Settings: set Redirect URI to:\n    https://localhost/callback\n4. Copy Client ID & Client Secret here and click Connect!",
                  style = TextStyle(fontSize = 9.sp, color = Color.White.copy(alpha = 0.4f), lineHeight = 12.sp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                if (spotifyIsLoading) {
                  PingleText(
                    text = "Connecting & Authenticating...",
                    style = TextStyle(fontSize = 11.sp, color = Color(0xFFFFD93D))
                  )
                } else {
                  MinimalistButton(
                    text = "CONNECT DEVELOPER ACCOUNT",
                    onClick = {
                      if (editClientId.isBlank() || editClientSecret.isBlank()) {
                        showToast(context, "Please enter both Client ID and Client Secret!")
                      } else {
                        spotifyManager.saveCredentials(editClientId, editClientSecret)
                        showLoginDialog = true
                      }
                    },
                    modifier = Modifier.fillMaxWidth()
                  )
                }
              }
            } else {
              val connectedMsg = if (spotifyIsSimulated) {
                "✅ Account Connected (Simulated: $spotifyUsername)!"
              } else {
                "✅ Account Connected Successfully!"
              }
              PingleText(
                text = connectedMsg,
                style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1DB954))
              )
              
              Spacer(modifier = Modifier.height(6.dp))
              
              PingleText(
                text = "While spinning in play modes, you will now see the Spotify Player to stream random songs from random playlists in your library!",
                style = TextStyle(fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
              )
              
              Spacer(modifier = Modifier.height(12.dp))
              
              MinimalistButton(
                text = "DISCONNECT ACCOUNT",
                onClick = {
                  spotifyManager.disconnect()
                  showToast(context, "Disconnected from Spotify.")
                },
                modifier = Modifier.fillMaxWidth()
              )
            }
            
            if (spotifyError != null) {
              Spacer(modifier = Modifier.height(8.dp))
              PingleText(
                text = spotifyError ?: "",
                style = TextStyle(fontSize = 11.sp, color = Color.Red)
              )
            }
          }
        }

        if (showLoginDialog) {
          Dialog(onDismissRequest = { showLoginDialog = false }) {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 40.dp, horizontal = 16.dp)
                .background(Color.Black, shape = RoundedCornerShape(16.dp))
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            ) {
              Column(modifier = Modifier.fillMaxSize()) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  PingleText(
                    text = "Connect Spotify",
                    color = Color.White,
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)
                  )
                  IconButton(onClick = { showLoginDialog = false }) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                  }
                }

                AndroidView(
                  modifier = Modifier.weight(1f),
                  factory = { ctx ->
                    WebView(ctx).apply {
                      settings.javaScriptEnabled = true
                      webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                          view: WebView?,
                          request: WebResourceRequest?
                        ): Boolean {
                          val uri = request?.url ?: return false
                          val uriString = uri.toString()
                          if (uriString.startsWith(SpotifyManager.REDIRECT_URI)) {
                            val code = uri.getQueryParameter("code")
                            if (code != null) {
                              scope.launch {
                                val success = spotifyManager.handleAuthCode(code)
                                if (success) {
                                  showToast(ctx, "Successfully authenticated with Spotify!")
                                }
                                showLoginDialog = false
                              }
                              return true
                            }
                          }
                          return false
                        }
                      }
                      loadUrl(spotifyManager.getAuthorizeUrl())
                    }
                  }
                )
              }
            }
          }
        }
      }

      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        MinimalistButton(
          text = "SAVE & BACK",
          onClick = onBackClicked,
          modifier = Modifier
            .padding(bottom = 24.dp)
            .testTag("save_back_button")
        )
      }
    }
  }
}

@Composable
fun FancyRGBSlider(
  label: String,
  value: Float,
  gradient: Brush,
  thumbColor: Color,
  onValueChange: (Float) -> Unit,
  valueText: String
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(10.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    PingleText(
      text = label,
      style = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = thumbColor
      ),
      modifier = Modifier.width(14.dp)
    )

    Box(
      modifier = Modifier
        .weight(1f)
        .height(32.dp),
      contentAlignment = Alignment.Center
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(8.dp)
          .background(gradient, shape = RoundedCornerShape(4.dp))
          .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
      )
      Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = 0f..255f,
        colors = SliderDefaults.colors(
          thumbColor = Color.White,
          activeTrackColor = Color.Transparent,
          inactiveTrackColor = Color.Transparent,
          activeTickColor = Color.Transparent,
          inactiveTickColor = Color.Transparent
        ),
        modifier = Modifier.fillMaxWidth()
      )
    }

    PingleText(
      text = valueText,
      style = TextStyle(
        color = Color.White.copy(alpha = 0.6f),
        fontSize = 12.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold
      ),
      modifier = Modifier.width(28.dp)
    )
  }
}

@Composable
fun ManualPlayScreen(
  pingleFriction: Float,
  pingleTintId: String,
  pingleCustomColorInt: Int,
  foldAngleThreshold: Float,
  useCustomImage: Boolean,
  pingleCustomImageUri: String?,
  onBackToHomeWithScore: (Long) -> Unit,
  onElapsedClickToHighScores: (Long) -> Unit
) {
  var rotation by remember { mutableFloatStateOf(0f) }
  var velocity by remember { mutableFloatStateOf(0f) }
  var elapsedTimeMs by remember { mutableLongStateOf(0L) }

  val context = LocalContext.current
  DisposableEffect(Unit) {
    onDispose {
      SpotifyManager.getInstance(context).stopPreview()
    }
  }

  val viewModel: PingleViewModel = viewModel()
  val easterRainbowNeon by viewModel.easterRainbowNeon.collectAsState()
  val easterReverseSpin by viewModel.easterReverseSpin.collectAsState()
  val easterSpaceStars by viewModel.easterSpaceStars.collectAsState()
  val easterMatrixBg by viewModel.easterMatrixBg.collectAsState()
  val invisiblePingleEnabledState by viewModel.invisiblePingleEnabled.collectAsState()
  val spinVibrationMode by viewModel.spinVibrationMode.collectAsState()
  val spinVibrationInterval by viewModel.spinVibrationInterval.collectAsState()

  var lastVibratedAngle by remember { mutableFloatStateOf(0f) }
  LaunchedEffect(rotation, spinVibrationInterval, spinVibrationMode) {
    if (spinVibrationMode != "none") {
      val normRotation = (rotation % 360f + 360f) % 360f
      val diff = Math.abs(normRotation - lastVibratedAngle)
      val shortestDiff = if (diff > 180f) 360f - diff else diff
      if (shortestDiff >= spinVibrationInterval) {
        VibrationHelper.vibrate(context, spinVibrationMode)
        lastVibratedAngle = normRotation
      }
    }
  }

  val isFolded = isFoldedState(threshold = foldAngleThreshold)
  val isFlipFoldable = isFlipStyleFoldable()

  val psmLayout by viewModel.psmLayout.collectAsState()
  val unfoldedPsmLayout by viewModel.unfoldedPsmLayout.collectAsState()
  val foldedPsmLayout by viewModel.foldedPsmLayout.collectAsState()

  val activeLayout = remember(isFolded, isFlipFoldable, psmLayout, unfoldedPsmLayout, foldedPsmLayout) {
    when {
      isFlipFoldable && isFolded -> foldedPsmLayout
      isFlipFoldable && !isFolded -> unfoldedPsmLayout
      else -> psmLayout
    }
  }

  val pingleScaleState = activeLayout.pingleScale
  val pingleOffsetXState = activeLayout.pingleOffsetX
  val pingleOffsetYState = activeLayout.pingleOffsetY
  val pingleTiltState = activeLayout.pingleTilt

  val timerScaleState = activeLayout.timerScale
  val timerOffsetXState = activeLayout.timerOffsetX
  val timerOffsetYState = activeLayout.timerOffsetY
  val timerTiltState = activeLayout.timerTilt

  val spotifyScaleState = activeLayout.spotifyScale
  val spotifyOffsetXState = activeLayout.spotifyOffsetX
  val spotifyOffsetYState = activeLayout.spotifyOffsetY
  val spotifyTiltState = activeLayout.spotifyTilt

  val discScaleState = activeLayout.discScale
  val discOffsetXState = activeLayout.discOffsetX
  val discOffsetYState = activeLayout.discOffsetY
  val discTiltState = activeLayout.discTilt


  // Cycle color if easterRainbowNeon is true
  val rainbowColor by if (easterRainbowNeon) {
    val infiniteTransition = rememberInfiniteTransition(label = "rainbow")
    val hue by infiniteTransition.animateFloat(
      initialValue = 0f,
      targetValue = 360f,
      animationSpec = infiniteRepeatable(
        animation = tween(6000, easing = LinearEasing),
        repeatMode = RepeatMode.Restart
      ),
      label = "hue"
    )
    remember(hue) {
      mutableStateOf(Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, 1f, 1f))))
    }
  } else {
    remember { mutableStateOf(Color.Transparent) }
  }

  val selectedTintOption = remember(pingleTintId) { PINGLE_TINTS.firstOrNull { it.id == pingleTintId } }
  val tintColor = remember(pingleTintId, pingleCustomColorInt, easterRainbowNeon, rainbowColor) {
    if (easterRainbowNeon) {
      rainbowColor
    } else if (pingleTintId == "custom") {
      Color(pingleCustomColorInt)
    } else {
      selectedTintOption?.color
    }
  }
  val colorFilter = remember(tintColor) { tintColor?.let { ColorFilter.tint(it, BlendMode.Color) } }

  val isFoldable = isFoldableDevice()

  val currentFriction by androidx.compose.runtime.rememberUpdatedState(pingleFriction)

  // High precision timer/physics loop updating at ~60fps
  LaunchedEffect(isFolded, invisiblePingleEnabledState) {
    var lastTime = System.currentTimeMillis()
    while (isActive) {
      androidx.compose.runtime.withFrameMillis {
        val now = System.currentTimeMillis()
        val dt = (now - lastTime).coerceIn(1, 100)
        lastTime = now

        if (invisiblePingleEnabledState) {
          elapsedTimeMs = 0L
        }

        if (Math.abs(velocity) > 0.05f) {
          val deltaRot = velocity * (dt / 16f)
          rotation = (rotation + (if (easterReverseSpin) -deltaRot else deltaRot)) % 360f
          if (!invisiblePingleEnabledState) {
            elapsedTimeMs += dt
          }
          // Proportional physics decay based on current friction: 0% friction means infinite spin, 100% means immediate stop
          // BUT if isFolded is true, friction does not apply!
          val decay = if (isFolded) 1.0f else (1.0f - (currentFriction / 100f)).coerceIn(0f, 1f)
          velocity *= decay
        } else {
          velocity = 0f
        }
      }
    }
  }

  val totalSeconds = elapsedTimeMs / 1000
  val minutes = (totalSeconds / 60) % 60
  val seconds = totalSeconds % 60
  val centiseconds = (elapsedTimeMs % 1000) / 10

  val formattedTime = if (totalSeconds < 60) {
    String.format(Locale.US, "%d.%02d", seconds, centiseconds)
  } else {
    String.format(Locale.US, "%02d:%02d.%02d", minutes, seconds, centiseconds)
  }

  val transitionProgress by animateFloatAsState(
    targetValue = if (isFolded) 1f else 0f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioLowBouncy,
      stiffness = Spring.StiffnessLow
    ),
    label = "fold_transition"
  )

  Box(
    modifier = Modifier
      .fillMaxSize()
      .pingleBackground(easterSpaceStars, easterMatrixBg)
      .testTag("manual_play_viewport")
  ) {
    // 1.5 Grey Disc Controller (slides up when folded/transitionProgress > 0f)
    if (transitionProgress > 0.01f) {
      var discAngle by remember { mutableFloatStateOf(0f) }

      Box(
        modifier = Modifier
          .align(BiasAlignment(0f, 1.15f - 0.65f * transitionProgress)) // dynamic entry slide up
          .offset(x = discOffsetXState.dp, y = discOffsetYState.dp)
          .graphicsLayer(
            scaleX = discScaleState,
            scaleY = discScaleState,
            rotationZ = discTiltState,
            alpha = transitionProgress
          )
          .size(170.dp)
          .pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
              change.consume()
              
              val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
              val currentPos = change.position
              val previousPos = change.previousPosition
              val currentAngleRad = Math.atan2((currentPos.y - center.y).toDouble(), (currentPos.x - center.x).toDouble())
              val previousAngleRad = Math.atan2((previousPos.y - center.y).toDouble(), (previousPos.x - center.x).toDouble())
              var deltaAngleRad = currentAngleRad - previousAngleRad
              while (deltaAngleRad < -Math.PI) deltaAngleRad += 2 * Math.PI
              while (deltaAngleRad > Math.PI) deltaAngleRad -= 2 * Math.PI

              val deltaAngleDeg = Math.toDegrees(deltaAngleRad).toFloat()

              discAngle = (discAngle + deltaAngleDeg) % 360f
              rotation = (rotation + deltaAngleDeg) % 360f

              // Direct velocity bump with zero friction decay
              velocity += deltaAngleDeg * 0.5f
              velocity = velocity.coerceIn(-80f, 80f)
            }
          },
        contentAlignment = Alignment.Center
      ) {
        // Flat grey disc (CD) body
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFB0B0B0), androidx.compose.foundation.shape.CircleShape)
            .border(2.dp, Color(0xFF8E8E8E), androidx.compose.foundation.shape.CircleShape)
        ) {
          androidx.compose.foundation.Canvas(
            modifier = Modifier
              .fillMaxSize()
              .graphicsLayer(rotationZ = discAngle)
          ) {
            val r = size.minDimension / 2f
            val ctr = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)

            // CD shine reflection stroke (white with alpha)
            drawCircle(
              color = Color.White.copy(alpha = 0.15f),
              radius = r * 0.85f,
              style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
            )

            // Red line from the middle to the end
            drawLine(
              color = Color(0xFFFF3B30),
              start = androidx.compose.ui.geometry.Offset(ctr.x + r * 0.25f, ctr.y),
              end = androidx.compose.ui.geometry.Offset(ctr.x + r, ctr.y),
              strokeWidth = 6f,
              cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
          }
        }

        // Black circle in the middle (looks like a CD spindle hole)
        Box(
          modifier = Modifier
            .size(42.dp)
            .background(Color.Black, androidx.compose.foundation.shape.CircleShape)
            .border(1.5.dp, Color(0xFF6E6E6E), androidx.compose.foundation.shape.CircleShape)
        )
      }
    }

    // 1. Centered Spinning Pringle unit moving smoothly with transition progress
    Column(
      modifier = Modifier
        .align(BiasAlignment(0f, -0.5f * transitionProgress))
        .offset(x = pingleOffsetXState.dp, y = pingleOffsetYState.dp)
        .graphicsLayer(
          scaleX = pingleScaleState,
          scaleY = pingleScaleState,
          rotationZ = pingleTiltState
        ),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      val pringleContainerSize = (320f - 40f * transitionProgress).dp
      val imageSize = (260f - 40f * transitionProgress).dp

      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
          .size(pringleContainerSize)
          .pointerInput(pingleFriction) {
            detectDragGestures { change, dragAmount ->
              change.consume()
              val horizontalSpeed = dragAmount.x
              val verticalSpeed = dragAmount.y
              val magnitude = horizontalSpeed + verticalSpeed
              val frictionFactor = pingleFriction / 100f
              val momentumFactor = (1.0f - frictionFactor).coerceIn(0f, 1f)
              velocity += magnitude * 0.15f * momentumFactor * 1.5f
              velocity = velocity.coerceIn(-80f, 80f)
            }
          }
      ) {
        Box(
          modifier = Modifier
            .size(pringleContainerSize, (200f - 20f * transitionProgress).dp)
            .background(
              Brush.radialGradient(
                colors = listOf(
                  Color(0x22FFD93D),
                  Color(0x00FFD93D)
                )
              )
            )
        )

        PingleImage(
          useCustomImage = useCustomImage,
          customImageUri = pingleCustomImageUri,
          contentDescription = "Manual Spinning Pringle",
          modifier = Modifier
            .size(imageSize)
            .graphicsLayer(rotationZ = rotation)
            .testTag("manual_spinning_pringle"),
          contentScale = ContentScale.Fit,
          colorFilter = colorFilter
        )
      }


    }

    // 2. Header Row (Timer, Sensing Active Badge, Check Button) sliding down/up
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val headerTopPadding = (statusBarPadding.value * (1f - transitionProgress)).dp
    val headerHorizontalPadding = (24f - 8f * transitionProgress).dp
    val headerVerticalPadding = (20f - 12f * transitionProgress).dp

    Box(
      modifier = Modifier
        .align(BiasAlignment(0f, -1.0f + 1.5f * transitionProgress))
        .offset(x = timerOffsetXState.dp, y = timerOffsetYState.dp)
        .graphicsLayer(
          scaleX = timerScaleState,
          scaleY = timerScaleState,
          rotationZ = timerTiltState
        )
        .padding(top = headerTopPadding)
        .padding(horizontal = headerHorizontalPadding, vertical = headerVerticalPadding)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Timer
        val timerSize = (24f + 4f * transitionProgress).sp
        val timerWeight = if (transitionProgress > 0.5f) FontWeight.SemiBold else FontWeight.Medium
        val timerAlpha = 0.85f + 0.05f * transitionProgress
        PingleText(
          text = formattedTime,
          style = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontSize = timerSize,
            fontWeight = timerWeight,
            color = Color.White.copy(alpha = timerAlpha),
            letterSpacing = (-0.5).sp,
            fontFeatureSettings = "tnum"
          ),
          modifier = Modifier
            .clickable { onElapsedClickToHighScores(elapsedTimeMs) }
            .burnInProtection(enableShine = true)
            .testTag("elapsed_timer")
        )

        // Stop & Save
        val buttonSize = (48f + 4f * transitionProgress).dp
        val iconSize = (24f + 2f * transitionProgress).dp
        Box(
          modifier = Modifier
            .size(buttonSize)
            .border(
              width = 1.dp,
              color = Color.White.copy(alpha = 0.15f),
              shape = RoundedCornerShape(12.dp)
            )
            .background(Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(12.dp))
            .clickable { onBackToHomeWithScore(elapsedTimeMs) }
            .testTag("stop_save_button"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Stop & Save",
            tint = Color.White,
            modifier = Modifier
              .size(iconSize)
              .burnInProtection(enableShine = true)
          )
        }
      }
    }

    // 3. Spotify Widget remains anchored beautifully at the bottom
    Box(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .navigationBarsPadding()
        .padding(bottom = 16.dp)
        .offset(x = spotifyOffsetXState.dp, y = spotifyOffsetYState.dp)
        .graphicsLayer(
          scaleX = spotifyScaleState,
          scaleY = spotifyScaleState,
          rotationZ = spotifyTiltState
        )
    ) {
      SpotifyPlayerWidget()
    }
  }
}

@Composable
fun PingleBackgroundWrapper(
  easterSpaceStars: Boolean,
  easterMatrixBg: Boolean,
  content: @Composable () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .pingleBackground(easterSpaceStars, easterMatrixBg)
  ) {
    content()
  }
}

@Composable
fun Modifier.pingleBackground(spaceStars: Boolean, matrixBg: Boolean): Modifier {
  if (spaceStars) {
    val infiniteTransition = rememberInfiniteTransition(label = "starfield")
    val twinkle by infiniteTransition.animateFloat(
      initialValue = 0.2f,
      targetValue = 1f,
      animationSpec = infiniteRepeatable(
        animation = tween(1500, easing = LinearEasing),
        repeatMode = RepeatMode.Reverse
      ),
      label = "twinkle"
    )
    return this.drawBehind {
      drawRect(Color.Black)
      val random = java.util.Random(42)
      for (i in 0 until 60) {
        val x = random.nextFloat() * this.size.width
        val y = random.nextFloat() * this.size.height
        val starSize = random.nextFloat() * 2.5f + 1f
        val starAlpha = (random.nextFloat() * 0.5f + 0.5f) * twinkle
        drawCircle(
          color = Color.White.copy(alpha = starAlpha),
          radius = starSize,
          center = androidx.compose.ui.geometry.Offset(x, y)
        )
      }
    }
  } else if (matrixBg) {
    val infiniteTransition = rememberInfiniteTransition(label = "matrix")
    val progress by infiniteTransition.animateFloat(
      initialValue = 0f,
      targetValue = 1f,
      animationSpec = infiniteRepeatable(
        animation = tween(10000, easing = LinearEasing),
        repeatMode = RepeatMode.Restart
      ),
      label = "progress"
    )
    return this.drawBehind {
      drawRect(Color.Black)
      val columns = (this.size.width / 40f).toInt().coerceAtLeast(10)
      val random = java.util.Random(99)
      for (col in 0 until columns) {
        val speed = random.nextFloat() * 0.5f + 0.5f
        val startOffset = random.nextFloat() * this.size.height
        val colX = col * 40f

        val currentY = (startOffset + progress * this.size.height * speed) % this.size.height

        for (trail in 0 until 12) {
          val dotY = (currentY - trail * 22f + this.size.height) % this.size.height
          val alpha = (1f - trail / 12f).coerceIn(0f, 1f)
          val color = if (trail == 0) Color(0xFF80FF80) else Color(0xFF00FF00).copy(alpha = alpha)
          drawRect(
            color = color,
            topLeft = androidx.compose.ui.geometry.Offset(colX, dotY),
            size = androidx.compose.ui.geometry.Size(6f, 12f)
          )
        }
      }
    }
  } else {
    return this.background(Color.Black)
  }
}

@Composable
fun KeepScreenOn() {
  val view = LocalView.current
  DisposableEffect(view) {
    view.keepScreenOn = true
    onDispose {
      view.keepScreenOn = false
    }
  }
}

@Composable
fun Modifier.burnInProtection(enableShine: Boolean = true): Modifier {
  val infiniteTransition = rememberInfiniteTransition(label = "burn_in_modifier")
  val shiftTime = infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 8000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "shift_time"
  )
  val shimmerTime = infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 5000f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 5000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "shimmer_time"
  )

  val shiftRad = (shiftTime.value * (Math.PI / 180.0)).toFloat()
  val dx = (Math.cos(shiftRad.toDouble()) * 2.0).toFloat().dp
  val dy = (Math.sin(shiftRad.toDouble()) * 2.0).toFloat().dp

  val isShining = shimmerTime.value < 1200f
  val ratio = if (isShining) (shimmerTime.value / 1200f) else 0f
  
  val shineBrush = if (enableShine && isShining) {
    val sweepWidth = 180f
    val startCoord = (ratio * (sweepWidth * 3.5f)) - sweepWidth
    Brush.linearGradient(
      colors = listOf(
        Color.Transparent,
        Color.White.copy(alpha = 0.0f),
        Color.White.copy(alpha = 0.75f),
        Color(0xFFFFD93D).copy(alpha = 0.5f), // Fancy gold flare
        Color.White.copy(alpha = 0.75f),
        Color.White.copy(alpha = 0.0f),
        Color.Transparent
      ),
      start = androidx.compose.ui.geometry.Offset(startCoord, 0f),
      end = androidx.compose.ui.geometry.Offset(startCoord + sweepWidth, sweepWidth)
    )
  } else {
    null
  }

  val density = androidx.compose.ui.platform.LocalDensity.current
  return this
    .graphicsLayer {
      translationX = with(density) { dx.toPx() }
      translationY = with(density) { dy.toPx() }
      compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen
    }
    .drawWithContent {
      drawContent()
      if (shineBrush != null) {
        drawRect(brush = shineBrush, blendMode = BlendMode.SrcAtop)
      }
    }
}

private var globalToast: Toast? = null

fun showToast(context: android.content.Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
  globalToast?.cancel()
  val toast = Toast.makeText(context.applicationContext, message, duration)
  globalToast = toast
  toast.show()
}

fun formatDuration(elapsedTimeMs: Long): String {
  val totalSeconds = elapsedTimeMs / 1000
  val minutes = (totalSeconds / 60) % 60
  val seconds = totalSeconds % 60
  val centiseconds = (elapsedTimeMs % 1000) / 10
  return if (totalSeconds < 60) {
    String.format(Locale.US, "%d.%02d", seconds, centiseconds)
  } else {
    String.format(Locale.US, "%02d:%02d.%02d", minutes, seconds, centiseconds)
  }
}

fun formatTimestamp(timestamp: Long): String {
  val date = java.util.Date(timestamp)
  val sdf = java.text.SimpleDateFormat("MMM dd, HH:mm", Locale.US)
  return sdf.format(date)
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
  MyApplicationTheme {
    HomeScreen(
      isManualUnlocked = false,
      useCustomImage = false,
      pingleCustomImageUri = null,
      showLoadingSequence = false,
      onLoadingFinished = {},
      onUnlockManual = {},
      onPlayClicked = {},
      onManualSpinClicked = {},
      onHighScoresClicked = {},
      onOptionsClicked = {}
    )
  }
}

@Composable
fun SpotifyPlayerWidget(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val spotifyManager = remember { SpotifyManager.getInstance(context) }
    val isConnected by spotifyManager.isConnected.collectAsState()
    val currentTrack by spotifyManager.currentTrack.collectAsState()
    val isPlaying by spotifyManager.isPlaying.collectAsState()
    val isLoading by spotifyManager.isLoading.collectAsState()
    val error by spotifyManager.error.collectAsState()
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    if (!isConnected) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(
                width = 1.dp,
                color = Color(0xFF1DB954).copy(alpha = 0.25f),
                shape = RoundedCornerShape(16.dp)
            )
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Black.copy(alpha = 0.9f), Color(0xFF0F0F0F).copy(alpha = 0.95f))
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (currentTrack == null) {
                // Initial prompt state
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Music",
                            tint = Color(0xFF1DB954),
                            modifier = Modifier.size(20.dp)
                        )
                        PingleText(
                            text = "Spotify Playlist Player",
                            style = TextStyle(
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.SansSerif
                            )
                        )
                    }

                    if (isLoading) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color(0xFF1DB954),
                            strokeWidth = 1.5.dp
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .border(1.dp, Color(0xFF1DB954), RoundedCornerShape(12.dp))
                                .clickable {
                                    scope.launch {
                                        spotifyManager.playRandomPlaylistSong()
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            PingleText(
                                text = "PLAY RANDOM SONG",
                                style = TextStyle(
                                    color = Color(0xFF1DB954),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            } else {
                val track = currentTrack!!
                // Track playing state
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Album art loaded via Coil AsyncImage
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (track.albumArtUrl != null) {
                            AsyncImage(
                                model = track.albumArtUrl,
                                contentDescription = "Album Art",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(6.dp))
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = "Track",
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Title & Artist
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        PingleText(
                            text = track.name,
                            style = TextStyle(
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.SansSerif
                            ),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        PingleText(
                            text = track.artist,
                            style = TextStyle(
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.SansSerif
                            ),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }

                    // Media Controls
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (track.previewUrl != null) {
                            IconButton(
                                onClick = { spotifyManager.togglePlayPause() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                scope.launch {
                                    spotifyManager.playRandomPlaylistSong()
                                }
                            },
                            enabled = !isLoading,
                            modifier = Modifier.size(32.dp)
                        ) {
                            if (isLoading) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color(0xFF1DB954),
                                    strokeWidth = 1.5.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = "Next Random Track",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // Subtitle/Footer with Open in Spotify link and status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PingleText(
                        text = if (track.previewUrl == null) "No preview available" else "Streaming preview track",
                        style = TextStyle(color = Color.White.copy(alpha = 0.4f), fontSize = 9.sp)
                    )

                    Box(
                        modifier = Modifier
                            .clickable {
                                try {
                                    val intent = android.content.Intent(
                                        android.content.Intent.ACTION_VIEW,
                                        android.content.Intent.ACTION_VIEW.let { android.net.Uri.parse(track.externalUrl) }
                                    )
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    showToast(context, "Could not open Spotify client")
                                }
                            }
                            .padding(vertical = 2.dp, horizontal = 6.dp)
                    ) {
                        PingleText(
                            text = "OPEN IN SPOTIFY ↗",
                            style = TextStyle(
                                color = Color(0xFF1DB954),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            if (error != null) {
                PingleText(
                    text = error ?: "",
                    style = TextStyle(color = Color.Red, fontSize = 10.sp),
                    modifier = Modifier.padding(top = 4.dp),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun PinguiSetupScreen(
  onBackClicked: () -> Unit,
  onSetUpNowClicked: () -> Unit
) {
  val viewModel: PingleViewModel = viewModel()
  val easterSpaceStars by viewModel.easterSpaceStars.collectAsState()
  val easterMatrixBg by viewModel.easterMatrixBg.collectAsState()

  Box(
    modifier = Modifier
      .fillMaxSize()
      .pingleBackground(easterSpaceStars, easterMatrixBg)
      .statusBarsPadding()
      .navigationBarsPadding()
      .testTag("pingui_setup_screen")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 32.dp, vertical = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Top bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        PingleText(
          text = "PINGUI SETUP",
          style = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraLight,
            color = Color.White,
            letterSpacing = 3.sp
          )
        )

        Box(
          modifier = Modifier
            .size(40.dp)
            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(10.dp))
            .clickable { onBackClicked() },
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close Setup",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
          )
        }
      }

      // Middle Card with cute illustration description
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .padding(vertical = 40.dp)
          .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
          .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(24.dp))
          .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        // Cute design icon
        Box(
          modifier = Modifier
            .size(80.dp)
            .border(1.dp, Color(0xFF0078D7).copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .background(Color(0xFF0078D7).copy(alpha = 0.08f), RoundedCornerShape(20.dp)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Build,
            contentDescription = "Builder Tool",
            tint = Color(0xFF0078D7),
            modifier = Modifier.size(36.dp)
          )
        }

        Spacer(modifier = Modifier.height(28.dp))

        PingleText(
          text = "LAYOUT EDITOR",
          style = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontSize = 18.sp,
            fontWeight = FontWeight.Light,
            color = Color.White,
            letterSpacing = 2.sp
          )
        )

        Spacer(modifier = Modifier.height(14.dp))

        PingleText(
          text = "Customize your game screen elements exactly the way you want!\n\nModify size, position, and tilt angles for the Spinning Pingle, Timer, Spotify Widget, and the Fold Mode Controller to create your custom UI experience.",
          style = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontSize = 13.sp,
            fontWeight = FontWeight.Light,
            color = Color.White.copy(alpha = 0.65f),
            letterSpacing = 0.5.sp
          ),
          textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
      }

      // Bottom Button
      MinimalistButton(
        text = "set it up now",
        onClick = onSetUpNowClicked,
        modifier = Modifier.testTag("set_it_up_now_button")
      )
    }
  }
}

@Composable
fun PinguiGameEditScreen(
  onBackClicked: () -> Unit
) {
  val viewModel: PingleViewModel = viewModel()
  val easterSpaceStars by viewModel.easterSpaceStars.collectAsState()
  val easterMatrixBg by viewModel.easterMatrixBg.collectAsState()

  // 1. Detect if flip-style foldable
  val isV2xUnlocked by viewModel.isV2xUnlocked.collectAsState()
  val isPlusMSUnlocked by viewModel.isPlusMSUnlocked.collectAsState()
  val isPsmUnlocked = isV2xUnlocked || isPlusMSUnlocked
  val isFlipStyle = isFlipStyleFoldable()
  val editPages = remember(isFlipStyle, isPsmUnlocked) {
    if (isFlipStyle) {
      if (isPsmUnlocked) {
        listOf("unfolded_normal", "folded_normal", "unfolded_psm", "folded_psm")
      } else {
        listOf("unfolded_normal", "folded_normal")
      }
    } else {
      if (isPsmUnlocked) {
        listOf("normal", "psm")
      } else {
        listOf("normal")
      }
    }
  }

  var currentPageIndex by remember { mutableStateOf(0) }
  var showWarningDialog by remember { mutableStateOf(false) }
  val currentConfig = editPages.getOrElse(currentPageIndex) { "normal" }

  // 2. Load active parameters when page changes
  val totalSpinDuration by viewModel.totalSpinDuration.collectAsState()
  val isSpotifyUnlocked = totalSpinDuration >= 600000L
  var selectedElement by remember { mutableStateOf("pingle") }
  val showDisc = isFlipStyle && currentConfig == "folded_psm"

  LaunchedEffect(currentPageIndex, isFlipStyle, isSpotifyUnlocked) {
    viewModel.loadActiveLayoutForConfig(currentConfig)
    if (!showDisc && selectedElement == "fold disc") {
      selectedElement = "pingle"
    }
    if (!isSpotifyUnlocked && selectedElement == "spotify") {
      selectedElement = "pingle"
    }
  }

  // 3. Collect active parameters from view model
  val pingleScale by viewModel.pingleScale.collectAsState()
  val pingleOffsetX by viewModel.pingleOffsetX.collectAsState()
  val pingleOffsetY by viewModel.pingleOffsetY.collectAsState()
  val pingleTilt by viewModel.pingleTilt.collectAsState()

  val timerScale by viewModel.timerScale.collectAsState()
  val timerOffsetX by viewModel.timerOffsetX.collectAsState()
  val timerOffsetY by viewModel.timerOffsetY.collectAsState()
  val timerTilt by viewModel.timerTilt.collectAsState()

  val spotifyScale by viewModel.spotifyScale.collectAsState()
  val spotifyOffsetX by viewModel.spotifyOffsetX.collectAsState()
  val spotifyOffsetY by viewModel.spotifyOffsetY.collectAsState()
  val spotifyTilt by viewModel.spotifyTilt.collectAsState()

  val discScale by viewModel.discScale.collectAsState()
  val discOffsetX by viewModel.discOffsetX.collectAsState()
  val discOffsetY by viewModel.discOffsetY.collectAsState()
  val discTilt by viewModel.discTilt.collectAsState()

  val useCustomImage by viewModel.useCustomImage.collectAsState()
  val pingleCustomImageUri by viewModel.pingleCustomImageUri.collectAsState()

  fun isAnyElementOffScreen(): Boolean {
    val limitX = 180f
    val limitY = 380f
    val pingleOff = Math.abs(pingleOffsetX) > limitX || Math.abs(pingleOffsetY) > limitY
    val timerOff = Math.abs(timerOffsetX) > limitX || Math.abs(timerOffsetY) > limitY
    val spotifyOff = isSpotifyUnlocked && (Math.abs(spotifyOffsetX) > limitX || Math.abs(spotifyOffsetY) > limitY)
    val discOff = showDisc && (Math.abs(discOffsetX) > limitX || Math.abs(discOffsetY) > limitY)
    return pingleOff || timerOff || spotifyOff || discOff
  }

  val isConfigFolded = currentConfig.startsWith("folded")
  val isConfigPsm = currentConfig.endsWith("psm") || currentConfig == "psm"
  val editTransitionProgress = if (isConfigFolded) 1f else 0f
  val density = androidx.compose.ui.platform.LocalDensity.current.density

  Box(
    modifier = Modifier
      .fillMaxSize()
      .pingleBackground(easterSpaceStars, easterMatrixBg)
      .testTag("pingui_edit_viewport")
      // Handle multi-touch gestures globally on the viewport for the selected element
      .pointerInput(selectedElement) {
        detectTransformGestures { _, pan, zoom, rotation ->
          val panDpX = pan.x / density
          val panDpY = pan.y / density
          when (selectedElement) {
            "pingle" -> {
              viewModel.setPingleOffsetX((viewModel.pingleOffsetX.value + panDpX).coerceIn(-400f, 400f))
              viewModel.setPingleOffsetY((viewModel.pingleOffsetY.value + panDpY).coerceIn(-600f, 600f))
              viewModel.setPingleScale((viewModel.pingleScale.value * zoom).coerceIn(0.4f, 4.0f))
              viewModel.setPingleTilt((viewModel.pingleTilt.value + rotation) % 360f)
            }
            "timer" -> {
              viewModel.setTimerOffsetX((viewModel.timerOffsetX.value + panDpX).coerceIn(-400f, 400f))
              viewModel.setTimerOffsetY((viewModel.timerOffsetY.value + panDpY).coerceIn(-600f, 600f))
              viewModel.setTimerScale((viewModel.timerScale.value * zoom).coerceIn(0.4f, 4.0f))
              viewModel.setTimerTilt((viewModel.timerTilt.value + rotation) % 360f)
            }
            "spotify" -> {
              if (isSpotifyUnlocked) {
                viewModel.setSpotifyOffsetX((viewModel.spotifyOffsetX.value + panDpX).coerceIn(-400f, 400f))
                viewModel.setSpotifyOffsetY((viewModel.spotifyOffsetY.value + panDpY).coerceIn(-600f, 600f))
                viewModel.setSpotifyScale((viewModel.spotifyScale.value * zoom).coerceIn(0.4f, 4.0f))
                viewModel.setSpotifyTilt((viewModel.spotifyTilt.value + rotation) % 360f)
              }
            }
            "fold disc" -> {
              viewModel.setDiscOffsetX((viewModel.discOffsetX.value + panDpX).coerceIn(-400f, 400f))
              viewModel.setDiscOffsetY((viewModel.discOffsetY.value + panDpY).coerceIn(-600f, 600f))
              viewModel.setDiscScale((viewModel.discScale.value * zoom).coerceIn(0.4f, 4.0f))
              viewModel.setDiscTilt((viewModel.discTilt.value + rotation) % 360f)
            }
          }
        }
      }
  ) {
    if (isConfigFolded) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .align(Alignment.Center)
          .height(30.dp),
        contentAlignment = Alignment.Center
      ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
          val path = androidx.compose.ui.graphics.Path()
          val numTeeth = (size.width / 12f).toInt().coerceAtLeast(20)
          val toothWidth = size.width / numTeeth
          val midY = size.height / 2f
          val amp = 6f
          path.moveTo(0f, midY)
          for (i in 0..numTeeth) {
            val x = i * toothWidth
            val y = midY + if (i % 2 == 0) -amp else amp
            path.lineTo(x, y)
          }
          drawPath(
            path = path,
            color = Color(0xFFFF9900),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
          )
        }
        // Small text indicating fold
        PingleText(
          text = "FOLD AXIS",
          style = TextStyle(
            color = Color(0xFFFF9900).copy(alpha = 0.8f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
          ),
          modifier = Modifier
            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .align(Alignment.Center)
        )
      }
    }

    // 1. Spinning Pingle Preview
    val isPingleSelected = selectedElement == "pingle"
    Column(
      modifier = Modifier
        .align(BiasAlignment(0f, -0.5f * editTransitionProgress))
        .offset(x = pingleOffsetX.dp, y = pingleOffsetY.dp)
        .graphicsLayer(
          scaleX = pingleScale,
          scaleY = pingleScale,
          rotationZ = pingleTilt
        )
        .border(
          width = if (isPingleSelected) 2.dp else 0.dp,
          color = if (isPingleSelected) Color(0xFF00FFCC) else Color.Transparent,
          shape = RoundedCornerShape(20.dp)
        )
        .clickable(
          interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
          indication = null
        ) {
          selectedElement = "pingle"
        }
        .padding(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      val pringleSize = if (isConfigPsm) {
        (260f - 40f * editTransitionProgress).dp
      } else {
        (260f - 20f * editTransitionProgress).dp
      }
      val pringleContainerSize = if (isConfigPsm) {
        (320f - 40f * editTransitionProgress).dp
      } else {
        320.dp
      }
      val containerHeight = if (isConfigPsm) {
        (200f - 20f * editTransitionProgress).dp
      } else {
        200.dp
      }

      Box(contentAlignment = Alignment.Center, modifier = Modifier.size(pringleContainerSize, containerHeight)) {
        Box(
          modifier = Modifier
            .size(pringleContainerSize, containerHeight)
            .background(
              Brush.radialGradient(
                colors = listOf(
                  Color(0x22FFD93D),
                  Color(0x00FFD93D)
                )
              )
            )
        )

        PingleImage(
          useCustomImage = useCustomImage,
          customImageUri = pingleCustomImageUri,
          contentDescription = "Spinning Pingle Preview",
          modifier = Modifier
            .size(pringleSize)
            .testTag("edit_pingle_image"),
          contentScale = ContentScale.Fit
        )
      }
    }

    // 2. Timer (Header Row) Preview
    val isTimerSelected = selectedElement == "timer"
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val headerTopPadding = (statusBarPadding.value * (1f - editTransitionProgress)).dp
    val headerHorizontalPadding = (24f - 8f * editTransitionProgress).dp
    val headerVerticalPadding = (20f - 12f * editTransitionProgress).dp

    Box(
      modifier = Modifier
        .align(BiasAlignment(0f, -1.0f + 1.5f * editTransitionProgress))
        .offset(x = timerOffsetX.dp, y = timerOffsetY.dp)
        .graphicsLayer(
          scaleX = timerScale,
          scaleY = timerScale,
          rotationZ = timerTilt
        )
        .border(
          width = if (isTimerSelected) 2.dp else 0.dp,
          color = if (isTimerSelected) Color(0xFF00FFCC) else Color.Transparent,
          shape = RoundedCornerShape(12.dp)
        )
        .clickable(
          interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
          indication = null
        ) {
          selectedElement = "timer"
        }
        .padding(top = headerTopPadding)
        .padding(horizontal = headerHorizontalPadding, vertical = headerVerticalPadding)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        val timerSize = (24f + 4f * editTransitionProgress).sp
        PingleText(
          text = "04.28",
          style = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontSize = timerSize,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.85f),
            letterSpacing = (-0.5).sp
          )
        )

        Box(
          modifier = Modifier
            .size((48f + 4f * editTransitionProgress).dp)
            .border(
              width = 1.dp,
              color = Color.White.copy(alpha = 0.15f),
              shape = RoundedCornerShape(12.dp)
            )
            .background(Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(12.dp)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Mock Check",
            tint = Color.White,
            modifier = Modifier.size((24f + 2f * editTransitionProgress).dp)
          )
        }
      }
    }

    // 3. Spotify Widget Preview
    if (isSpotifyUnlocked) {
      val isSpotifySelected = selectedElement == "spotify"
      Box(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .navigationBarsPadding()
          .padding(bottom = 16.dp)
          .offset(x = spotifyOffsetX.dp, y = spotifyOffsetY.dp)
          .graphicsLayer(
            scaleX = spotifyScale,
            scaleY = spotifyScale,
            rotationZ = spotifyTilt
          )
          .border(
            width = if (isSpotifySelected) 2.dp else 0.dp,
            color = if (isSpotifySelected) Color(0xFF00FFCC) else Color.Transparent,
            shape = RoundedCornerShape(16.dp)
          )
          .clickable(
            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
            indication = null
          ) {
            selectedElement = "spotify"
          }
      ) {
        SpotifyPlayerWidget()
      }
    }

    // 4. Disc Controller Preview (Only shown on pinglespinmanual (folded))
    if (showDisc) {
      val isDiscSelected = selectedElement == "fold disc"
      Box(
        modifier = Modifier
          .align(BiasAlignment(0f, 1.15f - 0.65f * editTransitionProgress))
          .offset(x = discOffsetX.dp, y = discOffsetY.dp)
          .graphicsLayer(
            scaleX = discScale,
            scaleY = discScale,
            rotationZ = discTilt
          )
          .border(
            width = if (isDiscSelected) 2.dp else 0.dp,
            color = if (isDiscSelected) Color(0xFF00FFCC) else Color.Transparent,
            shape = RoundedCornerShape(16.dp)
          )
          .clickable(
            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
            indication = null
          ) {
            selectedElement = "fold disc"
          }
      ) {
        // Flat grey disc (CD) body
        Box(
          modifier = Modifier
            .size(170.dp)
            .background(Color(0xFFB0B0B0), androidx.compose.foundation.shape.CircleShape)
            .border(2.dp, Color(0xFF8E8E8E), androidx.compose.foundation.shape.CircleShape),
          contentAlignment = Alignment.Center
        ) {
          androidx.compose.foundation.Canvas(
            modifier = Modifier.fillMaxSize()
          ) {
            val r = size.minDimension / 2f
            val ctr = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)

            // CD shine reflection stroke (white with alpha)
            drawCircle(
              color = Color.White.copy(alpha = 0.15f),
              radius = r * 0.85f,
              style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
            )

            // Red line from the middle to the end
            drawLine(
              color = Color(0xFFFF3B30),
              start = androidx.compose.ui.geometry.Offset(ctr.x + r * 0.25f, ctr.y),
              end = androidx.compose.ui.geometry.Offset(ctr.x + r, ctr.y),
              strokeWidth = 6f,
              cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
          }

          // Black circle in the middle (looks like a CD spindle hole)
          Box(
            modifier = Modifier
              .size(42.dp)
              .background(Color.Black, androidx.compose.foundation.shape.CircleShape)
              .border(1.5.dp, Color(0xFF6E6E6E), androidx.compose.foundation.shape.CircleShape)
          )
        }
      }
    }



    // 6. Bottom Selection Tabs and Navigation Bar
    Column(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .navigationBarsPadding()
        .background(Color.Black.copy(alpha = 0.85f))
        .border(width = 1.dp, color = Color.White.copy(alpha = 0.12f))
        .padding(bottom = 16.dp, top = 16.dp)
        .padding(horizontal = 24.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Element Selection Tabs
      val tabs = buildList {
        add("pingle")
        add("timer")
        if (isSpotifyUnlocked) {
          add("spotify")
        }
        if (showDisc) {
          add("fold disc")
        }
      }
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        tabs.forEach { tab ->
          val isSelected = selectedElement == tab
          Box(
            modifier = Modifier
              .weight(1f)
              .border(
                width = 1.5.dp,
                color = if (isSelected) Color(0xFF00FFCC) else Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(10.dp)
              )
              .background(
                if (isSelected) Color(0xFF00FFCC).copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp)
              )
              .clickable { selectedElement = tab }
              .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
          ) {
            PingleText(
              text = tab.uppercase(Locale.US),
              style = TextStyle(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color(0xFF00FFCC) else Color.White.copy(alpha = 0.6f),
                letterSpacing = 0.5.sp
              )
            )
          }
        }
      }

      // Small bottom bar containing Next/Done, Page, and Reset
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          if (currentPageIndex > 0) {
            Box(
              modifier = Modifier
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                .background(Color.Black, RoundedCornerShape(8.dp))
                .clickable {
                  viewModel.saveActiveLayoutForConfig(currentConfig)
                  currentPageIndex--
                }
                .padding(horizontal = 14.dp, vertical = 8.dp),
              contentAlignment = Alignment.Center
            ) {
              PingleText(
                text = "PREV",
                style = TextStyle(
                  color = Color.White.copy(alpha = 0.7f),
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.sp
                )
              )
            }
          } else {
            Box(
              modifier = Modifier
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                .background(Color.Black, RoundedCornerShape(8.dp))
                .clickable { onBackClicked() },
              contentAlignment = Alignment.Center
            ) {
              PingleText(
                text = "CLOSE",
                style = TextStyle(
                  color = Color.White.copy(alpha = 0.7f),
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.sp
                )
              )
            }
          }

          Box(
            modifier = Modifier
              .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
              .background(Color.Black, RoundedCornerShape(8.dp))
              .clickable {
                viewModel.resetLayoutConfig(currentConfig)
                viewModel.loadActiveLayoutForConfig(currentConfig)
              }
              .padding(horizontal = 14.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
          ) {
            PingleText(
              text = "RESET",
              style = TextStyle(
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
              )
            )
          }
        }

        // Page Progress Status Text
        val pageTitle = when (currentConfig) {
          "normal" -> "Normal UI"
          "psm" -> "Pinglespin Manual"
          "unfolded_normal" -> "Unfolded Normal"
          "folded_normal" -> "Folded Normal"
          "unfolded_psm" -> "Unfolded PSM"
          "folded_psm" -> "Folded PSM"
          else -> "Custom UI"
        }
        PingleText(
          text = "${currentPageIndex + 1}/${editPages.size}: $pageTitle".uppercase(Locale.US),
          style = TextStyle(
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
          )
        )

        val isLastPage = currentPageIndex == editPages.size - 1
        val nextText = if (isLastPage) "DONE" else "NEXT"

        Box(
          modifier = Modifier
            .border(1.dp, Color(0xFF00FFCC), RoundedCornerShape(8.dp))
            .background(Color(0xFF00FFCC).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .clickable {
              viewModel.saveActiveLayoutForConfig(currentConfig)
              if (isLastPage) {
                if (isAnyElementOffScreen()) {
                  showWarningDialog = true
                } else {
                  onBackClicked()
                }
              } else {
                currentPageIndex++
              }
            }
            .padding(horizontal = 18.dp, vertical = 8.dp),
          contentAlignment = Alignment.Center
        ) {
          PingleText(
            text = nextText,
            style = TextStyle(
              color = Color(0xFF00FFCC),
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            )
          )
        }
      }
    }

    if (showWarningDialog) {
      androidx.compose.material3.AlertDialog(
        onDismissRequest = { showWarningDialog = false },
        title = {
          PingleText(
            text = " WARNING",
            style = TextStyle(
              fontWeight = FontWeight.Bold,
              fontSize = 18.sp,
              color = Color.Red
            )
          )
        },
        text = {
          PingleText(
            text = "some ui elements are going to be shown off screen due to current settings ",
            style = TextStyle(
              fontSize = 14.sp,
              color = Color.White
            )
          )
        },
        confirmButton = {
          androidx.compose.material3.TextButton(
            onClick = {
              showWarningDialog = false
              onBackClicked()
            }
          ) {
            PingleText(
              text = " continue",
              style = TextStyle(color = Color(0xFF00FFCC), fontWeight = FontWeight.Bold)
            )
          }
        },
        dismissButton = {
          androidx.compose.material3.TextButton(
            onClick = {
              showWarningDialog = false
            }
          ) {
            PingleText(
              text = " redo setup",
              style = TextStyle(color = Color.White.copy(alpha = 0.6f))
            )
          }
        },
        containerColor = Color(0xFF1C1C1E),
        textContentColor = Color.White,
        titleContentColor = Color.Red
      )
    }
  }
}


