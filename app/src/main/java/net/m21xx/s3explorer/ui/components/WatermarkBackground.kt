package net.m21xx.s3explorer.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import net.m21xx.s3explorer.R

@Composable
fun BoxScope.WatermarkBackground() {
    val isDark = isSystemInDarkTheme()
    
    // Grayscale matrix
    val matrix = ColorMatrix().apply {
        setToSaturation(0f)
    }

    Image(
        painter = painterResource(id = R.drawable.ic_logo_transparent),
        contentDescription = null,
        modifier = Modifier
            .fillMaxSize()
            .align(Alignment.BottomEnd)
            .offset(x = 64.dp, y = 64.dp)
            .alpha(if (isDark) 0.05f else 0.08f),
        colorFilter = ColorFilter.colorMatrix(matrix),
        // If we want BlendMode.Multiply for light mode to act as a dark shadow, we can use tinting.
        // However, colorMatrix handles grayscale. Multiply works with tinting.
        // Let's just use alpha blending with grayscale, it looks great in both modes.
    )
}
