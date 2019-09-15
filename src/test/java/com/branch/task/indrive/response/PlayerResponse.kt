package com.branch.task.indrive.response

import com.branch.task.indrive.response.abstraction.WotResponse

data class PlayerResponse(
    override val status: String,
    override val meta: Meta,
    val data: ArrayList<Data>,
    override val error: Error
) : WotResponse() {
    data class Data(
        val nickname: String,
        val account_id: Int
    )
}

