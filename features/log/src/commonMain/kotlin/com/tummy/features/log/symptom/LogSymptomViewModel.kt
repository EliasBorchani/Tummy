package com.tummy.features.log.symptom

import androidx.lifecycle.viewModelScope
import com.tummy.domain.symptoms.model.Symptom
import com.tummy.domain.symptoms.model.SymptomLogEntry
import com.tummy.domain.symptoms.repository.SymptomRepository
import com.tummy.utilities.presentation.BaseViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class LogSymptomViewModel(
    private val date: LocalDate,
    private val symptomRepository: SymptomRepository,
) : BaseViewModel<LogSymptomState, LogSymptomIntent, LogSymptomEvent>() {
    override val state: StateFlow<LogSymptomState> = symptomRepository.getAtDate(date)
        .map { entries ->
            LogSymptomState(
                date = date,
                activeSymptoms = entries.map { it.symptom }.toSet(),
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = LogSymptomState(date = date),
        )

    override fun onIntent(intent: LogSymptomIntent) {
        when (intent) {
            is LogSymptomIntent.Toggle -> handleToggle(intent.symptom)
            LogSymptomIntent.Done -> emitEvent(LogSymptomEvent.Closed)
        }
    }

    private fun handleToggle(symptom: Symptom) {
        val isCurrentlyOn = symptom in state.value.activeSymptoms
        scope.launch {
            val entry = SymptomLogEntry(date, symptom)
            runCatching {
                if (isCurrentlyOn) {
                    symptomRepository.delete(entry)
                } else {
                    symptomRepository.upsert(entry)
                }
            }.onFailure {
                emitEvent(LogSymptomEvent.ShowError(it.message ?: "Failed to update"))
            }
        }
    }
}
