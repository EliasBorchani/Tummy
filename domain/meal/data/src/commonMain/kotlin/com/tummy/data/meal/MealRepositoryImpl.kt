package com.tummy.data.meal

import com.tummy.data.meal.db.MealDao
import com.tummy.data.meal.db.MealEntity
import com.tummy.data.meal.network.MealApi
import com.tummy.data.meal.network.MealDto
import com.tummy.domain.meal.model.Meal
import com.tummy.domain.meal.model.MealId
import com.tummy.domain.meal.repository.MealRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Instant

class MealRepositoryImpl(
    private val dao: MealDao,
    private val api: MealApi,
) : MealRepository {

    override fun observeAll(): Flow<List<Meal>> =
        dao.observeAll().map { list -> list.map(MealEntity::toDomain) }

    override suspend fun get(id: MealId): Meal? =
        dao.get(id.raw)?.toDomain()

    override suspend fun log(meal: Meal) {
        dao.upsert(meal.toEntity())
    }

    override suspend fun delete(id: MealId) {
        dao.delete(id.raw)
    }

    override suspend fun refresh() {
        val remote = api.fetchAll()
        remote.forEach { dto -> dao.upsert(dto.toEntity()) }
    }
}

private fun MealEntity.toDomain() = Meal(
    id = MealId(id),
    name = name,
    calories = calories,
    loggedAt = Instant.fromEpochMilliseconds(loggedAtEpochMillis),
)

private fun Meal.toEntity() = MealEntity(
    id = id.raw,
    name = name,
    calories = calories,
    loggedAtEpochMillis = loggedAt.toEpochMilliseconds(),
)

private fun MealDto.toEntity() = MealEntity(
    id = id,
    name = name,
    calories = calories,
    loggedAtEpochMillis = loggedAtEpochMillis,
)
