package com.example.hungrykiddo

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class FoodItem(
    val name: String,
    val icon: Int,
    val happinessBoost: Float,
    val soundEffect: Int
)

@Composable
fun GameScreen() {
    var happiness by remember { mutableStateOf(50f) }
    var isEating by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var itemsEaten by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var kidScale by remember { mutableStateOf(1f) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Kid's mouth position
    var mouthPosition by remember { mutableStateOf(Offset.Zero) }
    
    // List of all possible foods
    val allFoods = listOf(
        FoodItem("Pizza", R.drawable.food_pizza, 15f, R.raw.crunch_sound),
        FoodItem("Cake", R.drawable.food_cake, 20f, R.raw.munch_sound),
        FoodItem("Apple", R.drawable.food_apple, 10f, R.raw.crunch_sound),
        FoodItem("Chicken", R.drawable.food_chicken, 12f, R.raw.nom_sound),
        FoodItem("Ice Cream", R.drawable.food_ice_cream, 18f, R.raw.slurp_sound),
        FoodItem("Carrot", R.drawable.food_carrot, 8f, R.raw.crunch_sound),
        FoodItem("Sandwich", R.drawable.food_sandwich, 14f, R.raw.munch_sound),
        FoodItem("Cookie", R.drawable.food_cookie, 16f, R.raw.crunch_sound)
    )
    
    // Currently displayed foods
    var currentFoods by remember { mutableStateOf(allFoods.shuffled().take(4)) }

    // Animation for kid growth
    val kidScaleAnimation by animateFloatAsState(
        targetValue = kidScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFF3E0), // Warm beige color for the top
                        Color(0xFFFFE0B2)  // Slightly darker beige for the bottom
                    )
                )
            )
    ) {
        // Add background decorative elements
        Image(
            painter = painterResource(id = R.drawable.home_background),
            contentDescription = "Home background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Score Display
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Score: $score",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Happiness Meter",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            
            LinearProgressIndicator(
                progress = happiness / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            // Message display
            if (message.isNotEmpty()) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }

            // Kid's Image
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(kidScaleAnimation),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(
                        id = if (isEating) R.drawable.cartoon_kid_eating else R.drawable.cartoon_kid_happy
                    ),
                    contentDescription = "Kid's mood",
                    modifier = Modifier
                        .scale(if (isEating) 1.1f else 1f)
                        .onGloballyPositioned { coordinates ->
                            // Update mouth position (approximately center of the image)
                            mouthPosition = coordinates.positionInRoot() + Offset(
                                coordinates.size.width / 2f,
                                coordinates.size.height * 0.6f // Mouth is slightly below center
                            )
                        }
                )
            }

            // Food Items Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                currentFoods.forEach { food ->
                    DraggableFoodItem(
                        food = food,
                        mouthPosition = mouthPosition,
                        onEaten = { happinessBoost ->
                            scope.launch {
                                // Play specific eating sound for this food
                                val mediaPlayer = MediaPlayer.create(context, food.soundEffect)
                                mediaPlayer.start()
                                mediaPlayer.setOnCompletionListener { it.release() }
                                
                                // Update happiness and show eating animation
                                happiness = (happiness + happinessBoost).coerceIn(0f, 100f)
                                isEating = true
                                itemsEaten++
                                
                                // Update score and kid size
                                score += (happinessBoost * 10).toInt()
                                kidScale = (1f + (itemsEaten * 0.01f)).coerceAtMost(1.5f)
                                
                                updateMessage(itemsEaten) { message = it }
                                
                                // Replace the eaten food with a new random food
                                val remainingFoods = allFoods - currentFoods.toSet()
                                if (remainingFoods.isNotEmpty()) {
                                    currentFoods = currentFoods.map { 
                                        if (it == food) remainingFoods.random() else it 
                                    }
                                }
                                
                                delay(1000)
                                isEating = false
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DraggableFoodItem(
    food: FoodItem,
    mouthPosition: Offset,
    onEaten: (Float) -> Unit
) {
    var position by remember { mutableStateOf(Offset.Zero) }
    var originalPosition by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }
    var foodPosition by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .size(60.dp)
            .onGloballyPositioned { coordinates ->
                if (originalPosition == Offset.Zero) {
                    originalPosition = coordinates.positionInRoot()
                    foodPosition = originalPosition
                }
            }
            .graphicsLayer {
                if (isDragging) {
                    translationX = position.x
                    translationY = position.y
                }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        position = Offset.Zero
                    },
                    onDragEnd = {
                        isDragging = false
                        // Calculate the final position of the food
                        val finalFoodPosition = foodPosition + position
                        // Check if food is close to mouth
                        val distance = (finalFoodPosition - mouthPosition).getDistance()
                        if (distance < 150f) { // Increased threshold for better detection
                            onEaten(food.happinessBoost)
                        }
                        // Reset position with animation
                        position = Offset.Zero
                        foodPosition = originalPosition
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        position += dragAmount
                        foodPosition += dragAmount
                    }
                )
            }
    ) {
        Image(
            painter = painterResource(id = food.icon),
            contentDescription = food.name,
            modifier = Modifier.size(48.dp)
        )
    }
}

private fun updateMessage(itemsEaten: Int, setMessage: (String) -> Unit) {
    val message = when {
        itemsEaten % 5 == 0 -> "Yum yum yum!"
        itemsEaten % 3 == 0 -> "This is delicious!"
        itemsEaten % 2 == 0 -> "More please!"
        else -> ""
    }
    setMessage(message)
} 