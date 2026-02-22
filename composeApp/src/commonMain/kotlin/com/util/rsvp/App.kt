package com.util.rsvp

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.util.rsvp.theme.DarkColorScheme
import com.util.rsvp.theme.LightColorScheme
import com.util.rsvp.theme.LocalTheme
import kotlin.math.hypot

@Composable
@Preview
fun App() {
    val systemDark = isSystemInDarkTheme()
    var isDark by remember(systemDark) { mutableStateOf(value = systemDark) }

    var route: AppRoute by remember { mutableStateOf(AppRoute.Gate) }

    var rootSize by remember { mutableStateOf(IntSize.Zero) }
    var fabCenterInRootPx by remember { mutableStateOf(Offset.Unspecified) }

    var revealFromDark by remember { mutableStateOf<Boolean?>(null) }
    var revealTargetDark by remember { mutableStateOf<Boolean?>(null) }
    val revealRadiusPx = remember { Animatable(initialValue = 0f) }
    val revealActive = revealTargetDark != null

    androidx.compose.runtime.LaunchedEffect(revealTargetDark) {
        val target = revealTargetDark ?: return@LaunchedEffect

        if (rootSize == IntSize.Zero || fabCenterInRootPx == Offset.Unspecified) {
            revealFromDark = null
            revealTargetDark = null
            return@LaunchedEffect
        }

        val maxRadius = maxRevealRadiusPx(size = rootSize, center = fabCenterInRootPx)

        revealRadiusPx.snapTo(0f)
        revealRadiusPx.animateTo(
            targetValue = maxRadius,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        )

        revealFromDark = null
        revealTargetDark = null
    }

    LocalTheme(darkTheme = isDark) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { rootSize = it },
        ) {
            when (val r = route) {
                AppRoute.Gate -> GateScreen(
                    modifier = Modifier.fillMaxSize(),
                    onContinue = { text -> route = AppRoute.Home(text = text) },
                )

                is AppRoute.Home -> {
                    val homeState = rememberHomeState(lorem = r.text)
                    HomeScreen(
                        modifier = Modifier.fillMaxSize(),
                        state = homeState,
                    )
                }
            }

            if (revealActive) {
                val fromDark = revealFromDark ?: !isDark
                val fromScheme = if (fromDark) DarkColorScheme else LightColorScheme

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val r = revealRadiusPx.value.coerceAtLeast(0f)
                    val hole = Rect(
                        left = fabCenterInRootPx.x - r,
                        top = fabCenterInRootPx.y - r,
                        right = fabCenterInRootPx.x + r,
                        bottom = fabCenterInRootPx.y + r,
                    )

                    val path = Path().apply {
                        fillType = PathFillType.EvenOdd
                        addRect(Rect(0f, 0f, size.width, size.height))
                        addOval(hole)
                    }

                    drawPath(
                        path = path,
                        color = fromScheme.background,
                    )
                }
            }

            IconButton(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .offset(y = 16.dp)
                    .onGloballyPositioned { coordinates ->
                        val pos = coordinates.positionInRoot()
                        val size = coordinates.size
                        fabCenterInRootPx = pos + Offset(
                            x = size.width / 2f,
                            y = size.height / 2f,
                        )
                    },
                onClick = {
                    if (revealActive) return@IconButton
                    revealFromDark = isDark
                    val target = !isDark
                    isDark = target
                    revealTargetDark = target
                },
            ) {
                Icon(
                    imageVector = if (isDark) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.clickable{
                        if (!revealActive) {
                            revealFromDark = isDark
                            val target = !isDark
                            isDark = target
                            revealTargetDark = target
                        }
                    },
                    contentDescription = "Toggle theme",
                )
            }

            if (route is AppRoute.Home) {
                IconButton(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .offset(y = 16.dp),
                    onClick = { route = AppRoute.Gate },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        tint = MaterialTheme.colorScheme.onBackground,
                        contentDescription = "Back",
                    )
                }
            }

            if (revealActive) {
                val interactionSource = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = {},
                        )
                )
            }
        }
    }
}

private fun maxRevealRadiusPx(
    size: IntSize,
    center: Offset,
): Float {
    val dx = maxOf(center.x, size.width - center.x)
    val dy = maxOf(center.y, size.height - center.y)
    return hypot(dx, dy)
}