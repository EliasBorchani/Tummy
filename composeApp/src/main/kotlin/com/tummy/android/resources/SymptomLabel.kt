package com.tummy.android.resources

import com.tummy.domain.symptoms.model.Symptom
import com.tummy.tokens.resources.MR
import dev.icerock.moko.resources.StringResource

fun Symptom.label(): StringResource = when (this) {
    Symptom.Bloating -> MR.strings.symptom_bloating
    Symptom.AbdominalPain -> MR.strings.symptom_abdominal_pain
    Symptom.Gas -> MR.strings.symptom_gas
    Symptom.Diarrhea -> MR.strings.symptom_diarrhea
    Symptom.Constipation -> MR.strings.symptom_constipation
    Symptom.Nausea -> MR.strings.symptom_nausea
}
