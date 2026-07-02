package com.refresh.pos.ui.inventory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ArrayAdapter;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.refresh.pos.R;
import com.refresh.pos.domain.inventory.Category;
import com.refresh.pos.domain.inventory.Inventory;
import com.refresh.pos.domain.inventory.ProductCatalog;
import com.refresh.pos.techicalservices.NoDaoSetException;
import com.refresh.pos.ui.component.UpdatableFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * A dialog of adding a Product.
 * 
 * @author Refresh Team
 *
 */
@SuppressLint("ValidFragment")
public class AddProductDialogFragment extends DialogFragment {

	private EditText barcodeBox;
	private ProductCatalog productCatalog;
	private Button scanButton;
	private Spinner categorySpinner;
	private Button addCategoryButton;
	private EditText priceBox;
	private EditText nameBox;
	private EditText initialQuantityBox;
	private EditText lowStockThresholdBox;
	private EditText costPriceBox;
	private android.widget.CheckBox checkboxIsPack;
	private View layoutPackDetails;
	private EditText piecesPerPackBox;
	private EditText piecePriceBox;
	private Button confirmButton;
	private Button clearButton;
	private ImageView productImageView;
	private Button cameraButton;
	private Button galleryButton;
	private String currentImagePath = "";
	
	private static final int REQUEST_IMAGE_CAPTURE = 101;
	private static final int REQUEST_IMAGE_PICK = 102;

	private UpdatableFragment fragment;
	private Resources res;

	/**
	 * Construct a new AddProductDialogFragment
	 * @param fragment
	 */
	public AddProductDialogFragment(UpdatableFragment fragment) {
		
		super();
		this.fragment = fragment;
	}

	private void applyDarkMode(View v) {
		int theme = com.refresh.pos.domain.ThemeController.getInstance(getActivity()).getTheme();
		if (theme == com.refresh.pos.domain.ThemeController.THEME_DARK) {
			View root = v.findViewById(R.id.layout_popupAddProduct);
			if (root != null) root.setBackgroundColor(android.graphics.Color.parseColor("#121212"));
			
			int white = android.graphics.Color.WHITE;
			int gray = android.graphics.Color.parseColor("#999999");
			int darkGray = android.graphics.Color.parseColor("#1A1A1A");
			
			// Labels
			int[] labelIds = {R.id.label_image, R.id.label_category, R.id.label_barcode, R.id.label_name, R.id.label_price, R.id.label_threshold};
			for (int id : labelIds) {
				TextView tv = v.findViewById(id);
				if (tv != null) tv.setTextColor(white);
			}
			
			// Break Pack Section
			View breakPackBox = v.findViewById(R.id.section_break_pack);
			if (breakPackBox != null) {
				breakPackBox.setBackgroundColor(darkGray);
			}
			
			android.widget.CheckBox cb = v.findViewById(R.id.checkbox_is_pack);
			if (cb != null) cb.setTextColor(white);
			
			TextView pppLabel = v.findViewById(R.id.label_pieces_per_pack);
			if (pppLabel != null) pppLabel.setTextColor(gray);
			
			TextView ppLabel = v.findViewById(R.id.label_price_per_piece);
			if (ppLabel != null) ppLabel.setTextColor(gray);
			
			// Purchase Section
			TextView pdLabel = v.findViewById(R.id.label_purchase_details);
			if (pdLabel != null) pdLabel.setTextColor(android.graphics.Color.parseColor("#E91E63"));
			
			TextView qLabel = v.findViewById(R.id.label_quantity);
			if (qLabel != null) qLabel.setTextColor(gray);
			
			TextView cpLabel = v.findViewById(R.id.label_cost_price);
			if (cpLabel != null) cpLabel.setTextColor(gray);
			
			// Input boxes
			int[] inputIds = {R.id.barcodeBox, R.id.nameBox, R.id.priceBox, R.id.lowStockThresholdBox, 
							 R.id.piecesPerPackBox, R.id.piecePriceBox, R.id.initialQuantityBox, R.id.costPriceBox};
			
			for (int inputId : inputIds) {
				EditText input = v.findViewById(inputId);
				if (input != null) {
					input.setTextColor(white);
					input.setHintTextColor(android.graphics.Color.parseColor("#444444"));
				}
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		try {
			productCatalog = Inventory.getInstance().getProductCatalog();
		} catch (NoDaoSetException e) {
			e.printStackTrace();
		}
		
		View v = inflater.inflate(R.layout.layout_addproduct, container,
				false);
		
		res = getResources();
		
		applyDarkMode(v);
		
		barcodeBox = (EditText) v.findViewById(R.id.barcodeBox);
		
		scanButton = (Button) v.findViewById(R.id.scanButton);
		categorySpinner = (Spinner) v.findViewById(R.id.categorySpinner);
		addCategoryButton = (Button) v.findViewById(R.id.addCategoryButton);
		priceBox = (EditText) v.findViewById(R.id.priceBox);
		nameBox = (EditText) v.findViewById(R.id.nameBox);
		initialQuantityBox = (EditText) v.findViewById(R.id.initialQuantityBox);
		lowStockThresholdBox = (EditText) v.findViewById(R.id.lowStockThresholdBox);
		costPriceBox = (EditText) v.findViewById(R.id.costPriceBox);
		confirmButton = (Button) v.findViewById(R.id.confirmButton);
		clearButton = (Button) v.findViewById(R.id.clearButton);
		productImageView = (ImageView) v.findViewById(R.id.productImageView);
		cameraButton = (Button) v.findViewById(R.id.cameraButton);
		galleryButton = (Button) v.findViewById(R.id.galleryButton);
		
		checkboxIsPack = v.findViewById(R.id.checkbox_is_pack);
		layoutPackDetails = v.findViewById(R.id.layout_pack_details);
		piecesPerPackBox = (EditText) v.findViewById(R.id.piecesPerPackBox);
		piecePriceBox = (EditText) v.findViewById(R.id.piecePriceBox);

		checkboxIsPack.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(android.widget.CompoundButton buttonView, boolean isChecked) {
				layoutPackDetails.setVisibility(isChecked ? View.VISIBLE : View.GONE);
			}
		});
		
		addCategoryButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showAddCategoryDialog();
			}
		});
		
		cameraButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dispatchTakePictureIntent();
			}
		});
		
		galleryButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dispatchPickImageIntent();
			}
		});

		scanButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				IntentIntegrator integrator = IntentIntegrator.forSupportFragment(AddProductDialogFragment.this);
				integrator.setPrompt("Scan a barcode");
				integrator.setBeepEnabled(true);
				integrator.setOrientationLocked(false);
				integrator.initiateScan();
			}
		});

		confirmButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String name = nameBox.getText().toString();
				String barcode = barcodeBox.getText().toString();
				String priceStr = priceBox.getText().toString();
				String qtyStr = initialQuantityBox.getText().toString();
				String thresholdStr = lowStockThresholdBox.getText().toString();
				String costStr = costPriceBox.getText().toString();

				if (name.equals("")) {
					Toast.makeText(getActivity().getBaseContext(),
							"Please enter a product name.", Toast.LENGTH_SHORT)
							.show();
				} else {
					double price = 0;
					if (!priceStr.equals("")) {
						try {
							price = Double.parseDouble(priceStr);
						} catch (Exception e) {
							price = 0;
						}
					}

					int threshold = 10;
					if (!thresholdStr.equals("")) {
						try {
							threshold = Integer.parseInt(thresholdStr);
						} catch (Exception e) {
							threshold = 10;
						}
					}

					int categoryId = -1;
					int categoryPos = categorySpinner.getSelectedItemPosition();
					if (categoryPos > 0) {
						categoryId = productCatalog.getAllCategories().get(categoryPos - 1).getId();
					}

					boolean isPack = checkboxIsPack.isChecked();
					int pieces = 1;
					double pPrice = 0.0;
					if (isPack) {
						try { pieces = Integer.parseInt(piecesPerPackBox.getText().toString()); } catch (Exception e) {}
						try { pPrice = Double.parseDouble(piecePriceBox.getText().toString()); } catch (Exception e) {}
					}

					int productId = productCatalog.addProduct(
							new com.refresh.pos.domain.inventory.Product(
									com.refresh.pos.domain.inventory.Product.UNDEFINED_ID, 
									name, barcode, price, currentImagePath, threshold, categoryId, isPack, pieces, pPrice));

					if (productId != -1) {
						// Add initial stock if provided
						if (!qtyStr.equals("") && !costStr.equals("")) {
							try {
								int qty = Integer.parseInt(qtyStr);
								double cost = Double.parseDouble(costStr);
								if (qty > 0) {
									com.refresh.pos.domain.inventory.Product newProduct = productCatalog.getProductById(productId);
									if (newProduct != null) {
										Inventory.getInstance().getStock().addProductLot(
											com.refresh.pos.domain.DateTimeStrategy.getCurrentTime(),
											qty, newProduct, cost);

										// Log as financial expense
										logInventoryExpense(name, qty, cost);
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}

						Toast.makeText(getActivity().getBaseContext(),
								res.getString(R.string.success) + ", " + name,
								Toast.LENGTH_SHORT).show();
						
						if (getActivity() instanceof com.refresh.pos.ui.MainActivity) {
							((com.refresh.pos.ui.MainActivity) getActivity()).updateAllFragments();
						} else {
							fragment.update();
						}
						clearAllBox();
						AddProductDialogFragment.this.dismiss();
						
					} else {
						Toast.makeText(getActivity().getBaseContext(),
								res.getString(R.string.fail),
								Toast.LENGTH_SHORT).show();
					}
				}
			}
		});
		
		clearButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		
		updateCategories();
		return v;
	}

	public void updateCategories() {
		List<Category> categories = productCatalog.getAllCategories();
		List<String> names = new ArrayList<String>();
		names.add("No Category");
		for (Category c : categories) {
			names.add(c.getName());
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, names);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		categorySpinner.setAdapter(adapter);
	}

	private void showAddCategoryDialog() {
		final EditText input = new EditText(getActivity());
		input.setHint("Category Name");
		new AlertDialog.Builder(getActivity())
			.setTitle("Add Category")
			.setView(input)
			.setPositiveButton("Add", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String name = input.getText().toString();
					if (!name.isEmpty()) {
						productCatalog.addCategory(name);
						updateCategories();
					}
				}
			})
			.setNegativeButton("Cancel", null)
			.show();
	}

	private void logInventoryExpense(String productName, int qty, double cost) {
		com.refresh.pos.domain.finance.FinanceController finance = com.refresh.pos.domain.finance.FinanceController.getInstance();
		int invCatId = finance.findOrCreateCategory("Inventory", "EXPENSE");

		if (invCatId != -1) {
			finance.addTransaction(
					invCatId,
					qty * cost,
					com.refresh.pos.domain.DateTimeStrategy.getSQLDateFormat(java.util.Calendar.getInstance()),
					"CASH",
					"Initial stock for " + productName + " (" + qty + " x " + cost + ")",
					"EXPENSE"
			);
		}
	}

	private void clearAllBox() {
		barcodeBox.setText("");
		priceBox.setText("");
		nameBox.setText("");
		initialQuantityBox.setText("");
		lowStockThresholdBox.setText("");
		costPriceBox.setText("");
		currentImagePath = "";
		productImageView.setImageResource(R.drawable.ic_launcher);
		checkboxIsPack.setChecked(false);
		piecesPerPackBox.setText("");
		piecePriceBox.setText("");
	}

	private void dispatchTakePictureIntent() {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
			startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
		}
	}

	private void dispatchPickImageIntent() {
		Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(pickPhoto , REQUEST_IMAGE_PICK);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
		if (scanningResult != null && scanningResult.getContents() != null) {
			barcodeBox.setText(scanningResult.getContents());
		} else if (resultCode == Activity.RESULT_OK) {
			if (requestCode == REQUEST_IMAGE_CAPTURE) {
				Bundle extras = data.getExtras();
				Bitmap imageBitmap = (Bitmap) extras.get("data");
				saveImage(imageBitmap);
			} else if (requestCode == REQUEST_IMAGE_PICK) {
				Uri selectedImage = data.getData();
				try {
					InputStream imageStream = getActivity().getContentResolver().openInputStream(selectedImage);
					Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
					saveImage(bitmap);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void saveImage(Bitmap bitmap) {
		try {
			File folder = new File(getActivity().getExternalFilesDir(null), "products");
			if (!folder.exists()) folder.mkdirs();
			
			String fileName = "prod_" + System.currentTimeMillis() + ".jpg";
			File file = new File(folder, fileName);
			FileOutputStream out = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
			out.flush();
			out.close();
			
			currentImagePath = file.getAbsolutePath();
			productImageView.setImageBitmap(bitmap);
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(getActivity(), "Failed to save image", Toast.LENGTH_SHORT).show();
		}
	}
}
