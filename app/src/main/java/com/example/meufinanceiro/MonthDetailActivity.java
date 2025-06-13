package com.example.meufinanceiro;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MonthDetailActivity extends AppCompatActivity {

    public static final String EXTRA_MONTH_NAME = "EXTRA_MONTH_NAME";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_month_detail);

        // 1) Toolbar com back arrow
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // 2) Título vindo do Intent
        String monthName = getIntent().getStringExtra(EXTRA_MONTH_NAME);
        getSupportActionBar().setTitle(monthName);

        // 3) Configura ViewPager + Tabs
        ViewPager2 pager = findViewById(R.id.viewPager);
        pager.setAdapter(new DetailPagerAdapter(this, monthName));

        TabLayout tabs = findViewById(R.id.tabLayout);
        new TabLayoutMediator(tabs, pager,
                (tab, position) -> {
                    tab.setText(position == 0 ? "Overview" : "History");
                }
        ).attach();
    }

    // seta ação do back na toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
