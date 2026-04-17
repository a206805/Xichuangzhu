package com.example.a206805_xuyouyang_cikguizwan_lab03

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a206805_xuyouyang_cikguizwan_lab03.ui.theme.A206805_XUYOUYANG_CikguIzwan_Lab03Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            A206805_XUYOUYANG_CikguIzwan_Lab03Theme {
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // 1. 背景图
                        Image(
                            painter = painterResource(id = R.drawable.bg_classical),
                            contentDescription = "Classical Background",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // 2. 主体布局
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp)
                        ) {
                            // ==================== 顶部功能栏 ====================
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
                            Box(modifier = Modifier.height(24.dp))

                            // ==================== 分类标签 ====================
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(25.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Excerpt", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                                    Box(modifier = Modifier.width(16.dp).height(3.dp).background(MaterialTheme.colorScheme.primary))
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Category", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Normal)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Literature", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Normal)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Writer", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Normal)
                                }
                            }
                            Box(modifier = Modifier.height(16.dp))

                            // ==================== 搜索区卡片化 ====================
                            var searchQuery by remember { mutableStateOf("") }
                            var displayResult by remember { mutableStateOf("") }

                            ElevatedCard(
                                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
                                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Matrik: a206805", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        label = { Text("Search for poems...") },
                                        modifier = Modifier.fillMaxWidth().height(60.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { displayResult = if (searchQuery.isNotBlank()) "Searching the vast sea of books for you：「$searchQuery」" else "The search content cannot be empty！" },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Quoting and excerpting sentences (Search)")
                                    }
                                    if (displayResult.isNotEmpty()) {
                                        Text(text = displayResult, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
                                    }
                                }
                            }
                            Box(modifier = Modifier.height(16.dp))

                            // ==================== 中央内容卡片 (宣纸黄) ====================
                            var isExpanded by remember { mutableStateOf(false) }

                            ElevatedCard(
                                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
                                // 【关键修改 1】：完全去掉 alpha 透明度，呈现纯正的 tertiaryContainer (宣纸黄)
                                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium))
                                    .clickable { isExpanded = !isExpanded }
                            ) {
                                Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                                    // 【关键修改 2】：文字颜色换成配套的 onTertiaryContainer (墨褐色)
                                    Text("Click the card to view the translation.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f), modifier = Modifier.align(Alignment.CenterHorizontally))

                                    if (isExpanded) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "【translation】The wise first make themselves clear and then try to make others clear; but nowadays people try to make others clear before they themselves are clear.",
                                            color = MaterialTheme.colorScheme.onTertiaryContainer, // 翻译文字也是墨褐色
                                            fontSize = 15.sp,
                                            lineHeight = 22.sp
                                        )
                                    }

                                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                        Row(
                                            modifier = Modifier.align(Alignment.Center),
                                            horizontalArrangement = Arrangement.spacedBy(15.dp)
                                        ) {
                                            // 【关键修改 3】：所有竖排古文单字全部应用 onTertiaryContainer
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(text = "使", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                                Text(text = "人", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                                Text(text = "昭", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                                Text(text = "昭", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                            }
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(text = "今", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                                Text(text = "以", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                                Text(text = "其", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                                Text(text = "昏", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                                Text(text = "昏", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                            }
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(text = "使", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                                Text(text = "人", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                                Text(text = "昭", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                                Text(text = "昭", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                            }
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(text = "贤", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                                Text(text = "者", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                                Text(text = "以", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                                Text(text = "其", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                                Text(text = "昭", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                                Text(text = "昭", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                            }
                                        }
                                        Box(
                                            modifier = Modifier.align(Alignment.BottomStart).size(40.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("孟子", color = MaterialTheme.colorScheme.onPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                            Box(modifier = Modifier.height(16.dp))

                            // ==================== 底部导航栏 ====================
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.5f), RoundedCornerShape(30.dp))
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "🏯", fontSize = 22.sp)
                                    Text(text = "Library", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "🖌", fontSize = 22.sp)
                                    Text(text = "Writing", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "🧭", fontSize = 22.sp)
                                    Text(text = "Discovery", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "👤", fontSize = 22.sp)
                                    Text(text = "Mine", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}