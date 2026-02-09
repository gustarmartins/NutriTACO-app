package com.mekki.taco.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.mekki.taco.R
import com.mekki.taco.data.db.entity.Food
import com.mekki.taco.presentation.ui.MainActivity

private val FOOD_ID_KEY = ActionParameters.Key<Int>("food_id")

class FoodListWidget : GlanceAppWidget() {

    companion object {
        private val COMPACT_SIZE = DpSize(110.dp, 80.dp)
        private val FULL_SIZE = DpSize(250.dp, 110.dp)
    }

    override val sizeMode = SizeMode.Responsive(
        setOf(COMPACT_SIZE, FULL_SIZE)
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val foods = WidgetDataRepository.getTopFoods(context, 10)

        provideContent {
            val size = LocalSize.current
            val showAllMacros = size.width >= 250.dp

            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(GlanceTheme.colors.surface)
                        .cornerRadius(16.dp)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            provider = ImageProvider(R.drawable.ic_search_widget),
                            contentDescription = null,
                            modifier = GlanceModifier.size(20.dp)
                        )
                        Spacer(modifier = GlanceModifier.width(8.dp))
                        Text(
                            text = "Alimentos Frequentes",
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurface,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        )
                    }

                    if (foods.isEmpty()) {
                        Text(
                            text = "Adicione alimentos no aplicativo",
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurfaceVariant,
                                fontSize = 12.sp
                            ),
                            modifier = GlanceModifier.padding(top = 16.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = GlanceModifier.fillMaxSize()
                        ) {
                            items(foods, itemId = { it.id.toLong() }) { food ->
                                FoodListItem(food, showAllMacros)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FoodListItem(food: Food, showAllMacros: Boolean) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(
                actionRunCallback<OpenFoodDetailAction>(
                    actionParametersOf(FOOD_ID_KEY to food.id)
                )
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = food.name,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal
                ),
                maxLines = 1
            )
            MacroRow(food, showAllMacros)
        }
    }
}

@Composable
private fun MacroRow(food: Food, showAllMacros: Boolean) {
    val kcal = food.energiaKcal?.toInt() ?: 0
    val protein = food.proteina?.let { "%.0fg".format(it) } ?: "-"

    Row(
        modifier = GlanceModifier.padding(top = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            provider = ImageProvider(R.drawable.ic_widget_protein),
            contentDescription = null,
            modifier = GlanceModifier.size(12.dp)
        )
        Spacer(modifier = GlanceModifier.width(2.dp))
        Text(
            text = protein,
            style = TextStyle(
                color = GlanceTheme.colors.onSurfaceVariant,
                fontSize = 11.sp
            )
        )

        if (showAllMacros) {
            val carbs = food.carboidratos?.let { "%.0fg".format(it) } ?: "-"
            val fat = food.lipidios?.total?.let { "%.0fg".format(it) } ?: "-"

            Spacer(modifier = GlanceModifier.width(6.dp))
            Image(
                provider = ImageProvider(R.drawable.ic_widget_carbs),
                contentDescription = null,
                modifier = GlanceModifier.size(12.dp)
            )
            Spacer(modifier = GlanceModifier.width(2.dp))
            Text(
                text = carbs,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 11.sp
                )
            )

            Spacer(modifier = GlanceModifier.width(6.dp))
            Image(
                provider = ImageProvider(R.drawable.ic_widget_fat),
                contentDescription = null,
                modifier = GlanceModifier.size(12.dp)
            )
            Spacer(modifier = GlanceModifier.width(2.dp))
            Text(
                text = fat,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 11.sp
                )
            )
        }

        Spacer(modifier = GlanceModifier.width(6.dp))
        Text(
            text = "$kcal kcal",
            style = TextStyle(
                color = GlanceTheme.colors.primary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

class OpenFoodDetailAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val foodId = parameters[FOOD_ID_KEY] ?: return
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(MainActivity.EXTRA_OPEN_FOOD_DETAIL, foodId)
        }
        context.startActivity(intent)
    }
}
