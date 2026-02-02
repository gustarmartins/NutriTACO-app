# Nutritional Warnings Documentation

This document describes the implementation of front-of-package style nutritional warnings based on Brazilian government regulations.

## Regulatory Basis

The warning thresholds are based on **ANVISA Instrução Normativa IN Nº 75, de 8 de Outubro de 2020**, specifically **Anexo XV** which defines limits for front-of-package nutritional labeling (rotulagem nutricional frontal).

**Source:** [IN Nº 75/2020 - ANVISA](https://www.in.gov.br/en/web/dou/-/instrucao-normativa-in-n-75-de-8-de-outubro-de-2020-282071143)

## Thresholds (per 100g/100ml)

| Nutrient | Solid Foods (per 100g) | Liquid Foods (per 100ml) |
|----------|------------------------|--------------------------|
| **Added Sugar** | ≥ 15g | ≥ 7.5g |
| **Saturated Fat** | ≥ 6g | ≥ 3g |
| **Sodium** | ≥ 600mg | ≥ 300mg |

## Implementation Notes

### Currently Implemented
- **Sodium** (sodio): Using the `Food.sodio` field (mg per 100g)
- **Saturated Fat** (gorduras saturadas): Using `Food.lipidios.saturados` (g per 100g)

### Not Implemented
- **Added Sugar**: The TACO database only provides total carbohydrates, not "added sugars" which is a distinct regulatory concept. This warning could be added for custom foods where the user specifies added sugar content.

## Files

- `utils/NutrientWarnings.kt`: Core logic for threshold calculations
- `presentation/ui/components/NutrientWarningBadges.kt`: UI component
- `presentation/ui/fooddetail/FoodDetailScreen.kt`: Integration point
- `presentation/ui/settings/SettingsScreen.kt`: User-facing attribution

## Tests

Unit tests are located in `FoodFilterStateTest.kt` and cover threshold logic for both nutrients.
