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

        // Initialize Charts with presentation data
        val lineChart = view.findViewById<LineChart>(R.id.lineChartRevenue)
        val pieChart = view.findViewById<PieChart>(R.id.pieChartCategories)

        if (lineChart != null) {
            setupRevenueLineChart(lineChart)
        }
        if (pieChart != null) {
            setupCategoriesPieChart(pieChart)
        }

        // Logout Action
        view.findViewById<View>(R.id.btn_admin_logout)?.setOnClickListener {
            Toast.makeText(requireContext(), "Logged out from Admin Portal", Toast.LENGTH_SHORT).show()
            val popped = findNavController().popBackStack(R.id.adminLoginFragment, false)
            if (!popped) {
                findNavController().navigate(R.id.adminLoginFragment)
            }
        }

        // Navigate to Add Destination
        view.findViewById<View>(R.id.card_action_add_destination)?.setOnClickListener {
            findNavController().navigate(R.id.action_adminDashboardFragment_to_adminAddDestinationFragment)
        }

        // Manage Vendors action
        view.findViewById<View>(R.id.card_action_manage_vendors)?.setOnClickListener {
            Toast.makeText(requireContext(), "Opening Vendor Management queue...", Toast.LENGTH_SHORT).show()
        }

        // Quick Stat Card Clicks
        view.findViewById<View>(R.id.card_stat_users)?.setOnClickListener {
            Toast.makeText(requireContext(), "Total Users: 3,420 registered accounts", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<View>(R.id.card_stat_vendors)?.setOnClickListener {
            Toast.makeText(requireContext(), "Total Vendors: 148 verified partners", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<View>(R.id.card_stat_revenue)?.setOnClickListener {
            Toast.makeText(requireContext(), "Monthly Revenue: LKR 5.4 Million", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<View>(R.id.card_stat_trips)?.setOnClickListener {
            findNavController().navigate(R.id.action_adminDashboardFragment_to_adminAddDestinationFragment)
        }
    }

    /**
     * Populates LineChart with Revenue data for Viva presentation
     */
    private fun setupRevenueLineChart(chart: LineChart) {
        val days = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val revenueInThousands = listOf(420f, 580f, 510f, 690f, 820f, 980f, 920f)

        val entries = ArrayList<Entry>()
        for (i in revenueInThousands.indices) {
            entries.add(Entry(i.toFloat(), revenueInThousands[i]))
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
            fillAlpha = 150
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
            animateY(900, Easing.EaseInOutCubic)
            invalidate()
        }
    }

    /**
     * Populates PieChart with Top Destination Categories
     */
    private fun setupCategoriesPieChart(chart: PieChart) {
        val entries = ArrayList<PieEntry>().apply {
            add(PieEntry(40f, "Heritage"))
            add(PieEntry(25f, "Coastal"))
            add(PieEntry(20f, "Wildlife"))
            add(PieEntry(15f, "Culture"))
        }

        val categoryColors = listOf(
            Color.parseColor("#1A2B4C"),
            Color.parseColor("#4A72B2"),
            Color.parseColor("#D3E3F3"),
            Color.parseColor("#10B981")
        )

        val dataSet = PieDataSet(entries, "").apply {
            colors = categoryColors
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
            centerText = "LankaGo\nPlaces"
            setCenterTextSize(13f)
            setCenterTextColor(Color.parseColor("#1A2B4C"))
            legend.textColor = Color.parseColor("#1A2B4C")
            legend.textSize = 12f
            animateY(900, Easing.EaseInOutQuad)
            invalidate()
        }
    }
}
