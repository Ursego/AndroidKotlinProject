//package ca.intfast.iftimer.util
//
//import android.content.Context
//
///****************************************************************************************************************************
//To be implemented by classes whose constants are used to populate multi-values Preferences
//(ListPreference, MultiSelectListPreference, DropDownPreference).
//Example of use: https://tinyurl.com/PrefsFromConstants
//****************************************************************************************************************************/
//
//interface ConstantsSet<T> {
//    // Example of constants, declared in the implementing class:
//    // const val FIRST = 1
//    // const val SECOND = 2
//    // const val THIRD = 3
//
//    /***********************************************************************************************************************/
//    fun toArray(): Array<T>
//    // Returns the values of all the constants as an array of the constants type, like {1, 2, 3}
//    // Call it to populate Preference.entryValues in your PreferenceFragmentCompat when the constants type is String.
//
//    // Sample implementation:
//    // override fun toArray(): Array<String> = arrayOf(FIRST, SECOND, THIRD)
//
//    /***********************************************************************************************************************/
//    fun toStringArray(): Array<String?> {
//        // Returns the values of all the constants as an array of String, like {"1", "2", "3"}
//        // Call it to populate Preference.entryValues in your PreferenceFragmentCompat when the constants type is NOT String.
//        val origTypeArray = toArray()
//        val stringArray: Array<String?> = arrayOfNulls<String?>(size = origTypeArray.size)
//        for ((i, constantValue) in (origTypeArray.withIndex())) {
//            stringArray[i] = constantValue.toString()
//        }
//        return stringArray
//    }
//
//    /***********************************************************************************************************************/
//    fun toDisplayedValuesArray(context: Context): Array<String?> {
//        // Returns array of human-readable texts for each constant, like {"First", "Second", "Third"}.
//        // Call it to populate Preference.entries in your PreferenceFragmentCompat.
//        val constantValuesArray = toArray()
//        val displayedValuesArray: Array<String?> = arrayOfNulls<String?>(size = constantValuesArray.size)
//        for ((i, constantValue) in (constantValuesArray.withIndex())) {
//            displayedValuesArray[i] = getDisplayedValue(constantValue, context)
//        }
//        return displayedValuesArray
//    }
//
//    /***********************************************************************************************************************/
//    fun getDisplayedValue(constantValue: T, context: Context): String {
//        // Returns the human-readable text for this constant (like "First", "Second" or "Third").
//        val resourceId = getResourceId(constantValue)
//        return context.resources.getString(resourceId)
//    }
//
//    /***********************************************************************************************************************/
//    fun getResourceId(constantValue: T): Int
//    // Returns R.string.XXX to obtain the human-readable text for this constant.
//
//    /* Sample implementation:
//    override fun getResourceId(constantValue: String): Int {
//        return when (constantValue) {
//            FIRST -> R.string.whatever__first
//            SECOND -> R.string.whatever__second
//            THIRD -> R.string.whatever__third
//            else -> throw Exception("$constantValue is not a valid value of ${this.javaClass.name}.")
//        }
//    }
//
//    >>>>>>> If R.string is irrelevant (for example, the human-readable texts are hardcoded or retrieved from DB):
//
//    STEP 1: implement getResourceId() this way
//        (constantValue must be of the type, actually passed as T when ConstantsSet<T> was created):
//
//    override fun getResourceId(constantValue: Int): Int =
//        throw Exception("Fun ${this.javaClass.name}.getResourceId() should never be called since " +
//                "the human readable texts are not stored in R.string.")
//
//    STEP 2 (only if toDisplayedValuesArray() will be called): override getDisplayedValue() which is called from
//    toDisplayedValuesArray() per each constant:
//
//    override fun getDisplayedValue(constantValue: Int, context: Context): String {
//        return <the text which must be displayed for this constant>
//    }
//
//    Or, alternatively, you can override toDisplayedValuesArray() and return the whole array at one stroke:
//
//    override fun toDisplayedValuesArray(context: Context): Array<String?> = arrayOf("First", "Second", "Third")
//    */
//
//    /***********************************************************************************************************************/
//    fun validate(constantValue : T?) {
//        // Call it in setters when the set value is represented by a constant, contained in the implementing object.
//        if (constantValue == null) return
//        if (!toArray().contains(constantValue))
//            throw Exception("'$constantValue' is not a valid value of ${this.javaClass.name}.")
//    }
//}