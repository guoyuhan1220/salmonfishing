package com.example.salmontrollingassistant.domain.service

import java.io.IOException

sealed class TideException(message: String? = null, cause: Throwable? = null) : Exception(message, cause) {
    class NetworkError(cause: IOException) : TideException("Network error", cause)
    class ServerError(val code: Int) : TideException("Server error with code: $code")
    class InvalidLocation : TideException("Invalid location provided")
    class NoDataAvailable : TideException("No tide data available for this location")
    class Unknown : TideException("Unknown error occurred")
}