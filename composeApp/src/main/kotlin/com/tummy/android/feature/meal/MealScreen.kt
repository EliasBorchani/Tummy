package com.tummy.android.feature.meal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tummy.domain.meal.model.MealId
import com.tummy.features.meal.MealEvent
import com.tummy.features.meal.MealIntent
import com.tummy.features.meal.MealViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MealScreen(
    onOpenDetail: (MealId) -> Unit,
    vm: MealViewModel = koinViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        vm.events.collect { event ->
            when (event) {
                is MealEvent.NavigateToDetail -> onOpenDetail(event.id)
                is MealEvent.ShowError -> { /* snackbar */ }
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        if (state.isLoading) CircularProgressIndicator()
        LazyColumn {
            items(state.meals, key = { it.id.raw }) { meal ->
                Text("${meal.name} — ${meal.calories} kcal", Modifier.padding(vertical = 8.dp))
            }
        }
        // Exemple d'intent: vm.onIntent(MealIntent.LogQuick("Apple", 80))
    }
}
