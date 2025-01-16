package ca.intfast.iftimer.dbspy

import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import ca.intfast.iftimer.R
import ca.intfast.iftimer.cycle.CycleController
import kotlinx.android.synthetic.main.activity_db_spy.*

class DbSpyActivity: AppCompatActivity() {
    private lateinit var cycleController: CycleController
    /***********************************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_db_spy)

        cycleController = CycleController(this)

        // Display "back" icon (left arrow) on the menu bar:
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        populateListView()
    }
    /***********************************************************************************************************************/
    private fun populateListView() {
        val cycleList = cycleController.retrieveCycleList()
        val stringList = ArrayList<String>(cycleList.size)

        for (cycle in cycleList) {
            val rowAsString = "${cycle.id}: m1='${cycle.meal1Start}', b='${cycle.betweenMealsStart}', " +
                    "m2='${cycle.meal2Start}', fs='${cycle.fastingStart}', ff='${cycle.fastingFinish}'"
            stringList.add(rowAsString)
        }

        listView.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, stringList)
    }
    /***********************************************************************************************************************/
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> finish() // user clicked "back" icon (left arrow) on the menu bar
        }
        return super.onOptionsItemSelected(item!!)
    }
    /***********************************************************************************************************************/
}
