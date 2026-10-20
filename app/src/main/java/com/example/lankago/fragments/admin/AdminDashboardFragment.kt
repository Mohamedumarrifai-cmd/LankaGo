package com.example.lankago.fragments.admin

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.lankago.R
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter

class AdminDashboardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.admin_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val lineChart = view.findViewById<LineChart>(R.id.lineChartRevenue)
        val pieChart = view.findViewById<PieChart>(R.id.pieChartCategories)

        if (lineChart != null) {
            setupRevenueLineChart(lineChart)
        }
        if (pieChart != null) {
            setupCategoriesPieChart(pieChart)
        }

        }

        view.findViewById<View>(R.id.card_stat_users)?.setOnClickListener {
        }

        view.findViewById<View>(R.id.card_stat_vendors)?.setOnClickListener {
        }

        view.findViewById<View>(R.id.card_stat_revenue)?.setOnClickListener {
        }

        view.findViewById<View>(R.id.card_stat_trips)?.setOnClickListener {
        }
    }

    private fun setupRevenueLineChart(chart: LineChart) {
        val days = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

        val entries = ArrayList<Entry>()
        }

        val dataSet = LineDataSet(entries, "Revenue (K LKR)").apply {
            color = Color.parseColor("#1A2B4C")
            lineWidth = 3f
            circleRadius = 5f
            setCircleColor(Color.parseColor("#1A2B4C"))
            circleHoleColor = Color.WHITE
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = Color.parseColor("#D3E3F3")
            valueTextSize = 10f
            valueTextColor = Color.parseColor("#1A2B4C")
        }

        chart.apply {
            data = LineData(dataSet)
            description.isEnabled = false
            legend.isEnabled = false
            setTouchEnabled(true)
            setPinchZoom(false)
            setDrawGridBackground(false)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(days)
                granularity = 1f
                setDrawGridLines(false)
                textColor = Color.parseColor("#8A8F98")
            }

            axisLeft.apply {
                textColor = Color.parseColor("#8A8F98")
                setDrawGridLines(true)
                gridColor = Color.parseColor("#F0F2F5")
            }

            axisRight.isEnabled = false
            invalidate()
        }
    }

    private fun setupCategoriesPieChart(chart: PieChart) {
        val entries = ArrayList<PieEntry>().apply {
        }

        )

        val dataSet = PieDataSet(entries, "").apply {
            sliceSpace = 3f
            selectionShift = 5f
            valueTextColor = Color.WHITE
            valueTextSize = 12f
            valueFormatter = PercentFormatter(chart)
        }

        chart.apply {
            data = PieData(dataSet)
            setUsePercentValues(true)
            description.isEnabled = false
            isDrawHoleEnabled = true
            holeRadius = 52f
            transparentCircleRadius = 57f
            setHoleColor(Color.WHITE)
            setCenterTextSize(13f)
            setCenterTextColor(Color.parseColor("#1A2B4C"))
            legend.textColor = Color.parseColor("#1A2B4C")
            legend.textSize = 12f
            invalidate()
        }
    }
}
