@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.progettoesame.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.progettoesame.ui.utils.FeedbackBanner
import com.example.progettoesame.ui.utils.PreviewCard
import com.example.progettoesame.ui.utils.TimeType
import com.example.progettoesame.ui.utils.Units
import com.example.progettoesame.ui.utils.createImageUriInGallery
import com.example.progettoesame.ui.utils.formatTime
import com.example.progettoesame.ui.viewmodels.NewRecipeViewModel
import kotlinx.coroutines.delay

@Composable
fun NewRecipeScreen(navController: NavController, newRecipeViewModel: NewRecipeViewModel, recipeId: String?) {
    var activeTimeDialog by remember { mutableStateOf<TimeType?>(null) }
    val categories by newRecipeViewModel.categories.collectAsStateWithLifecycle()
    val recipeState by newRecipeViewModel.state.collectAsStateWithLifecycle()
    val errorMessage by newRecipeViewModel.errorMessage.collectAsStateWithLifecycle()
    val isRefreshing by newRecipeViewModel.isRefreshing.collectAsStateWithLifecycle()
    val isSaved by newRecipeViewModel.isSaved.collectAsStateWithLifecycle()

    val ctx = LocalContext.current

    val appBackgroundColor = MaterialTheme.colorScheme.background
    val appTextColor = MaterialTheme.colorScheme.onBackground
    val containerSectionColor = MaterialTheme.colorScheme.surface

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            delay(4000)
            newRecipeViewModel.clearErrorMessage()
        }
    }

    LaunchedEffect(isSaved) {
        if (isSaved) {
            navController.navigateUp()
        }
    }

    key(activeTimeDialog) {
        activeTimeDialog?.let { type ->
            val minutes = when (type) {
                TimeType.PREPARATION -> recipeState.preparation
                TimeType.WAITING -> recipeState.waiting ?: 0
                TimeType.COOKING -> recipeState.cooking
            }

            val timePickerState = rememberTimePickerState(
                initialHour = minutes / 60,
                initialMinute = minutes % 60,
                is24Hour = true
            )

            TimeInputDialog(
                onDismiss = { activeTimeDialog = null },
                onConfirm = { newTime ->
                    when (type) {
                        TimeType.PREPARATION -> newRecipeViewModel.recipeActions.onPreparationChange(newTime)
                        TimeType.WAITING -> newRecipeViewModel.recipeActions.onWaitingChange(newTime)
                        TimeType.COOKING -> newRecipeViewModel.recipeActions.onCookingChange(newTime)
                    }
                    activeTimeDialog = null
                },
                timePickerState = when (type) {
                    TimeType.PREPARATION -> timePickerState
                    TimeType.WAITING -> timePickerState
                    TimeType.COOKING -> timePickerState
                }
            )
        }
    }

    Scaffold(
        containerColor = appBackgroundColor,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (recipeId == null) "Nuova ricetta" else "Modifica Ricetta", color = appTextColor, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = appBackgroundColor,
                    titleContentColor = appTextColor,
                    navigationIconContentColor = appTextColor
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back icon",
                            tint = appTextColor
                        )
                    }
                }
            )
        }
    ) {paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (recipeState.previewImageUrl != null) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        PreviewCard(recipeState.previewImageUrl!!, recipeState.title)
                        IconButton(
                            onClick = { newRecipeViewModel.recipeActions.onPreviewImageChange(null) },
                            modifier = Modifier.align(Alignment.BottomEnd)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Icon",
                                tint = appTextColor
                            )
                        }
                    }

                } else {
                    AddImageCard(
                        onNewUri = { newUri -> newRecipeViewModel.recipeActions.onPreviewImageChange(newUri) },
                        maxPhotos = 1,
                        ctx = ctx,
                        backgroundColor = appBackgroundColor,
                        iconTint = appTextColor.copy(alpha = 0.6f)
                    )
                }

                OutlinedTextField(
                    value = recipeState.title,
                    onValueChange = { newRecipeViewModel.recipeActions.onTitleChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Titolo") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = appTextColor,
                        unfocusedTextColor = appTextColor,
                        focusedLabelColor = appTextColor,
                        unfocusedLabelColor = appTextColor.copy(alpha = 0.6f),
                        focusedBorderColor = appTextColor,
                        unfocusedBorderColor = appTextColor.copy(alpha = 0.4f)
                    )
                )

                CategoryItem(
                    name = recipeState.category?.name ?: "",
                    categories = categories.categories.sortedBy { it.order }.map { it.name },
                    onCategoryChange = { newRecipeName -> newRecipeViewModel.recipeActions.onCategoryChange(newRecipeName) },
                    containerColor = containerSectionColor,
                    textColor = appTextColor
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Tempi:", color = appTextColor, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Normal)
                    TimeInputCard({ activeTimeDialog = TimeType.PREPARATION }, "Preparazione: ${formatTime(recipeState.preparation)}", containerSectionColor, appTextColor)
                    TimeInputCard({ activeTimeDialog = TimeType.WAITING }, "Riposo: ${formatTime(recipeState.waiting ?: 0)}", containerSectionColor, appTextColor)
                    TimeInputCard({ activeTimeDialog = TimeType.COOKING }, "Cottura: ${formatTime(recipeState.cooking)}", containerSectionColor, appTextColor)
                }

                Text("Ingredienti", color = appTextColor, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    recipeState.ingredients.forEachIndexed { index, ingredient ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IngredientItem(ingredient.name, ingredient.quantity, ingredient.unit,
                                index > 0,
                                { newName -> newRecipeViewModel.ingredientActions.onValueChange(index, ingredient.copy(name = newName)) },
                                { newQuantity -> newRecipeViewModel.ingredientActions.onValueChange(index, ingredient.copy(quantity = newQuantity)) },
                                { newUnit -> newRecipeViewModel.ingredientActions.onValueChange(index, ingredient.copy(unit = newUnit)) },
                                { newRecipeViewModel.ingredientActions.onDeleteIngredient(index) },
                                containerSectionColor,
                                appTextColor)
                        }
                    }
                    TextButton(
                        onClick = {
                            newRecipeViewModel.ingredientActions.onAddIngredient()
                        },
                        modifier = Modifier.align(alignment = Alignment.CenterHorizontally),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.textButtonColors(contentColor = appTextColor)
                    ) {
                        Text("Aggiungi ")
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Text("Procedimento", color = appTextColor, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        recipeState.steps.sortedBy { it.number }.forEach { step ->
                            StepItem(
                                step.number, step.description, step.imageUrls,
                                step.number > 1 && step.number == recipeState.steps.size, ctx,
                                { newDescription -> newRecipeViewModel.stepActions.onDescriptionChange(step.copy(description = newDescription)) },
                                { newRecipeViewModel.stepActions.onDeleteStep(step) },
                                { url -> newRecipeViewModel.stepActions.onAddImage(step.number, url) },
                                { imageIndex -> newRecipeViewModel.stepActions.onDeleteImage(step.number, imageIndex) },
                                containerSectionColor,
                                appTextColor,
                                appBackgroundColor)
                        }
                    }
                    TextButton(
                        onClick = { newRecipeViewModel.stepActions.onAddStep(recipeState.steps.size + 1) },
                        modifier = Modifier.align(alignment = Alignment.CenterHorizontally),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.textButtonColors(contentColor = appTextColor)
                    ) {
                        Text("Aggiungi ")
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Button(
                    onClick = { newRecipeViewModel.saveRecipe(ctx) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = appTextColor)
                ) {
                    Text("Salva", color = appBackgroundColor)
                }
            }
            errorMessage?.let {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 80.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    FeedbackBanner(it, true)
                }
            }
            if (isRefreshing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(appTextColor.copy(alpha = 0.3f))
                        .pointerInput(Unit) {},
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = appTextColor)
                }
            }
        }
    }
}

@Composable
fun TimeInputDialog(onDismiss: () -> Unit, onConfirm: (Int) -> Unit, timePickerState: TimePickerState) {
        AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(timePickerState.hour * 60 + timePickerState.minute) }) {
                Text("Salva")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        },
        title = {
            Text(text = "Imposta il tempo")
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TimeInput(state = timePickerState)
            }
        },
        containerColor = Color.White,
        textContentColor = Color.Black,
        titleContentColor = Color.Black
    )
}

@Composable
fun TimeInputCard(onClick: () -> Unit, text: String, containerColor: Color, textColor: Color) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text, color = textColor, style = MaterialTheme.typography.bodyMedium)
            Icon(Icons.Default.Timer, contentDescription = "Clock icon", tint = textColor)
        }
    }
}

@Composable
fun CategoryItem(name: String, categories: List<String>, onCategoryChange: (String) -> Unit, containerColor: Color, textColor: Color) {
    var isCategoryMenuExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = isCategoryMenuExpanded,
        onExpandedChange = { isCategoryMenuExpanded = !isCategoryMenuExpanded }
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { },
            readOnly = true,
            label = { Text("Categoria") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryMenuExpanded) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = textColor,
                unfocusedTextColor = textColor,
                focusedLabelColor = textColor,
                unfocusedLabelColor = textColor.copy(alpha = 0.6f),
                focusedBorderColor = textColor,
                unfocusedBorderColor = textColor.copy(alpha = 0.4f)
            )
        )

        ExposedDropdownMenu(
            expanded = isCategoryMenuExpanded,
            onDismissRequest = { isCategoryMenuExpanded = false },
            modifier = Modifier.background(color = containerColor)
        ) {
            categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category, color = textColor) },
                        onClick = {
                            onCategoryChange(category)
                            isCategoryMenuExpanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
        }
    }
}

@Composable
fun IngredientItem(name: String, quantity: Float, unit: String, isDeletable: Boolean,
                   onNameChange: (String) -> Unit,
                   onQuantityChange: (Float) -> Unit,
                   onUnitChange: (String) -> Unit,
                   onDelete: () -> Unit,
                   containerSectionColor: Color, textColor: Color){
    var isExpanded by remember { mutableStateOf(false) }
    var localQuantityText by remember(quantity) {
        mutableStateOf(if (quantity == 0f) "" else quantity.toString().replace(".0", ""))
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isDeletable) {
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Icon",
                    tint = textColor.copy(alpha = 0.5f)
                )
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { onNameChange(it) },
                placeholder = { Text("Ingrediente") },
                shape = RoundedCornerShape(12.dp),
                maxLines = 1,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    focusedLabelColor = textColor,
                    unfocusedLabelColor = textColor.copy(alpha = 0.6f),
                    focusedBorderColor = textColor,
                    unfocusedBorderColor = textColor.copy(alpha = 0.4f)
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = localQuantityText,
                    onValueChange = { input ->
                        if (input.isEmpty()) {
                            localQuantityText = ""
                            onQuantityChange(0f)
                        } else if (input.all { it.isDigit() || it == '.' } && input.count { it == '.' } <= 1) {
                            localQuantityText = input
                            input.toFloatOrNull()?.let { onQuantityChange(it) }
                        }
                    },
                    modifier = Modifier.weight(0.5f),
                    placeholder = {Text("Q.tà")},
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 1,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedLabelColor = textColor,
                        unfocusedLabelColor = textColor.copy(alpha = 0.6f),
                        focusedBorderColor = textColor,
                        unfocusedBorderColor = textColor.copy(alpha = 0.4f)
                    )
                )
                ExposedDropdownMenuBox(
                    expanded = isExpanded,
                    onExpandedChange = { isExpanded = !isExpanded },
                    modifier = Modifier.weight(0.5f)
                ) {
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { },
                        readOnly = true,
                        placeholder = { Text("Unità") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedLabelColor = textColor,
                            unfocusedLabelColor = textColor.copy(alpha = 0.6f),
                            focusedBorderColor = textColor,
                            unfocusedBorderColor = textColor.copy(alpha = 0.4f)
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = isExpanded,
                        onDismissRequest = { isExpanded = false },
                        modifier = Modifier.background(color = containerSectionColor)
                    ) {
                        Units.entries.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(unit.name, color = textColor) },
                                onClick = {
                                    onUnitChange(unit.name)
                                    isExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StepItem(number: Int, description: String,
             imageUrls: List<String>,
             isDeletable: Boolean,
             ctx: Context,
             onDescriptionChange: (String) -> Unit,
             onDelete: () -> Unit,
             onAddImage: (String) -> Unit,
             onDeleteImage: (Int) -> Unit,
             containerColor: Color,
             textColor: Color,
             appBackgroundColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = containerColor, shape = RoundedCornerShape(12.dp))
            .padding(start = 8.dp)
            .height(56.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Passaggio ${number}",
            color = textColor,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Normal
        )
        if (isDeletable) {
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Icon",
                    modifier = Modifier.size(24.dp),
                    tint = textColor.copy(alpha = 0.6f)
                )
            }
        }
    }


    val pagerState = rememberPagerState(
        pageCount = { imageUrls.size + 1}
    )

    HorizontalPager(
        state = pagerState,
        contentPadding = PaddingValues(horizontal = 32.dp),
        pageSpacing = 16.dp,
        modifier = Modifier.fillMaxWidth()
    ) { page ->
        if (page == 0) {
            AddImageCard(onAddImage, 5, ctx, appBackgroundColor, textColor.copy(alpha = 0.5f))
        } else {
            Box(modifier = Modifier.fillMaxWidth()) {
                PreviewCard(imageUrls[page - 1], "Passaggio ${number} - Foto ${page}")
                IconButton(
                    onClick = { onDeleteImage(page - 1) },
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Icon",
                        tint = textColor
                    )
                }
            }
        }
    }

    OutlinedTextField(
        value = description,
        onValueChange = { onDescriptionChange(it) },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Descrizione") },
        minLines = 4,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Default,
            keyboardType = KeyboardType.Text
        ),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,
            focusedLabelColor = textColor,
            unfocusedLabelColor = textColor.copy(alpha = 0.6f),
            focusedBorderColor = textColor,
            unfocusedBorderColor = textColor.copy(alpha = 0.4f)
        )
    )
}

@Composable
fun TakePhotoButton(onNewUri: (String) -> Unit, ctx: Context, iconTint: Color) {
    var launcherUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { pictureTaken ->
        if (pictureTaken) launcherUri?.let {
            onNewUri(it.toString())
        }
    }

    IconButton(
        modifier = Modifier
            .border(1.dp, iconTint.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(4.dp),
        onClick = {
            val uri = createImageUriInGallery(ctx)
            if (uri != null) {
                launcherUri = uri
                launcher.launch(uri)
            }
        }) {
        Icon(
            imageVector = Icons.Default.Camera,
            contentDescription = "Take photo Icon",
            tint = iconTint
        )
    }
}
@Composable
fun AddImageCard(onNewUri: (String) -> Unit, maxPhotos: Int, ctx: Context, backgroundColor: Color, iconTint: Color) {

    val pickMedia = when (maxPhotos) {
        1 -> rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                onNewUri(uri.toString())
            }
        }
        else -> rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(maxPhotos)) { uris ->
            if (uris.isNotEmpty()) {
                uris.forEach { uri ->
                    onNewUri(uri.toString())
                }
            }
        }
    }

    OutlinedCard(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = backgroundColor
        ),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(iconTint.copy(alpha = 0.3f)))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                modifier = Modifier
                    .border(1.dp, iconTint.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(4.dp),
                onClick = {
                    pickMedia.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "Select image Icon",
                    tint = iconTint
                )
            }
            TakePhotoButton(onNewUri, ctx, iconTint)
        }
    }
}
