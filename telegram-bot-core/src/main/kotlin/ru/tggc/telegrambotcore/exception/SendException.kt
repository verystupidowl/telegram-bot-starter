package ru.tggc.telegrambotcore.exception

import com.pengrad.telegrambot.response.BaseResponse

class SendException(
    var rs: BaseResponse,
    override var message: String
) : RuntimeException(message) {
    constructor(rs: BaseResponse) : this(rs, "Send Failed " + rs.description()) {
        this.rs = rs
    }
}
