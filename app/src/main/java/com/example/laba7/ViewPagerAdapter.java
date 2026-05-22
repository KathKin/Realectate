package com.example.laba7;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapter extends FragmentStateAdapter {

    private final String[] tabTypes = {"ALL", "SALE", "RENT"}; // Можно добавить "DAILY"

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return PropertyListFragment.newInstance(tabTypes[position]);
    }

    @Override
    public int getItemCount() {
        return tabTypes.length;
    }

    public String getType(int position) {
        return tabTypes[position];
    }
}
