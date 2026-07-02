package com.refresh.pos.ui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.refresh.pos.R;
import com.refresh.pos.domain.inventory.Inventory;
import com.refresh.pos.domain.inventory.Product;
import com.refresh.pos.domain.sale.Sale;
import com.refresh.pos.domain.sale.SaleLedger;
import com.refresh.pos.domain.staff.StaffController;
import com.refresh.pos.domain.capital.CapitalController;
import com.refresh.pos.techicalservices.NoDaoSetException;
import com.refresh.pos.ui.component.UpdatableFragment;
import com.refresh.pos.ui.sale.ExpenseReportActivity;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends UpdatableFragment {

    private TextView cashProfitText;
    private TextView gcashProfitText;
    private TextView profitsText;
    private TextView totalPriceText;
    private TextView expensesText;
    private TextView dateDisplayText;
    private TextView storeCapitalText;
    private TextView gcashBalanceText;
    private TextView staffTodayText;
    private TextView stockValueText;
    private TextView clockText;
    private TextView chartDetailText;
    private View chartDot;
    private LinearLayout chartContainer;

    private Handler clockHandler = new Handler();
    private Runnable clockRunnable = new Runnable() {
        @Override
        public void run() {
            if (clockText != null) {
                Calendar cal = Calendar.getInstance();
                String time = new java.text.SimpleDateFormat("hh:mm:ss a", Locale.US).format(cal.getTime());
                clockText.setText(time);
            }
            clockHandler.postDelayed(this, 1000);
        }
    };
    private View cardStoreCapital;
    private View cardGcashBalance;
    private View cardSales;
    private View cardExpenses;
    private View cardStaffToday;
    private View cardStockValue;
    private View chartWrapper;
    private View homeTitle;
    private View periodSwitcher;
    private View dateNavBar;
    private View cards_container;
    private View home_main_container;
    private Button btnDaily, btnWeekly, btnMonthly;
    private Button btnPrev, btnNext;

    private SaleLedger saleLedger;
    private Inventory inventory;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private enum Period { DAILY, WEEKLY, MONTHLY }
    private Period currentPeriod = Period.DAILY;
    private Calendar selectedDate = Calendar.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_home, container, false);
        
        cashProfitText = view.findViewById(R.id.text_cash_profit_value);
        gcashProfitText = view.findViewById(R.id.text_gcash_profit_value);
        profitsText = view.findViewById(R.id.text_total_income_value);
        totalPriceText = view.findViewById(R.id.text_total_price);
        expensesText = view.findViewById(R.id.text_expenses_value);
        dateDisplayText = view.findViewById(R.id.text_date_display);
        stockValueText = view.findViewById(R.id.text_stock_value);

        storeCapitalText = view.findViewById(R.id.text_store_capital_value);
        gcashBalanceText = view.findViewById(R.id.text_gcash_balance_value);
        staffTodayText = view.findViewById(R.id.text_staff_today_value);
        clockText = view.findViewById(R.id.clock_text);
        chartDetailText = view.findViewById(R.id.chart_detail_text);
        chartDot = view.findViewById(R.id.chart_dot);
        chartContainer = view.findViewById(R.id.chart_container);
        chartWrapper = view.findViewById(R.id.chart_wrapper);
        homeTitle = view.findViewById(R.id.home_title);
        periodSwitcher = view.findViewById(R.id.period_switcher);
        dateNavBar = view.findViewById(R.id.date_nav_bar);
        cards_container = view.findViewById(R.id.cards_container);
        home_main_container = view.findViewById(R.id.home_main_container);
        
        clockHandler.post(clockRunnable);
        cardStoreCapital = view.findViewById(R.id.card_store_capital);
        cardGcashBalance = view.findViewById(R.id.card_gcash_balance);
        cardSales = view.findViewById(R.id.card_sales);
        cardExpenses = view.findViewById(R.id.card_expenses);
        cardStaffToday = view.findViewById(R.id.card_staff_today);
        cardStockValue = view.findViewById(R.id.card_stock_value);
        
        btnDaily = view.findViewById(R.id.btn_daily);
        btnWeekly = view.findViewById(R.id.btn_weekly);
        btnMonthly = view.findViewById(R.id.btn_monthly);
        
        btnPrev = view.findViewById(R.id.btn_prev);
        btnNext = view.findViewById(R.id.btn_next);

        try {
            saleLedger = SaleLedger.getInstance();
            inventory = Inventory.getInstance();
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }

        initUI();
        applyDarkModeStyles();
        update();
        return view;
    }

    private void initUI() {
        cardStoreCapital.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditCapitalDialog("store_cash");
            }
        });

        cardGcashBalance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditCapitalDialog("gcash_balance");
            }
        });

        cardSales.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar start = (Calendar) selectedDate.clone();
                Calendar end = (Calendar) selectedDate.clone();
                prepareRange(start, end);
                
                Intent intent = new Intent(getActivity(), com.refresh.pos.ui.sale.IncomeReportActivity.class);
                intent.putExtra("start", start.getTimeInMillis());
                intent.putExtra("end", end.getTimeInMillis());
                startActivity(intent);
            }
        });

        cardExpenses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar start = (Calendar) selectedDate.clone();
                Calendar end = (Calendar) selectedDate.clone();
                prepareRange(start, end);
                
                Intent intent = new Intent(getActivity(), com.refresh.pos.ui.sale.ExpenseReportActivity.class);
                intent.putExtra("start", start.getTimeInMillis());
                intent.putExtra("end", end.getTimeInMillis());
                startActivity(intent);
            }
        });

        btnDaily.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentPeriod = Period.DAILY;
                updatePeriodButtons();
                update();
            }
        });

        btnWeekly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentPeriod = Period.WEEKLY;
                updatePeriodButtons();
                update();
            }
        });

        btnMonthly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentPeriod = Period.MONTHLY;
                updatePeriodButtons();
                update();
            }
        });

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDate(-1);
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDate(1);
            }
        });

        dateDisplayText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });
        
        updatePeriodButtons();
    }

    private void showDatePicker() {
        new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                selectedDate.set(year, month, dayOfMonth);
                update();
            }
        }, selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void addDate(int amount) {
        if (currentPeriod == Period.DAILY) selectedDate.add(Calendar.DATE, amount);
        else if (currentPeriod == Period.WEEKLY) selectedDate.add(Calendar.WEEK_OF_YEAR, amount);
        else if (currentPeriod == Period.MONTHLY) selectedDate.add(Calendar.MONTH, amount);
        update();
    }

    private void updatePeriodButtons() {
        btnDaily.setBackgroundColor(currentPeriod == Period.DAILY ? Color.parseColor("#333333") : Color.parseColor("#1A1A1A"));
        btnDaily.setTextColor(currentPeriod == Period.DAILY ? Color.WHITE : Color.parseColor("#999999"));
        
        btnWeekly.setBackgroundColor(currentPeriod == Period.WEEKLY ? Color.parseColor("#333333") : Color.parseColor("#1A1A1A"));
        btnWeekly.setTextColor(currentPeriod == Period.WEEKLY ? Color.WHITE : Color.parseColor("#999999"));

        btnMonthly.setBackgroundColor(currentPeriod == Period.MONTHLY ? Color.parseColor("#333333") : Color.parseColor("#1A1A1A"));
        btnMonthly.setTextColor(currentPeriod == Period.MONTHLY ? Color.WHITE : Color.parseColor("#999999"));
    }

    private void showEditCapitalDialog(final String key) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(key.equals("store_cash") ? "Edit Cash Register" : "Edit GCash Balance");
        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setText(String.valueOf(key.equals("store_cash") ? CapitalController.getInstance().getStoreCash() : CapitalController.getInstance().getGcashBalance()));
        builder.setView(input);
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    double val = Double.parseDouble(input.getText().toString());
                    if (key.equals("store_cash")) CapitalController.getInstance().setStoreCash(val);
                    else CapitalController.getInstance().setGcashBalance(val);
                    update();
                } catch (Exception e) {}
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.store_overview));
        }
        update();
        if (getUserVisibleHint()) {
            runEntranceAnimations();
        } else {
            // Ensure visible if not animating
            ensureViewsVisible();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (isResumed()) {
                runEntranceAnimations();
            }
        } else {
            // Optional: reset translations when hidden
        }
    }

    private void ensureViewsVisible() {
        if (homeTitle == null) return;
        homeTitle.setAlpha(1);
        clockText.setAlpha(1);
        chartWrapper.setAlpha(1);
        chartDetailText.setAlpha(1);
        periodSwitcher.setAlpha(1);
        dateNavBar.setAlpha(1);
        cardSales.setTranslationX(0);
        cardExpenses.setTranslationX(0);
        cardStockValue.setTranslationX(0);
        cardStoreCapital.setTranslationX(0);
        cardGcashBalance.setTranslationX(0);
        cardStaffToday.setTranslationX(0);
    }

    private void runEntranceAnimations() {
        if (homeTitle == null || cards_container == null) return;

        // Ensure container is visible
        if (home_main_container != null) home_main_container.setVisibility(View.VISIBLE);

        // Reset states immediately before animating
        float offScreenLeft = -1500f;
        float offScreenRight = 1500f;
        
        homeTitle.setAlpha(0);
        clockText.setAlpha(0);
        chartWrapper.setAlpha(0);
        chartDetailText.setAlpha(0);
        periodSwitcher.setAlpha(0);
        dateNavBar.setAlpha(0);

        cardSales.setTranslationX(offScreenLeft);
        cardExpenses.setTranslationX(offScreenRight);
        cardStockValue.setTranslationX(offScreenLeft);
        cardStoreCapital.setTranslationX(offScreenRight);
        cardGcashBalance.setTranslationX(offScreenLeft);
        cardStaffToday.setTranslationX(offScreenRight);

        // Animations
        long durationFade = 1200;
        long durationSlide = 900;
        long stagger = 200;

        // 1. Fade Group
        homeTitle.animate().alpha(1).setDuration(durationFade).setInterpolator(new android.view.animation.DecelerateInterpolator()).start();
        clockText.animate().alpha(1).setDuration(durationFade).setStartDelay(150).start();
        chartWrapper.animate().alpha(1).setDuration(durationFade).setStartDelay(300).start();
        chartDetailText.animate().alpha(1).setDuration(durationFade).setStartDelay(450).start();
        periodSwitcher.animate().alpha(1).setDuration(durationFade).setStartDelay(600).start();
        dateNavBar.animate().alpha(1).setDuration(durationFade).setStartDelay(750).start();

        // 2. Alternating Slide Group
        cardSales.animate().translationX(0).setDuration(durationSlide).setStartDelay(900).setInterpolator(new android.view.animation.OvershootInterpolator(0.8f)).start();
        cardExpenses.animate().translationX(0).setDuration(durationSlide).setStartDelay(900 + stagger).setInterpolator(new android.view.animation.OvershootInterpolator(0.8f)).start();
        cardStockValue.animate().translationX(0).setDuration(durationSlide).setStartDelay(900 + stagger * 2).setInterpolator(new android.view.animation.OvershootInterpolator(0.8f)).start();
        cardStoreCapital.animate().translationX(0).setDuration(durationSlide).setStartDelay(900 + stagger * 3).setInterpolator(new android.view.animation.OvershootInterpolator(0.8f)).start();
        cardGcashBalance.animate().translationX(0).setDuration(durationSlide).setStartDelay(900 + stagger * 4).setInterpolator(new android.view.animation.OvershootInterpolator(0.8f)).start();
        cardStaffToday.animate().translationX(0).setDuration(durationSlide).setStartDelay(900 + stagger * 5).setInterpolator(new android.view.animation.OvershootInterpolator(0.8f)).start();
    }

    private void applyAnimations() {
        // Obsolete
    }

    private void applyDarkModeStyles() {
        int theme = com.refresh.pos.domain.ThemeController.getInstance(getActivity()).getTheme();
        if (theme == com.refresh.pos.domain.ThemeController.THEME_DARK) {
            // Interactive cards (View/Edit) get a highlight
            int activeHighlight = Color.parseColor("#1FBB86FC"); // ~12% Alpha purple
            int secondaryHighlight = Color.parseColor("#1F03A9F4"); // ~12% Alpha blue for balance
            
            cardSales.setBackgroundColor(activeHighlight);
            cardExpenses.setBackgroundColor(Color.parseColor("#1FFF5252")); // ~12% Alpha red
            cardStoreCapital.setBackgroundColor(Color.parseColor("#1F4CAF50")); // ~12% Alpha green
            cardGcashBalance.setBackgroundColor(secondaryHighlight);
            
            // Non-interactive or static info cards
            cardStockValue.setBackgroundColor(Color.parseColor("#1A1A1A"));
            cardStaffToday.setBackgroundColor(Color.parseColor("#1A1A1A"));
        }
    }

    @Override
    public void update() {
        if (cashProfitText == null || saleLedger == null || inventory == null) return;

        final Calendar start = (Calendar) selectedDate.clone();
        final Calendar end = (Calendar) selectedDate.clone();
        prepareRange(start, end);

        String dateLabel = "";
        if (currentPeriod == Period.DAILY) {
            String sqlDate = com.refresh.pos.domain.DateTimeStrategy.getSQLDateFormat(selectedDate);
            
            Calendar today = Calendar.getInstance();
            String todayStr = com.refresh.pos.domain.DateTimeStrategy.getSQLDateFormat(today);
            
            Calendar yesterday = Calendar.getInstance();
            yesterday.add(Calendar.DATE, -1);
            String yesterdayStr = com.refresh.pos.domain.DateTimeStrategy.getSQLDateFormat(yesterday);
            
            Calendar tomorrow = Calendar.getInstance();
            tomorrow.add(Calendar.DATE, 1);
            String tomorrowStr = com.refresh.pos.domain.DateTimeStrategy.getSQLDateFormat(tomorrow);

            if (sqlDate.equals(todayStr)) dateLabel = "TODAY (" + sqlDate + ")";
            else if (sqlDate.equals(yesterdayStr)) dateLabel = "YESTERDAY (" + sqlDate + ")";
            else if (sqlDate.equals(tomorrowStr)) dateLabel = "TOMORROW (" + sqlDate + ")";
            else dateLabel = sqlDate;

        } else if (currentPeriod == Period.WEEKLY) {
            dateLabel = com.refresh.pos.domain.DateTimeStrategy.getSQLDateFormat(start) + " ~ " + com.refresh.pos.domain.DateTimeStrategy.getSQLDateFormat(end);

        } else if (currentPeriod == Period.MONTHLY) {
            dateLabel = start.get(Calendar.YEAR) + "-" + (start.get(Calendar.MONTH) + 1);
        }

        dateDisplayText.setText(dateLabel);
        
        // Use background thread for heavy calculations
        executor.execute(new Runnable() {
            @Override
            public void run() {
                calculateAndShowData(start, end);
            }
        });
    }

    private void calculateAndShowData(final Calendar start, final Calendar end) {
        // Calculate Sales and Profits from Optimized Summary
        final com.refresh.pos.domain.sale.SaleSummary summary = saleLedger.getSaleSummaryDuring(start, end);
        
        // We still need gcash/cash revenue breakdown, unfortunately this still requires a loop 
        // OR we can add gcash/cash summary in the DAO. Let's stick to DAO summary for main values.
        
        final List<Sale> quickSales = saleLedger.getAllSaleDuring(start, end);
        double totalRevenue = summary.getTotalRevenue();
        double totalCogs = summary.getTotalCost();
        double cashRevenue = 0;
        double gcashRevenue = 0;

        for (Sale s : quickSales) {
            if ("GCASH".equals(s.getPaymentMethod())) gcashRevenue += s.getTotal();
            else cashRevenue += s.getTotal();
        }

        // Calculate Expenses (Restocking costs)
        List<com.refresh.pos.domain.inventory.ProductLot> lots = inventory.getStock().getAllProductLotDuring(start, end);
        double restockingExpenses = 0;
        for (com.refresh.pos.domain.inventory.ProductLot lot : lots) {
            restockingExpenses += lot.unitCost() * lot.getQuantity();
        }

        // Add Staff Salary to Expenses
        String startStr = com.refresh.pos.domain.DateTimeStrategy.getSQLDateFormat(start);
        String endStr = com.refresh.pos.domain.DateTimeStrategy.getSQLDateFormat(end);
        double staffExpenses = StaffController.getInstance().getTotalSalaryBetween(startStr, endStr);
        double totalExpenses = restockingExpenses + staffExpenses;

        // Add Other Expenses (Utilities, Online Shopping, etc) - Excluding OWNER_USE
        double otherExpenses = 0;
        List<com.refresh.pos.domain.finance.FinanceTransaction> transactions = com.refresh.pos.domain.finance.FinanceController.getInstance().getTransactionsBetween("EXPENSE", startStr, endStr);
        for (com.refresh.pos.domain.finance.FinanceTransaction t : transactions) {
            if (!"OWNER_USE".equalsIgnoreCase(t.getCategoryName())) {
                otherExpenses += t.getAmount();
            }
        }
        totalExpenses += otherExpenses;

        // Calculate Loading Revenue and Profits
        double loadingRevenue = 0;
        double loadingMarkup = 0;
        try {
            List<com.refresh.pos.domain.loading.LoadingTransaction> loadingSales = com.refresh.pos.domain.loading.LoadingController.getInstance().getLoadingProfits(startStr, endStr);
            for (com.refresh.pos.domain.loading.LoadingTransaction t : loadingSales) {
                loadingRevenue += t.getAmountSold();
                loadingMarkup += (t.getAmountSold() - t.getAmountDeducted());

                if ("GCASH".equals(t.getPaymentMethod())) {
                    gcashRevenue += t.getAmountSold();
                } else {
                    cashRevenue += t.getAmountSold();
                }
            }
        } catch (NoDaoSetException e) {}

        // Total Profits = markup from sales + loading markup
        final double totalMarkupProfit = (totalRevenue - totalCogs) + loadingMarkup;
        final double totalIncome = totalRevenue + loadingRevenue;
        final double finalTotalExpenses = totalExpenses;
        final double finalCashRevenue = cashRevenue;
        final double finalGcashRevenue = gcashRevenue;

        // Optimized chart data loading
        final List<Sale> fullSalesForChart = new ArrayList<>();
        for (Sale s : quickSales) {
            Sale full = saleLedger.getSaleById(s.getId());
            if (full != null) fullSalesForChart.add(full);
        }

        // Calculate Total Stock Value (Current stock * Retail price)
        double totalStockValue = 0;
        List<Product> allProducts = inventory.getProductCatalog().getAllProduct();
        for (Product p : allProducts) {
            int currentStock = inventory.getStock().getStockSumById(p.getId());
            totalStockValue += (p.getUnitPrice() * currentStock);
        }
        final double finalStockValue = totalStockValue;

        if (getActivity() == null) return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (cashProfitText == null) return;
                
                expensesText.setText(String.format(Locale.US, "₱%.2f", finalTotalExpenses));
                cashProfitText.setText(String.format(Locale.US, "Cash: ₱%.2f", finalCashRevenue));
                gcashProfitText.setText(String.format(Locale.US, "GCash: ₱%.2f", finalGcashRevenue));
                profitsText.setText(String.format(Locale.US, "₱%.2f", totalMarkupProfit));
                totalPriceText.setText(String.format(Locale.US, "₱%.2f", totalIncome));
                storeCapitalText.setText(String.format(Locale.US, "₱%.2f", CapitalController.getInstance().getStoreCash()));
                gcashBalanceText.setText(String.format(Locale.US, "₱%.2f", CapitalController.getInstance().getGcashBalance()));
                stockValueText.setText(String.format(Locale.US, "₱%.2f", finalStockValue));
                
                updateChart(fullSalesForChart);
            }
        });
    }

    private void updateChart(final List<Sale> sales) {
        chartContainer.removeAllViews();
        chartDot.setVisibility(View.GONE);
        if (sales.isEmpty()) {
            chartDetailText.setText("No sales data for this period.");
            chartDetailText.setAlpha(0.5f);
            return;
        }

        XYSeries series = new XYSeries("Sales");
        for (int i = 0; i < sales.size(); i++) {
            series.add(i + 1, sales.get(i).getTotal());
        }

        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        dataset.addSeries(series);

        XYSeriesRenderer renderer = new XYSeriesRenderer();
        renderer.setColor(Color.parseColor("#FFD700")); // Brighter gold
        renderer.setPointStyle(org.achartengine.chart.PointStyle.CIRCLE);
        renderer.setFillPoints(true);
        renderer.setLineWidth(8); // Thicker line
        renderer.setDisplayChartValues(false);
        
        // Brighter area fill
        XYSeriesRenderer.FillOutsideLine fill = new XYSeriesRenderer.FillOutsideLine(XYSeriesRenderer.FillOutsideLine.Type.BOUNDS_ALL);
        fill.setColor(Color.parseColor("#33FFD700")); // ~20% alpha gold
        renderer.addFillOutsideLine(fill);

        float density = getActivity().getResources().getDisplayMetrics().density;
        final int marginT = (int) (15 * density);
        final int marginL = (int) (55 * density); 
        final int marginB = (int) (35 * density); 
        final int marginR = (int) (15 * density);

        XYMultipleSeriesRenderer multiRenderer = new XYMultipleSeriesRenderer();
        multiRenderer.addSeriesRenderer(renderer);
        multiRenderer.setApplyBackgroundColor(true);
        multiRenderer.setBackgroundColor(Color.parseColor("#1A1A1A"));
        multiRenderer.setMarginsColor(Color.parseColor("#1A1A1A"));
        multiRenderer.setPanEnabled(false, false);
        multiRenderer.setInScroll(false);
        multiRenderer.setShowGrid(true);
        multiRenderer.setGridColor(Color.parseColor("#33FFFFFF"));
        multiRenderer.setLabelsTextSize(18 * density / 2);
        multiRenderer.setXLabels(0);
        multiRenderer.setYLabels(5);
        multiRenderer.setShowAxes(true);
        multiRenderer.setLabelsColor(Color.LTGRAY);
        multiRenderer.setXLabelsColor(Color.LTGRAY);
        multiRenderer.setYLabelsColor(0, Color.LTGRAY);
        
        multiRenderer.setShowLegend(false);
        multiRenderer.setXTitle("Transactions");
        multiRenderer.setYTitle("Revenue (₱)");
        multiRenderer.setAxisTitleTextSize(20 * density / 2);
        multiRenderer.setMargins(new int[]{marginT, marginL, marginB, marginR});

        multiRenderer.setClickEnabled(true);
        multiRenderer.setXAxisMin(0.5);
        multiRenderer.setXAxisMax(sales.size() + 0.5);
        
        if (sales.size() <= 10) {
            for (int i = 1; i <= sales.size(); i++) {
                multiRenderer.addXTextLabel(i, String.valueOf(i));
            }
        } else {
            multiRenderer.addXTextLabel(1, "1");
            multiRenderer.addXTextLabel(sales.size(), String.valueOf(sales.size()));
        }

        double maxSales = 0;
        for (Sale s : sales) if (s.getTotal() > maxSales) maxSales = s.getTotal();
        multiRenderer.setYAxisMin(0);
        multiRenderer.setYAxisMax(maxSales * 1.2); // More headroom

        final GraphicalView mChartView = ChartFactory.getLineChartView(getActivity(), dataset, multiRenderer);
        mChartView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        
        chartDetailText.setText(getString(R.string.graph_hint));
        chartDetailText.setAlpha(0.7f);
        
        mChartView.setOnTouchListener(new View.OnTouchListener() {
            private java.lang.reflect.Field mChartField;
            private java.lang.reflect.Method toRealMethod;
            private java.lang.reflect.Method toScreenMethod;
            private Object chartObj;

            private void initReflection() {
                if (chartObj != null) return;
                try {
                    mChartField = mChartView.getClass().getDeclaredField("mChart");
                    mChartField.setAccessible(true);
                    chartObj = mChartField.get(mChartView);
                    toRealMethod = chartObj.getClass().getMethod("toRealPoint", float.class, float.class, int.class);
                    toScreenMethod = chartObj.getClass().getMethod("toScreenPoint", double[].class, int.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    initReflection();
                    
                    float touchX = event.getX();
                    float touchY = event.getY();
                    
                    if (chartObj != null) {
                        try {
                            double[] realPoint = (double[]) toRealMethod.invoke(chartObj, touchX, touchY, 0);
                            if (realPoint == null) return true;
                            double dataX = realPoint[0];

                            if (dataX < 1) dataX = 1;
                            if (dataX > sales.size()) dataX = sales.size();

                            int lowerIdx = (int) Math.floor(dataX) - 1;
                            int upperIdx = (int) Math.ceil(dataX) - 1;
                            
                            double interpolatedY;
                            Sale nearestSale;
                            
                            if (lowerIdx == upperIdx) {
                                interpolatedY = sales.get(lowerIdx).getTotal();
                                nearestSale = sales.get(lowerIdx);
                            } else {
                                double y1 = sales.get(lowerIdx).getTotal();
                                double y2 = sales.get(upperIdx).getTotal();
                                double fraction = dataX - (lowerIdx + 1);
                                interpolatedY = y1 + fraction * (y2 - y1);
                                nearestSale = (fraction < 0.5) ? sales.get(lowerIdx) : sales.get(upperIdx);
                            }

                            // ALIGNMENT FIX: Use absolute screen coordinates to calculate precise offset
                            Object screenPointObj = toScreenMethod.invoke(chartObj, new double[]{dataX, interpolatedY}, 0);
                            
                            if (screenPointObj != null) {
                                chartDot.setVisibility(View.VISIBLE);
                                float dotW = chartDot.getWidth() > 0 ? chartDot.getWidth() : 12 * v.getContext().getResources().getDisplayMetrics().density;
                                float dotH = chartDot.getHeight() > 0 ? chartDot.getHeight() : 12 * v.getContext().getResources().getDisplayMetrics().density;
                                
                                float screenX = 0, screenY = 0;
                                if (screenPointObj instanceof float[]) {
                                    float[] sp = (float[]) screenPointObj;
                                    screenX = sp[0];
                                    screenY = sp[1];
                                } else if (screenPointObj instanceof double[]) {
                                    double[] sp = (double[]) screenPointObj;
                                    screenX = (float) sp[0];
                                    screenY = (float) sp[1];
                                }

                                int[] chartLocation = new int[2];
                                v.getLocationInWindow(chartLocation);
                                
                                int[] wrapperLocation = new int[2];
                                chartWrapper.getLocationInWindow(wrapperLocation);
                                
                                float offsetX = chartLocation[0] - wrapperLocation[0];
                                float offsetY = chartLocation[1] - wrapperLocation[1];

                                chartDot.setX(offsetX + screenX - dotW / 2.0f);
                                chartDot.setY(offsetY + screenY - dotH / 2.0f);
                            }

                            List<com.refresh.pos.domain.inventory.LineItem> items = nearestSale.getAllLineItem();
                            StringBuilder detailBuilder = new StringBuilder();
                            for (int i = 0; i < items.size(); i++) {
                                com.refresh.pos.domain.inventory.LineItem item = items.get(i);
                                String qtyStr = (item.getQuantity() == (int)item.getQuantity()) 
                                        ? String.valueOf((int)item.getQuantity()) 
                                        : String.format(Locale.US, "%.2f", item.getQuantity());
                                
                                detailBuilder.append(item.getProduct().getName())
                                        .append(" (").append(qtyStr).append("x) @ ₱")
                                        .append(String.format(Locale.US, "%.2f", item.getTotalPriceAtSale()));
                                
                                if (i < items.size() - 1) detailBuilder.append(" | ");
                            }
                            
                            String method = nearestSale.getPaymentMethod();
                            if (method == null) method = "CASH";
                            String timeAmPm = com.refresh.pos.domain.DateTimeStrategy.formatToAmPm(nearestSale.getStartTime());
                            
                            String info = detailBuilder.toString() + "\n" + method + " • " + timeAmPm;
                            chartDetailText.setText(info);
                            chartDetailText.setTextColor(Color.WHITE);
                            chartDetailText.setLineSpacing(0, 1.2f);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    
                    if (action == MotionEvent.ACTION_DOWN) {
                        v.performClick();
                    }
                    return true;
                } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                }
                return false;
            }
        });

        chartContainer.addView(mChartView);
    }

    private void prepareRange(Calendar start, Calendar end) {
        if (currentPeriod == Period.DAILY) {
            start.set(Calendar.HOUR_OF_DAY, 0);
            start.set(Calendar.MINUTE, 0);
            start.set(Calendar.SECOND, 0);
            end.set(Calendar.HOUR_OF_DAY, 23);
            end.set(Calendar.MINUTE, 59);
            end.set(Calendar.SECOND, 59);
        } else if (currentPeriod == Period.WEEKLY) {
            while (start.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                start.add(Calendar.DATE, -1);
            }
            start.set(Calendar.HOUR_OF_DAY, 0);
            start.set(Calendar.MINUTE, 0);
            start.set(Calendar.SECOND, 0);
            end.setTimeInMillis(start.getTimeInMillis());
            end.add(Calendar.DATE, 6);
            end.set(Calendar.HOUR_OF_DAY, 23);
            end.set(Calendar.MINUTE, 59);
            end.set(Calendar.SECOND, 59);
        } else {
            start.set(Calendar.DAY_OF_MONTH, 1);
            start.set(Calendar.HOUR_OF_DAY, 0);
            start.set(Calendar.MINUTE, 0);
            start.set(Calendar.SECOND, 0);
            end.setTimeInMillis(start.getTimeInMillis());
            end.add(Calendar.MONTH, 1);
            end.add(Calendar.DATE, -1);
            end.set(Calendar.HOUR_OF_DAY, 23);
            end.set(Calendar.MINUTE, 59);
            end.set(Calendar.SECOND, 59);
        }
    }
}
