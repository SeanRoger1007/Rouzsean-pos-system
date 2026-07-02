package com.refresh.pos.ui.sale;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.refresh.pos.R;
import com.refresh.pos.domain.DateTimeStrategy;
import com.refresh.pos.domain.inventory.LineItem;
import com.refresh.pos.domain.inventory.Product;
import com.refresh.pos.domain.sale.Sale;
import com.refresh.pos.domain.sale.SaleLedger;
import com.refresh.pos.techicalservices.NoDaoSetException;
import com.refresh.pos.ui.MainActivity;
import com.refresh.pos.ui.component.ButtonAdapter;
import com.refresh.pos.ui.component.UpdatableFragment;

/**
 * UI for showing sale's record.
 * @author Refresh Team
 *
 */
public class ReportFragment extends UpdatableFragment {
	
	private SaleLedger saleLedger;
	List<Map<String, String>> saleList;
	private ListView saleLedgerListView;
	private EditText searchBox;
	private TextView totalBox;
	private Spinner spinner;
	private Button previousButton;
	private Button nextButton;
	private TextView currentBox;
	private Calendar currentTime;
	private DatePickerDialog datePicker;
	
	public static final int DAILY = 0;
	public static final int WEEKLY = 1;
	public static final int MONTHLY = 2;
	public static final int YEARLY = 3;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		try {
			saleLedger = SaleLedger.getInstance();
		} catch (NoDaoSetException e) {
			e.printStackTrace();
		}
		
		View view = inflater.inflate(R.layout.layout_report, container, false);
		
		previousButton = (Button) view.findViewById(R.id.previousButton);
		nextButton = (Button) view.findViewById(R.id.nextButton);
		currentBox = (TextView) view.findViewById(R.id.currentBox);
		searchBox = (EditText) view.findViewById(R.id.searchBox);
		saleLedgerListView = (ListView) view.findViewById(R.id.saleListView);
		totalBox = (TextView) view.findViewById(R.id.totalBox);
		spinner = (Spinner) view.findViewById(R.id.spinner1);
		
		applyDarkMode(view);
		initUI();
		return view;
	}

	private void applyDarkMode(View v) {
		int theme = com.refresh.pos.domain.ThemeController.getInstance(getActivity()).getTheme();
		if (theme == com.refresh.pos.domain.ThemeController.THEME_DARK) {
			v.findViewById(R.id.report_root).setBackgroundColor(android.graphics.Color.parseColor("#121212"));
			currentBox.setTextColor(android.graphics.Color.WHITE);
			totalBox.setTextColor(android.graphics.Color.WHITE);

			if (searchBox != null) {
				searchBox.setBackgroundColor(android.graphics.Color.parseColor("#1A1A1A"));
				searchBox.setTextColor(android.graphics.Color.WHITE);
				searchBox.setHintTextColor(android.graphics.Color.parseColor("#999999"));
			}
			
			int darkGray = android.graphics.Color.parseColor("#1A1A1A");
			int slate = android.graphics.Color.parseColor("#2C3E50");
			
			int[] headerRowIds = {R.id.row_header_id, R.id.row_header_qty, R.id.row_header_product, 
								  R.id.row_header_date, R.id.row_header_total, R.id.row_header_seller, R.id.row_header_receipt};
			for (int id : headerRowIds) {
				v.findViewById(id).setBackgroundColor(darkGray);
			}
			
			v.findViewById(R.id.row_footer_label).setBackgroundColor(darkGray);
			v.findViewById(R.id.row_footer_total).setBackgroundColor(darkGray);
			
			saleLedgerListView.setBackgroundColor(android.graphics.Color.parseColor("#121212"));
			saleLedgerListView.setDivider(new android.graphics.drawable.ColorDrawable(android.graphics.Color.parseColor("#333333")));
			saleLedgerListView.setDividerHeight(1);
		}
	}

	/**
	 * Initiate this UI.
	 */
	private void initUI() {
		currentTime = Calendar.getInstance();
		datePicker = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker view, int y, int m, int d) {
				currentTime.set(Calendar.YEAR, y);
				currentTime.set(Calendar.MONTH, m);
				currentTime.set(Calendar.DAY_OF_MONTH, d);
				update();
			}
		}, currentTime.get(Calendar.YEAR), currentTime.get(Calendar.MONTH), currentTime.get(Calendar.DAY_OF_MONTH));
		
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity().getBaseContext(),
		        R.array.period, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		spinner.setAdapter(adapter);
		spinner.setSelection(0);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener(){
			
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {	
				update();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) { }
			
		});
		
		currentBox.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				datePicker.show();
			}
		});
		
		saleLedgerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String saleId = saleList.get(position).get("id");
				viewReceipt(saleId);
			}
		});
		
		previousButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				addDate(-1);
			}
		});
		
		nextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				addDate(1);
			}
		});

		searchBox.addTextChangedListener(new android.text.TextWatcher() {
			@Override public void afterTextChanged(android.text.Editable s) { update(); }
			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
		});
	}

	public void viewReceipt(String id) {
		Intent intent = new Intent(getActivity(), ReceiptPreviewActivity.class);
		intent.putExtra("saleId", Integer.parseInt(id));
		startActivity(intent);
	}
	
	/**
	 * Show list.
	 * @param list
	 */
	private void showList(List<Sale> list) {

		saleList = new ArrayList<Map<String, String>>();
		for (Sale sale : list) {
			// Reload full sale to get LineItems
			Sale fullSale = saleLedger.getSaleById(sale.getId());
			Map<String, String> map = fullSale.toMap();
			
			// Format product names and quantities
			StringBuilder productNames = new StringBuilder();
			StringBuilder quantitySummary = new StringBuilder();
			int totalPacks = 0;
			int totalPieces = 0;
			int totalStandardQty = 0;
			boolean hasPacks = false;
			boolean hasStandard = false;

			List<LineItem> items = fullSale.getAllLineItem();
			for (int i = 0; i < items.size(); i++) {
				LineItem item = items.get(i);
				Product p = item.getProduct();
				productNames.append(p.getName());
				if (i < items.size() - 1) productNames.append(", ");
				
				if (p.isPack()) {
					hasPacks = true;
					if (item.isPieceSale()) {
						totalPieces += (int) Math.round(item.getQuantity() * p.getPiecesPerPack());
					} else {
						totalPacks += (int) item.getQuantity();
					}
				} else {
					hasStandard = true;
					totalStandardQty += (int) item.getQuantity();
				}
			}

			if (hasPacks) {
				if (totalPacks > 0) quantitySummary.append(totalPacks).append(" Pk");
				if (totalPieces > 0) {
					if (quantitySummary.length() > 0) quantitySummary.append(", ");
					quantitySummary.append(totalPieces).append(" Pc");
				}
			}
			
			if (hasStandard) {
				if (quantitySummary.length() > 0) quantitySummary.append(", ");
				quantitySummary.append(totalStandardQty);
			}
			
			map.put("product_names", productNames.toString());
			map.put("total_qty", quantitySummary.length() > 0 ? quantitySummary.toString() : "0");
			map.put("seller", fullSale.getSellerName());
			
			saleList.add(map);
		}
		
		ButtonAdapter sAdap = new ButtonAdapter(getActivity() , saleList,
				R.layout.listview_report, new String[] { "id", "total_qty", "product_names", "startTime", "total", "seller"},
				new int[] { R.id.sid, R.id.qty, R.id.product_name, R.id.startTime , R.id.total, R.id.seller}, R.id.btn_reprint, "id") {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);
				int theme = com.refresh.pos.domain.ThemeController.getInstance(getActivity()).getTheme();
				if (theme == com.refresh.pos.domain.ThemeController.THEME_DARK) {
					view.setBackgroundColor(android.graphics.Color.parseColor("#1A1A1A"));
					int white = android.graphics.Color.WHITE;
					((TextView) view.findViewById(R.id.sid)).setTextColor(white);
					((TextView) view.findViewById(R.id.qty)).setTextColor(white);
					((TextView) view.findViewById(R.id.product_name)).setTextColor(white);
					((TextView) view.findViewById(R.id.startTime)).setTextColor(white);
					((TextView) view.findViewById(R.id.total)).setTextColor(white);
					((TextView) view.findViewById(R.id.seller)).setTextColor(white);
				}
				return view;
			}
		};
		saleLedgerListView.setAdapter(sAdap);
	}

	@Override
	public void update() {
		if (spinner == null || saleLedger == null) return;
		int period = spinner.getSelectedItemPosition();
		List<Sale> list = null;
		Calendar cTime = (Calendar) currentTime.clone();
		Calendar eTime = (Calendar) currentTime.clone();
		
		if(period == DAILY){
			Calendar today = Calendar.getInstance();
			Calendar yesterday = Calendar.getInstance();
			yesterday.add(Calendar.DATE, -1);
			Calendar tomorrow = Calendar.getInstance();
			tomorrow.add(Calendar.DATE, 1);

			String dateStr = DateTimeStrategy.getSQLDateFormat(currentTime);
			String label = dateStr;
			
			if (dateStr.equals(DateTimeStrategy.getSQLDateFormat(today))) label = "TODAY";
			else if (dateStr.equals(DateTimeStrategy.getSQLDateFormat(yesterday))) label = "YESTERDAY";
			else if (dateStr.equals(DateTimeStrategy.getSQLDateFormat(tomorrow))) label = "TOMORROW";

			currentBox.setText(" [" + label + " (" + dateStr + ")] ");
			currentBox.setTextSize(14);
		} else if (period == WEEKLY){
			while(cTime.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY){
				cTime.add(Calendar.DATE, -1);
			}
			
			String toShow = " [" + DateTimeStrategy.getSQLDateFormat(cTime) +  "] ~ [";
			eTime = (Calendar) cTime.clone();
			eTime.add(Calendar.DATE, 7);
			toShow += DateTimeStrategy.getSQLDateFormat(eTime) +  "] ";
			currentBox.setTextSize(16);
			currentBox.setText(toShow);
		} else if (period == MONTHLY){
			cTime.set(Calendar.DATE, 1);
			eTime = (Calendar) cTime.clone();
			eTime.add(Calendar.MONTH, 1);
			eTime.add(Calendar.DATE, -1);
			currentBox.setTextSize(18);
			currentBox.setText(" [" + currentTime.get(Calendar.YEAR) + "-" + (currentTime.get(Calendar.MONTH)+1) + "] ");
		} else if (period == YEARLY){
			cTime.set(Calendar.DATE, 1);
			cTime.set(Calendar.MONTH, 0);
			eTime = (Calendar) cTime.clone();
			eTime.add(Calendar.YEAR, 1);
			eTime.add(Calendar.DATE, -1);
			currentBox.setTextSize(20);
			currentBox.setText(" [" + currentTime.get(Calendar.YEAR) +  "] ");
		}
		currentTime = cTime;
		list = saleLedger.getAllSaleDuring(cTime, eTime);

		// Apply Search Filtering
		String query = searchBox.getText().toString().toLowerCase();
		List<Sale> filteredList = new ArrayList<>();
		
		for (Sale sale : list) {
			Sale fullSale = saleLedger.getSaleById(sale.getId());
			boolean match = false;
			
			// 1. Match ID
			if (String.valueOf(sale.getId()).contains(query)) match = true;
			
			// 2. Match Seller
			if (!match && fullSale.getSellerName().toLowerCase().contains(query)) match = true;
			
			// 3. Match Products
			if (!match) {
				for (LineItem item : fullSale.getAllLineItem()) {
					if (item.getProduct().getName().toLowerCase().contains(query)) {
						match = true;
						break;
					}
				}
			}
			
			if (match) filteredList.add(sale);
		}

		double total = 0;
		for (Sale sale : filteredList)
			total += sale.getTotal();
		
		totalBox.setText(String.format(java.util.Locale.US, "₱%.2f", total));
		showList(filteredList);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (getUserVisibleHint()) {
			((MainActivity) getActivity()).setActionBarTitle(getString(R.string.report));
		}
	}
	
	/**
	 * Add date.
	 * @param increment
	 */
	private void addDate(int increment) {
		int period = spinner.getSelectedItemPosition();
		if (period == DAILY){
			currentTime.add(Calendar.DATE, 1 * increment);
		} else if (period == WEEKLY){
			currentTime.add(Calendar.DATE, 7 * increment);
		} else if (period == MONTHLY){
			currentTime.add(Calendar.MONTH, 1 * increment);
		} else if (period == YEARLY){
			currentTime.add(Calendar.YEAR, 1 * increment);
		}
		update();
	}

}
