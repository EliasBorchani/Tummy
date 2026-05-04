package com.tummy.android.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tummy.android.resources.label
import com.tummy.domain.symptoms.model.Symptom
import com.tummy.features.home.DotColor
import com.tummy.features.home.HomeEvent
import com.tummy.features.home.HomeIntent
import com.tummy.features.home.HomeViewModel
import com.tummy.features.home.IngredientWithDot
import com.tummy.features.home.SuspectScoreComputer
import com.tummy.tokens.resources.MR
import kotlinx.datetime.LocalDate
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToLogIngredient: (LocalDate) -> Unit,
    onNavigateToLogSymptom: (LocalDate) -> Unit,
    vm: HomeViewModel = koinViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(Unit) {
        vm.events.collect { event ->
            when (event) {
                is HomeEvent.NavigateToLogIngredient -> onNavigateToLogIngredient(event.date)
                is HomeEvent.NavigateToLogSymptom -> onNavigateToLogSymptom(event.date)
                is HomeEvent.ShowError -> Unit
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { vm.onIntent(HomeIntent.PreviousDay) }) {
                            Text("<")
                        }
                        Text(state.selectedDate.toString())
                        IconButton(onClick = { vm.onIntent(HomeIntent.NextDay) }) {
                            Text(">")
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showSheet = true }) {
                Text("+")
            }
        },
    ) { padding ->
        DayContent(
            ingredients = state.ingredients,
            symptoms = state.symptoms,
            daysLoggedTotal = state.daysLoggedTotal,
            onDeleteIngredient = { ingredient ->
                vm.onIntent(HomeIntent.DeleteIngredient(ingredient.ingredient))
            },
            onDeleteSymptom = { symptom ->
                vm.onIntent(HomeIntent.DeleteSymptom(symptom))
            },
            modifier = Modifier.padding(padding),
        )
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
        ) {
            ListItem(
                headlineContent = { Text(stringResource(MR.strings.home_add_ingredient.resourceId)) },
                modifier = Modifier.clickable {
                    showSheet = false
                    vm.onIntent(HomeIntent.AddIngredient)
                },
            )
            ListItem(
                headlineContent = { Text(stringResource(MR.strings.home_add_symptom.resourceId)) },
                modifier = Modifier.clickable {
                    showSheet = false
                    vm.onIntent(HomeIntent.AddSymptom)
                },
            )
        }
    }
}

@Composable
private fun DayContent(
    ingredients: List<IngredientWithDot>,
    symptoms: Set<Symptom>,
    daysLoggedTotal: Int,
    onDeleteIngredient: (IngredientWithDot) -> Unit,
    onDeleteSymptom: (Symptom) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isEmpty = ingredients.isEmpty() && symptoms.isEmpty()
    val daysRemaining = (SuspectScoreComputer.MIN_DAYS_FOR_SCORING - daysLoggedTotal)
        .coerceAtLeast(0)

    LazyColumn(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        if (isEmpty) {
            item {
                Text(
                    text = stringResource(MR.strings.home_empty_day.resourceId),
                    modifier = Modifier.padding(vertical = 32.dp),
                )
            }
        }

        if (daysLoggedTotal in 1 until SuspectScoreComputer.MIN_DAYS_FOR_SCORING) {
            item {
                Text(
                    text = stringResource(MR.strings.home_more_days_hint.resourceId, daysRemaining),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
                HorizontalDivider()
            }
        }

        if (ingredients.isNotEmpty()) {
            item {
                SectionHeader(text = stringResource(MR.strings.home_section_ingredients.resourceId))
            }
            items(ingredients, key = { it.ingredient.toString() }) { entry ->
                IngredientRow(
                    entry = entry,
                    onDelete = { onDeleteIngredient(entry) },
                )
            }
        }

        if (symptoms.isNotEmpty()) {
            item {
                SectionHeader(text = stringResource(MR.strings.home_section_symptoms.resourceId))
            }
            items(symptoms.toList(), key = { it.name }) { symptom ->
                SymptomRow(
                    symptom = symptom,
                    onDelete = { onDeleteSymptom(symptom) },
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
    )
}

@Composable
private fun IngredientRow(
    entry: IngredientWithDot,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = entry.displayName, modifier = Modifier.weight(1f))
        DotIndicator(color = entry.dot)
        Spacer(modifier = Modifier.width(12.dp))
        IconButton(onClick = onDelete) { Text("✕") }
    }
}

@Composable
private fun SymptomRow(
    symptom: Symptom,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = stringResource(symptom.label().resourceId), modifier = Modifier.weight(1f))
        IconButton(onClick = onDelete) { Text("✕") }
    }
}

@Composable
private fun DotIndicator(color: DotColor) {
    val tint = when (color) {
        DotColor.Grey -> Color.LightGray
        DotColor.Green -> Color(0xFF4CAF50)
        DotColor.Yellow -> Color(0xFFFFC107)
        DotColor.Red -> Color(0xFFF44336)
    }
    Box(
        modifier = Modifier
            .size(12.dp)
            .background(color = tint, shape = CircleShape),
    )
}
