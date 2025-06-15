package com.example.meufinanceiro;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MonthDetailActivity extends AppCompatActivity {

    private String monthName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_month_detail);

        // Agora o nome do mês vem dinamicamente da Intent:
        monthName = getIntent().getStringExtra("EXTRA_MONTH_NAME");

        // Toolbar SEM seta de back
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(monthName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        // ViewPager + Tabs
        ViewPager2 pager = findViewById(R.id.viewPager);
        pager.setAdapter(new DetailPagerAdapter(this, monthName));

        TabLayout tabs = findViewById(R.id.tabLayout);
        new TabLayoutMediator(tabs, pager, (tab, position) -> {
            tab.setText(position == 0 ? "Overview" : "History");
        }).attach();

        // FloatingActionButton para adicionar transação
        FloatingActionButton fabAddTransaction = findViewById(R.id.fabAddTransaction);
        fabAddTransaction.setOnClickListener(v -> {
            AddTransactionDialogFragment dialog = new AddTransactionDialogFragment(monthName);
            dialog.show(getSupportFragmentManager(), "AddTransaction");
        });

        configurarBottomNavigation();
    }

    private void configurarBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Marca o ícone Transactions como ativo
        bottomNav.setSelectedItemId(R.id.nav_transactions);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_transactions) {
                return true;
            } else if (id == R.id.nav_home) {
                startActivity(new Intent(MonthDetailActivity.this, MainActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(MonthDetailActivity.this, SettingsActivity.class));
                return true;
            }
            return false;
        });
    }
}
