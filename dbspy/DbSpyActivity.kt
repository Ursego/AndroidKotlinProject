package ca.intfast.iftimer.dbspy

import android.os.Bundle
import android.widget.ArrayAdapter
import ca.intfast.iftimer.cycle.CycleController
import ca.intfast.iftimer.databinding.ActivityDbSpyBinding
import ca.intfast.iftimer.util.AppActivity

class DbSpyActivity: AppActivity() {
    private lateinit var binding: ActivityDbSpyBinding
    private lateinit var cyc: CycleController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDbSpyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cyc = CycleController(this)

        this.populateCycleListView()
    }

    private fun populateCycleListView() {
        val cycleList = cyc.retrieveCycleList()
        val stringList = cycleList.map { it.toString() }
        binding.cycleListView.adapter =
            ArrayAdapter(this@DbSpyActivity, android.R.layout.simple_list_item_1, stringList)
    }

}
