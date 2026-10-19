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

        // Setup Charts for Presentation
        val lineChart = view.findViewById<LineChart>(R.id.lineChartRevenue)
        val pieChart = view.findViewById<PieChart>(R.id.pieChartCategories)

        if (lineChart != null) {
            setupRevenueLineChart(lineChart)
        }
        if (pieChart != null) {
            setupCategoriesPieChart(pieChart)
        }

        // Profile / Logout Header Button
        view.findViewById<View>(R.id.btn_admin_profile)?.setOnClickListener {
            Toast.makeText(requireContext(), "Admin Profile & Settings", Toast.LENGTH_SHORT).show()
        }

        // Stat Card Click Listeners (Navigate to respective management screens)
        view.findViewById<View>(R.id.card_stat_users)?.setOnClickListener {
            findNavController().navigate(R.id.adminManageUsersFragment)
        }

        view.findViewById<View>(R.id.card_stat_vendors)?.setOnClickListener {
            findNavController().navigate(R.id.adminManageVendorsFragment)
        }

        view.findViewById<View>(R.id.card_stat_revenue)?.setOnClickListener {
            Toast.makeText(requireContext(), "Revenue Ledger: LKR 4,280,000 this month", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<View>(R.id.card_stat_trips)?.setOnClickListener {
            findNavController().navigate(R.id.adminAddDestinationFragment)
        }
    }

    private fun setupRevenueLineChart(chart: LineChart) {
        val days = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val revenueValues = listOf(380f, 510f, 490f, 620f, 780f, 950f, 890f) // in thousands LKR

        val entries = ArrayList<Entry>()
        for (i in revenueValues.indices) {
            entries.add(Entry(i.toFloat(), revenueValues[i]))
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
            fillAlpha = 140
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
            animateY(1000, Easing.EaseInOutCubic)
            invalidate()
        }
    }

    private fun setupCategoriesPieChart(chart: PieChart) {
        val entries = ArrayList<PieEntry>().apply {
            add(PieEntry(38f, "Heritage"))
            add(PieEntry(26f, "Beach & Surf"))
            add(PieEntry(20f, "Wildlife Safari"))
            add(PieEntry(16f, "Cultural Craft"))
        }

        val colors = listOf(
            Color.parseColor("#1A2B4C"), // Dark Blue
            Color.parseColor("#4A72B2"), // Medium Blue
            Color.parseColor("#D3E3F3"), // Light Blue
            Color.parseColor("#10B981")  // Accent Emerald
        )

        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors
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
            centerText = "LankaGo\nBookings"
            setCenterTextSize(13f)
            setCenterTextColor(Color.parseColor("#1A2B4C"))
            legend.textColor = Color.parseColor("#1A2B4C")
            legend.textSize = 12f
            animateY(1000, Easing.EaseInOutQuad)
            invalidate()
        }
    }
}
