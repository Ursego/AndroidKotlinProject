package ca.intfast.iftimer.appwide
 
object CurrAppState {
    private var _curr: AppState = AppState.FASTING // the current state of the app

    var fasting: Boolean // between MEAL_2 one day & MEAL_1 next day
        set(value) = setCurr(value, AppState.FASTING)
        get() = (_curr == AppState.FASTING)

    var meal1: Boolean
        set(value) = setCurr(value, AppState.MEAL_1)
        get() = (_curr == AppState.MEAL_1)

    var betweenMeals: Boolean // between MEAL_1 & MEAL_2 inside an Eating Window
        set(value) = setCurr(value, AppState.BETWEEN_MEALS)
        get() = (_curr == AppState.BETWEEN_MEALS)

    var meal2: Boolean
        set(value) = setCurr(value, AppState.MEAL_2)
        get() = (_curr == AppState.MEAL_2)

    private fun setCurr(value: Boolean, newCurr: AppState) {
        if (!value) throw Exception("Only 'true' can be assigned to properties of AppState. " +
                "Each property becomes 'false' automatically when 'true' is assigned to another property.")
        _curr = newCurr
    }
}