package com.github.hakonschia.bottomsheetcrash

import android.app.Activity
import android.app.PictureInPictureParams
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.github.hakonschia.bottomsheetcrash.ui.theme.BottomsheetcrashTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BottomsheetcrashTheme {
                Box {
                    var scenario1 by remember { mutableStateOf(true) }
                    Row(
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text(text = if (scenario1) "Scenario1" else "Scenario2")
                        Switch(
                            checked = scenario1,
                            onCheckedChange = { scenario1 = !scenario1 },
                        )
                    }

                    if (scenario1) {
                        Scenario1()
                    } else {
                        Scenario2()
                    }
                }
            }
        }
    }
}


/**
 * This scenario crashes when opening the sheet in landscape then rotating to portrait. This seems to depend on
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Scenario1() {
    val bottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val coroutineScope = rememberCoroutineScope()

    ModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetBackgroundColor = Color.Red,
        sheetContent = {
            SheetContent()
        }
    ) {
        Button(
            onClick = {
                coroutineScope.launch {
                    bottomSheetState.show()
                }
            }
        ) {
            Text(text = "Open sheet")
        }
    }
}

/**
 * This scenario crashes when opening the sheet. Seems like something goes wrong when going from empty sheet content to having something
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Scenario2() {
    val bottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val coroutineScope = rememberCoroutineScope()

    var showSheetContent by remember { mutableStateOf(bottomSheetState.isVisible) }

    LaunchedEffect(bottomSheetState.isVisible) {
        if (!bottomSheetState.isVisible) {
            showSheetContent = false
        }
    }

    ModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetBackgroundColor = if (showSheetContent) Color.Red else Color.Transparent,
        scrimColor = if (showSheetContent) ModalBottomSheetDefaults.scrimColor else Color.Transparent,
        sheetContent = {
            // Before 1.4.0-alpha04 with the rewrite of ModalBottomSheetLayout this was needed to avoid "Initial value must have an associated anchor"
            // In alpha04 it is still necessary to avoid "Key HalfExpanded is missing in the map."
            //Spacer(Modifier.height(1.dp))

            if (showSheetContent) {
                SheetContent()
            }
        }
    ) {
        Column {
            Button(
                onClick = {
                    showSheetContent = true

                    coroutineScope.launch {
                        bottomSheetState.show()
                    }
                }
            ) {
                Text(text = "Open sheet")
            }

            val context = LocalContext.current

            Button(
                onClick = {
                    (context as Activity).enterPictureInPictureMode(PictureInPictureParams.Builder().build())
                }
            ) {
                Text(text = "Enter PiP")
            }
        }
    }
}

@Composable
fun SheetContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    ) {
        Text(text = "Hello BottomSheet")
    }
}
