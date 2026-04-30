package com.tummy.android.feature.diary

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tummy.features.diary.DiaryViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DiaryScreen(
    onOpenMeals: () -> Unit,
    vm: DiaryViewModel = koinViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Consumed: ${state.caloriesConsumed} kcal")
        Text("Remaining: ${state.caloriesRemaining} kcal")
        Button(onClick = onOpenMeals) { Text("Open meals") }
    }
}
