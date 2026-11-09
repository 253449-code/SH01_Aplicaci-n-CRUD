package com.upchiapas.kt_template.focustask.data.datasource

import com.upchiapas.kt_template.focustask.data.dto.CreateTaskDto
import com.upchiapas.kt_template.focustask.data.dto.TaskDto
import com.upchiapas.kt_template.focustask.data.dto.UpdateTaskDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface TasksApiService {

    @GET("tasks")
    suspend fun getTasks(): List<TaskDto>

    @GET("tasks/{id}")
    suspend fun getTaskById(@Path("id") id: Int): TaskDto

    @POST("tasks")
    suspend fun createTask(@Body task: CreateTaskDto): TaskDto

    @PUT("tasks/{id}")
    suspend fun updateTask(
        @Path("id") id: Int,
        @Body task: UpdateTaskDto
    ): TaskDto

    @DELETE("tasks/{id}")
    suspend fun deleteTask(@Path("id") id: Int): Response<Unit>
}
