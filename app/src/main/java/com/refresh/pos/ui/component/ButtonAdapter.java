package com.refresh.pos.ui.component;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;

import com.refresh.pos.R;

/**
 * An adapter for ListView which able to assign a sub-button in each row data.
 * 
 * @author Refresh Team
 *
 */
public class ButtonAdapter extends SimpleAdapter {

	private List<? extends Map<String, ?>> data;
	private int buttonId;
	private String tag;
	private boolean hideButton = false;
	
	/**
	 * Construct a new ButtonAdapter
	 * @param context
	 * @param data
	 * @param resource
	 * @param from
	 * @param to
	 * @param buttonId
	 * @param tag
	 */
	public ButtonAdapter(Context context, List<? extends Map<String, ?>> data,
			int resource, String[] from, int[] to, int buttonId, String tag) {
		this(context, data, resource, from, to, buttonId, tag, false);
	}

	public ButtonAdapter(Context context, List<? extends Map<String, ?>> data,
			int resource, String[] from, int[] to, int buttonId, String tag, boolean hideButton) {
		super(context, data, resource, from, to);
		this.data = data;
		this.buttonId = buttonId;
		this.tag = tag;
		this.hideButton = hideButton;

		setViewBinder(new SimpleAdapter.ViewBinder() {
			@Override
			public boolean setViewValue(View view, Object data, String textRepresentation) {
				if (view.getId() == R.id.productImage) {
					ImageView imageView = (ImageView) view;
					String imagePath = (String) data;
					if (imagePath != null && imagePath.length() > 0) {
						Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
						if (bitmap != null) {
							imageView.setImageBitmap(bitmap);
							return true;
						}
					}
					imageView.setImageResource(R.drawable.ic_launcher);
					return true;
				}
				return false;
			}
		});
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);
		View button = view.findViewById(buttonId);
		if (button != null) {
			button.setTag(data.get(position).get(tag));
			if (hideButton) {
				button.setVisibility(View.GONE);
			}
		}
		return view;
	}

}
