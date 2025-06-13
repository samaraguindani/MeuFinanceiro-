package com.example.meufinanceiro;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import android.widget.TextView;

public class HistoryFragment extends Fragment {

    private static final String ARG_MONTH = "ARG_MONTH";

    public static HistoryFragment newInstance(String month) {
        Bundle b = new Bundle();
        b.putString(ARG_MONTH, month);
        HistoryFragment f = new HistoryFragment();
        f.setArguments(b);
        return f;
    }

    @Nullable @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View v = inflater.inflate(R.layout.fragment_history, container, false);
        String month = getArguments().getString(ARG_MONTH);
        ((TextView)v.findViewById(R.id.tvHistoryTitle))
                .setText("History de " + month);
        // TODO: aqui você colocaria uma RecyclerView com as transações
        return v;
    }
}
