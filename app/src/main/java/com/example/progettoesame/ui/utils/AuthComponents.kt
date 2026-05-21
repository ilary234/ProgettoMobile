package com.example.progettoesame.ui.utils

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.progettoesame.R
import com.example.progettoesame.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreenTemplate(
    onBackClick: () -> Unit,
    title: String,
    subtitle: String,
    buttonText: String,
    isLoading: Boolean = false,
    onSocialGoogleClick: () -> Unit,
    onButtonClick: (email: String, pass: String, username: String) -> Unit,
    onForgotPasswordClick: ((String) -> Unit)? = null,
    footerText: AnnotatedString,
    onFooterClick: () -> Unit,
    isSignUp: Boolean = false
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val isDark = AppTheme.isDark

    val appBackgroundColor = if (isDark) Color.Black else Color.White
    val appTextColor = if (isDark) Color.White else Color.Black
    val appGrayColor = if (isDark) Color(0x99FFFFFF) else Color.Gray
    val dividerColor = if (isDark) Color(0xFF303030) else Color(0xFFE0E0E0)
    val socialBtnColor = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF2F2F2)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Nome App",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Indietro",
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = appBackgroundColor,
                    titleContentColor = appTextColor,
                    navigationIconContentColor = appTextColor
                )
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(appBackgroundColor)
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = appTextColor
            )

            Text(
                text = subtitle,
                color = appGrayColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Email") },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Password") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = null)
                    }
                },
                shape = RoundedCornerShape(12.dp)
            )

            if (isSignUp) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Username") },
                    placeholder = { Text("Come vuoi farti chiamare?") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = "Password dimenticata?",
                        modifier = Modifier
                            .clickable { onForgotPasswordClick?.invoke(email) },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onButtonClick(email, password, username) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = appTextColor,
                    contentColor = appBackgroundColor
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = appBackgroundColor, modifier = Modifier.size(24.dp))
                } else {
                    Text(buttonText, color = appBackgroundColor)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = dividerColor)
                Text("oppure", modifier = Modifier.padding(horizontal = 16.dp), color = appGrayColor)
                HorizontalDivider(modifier = Modifier.weight(1f), color = dividerColor)
            }

            Spacer(modifier = Modifier.height(24.dp))

            SocialLoginButton(
                text = "Continua con Google",
                iconRes = R.drawable.ic_google,
                backgroundColor = socialBtnColor,
                contentColor = appTextColor,
                onClick = onSocialGoogleClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            LegalText(primaryColor = appTextColor, secondaryColor = appGrayColor)

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = footerText,
                modifier = Modifier.padding(bottom = 32.dp).clickable { onFooterClick() },
                color = appTextColor
            )
        }
    }
}

@Composable
fun SocialLoginButton(text: String, iconRes: Int, backgroundColor: Color, contentColor: Color, onClick : () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        elevation = null
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(30.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = text, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun LegalText(primaryColor: Color, secondaryColor: Color) {
    val annotatedText = buildAnnotatedString {
        withStyle(style = SpanStyle(color = secondaryColor)) {
            append("By clicking continue, you agree to our ")
        }
        withStyle(style = SpanStyle(color = primaryColor, fontWeight = FontWeight.Medium)) {
            append("Terms of Service")
        }
        withStyle(style = SpanStyle(color = secondaryColor)) {
            append(" and ")
        }
        withStyle(style = SpanStyle(color = primaryColor, fontWeight = FontWeight.Medium)) {
            append("Privacy Policy")
        }
    }

    Text(
        text = annotatedText,
        style = TextStyle(fontSize = 12.sp, textAlign = TextAlign.Center),
        modifier = Modifier.padding(bottom = 24.dp)
    )
}

@Composable
fun FeedbackBanner(message: String, isVisible: Boolean, isError: Boolean = true) {
    val backgroundColor = if (isError) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
    val contentColor = if (isError) Color(0xFFD32F2F) else Color(0xFF2E7D32)
    val icon = if (isError) Icons.Default.ErrorOutline else Icons.Default.CheckCircle

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .statusBarsPadding(),
            color = backgroundColor,
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = message,
                    color = contentColor,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}