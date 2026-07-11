package ru.tggc.telegrambotcore.util

import org.apache.commons.text.StringSubstitutor
import java.time.Duration
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Supplier
import kotlin.math.roundToInt

class Utils {
    companion object {
        @JvmStatic
        fun checkNumber(number: String): String {
            val longNumber = number.toLong()
            return longNumber.toString()
        }

        @JvmStatic
        fun <T, R> getOrNull(t: T?, function: Function<T?, R?>): R? = t?.let { function.apply(it) }

        @JvmStatic
        fun <T, R> getOrElse(t: T?, function: Function<T?, R?>, orElse: R?): R? =
            t?.let { function.apply(it) } ?: orElse

        @JvmStatic
        fun <T, R> getOrElseGet(t: T?, function: Function<T?, R?>, orElse: Supplier<R?>): R? =
            t?.let { function.apply(it) } ?: orElse.get()

        @JvmStatic
        fun <T> ifPresent(t: T?, consumer: Consumer<T?>) = t?.let { consumer.accept(it) }

        @JvmStatic
        fun <T, R : Throwable> throwIfNull(t: T?, e: Supplier<R?>) = t ?: throw e.get() as Throwable

        @JvmStatic
        fun <R : Throwable> throwIf(condition: Boolean, e: Supplier<R?>) {
            if (condition) {
                throw e.get() as Throwable
            }
        }

        @JvmStatic
        fun getText(text: String?, params: MutableMap<String?, String?>?): String? {
            val sub = StringSubstitutor(params)
            return sub.replace(text)
        }

        @JvmStatic
        fun renderStaminaBar(percent: Double): String {
            val totalBlocks = 5
            val filledBlocks = (percent / (100.0 / totalBlocks)).roundToInt()

            return ("["
                    + "█".repeat(filledBlocks)
                    + "░".repeat(totalBlocks - filledBlocks)
                    + "] "
                    + percent.toInt() + "%")
        }

        @JvmStatic
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
