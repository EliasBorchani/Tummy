package com.tummy.data.ingredients

import com.tummy.data.ingredients.db.CustomIngredientLogEntity
import com.tummy.data.ingredients.db.IngredientLogDao
import com.tummy.data.ingredients.db.StandardIngredientLogEntity
import com.tummy.domain.ingredients.model.Ingredient
import com.tummy.domain.ingredients.model.IngredientLogEntry
import com.tummy.domain.ingredients.model.StandardIngredient
import com.tummy.domain.ingredients.repository.IngredientRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDate

class IngredientRepositoryImpl(
    private val dao: IngredientLogDao,
) : IngredientRepository {
    override fun get(
        start: LocalDate?,
        endInclusive: LocalDate?,
    ): Flow<List<IngredientLogEntry>> {
        return combine(
            dao.getStandard(start?.toString(), endInclusive?.toString()),
            dao.getCustom(start?.toString(), endInclusive?.toString()),
        ) { standards, customs ->
            (standards.map { it.toDomain() } + customs.map { it.toDomain() })
                .sortedBy { it.date }
        }
    }

    override fun getAtDate(date: LocalDate): Flow<List<IngredientLogEntry>> = get(
        start = date,
        endInclusive = date,
    )

    override suspend fun upsert(logEntry: IngredientLogEntry) {
        when (val ingredient = logEntry.ingredient) {
            is Ingredient.Standard ->
                dao.insertStandard(
                    StandardIngredientLogEntity(
                        date = logEntry.date.toString(),
                        ingredient = ingredient.ref.name,
                    ),
                )
            is Ingredient.Custom ->
                dao.insertCustom(
                    CustomIngredientLogEntity(
                        date = logEntry.date.toString(),
                        name = ingredient.name,
                    ),
                )
        }
    }

    override suspend fun delete(logEntry: IngredientLogEntry) {
        when (val ingredient = logEntry.ingredient) {
            is Ingredient.Standard ->
                dao.deleteStandard(
                    date = logEntry.date.toString(),
                    ingredient = ingredient.ref.name,
                )
            is Ingredient.Custom ->
                dao.deleteCustom(
                    date = logEntry.date.toString(),
                    name = ingredient.name,
                )
        }
    }
}

private fun StandardIngredientLogEntity.toDomain(): IngredientLogEntry = IngredientLogEntry(
    date = LocalDate.parse(date),
    ingredient = Ingredient.Standard(StandardIngredient.valueOf(ingredient)),
)

private fun CustomIngredientLogEntity.toDomain(): IngredientLogEntry = IngredientLogEntry(
    date = LocalDate.parse(date),
    ingredient = Ingredient.Custom(name),
)
