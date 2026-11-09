package com.upchiapas.kt_template.core.network

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException

interface ErrorMapper {
    fun <T> mapResult(result: Result<T>): Result<T>
    fun mapThrowable(throwable: Throwable): Throwable
}

class NetworkErrorMapper(
    private val json: Json
) : ErrorMapper {
    override fun <T> mapResult(result: Result<T>): Result<T> {
        return result.fold(
            onSuccess = { Result.success(it) },
            onFailure = { throwable -> Result.failure(mapThrowable(throwable)) }
        )
    }

    override fun mapThrowable(throwable: Throwable): Throwable {
        return when (throwable) {
            is HttpException -> {
                val parsedMessage = parseHttpErrorMessage(throwable)
                Exception(parsedMessage ?: "Error del servidor (${throwable.code()})")
            }
            is SocketTimeoutException -> {
                Exception("Tiempo de espera agotado al conectar con el servidor")
            }
            is ConnectException -> {
                Exception("No se pudo conectar con el servidor. Verifica tu conexión a internet.")
            }
            is IOException -> {
                Exception("Error de red: comprueba tu conexión")
            }
            is SecurityException -> {
                Exception("Error de permisos: la app no tiene acceso a internet")
            }
            else -> throwable
        }
    }

    private fun parseHttpErrorMessage(exception: HttpException): String? {
        val errorBody = exception.response()?.errorBody()?.string().orEmpty()
        if (errorBody.isBlank()) return null

        return runCatching {
            val root = json.parseToJsonElement(errorBody).jsonObject
            when (val detail = root["detail"]) {
                is JsonPrimitive -> detail.content
                is JsonArray -> {
                    detail.firstOrNull()
                        ?.let { item ->
                            (item as? JsonObject)?.get("msg")?.jsonPrimitive?.content
                        }
                }
                else -> null
            }
        }.getOrNull()
    }
}
