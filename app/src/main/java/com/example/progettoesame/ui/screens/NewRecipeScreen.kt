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
import androidx.compose.ui.text.style.TextAlign
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
                TimeType.WAITING -> recipeState.waiting?: 0
                TimeType.COOKING ->recipeState.cooking
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
        containerColor = Color.White,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (recipeId == null) "Nuova ricetta" else "Modifica Ricetta", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Back icon"
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
                                tint = Color.Black
                            )
                        }
                    }

                } else {
                    AddImageCard({ newUri ->
                        newRecipeViewModel.recipeActions.onPreviewImageChange(
                            newUri
                        )
                    }, 1, ctx)
                }

                OutlinedTextField(
                    value = recipeState.title,
                    onValueChange = { newRecipeViewModel.recipeActions.onTitleChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Titolo") },
                    shape = RoundedCornerShape(12.dp)
                )

                CategoryItem(
                    recipeState.category?.name ?: "",
                    categories.categories.sortedBy { it.order }.map { it.name },
                    { newRecipeName ->
                        newRecipeViewModel.recipeActions.onCategoryChange(
                            newRecipeName
                        )
                    })

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Tempi:", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Normal)
                    TimeInputCard({ activeTimeDialog = TimeType.PREPARATION }, "Preparazione: ${formatTime(recipeState.preparation)}")
                    TimeInputCard({ activeTimeDialog = TimeType.WAITING }, "Riposo: ${formatTime(recipeState.waiting ?: 0)}")
                    TimeInputCard({ activeTimeDialog = TimeType.COOKING }, "Cottura: ${formatTime(recipeState.cooking)}")
                }

                Text("Ingredienti", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
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
                                { newRecipeViewModel.ingredientActions.onDeleteIngredient(index) })
                        }
                    }
                    TextButton(
                        onClick = {
                            newRecipeViewModel.ingredientActions.onAddIngredient()
                        },
                        modifier = Modifier.align(alignment = Alignment.CenterHorizontally),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Aggiungi ")
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Text("Procedimento", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
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
                                { imageIndex -> newRecipeViewModel.stepActions.onDeleteImage(step.number, imageIndex) })
                        }
                    }
                    TextButton(
                        onClick = { newRecipeViewModel.stepActions.onAddStep(recipeState.steps.size + 1) },
                        modifier = Modifier.align(alignment = Alignment.CenterHorizontally),
                        shape = RoundedCornerShape(12.dp)
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text("Salva", color = Color.White)
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
                        .background(Color.Black.copy(alpha = 0.3f))
                        .pointerInput(Unit) {},
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.Gray)
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
fun TimeInputCard(onClick: () -> Unit, text: String) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = Color(0xfff7ead0)
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
            Text(text, style = MaterialTheme.typography.bodyMedium)
            Icon(Icons.Default.Timer, contentDescription = "Clock icon")
        }
    }
}

@Composable
fun CategoryItem(name: String, categories: List<String>, onCategoryChange: (String) -> Unit) {
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
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = isCategoryMenuExpanded,
            onDismissRequest = { isCategoryMenuExpanded = false },
            modifier = Modifier.background(color = Color(0xfff7ead0))
        ) {
            categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category, color = Color.Black) },
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
                   onDelete: () -> Unit){
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
                    tint = Color.Gray
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
                    maxLines = 1
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
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )

                    ExposedDropdownMenu(
                        expanded = isExpanded,
                        onDismissRequest = { isExpanded = false },
                        modifier = Modifier.background(color = Color(0xfff7ead0))
                    ) {
                        Units.entries.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(unit.name, color = Color.Black) },
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
             onDeleteImage: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xfff7ead0),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(start = 8.dp)
            .height(56.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Passaggio ${number}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Normal
        )
        if (isDeletable) {
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Icon",
                    modifier = Modifier.size(24.dp),
                    tint = Color.Gray
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
            AddImageCard(onAddImage, 5, ctx)
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
                        tint = Color.Black
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
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun TakePhotoButton(onNewUri: (String) -> Unit, ctx: Context) {
    var launcherUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { pictureTaken ->
        if (pictureTaken) launcherUri?.let {
            onNewUri(it.toString())
        }
    }

    IconButton(
        modifier = Modifier
            .border(1.dp, Color.Gray, RoundedCornerShape(16.dp))
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
            tint = Color.Gray
        )
    }
}
@Composable
fun AddImageCard(onNewUri: (String) -> Unit, maxPhotos: Int, ctx: Context) {

    val pickMedia = when(maxPhotos) {
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
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                modifier = Modifier
                    .border(1.dp, Color.Gray, RoundedCornerShape(16.dp))
                    .padding(4.dp),
                onClick = { pickMedia.launch(PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly))
            }) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "Select image Icon",
                    tint = Color.Gray
                )
            }
            TakePhotoButton(onNewUri, ctx)
        }
    }
}
