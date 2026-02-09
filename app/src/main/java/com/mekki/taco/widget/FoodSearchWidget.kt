package com.mekki.taco.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.mekki.taco.R
import com.mekki.taco.presentation.ui.MainActivity

class FoodSearchWidget : GlanceAppWidget() {

    companion object {
        private val SMALL_SIZE = DpSize(60.dp, 60.dp)
        private val MEDIUM_SIZE = DpSize(180.dp, 56.dp)
    }

    override val sizeMode = SizeMode.Responsive(
        setOf(SMALL_SIZE, MEDIUM_SIZE)
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val size = LocalSize.current
            val isCompact = size.width < 100.dp

            GlanceTheme {
                if (isCompact) {
                    CompactLayout()
                } else {
                    ExpandedLayout()
                }
            }
        }
    }
}

@Composable
private fun CompactLayout() {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(actionRunCallback<OpenSearchAction>()),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = GlanceModifier
                .size(48.dp)
                .background(ImageProvider(R.drawable.widget_compact_background))
                .cornerRadius(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_search_widget_white),
                contentDescription = "Buscar alimentos",
                modifier = GlanceModifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun ExpandedLayout() {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(actionRunCallback<OpenSearchAction>()),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(48.dp)
                .background(GlanceTheme.colors.surfaceVariant)
                .cornerRadius(24.dp)
                .padding(start = 16.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_search_widget),
                contentDescription = null,
                modifier = GlanceModifier.size(20.dp)
            )

            Spacer(modifier = GlanceModifier.width(12.dp))

            Text(
                text = "Buscar alimentos...",
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant
                ),
                modifier = GlanceModifier.defaultWeight()
            )

            Spacer(modifier = GlanceModifier.width(8.dp))

            Box(
                modifier = GlanceModifier
                    .size(36.dp)
                    .background(ImageProvider(R.drawable.widget_app_icon_background))
                    .cornerRadius(18.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(R.mipmap.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = GlanceModifier.size(36.dp)
                )
            }
        }
    }
}

class OpenSearchAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(MainActivity.EXTRA_OPEN_SEARCH, true)
        }
        context.startActivity(intent)
    }
}
