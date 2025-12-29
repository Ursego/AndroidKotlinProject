package ca.intfast.iftimer.dbspy

import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import ca.intfast.iftimer.cycle.CycleController
import ca.intfast.iftimer.databinding.ActivityDbSpyBinding

class DbSpyActivity: AppCompatActivity() {
    private lateinit var b: ActivityDbSpyBinding // "b"inding
    private lateinit var cyc: CycleController
    /***********************************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityDbSpyBinding.inflate(layoutInflater)
        setContentView(b.root)

        cyc = CycleController(this)

        // Display "back" icon (left arrow) on the menu bar:
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        populateListView()
    }
    /***********************************************************************************************************************/
    private fun populateListView() {
        val cycleList = cyc.retrieveCycleList()
        val stringList = ArrayList<String>(cycleList.size)

        for (cycle in cycleList) {
            val rowAsString = "${cycle.id}: m1='${cycle.meal1Start}', b='${cycle.betweenMealsStart}', " +
                    "m2='${cycle.meal2Start}', fs='${cycle.fastingStart}', ff='${cycle.fastingFinish}'"
            stringList.add(rowAsString)
        }

        b.listView.adapter =
            ArrayAdapter(this@DbSpyActivity, android.R.layout.simple_list_item_1, stringList)
    }
    /***********************************************************************************************************************/
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish() // user clicked "back" icon (left arrow) on the menu bar
        }
        return super.onOptionsItemSelected(item)
    }
    /***********************************************************************************************************************/
}
