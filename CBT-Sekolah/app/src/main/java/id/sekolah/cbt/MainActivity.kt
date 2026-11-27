package id.sekolah.cbt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = androidx.compose.ui.graphics.Color(0xFF1746A2),
                    secondary = androidx.compose.ui.graphics.Color(0xFF16A085),
                    background = androidx.compose.ui.graphics.Color(0xFFF4F7FC)
                )
            ) {
                CbtApp()
            }
        }
    }
}

@Composable
private fun CbtApp() {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var signedIn by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        if (signedIn) {
            DashboardScreen(
                username = username.trim(),
                onLogout = {
                    signedIn = false
                    password = ""
                    message = ""
                }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("CBT", fontSize = 42.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                Text("SEKOLAH", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                Text("Computer Based Test", color = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.height(32.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Login Peserta", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text("Masukkan akun yang diberikan oleh sekolah.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it; message = "" },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Username / NIS") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; message = "" },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Password") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation()
                        )
                        if (message.isNotBlank()) {
                            Text(message, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                        }
                        Button(
                            onClick = {
                                if (username.isBlank() || password.isBlank()) {
                                    message = "Username dan password wajib diisi."
                                } else {
                                    // Tahap 1: navigasi lokal untuk menguji tampilan.
                                    // Belum melakukan autentikasi dan jangan dipakai untuk ujian nyata.
                                    signedIn = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("MASUK", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
                Text("Aplikasi Ujian Digital Sekolah", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                Text("Versi 0.1.0 • Tahap pengembangan", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun DashboardScreen(username: String, onLogout: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("CBT SEKOLAH", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, fontSize = 22.sp)
                Text("Dashboard Peserta", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(onClick = onLogout) { Text("Keluar") }
        }
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)) {
            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Selamat datang", color = androidx.compose.ui.graphics.Color.White.copy(alpha = .8f))
                Text(username, color = androidx.compose.ui.graphics.Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                Text("Akun demo lokal", color = androidx.compose.ui.graphics.Color.White.copy(alpha = .8f), fontSize = 12.sp)
            }
        }
        Text("Ujian Saya", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Card(shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Belum ada ujian", fontWeight = FontWeight.Bold)
                Text("Daftar ujian akan muncul di sini setelah akun terhubung ke server sekolah.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedButton(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
                    Text("Belum tersedia")
                }
            }
        }
        Spacer(Modifier.weight(1f))
        Text("CBT Sekolah • Tahap 1 — antarmuka awal", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
    }
}
