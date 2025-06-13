package com.example.meufinanceiro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import android.widget.TextView;

public class OverviewFragment extends Fragment {

    private static final String ARG_MONTH = "ARG_MONTH";

    public static OverviewFragment newInstance(String month) {
        Bundle b = new Bundle();
        b.putString(ARG_MONTH, month);
        OverviewFragment f = new OverviewFragment();
        f.setArguments(b);
        return f;
    }

    @Nullable @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View v = inflater.inflate(R.layout.fragment_overview, container, false);
        String month = getArguments().getString(ARG_MONTH);
        ((TextView)v.findViewById(R.id.tvOverviewTitle))
                .setText("Overview de " + month);
        // TODO: aqui você inflaria os totais e gráfico...
        return v;
    }
}
