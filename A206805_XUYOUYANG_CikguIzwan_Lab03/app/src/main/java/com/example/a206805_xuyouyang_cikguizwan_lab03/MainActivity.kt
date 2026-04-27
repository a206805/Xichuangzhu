// 声明这个文件属于哪个包。必须和你项目的真实包名一字不差，否则会报错。
package com.example.a206805_xuyouyang_cikguizwan_lab03

// 导入 Android 系统用来在页面间传递和保存基础状态的 Bundle 类
import android.os.Bundle
// 导入用于弹出短暂提示框（Toast）的工具类
import android.widget.Toast
// 导入 ComponentActivity，这是所有基于 Compose 构建界面的基础活动类
import androidx.activity.ComponentActivity
// 导入 setContent，它的作用是把 Compose 写的界面“贴”到屏幕上
import androidx.activity.compose.setContent
// 导入 enableEdgeToEdge，用来让应用画面铺满整个屏幕（延伸到状态栏和导航栏下面）
import androidx.activity.enableEdgeToEdge
// 导入 Image 组件，用于在界面上展示图片
import androidx.compose.foundation.Image
// 导入 background 修饰符，用来给组件涂上背景颜色
import androidx.compose.foundation.background
// 导入 clickable 修饰符，加上它之后，组件就能响应用户的点击事件了
import androidx.compose.foundation.clickable
// 导入基础布局组件，包括 Row(横排), Column(竖排), Box(叠放), Spacer(空白) 等
import androidx.compose.foundation.layout.*
// 导入 RoundedCornerShape，用来画圆角矩形
import androidx.compose.foundation.shape.RoundedCornerShape
// 导入 Material Design 3 风格的所有核心组件，比如 Text, Button, Card 等
import androidx.compose.material3.*
// 导入 Compose 的状态管理工具，比如 remember(记住状态) 和 mutableStateOf(可变状态)
import androidx.compose.runtime.*
// 导入 Alignment，用来控制组件在父容器里的对齐方式（比如居中对齐、靠左对齐）
import androidx.compose.ui.Alignment
// 导入 Modifier，它是 Compose 的灵魂，用来修改组件的大小、边距、点击行为等
import androidx.compose.ui.Modifier
// 导入 ContentScale，用来设置图片该怎么缩放（比如裁剪铺满还是拉伸铺满）
import androidx.compose.ui.layout.ContentScale
// 导入 LocalContext，用来获取当前应用的环境上下文（弹出 Toast 时必须要用到它）
import androidx.compose.ui.platform.LocalContext
// 导入 painterResource，用来从 res/drawable 文件夹里读取图片资源
import androidx.compose.ui.res.painterResource
// 导入 FontWeight，用来设置字体的粗细（比如 Bold 粗体）
import androidx.compose.ui.text.font.FontWeight
// 导入 dp 单位，这是屏幕密度无关的尺寸单位，用来设置宽高和边距
import androidx.compose.ui.unit.dp
// 导入 sp 单位，这是字体大小专用的单位，会随着用户手机系统字体大小的设置而缩放
import androidx.compose.ui.unit.sp
// 导入 ViewModel 的基础类，继承它就能让数据在手机屏幕旋转时不丢失
import androidx.lifecycle.ViewModel
// 导入 viewModel() 函数，在 Compose 里面用它来获取 ViewModel 的实例
import androidx.lifecycle.viewmodel.compose.viewModel
// 导入 NavController 类，它是控制页面跳转的核心指挥官
import androidx.navigation.NavController
// 导入 NavHost 组件，它是一个容器，用来装载所有的页面（路线）
import androidx.navigation.compose.NavHost
// 导入 composable 函数，用来在 NavHost 里面定义每一个具体的页面长什么样
import androidx.navigation.compose.composable
// 导入 rememberNavController 函数，用来创建并记住一个导航控制器
import androidx.navigation.compose.rememberNavController
// 导入 MutableStateFlow，它是协程库里的东西，用来存放可以改变的数据流
import kotlinx.coroutines.flow.MutableStateFlow
// 导入 StateFlow，它是只读的数据流，UI 界面只能看它，不能直接改它
import kotlinx.coroutines.flow.StateFlow
// 导入 asStateFlow，用来把可变的数据流变成只读的数据流暴露出去
import kotlinx.coroutines.flow.asStateFlow
// 导入 update，这是一个安全修改 MutableStateFlow 里面数据的方法
import kotlinx.coroutines.flow.update

// 导入你自定义的主题配置（颜色、字体等），请确保包名和你的项目一致
import com.example.a206805_xuyouyang_cikguizwan_lab03.ui.theme.A206805_XUYOUYANG_CikguIzwan_Lab03Theme

// ==========================================
// 1. 数据类 (Data Class)
// ==========================================
// 定义一个名为 ExcerptData 的数据类，相当于创建了一个专门装这三个数据的“快递盒”
data class ExcerptData(
    // 定义一个字符串类型的变量 searchQuery，默认是空字符串，用来存用户搜了什么
    val searchQuery: String = "",
    // 定义一个字符串类型的变量 author，默认值是"孟子"，用来存诗文的作者
    val author: String = "孟子",
    // 定义一个字符串类型的变量 translation，用来存这句古文的英文翻译
    val translation: String = "【translation】The wise first make themselves clear and then try to make others clear; but nowadays people try to make others clear before they themselves are clear."
)

// ==========================================
// 2. 视图模型 (ViewModel)
// ==========================================
// 创建一个类叫 LiteratureViewModel，继承自 ViewModel。它的命比 UI 长，能保护数据
class LiteratureViewModel : ViewModel() {
    // 创建一个私有的、可变的数据流 _uiState，里面装着刚刚定义的那个“快递盒”（数据类）
    private val _uiState = MutableStateFlow(ExcerptData())

    // 创建一个公开的、只读的数据流 uiState，把上面的 _uiState 变成只读版暴露给 UI 去用
    val uiState: StateFlow<ExcerptData> = _uiState.asStateFlow()

    // 定义一个公开的函数，专门用来更新用户的搜索词，UI 层会调用它
    fun updateSearchQuery(query: String) {
        // 调用数据流的 update 方法来更新里面的内容
        _uiState.update { currentState ->
            // 把现在的状态复制一份，只把 searchQuery 这一项替换成用户刚输入的新词
            currentState.copy(searchQuery = query)
        }
    }
}

// ==========================================
// 3. 主活动 (Main Activity)
// ==========================================
// MainActivity 是整个安卓应用的起点，继承自 ComponentActivity
class MainActivity : ComponentActivity() {
    // 重写 onCreate 方法，当应用一启动，系统就会最先调用这个方法
    override fun onCreate(savedInstanceState: Bundle?) {
        // 调用父类的 onCreate，做一些系统级别的初始化工作
        super.onCreate(savedInstanceState)
        // 开启全面屏模式，让内容可以显示到手机最顶部的状态栏区域
        enableEdgeToEdge()
        // 调用 setContent，从这里开始用 Compose 写 UI 界面
        setContent {
            // 套上你自定义的主题，这样里面的组件才会用你设定好的颜色和字体
            A206805_XUYOUYANG_CikguIzwan_Lab03Theme {
                // 通过 viewModel() 函数拿到 LiteratureViewModel 的实例，保证全剧共享这一个模型
                val viewModel: LiteratureViewModel = viewModel()
                // 创建并记住一个导航控制器，把它想象成应用的“方向盘”
                val navController = rememberNavController()

                // 画一个 Box 容器，modifier 设为填满全屏，并且涂上主题规定的背景色
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                    // 画一张图片作为背景
                    Image(
                        // 从 res/drawable 里找到叫 bg_classical 的图片
                        painter = painterResource(id = R.drawable.bg_classical),
                        // 无障碍提示文字
                        contentDescription = "Classical Background",
                        // 让图片填满整个屏幕
                        modifier = Modifier.fillMaxSize(),
                        // 设置裁剪模式，保持比例放大，多余的边角裁掉
                        contentScale = ContentScale.Crop
                    )

                    // 画一个 Column 容器，把里面的东西从上到下竖着排
                    Column(
                        // 填满屏幕，并且在四周留出 24dp 的安全边距，防止内容贴边
                        modifier = Modifier.fillMaxSize().padding(24.dp)
                    ) {
                        // 调用顶部的菜单栏组件
                        TopBar()
                        // 塞一个高度为 24dp 的透明盒子，用来把顶部栏和下面的内容隔开
                        Box(modifier = Modifier.height(24.dp))

                        // ==========================================
                        // 4. 导航宿主 (NavHost)
                        // ==========================================
                        // 画一个 Box 容器，weight(1f) 意思是让它像弹簧一样把中间剩下的空间全占满
                        Box(modifier = Modifier.weight(1f)) {
                            // 创建 NavHost，把方向盘 navController 交给它，设置默认主界面显示 "search_screen"
                            NavHost(navController = navController, startDestination = "search_screen") {

                                // 定义第一条路线：名字叫 "search_screen"
                                composable("search_screen") {
                                    // 在这里面显示 SearchScreen 组件
                                    SearchScreen(
                                        // 把存数据的 viewModel 传给它
                                        viewModel = viewModel,
                                        // 告诉它如果搜到了东西，就转动方向盘跳转到 "library_screen"
                                        onSearchClicked = { navController.navigate("library_screen") }
                                    )
                                }

                                // 定义第二条路线：名字叫 "library_screen"
                                composable("library_screen") {
                                    // 显示书库组件
                                    LibraryScreen(
                                        // 同样把 viewModel 传给它
                                        viewModel = viewModel,
                                        // 如果卡片被点了，就跳转到 "detail_screen"
                                        onCardClicked = { navController.navigate("detail_screen") }
                                    )
                                }

                                // 定义第三条路线：名字叫 "detail_screen"
                                composable("detail_screen") {
                                    // 显示详情页组件
                                    DetailScreen(
                                        // 传入 viewModel 读取翻译数据
                                        viewModel = viewModel,
                                        // 如果点了返回按钮，就执行 popBackStack() 把当前页面弹出，回到上一页
                                        onBackClicked = { navController.popBackStack() }
                                    )
                                }
                            }
                        }

                        // 再塞一个 16dp 的盒子，把导航区和底部菜单隔开
                        Box(modifier = Modifier.height(16.dp))
                        // 调用底部的导航栏组件，并把方向盘传给它，让它也能控制跳转
                        BottomNavBar(navController)
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. 独立 UI 组件
// ==========================================

// 定义一个叫 TopBar 的组件
@Composable
fun TopBar() {
    // 用 Row 容器把里面的东西横向排列
    Row(
        // 填满横向宽度
        modifier = Modifier.fillMaxWidth(),
        // 里面的内容两端对齐（左边的贴左，右边的贴右）
        horizontalArrangement = Arrangement.SpaceBetween,
        // 里面的内容在垂直方向上居中对齐
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 画一个汉堡菜单图标
        Text("☰", fontSize = 28.sp, color = MaterialTheme.colorScheme.onSurface)
        // 右边再套一个 Row，专门放两个小图标
        Row {
            // 画搜索图标，左右各留 12dp 的空隙
            Text("🔍", fontSize = 22.sp, modifier = Modifier.padding(horizontal = 12.dp))
            // 画设置图标，左边留 12dp 的空隙
            Text("⚙", fontSize = 22.sp, modifier = Modifier.padding(start = 12.dp))
        }
    }
}

// 定义底部导航栏组件，必须要接收 navController 才能工作
@Composable
fun BottomNavBar(navController: NavController) {
    // 横向排列的容器
    Row(
        modifier = Modifier
            // 填满宽度
            .fillMaxWidth()
            // 涂上半透明的底层背景色，并且把四个角切成 30dp 的圆角
            .background(MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.5f), RoundedCornerShape(30.dp))
            // 上下增加 12dp 的内边距
            .padding(vertical = 12.dp),
        // 让里面的四个按钮均匀地分散在横向空间里
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        // 第一个按钮：书库。设置点击事件：被点时跳转到 "library_screen"
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { navController.navigate("library_screen") }) {
            // 图标
            Text(text = "🏯", fontSize = 22.sp)
            // 底部的小字
            Text(text = "Library", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
        }
        // 第二个按钮：写作（没有加点击事件）
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "🖌", fontSize = 22.sp)
            Text(text = "Writing", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
        }
        // 第三个按钮：发现。设置点击事件：被点时跳回首屏 "search_screen"
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { navController.navigate("search_screen") }) {
            Text(text = "🧭", fontSize = 22.sp)
            Text(text = "Discovery", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
        }
        // 第四个按钮：我的（没有加点击事件）
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "👤", fontSize = 22.sp)
            Text(text = "Mine", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

// ==========================================
// 6. 三个主要屏幕
// ==========================================

// --- 屏幕 1: 搜索屏幕 ---
// 接收 viewModel 和一个叫 onSearchClicked 的动作函数
@Composable
fun SearchScreen(viewModel: LiteratureViewModel, onSearchClicked: () -> Unit) {
    // 像雷达一样监控 viewModel 里的状态，数据一变，界面就自动刷新
    val uiState by viewModel.uiState.collectAsState()
    // 获取当前的环境上下文（只有拿到了它，等下 Toast 才知道该在哪显示）
    val context = LocalContext.current

    // 画一个带有立体阴影的高级卡片
    ElevatedCard(
        // 设置卡片阴影高度为 6dp
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
        // 给卡片涂上属于 surface 层级的背景色
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        // 让卡片填充满整个横向宽度
        modifier = Modifier.fillMaxWidth()
    ) {
        // 卡片里面纵向排列，内边距 16dp
        Column(modifier = Modifier.padding(16.dp)) {
            // 写上你的学号
            Text("Matrik: a206805 (Discovery Mode)", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            // 留 4dp 空白
            Spacer(modifier = Modifier.height(4.dp))

            // 这是一个带边框的文本输入框
            OutlinedTextField(
                // 输入框里显示的内容，来自于刚才监控的 uiState 里的 searchQuery
                value = uiState.searchQuery,
                // 当你在键盘上打字时，会触发这里。it 就是你刚打的字
                onValueChange = {
                    // 马上把新打的字送回 ViewModel 里保存起来
                    viewModel.updateSearchQuery(it)
                },
                // 输入框没字时显示的灰色提示语
                label = { Text("Search for poems...") },
                // 填满宽度，高度固定为 60dp
                modifier = Modifier.fillMaxWidth().height(60.dp)
            )
            // 留 8dp 空白
            Spacer(modifier = Modifier.height(8.dp))

            // 画一个普通按钮
            Button(
                // 定义点击按钮时发生的事情
                onClick = {
                    // 检查 ViewModel 里的搜索词是不是空白的
                    if (uiState.searchQuery.isNotBlank()) {
                        // 如果有字，正常执行外面传进来的跳转函数
                        onSearchClicked()
                    } else {
                        // 如果没字或者是纯空格，调用 Toast 弹出一个黑色的短时间提示框
                        Toast.makeText(context, "The search content cannot be empty!", Toast.LENGTH_SHORT).show()
                    }
                },
                // 按钮填充满横向宽度
                modifier = Modifier.fillMaxWidth()
            ) {
                // 按钮上的白字
                Text("Quoting and excerpting sentences (Search)")
            }
        }
    }
}

// --- 屏幕 2: 书库预览屏幕 ---
@Composable
fun LibraryScreen(viewModel: LiteratureViewModel, onCardClicked: () -> Unit) {
    // 同样，先监控 ViewModel 里的状态数据
    val uiState by viewModel.uiState.collectAsState()

    // 纵向容器，填满最大空间
    Column(modifier = Modifier.fillMaxSize()) {
        // 画顶部的三个分类标签，横排
        Row(
            // 填满宽度
            modifier = Modifier.fillMaxWidth(),
            // 标签和标签之间隔开 25dp
            horizontalArrangement = Arrangement.spacedBy(25.dp)
        ) {
            // 第一个标签：摘录 (当前选中的状态)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // 字体加粗
                Text("Excerpt", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                // 在字下面画一个主色调的小横条，代表它被选中了
                Box(modifier = Modifier.width(16.dp).height(3.dp).background(MaterialTheme.colorScheme.primary))
            }
            // 第二个标签：分类
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // 字体正常
                Text("Category", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Normal)
            }
            // 第三个标签：文献
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // 字体正常
                Text("Literature", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Normal)
            }
        }

        // 留 16dp 空白
        Spacer(modifier = Modifier.height(16.dp))

        // 判断：如果搜索词不为空（说明从上个页面带词过来了）
        if (uiState.searchQuery.isNotBlank()) {
            // 就在界面上显示出“你搜索了：xxx”，证明 ViewModel 传值成功
            Text("Results for: ${uiState.searchQuery}", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 8.dp))
        }

        // 画古文预览卡片
        ElevatedCard(
            // 阴影高度 8dp
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
            // 背景色用特定的“宣纸黄” tertiaryContainer
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
            modifier = Modifier
                // 填满宽度
                .fillMaxWidth()
                // 整个卡片可以点击，点击后触发外面传进来的跳转事件
                .clickable { onCardClicked() }
        ) {
            // 卡片内部布局，边距 24dp
            Column(modifier = Modifier.padding(24.dp)) {
                // 写一行提示点击的半透明文字，并且在容器里水平居中
                Text("Click the card to view the translation.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f), modifier = Modifier.align(Alignment.CenterHorizontally))
                // 留 16dp 空白
                Spacer(modifier = Modifier.height(16.dp))
                // 调用自己写的古文排版组件，把 ViewModel 里的作者名传进去
                AncientTextColumns(uiState.author)
            }
        }
    }
}

// --- 屏幕 3: 详情屏幕 ---
@Composable
fun DetailScreen(viewModel: LiteratureViewModel, onBackClicked: () -> Unit) {
    // 监控 ViewModel 状态
    val uiState by viewModel.uiState.collectAsState()

    // 详情页的大卡片
    ElevatedCard(
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        // 让这张卡片完全填充满整个屏幕
        modifier = Modifier.fillMaxSize()
    ) {
        // 卡片内部纵向排列
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            // 返回按钮
            Button(
                // 点击执行返回上一页的操作
                onClick = onBackClicked,
                // 让这个按钮靠左对齐 (Start)
                modifier = Modifier.align(Alignment.Start)
            ) {
                // 按钮文字
                Text("Back to Library")
            }

            // 留 16dp 间距
            Spacer(modifier = Modifier.height(16.dp))
            // 画一条透明度为 30% 的细横线，用来分割按钮和文字
            HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            // 再留 16dp 间距
            Spacer(modifier = Modifier.height(16.dp))

            // 显示翻译的段落
            Text(
                // 文本内容直接从 ViewModel 里拿
                text = uiState.translation,
                // 文字颜色设为深墨褐色
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                // 字体大小 15sp
                fontSize = 15.sp,
                // 行高设为 22sp，让多行文字阅读更舒服
                lineHeight = 22.sp
            )

            // 画一个用来装古文的 Box 容器
            Box(
                modifier = Modifier
                    // 【关键技巧】weight(1f) 会把屏幕下面剩下的空地全霸占了
                    .weight(1f)
                    // 填满宽度
                    .fillMaxWidth(),
                // 因为霸占了所有空地，再设置居中，古文就能悬浮在剩余空间的正中央了
                contentAlignment = Alignment.Center
            ) {
                // 往正中央塞入古文排版组件
                AncientTextColumns(uiState.author)
            }
        }
    }
}

// ==========================================
// 7. 复用的古文组件 (只用写一次，到处调用)
// ==========================================
@Composable
// 这个组件接收一个参数 author（作者名）
fun AncientTextColumns(author: String) {
    // 最外层的盒子，填满横向宽度
    Box(modifier = Modifier.fillMaxWidth()) {
        // 横向排列容器
        Row(
            // 让这一排文字在 Box 里面完全居中
            modifier = Modifier.align(Alignment.Center),
            // 列与列之间隔开 15dp
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            // 第一竖列：使人昭昭
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // 每一个字都是单独的一个 Text，字体设为 24sp 大小
                Text("使", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                Text("人", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                Text("昭", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                Text("昭", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
            }
            // 第二竖列：今以其昏昏
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("今", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                Text("以", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                Text("其", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                Text("昏", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                Text("昏", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
            }
            // 第三竖列：使人昭昭
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("使", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                Text("人", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                Text("昭", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                Text("昭", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
            }
            // 第四竖列：贤者以其昭昭
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("贤", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                Text("者", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                Text("以", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                Text("其", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                Text("昭", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                Text("昭", fontSize = 24.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
            }
        }

        // 作者的红底小印章
        Box(
            modifier = Modifier
                // 钉在左下角 (BottomStart)
                .align(Alignment.BottomStart)
                // 强制要求宽高都是 40dp (正方形印章)
                .size(40.dp)
                // 涂上主色调背景色，角切成 4dp 的微微圆角
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)),
            // 让印章里面的字居中
            contentAlignment = Alignment.Center
        ) {
            // 显示外面传进来的作者名字，颜色为反白的 onPrimary，字体加粗
            Text(author, color = MaterialTheme.colorScheme.onPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}