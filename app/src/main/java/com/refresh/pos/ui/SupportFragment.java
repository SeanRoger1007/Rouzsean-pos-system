package com.refresh.pos.ui;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.refresh.pos.R;
import com.refresh.pos.ui.component.UpdatableFragment;

public class SupportFragment extends UpdatableFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_support, container, false);

        view.findViewById(R.id.option_terms).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog("Terms of use", LegalContent.TERMS_OF_USE);
            }
        });

        view.findViewById(R.id.option_privacy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog("Privacy policy", LegalContent.PRIVACY_POLICY);
            }
        });

        int theme = com.refresh.pos.domain.ThemeController.getInstance(getActivity()).getTheme();
        if (theme == com.refresh.pos.domain.ThemeController.THEME_DARK) {
            view.findViewById(R.id.support_root).setBackgroundColor(Color.parseColor("#121212"));
            ((TextView) view.findViewById(R.id.text_terms)).setTextColor(Color.WHITE);
            ((TextView) view.findViewById(R.id.text_privacy)).setTextColor(Color.WHITE);
        }

        return view;
    }

    private void showDialog(String title, String content) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        
        TextView textView = new TextView(getActivity());
        textView.setText(content);
        textView.setPadding(40, 40, 40, 40);
        textView.setTextSize(14);
        
        ScrollView scrollView = new ScrollView(getActivity());
        scrollView.addView(textView);
        
        builder.setView(scrollView);
        builder.setPositiveButton("Close", null);
        builder.show();
    }

    @Override
    public void update() {
    }
}
