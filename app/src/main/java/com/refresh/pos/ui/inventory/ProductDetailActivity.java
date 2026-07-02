package com.refresh.pos.ui.inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.refresh.pos.R;
import com.refresh.pos.domain.DateTimeStrategy;
import com.refresh.pos.domain.inventory.Category;
import com.refresh.pos.domain.inventory.Inventory;
import com.refresh.pos.domain.inventory.Product;
import com.refresh.pos.domain.inventory.ProductCatalog;
import com.refresh.pos.domain.inventory.ProductLot;
import com.refresh.pos.domain.inventory.Stock;
import com.refresh.pos.techicalservices.NoDaoSetException;

/**
 * UI for shows the datails of each Product.
 * @author Refresh Team
 *
 */
@SuppressLint("NewApi")
public class ProductDetailActivity extends FragmentActivity {

	private ProductCatalog productCatalog;
	private Stock stock;
	private Product product;
	private List<Category> categoriesList;
	private List<Map<String, String>> stockList;
	private EditText nameBox;
	private EditText barcodeBox;
	private EditText lowStockThresholdBox;
	private TextView stockSumBox;
	private EditText priceBox;
	private Spinner categorySpinner;
	private Button submitEditButton;
	private Button cancelEditButton;
	private Button openEditButton;
	private View editButtonGroup;
	private LinearLayout purchaseHistoryList;
	private ImageView productImage;
	private TabHost mTabHost;
	private ListView stockListView;
	private String id;
	private String[] remember;
	private LayoutInflater inflater ;
	private Resources res;
	private CheckBox checkboxIsPack;
	private View layoutPackDetails;
	private EditText piecesPerPackBox;
	private EditText piecePriceBox;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    return true;
	  } 
	
	@SuppressLint("NewApi")
	private void initiateActionBar() {
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ActionBar actionBar = getActionBar();
			if (actionBar != null) {
				actionBar.setDisplayHomeAsUpEnabled(true);
				actionBar.setTitle(res.getString(R.string.product_detail));
				actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#33B5E5")));
			}
		}
	}
	
	private void applyDarkMode() {
		int theme = com.refresh.pos.domain.ThemeController.getInstance(this).getTheme();
		if (theme == com.refresh.pos.domain.ThemeController.THEME_DARK) {
			View root = findViewById(R.id.product_detail_root);
			if (root != null) root.setBackgroundColor(Color.parseColor("#121212"));
			
			View tab1 = findViewById(R.id.tab1);
			if (tab1 != null) tab1.setBackgroundColor(Color.parseColor("#121212"));
			
			View mainCard = findViewById(R.id.main_info_card);
			if (mainCard != null) mainCard.setBackgroundColor(Color.parseColor("#1A1A1A"));
			
			View purchaseContainer = findViewById(R.id.purchase_details_container);
			if (purchaseContainer != null) purchaseContainer.setBackgroundColor(Color.parseColor("#1A1A1A"));
			
			View purchaseHeader = findViewById(R.id.purchase_table_header);
			if (purchaseHeader != null) purchaseHeader.setBackgroundColor(Color.parseColor("#333333"));
			
			View tab2 = findViewById(R.id.tab2);
			if (tab2 != null) tab2.setBackgroundColor(Color.parseColor("#121212"));

			// Stock Table Header
			View stockHeader = findViewById(R.id.stock_table_header);
			if (stockHeader != null) {
				findViewById(R.id.row_date).setBackgroundColor(Color.parseColor("#1A1A1A"));
				findViewById(R.id.row_cost).setBackgroundColor(Color.parseColor("#1A1A1A"));
				findViewById(R.id.row_quantity).setBackgroundColor(Color.parseColor("#1A1A1A"));
				
				findViewById(R.id.dateAdded_header).setBackgroundColor(Color.parseColor("#2C3E50"));
				findViewById(R.id.cost_header).setBackgroundColor(Color.parseColor("#2C3E50"));
				findViewById(R.id.quantity_header).setBackgroundColor(Color.parseColor("#2C3E50"));
			}
			
			if (stockListView != null) {
				stockListView.setBackgroundColor(Color.parseColor("#121212"));
				stockListView.setDivider(new ColorDrawable(Color.parseColor("#333333")));
				stockListView.setDividerHeight(1);
			}
			
			int white = Color.WHITE;
			int gray = Color.parseColor("#999999");
			
			if (nameBox != null) nameBox.setTextColor(white);
			if (barcodeBox != null) barcodeBox.setTextColor(white);
			if (lowStockThresholdBox != null) lowStockThresholdBox.setTextColor(white);
			if (stockSumBox != null) stockSumBox.setTextColor(white);
			if (piecesPerPackBox != null) piecesPerPackBox.setTextColor(white);
			
			TextView labelCat = findViewById(R.id.label_category);
			if (labelCat != null) labelCat.setTextColor(gray);
			
			TextView labelBarcode = findViewById(R.id.label_barcode);
			if (labelBarcode != null) labelBarcode.setTextColor(gray);
			
			TextView labelPrice = findViewById(R.id.label_sale_price);
			if (labelPrice != null) labelPrice.setTextColor(gray);
			
			TextView labelStockAlert = findViewById(R.id.label_stock_alert);
			if (labelStockAlert != null) labelStockAlert.setTextColor(gray);
			
			TextView labelTotalStock = findViewById(R.id.label_total_stock);
			if (labelTotalStock != null) labelTotalStock.setTextColor(gray);
			
			TextView labelPPP = findViewById(R.id.label_pieces_per_pack);
			if (labelPPP != null) labelPPP.setTextColor(gray);
			
			TextView labelPricePP = findViewById(R.id.label_price_per_piece);
			if (labelPricePP != null) labelPricePP.setTextColor(gray);
			
			TextView labelPurchaseDetails = findViewById(R.id.label_purchase_details);
			if (labelPurchaseDetails != null) labelPurchaseDetails.setTextColor(gray);
			
			TextView hDate = findViewById(R.id.header_date);
			if (hDate != null) hDate.setTextColor(white);
			
			TextView hCost = findViewById(R.id.header_cost);
			if (hCost != null) hCost.setTextColor(white);
			
			TextView hQty = findViewById(R.id.header_qty);
			if (hQty != null) hQty.setTextColor(white);
			
			if (checkboxIsPack != null) checkboxIsPack.setTextColor(white);
			if (productImage != null) productImage.setBackgroundColor(Color.parseColor("#333333"));

			// Tab Labels
			for (int i = 0; i < mTabHost.getTabWidget().getChildCount(); i++) {
				TextView tv = (TextView) mTabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
				if (tv != null) tv.setTextColor(white);
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		res = getResources();
		initiateActionBar();
		
		try {
			stock = Inventory.getInstance().getStock();
			productCatalog = Inventory.getInstance().getProductCatalog();
		} catch (NoDaoSetException e) {
			e.printStackTrace();
		}

		id = getIntent().getStringExtra("id");
		if (id != null && !id.isEmpty()) {
			product = productCatalog.getProductById(Integer.parseInt(id));
		}

		if (product == null) {
			Toast.makeText(this, "Error: Product not found", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		initUI(savedInstanceState);
		applyDarkMode();
		remember = new String[4];
		nameBox.setText(product.getName());
		priceBox.setText(product.getUnitPrice() + "");
		barcodeBox.setText(product.getBarcode());
		lowStockThresholdBox.setText(product.getLowStockThreshold() + "");
		
		checkboxIsPack.setChecked(product.isPack());
		layoutPackDetails.setVisibility(product.isPack() ? View.VISIBLE : View.GONE);
		piecesPerPackBox.setText(product.getPiecesPerPack() + "");
		piecePriceBox.setText(String.format(java.util.Locale.US, "₱%.2f", product.getPiecePrice()));

	}

	/**
	 * Initiate this UI.
	 * @param savedInstanceState
	 */
	private void initUI(Bundle savedInstanceState) {
		setContentView(R.layout.layout_productdetail_main);
		stockListView = (ListView) findViewById(R.id.stockListView);
		nameBox = (EditText) findViewById(R.id.nameBox);
		priceBox = (EditText) findViewById(R.id.priceBox);
		barcodeBox = (EditText) findViewById(R.id.barcodeBox);
		lowStockThresholdBox = (EditText) findViewById(R.id.lowStockThresholdBox);
		stockSumBox = (TextView) findViewById(R.id.stockSumBox);
		categorySpinner = (Spinner) findViewById(R.id.categorySpinner);
		
		submitEditButton = (Button) findViewById(R.id.submitEditButton);
		cancelEditButton = (Button) findViewById(R.id.cancelEditButton);
		openEditButton = (Button) findViewById(R.id.openEditButton);
		editButtonGroup = findViewById(R.id.editButtonGroup);
		
		purchaseHistoryList = (LinearLayout) findViewById(R.id.purchaseHistoryList);
		productImage = (ImageView) findViewById(R.id.productImage);
		
		checkboxIsPack = (CheckBox) findViewById(R.id.checkbox_is_pack);
		layoutPackDetails = findViewById(R.id.layout_pack_details);
		piecesPerPackBox = (EditText) findViewById(R.id.piecesPerPackBox);
		piecePriceBox = (EditText) findViewById(R.id.piecePriceBox);

		checkboxIsPack.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(android.widget.CompoundButton buttonView, boolean isChecked) {
				layoutPackDetails.setVisibility(isChecked ? View.VISIBLE : View.GONE);
			}
		});

		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup();
		mTabHost.addTab(mTabHost.newTabSpec("tab_test1").setIndicator(res.getString(R.string.product_detail))
				.setContent(R.id.tab1));
		mTabHost.addTab(mTabHost.newTabSpec("tab_test2").setIndicator(res.getString(R.string.stock))
				.setContent(R.id.tab2));
		mTabHost.setCurrentTab(0);
		inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);

		openEditButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				edit();
			}
		});

		submitEditButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				submitEdit();
			}
		});
		
		cancelEditButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				cancelEdit();
			}
		});
	}

	/**
	 * Show list.
	 * @param list
	 */
	private void showList(List<ProductLot> list) {

		stockList = new ArrayList<Map<String, String>>();
		for (ProductLot productLot : list) {
			stockList.add(productLot.toMap());
		}

		SimpleAdapter sAdap = new SimpleAdapter(ProductDetailActivity.this, stockList,
				R.layout.listview_stock, new String[] { "dateAdded",
				"cost", "quantity" }, new int[] {
				R.id.dateAdded, R.id.cost, R.id.quantity, }) {
			@Override
			public View getView(int position, View convertView, android.view.ViewGroup parent) {
				View view = super.getView(position, convertView, parent);
				int theme = com.refresh.pos.domain.ThemeController.getInstance(ProductDetailActivity.this).getTheme();
				boolean isDark = (theme == com.refresh.pos.domain.ThemeController.THEME_DARK);
				
				if (isDark) {
					view.setBackgroundColor(Color.parseColor("#121212"));
					((TextView) view.findViewById(R.id.dateAdded)).setTextColor(Color.WHITE);
					((TextView) view.findViewById(R.id.cost)).setTextColor(Color.WHITE);
					((TextView) view.findViewById(R.id.quantity)).setTextColor(Color.WHITE);
					
					// Update individual TableRow backgrounds if they exist
					View r1 = view.findViewById(R.id.dateAdded).getParent() instanceof View ? (View) view.findViewById(R.id.dateAdded).getParent() : null;
					View r2 = view.findViewById(R.id.cost).getParent() instanceof View ? (View) view.findViewById(R.id.cost).getParent() : null;
					View r3 = view.findViewById(R.id.quantity).getParent() instanceof View ? (View) view.findViewById(R.id.quantity).getParent() : null;
					
					if (r1 != null) r1.setBackgroundColor(Color.parseColor("#1A1A1A"));
					if (r2 != null) r2.setBackgroundColor(Color.parseColor("#1A1A1A"));
					if (r3 != null) r3.setBackgroundColor(Color.parseColor("#1A1A1A"));
				}
				return view;
			}
		};
		stockListView.setAdapter(sAdap);
	}

	@Override
	protected void onResume() {
		super.onResume();
		int productId = Integer.parseInt(id);
		stockSumBox.setText(stock.getStockSumById(productId)+"");
		showList(stock.getProductLotByProductId(productId));
		
		updatePurchaseHistory(productId);
		loadCategories();
		
		// Update Image
		if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
			Bitmap bitmap = BitmapFactory.decodeFile(product.getImagePath());
			if (bitmap != null) {
				productImage.setImageBitmap(bitmap);
			} else {
				productImage.setImageResource(R.drawable.ic_launcher);
			}
		} else {
			productImage.setImageResource(R.drawable.ic_launcher);
		}
	}

	private void loadCategories() {
		categoriesList = productCatalog.getAllCategories();
		List<String> names = new ArrayList<>();
		names.add("No Category");
		int selectedIndex = 0;
		for (int i = 0; i < categoriesList.size(); i++) {
			names.add(categoriesList.get(i).getName());
			if (categoriesList.get(i).getId() == product.getCategoryId()) {
				selectedIndex = i + 1;
			}
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		categorySpinner.setAdapter(adapter);
		categorySpinner.setSelection(selectedIndex);
	}

	private void updatePurchaseHistory(int productId) {
		purchaseHistoryList.removeAllViews();
		List<ProductLot> lots = stock.getProductLotByProductId(productId);
		
		if (lots == null || lots.isEmpty()) {
			TextView emptyText = new TextView(this);
			emptyText.setText("No purchase history found.");
			emptyText.setPadding(20, 20, 20, 20);
			emptyText.setTextSize(12);
			
			int theme = com.refresh.pos.domain.ThemeController.getInstance(this).getTheme();
			if (theme == com.refresh.pos.domain.ThemeController.THEME_DARK) {
				emptyText.setTextColor(Color.WHITE);
			}

			purchaseHistoryList.addView(emptyText);
			return;
		}

		int theme = com.refresh.pos.domain.ThemeController.getInstance(this).getTheme();
		boolean isDark = (theme == com.refresh.pos.domain.ThemeController.THEME_DARK);

		for (ProductLot lot : lots) {
			View itemView = inflater.inflate(R.layout.list_item_purchase_detail, purchaseHistoryList, false);
			TextView tvDate = (TextView) itemView.findViewById(R.id.dateText);
			TextView tvCost = (TextView) itemView.findViewById(R.id.costText);
			TextView tvQty = (TextView) itemView.findViewById(R.id.qtyText);

			tvDate.setText(lot.getDateAdded());
			tvCost.setText(String.format(java.util.Locale.US, "₱%.2f", lot.unitCost()));
			tvQty.setText(String.valueOf(lot.getQuantity()));
			
			if (isDark) {
				tvDate.setTextColor(Color.WHITE);
				tvCost.setTextColor(Color.WHITE);
				tvQty.setTextColor(Color.WHITE);
			}

			purchaseHistoryList.addView(itemView);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			if (editButtonGroup != null && editButtonGroup.getVisibility() == View.VISIBLE) {
				cancelEdit();
			} else {
				this.finish();
			}
			return true;
		} else if (id == R.id.action_edit) {
			edit();
			return true;
		} else if (id == R.id.action_restock) {
			showRestockDialog();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	private void showRestockDialog() {
		com.refresh.pos.ui.inventory.RestockDialogFragment dialog = new com.refresh.pos.ui.inventory.RestockDialogFragment(product);
		dialog.show(getSupportFragmentManager(), "RestockDialogFragment");
	}
	
	/**
	 * Submit editing.
	 */
	private void submitEdit() {
		if (nameBox == null || product == null) return;
		
		nameBox.setFocusable(false);
		nameBox.setFocusableInTouchMode(false);
		nameBox.setBackgroundColor(Color.TRANSPARENT);
		priceBox.setFocusable(false);
		priceBox.setFocusableInTouchMode(false);
		priceBox.setBackgroundColor(Color.TRANSPARENT);
		barcodeBox.setFocusable(false);
		barcodeBox.setFocusableInTouchMode(false);
		barcodeBox.setBackgroundColor(Color.TRANSPARENT);
		lowStockThresholdBox.setFocusable(false);
		lowStockThresholdBox.setFocusableInTouchMode(false);
		lowStockThresholdBox.setBackgroundColor(Color.TRANSPARENT);
		categorySpinner.setEnabled(false);
		checkboxIsPack.setEnabled(false);
		piecesPerPackBox.setFocusable(false);
		piecesPerPackBox.setFocusableInTouchMode(false);
		piecesPerPackBox.setBackgroundColor(Color.TRANSPARENT);
		piecePriceBox.setFocusable(false);
		piecePriceBox.setFocusableInTouchMode(false);
		piecePriceBox.setBackgroundColor(Color.TRANSPARENT);
		
		product.setName(nameBox.getText().toString());
		String priceStr = priceBox.getText().toString().replace("₱", "").trim();
		if(priceStr.equals("")) priceStr = "0.0";
		product.setUnitPrice(Double.parseDouble(priceStr));
		
		product.setBarcode(barcodeBox.getText().toString());
		if(lowStockThresholdBox.getText().toString().equals(""))
			lowStockThresholdBox.setText("0");
		product.setLowStockThreshold(Integer.parseInt(lowStockThresholdBox.getText().toString()));
		
		// Update Category
		int pos = categorySpinner.getSelectedItemPosition();
		if (pos == 0) {
			product.setCategoryId(-1);
		} else if (categoriesList != null && (pos - 1) < categoriesList.size()) {
			product.setCategoryId(categoriesList.get(pos - 1).getId());
		}
		
		product.setPack(checkboxIsPack.isChecked());
		if (product.isPack()) {
			try {
				product.setPiecesPerPack(Integer.parseInt(piecesPerPackBox.getText().toString()));
			} catch (Exception e) {
				product.setPiecesPerPack(1);
			}
			try {
				String piecePriceStr = piecePriceBox.getText().toString().replace("₱", "").trim();
				product.setPiecePrice(Double.parseDouble(piecePriceStr));
			} catch (Exception e) {
				product.setPiecePrice(0.0);
			}
		}
		
		productCatalog.editProduct(product);
		Toast.makeText(this, "Changes saved successfully", Toast.LENGTH_SHORT).show();
		setResult(RESULT_OK);
		
		ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.setTitle(res.getString(R.string.product_detail));
		}
		editButtonGroup.setVisibility(View.GONE);
		submitEditButton.setVisibility(View.GONE); // Hide Save button
		openEditButton.setVisibility(View.VISIBLE);
		onResume(); // Refresh display
	}
	
	/**
	 * Cancel editing.
	 */
	private void cancelEdit() {
		if (nameBox == null || remember == null) return;
		
		nameBox.setFocusable(false);
		nameBox.setFocusableInTouchMode(false);
		nameBox.setBackgroundColor(Color.TRANSPARENT);
		priceBox.setFocusable(false);
		priceBox.setFocusableInTouchMode(false);
		priceBox.setBackgroundColor(Color.TRANSPARENT);
		barcodeBox.setFocusable(false);
		barcodeBox.setFocusableInTouchMode(false);
		barcodeBox.setBackgroundColor(Color.TRANSPARENT);
		lowStockThresholdBox.setFocusable(false);
		lowStockThresholdBox.setFocusableInTouchMode(false);
		lowStockThresholdBox.setBackgroundColor(Color.TRANSPARENT);
		categorySpinner.setEnabled(false);
		checkboxIsPack.setEnabled(false);
		piecesPerPackBox.setFocusable(false);
		piecesPerPackBox.setFocusableInTouchMode(false);
		piecesPerPackBox.setBackgroundColor(Color.TRANSPARENT);
		piecePriceBox.setFocusable(false);
		piecePriceBox.setFocusableInTouchMode(false);
		piecePriceBox.setBackgroundColor(Color.TRANSPARENT);
		
		editButtonGroup.setVisibility(View.GONE);
		submitEditButton.setVisibility(View.GONE); // Hide Save button
		
		ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.setTitle(res.getString(R.string.product_detail));
		}
		nameBox.setText(remember[0]);
		priceBox.setText(remember[1]);
		barcodeBox.setText(remember[2]);
		lowStockThresholdBox.setText(remember[3]);
		openEditButton.setVisibility(View.VISIBLE);
		onResume(); // Refresh display
	}
	
	/**
	 * Edit
	 */
	private void edit() {
		if (nameBox == null || product == null) return;

		ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.setTitle("Editing: " + (product != null ? product.getName() : "Product"));
		}
		nameBox.setFocusable(true);
		nameBox.setFocusableInTouchMode(true);
		nameBox.setBackgroundColor(Color.parseColor("#FFBB33"));
		priceBox.setFocusable(true);
		priceBox.setFocusableInTouchMode(true);
		priceBox.setBackgroundColor(Color.parseColor("#FFBB33"));
		barcodeBox.setFocusable(true);
		barcodeBox.setFocusableInTouchMode(true);
		barcodeBox.setBackgroundColor(Color.parseColor("#FFBB33"));
		lowStockThresholdBox.setFocusable(true);
		lowStockThresholdBox.setFocusableInTouchMode(true);
		lowStockThresholdBox.setBackgroundColor(Color.parseColor("#FFBB33"));
		categorySpinner.setEnabled(true);
		checkboxIsPack.setEnabled(true);
		piecesPerPackBox.setFocusable(true);
		piecesPerPackBox.setFocusableInTouchMode(true);
		piecesPerPackBox.setBackgroundColor(Color.parseColor("#FFBB33"));
		piecePriceBox.setFocusable(true);
		piecePriceBox.setFocusableInTouchMode(true);
		piecePriceBox.setBackgroundColor(Color.parseColor("#FFBB33"));
		
		remember[0] = nameBox.getText().toString();
		remember[1] = String.valueOf(product.getUnitPrice());
		remember[2] = barcodeBox.getText().toString();
		remember[3] = String.valueOf(product.getLowStockThreshold());
		
		priceBox.setText(remember[1]);
		lowStockThresholdBox.setText(remember[3]);
		
		editButtonGroup.setVisibility(View.VISIBLE);
		submitEditButton.setVisibility(View.VISIBLE); // Show Save button
		openEditButton.setVisibility(View.GONE);
	}

	@Override
	public void onBackPressed() {
		if (editButtonGroup != null && editButtonGroup.getVisibility() == View.VISIBLE) {
			cancelEdit();
		} else {
			super.onBackPressed();
		}
	}
}
