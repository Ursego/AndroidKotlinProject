package ca.intfast.iftimer.appwide

/****************************************************************************************************************************
Constants which describe all the states in which the app can be at any given moment.
****************************************************************************************************************************/

object AppState {
    const val MEAL_1 = "MEAL_1"
    const val BETWEEN_MEALS = "BETWEEN_MEALS"
    const val MEAL_2 = "MEAL_2"
    const val FASTING = "FASTING"

    var curr: String = FASTING // the current state of the app
        set(value) {
            if (!arrayOf(FASTING, MEAL_1, BETWEEN_MEALS, MEAL_2).contains(value))
                throw Exception("'$value' is not a valid value of AppState.")
            field = value
        }

    // ----------------------------------------------------------------------------------------------------------------------
    // Shorthand properties (instead of manipulations with curr and the constants):
    // ----------------------------------------------------------------------------------------------------------------------

    var fasting: Boolean // between MEAL_2 one day & MEAL_1 next day
        set(value) = set(value, FASTING)
        get() = (curr == FASTING)
    
    var meal1: Boolean
        set(value) = set(value, MEAL_1)
        get() = (curr == MEAL_1)
    
    var betweenMeals: Boolean // inside the Eating Window - between MEAL_1 & MEAL_2
        set(value) = set(value, BETWEEN_MEALS)
        get() = (curr == BETWEEN_MEALS)
    
    var meal2: Boolean
        set(value) = set(value, MEAL_2)
        get() = (curr == MEAL_2)
    
    private fun set(value: Boolean, newCurr: String) {
        if (!value) throw Exception("Only 'true' can be assigned to properties of AppState. " +
                "Each property becomes 'false' automatically when 'true' is assigned to another property.")
        curr = newCurr
    }
}