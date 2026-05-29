package com.example.breeddetector

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

// DATA CLASSES
data class BreedResponse(val predictions: List<BreedResult>)
data class BreedResult(val breed: String, val confidence: Double, val info: BreedDetails?)
data class BreedDetails(val origin: String, val utility: String, val milk_yield: String, val traits: String, val fact: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var languageCode by remember { mutableStateOf("en") }
            var imageUri by remember { mutableStateOf<Uri?>(null) }
            var predictionList by remember { mutableStateOf<List<BreedResult>>(emptyList()) }
            var isLoading by remember { mutableStateOf(false) }
            var errorMessage by remember { mutableStateOf<String?>(null) }

            CattleBreedTheme {
                key(languageCode) {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        CattleScreen(
                            currentLang = languageCode,
                            imageUri = imageUri,
                            predictionList = predictionList,
                            isLoading = isLoading,
                            errorMessage = errorMessage,
                            onImageChange = { imageUri = it },
                            onPredictionChange = { predictionList = it },
                            onLoadingChange = { isLoading = it },
                            onErrorMessageChange = { errorMessage = it },
                            onLanguageChange = { languageCode = it }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CattleScreen(
    currentLang: String,
    imageUri: Uri?,
    predictionList: List<BreedResult>,
    isLoading: Boolean,
    errorMessage: String?,
    onImageChange: (Uri?) -> Unit,
    onPredictionChange: (List<BreedResult>) -> Unit,
    onLoadingChange: (Boolean) -> Unit,
    onErrorMessageChange: (String?) -> Unit,
    onLanguageChange: (String) -> Unit
) {
    val isHindi = currentLang == "hi"
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        onImageChange(uri)
        onErrorMessageChange(null)
        onPredictionChange(emptyList())
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (isHindi) "ब्रीडनोड" else "BreedNode",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Language Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text("English", style = MaterialTheme.typography.bodyMedium)
                Switch(
                    checked = isHindi,
                    onCheckedChange = { checked ->
                        val newLang = if (checked) "hi" else "en"
                        onLanguageChange(newLang)
                        val appLocale = LocaleListCompat.forLanguageTags(newLang)
                        AppCompatDelegate.setApplicationLocales(appLocale)
                    },
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Text("हिन्दी", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Image Preview Area
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = if (isHindi) "कोई फोटो नहीं चुनी गई" else "No Image Selected",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (isLoading) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = if (isHindi) "विश्लेषण किया जा रहा है..." else "Analyzing Traits...")
            } else if (predictionList.isNotEmpty()) {
                Text(
                    text = if (isHindi) "प्रमुख परिणाम (जानकारी के लिए टैप करें)" else "Top 3 Predictions (Tap to expand)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )

                predictionList.forEach { result ->
                    ProfessionalResultCard(result, isHindi)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Button(
                    onClick = { openMapsToFindVets(context) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(text = if (isHindi) "नजदीकी पशु चिकित्सक खोजें" else "Find Nearest Veterinary Center")
                }
            } else if (errorMessage != null) {
                Text(text = errorMessage, color = Color.Red, textAlign = TextAlign.Center)
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { launcher.launch("image/*") },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = if (isHindi) "फोटो चुनें" else "Pick Cow Image")
            }

            Spacer(modifier = Modifier.height(12.dp))

            FilledTonalButton(
                onClick = {
                    scope.launch {
                        onLoadingChange(true)
                        onErrorMessageChange(null)
                        uploadImage(context, imageUri) { results, error ->
                            onLoadingChange(false)
                            if (error != null) onErrorMessageChange(error)
                            else onPredictionChange(results)
                        }
                    }
                },
                enabled = imageUri != null && !isLoading,
                modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 24.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = if (isHindi) "नस्ल की पहचान करें" else "Identify Breed")
            }
        }
    }
}

@Composable
fun ProfessionalResultCard(result: BreedResult, isHindi: Boolean) {
    var isExpanded by remember { mutableStateOf(false) }
    val progress = (result.confidence / 100).toFloat()

    Card(
        modifier = Modifier.fillMaxWidth().clickable { isExpanded = !isExpanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = result.breed, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(
                        text = (if (isHindi) "विश्वास: " else "Confidence: ") + "${"%.1f".format(result.confidence)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Icon(if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null)
            }

            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = if (progress > 0.7f) Color(0xFF2E7D32) else Color(0xFFFFA000)
            )

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Divider(modifier = Modifier.padding(vertical = 12.dp))
                    result.info?.let { info ->
                        BreedDetailRow(if (isHindi) "📍 मूल स्थान" else "📍 Origin", info.origin)
                        BreedDetailRow(if (isHindi) "🏗️ उपयोगिता" else "🏗️ Utility", info.utility)
                        BreedDetailRow(if (isHindi) "🥛 दूध की मात्रा" else "🥛 Milk Yield", info.milk_yield)
                        BreedDetailRow(if (isHindi) "🐄 लक्षण" else "🐄 Traits", info.traits)

                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "💡 ${info.fact}",
                                modifier = Modifier.padding(10.dp),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BreedDetailRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 3.dp)) {
        Text(text = "$label: ", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(text = value, fontSize = 13.sp)
    }
}

suspend fun uploadImage(context: Context, uri: Uri?, onComplete: (List<BreedResult>, String?) -> Unit) {
    if (uri == null) return
    try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val fileBytes = inputStream?.readBytes() ?: return
        inputStream.close()

        val requestFile = fileBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", "image.jpg", requestFile)

        val response = RetrofitClient.instance.predictBreed(body)
        onComplete(response.predictions, null)
    } catch (e: Exception) {
        onComplete(emptyList(), "Connection Error: Check Server")
    }
}

fun openMapsToFindVets(context: Context) {
    val uri = Uri.parse("geo:0,0?q=veterinary+hospital+near+me")
    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
    intent.setPackage("com.google.android.apps.maps")
    context.startActivity(intent)
}

@Composable
fun CattleBreedTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF2E7D32),
            primaryContainer = Color(0xFFC8E6C9),
            onPrimaryContainer = Color(0xFF1B5E20)
        ),
        content = content
    )
}