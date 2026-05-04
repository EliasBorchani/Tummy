package com.tummy.android.feature.log

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tummy.domain.symptoms.model.Symptom
import com.tummy.features.log.symptom.LogSymptomEvent
import com.tummy.features.log.symptom.LogSymptomIntent
import com.tummy.features.log.symptom.LogSymptomViewModel
import kotlinx.datetime.LocalDate
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogSymptomScreen(
    date: LocalDate,
    onClose: () -> Unit,
    vm: LogSymptomViewModel = koinViewModel { parametersOf(date) },
) {
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        vm.events.collect { event ->
            when (event) {
                LogSymptomEvent.Closed -> onClose()
                is LogSymptomEvent.ShowError -> Unit
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Symptoms") },
                navigationIcon = {
                    IconButton(onClick = onClose) { Text("<") }
                },
                actions = {
                    TextButton(onClick = { vm.onIntent(LogSymptomIntent.Done) }) {
                        Text("Done")
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(Symptom.entries, key = { it.name }) { symptom ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(text = symptom.name)
                        Switch(
                            checked = symptom in state.activeSymptoms,
                            onCheckedChange = {
                                vm.onIntent(LogSymptomIntent.Toggle(symptom))
                            },
                        )
                    }
                }
            }
        }
    }
}
