package org.lineageos.xiaomiparts.theme

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object GroupedListDefaults {

    val VerticalSpacing: Dp = 3.dp

    @Composable
    fun listColors(): ListItemColors =
        ListItemDefaults.colors(
            containerColor = CustomColors.listItemColors.containerColor
        )

    @Composable
    fun getShape(index: Int, totalItems: Int): CornerBasedShape {
        return when {
            totalItems == 1 -> XiaomiPartsShapeDefaults.cardShape
            index == 0 -> XiaomiPartsShapeDefaults.topListItemShape
            index == totalItems - 1 -> XiaomiPartsShapeDefaults.bottomListItemShape
            else -> XiaomiPartsShapeDefaults.middleListItemShape
        }
    }
}