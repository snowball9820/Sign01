package com.app.sign01

import android.content.ContentValues.TAG
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.app.sign01.ui.theme.Sign01Theme
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.digitalink.DigitalInkRecognition
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModel
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModelIdentifier
import com.google.mlkit.vision.digitalink.DigitalInkRecognizer
import com.google.mlkit.vision.digitalink.DigitalInkRecognizerOptions
import com.google.mlkit.vision.digitalink.Ink
import com.google.mlkit.vision.digitalink.RecognitionResult


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Sign01Theme {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(){
                        Column {
                            val inkBuilder = Ink.builder()
                            var strokeBuilder: Ink.Stroke.Builder? = null

                            val drawingPoints = remember { mutableListOf<Offset>() }

                            fun addNewTouchEvent(event: MotionEvent, drawingPoints: MutableList<Offset>) {
                                val action = event.actionMasked
                                val x = event.x
                                val y = event.y
                                val t = System.currentTimeMillis()

                                when (action) {
                                    MotionEvent.ACTION_DOWN -> {
                                        strokeBuilder = Ink.Stroke.builder()
                                        strokeBuilder!!.addPoint(Ink.Point.create(x, y, t))
                                    }

                                    MotionEvent.ACTION_MOVE -> {
                                        strokeBuilder?.addPoint(Ink.Point.create(x, y, t))
                                    }

                                    MotionEvent.ACTION_UP -> {
                                        strokeBuilder?.addPoint(Ink.Point.create(x, y, t))
                                        strokeBuilder?.let {
                                            inkBuilder.addStroke(it.build())
                                        }
                                        strokeBuilder = null
                                    }

                                    else -> {

                                    }
                                }
                            }


                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .pointerInput(Unit) {
                                        detectTransformGestures { _, pan, _, _ ->
                                            val offsetX = pan.x
                                            val offsetY = pan.y
                                            drawingPoints.add(Offset(offsetX, offsetY))

                                            val event = MotionEvent.obtain(
                                                SystemClock.uptimeMillis(),
                                                SystemClock.uptimeMillis(),
                                                MotionEvent.ACTION_MOVE,
                                                offsetX,
                                                offsetY,
                                                0
                                            )
                                            addNewTouchEvent(event, drawingPoints)
                                        }
                                    }
                            ) {
                                for (i in 0 until drawingPoints.size - 1) {
                                    drawLine(
                                        color = androidx.compose.ui.graphics.Color.DarkGray,
                                        start = drawingPoints[i],
                                        end = drawingPoints[i + 1],
                                        strokeWidth = 5f
                                    )
                                }
                            }

                            fun createInkFromDrawingPoints(points: List<Offset>): Ink {
                                val inkBuilder = Ink.builder()
                                val strokeBuilder = Ink.Stroke.builder()

                                for (point in points) {
                                    strokeBuilder.addPoint(Ink.Point.create(point.x, point.y))
                                }

                                inkBuilder.addStroke(strokeBuilder.build())
                                return inkBuilder.build()
                            }

                            



                            val ink = createInkFromDrawingPoints(drawingPoints)
                            recognizeInk(ink)

                            var selectUri by remember {
                                mutableStateOf<Uri?>(null)
                            }
                            val launcher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.PickVisualMedia(),
                                onResult = { uri ->
                                    selectUri = uri

                                }
                            )

                            Button(
                                onClick = {
                                    launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary,

                                    ),
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(50.dp)
                                    .padding(top = 12.dp)
                            ) {
                                Text(
                                    "서류 등록",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )

                            }




                            Image(
                                painter = rememberImagePainter(data = selectUri),
                                contentDescription = "",
                                modifier = Modifier
                                    .padding(top = 20.dp)
                                    .fillMaxWidth()
                                    .height(300.dp)
                            )



                        }

                    }




                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Sign01Theme {
        Greeting("Android")
    }
}