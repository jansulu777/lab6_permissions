package com.example.myapplication

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        @OptIn(ExperimentalMaterial3Api::class)
                        TopAppBar(
                            title = { Text("Lecture 10: Permissions in Compose") },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                ) { innerPadding ->
                    PermissionsPracticeApp(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionsPracticeApp(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // State to trigger UI refresh on lifecycle resume (e.g. after returning from Settings)
    var lifecycleRefreshKey by remember { mutableIntStateOf(0) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                lifecycleRefreshKey++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HeaderSection()

        // Section 1: Normal Permissions (Install-time)
        NormalPermissionsSection(context = context)

        // Section 2: Single Runtime Permission (Camera)
        SingleRuntimePermissionSection(
            context = context,
            refreshKey = lifecycleRefreshKey
        )

        // Section 3: Multiple Runtime Permissions (Camera, Audio, Location)
        MultipleRuntimePermissionsSection(
            context = context,
            refreshKey = lifecycleRefreshKey
        )
    }
}

@Composable
fun HeaderSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Lecture 10 — Practice Class Work",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Demonstration of Install-time (Normal) and Runtime Permission handling in Jetpack Compose.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun NormalPermissionsSection(context: Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "1. Install-time Permissions (Normal)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Normal permissions (like INTERNET, VIBRATE, ACCESS_NETWORK_STATE) require no runtime dialogs or checks. They are granted automatically by the OS at install time.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VIBRATE Permission:",
                    fontWeight = FontWeight.SemiBold
                )
                Button(
                    onClick = {
                        triggerVibration(context)
                    }
                ) {
                    Text("Trigger Vibrate")
                }
            }
        }
    }
}

@Composable
fun SingleRuntimePermissionSection(
    context: Context,
    refreshKey: Int
) {
    val permission = Manifest.permission.CAMERA

    // State tracking permission status
    var isGranted by remember(refreshKey) {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var showRationaleDialog by remember { mutableStateOf(false) }
    var showDeniedPermanentlyDialog by remember { mutableStateOf(false) }

    // ActivityResultLauncher for requesting single permission
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        isGranted = granted
        if (!granted) {
            val activity = context as? ComponentActivity
            val showRationale = activity?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(it, permission)
            } ?: false

            if (!showRationale) {
                // User checked "Don't ask again" or permanently denied
                showDeniedPermanentlyDialog = true
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "2. Single Runtime Permission (Camera)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            PermissionStatusBadge(isGranted = isGranted)

            Spacer(modifier = Modifier.height(12.dp))

            if (isGranted) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "📷 Camera Feature Active",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Permission is granted! The app can now access camera preview and capture photos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val activity = context as? ComponentActivity
                            val showRationale = activity?.let {
                                ActivityCompat.shouldShowRequestPermissionRationale(it, permission)
                            } ?: false

                            if (showRationale) {
                                showRationaleDialog = true
                            } else {
                                cameraPermissionLauncher.launch(permission)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Request Camera")
                    }

                    OutlinedButton(
                        onClick = { openAppSettings(context) }
                    ) {
                        Text("App Settings")
                    }
                }
            }
        }
    }

    // Rationale Dialog
    if (showRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showRationaleDialog = false },
            title = { Text("Camera Permission Needed") },
            text = { Text("The app requires camera access to let you capture and upload photos. Please grant permission when prompted.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRationaleDialog = false
                        cameraPermissionLauncher.launch(permission)
                    }
                ) {
                    Text("Grant")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRationaleDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Permanently Denied Dialog
    if (showDeniedPermanentlyDialog) {
        AlertDialog(
            onDismissRequest = { showDeniedPermanentlyDialog = false },
            title = { Text("Permission Permanently Denied") },
            text = { Text("Camera permission was denied. Please enable it manually in App Settings to use photo features.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeniedPermanentlyDialog = false
                        openAppSettings(context)
                    }
                ) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeniedPermanentlyDialog = false }) {
                    Text("Dismiss")
                }
            }
        )
    }
}

@Composable
fun MultipleRuntimePermissionsSection(
    context: Context,
    refreshKey: Int
) {
    val permissionsToRequest = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    var permissionsStatusMap by remember(refreshKey) {
        mutableStateOf(
            permissionsToRequest.associateWith { perm ->
                ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
            }
        )
    }

    val multiplePermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsResult ->
        permissionsStatusMap = permissionsToRequest.associateWith { perm ->
            permissionsResult[perm] ?: (ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "3. Multiple Runtime Permissions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Requesting multiple permissions simultaneously (Camera, Microphone, Location) using RequestMultiplePermissions contract:",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(12.dp))

            permissionsToRequest.forEach { perm ->
                val shortName = perm.substringAfterLast('.')
                val granted = permissionsStatusMap[perm] ?: false
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = shortName, fontWeight = FontWeight.Medium)
                    PermissionStatusBadge(isGranted = granted)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        multiplePermissionsLauncher.launch(permissionsToRequest)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Request All Permissions")
                }

                OutlinedButton(
                    onClick = { openAppSettings(context) }
                ) {
                    Text("Settings")
                }
            }
        }
    }
}

@Composable
fun PermissionStatusBadge(isGranted: Boolean) {
    val backgroundColor = if (isGranted) Color(0xFFD4EDDA) else Color(0xFFF8D7DA)
    val textColor = if (isGranted) Color(0xFF155724) else Color(0xFF721C24)
    val text = if (isGranted) "GRANTED" else "DENIED"

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

private fun triggerVibration(context: Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator.vibrate(
                VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(200)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }
    context.startActivity(intent)
}

@Preview(showBackground = true)
@Composable
fun PermissionsPracticeAppPreview() {
    MyApplicationTheme {
        PermissionsPracticeApp()
    }
}
