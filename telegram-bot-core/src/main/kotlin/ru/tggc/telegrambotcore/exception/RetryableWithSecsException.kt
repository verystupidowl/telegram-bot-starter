package ru.tggc.telegrambotcore.exception

class RetryableWithSecsException(message: String?) : RetryableException(message, 5)
