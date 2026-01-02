package ca.intfast.iftimer.dbspy

import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import ca.intfast.iftimer.cycle.CycleController
import ca.intfast.iftimer.databinding.ActivityDbSpyBinding

class DbSpyActivity: AppCompatActivity() {
    private lateinit var binding: ActivityDbSpyBinding
    private lateinit var cyc: CycleController
    /***********************************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDbSpyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cyc = CycleController(this)

        // Display "back" icon (left arrow) on the menu bar:
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        populateListView()
    }
    /***********************************************************************************************************************/
    private fun populateListView() {
        val cycleList = cyc.retrieveCycleList()
        val stringList = cycleList.map { it.toString() }
        binding.listView.adapter =
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
