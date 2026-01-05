package ca.intfast.iftimer.db

object DbColumn {
    const val ID = "_id" // "_id" is the default ID col name; it's used automatically by some functions of CrudHelper if you don't pass another col name
    const val MEAL_1_START = "meal1Start"
    const val BETWEEN_MEALS_START = "betweenMealsStart"
    const val MEAL_2_START = "meal2Start"
    const val FASTING_START = "fastingStart"
    const val FASTING_FINISH = "fastingFinish" // it's also a flag: null - active cycle, not null - archived cycle

    // For Stats class:
    const val AVG_MEAL_1 = "avgMeal1"
    const val AVG_BETWEEN_MEALS = "avgBetweenMeals"
    const val AVG_MEAL_2 = "avgMeal2"
    const val AVG_EW = "avgEw"
    const val MEAL_1_COUNT = "meal1Count"
    const val MEAL_2_COUNT = "meal2Count"
}