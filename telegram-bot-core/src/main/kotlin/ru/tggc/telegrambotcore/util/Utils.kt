package ru.tggc.telegrambotcore.util

import org.apache.commons.text.StringSubstitutor
import java.time.Duration
import java.util.*
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Supplier
import kotlin.math.roundToInt

class Utils {
    companion object {
        fun checkNumber(number: String): String {
            val longNumber = number.toLong()
            return longNumber.toString()
        }

        fun <T, R> getOrNull(t: T?, function: Function<T?, R?>): R? {
            return Optional.ofNullable<T?>(t).map<R?>(function).orElse(null)
        }

        fun <T, R> getOrElse(t: T?, function: Function<T?, R?>, orElse: R?): R? {
            return Optional.ofNullable<T?>(t).map<R?>(function).orElse(orElse)
        }

        fun <T, R> getOrElseGet(t: T?, function: Function<T?, R?>, orElse: Supplier<R?>?): R? {
            return Optional.ofNullable<T?>(t).map<R?>(function).orElseGet(orElse)
        }

        fun <T> ifPresent(t: T?, consumer: Consumer<T?>) {
            Optional.ofNullable<T?>(t).ifPresent(consumer)
        }

        fun <T, R : Throwable> throwIfNull(t: T?, e: Supplier<R?>) {
            if (t == null) {
                throw e.get() as Throwable
            }
        }

        fun <R : Throwable> throwIf(condition: Boolean, e: Supplier<R?>) {
            if (condition) {
                throw e.get() as Throwable
            }
        }

        fun getText(text: String?, params: MutableMap<String?, String?>?): String? {
            val sub = StringSubstitutor(params)
            return sub.replace(text)
        }

        fun renderStaminaBar(percent: Double): String {
            val totalBlocks = 5
            val filledBlocks = (percent / (100.0 / totalBlocks)).roundToInt()

            return ("["
                    + "█".repeat(filledBlocks)
                    + "░".repeat(totalBlocks - filledBlocks)
                    + "] "
                    + percent.toInt() + "%")
        }

        fun formatDuration(duration: Duration): String {
            val hours = duration.toHours()
            val minutes = duration.toMinutesPart().toLong()
            val seconds = duration.toSecondsPart().toLong()
            if (hours > 0) return hours.toString() + "ч " + minutes + "м"
            if (minutes > 0) return minutes.toString() + "м " + seconds + "с"
            return seconds.toString() + "с"
        }
    }
}
