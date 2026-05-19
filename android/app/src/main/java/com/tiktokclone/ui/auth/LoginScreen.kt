package com.tiktokclone.ui.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onLoginSuccess: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.handleGoogleSignInResult(result)
        }
    }

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) onLoginSuccess()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        Text(
            text = "TikTok Clone",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Log in to your account",
            fontSize = 16.sp,
            color = Color.Gray,
        )

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFE2C55),
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color(0xFFFE2C55),
                focusedLabelColor = Color(0xFFFE2C55),
                unfocusedLabelColor = Color.Gray,
                focusedLeadingIconColor = Color(0xFFFE2C55),
                unfocusedLeadingIconColor = Color.Gray,
            ),
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFE2C55),
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color(0xFFFE2C55),
                focusedLabelColor = Color(0xFFFE2C55),
                unfocusedLabelColor = Color.Gray,
                focusedLeadingIconColor = Color(0xFFFE2C55),
                unfocusedLeadingIconColor = Color.Gray,
            ),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Forgot password?",
            color = Color(0xFFFE2C55),
            fontSize = 14.sp,
            modifier = Modifier
                .align(Alignment.End)
                .clickable { onNavigateToForgotPassword() },
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.login(email, password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE2C55)),
            shape = RoundedCornerShape(4.dp),
            enabled = !uiState.isLoading && email.isNotBlank() && password.isNotBlank(),
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Text("Log In", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        uiState.error?.let { error ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = error, color = Color.Red, fontSize = 14.sp, textAlign = TextAlign.Center)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Divider(modifier = Modifier.weight(1f), color = Color.Gray.copy(alpha = 0.5f))
            Text("  OR  ", color = Color.Gray, fontSize = 14.sp)
            Divider(modifier = Modifier.weight(1f), color = Color.Gray.copy(alpha = 0.5f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        SocialLoginButton(
            text = "Continue with Google",
            icon = Icons.Default.AccountCircle,
            onClick = {
                val signInIntent = viewModel.getGoogleSignInIntent()
                googleSignInLauncher.launch(signInIntent)
            },
        )

        Spacer(modifier = Modifier.height(12.dp))

        SocialLoginButton(
            text = "Continue with Facebook",
            icon = Icons.Default.Facebook,
            onClick = { /* Facebook sign in - future implementation */ },
        )

        Spacer(modifier = Modifier.height(12.dp))

        SocialLoginButton(
            text = "Continue with Phone",
            icon = Icons.Default.Phone,
            onClick = { /* Phone auth - future implementation */ },
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Don't have an account? ", color = Color.Gray, fontSize = 14.sp)
            Text(
                text = "Sign Up",
                color = Color(0xFFFE2C55),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onNavigateToRegister() },
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun SocialLoginButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = androidx.compose.ui.graphics.SolidColor(Color.Gray.copy(alpha = 0.5f))
        ),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, fontSize = 14.sp)
    }
}
