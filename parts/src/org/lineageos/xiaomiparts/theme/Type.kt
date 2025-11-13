@file:OptIn(ExperimentalTextApi::class)

package org.lineageos.xiaomiparts.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import org.lineageos.xiaomiparts.R 
import org.lineageos.xiaomiparts.theme.AppFonts.interBody
import org.lineageos.xiaomiparts.theme.AppFonts.interLabel
import org.lineageos.xiaomiparts.theme.AppFonts.robotoFlexHeadline
import org.lineageos.xiaomiparts.theme.AppFonts.robotoFlexTitle

val TYPOGRAPHY = Typography()

// Set of Material typography styles to start with
val Typography = Typography(
    displayLarge = TYPOGRAPHY.displayLarge.copy(fontFamily = robotoFlexHeadline),
    displayMedium = TYPOGRAPHY.displayMedium.copy(fontFamily = robotoFlexHeadline),
    displaySmall = TYPOGRAPHY.displaySmall.copy(fontFamily = robotoFlexHeadline),
    headlineLarge = TYPOGRAPHY.headlineLarge.copy(fontFamily = robotoFlexHeadline),
    headlineMedium = TYPOGRAPHY.headlineMedium.copy(fontFamily = robotoFlexHeadline),
    headlineSmall = TYPOGRAPHY.headlineSmall.copy(fontFamily = robotoFlexHeadline),
    titleLarge = TYPOGRAPHY.titleLarge.copy(fontFamily = robotoFlexTitle),
    titleMedium = TYPOGRAPHY.titleMedium.copy(fontFamily = robotoFlexTitle),
    titleSmall = TYPOGRAPHY.titleSmall.copy(fontFamily = robotoFlexTitle),
    bodyLarge = TYPOGRAPHY.bodyLarge.copy(fontFamily = interBody),
    bodyMedium = TYPOGRAPHY.bodyMedium.copy(fontFamily = interBody),
    bodySmall = TYPOGRAPHY.bodySmall.copy(fontFamily = interBody),
    labelLarge = TYPOGRAPHY.labelLarge.copy(fontFamily = interLabel),
    labelMedium = TYPOGRAPHY.labelMedium.copy(fontFamily = interLabel),
    labelSmall = TYPOGRAPHY.labelSmall.copy(fontFamily = interLabel)
)

@OptIn(ExperimentalTextApi::class)
object AppFonts {
    val interClock = FontFamily(
        Font(
            R.font.inter_variable, variationSettings = FontVariation.Settings(
                FontWeight.Bold,
                FontStyle.Normal
            )
        )
    )

    val interBody = FontFamily(
        Font(
            R.font.inter_variable, variationSettings = FontVariation.Settings(
                FontWeight.Normal,
                FontStyle.Normal
            )
        )
    )

    val interLabel = FontFamily(
        Font(
            R.font.inter_variable, variationSettings = FontVariation.Settings(
                FontWeight.Medium,
                FontStyle.Normal
            )
        )
    )

    val robotoFlexTopBar = FontFamily(
        Font(
            R.font.roboto_flex_variable,
            variationSettings = FontVariation.Settings(
                FontVariation.width(125f),
                FontVariation.weight(1000),
                FontVariation.grade(0),
                FontVariation.Setting("XOPQ", 96F),
                FontVariation.Setting("XTRA", 500F),
                FontVariation.Setting("YOPQ", 79F),
                FontVariation.Setting("YTAS", 750F),
                FontVariation.Setting("YTDE", -203F),
                FontVariation.Setting("YTFI", 738F),
                FontVariation.Setting("YTLC", 514F),
                FontVariation.Setting("YTUC", 712F)
            )
        )
    )

    val robotoFlexHeadline = FontFamily(
        Font(
            R.font.roboto_flex_variable,
            variationSettings = FontVariation.Settings(
                FontVariation.width(130f),
                FontVariation.weight(600),
                FontVariation.grade(0)
            )
        )
    )

    val robotoFlexTitle = FontFamily(
        Font(
            R.font.roboto_flex_variable,
            variationSettings = FontVariation.Settings(
                FontVariation.width(130f),
                FontVariation.weight(700),
                FontVariation.grade(0)
            )
        )
    )
}