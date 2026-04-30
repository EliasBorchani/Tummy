package com.tummy.data.meal.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class MealApi(private val client: HttpClient) {
    suspend fun fetchAll(): List<MealDto> = client.get("/meals").body()
}
