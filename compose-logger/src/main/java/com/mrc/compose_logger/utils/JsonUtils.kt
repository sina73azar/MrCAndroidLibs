package com.mrc.compose_logger.utils


import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import kotlinx.serialization.json.*

object JsonUtils {

    fun isJson(input: String): Boolean {
        val trimmed = input.trim()
        return (trimmed.startsWith("{") && trimmed.endsWith("}")) ||
                (trimmed.startsWith("[") && trimmed.endsWith("]"))
    }

    fun prettify(json: String): AnnotatedString {
        if (!isJson(json)) {
            // Not a JSON → return normal text
            return AnnotatedString(json)
        }

        val parsed = try {
            Json.parseToJsonElement(json)
        } catch (e: Exception) {
            return AnnotatedString(json)
        }

        return buildAnnotatedString {
            appendStyledJson(parsed, 0)
        }
    }

    private fun AnnotatedString.Builder.appendStyledJson(element: JsonElement, indent: Int) {
        when (element) {
            is JsonObject -> {
                appendStyled("{\n", Color.Gray)
                element.entries.forEachIndexed { i, (key, value) ->
                    append("  ".repeat(indent + 1))
                    appendStyled("\"$key\"", Color(0xFF64B5F6)) // key = blue
                    appendStyled(": ", Color.Gray)
                    appendStyledJson(value, indent + 1)
                    if (i != element.size - 1) appendStyled(",\n", Color.Gray)
                    else appendStyled("\n", Color.Gray)
                }
                append("  ".repeat(indent))
                appendStyled("}", Color.Gray)
            }

            is JsonArray -> {
                appendStyled("[\n", Color.Gray)
                element.forEachIndexed { i, value ->
                    append("  ".repeat(indent + 1))
                    appendStyledJson(value, indent + 1)
                    if (i != element.size - 1) appendStyled(",\n", Color.Gray)
                    else appendStyled("\n", Color.Gray)
                }
                append("  ".repeat(indent))
                appendStyled("]", Color.Gray)
            }

            is JsonPrimitive -> {
                when {
                    element.isString -> appendStyled("\"${element.content}\"", Color(0xFF81C784)) // green
                    element.booleanOrNull != null -> appendStyled(element.content, Color(0xFFFFB74D))
                    element.content == "null" -> appendStyled("null", Color(0xFF9E9E9E))
                    else -> appendStyled(element.content, Color(0xFFBA68C8)) // number purple
                }
            }
        }
    }

    private fun AnnotatedString.Builder.appendStyled(text: String, color: Color) {
        withStyle(
            SpanStyle(
                color = color,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal
            )
        ) { append(text) }
    }
}