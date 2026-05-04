package com.tummy.android.feature.log

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tummy.features.log.ingredient.LogIngredientEvent
import com.tummy.features.log.ingredient.LogIngredientIntent
import com.tummy.features.log.ingredient.LogIngredientViewModel
import com.tummy.tokens.resources.MR
import kotlinx.datetime.LocalDate
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogIngredientScreen(
    date: LocalDate,
    onClose: () -> Unit,
    vm: LogIngredientViewModel = koinViewModel { parametersOf(date) },
) {
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        vm.events.collect { event ->
            when (event) {
                LogIngredientEvent.Saved -> onClose()
                is LogIngredientEvent.ShowError -> Unit
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(MR.strings.log_ingredient_title.resourceId)) },
                navigationIcon = {
                    IconButton(onClick = onClose) { Text("<") }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = { vm.onIntent(LogIngredientIntent.QueryChanged(it)) },
                placeholder = { Text(stringResource(MR.strings.log_ingredient_search_hint.resourceId)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            LazyColumn {
                items(state.suggestions, key = { it.ref.toString() }) { suggestion ->
                    ListItem(
                        headlineContent = { Text(suggestion.displayName) },
                        modifier = Modifier.clickable {
                            vm.onIntent(LogIngredientIntent.SelectSuggestion(suggestion))
                        },
                    )
                }

                if (state.query.isNotBlank() && !state.isSearching) {
                    val trimmed = state.query.trim()
                    val exactMatch = state.suggestions.any {
                        it.displayName.equals(trimmed, ignoreCase = true)
                    }
                    if (!exactMatch) {
                        item {
                            ListItem(
                                headlineContent = {
                                    Text(stringResource(MR.strings.log_ingredient_save_as_new.resourceId, trimmed))
                                },
                                modifier = Modifier.clickable {
                                    vm.onIntent(LogIngredientIntent.SaveAsCustom)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
