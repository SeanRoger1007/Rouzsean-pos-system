package com.refresh.pos.ui.inventory;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import com.refresh.pos.R;
import com.refresh.pos.domain.inventory.Inventory;
import com.refresh.pos.domain.inventory.ProductCatalog;
import com.refresh.pos.techicalservices.NoDaoSetException;

@SuppressLint("ValidFragment")
public class AddCategoryDialogFragment extends DialogFragment {

    private EditText categoryNameBox;
    private EditText categoryIconBox;
    private GridView colorGridView;
    private Button confirmButton;
    private Button cancelButton;
    private ProductCatalog productCatalog;
    private String selectedColor = "#4CAF50"; // Default green
    private AddProductDialogFragment parentDialog;

    private final String[] colors = {
            "#F44336", "#E91E63", "#9C27B0", "#673AB7",
            "#3F51B5", "#2196F3", "#03A9F4", "#00BCD4",
            "#009688", "#4CAF50", "#8BC34A", "#CDDC39",
            "#FFEB3B", "#FFC107", "#FF9800", "#FF5722"
    };

    public AddCategoryDialogFragment(AddProductDialogFragment parentDialog) {
        this.parentDialog = parentDialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.layout_addcategory, container, false);

        try {
            productCatalog = Inventory.getInstance().getProductCatalog();
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }

        categoryNameBox = (EditText) v.findViewById(R.id.categoryNameBox);
        categoryIconBox = (EditText) v.findViewById(R.id.categoryIconBox);
        colorGridView = (GridView) v.findViewById(R.id.colorGridView);
        confirmButton = (Button) v.findViewById(R.id.confirmButton);
        cancelButton = (Button) v.findViewById(R.id.cancelButton);

        colorGridView.setAdapter(new ColorAdapter());
        colorGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedColor = colors[position];
                ((ColorAdapter)colorGridView.getAdapter()).notifyDataSetChanged();
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = categoryNameBox.getText().toString();
                String icon = categoryIconBox.getText().toString();
                if (name.isEmpty()) {
                    Toast.makeText(getActivity(), "Please enter category name", Toast.LENGTH_SHORT).show();
                    return;
                }
                productCatalog.addCategory(name, selectedColor, icon);
                if (parentDialog != null) {
                    parentDialog.updateCategories();
                }
                dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return v;
    }

    private class ColorAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return colors.length;
        }

        @Override
        public Object getItem(int position) {
            return colors[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = new View(getActivity());
            view.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 80));
            view.setBackgroundColor(Color.parseColor(colors[position]));
            if (colors[position].equals(selectedColor)) {
                view.setAlpha(0.5f); // Simple selection feedback
            } else {
                view.setAlpha(1.0f);
            }
            return view;
        }
    }
}
