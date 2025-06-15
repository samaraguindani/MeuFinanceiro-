package com.example.meufinanceiro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class DetailPagerAdapter extends FragmentStateAdapter {

    private final String monthName;

    public DetailPagerAdapter(@NonNull AppCompatActivity fa, String monthName) {
        super(fa);
        this.monthName = monthName;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return OverviewFragment.newInstance(monthName);
        } else {
            return com.example.meufinanceiro.HistoryFragment.newInstance(monthName);
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
