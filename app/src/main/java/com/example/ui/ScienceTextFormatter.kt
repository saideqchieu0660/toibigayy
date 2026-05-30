package com.example.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

@Composable
fun formatScienceText(rawText: String): AnnotatedString {
    return buildAnnotatedString {
        // First pre-process mathematical representation and LaTeX symbols
        val processed = rawText
            .replace("$$", "") // strip block dollar tags
            .replace("$", "")  // strip inline dollar tags
            .replace("\\(", "")
            .replace("\\)", "")
            .replace("\\[", "")
            .replace("\\]", "")
            .replace("\\left(", "(")
            .replace("\\right)", ")")
            .replace("\\frac{", "")
            .replace("}{", "/")
            .replace("}", "")
            .replace("\\times", "×")
            .replace("\\div", "÷")
            .replace("\\pi", "π")
            .replace("\\alpha", "α")
            .replace("\\beta", "β")
            .replace("\\gamma", "γ")
            .replace("\\theta", "θ")
            .replace("\\lambda", "λ")
            .replace("\\omega", "ω")
            .replace("\\Delta", "Δ")
            .replace("\\sigma", "σ")
            .replace("\\approx", "≈")
            .replace("\\rightarrow", "→")
            .replace("\\cdot", "·")
            .replace("\\sqrt{", "√(")
            // Common subscripts
            .replace("_0", "₀")
            .replace("_1", "₁")
            .replace("_2", "₂")
            .replace("_3", "₃")
            .replace("_4", "₄")
            .replace("_5", "₅")
            .replace("_6", "₆")
            .replace("_7", "₇")
            .replace("_8", "₈")
            .replace("_9", "₉")
            .replace("_a", "ₐ")
            .replace("_e", "ₑ")
            .replace("_h", "ₕ")
            .replace("_i", "ᵢ")
            .replace("_j", "ⱼ")
            .replace("_k", "ₖ")
            .replace("_l", "ₗ")
            .replace("_m", "ₘ")
            .replace("_n", "ₙ")
            .replace("_o", "ₒ")
            .replace("_p", "ₚ")
            .replace("_r", "ᵣ")
            .replace("_s", "ₛ")
            .replace("_t", "ₜ")
            .replace("_u", "ᵤ")
            .replace("_v", "ᵥ")
            .replace("_x", "ₓ")
            // Common superscripts
            .replace("^0", "⁰")
            .replace("^1", "¹")
            .replace("^2", "²")
            .replace("^3", "³")
            .replace("^4", "⁴")
            .replace("^5", "⁵")
            .replace("^6", "⁶")
            .replace("^7", "⁷")
            .replace("^8", "⁸")
            .replace("^9", "⁹")
            .replace("^+", "⁺")
            .replace("^-", "⁻")
            .replace("^n", "ⁿ")
            .replace("^{-1}", "⁻¹")
            .replace("^{-2}", "⁻²")
            .replace("^{2}", "²")
            .replace("^{3}", "³")
            .replace("^{4}", "⁴")

        // Parse HTML tags like <b>, <i>, <ul>, <li>, <br> or markdown formatting like ** or *
        var currentIndex = 0
        val regex = Regex("(<[^>]*>|\\*\\*|\\*)")
        val matches = regex.findAll(processed).toList()
        
        var currentStyleIsBold = false
        var currentStyleIsItalic = false
        
        for (match in matches) {
            val start = match.range.first
            val end = match.range.last + 1
            val token = match.value
            
            // Append preceding raw text
            if (start > currentIndex) {
                val segment = processed.substring(currentIndex, start)
                val currentStyle = SpanStyle(
                    fontWeight = if (currentStyleIsBold) FontWeight.Bold else FontWeight.Normal,
                    fontStyle = if (currentStyleIsItalic) FontStyle.Italic else FontStyle.Normal,
                    color = Color.Unspecified
                )
                withStyle(currentStyle) {
                    append(segment)
                }
            }
            
            // Update the styles
            when {
                token == "<b>" || token == "<strong>" || token == "**" -> {
                    currentStyleIsBold = true
                }
                token == "</b>" || token == "<strong>" -> {
                    currentStyleIsBold = false
                }
                token == "<i>" || token == "<em>" || token == "*" -> {
                    currentStyleIsItalic = true
                }
                token == "</i>" || token == "</em>" -> {
                    currentStyleIsItalic = false
                }
                token == "<li>" -> {
                    append("\n  •  ")
                }
                token == "</li>" -> {
                    // Handled automatically at line boundary
                }
                token == "<br>" || token == "<br/>" -> {
                    append("\n")
                }
                token == "<ul>" || token == "</ul>" || token == "<ol>" || token == "</ol>" -> {
                    // Spacing or block wraps
                }
            }
            
            currentIndex = end
        }
        
        // Append remaining text
        if (currentIndex < processed.length) {
            val segment = processed.substring(currentIndex)
            val currentStyle = SpanStyle(
                fontWeight = if (currentStyleIsBold) FontWeight.Bold else FontWeight.Normal,
                fontStyle = if (currentStyleIsItalic) FontStyle.Italic else FontStyle.Normal,
                color = Color.Unspecified
            )
            withStyle(currentStyle) {
                append(segment)
            }
        }
    }
}
