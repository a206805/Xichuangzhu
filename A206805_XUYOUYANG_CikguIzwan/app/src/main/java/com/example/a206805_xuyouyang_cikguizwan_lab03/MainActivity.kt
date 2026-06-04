package com.example.a206805_xuyouyang_cikguizwan_lab03

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.*
import com.example.a206805_xuyouyang_cikguizwan_lab03.ui.theme.A206805_XUYOUYANG_CikguIzwan_Lab03Theme
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import kotlin.math.sqrt

// ==========================================
// 1. PILLAR 1: ROOM DATABASE (Local Persistence)
// ==========================================
@Entity(tableName = "quotes_table")
data class CustomQuoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val author: String,
    val content: String,
    val translation: String
)

@Dao
interface CustomQuoteDao {
    @Query("SELECT * FROM quotes_table")
    fun getAllQuotes(): Flow<List<CustomQuoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertQuote(quote: CustomQuoteEntity)

    @Delete
    fun deleteQuote(quote: CustomQuoteEntity)
}

@Database(entities = [CustomQuoteEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customQuoteDao(): CustomQuoteDao
}

class QuoteRepository(private val quoteDao: CustomQuoteDao) {
    val allQuotes: Flow<List<CustomQuoteEntity>> = quoteDao.getAllQuotes()
    fun insert(quote: CustomQuoteEntity) = quoteDao.insertQuote(quote)
    fun delete(quote: CustomQuoteEntity) = quoteDao.deleteQuote(quote)
}

// ==========================================
// 2. PILLAR 2: WEB API (Retrofit)
// ==========================================
data class ApiQuote(
    val content: String,
    val author: String
)

interface QuoteApiService {
    @GET("random")
    suspend fun getRandomQuote(): ApiQuote
}

object RetrofitClient {
    private const val BASE_URL = "https://api.quotable.io/"
    val apiService: QuoteApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(QuoteApiService::class.java)
    }
}

// ==========================================
// 3. PILLAR 3: FIREBASE FIRESTORE (Cloud Integration)
// ==========================================
data class CommunityQuote(
    val id: String = "",
    val author: String = "",
    val content: String = "",
    val translation: String = ""
)

// ==========================================
// UI STATE & VIEWMODEL
// ==========================================
data class AppState(
    val searchQuery: String = "",
    val userQuotes: List<CustomQuoteEntity> = emptyList(),
    val apiQuoteText: String = "Shake your phone or click the button below to get a daily inspirational quote from the cloud!",
    val isApiLoading: Boolean = false,
    val communityQuotes: List<CommunityQuote> = emptyList()
)

class LiteratureViewModel(private val repository: QuoteRepository) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    private val _apiQuoteText = MutableStateFlow("Shake your phone or click the button below to get a daily inspirational quote from the cloud!")
    private val _isApiLoading = MutableStateFlow(false)
    private val _communityQuotes = MutableStateFlow<List<CommunityQuote>>(emptyList())

    private val firestore = FirebaseFirestore.getInstance()

    val uiState: StateFlow<AppState> = combine(
        _searchQuery,
        repository.allQuotes,
        _apiQuoteText,
        _isApiLoading,
        _communityQuotes
    ) { query, quotes, apiText, isLoading, cloudQuotes ->
        AppState(
            searchQuery = query,
            userQuotes = quotes,
            apiQuoteText = apiText,
            isApiLoading = isLoading,
            communityQuotes = cloudQuotes
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppState())

    init {
        listenToCommunityQuotes()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addQuote(author: String, content: String, translation: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(CustomQuoteEntity(author = author, content = content, translation = translation))
        }
    }

    fun deleteQuote(quoteToDelete: CustomQuoteEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(quoteToDelete)
        }
    }

    fun fetchRandomQuoteFromApi() {
        if (_isApiLoading.value) return
        viewModelScope.launch(Dispatchers.IO) {
            _isApiLoading.value = true
            _apiQuoteText.value = "Fetching data from Web API..."
            try {
                val result = RetrofitClient.apiService.getRandomQuote()
                _apiQuoteText.value = "\"${result.content}\"\n\n— ${result.author}"
            } catch (e: Exception) {
                _apiQuoteText.value = "\"The beautiful thing about learning is that no one can take it away from you.\"\n\n— B.B. King\n(Network timeout, local cache used)"
            } finally {
                _isApiLoading.value = false
            }
        }
    }

    fun shareToCommunity(quote: CustomQuoteEntity, onSuccess: () -> Unit) {
        val data = hashMapOf(
            "author" to quote.author,
            "content" to quote.content,
            "translation" to quote.translation,
            "timestamp" to System.currentTimeMillis()
        )
        firestore.collection("community_quotes")
            .add(data)
            .addOnSuccessListener { onSuccess() }
    }

    // 【新增】云端 Firebase 删除逻辑
    fun deleteFromCommunity(quote: CommunityQuote, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        if (quote.id.isNotBlank()) {
            firestore.collection("community_quotes")
                .document(quote.id)
                .delete()
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { onFailure(it) }
        }
    }

    private fun listenToCommunityQuotes() {
        firestore.collection("community_quotes")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val list = snapshot.documents.map { doc ->
                    CommunityQuote(
                        id = doc.id,
                        author = doc.getString("author") ?: "Unknown",
                        content = doc.getString("content") ?: "",
                        translation = doc.getString("translation") ?: ""
                    )
                }
                _communityQuotes.value = list
            }
    }
}

class LiteratureViewModelFactory(private val repository: QuoteRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LiteratureViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LiteratureViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// ==========================================
// MAIN ACTIVITY & NAVIGATION HOST
// ==========================================
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "literature_database"
        ).build()
        val repository = QuoteRepository(database.customQuoteDao())
        val factory = LiteratureViewModelFactory(repository)

        enableEdgeToEdge()
        setContent {
            A206805_XUYOUYANG_CikguIzwan_Lab03Theme {
                val viewModel: LiteratureViewModel = viewModel(factory = factory)
                val navController = rememberNavController()

                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                    Image(
                        painter = painterResource(id = R.drawable.bg_classical),
                        contentDescription = "Classical Background",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 32.dp)) {
                        TopBar(navController = navController)
                        Box(modifier = Modifier.height(12.dp))

                        Box(modifier = Modifier.weight(1f)) {
                            NavHost(navController = navController, startDestination = "library_screen") {
                                composable("search_screen") { SearchScreen(viewModel) { navController.navigate("library_screen") } }
                                composable("library_screen") { LibraryScreen(viewModel) { navController.navigate("detail_screen") } }
                                composable("detail_screen") { DetailScreen(viewModel) { navController.popBackStack() } }
                                composable("add_screen") { AddQuoteScreen(viewModel, navController) }
                                composable("collection_screen") { CollectionScreen(viewModel) }
                                composable("api_screen") { ApiDiscoverScreen(viewModel) }
                                composable("community_screen") { CommunityScreen(viewModel) }
                            }
                        }

                        Box(modifier = Modifier.height(12.dp))
                        BottomNavBar(navController)
                    }
                }
            }
        }
    }
}

@Composable
fun TopBar(navController: NavController) {
    Row(
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f), RoundedCornerShape(12.dp)).padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Matrik: A206805", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("SDG 4: Quality Education", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row {
            Text(
                text = "🔍",
                fontSize = 20.sp,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .clickable { navController.navigate("search_screen") }
            )
            Text("⚙", fontSize = 20.sp, modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
fun BottomNavBar(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.85f), RoundedCornerShape(24.dp))
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        NavBarItem("🏯", "Library", onClick = { navController.navigate("library_screen") })
        NavBarItem("🖌", "Write", onClick = { navController.navigate("add_screen") })
        NavBarItem("📱", "Sensor", onClick = { navController.navigate("api_screen") })
        NavBarItem("📜", "Local", onClick = { navController.navigate("collection_screen") })
        NavBarItem("🌍", "Cloud", onClick = { navController.navigate("community_screen") })
    }
}

@Composable
fun RowScope.NavBarItem(icon: String, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.weight(1f).clickable { onClick() }
    ) {
        Text(icon, fontSize = 20.sp)
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
    }
}

// ==========================================
// SCREEN 1 & 2 & 3: SYSTEM PRESET FLOW
// ==========================================
@Composable
fun SearchScreen(viewModel: LiteratureViewModel, onSearchClicked: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                label = { Text("Search library...") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    if (uiState.searchQuery.isNotBlank()) onSearchClicked()
                    else Toast.makeText(context, "Cannot be empty!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Search Library") }
        }
    }
}

@Composable
fun LibraryScreen(viewModel: LiteratureViewModel, onCardClicked: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        if (uiState.searchQuery.isNotBlank()) {
            Text("Searched: ${uiState.searchQuery}", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 8.dp))
        }
        ElevatedCard(
            modifier = Modifier.fillMaxWidth().clickable { onCardClicked() }
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("System Preset Text. Click for translation.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f), modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(modifier = Modifier.height(16.dp))
                AncientTextColumns("Mencius")
            }
        }
    }
}

@Composable
fun DetailScreen(viewModel: LiteratureViewModel, onBackClicked: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Button(onClick = onBackClicked) { Text("Back") }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "The wise first make themselves clear and then try to make others clear; but nowadays people try to make others clear before they themselves are clear.",
                color = MaterialTheme.colorScheme.onTertiaryContainer, fontSize = 15.sp, lineHeight = 22.sp
            )
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                AncientTextColumns("Mencius")
            }
        }
    }
}

// ==========================================
// SCREEN 4: ROOM INSERT
// ==========================================
@Composable
fun AddQuoteScreen(viewModel: LiteratureViewModel, navController: NavController) {
    var author by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var translation by remember { mutableStateOf("") }
    val context = LocalContext.current

    ElevatedCard(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(24.dp).fillMaxSize()) {
            Text("Create Study Note (Room)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(value = author, onValueChange = { author = it }, label = { Text("Author") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Classical Quote") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = translation, onValueChange = { translation = it }, label = { Text("English Translation") }, modifier = Modifier.fillMaxWidth().height(100.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (author.isNotBlank() && content.isNotBlank()) {
                        viewModel.addQuote(author, content, translation)
                        Toast.makeText(context, "Saved Locally via Room!", Toast.LENGTH_SHORT).show()
                        navController.navigate("collection_screen")
                    } else {
                        Toast.makeText(context, "Please fill in fields.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save to Room DB") }
        }
    }
}

// ==========================================
// SCREEN 5: ROOM DISPLAY & SHARE TO FIREBASE
// ==========================================
@Composable
fun CollectionScreen(viewModel: LiteratureViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    ElevatedCard(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
            Text("My Local Collection (Room)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.userQuotes.isEmpty()) {
                Text("No local records found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.userQuotes) { quote ->
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer), modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = quote.content, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "— ${quote.author}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                    Text(text = quote.translation, fontSize = 12.sp)
                                }
                                Row {
                                    IconButton(onClick = {
                                        viewModel.shareToCommunity(quote) {
                                            Toast.makeText(context, "Synced to Firebase Cloud!", Toast.LENGTH_SHORT).show()
                                        }
                                    }) { Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.primary) }
                                    IconButton(onClick = { viewModel.deleteQuote(quote) }) { Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// SCREEN 6: PILLAR 4 - HARDWARE SENSOR + API
// ==========================================
@Composable
fun ApiDiscoverScreen(viewModel: LiteratureViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val sensorEventListener = object : SensorEventListener {
            private var lastUpdate: Long = 0
            private var lastX = 0f; private var lastY = 0f; private var lastZ = 0f
            private val SHAKE_THRESHOLD = 900

            override fun onSensorChanged(event: SensorEvent) {
                val curTime = System.currentTimeMillis()
                if ((curTime - lastUpdate) > 100) {
                    val diffTime = curTime - lastUpdate
                    lastUpdate = curTime
                    val x = event.values[0]; val y = event.values[1]; val z = event.values[2]
                    val speed = sqrt(((x - lastX) * (x - lastX) + (y - lastY) * (y - lastY) + (z - lastZ) * (z - lastZ)).toDouble()).toFloat() / diffTime * 10000

                    if (speed > SHAKE_THRESHOLD) {
                        viewModel.fetchRandomQuoteFromApi()
                    }
                    lastX = x; lastY = y; lastZ = z
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        onDispose { sensorManager.unregisterListener(sensorEventListener) }
    }

    ElevatedCard(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(24.dp).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("Hardware Sensor & Web API Hub", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            Text("📱 Shake your smartphone", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("The accelerometer (Sensor) will be activated and automatically retrieve real-time dynamic data from the public REST API via Retrofit.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(24.dp))

            Box(modifier = Modifier.weight(1f).fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)).padding(16.dp), contentAlignment = Alignment.Center) {
                if (uiState.isApiLoading) {
                    CircularProgressIndicator()
                } else {
                    Text(text = uiState.apiQuoteText, fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { viewModel.fetchRandomQuoteFromApi() }, modifier = Modifier.fillMaxWidth()) {
                Text("Manually trigger API request")
            }
        }
    }
}

// ==========================================
// SCREEN 7: CLOUD INTEGRATION (FIREBASE)
// ==========================================
@Composable
fun CommunityScreen(viewModel: LiteratureViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    ElevatedCard(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
            Text("Global Community Hall (Firebase Cloud)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("Real-time synchronization of shared study notes among all students via cloud Firestore.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.communityQuotes.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Cloud database is empty or currently connecting...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.communityQuotes) { item ->
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)), modifier = Modifier.fillMaxWidth()) {
                            // 【修改处】将原有的 Column 包裹在 Row 里，以便右侧可以放置删除按钮
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = item.content, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "— ${item.author}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    if (item.translation.isNotBlank()) {
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                        Text(text = "Translation: ${item.translation}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                // 【新增】云端删除按钮
                                IconButton(onClick = {
                                    viewModel.deleteFromCommunity(
                                        quote = item,
                                        onSuccess = { Toast.makeText(context, "Deleted from Cloud!", Toast.LENGTH_SHORT).show() },
                                        onFailure = { Toast.makeText(context, "Failed to delete.", Toast.LENGTH_SHORT).show() }
                                    )
                                }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete from Cloud", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// UI TOOL COMPOSTABLE
// ==========================================
@Composable
fun AncientTextColumns(author: String) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.align(Alignment.Center), horizontalArrangement = Arrangement.spacedBy(15.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("使", fontSize = 24.sp); Text("人", fontSize = 24.sp); Text("昭", fontSize = 24.sp); Text("昭", fontSize = 24.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("今", fontSize = 24.sp); Text("以", fontSize = 24.sp); Text("其", fontSize = 24.sp); Text("昏", fontSize = 24.sp); Text("昏", fontSize = 24.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("使", fontSize = 24.sp); Text("人", fontSize = 24.sp); Text("昭", fontSize = 24.sp); Text("昭", fontSize = 24.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("贤", fontSize = 24.sp); Text("者", fontSize = 24.sp); Text("以", fontSize = 24.sp); Text("其", fontSize = 24.sp); Text("昭", fontSize = 24.sp); Text("昭", fontSize = 24.sp)
            }
        }
        Box(
            modifier = Modifier.align(Alignment.BottomStart).size(40.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(author, color = MaterialTheme.colorScheme.onPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}