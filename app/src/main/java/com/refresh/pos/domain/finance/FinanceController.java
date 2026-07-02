package com.refresh.pos.domain.finance;

import android.content.ContentValues;
import com.refresh.pos.techicalservices.Database;
import com.refresh.pos.techicalservices.DatabaseContents;
import com.refresh.pos.domain.capital.CapitalController;
import java.util.ArrayList;
import java.util.List;

public class FinanceController {
    private static FinanceController instance;
    private Database database;

    private FinanceController(Database database) {
        this.database = database;
    }

    public static void init(Database database) {
        instance = new FinanceController(database);
    }

    public static FinanceController getInstance() {
        return instance;
    }

    public List<FinanceCategory> getCategories(String type) {
        String query = "SELECT * FROM " + DatabaseContents.TABLE_FINANCE_CATEGORY + " WHERE type = '" + type + "'";
        List<Object> objects = database.select(query);
        List<FinanceCategory> list = new ArrayList<>();
        if (objects != null) {
            for (Object obj : objects) {
                ContentValues row = (ContentValues) obj;
                Integer id = row.getAsInteger("_id");
                if (id == null) continue;

                list.add(new FinanceCategory(
                        id,
                        row.getAsString("name"),
                        row.getAsString("type")
                ));
            }
        }
        return list;
    }

    public void addCategory(String name, String type) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("type", type);
        database.insert(DatabaseContents.TABLE_FINANCE_CATEGORY.toString(), values);
    }

    public void deleteCategory(int categoryId) {
        database.delete(DatabaseContents.TABLE_FINANCE_CATEGORY.toString(), categoryId);
        // We no longer delete transactions. They will remain in history
        // with the original category ID, but the category name fallback 
        // will need to be handled in UI if needed.
    }

    public int findOrCreateCategory(String name, String type) {
        List<FinanceCategory> categories = getCategories(type);
        for (FinanceCategory cat : categories) {
            if (cat.getName().equalsIgnoreCase(name)) {
                return cat.getId();
            }
        }
        addCategory(name, type);
        categories = getCategories(type);
        for (FinanceCategory cat : categories) {
            if (cat.getName().equalsIgnoreCase(name)) {
                return cat.getId();
            }
        }
        return -1;
    }

    public void addTransaction(int categoryId, double amount, String date, String account, String notes, String type) {
        ContentValues values = new ContentValues();
        values.put("category_id", categoryId);
        values.put("amount", amount);
        values.put("date", date);
        values.put("account", account);
        values.put("notes", notes);
        values.put("type", type);
        database.insert(DatabaseContents.TABLE_FINANCE_TRANSACTION.toString(), values);

        // Update balances
        CapitalController capital = CapitalController.getInstance();
        if (type.equals("EXPENSE")) {
            if (account.equals("CASH")) capital.setStoreCash(capital.getStoreCash() - amount);
            else capital.setGcashBalance(capital.getGcashBalance() - amount);
        } else {
            if (account.equals("CASH")) capital.setStoreCash(capital.getStoreCash() + amount);
            else capital.setGcashBalance(capital.getGcashBalance() + amount);
        }
    }

    public double getTotalAmountBetween(String type, String startDate, String endDate) {
        return getTotalAmountBetween(type, null, startDate, endDate);
    }

    public double getTotalAmountBetween(String type, String account, String startDate, String endDate) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT SUM(t.amount) as total FROM " + DatabaseContents.TABLE_FINANCE_TRANSACTION + " t ");
        query.append("WHERE t.type = '" + type + "' ");
        
        if (account != null) {
            query.append("AND t.account = '" + account + "' ");
        }
        
        query.append("AND t.date >= '" + startDate + "' AND t.date <= '" + endDate + " 23:59:59'");

        List<Object> objects = database.select(query.toString());
        if (objects != null && !objects.isEmpty()) {
            ContentValues row = (ContentValues) objects.get(0);
            Double total = row.getAsDouble("total");
            return total != null ? total : 0;
        }
        return 0;
    }

    public java.util.List<FinanceTransaction> getTransactionsBetween(String type, String start, String end) {
        // Use >= and <= to handle both date-only and date-time strings correctly
        String query = "SELECT t.*, COALESCE(c.name, 'Deleted Category') as category_name FROM " + com.refresh.pos.techicalservices.DatabaseContents.TABLE_FINANCE_TRANSACTION + " t " +
                "LEFT JOIN " + com.refresh.pos.techicalservices.DatabaseContents.TABLE_FINANCE_CATEGORY + " c ON t.category_id = c._id " +
                "WHERE t.type = '" + type + "' AND t.date >= '" + start + "' AND t.date <= '" + end + " 23:59:59'";
        
        java.util.List<Object> results = database.select(query);
        java.util.List<FinanceTransaction> list = new java.util.ArrayList<FinanceTransaction>();
        if (results != null) {
            for (Object obj : results) {
                android.content.ContentValues cv = (android.content.ContentValues) obj;
                
                Integer id = cv.getAsInteger("_id");
                Integer catId = cv.getAsInteger("category_id");
                Double amount = cv.getAsDouble("amount");
                
                if (id == null || catId == null) continue;
                if (amount == null) amount = 0.0;

                list.add(new FinanceTransaction(
                        id,
                        catId,
                        amount,
                        cv.getAsString("date"),
                        cv.getAsString("account"),
                        cv.getAsString("notes"),
                        cv.getAsString("category_name")
                ));
            }
        }
        return list;
    }
}
