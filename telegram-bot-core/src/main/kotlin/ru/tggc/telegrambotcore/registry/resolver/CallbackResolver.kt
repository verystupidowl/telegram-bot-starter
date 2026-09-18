package ru.tggc.telegrambotcore.registry.resolver

import com.pengrad.telegrambot.model.CallbackQuery
import org.springframework.stereotype.Component
import ru.tggc.telegrambotcore.annotation.params.CallbackParam
import java.lang.reflect.Parameter

@Component
class CallbackResolver : ParameterResolver<CallbackQuery?> {
    override fun supports(p: Parameter?): Boolean =
        p?.getType() == CallbackQuery::class.java && p.isAnnotationPresent(CallbackParam::class.java)

    override fun resolve(parameter: Parameter?, ctx: HandlerCtx): CallbackQuery? = ctx.update?.callbackQuery()

}
