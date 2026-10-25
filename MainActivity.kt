package com.example.nagakawair

import android.content.Context
import android.hardware.ConsumerIrManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Accent = Color(0xFF0866F5)
private val Page = Color(0xFFF1F2F5)
private val Muted = Color(0xFFE7E8EC)
private val Ink = Color(0xFF17181B)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { RemoteScreen() } }
    }
}

private enum class AcMode(val label: String, val short: String, val icon: ImageVector) {
    AUTO("Tự động", "AUTO", Icons.Default.Autorenew),
    COOL("Mát", "COOL", Icons.Default.Waves),
    HEAT("Nóng", "HEAT", Icons.Default.Thermostat),
    DRY("Khô", "DRY", Icons.Default.WaterDrop),
    FAN("Quạt", "FAN", Icons.Default.Air)
}

@Composable
private fun RemoteScreen() {
    val context = LocalContext.current
    val irManager = remember {
        context.getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager
    }
    val hasIr = irManager?.hasIrEmitter() == true

    var power by remember { mutableStateOf(false) }
    var temperature by remember { mutableIntStateOf(26) }
    var mode by remember { mutableStateOf(AcMode.COOL) }
    var fanSpeed by remember { mutableIntStateOf(3) }
    var swing by remember { mutableStateOf(false) }
    var status by remember {
        mutableStateOf(if (hasIr) "Có bộ phát IR · Chưa có bộ mã AUX/Nagakawa đã xác minh" else "Điện thoại không phát hiện bộ phát hồng ngoại IR")
    }
    var showMore by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    val features = remember {
        mutableStateMapOf(
            "AUX HEAT" to false, "BODY FEEL" to false, "DISPLAY" to true,
            "ECO" to false, "HEALTH" to false, "HUMIDIFICATION" to false,
            "LR_WIND_MODE" to false, "MUTE" to false, "TURBO" to false,
            "NGỦ" to false, "HẸN GIỜ" to false
        )
    }

    Surface(color = Page, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.ArrowBack, null, modifier = Modifier.size(28.dp), tint = Ink)
                Spacer(Modifier.width(18.dp))
                Text("Nagakawa Điều hòa", fontSize = 22.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.Settings, "Cài đặt")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(text = { Text("Kiểm tra hồng ngoại") }, onClick = {
                            status = if (hasIr) "Bộ phát IR có sẵn; cần bộ mã đúng model để điều khiển." else "Thiết bị không có bộ phát IR."
                            showMenu = false
                        })
                        DropdownMenuItem(text = { Text("Thông tin giao thức") }, onClick = {
                            status = "Chưa tích hợp bộ mã AUX/Nagakawa đã xác minh."
                            showMenu = false
                        })
                    }
                }
            }

            Spacer(Modifier.height(34.dp))
            Text("$temperature", fontSize = 92.sp, fontWeight = FontWeight.Light, color = Color.Black, lineHeight = 96.sp)
            Text("Nhiệt độ (°C)", color = Color.Gray, fontSize = 16.sp)

            Spacer(Modifier.height(76.dp))
            Row(
                modifier = Modifier.fillMaxWidth().background(Muted, RoundedCornerShape(24.dp)).padding(vertical = 22.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SummaryItem("Chế độ", mode.label)
                SummaryItem("Tốc độ quạt", fanSpeed.toString())
                SummaryItem("Hướng", if (swing) "Đảo gió" else "Lên")
            }

            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                BigButton("Nguồn", Icons.Default.PowerSettingsNew, power, Modifier.weight(1f)) {
                    power = !power
                    status = "Đã cập nhật trạng thái trong ứng dụng; chưa gửi lệnh máy lạnh vì chưa có bộ mã xác minh."
                }
                BigButton("Xoay", Icons.Default.SwapVert, swing, Modifier.weight(1f)) {
                    swing = !swing
                    status = "Đã cập nhật hướng gió trong ứng dụng; chưa gửi lệnh IR."
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(48.dp)).padding(horizontal = 22.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("−", fontSize = 36.sp, modifier = Modifier.clickable {
                    if (temperature > 16) temperature--
                    status = "Nhiệt độ cài đặt: $temperature°C · Chưa gửi lệnh IR."
                }.padding(horizontal = 10.dp))
                Text("Nhiệt độ", fontSize = 18.sp)
                Text("+", fontSize = 36.sp, modifier = Modifier.clickable {
                    if (temperature < 30) temperature++
                    status = "Nhiệt độ cài đặt: $temperature°C · Chưa gửi lệnh IR."
                }.padding(horizontal = 10.dp))
            }

            Spacer(Modifier.height(22.dp))
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(14.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        AcMode.entries.forEach { item ->
                            ModeButton(item, selected = mode == item) {
                                mode = item
                                status = "Đã chọn chế độ ${item.label}; chưa gửi lệnh IR."
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Chức năng điều khiển khác", fontSize = 19.sp, fontWeight = FontWeight.Medium,
                        modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    val labels = listOf("AUX HEAT", "BODY FEEL", "DISPLAY", "ECO", "HEALTH",
                        "HUMIDIFICATION", "LR_WIND_MODE", "MUTE", "TURBO")
                    labels.chunked(2).forEach { pair ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            pair.forEach { label ->
                                FeatureButton(label, features[label] == true, Modifier.weight(1f)) {
                                    features[label] = !(features[label] ?: false)
                                    status = "$label: đã đổi trạng thái giao diện; chưa gửi lệnh IR."
                                }
                            }
                            if (pair.size == 1) Spacer(Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                    TextButton(onClick = { showMore = !showMore }) {
                        Icon(Icons.Default.GridView, null)
                        Spacer(Modifier.width(6.dp))
                        Text(if (showMore) "Ẩn chức năng" else "Thêm chức năng")
                    }
                    if (showMore) {
                        listOf("NGỦ", "HẸN GIỜ").forEach { label ->
                            FeatureButton(label, features[label] == true, Modifier.fillMaxWidth()) {
                                features[label] = !(features[label] ?: false)
                                status = "$label: trạng thái giao diện đã đổi; chưa gửi lệnh IR."
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                        Text("Hẹn giờ hiện chỉ là trạng thái giao diện; chưa có bộ lập lịch điều khiển IR.", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
                Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.DeviceThermostat, null, tint = Accent)
                    Spacer(Modifier.width(8.dp))
                    Text(status, fontSize = 13.sp, color = Color.DarkGray)
                }
            }
            Spacer(Modifier.height(22.dp))
            Text("Lưu ý: giao diện và trạng thái cài đặt đã hoạt động. Cần bộ mã IR AUX/Nagakawa đúng model để điều khiển máy lạnh thật.", color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SummaryItem(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        Text(title, fontSize = 13.sp, color = Color.Gray)
    }
}

@Composable
private fun BigButton(label: String, icon: ImageVector, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val bg by animateColorAsState(if (selected) Color(0xFFDCE8FF) else Color.White, label = "buttonBg")
    Row(
        modifier = modifier.height(76.dp).background(bg, RoundedCornerShape(42.dp)).clickable(onClick = onClick).padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center
    ) {
        Icon(icon, null, tint = if (label == "Nguồn") Color(0xFFD13D3D) else Ink, modifier = Modifier.size(27.dp))
        Spacer(Modifier.width(10.dp))
        Text(label, fontSize = 17.sp)
    }
}

@Composable
private fun ModeButton(mode: AcMode, selected: Boolean, onClick: () -> Unit) {
    val bg by animateColorAsState(if (selected) Accent else Muted, label = "modeBg")
    val fg = if (selected) Color.White else Ink
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(58.dp).clickable(onClick = onClick)) {
        Box(Modifier.size(54.dp).background(bg, CircleShape), contentAlignment = Alignment.Center) {
            Icon(mode.icon, null, tint = fg, modifier = Modifier.size(27.dp))
        }
        Spacer(Modifier.height(7.dp))
        Text(mode.label, fontSize = 11.sp, color = if (selected) Accent else Color.DarkGray, textAlign = TextAlign.Center)
    }
}

@Composable
private fun FeatureButton(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val bg by animateColorAsState(if (selected) Color(0xFFDCE8FF) else Muted, label = "featureBg")
    val fg = if (selected) Accent else Ink
    Row(
        modifier = modifier.heightIn(min = 54.dp).background(bg, RoundedCornerShape(32.dp)).clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center
    ) {
        Text(label, fontSize = 13.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal, color = fg, textAlign = TextAlign.Center)
    }
}
