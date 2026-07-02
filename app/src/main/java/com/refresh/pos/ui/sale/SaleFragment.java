package com.refresh.pos.ui.sale;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.refresh.pos.R;
import com.refresh.pos.domain.inventory.Category;
import com.refresh.pos.domain.inventory.Inventory;
import com.refresh.pos.domain.inventory.LineItem;
import com.refresh.pos.domain.inventory.Product;
import com.refresh.pos.domain.sale.Register;
import com.refresh.pos.techicalservices.NoDaoSetException;
import com.refresh.pos.ui.component.UpdatableFragment;
import com.refresh.pos.ui.inventory.AddProductDialogFragment;

/**
 * UI for Sale process.
 * @author Refresh Team
 *
 */
public class SaleFragment extends UpdatableFragment {

	private EditText searchBox;
	private LinearLayout saleContainer;
	private Register register;
    private View hintBar;
    private TextView hintText;
    private View bottomBar;
    private Button endButton;
	private int cardWidth;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.layout_sale, container, false);
		
		try {
			register = Register.getInstance();
		} catch (NoDaoSetException e) {
			e.printStackTrace();
		}

		saleContainer = view.findViewById(R.id.sale_container);
		searchBox = view.findViewById(R.id.searchBox);
        hintBar = view.findViewById(R.id.hint_row_container);
        hintText = view.findViewById(R.id.text_checkout_hint);
        bottomBar = view.findViewById(R.id.bottom_bar);
        endButton = view.findViewById(R.id.endButton);
		
		applyDarkMode(view);
		initUI();
		update();
		return view;
	}

	private void applyDarkMode(View v) {
		int theme = com.refresh.pos.domain.ThemeController.getInstance(getActivity()).getTheme();
		if (theme == com.refresh.pos.domain.ThemeController.THEME_DARK) {
			v.findViewById(R.id.sale_root).setBackgroundColor(Color.parseColor("#121212"));
			searchBox.setBackgroundColor(Color.parseColor("#1A1A1A"));
			searchBox.setTextColor(Color.WHITE);
			searchBox.setHintTextColor(Color.parseColor("#999999"));
		}
	}

	private void initUI() {
		searchBox.addTextChangedListener(new TextWatcher() {
			@Override public void afterTextChanged(Editable s) { update(); }
			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
		});

		endButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (register.hasSale()) {
					Intent intent = new Intent(getActivity(), CheckoutActivity.class);
					startActivity(intent);
				}
			}
		});

		int screenWidth = getResources().getDisplayMetrics().widthPixels;
		int padding = (int) (20 * getResources().getDisplayMetrics().density);
		cardWidth = (screenWidth - padding * 2) / 5;
	}

	@Override
	public void update() {
		if (saleContainer == null) return;
		saleContainer.removeAllViews();
		
		int theme = com.refresh.pos.domain.ThemeController.getInstance(getActivity()).getTheme();
		boolean isDark = (theme == com.refresh.pos.domain.ThemeController.THEME_DARK);

        if (isDark) {
            hintText.setTextColor(Color.parseColor("#999999"));
        } else {
            hintText.setTextColor(Color.parseColor("#666666"));
        }

        bottomBar.setVisibility(register.hasSale() ? View.VISIBLE : View.GONE);

		String search = searchBox.getText().toString();
		List<Category> categories = null;
		try {
			categories = Inventory.getInstance().getProductCatalog().getAllCategories();
		} catch (NoDaoSetException e) {
			e.printStackTrace();
		}

		Map<Integer, Double> cartQuantities = new HashMap<>();
		if (register.hasSale()) {
			for (LineItem item : register.getCurrentSale().getAllLineItem()) {
				cartQuantities.put(item.getProduct().getId(), item.getQuantity());
			}
		}

		LayoutInflater inflater = LayoutInflater.from(getActivity());

		// 1. Group by Categories
		if (categories != null) {
			for (Category category : categories) {
				List<Product> products = null;
				try {
					products = Inventory.getInstance().getProductCatalog().searchProduct(search);
				} catch (NoDaoSetException e) {}

				List<Product> categoryProducts = new ArrayList<>();
				if (products != null) {
					for (Product p : products) {
						if (p.getCategoryId() == category.getId()) {
							categoryProducts.add(p);
						}
					}
				}

				if (!categoryProducts.isEmpty()) {
					addCategorySection(inflater, category, categoryProducts, cartQuantities, isDark);
				}
			}
		}

		// 2. No Category Products
		List<Product> allProducts = null;
		try {
			allProducts = Inventory.getInstance().getProductCatalog().searchProduct(search);
		} catch (NoDaoSetException e) {}

		List<Product> noCategoryProducts = new ArrayList<>();
		if (allProducts != null) {
			for (Product p : allProducts) {
				if (p.getCategoryId() == -1) {
					noCategoryProducts.add(p);
				}
			}
		}

		if (!noCategoryProducts.isEmpty()) {
			addCategorySection(inflater, null, noCategoryProducts, cartQuantities, isDark);
		}

		addAddProductCard(inflater, isDark);
	}

	private void addCategorySection(LayoutInflater inflater, Category category, List<Product> products, Map<Integer, Double> cartQuantities, boolean isDark) {
		View header = inflater.inflate(R.layout.layout_sale_category_header, saleContainer, false);
		TextView catName = header.findViewById(R.id.categoryName);
		View root = header.findViewById(R.id.category_header_root);

		if (category != null) {
			catName.setText(category.getName().toUpperCase());
			int color = Color.parseColor(category.getColor());
			int alphaColor = Color.argb(100, Color.red(color), Color.green(color), Color.blue(color));
			root.setBackgroundColor(alphaColor);
			catName.setTextColor(isDark ? Color.WHITE : Color.BLACK);
		} else {
			catName.setText("NO CATEGORY");
			root.setBackgroundColor(isDark ? Color.parseColor("#333333") : Color.parseColor("#888888"));
			catName.setTextColor(Color.WHITE);
		}

		saleContainer.addView(header);

		GridLayout grid = new GridLayout(getActivity());
		grid.setColumnCount(5);
		grid.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		
		for (final Product product : products) {
			View card = inflater.inflate(R.layout.listview_product_grid, grid, false);
			setupProductCard(card, product, cartQuantities, category, isDark);
			
			card.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (product.isPack()) {
						showPackPieceDialog(product, cartQuantities);
					} else {
						tryAddProduct(product, cartQuantities);
					}
				}
			});

			card.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					if (register.hasSale()) {
						List<LineItem> items = register.getCurrentSale().getAllLineItem();
						for (int i = 0; i < items.size(); i++) {
							if (items.get(i).getProduct().getId() == product.getId()) {
								showEditPopup(i);
								return true;
							}
						}
					}
					Toast.makeText(getActivity(), "Item not in cart yet", Toast.LENGTH_SHORT).show();
					return true;
				}
			});

			GridLayout.LayoutParams params = new GridLayout.LayoutParams();
			params.width = cardWidth;
			card.setLayoutParams(params);
			grid.addView(card);
		}
		saleContainer.addView(grid);
	}

	private void addAddProductCard(LayoutInflater inflater, boolean isDark) {
		View newItemCard = inflater.inflate(R.layout.listview_product_grid, null);
		TextView newItemName = (TextView) newItemCard.findViewById(R.id.name);
		newItemName.setText("ADD PRODUCT");
		newItemCard.findViewById(R.id.productImage).setVisibility(View.INVISIBLE);
		newItemCard.findViewById(R.id.new_item_icon).setVisibility(View.VISIBLE);
		newItemCard.findViewById(R.id.categoryColorBar).setVisibility(View.GONE);
		
		View newItemRoot = newItemCard.findViewById(R.id.product_item_root);
		if (isDark) {
			newItemRoot.setBackgroundResource(R.drawable.product_card_bg);
			newItemRoot.getBackground().setAlpha(30);
			newItemName.setTextColor(Color.WHITE);
		} else {
			newItemRoot.setBackgroundResource(R.drawable.product_card_bg);
			newItemName.setTextColor(Color.parseColor("#333333"));
		}
		
		newItemCard.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AddProductDialogFragment dialog = new AddProductDialogFragment(SaleFragment.this);
				dialog.show(getFragmentManager(), "AddProductDialogFragment");
			}
		});
		
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(cardWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
		lp.topMargin = 20;
		newItemCard.setLayoutParams(lp);
		saleContainer.addView(newItemCard);
	}

	private void setupProductCard(View card, Product product, Map<Integer, Double> cartQuantities, Category category, boolean isDark) {
		TextView nameText = card.findViewById(R.id.name);
		TextView priceText = card.findViewById(R.id.price);
		TextView badge = card.findViewById(R.id.quantity_badge);
		TextView categoryIcon = card.findViewById(R.id.category_icon);
		ImageView image = card.findViewById(R.id.productImage);
		View colorBar = card.findViewById(R.id.categoryColorBar);
		View itemRoot = card.findViewById(R.id.product_item_root);

		nameText.setText(product.getName());
		priceText.setText(String.format(java.util.Locale.US, "₱%.2f", product.getUnitPrice()));
		Double qty = cartQuantities.get(product.getId());
		badge.setVisibility((qty != null && qty > 0) ? View.VISIBLE : View.GONE);
		
		if (qty != null) {
			if (product.isPack()) {
				int piecesPerPack = product.getPiecesPerPack();
				int totalPieces = (int) Math.round(qty * piecesPerPack);
				int packs = totalPieces / piecesPerPack;
				int pieces = totalPieces % piecesPerPack;
				
				if (packs > 0 && pieces > 0) badge.setText(packs + "Pk," + pieces + "Pc");
				else if (packs > 0) badge.setText(packs + "Pk");
				else badge.setText(pieces + "Pc");
			} else {
				if (qty == qty.intValue()) badge.setText(String.valueOf(qty.intValue()));
				else badge.setText(String.format(java.util.Locale.US, "%.2f", qty));
			}
		}

		if (isDark) {
			itemRoot.setBackgroundResource(R.drawable.product_card_bg);
			itemRoot.getBackground().setAlpha(30);
			nameText.setTextColor(Color.WHITE);
		} else {
			itemRoot.setBackgroundResource(R.drawable.product_card_bg);
			nameText.setTextColor(Color.parseColor("#333333"));
		}

		if (category != null) {
			colorBar.setBackgroundColor(Color.parseColor(category.getColor()));
			if (category.getIcon() != null && !category.getIcon().isEmpty()) {
				categoryIcon.setText(category.getIcon());
				categoryIcon.setVisibility(View.VISIBLE);
			} else {
				categoryIcon.setVisibility(View.GONE);
			}
		} else {
			colorBar.setBackgroundColor(Color.LTGRAY);
			categoryIcon.setVisibility(View.GONE);
		}

		if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
			Bitmap bitmap = decodeSampledBitmapFromFile(product.getImagePath(), 100, 100);
			if (bitmap != null) image.setImageBitmap(bitmap);
			else image.setImageResource(R.drawable.ic_launcher);
		} else {
			image.setImageResource(R.drawable.ic_launcher);
		}

		try {
			double currentStock = Inventory.getInstance().getStock().getStockSumDoubleById(product.getId());
			Double inCart = cartQuantities.get(product.getId());
			if (inCart == null) inCart = 0.0;

			if (currentStock - inCart <= 0) {
				itemRoot.setAlpha(0.4f);
				priceText.setText("OUT OF STOCK");
				priceText.setTextColor(Color.RED);
			} else {
				itemRoot.setAlpha(1.0f);
				priceText.setTextColor(isDark ? Color.parseColor("#BB86FC") : Color.parseColor("#3F51B5"));
			}
		} catch (NoDaoSetException e) {
			e.printStackTrace();
		}
	}

	public static Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);

		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(path, options);
	}

	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			final int halfHeight = height / 2;
			final int halfWidth = width / 2;
			while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
				inSampleSize *= 2;
			}
		}
		return inSampleSize;
	}

	private void showPackPieceDialog(final Product product, final Map<Integer, Double> cartQuantities) {
		View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_pack_piece, null);
		final AlertDialog dialog = new AlertDialog.Builder(getActivity()).setView(v).create();

		TextView packPrice = v.findViewById(R.id.textWholePrice);
		TextView piecePrice = v.findViewById(R.id.textPiecePrice);
		TextView stockInfo = v.findViewById(R.id.textStockAvailable);

		packPrice.setText(String.format(java.util.Locale.US, "₱%.2f", product.getUnitPrice()));
		piecePrice.setText(String.format(java.util.Locale.US, "₱%.2f", product.getPiecePrice()));
		
		try {
			double totalStock = Inventory.getInstance().getStock().getStockSumDoubleById(product.getId());
			Double inCart = cartQuantities.get(product.getId());
			if (inCart == null) inCart = 0.0;
			double remaining = totalStock - inCart;

			String stockStr;
			if (product.isPack()) {
				int ppp = product.getPiecesPerPack();
				int totalPieces = (int) Math.round(remaining * ppp);
				int packs = totalPieces / ppp;
				int pieces = totalPieces % ppp;
				if (packs > 0 && pieces > 0) stockStr = packs + " Pk, " + pieces + " Pc";
				else if (packs > 0) stockStr = packs + " Pk";
				else stockStr = pieces + " Pc";
			} else {
				stockStr = (remaining == (int)remaining) ? String.valueOf((int)remaining) : String.format(java.util.Locale.US, "%.2f", remaining);
			}

			stockInfo.setText("Stock available: " + stockStr);
		} catch (NoDaoSetException e) {}

		v.findViewById(R.id.btnWholePack).setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) { tryAddProduct(product, cartQuantities); dialog.dismiss(); }
		});

		v.findViewById(R.id.btnIndividualPiece).setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) { tryAddProductPiece(product, cartQuantities); dialog.dismiss(); }
		});

		dialog.show();
	}

	private void tryAddProduct(Product product, Map<Integer, Double> cartQuantities) {
		try {
			double currentStock = Inventory.getInstance().getStock().getStockSumDoubleById(product.getId());
			Double inCart = cartQuantities.get(product.getId());
			if (inCart == null) inCart = 0.0;

			if (currentStock - inCart < 1.0) {
				Toast.makeText(getActivity(), "Insufficient stock", Toast.LENGTH_SHORT).show();
				return;
			}
			register.addItem(product, 1.0, false);
			update();
			((com.refresh.pos.ui.MainActivity) getActivity()).updateAllFragments();
		} catch (NoDaoSetException e) {
			e.printStackTrace();
		}
	}

	private void tryAddProductPiece(Product product, Map<Integer, Double> cartQuantities) {
		try {
			double currentStock = Inventory.getInstance().getStock().getStockSumDoubleById(product.getId());
			Double inCart = cartQuantities.get(product.getId());
			if (inCart == null) inCart = 0.0;
			
			double pieceQuantity = 1.0 / product.getPiecesPerPack();

			if (currentStock - inCart < pieceQuantity) {
				Toast.makeText(getActivity(), "Insufficient stock", Toast.LENGTH_SHORT).show();
				return;
			}
			register.addItem(product, pieceQuantity, true);
			update();
			((com.refresh.pos.ui.MainActivity) getActivity()).updateAllFragments();
		} catch (NoDaoSetException e) {
			e.printStackTrace();
		}
	}

	private void showEditPopup(final int position) {
		View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_saleedit, null);
		final LineItem item = register.getCurrentSale().getLineItemAt(position);
		
		final EditText inputQty = v.findViewById(R.id.quantityBox);
		final EditText inputPrice = v.findViewById(R.id.priceBox);
		
		double displayQty = item.getQuantity();
		if (item.getProduct().isPack() && item.isPieceSale()) {
			displayQty = Math.round(item.getQuantity() * item.getProduct().getPiecesPerPack());
		}
		
		inputQty.setText(String.valueOf((int)displayQty));
		inputPrice.setText(String.valueOf(item.getPriceAtSale()));

		final AlertDialog dialog = new AlertDialog.Builder(getActivity())
			.setTitle("Edit Item")
			.setView(v)
			.create();

		v.findViewById(R.id.confirmButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					int newQty = Integer.parseInt(inputQty.getText().toString());
					double newPrice = Double.parseDouble(inputPrice.getText().toString());
					
					double actualQty = newQty;
					if (item.getProduct().isPack() && item.isPieceSale()) {
						actualQty = (double)newQty / item.getProduct().getPiecesPerPack();
					}
					
					register.updateItem(register.getCurrentSale().getId(), item, actualQty, newPrice);
					update();
					((com.refresh.pos.ui.MainActivity) getActivity()).updateAllFragments();
					dialog.dismiss();
				} catch (Exception e) {
					Toast.makeText(getActivity(), "Invalid input", Toast.LENGTH_SHORT).show();
				}
			}
		});

		v.findViewById(R.id.removeButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				register.removeItem(item);
				update();
				((com.refresh.pos.ui.MainActivity) getActivity()).updateAllFragments();
				dialog.dismiss();
			}
		});

		dialog.show();
	}
}
