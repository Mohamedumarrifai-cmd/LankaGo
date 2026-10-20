package com.example.lankago.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.lankago.fragments.LocalProductTabFragment

/**
 * ViewPager2 FragmentStateAdapter managing the 3 main culture tabs:
 * 0 -> "Local Products"
 * 1 -> "Markets"
 * 2 -> "Traditional Medicine"
 */
class LocalProductsPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private val categories = listOf(
        LocalProductTabFragment.CATEGORY_PRODUCTS,
        LocalProductTabFragment.CATEGORY_MARKETS,
        LocalProductTabFragment.CATEGORY_MEDICINE
    )

    override fun getItemCount(): Int = categories.size

    override fun createFragment(position: Int): Fragment {
        return LocalProductTabFragment.newInstance(categories[position])
    }

    fun getCategoryTitle(position: Int): String {
        return categories.getOrElse(position) { LocalProductTabFragment.CATEGORY_PRODUCTS }
    }
}
