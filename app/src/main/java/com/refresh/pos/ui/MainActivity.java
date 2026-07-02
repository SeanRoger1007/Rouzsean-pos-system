package com.refresh.pos.ui;

import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.refresh.pos.R;
import com.refresh.pos.domain.LanguageController;
import com.refresh.pos.domain.inventory.Inventory;
import com.refresh.pos.domain.inventory.Product;
import com.refresh.pos.domain.inventory.ProductCatalog;
import com.refresh.pos.techicalservices.NoDaoSetException;
import com.refresh.pos.ui.component.UpdatableFragment;
import com.refresh.pos.ui.inventory.InventoryFragment;
import com.refresh.pos.ui.inventory.ProductDetailActivity;
import com.refresh.pos.ui.sale.ReportFragment;
import com.refresh.pos.ui.sale.SaleFragment;
import com.refresh.pos.ui.finance.FinanceFragment;

/**
 * Main activity of the application.
 */
@SuppressLint("NewApi")
public class MainActivity extends FragmentActivity {

	public static MainActivity instance;
	private ViewPager viewPager;
	private DrawerLayout drawerLayout;
	private View leftDrawer;
	private ProductCatalog productCatalog;
	private String productId;
	private Product product;
	private static boolean SDK_SUPPORTED;
	private PagerAdapter pagerAdapter;
	private Resources res;

	private void initiateActionBar() {
		if (SDK_SUPPORTED) {
			final ActionBar actionBar = getActionBar();
			if (actionBar != null) {
				actionBar.setDisplayHomeAsUpEnabled(false);
				actionBar.setHomeButtonEnabled(false);
				actionBar.setDisplayShowHomeEnabled(false); // Hide default app icon
				actionBar.setDisplayShowTitleEnabled(false);
				actionBar.setDisplayShowCustomEnabled(true);
				actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3F51B5")));

				View customView = getLayoutInflater().inflate(R.layout.layout_custom_actionbar, null);
				actionBar.setCustomView(customView);

				customView.findViewById(R.id.btn_hamburger).setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (drawerLayout.isDrawerOpen(leftDrawer)) {
							drawerLayout.closeDrawer(leftDrawer);
						} else {
							drawerLayout.openDrawer(leftDrawer);
						}
					}
				});
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		int theme = com.refresh.pos.domain.ThemeController.getInstance(this).getTheme();
		if (theme == com.refresh.pos.domain.ThemeController.THEME_DARK) {
			setTheme(android.R.style.Theme_Holo);
		} else {
			setTheme(android.R.style.Theme_Holo_Light);
		}
		res = getResources();
		setContentView(R.layout.layout_main);
		instance = this;
		viewPager = findViewById(R.id.pager);
		drawerLayout = findViewById(R.id.drawer_layout);
		leftDrawer = findViewById(R.id.left_drawer);

		DisplayMetrics metrics = getResources().getDisplayMetrics();
		android.view.ViewGroup.LayoutParams params = leftDrawer.getLayoutParams();
		params.width = (int) (metrics.widthPixels * 0.75);
		leftDrawer.setLayoutParams(params);

		super.onCreate(savedInstanceState);
		setLanguage();
		SDK_SUPPORTED = true;
		initiateActionBar();
		
		FragmentManager fragmentManager = getSupportFragmentManager();
		pagerAdapter = new PagerAdapter(fragmentManager, res);
		viewPager.setAdapter(pagerAdapter);
		viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				highlightMenuItem(position);
				setActionBarTitle(pagerAdapter.getPageTitle(position).toString());
			}
		});
		
		applyThemeToDrawer();
		initDrawerClickListeners();

		viewPager.setCurrentItem(0); // Store Overview is now homepage
		highlightMenuItem(0);
		setActionBarTitle(getString(R.string.store_overview));
		showClockInDialog();
		checkLowStockAndAlert();
	}

	private void initDrawerClickListeners() {
		findViewById(R.id.button_overview).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				viewPager.setCurrentItem(0);
				drawerLayout.closeDrawer(leftDrawer);
			}
		});

		findViewById(R.id.button_sales).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				viewPager.setCurrentItem(1);
				drawerLayout.closeDrawer(leftDrawer);
			}
		});

		findViewById(R.id.button_inventory).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				viewPager.setCurrentItem(2);
				drawerLayout.closeDrawer(leftDrawer);
			}
		});

		findViewById(R.id.button_receipt).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				viewPager.setCurrentItem(3);
				drawerLayout.closeDrawer(leftDrawer);
			}
		});

		findViewById(R.id.button_finance).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				viewPager.setCurrentItem(4);
				drawerLayout.closeDrawer(leftDrawer);
			}
		});

		findViewById(R.id.button_staff).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				viewPager.setCurrentItem(5);
				drawerLayout.closeDrawer(leftDrawer);
			}
		});

		findViewById(R.id.button_owner_use).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				viewPager.setCurrentItem(6);
				drawerLayout.closeDrawer(leftDrawer);
			}
		});

		findViewById(R.id.button_settings).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				viewPager.setCurrentItem(7);
				drawerLayout.closeDrawer(leftDrawer);
			}
		});

		findViewById(R.id.button_support).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				viewPager.setCurrentItem(8);
				drawerLayout.closeDrawer(leftDrawer);
			}
		});

		findViewById(R.id.button_loading_station).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				viewPager.setCurrentItem(9);
				drawerLayout.closeDrawer(leftDrawer);
			}
		});
	}

	private void highlightMenuItem(int position) {
		int theme = com.refresh.pos.domain.ThemeController.getInstance(this).getTheme();
		boolean isDark = (theme == com.refresh.pos.domain.ThemeController.THEME_DARK);

		int activeBg = isDark ? Color.parseColor("#33BB86FC") : Color.parseColor("#E8EAF6");
		int activeText = isDark ? Color.parseColor("#BB86FC") : Color.parseColor("#3F51B5");
		int inactiveBg = Color.TRANSPARENT;
		int inactiveText = isDark ? Color.WHITE : Color.BLACK;

		// Order: Overview (0), Sale (1), Inventory (2), Report (3), Finance (4), Staff (5), Rouzsean Use (6), Settings (7), Support (8), Loading (9)
		int[] buttonIds = {
				R.id.button_overview, R.id.button_sales, R.id.button_inventory,
				R.id.button_receipt, R.id.button_finance, R.id.button_staff,
				R.id.button_owner_use, R.id.button_settings, R.id.button_support,
				R.id.button_loading_station
		};

		for (int i = 0; i < buttonIds.length; i++) {
			Button btn = findViewById(buttonIds[i]);
			if (btn != null) {
				if (i == position) {
					btn.setBackgroundColor(activeBg);
					btn.setTextColor(activeText);
					btn.setTypeface(null, android.graphics.Typeface.BOLD);
				} else {
					btn.setBackgroundColor(inactiveBg);
					btn.setTextColor(inactiveText);
					btn.setTypeface(null, android.graphics.Typeface.NORMAL);
				}
			}
		}
	}

	private void applyThemeToDrawer() {
		int theme = com.refresh.pos.domain.ThemeController.getInstance(this).getTheme();
		boolean isDark = (theme == com.refresh.pos.domain.ThemeController.THEME_DARK);

		if (isDark) {
			leftDrawer.setBackgroundColor(Color.parseColor("#121212"));
			int textColor = Color.WHITE;
			((TextView) findViewById(R.id.drawer_owner)).setTextColor(Color.parseColor("#E91E63"));
			((TextView) findViewById(R.id.drawer_store_name)).setTextColor(Color.parseColor("#4CAF50"));
			((TextView) findViewById(R.id.drawer_pos_name)).setTextColor(Color.parseColor("#2196F3"));

			int[] buttonIds = {R.id.button_inventory, R.id.button_sales, R.id.button_loading_station, R.id.button_receipt, 
							   R.id.button_staff, R.id.button_settings, R.id.button_finance, R.id.button_overview, R.id.button_support};
			for (int id : buttonIds) {
				View btn = findViewById(id);
				if (btn != null) ((Button) btn).setTextColor(textColor);
			}
		} else {
			leftDrawer.setBackgroundColor(Color.WHITE);
			int textColor = Color.BLACK;
			int[] buttonIds = {R.id.button_inventory, R.id.button_sales, R.id.button_loading_station, R.id.button_receipt, 
							   R.id.button_staff, R.id.button_settings, R.id.button_finance, R.id.button_overview, R.id.button_support};
			for (int id : buttonIds) {
				View btn = findViewById(id);
				if (btn != null) ((Button) btn).setTextColor(textColor);
			}
		}
	}

	private void checkLowStockAndAlert() {
		try {
			List<Product> products = Inventory.getInstance().getProductCatalog().getAllProduct();
			StringBuilder lowStockItems = new StringBuilder();
			boolean hasLowStock = false;
			
			for (Product p : products) {
				int stock = Inventory.getInstance().getStock().getStockSumById(p.getId());
				if (stock < p.getLowStockThreshold()) {
					hasLowStock = true;
					lowStockItems.append("- ").append(p.getName()).append(" (").append(stock).append(" left)\n");
				}
			}

			// Check Loading Balances
			try {
				List<com.refresh.pos.domain.loading.LoadingCategory> loadingCats = com.refresh.pos.domain.loading.LoadingController.getInstance().getAllCategories();
				for (com.refresh.pos.domain.loading.LoadingCategory cat : loadingCats) {
					if (cat.getBalance() < 100.0) { // Default low threshold for loading
						hasLowStock = true;
						lowStockItems.append("- ").append(cat.getName()).append(" Balance (₱").append(String.format(Locale.US, "%.2f", cat.getBalance())).append(")\n");
					}
				}
			} catch (Exception e) {}
			
			if (hasLowStock) {
				android.os.Vibrator vibrator = (android.os.Vibrator) getSystemService(android.content.Context.VIBRATOR_SERVICE);
				if (vibrator != null) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
						vibrator.vibrate(android.os.VibrationEffect.createOneShot(500, android.os.VibrationEffect.DEFAULT_AMPLITUDE));
					} else {
						vibrator.vibrate(500);
					}
				}
				
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("⚠️ LOW STOCK ALERT");
				builder.setMessage("Please replenish the following items:\n\n" + lowStockItems);
				builder.setPositiveButton("OK", null);
				builder.show();
			}
		} catch (Exception e) {}
	}

	private void showClockInDialog() {
		final List<com.refresh.pos.domain.staff.Staff> staffList = com.refresh.pos.domain.staff.StaffController.getInstance().getAllStaff();
		if (staffList.isEmpty()) {
			return; // No staff found, don't show dialog
		}

		String lastPromptDate = getSharedPreferences("StaffPrefs", MODE_PRIVATE).getString("last_prompt_date", "");
		final String today = com.refresh.pos.domain.DateTimeStrategy.getSQLDateFormat(java.util.Calendar.getInstance());
		
		if (today.equals(lastPromptDate)) {
			return; // Already prompted today
		}

		View dialogView = getLayoutInflater().inflate(R.layout.dialog_staff_clockin, null);
		android.support.v7.widget.GridLayout grid = dialogView.findViewById(R.id.staffGrid);
		grid.setColumnCount(3);
		
		final java.util.Set<Integer> selectedStaffIds = new java.util.HashSet<>();
		
		for (final com.refresh.pos.domain.staff.Staff staff : staffList) {
			final View itemView = getLayoutInflater().inflate(R.layout.item_staff_clockin, grid, false);
			((TextView) itemView.findViewById(R.id.staffName)).setText(staff.getName());
			final View checkIcon = itemView.findViewById(R.id.checkIcon);
			
			itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (selectedStaffIds.contains(staff.getId())) {
						selectedStaffIds.remove(staff.getId());
						checkIcon.setVisibility(View.GONE);
					} else {
						selectedStaffIds.add(staff.getId());
						checkIcon.setVisibility(View.VISIBLE);
					}
				}
			});
			
			grid.addView(itemView);
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(dialogView);
		final AlertDialog dialog = builder.create();

		dialogView.findViewById(R.id.btnNoOne).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				savePromptDate();
				dialog.dismiss();
			}
		});

		dialogView.findViewById(R.id.btnConfirm).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				for (Integer id : selectedStaffIds) {
					com.refresh.pos.domain.staff.StaffController.getInstance().toggleWork(id, today, true);
				}
				savePromptDate();
				updateAllFragments();
				dialog.dismiss();
			}
		});

		dialog.show();
	}

	private void savePromptDate() {
		String today = com.refresh.pos.domain.DateTimeStrategy.getSQLDateFormat(java.util.Calendar.getInstance());
		getSharedPreferences("StaffPrefs", MODE_PRIVATE).edit().putString("last_prompt_date", today).apply();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			openQuitDialog();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void openQuitDialog() {
		AlertDialog.Builder quitDialog = new AlertDialog.Builder(this);
		quitDialog.setTitle(res.getString(R.string.dialog_quit));
		quitDialog.setPositiveButton(res.getString(R.string.quit), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
		quitDialog.setNegativeButton(res.getString(R.string.no), null);
		quitDialog.show();
	}

	public void optionOnClickHandler(View view) {
		viewPager.setCurrentItem(1); // Stay on Inventory screen (index 1)
		String id = view.getTag().toString();
		productId = id;
		try {
			productCatalog = Inventory.getInstance().getProductCatalog();
		} catch (NoDaoSetException e) {
			e.printStackTrace();
		}
		product = productCatalog.getProductById(Integer.parseInt(productId));
		openDetailDialog();
	}

	private void openDetailDialog() {
		AlertDialog.Builder quitDialog = new AlertDialog.Builder(this);
		quitDialog.setTitle(product.getName());
		quitDialog.setPositiveButton("Restock", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				openRestockDialog();
			}
		});
		quitDialog.setNegativeButton(res.getString(R.string.product_detail), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent newActivity = new Intent(MainActivity.this, ProductDetailActivity.class);
				newActivity.putExtra("id", productId);
				startActivityForResult(newActivity, 1);
			}
		});
		quitDialog.show();
	}

	private void openRestockDialog() {
		com.refresh.pos.ui.inventory.RestockDialogFragment dialog = new com.refresh.pos.ui.inventory.RestockDialogFragment(product);
		dialog.show(getSupportFragmentManager(), "RestockDialogFragment");
	}

	public ViewPager getViewPager() {
		return viewPager;
	}

	public void setActionBarTitle(String title) {
		if (SDK_SUPPORTED) {
			ActionBar actionBar = getActionBar();
			if (actionBar != null && actionBar.getCustomView() != null) {
				TextView titleText = actionBar.getCustomView().findViewById(R.id.action_bar_title);
				if (titleText != null) {
					titleText.setText(title);
				}
			}
		}
	}

	public void updateAllFragments() {
		if (pagerAdapter != null) {
			for (int p = 0; p < pagerAdapter.getCount(); p++) {
				pagerAdapter.update(p);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (drawerLayout.isDrawerOpen(leftDrawer)) {
                drawerLayout.closeDrawer(leftDrawer);
            } else {
                drawerLayout.openDrawer(leftDrawer);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			updateAllFragments();
		}
		Fragment fragment = getSupportFragmentManager().findFragmentByTag("AddProductDialogFragment");
		if (fragment != null) {
			fragment.onActivityResult(requestCode, resultCode, data);
		}
		
		if (viewPager != null && pagerAdapter != null) {
			Fragment currentFragment = pagerAdapter.getItem(viewPager.getCurrentItem());
			if (currentFragment instanceof InventoryFragment) {
				currentFragment.onActivityResult(requestCode, resultCode, data);
			}
		}
	}

	private void setLanguage() {
		String localeString = LanguageController.getInstance().getLanguage();
		Locale locale = new Locale(localeString);
		Locale.setDefault(locale);
		Configuration config = new Configuration();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			config.setLocale(locale);
		} else {
			config.locale = locale;
		}
		getBaseContext().getResources().updateConfiguration(config,
				getBaseContext().getResources().getDisplayMetrics());
	}
}

class PagerAdapter extends FragmentStatePagerAdapter {
	private final UpdatableFragment[] fragments;
	private final String[] fragmentNames;

	public PagerAdapter(FragmentManager fragmentManager, Resources res) {
		super(fragmentManager);
		UpdatableFragment homeFragment = new HomeFragment();
		UpdatableFragment saleFragment = new SaleFragment();
		UpdatableFragment inventoryFragment = new InventoryFragment(saleFragment);
		UpdatableFragment reportFragment = new ReportFragment();
		UpdatableFragment financeFragment = new FinanceFragment();
		UpdatableFragment staffFragment = new StaffFragment();
		UpdatableFragment ownerConsumptionFragment = new com.refresh.pos.ui.inventory.OwnerConsumptionFragment();
		UpdatableFragment settingsFragment = new SettingsFragment();
		UpdatableFragment supportFragment = new SupportFragment();
		UpdatableFragment loadingStationFragment = new com.refresh.pos.ui.loading.LoadingStationFragment();

		// Order: Overview (0), Sale (1), Inventory (2), Report (3), Finance (4), Staff (5), Rouzsean Use (6), Settings (7), Support (8), Loading (9)
		fragments = new UpdatableFragment[] { homeFragment, saleFragment, inventoryFragment, reportFragment, 
				financeFragment, staffFragment, ownerConsumptionFragment, settingsFragment, supportFragment, loadingStationFragment };
		fragmentNames = new String[] { res.getString(R.string.store_overview), res.getString(R.string.sale), res.getString(R.string.inventory),
				res.getString(R.string.receipt),
				res.getString(R.string.expense_income), res.getString(R.string.staff),
				res.getString(R.string.rouzsean_use), res.getString(R.string.settings), res.getString(R.string.support), "Loading" };
	}

	@Override
	public Fragment getItem(int i) { return fragments[i]; }
	@Override
	public int getCount() { return fragments.length; }
	@Override
	public CharSequence getPageTitle(int i) { return fragmentNames[i]; }

	public void update(int index) {
		UpdatableFragment fragment = fragments[index];
		if (fragment != null && fragment.isAdded()) {
			fragment.update();
		}
	}
}
