package com.example.a206805_xuyouyang_cikguizwan_lab03

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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

import com.example.a206805_xuyouyang_cikguizwan_lab03.ui.theme.A206805_XUYOUYANG_CikguIzwan_Lab03Theme


data class CustomQuote(
    val author: String,
    val content: String,
    val translation: String
)

data class AppState(
    val searchQuery: String = "",
    val userQuotes: List<CustomQuote> = emptyList()
)


class LiteratureViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AppState())
    val uiState: StateFlow<AppState> = _uiState.asStateFlow()

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun addQuote(author: String, content: String, translation: String) {
        _uiState.update { currentState ->
            val newQuote = CustomQuote(author, content, translation)
            currentState.copy(userQuotes = currentState.userQuotes + newQuote)
        }
    }

    // 【新增】仅仅增加删除函数
    fun deleteQuote(quoteToDelete: CustomQuote) {
        _uiState.update { currentState ->
            currentState.copy(
                userQuotes = currentState.userQuotes.filter { it != quoteToDelete }
            )
        }
    }
}


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            A206805_XUYOUYANG_CikguIzwan_Lab03Theme {
                val viewModel: LiteratureViewModel = viewModel()
                val navController = rememberNavController()

                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

                    Image(
                        painter = painterResource(id = R.drawable.bg_classical),
                        contentDescription = "Classical Background",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                        TopBar()
                        Box(modifier = Modifier.height(24.dp))


                        Box(modifier = Modifier.weight(1f)) {

                            NavHost(navController = navController, startDestination = "library_screen") {

                                composable("search_screen") {
                                    SearchScreen(viewModel) { navController.navigate("library_screen") }
                                }

                                composable("library_screen") {
                                    LibraryScreen(viewModel) { navController.navigate("detail_screen") }
                                }

                                composable("detail_screen") {
                                    DetailScreen(viewModel) { navController.popBackStack() }
                                }

                                composable("add_screen") {
                                    AddQuoteScreen(viewModel, navController)
                                }

                                composable("collection_screen") {
                                    CollectionScreen(viewModel)
                                }
                            }
                        }

                        Box(modifier = Modifier.height(16.dp))
                        BottomNavBar(navController)
                    }
                }
            }
        }
    }
}


@Composable
fun TopBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("☰", fontSize = 28.sp, color = MaterialTheme.colorScheme.onSurface)
        Row {
            Text("🔍", fontSize = 22.sp, modifier = Modifier.padding(horizontal = 12.dp))
            Text("⚙", fontSize = 22.sp, modifier = Modifier.padding(start = 12.dp))
        }
    }
}

@Composable
fun BottomNavBar(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.5f), RoundedCornerShape(30.dp))
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { navController.navigate("library_screen") }) {
            Text("🏯", fontSize = 22.sp)
            Text("Library", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { navController.navigate("add_screen") }) {
            Text("🖌", fontSize = 22.sp)
            Text("Write", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { navController.navigate("search_screen") }) {
            Text("🧭", fontSize = 22.sp)
            Text("Discover", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { navController.navigate("collection_screen") }) {
            Text("📜", fontSize = 22.sp)
            Text("Collection", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}




@Composable
fun SearchScreen(viewModel: LiteratureViewModel, onSearchClicked: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    ElevatedCard(
        elevation = CardDefaults.elevatedCardElevation(6.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Matrik: A206805 (SDG 4: Quality Ed)", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                label = { Text("Search system library...") },
                modifier = Modifier.fillMaxWidth().height(60.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    if (uiState.searchQuery.isNotBlank()) onSearchClicked()
                    else Toast.makeText(context, "Cannot be empty!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Search Library")
            }
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
            elevation = CardDefaults.elevatedCardElevation(8.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
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
    ElevatedCard(
        elevation = CardDefaults.elevatedCardElevation(8.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Button(onClick = onBackClicked) { Text("Back") }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "The wise first make themselves clear and then try to make others clear; but nowadays people try to make others clear before they themselves are clear.",
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                fontSize = 15.sp,
                lineHeight = 22.sp
            )
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                AncientTextColumns("Mencius")
            }
        }
    }
}


@Composable
fun AddQuoteScreen(viewModel: LiteratureViewModel, navController: NavController) {
    var author by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var translation by remember { mutableStateOf("") }
    val context = LocalContext.current

    ElevatedCard(
        elevation = CardDefaults.elevatedCardElevation(6.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.padding(24.dp).fillMaxSize()) {
            Text("Create Study Note", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = author,
                onValueChange = { author = it },
                label = { Text("Author (e.g. Tang Xianzu)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Classical Quote") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = translation,
                onValueChange = { translation = it },
                label = { Text("Your English Reflection/Translation") },
                modifier = Modifier.fillMaxWidth().height(100.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (author.isNotBlank() && content.isNotBlank()) {
                        viewModel.addQuote(author, content, translation)
                        Toast.makeText(context, "Saved Successfully!", Toast.LENGTH_SHORT).show()
                        navController.navigate("collection_screen")
                    } else {
                        Toast.makeText(context, "Please fill in author and quote.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save to Collection")
            }
        }
    }
}


@Composable
fun CollectionScreen(viewModel: LiteratureViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    ElevatedCard(
        elevation = CardDefaults.elevatedCardElevation(6.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
            Text("My Classical Collection", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.userQuotes.isEmpty()) {
                Text("No records yet. Go to 'Write' to add some!", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.userQuotes) { quote ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // 【修改】仅仅在这里增加了 Row 和 IconButton，其他完全没变
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = quote.content, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "— ${quote.author}", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = quote.translation, fontSize = 14.sp, color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f))
                                }

                                // 删除按钮
                                IconButton(onClick = { viewModel.deleteQuote(quote) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Quote",
                                        tint = MaterialTheme.colorScheme.error // 使用主题标准的错误颜色(通常是红色)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun AncientTextColumns(author: String) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.align(Alignment.Center),
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("使", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                Text("人", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                Text("昭", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                Text("昭", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("今", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                Text("以", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                Text("其", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                Text("昏", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                Text("昏", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("使", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                Text("人", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                Text("昭", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                Text("昭", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("贤", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                Text("者", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                Text("以", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                Text("其", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                Text("昭", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                Text("昭", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(40.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(author, color = MaterialTheme.colorScheme.onPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}