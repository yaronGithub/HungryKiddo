package com.example.hungrykiddo

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun GameScreen() {
    // State for the game
    var happiness by remember { mutableStateOf(50f) }
    var isHappy by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Happiness Meter
        Text(
            text = "Happiness Meter",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        LinearProgressIndicator(
            progress = happiness / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        // Kid's Image
        Image(
            painter = painterResource(
                id = if (isHappy) R.drawable.happy_kid else R.drawable.sad_kid
            ),
            contentDescription = "Kid's mood",
            modifier = Modifier.size(200.dp)
        )

        // Food Buttons
        Column(
            modifier = Modifier.padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FoodButton("Pizza", happiness) { newHappiness ->
                handleFoodClick(context, newHappiness) { happiness = it; isHappy = !isHappy }
            }
            FoodButton("Cake", happiness) { newHappiness ->
                handleFoodClick(context, newHappiness) { happiness = it; isHappy = !isHappy }
            }
            FoodButton("Apple", happiness) { newHappiness ->
                handleFoodClick(context, newHappiness) { happiness = it; isHappy = !isHappy }
            }
            FoodButton("Chicken", happiness) { newHappiness ->
                handleFoodClick(context, newHappiness) { happiness = it; isHappy = !isHappy }
            }
        }
    }
}

@Composable
fun FoodButton(
    food: String,
    currentHappiness: Float,
    onFoodClick: (Float) -> Unit
) {
    Button(
        onClick = {
            val happinessChange = when (food) {
                "Pizza" -> 15f
                "Cake" -> 20f
                "Apple" -> 10f
                "Chicken" -> 12f
                else -> 10f
            }
            val newHappiness = (currentHappiness + happinessChange).coerceIn(0f, 100f)
            onFoodClick(newHappiness)
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Text(text = food, fontSize = 20.sp)
    }
}

private fun handleFoodClick(
    context: Context,
    newHappiness: Float,
    updateState: (Float) -> Unit
) {
    // Play eating sound
    val mediaPlayer = MediaPlayer.create(context, R.raw.eating_sound)
    mediaPlayer.start()
    mediaPlayer.setOnCompletionListener { it.release() }
    
    // Update happiness
    updateState(newHappiness)
} 