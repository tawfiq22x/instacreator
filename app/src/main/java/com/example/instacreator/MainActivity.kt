package com.example.instacreator

import android.content.ClipData
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.instacreator.ui.MainViewModel
import com.google.gson.Gson
import java.util.Date

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Instagram Creator", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.fullName,
            onValueChange = { viewModel.updateFullName(it) },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.username,
            onValueChange = { viewModel.updateUsername(it) },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.password,
            onValueChange = { viewModel.updatePassword(it) },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.createAccount(state.fullName, state.username, state.password) },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (state.isLoading) "Creating..." else "Create Account")
        }
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            state.statusMessage,
            color = if (state.statusMessage.contains("✅")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )

        if (state.cookies.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Cookies:", style = MaterialTheme.typography.titleMedium)
            Text(Gson().toJson(state.cookies), fontSize = 12.sp)
            Button(
                onClick = {
                    val json = Gson().toJson(state.cookies)
                    clipboard.setText(ClipData.newPlainText("cookies", json))
                    Toast.makeText(context, "Cookies copied!", Toast.LENGTH_SHORT).show()
                }
            ) {
                Text("Copy Cookies")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("History", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.height(200.dp)) {
            items(state.history) { account ->
                Text("${account.username} - ${Date(account.timestamp)}")
                Divider()
            }
        }
    }
}
