package com.branch.task.indrive.response

import com.branch.task.indrive.response.abstraction.WotResponse

data class VehiclesResponse(
    override val status: String,
    override val meta: Meta,
    val data: Map<String, List<Data>>,
    override val error: Error
) : WotResponse() {
    data class Data(
        val statistics: Statistics,
        val mark_of_mastery: Int?,
        val tank_id: Int
    ) {
        data class Statistics(
            val wins: Int,
            val battles: Int
        )
    }
}

