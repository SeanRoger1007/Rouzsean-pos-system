package com.refresh.pos.ui.inventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.refresh.pos.R;
import com.refresh.pos.domain.inventory.Category;
import com.refresh.pos.domain.inventory.Inventory;
import com.refresh.pos.domain.inventory.Product;
import com.refresh.pos.domain.inventory.ProductCatalog;
import com.refresh.pos.techicalservices.NoDaoSetException;
import com.refresh.pos.ui.MainActivity;
import com.refresh.pos.ui.component.UpdatableFragment;
import com.refresh.pos.ui.sale.SaleFragment;

/**
 * UI for Inventory.
 * @author Refresh Team
 *
 */
public class InventoryFragment extends UpdatableFragment {

	private ExpandableListView expandableListView;
	private ListView flatListView;
	private ProductCatalog productCatalog;
	private Button addProductButton;
	private EditText searchBox;
	private Button scanButton;
	private TextView totalItemsText;
	private TextView totalCategoriesText;
	private View btnAllItems, btnCategories;
	
	private enum ViewMode { ALL_ITEMS, CATEGORIES }
	private ViewMode currentViewMode = ViewMode.CATEGORIES;

	private Resources res;

	public InventoryFragment(UpdatableFragment fragment) {
		super();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		try {
			productCatalog = Inventory.getInstance().getProductCatalog();
		} catch (NoDaoSetException e) {
			e.printStackTrace();
		}
		
		View view = inflater.inflate(R.layout.layout_inventory, container, false);
		
		res = getResources();
		expandableListView = (ExpandableListView) view.findViewById(R.id.productListView);
		flatListView = (ListView) view.findViewById(R.id.flatProductListView);
		addProductButton = (Button) view.findViewById(R.id.addProductButton);
		searchBox = (EditText) view.findViewById(R.id.searchBox);
		scanButton = (Button) view.findViewById(R.id.scanButton);
		totalItemsText = (TextView) view.findViewById(R.id.text_total_items);
		totalCategoriesText = (TextView) view.findViewById(R.id.text_total_categories);
		btnAllItems = view.findViewById(R.id.btn_all_items);
		btnCategories = view.findViewById(R.id.btn_categories);

		applyDarkMode(view);
		initUI();
		return view;
	}

	private void applyDarkMode(View v) {
		int theme = com.refresh.pos.domain.ThemeController.getInstance(getActivity()).getTheme();
		if (theme == com.refresh.pos.domain.ThemeController.THEME_DARK) {
			v.findViewById(R.id.inventory_root).setBackgroundColor(Color.parseColor("#121212"));
			searchBox.setBackgroundColor(Color.parseColor("#1A1A1A"));
			searchBox.setTextColor(Color.WHITE);
			searchBox.setHintTextColor(Color.parseColor("#999999"));
			
			expandableListView.setBackgroundColor(Color.parseColor("#121212"));
			flatListView.setBackgroundColor(Color.parseColor("#121212"));
			
			expandableListView.setDivider(new android.graphics.drawable.ColorDrawable(Color.parseColor("#333333")));
			expandableListView.setDividerHeight(1);
			flatListView.setDivider(new android.graphics.drawable.ColorDrawable(Color.parseColor("#333333")));
			flatListView.setDividerHeight(1);
		}
	}

	/**
	 * Initiate this UI.
	 */
	private void initUI() {
		btnCategories.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) { currentViewMode = ViewMode.CATEGORIES; updateViewMode(); }
		});
		btnAllItems.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) { currentViewMode = ViewMode.ALL_ITEMS; updateViewMode(); }
		});

		addProductButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showPopup(v);
			}
		});

		searchBox.addTextChangedListener(new android.text.TextWatcher() {
			@Override public void afterTextChanged(android.text.Editable s) { search(); }
			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
		});
		
		scanButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				IntentIntegrator integrator = IntentIntegrator.forSupportFragment(InventoryFragment.this);
				integrator.setPrompt("Scan a barcode");
				integrator.setBeepEnabled(true);
				integrator.setOrientationLocked(false);
				integrator.initiateScan();
			}
		});

		updateViewMode();
	}

	private void updateViewMode() {
		if (currentViewMode == ViewMode.CATEGORIES) {
			expandableListView.setVisibility(View.VISIBLE);
			flatListView.setVisibility(View.GONE);
			btnCategories.setBackgroundColor(Color.parseColor("#4CAF50")); // Matching layout_inventory default green
			btnAllItems.setBackgroundColor(Color.parseColor("#3F51B5")); // Matching layout_inventory blue
			// Using alphas or different color to show "selected"
			btnCategories.setAlpha(1.0f);
			btnAllItems.setAlpha(0.6f);
		} else {
			expandableListView.setVisibility(View.GONE);
			flatListView.setVisibility(View.VISIBLE);
			btnAllItems.setBackgroundColor(Color.parseColor("#3F51B5"));
			btnCategories.setBackgroundColor(Color.parseColor("#4CAF50"));
			btnAllItems.setAlpha(1.0f);
			btnCategories.setAlpha(0.6f);
		}
		update();
	}

	private void showList(List<Product> products) {
		totalItemsText.setText(String.valueOf(products.size()));
		
		// Setup Flat List
		flatListView.setAdapter(new FlatInventoryAdapter(getActivity(), products));

		// Setup Categorized List
		List<Category> categories = productCatalog.getAllCategories();
		java.util.Set<Integer> validCatIds = new java.util.HashSet<Integer>();
		for (Category c : categories) validCatIds.add(c.getId());
		
		totalCategoriesText.setText(String.valueOf(categories.size()));
		
		List<CategoryGroup> groups = new ArrayList<CategoryGroup>();
		
		// Add all Categories (even empty ones)
		for (Category cat : categories) {
			CategoryGroup group = new CategoryGroup(cat);
			for (Product p : products) {
				if (p.getCategoryId() == cat.getId()) {
					group.products.add(p);
				}
			}
			groups.add(group);
		}

		// Add "No Category" group for products with -1 or non-existent IDs
		CategoryGroup noCatGroup = new CategoryGroup(null);
		for (Product p : products) {
			if (!validCatIds.contains(p.getCategoryId())) {
				noCatGroup.products.add(p);
			}
		}
		if (!noCatGroup.products.isEmpty()) {
			groups.add(noCatGroup);
		}

		// Sort groups: No Category at bottom, others alphabetical
		Collections.sort(groups, new Comparator<CategoryGroup>() {
			@Override
			public int compare(CategoryGroup g1, CategoryGroup g2) {
				if (g1.category == null) return 1;
				if (g2.category == null) return -1;
				return g1.getName().compareToIgnoreCase(g2.getName());
			}
		});

		expandableListView.setAdapter(new InventoryExpandableAdapter(getActivity(), groups));
		
		// Expand all by default
		for (int i = 0; i < groups.size(); i++) {
			expandableListView.expandGroup(i);
		}
	}

	private void search() {
		String search = searchBox.getText().toString();
		if (search.equals("")) {
			showList(productCatalog.getAllProduct());
		} else {
			List<Product> results = productCatalog.searchProduct(search);
			final String query = search.toLowerCase();
			Collections.sort(results, new Comparator<Product>() {
				@Override
				public int compare(Product p1, Product p2) {
					boolean b1 = p1.getName().toLowerCase().startsWith(query);
					boolean b2 = p2.getName().toLowerCase().startsWith(query);
					if (b1 && !b2) return -1;
					if (!b1 && b2) return 1;
					return p1.getName().compareToIgnoreCase(p2.getName());
				}
			});
			showList(results);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
		if (scanningResult != null) {
			if (scanningResult.getContents() != null) {
				searchBox.setText(scanningResult.getContents());
			}
		} else {
			super.onActivityResult(requestCode, resultCode, intent);
		}
	}

	public void showPopup(View anchorView) {
		AddProductDialogFragment newFragment = new AddProductDialogFragment(InventoryFragment.this);
		newFragment.show(getFragmentManager(), "AddProductDialogFragment");
	}

	@Override
	public void update() {
		search();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (getUserVisibleHint()) {
			((MainActivity) getActivity()).setActionBarTitle(res.getString(R.string.inventory));
		}
		update();
	}

	private class CategoryGroup {
		Category category;
		List<Product> products = new ArrayList<>();
		CategoryGroup(Category c) { this.category = c; }
		String getName() {
			if (category == null) return "No Category";
			return category.getName();
		}
	}

	private class InventoryExpandableAdapter extends BaseExpandableListAdapter {
		private Context context;
		private List<CategoryGroup> groups;

		InventoryExpandableAdapter(Context context, List<CategoryGroup> groups) {
			this.context = context;
			this.groups = groups;
		}

		@Override public int getGroupCount() { return groups.size(); }
		@Override public int getChildrenCount(int g) { return groups.get(g).products.size(); }
		@Override public Object getGroup(int g) { return groups.get(g); }
		@Override public Object getChild(int g, int c) { return groups.get(g).products.get(c); }
		@Override public long getGroupId(int g) { return g; }
		@Override public long getChildId(int g, int c) { return c; }
		@Override public boolean hasStableIds() { return true; }

		@Override
		public View getGroupView(int g, boolean isExpanded, View v, ViewGroup parent) {
			if (v == null) v = LayoutInflater.from(context).inflate(R.layout.list_group_category, parent, false);
			
			int theme = com.refresh.pos.domain.ThemeController.getInstance(context).getTheme();
			boolean isDark = (theme == com.refresh.pos.domain.ThemeController.THEME_DARK);
			
			final CategoryGroup group = groups.get(g);
			TextView name = v.findViewById(R.id.text_category_name);
			TextView count = v.findViewById(R.id.text_item_count);
			View root = v.findViewById(R.id.category_group_root);
			View btnDelete = v.findViewById(R.id.btn_delete_category);
			TextView iconText = v.findViewById(R.id.category_icon);

			name.setText(group.getName());
			count.setText(group.products.size() + " items");

			if (group.category != null) {
				int color = Color.parseColor(group.category.getColor());
				root.setBackgroundColor(Color.argb(40, Color.red(color), Color.green(color), Color.blue(color)));
				name.setTextColor(isDark ? Color.WHITE : Color.BLACK);
				
				iconText.setText(group.category.getIcon() != null ? group.category.getIcon() : "");
				iconText.setBackgroundColor(color);
				
				btnDelete.setVisibility(View.VISIBLE);
				btnDelete.setOnClickListener(new View.OnClickListener() {
					@Override public void onClick(View v) { confirmDeleteCategory(group.category); }
				});
			} else {
				root.setBackgroundColor(isDark ? Color.parseColor("#1A1A1A") : Color.parseColor("#EEEEEE"));
				name.setTextColor(isDark ? Color.WHITE : Color.BLACK);
				iconText.setText("");
				iconText.setBackgroundColor(Color.GRAY);
				btnDelete.setVisibility(View.GONE);
			}

			return v;
		}

		private void confirmDeleteCategory(final Category cat) {
			new AlertDialog.Builder(context)
				.setTitle("Delete Category")
				.setMessage("Are you sure you want to delete '" + cat.getName() + "'?")
				.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
					@Override public void onClick(DialogInterface dialog, int which) { performDeleteCategory(cat); }
				})
				.setNegativeButton("Cancel", null)
				.show();
		}

		private void performDeleteCategory(Category cat) {
			try {
				productCatalog.deleteCategory(cat.getId());
				update();
				Toast.makeText(context, "Category deleted", Toast.LENGTH_SHORT).show();
			} catch (Exception e) { e.printStackTrace(); }
		}

		@Override
		public View getChildView(int g, int c, boolean isLast, View v, ViewGroup parent) {
			if (v == null) v = LayoutInflater.from(context).inflate(R.layout.listview_inventory, parent, false);
			
			int theme = com.refresh.pos.domain.ThemeController.getInstance(context).getTheme();
			boolean isDark = (theme == com.refresh.pos.domain.ThemeController.THEME_DARK);

			final Product product = groups.get(g).products.get(c);
			
			TextView nameText = v.findViewById(R.id.barcode_text);
			TextView soldPriceText = v.findViewById(R.id.sold_price_text);
			TextView priceText = v.findViewById(R.id.unit_price_text);
			TextView stockText = v.findViewById(R.id.stock_text);
			ImageView imageView = v.findViewById(R.id.productImage);
			TextView totalValueText = v.findViewById(R.id.total_value_text);
			TextView lowStockAlert = v.findViewById(R.id.low_stock_alert);
			View optionView = v.findViewById(R.id.optionView);
			View deleteView = v.findViewById(R.id.deleteView);

			if (isDark) {
				v.setBackgroundColor(Color.parseColor("#121212"));
				nameText.setTextColor(Color.WHITE);
				stockText.setTextColor(Color.parseColor("#CCCCCC"));
				totalValueText.setTextColor(Color.parseColor("#81C784"));
			} else {
				v.setBackgroundColor(Color.WHITE);
				nameText.setTextColor(Color.parseColor("#333333"));
				stockText.setTextColor(Color.parseColor("#333333"));
				totalValueText.setTextColor(Color.parseColor("#4CAF50"));
			}

			nameText.setText(product.getName());
			soldPriceText.setText(String.format(java.util.Locale.US, "₱%.2f", product.getUnitPrice()));
			
			try {
				double stockQty = Inventory.getInstance().getStock().getStockSumDoubleById(product.getId());
				List<com.refresh.pos.domain.inventory.ProductLot> lots = Inventory.getInstance().getStock().getProductLotByProductId(product.getId());
				double latestCost = (lots != null && !lots.isEmpty()) ? lots.get(lots.size() - 1).unitCost() : 0.0;
				priceText.setText(String.format(java.util.Locale.US, "₱%.2f", latestCost));
				
				if (stockQty <= 0) {
					stockText.setVisibility(View.GONE);
					lowStockAlert.setVisibility(View.VISIBLE);
					lowStockAlert.setText("⚠️ (NO STOCK!)");
				} else {
					stockText.setVisibility(View.VISIBLE);
					if (product.isPack()) {
						int ppp = product.getPiecesPerPack();
						int tp = (int) Math.round(stockQty * ppp);
						stockText.setText((tp/ppp) + " Pk, " + (tp%ppp) + " Pc");
					} else {
						stockText.setText("Qty: " + (int)stockQty);
					}
					
					if (stockQty < product.getLowStockThreshold()) {
						stockText.setTextColor(Color.RED);
						lowStockAlert.setVisibility(View.VISIBLE);
						lowStockAlert.setText("⚠️ (LOW STOCK!)");
					} else {
						stockText.setTextColor(isDark ? Color.parseColor("#CCCCCC") : Color.parseColor("#333333"));
						lowStockAlert.setVisibility(View.GONE);
					}
				}
				totalValueText.setText(String.format(java.util.Locale.US, "Total: ₱%.2f", latestCost * stockQty));
			} catch (NoDaoSetException e) {}

			if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
				Bitmap bitmap = SaleFragment.decodeSampledBitmapFromFile(product.getImagePath(), 100, 100);
				if (bitmap != null) imageView.setImageBitmap(bitmap);
				else imageView.setImageResource(R.drawable.ic_launcher);
			} else {
				imageView.setImageResource(R.drawable.ic_launcher);
			}

			optionView.setTag(product.getId());
			optionView.setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View view) { ((MainActivity) getActivity()).optionOnClickHandler(view); }
			});

			deleteView.setTag(product.getId());
			deleteView.setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View v) { confirmDelete(product); }
			});

			return v;
		}

		private void confirmDelete(final Product product) {
			new AlertDialog.Builder(context)
				.setTitle("Remove Product")
				.setMessage("Are you sure you want to completely remove '" + product.getName() + "' and all its history?")
				.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
					@Override public void onClick(DialogInterface dialog, int which) { performDelete(product); }
				})
				.setNegativeButton("Cancel", null)
				.show();
		}

		private void performDelete(Product product) {
			try {
				productCatalog.removeProductCompletely(product);
				update();
				Toast.makeText(context, "Product removed completely", Toast.LENGTH_SHORT).show();
			} catch (Exception e) { e.printStackTrace(); }
		}

		@Override public boolean isChildSelectable(int g, int c) { return true; }
	}

	private class FlatInventoryAdapter extends BaseAdapter {
		private Context context;
		private List<Product> products;

		FlatInventoryAdapter(Context context, List<Product> products) {
			this.context = context;
			this.products = products;
		}

		@Override public int getCount() { return products.size(); }
		@Override public Object getItem(int position) { return products.get(position); }
		@Override public long getItemId(int position) { return position; }

		@Override
		public View getView(int position, View v, ViewGroup parent) {
			if (v == null) v = LayoutInflater.from(context).inflate(R.layout.listview_inventory, parent, false);
			
			int theme = com.refresh.pos.domain.ThemeController.getInstance(context).getTheme();
			boolean isDark = (theme == com.refresh.pos.domain.ThemeController.THEME_DARK);

			final Product product = products.get(position);
			
			TextView nameText = v.findViewById(R.id.barcode_text);
			TextView soldPriceText = v.findViewById(R.id.sold_price_text);
			TextView priceText = v.findViewById(R.id.unit_price_text);
			TextView stockText = v.findViewById(R.id.stock_text);
			ImageView imageView = v.findViewById(R.id.productImage);
			TextView totalValueText = v.findViewById(R.id.total_value_text);
			TextView lowStockAlert = v.findViewById(R.id.low_stock_alert);
			View optionView = v.findViewById(R.id.optionView);
			View deleteView = v.findViewById(R.id.deleteView);

			if (isDark) {
				v.setBackgroundColor(Color.parseColor("#121212"));
				nameText.setTextColor(Color.WHITE);
				stockText.setTextColor(Color.parseColor("#CCCCCC"));
				totalValueText.setTextColor(Color.parseColor("#81C784"));
			} else {
				v.setBackgroundColor(Color.WHITE);
				nameText.setTextColor(Color.parseColor("#333333"));
				stockText.setTextColor(Color.parseColor("#333333"));
				totalValueText.setTextColor(Color.parseColor("#4CAF50"));
			}

			nameText.setText(product.getName());
			soldPriceText.setText(String.format(java.util.Locale.US, "₱%.2f", product.getUnitPrice()));
			
			try {
				double stockQty = Inventory.getInstance().getStock().getStockSumDoubleById(product.getId());
				List<com.refresh.pos.domain.inventory.ProductLot> lots = Inventory.getInstance().getStock().getProductLotByProductId(product.getId());
				double latestCost = (lots != null && !lots.isEmpty()) ? lots.get(lots.size() - 1).unitCost() : 0.0;
				priceText.setText(String.format(java.util.Locale.US, "₱%.2f", latestCost));
				
				if (stockQty <= 0) {
					stockText.setVisibility(View.GONE);
					lowStockAlert.setVisibility(View.VISIBLE);
					lowStockAlert.setText("⚠️ (NO STOCK!)");
				} else {
					stockText.setVisibility(View.VISIBLE);
					if (product.isPack()) {
						int ppp = product.getPiecesPerPack();
						int tp = (int) Math.round(stockQty * ppp);
						stockText.setText((tp/ppp) + " Pk, " + (tp%ppp) + " Pc");
					} else {
						stockText.setText("Qty: " + (int)stockQty);
					}
					
					if (stockQty < product.getLowStockThreshold()) {
						stockText.setTextColor(Color.RED);
						lowStockAlert.setVisibility(View.VISIBLE);
						lowStockAlert.setText("⚠️ (LOW STOCK!)");
					} else {
						stockText.setTextColor(isDark ? Color.parseColor("#CCCCCC") : Color.parseColor("#333333"));
						lowStockAlert.setVisibility(View.GONE);
					}
				}
				totalValueText.setText(String.format(java.util.Locale.US, "Total: ₱%.2f", latestCost * stockQty));
			} catch (NoDaoSetException e) {}

			if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
				Bitmap bitmap = SaleFragment.decodeSampledBitmapFromFile(product.getImagePath(), 100, 100);
				if (bitmap != null) imageView.setImageBitmap(bitmap);
				else imageView.setImageResource(R.drawable.ic_launcher);
			} else {
				imageView.setImageResource(R.drawable.ic_launcher);
			}

			optionView.setTag(product.getId());
			optionView.setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View view) { ((MainActivity) getActivity()).optionOnClickHandler(view); }
			});

			deleteView.setTag(product.getId());
			deleteView.setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View v) { confirmDelete(product); }
			});

			return v;
		}

		private void confirmDelete(final Product product) {
			new AlertDialog.Builder(context)
				.setTitle("Remove Product")
				.setMessage("Are you sure you want to completely remove '" + product.getName() + "' and all its history?")
				.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
					@Override public void onClick(DialogInterface dialog, int which) { performDelete(product); }
				})
				.setNegativeButton("Cancel", null)
				.show();
		}

		private void performDelete(Product product) {
			try {
				productCatalog.removeProductCompletely(product);
				update();
				Toast.makeText(context, "Product removed completely", Toast.LENGTH_SHORT).show();
			} catch (Exception e) { e.printStackTrace(); }
		}
	}
}
