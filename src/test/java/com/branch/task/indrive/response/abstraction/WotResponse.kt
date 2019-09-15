package com.branch.task.indrive.response.abstraction

abstract class WotResponse {
    abstract val status: String
    abstract val meta: Meta
    abstract val error: Error

    data class Meta(
        val count: Byte
    )

    data class Error(
        val field: String,
        val message: String,
        val code: Int,
        val value: String
    )
}