package com.refresh.pos.techicalservices.loading;

import android.content.ContentValues;
import com.refresh.pos.domain.loading.LoadingCategory;
import com.refresh.pos.domain.loading.LoadingSubCategory;
import com.refresh.pos.domain.loading.LoadingTransaction;
import com.refresh.pos.techicalservices.Database;
import com.refresh.pos.techicalservices.DatabaseContents;

import java.util.ArrayList;
import java.util.List;

public class LoadingDaoAndroid implements LoadingDao {
    private Database database;

    public LoadingDaoAndroid(Database database) {
        this.database = database;
    }

    @Override
    public List<LoadingCategory> getAllCategories() {
        String query = "SELECT * FROM " + DatabaseContents.TABLE_LOADING_CATEGORY + " ORDER BY name";
        List<Object> objects = database.select(query);
        List<LoadingCategory> list = new ArrayList<>();
        if (objects == null) return list;
        for (Object obj : objects) {
            ContentValues cv = (ContentValues) obj;
            list.add(new LoadingCategory(cv.getAsInteger("_id"), cv.getAsString("name"), cv.getAsDouble("balance")));
        }
        return list;
    }

    @Override
    public int addCategory(String name) {
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("balance", 0.0);
        return database.insert(DatabaseContents.TABLE_LOADING_CATEGORY.toString(), cv);
    }

    @Override
    public boolean deleteCategory(int id) {
        database.execute("DELETE FROM " + DatabaseContents.TABLE_LOADING_SUBCATEGORY + " WHERE cat_id = " + id);
        database.execute("DELETE FROM " + DatabaseContents.TABLE_LOADING_TRANSACTION + " WHERE cat_id = " + id);
        return database.delete(DatabaseContents.TABLE_LOADING_CATEGORY.toString(), id);
    }

    @Override
    public void updateBalance(int catId, double newBalance) {
        ContentValues cv = new ContentValues();
        cv.put("_id", catId);
        cv.put("balance", newBalance);
        database.update(DatabaseContents.TABLE_LOADING_CATEGORY.toString(), cv);
    }

    @Override
    public List<LoadingSubCategory> getSubCategories(int catId) {
        String query = "SELECT * FROM " + DatabaseContents.TABLE_LOADING_SUBCATEGORY + " WHERE cat_id = " + catId;
        List<Object> objects = database.select(query);
        List<LoadingSubCategory> list = new ArrayList<>();
        if (objects == null) return list;
        for (Object obj : objects) {
            ContentValues cv = (ContentValues) obj;
            list.add(new LoadingSubCategory(cv.getAsInteger("_id"), cv.getAsInteger("cat_id"), cv.getAsString("name"), cv.getAsDouble("cost"), cv.getAsDouble("price")));
        }
        return list;
    }

    @Override
    public int addSubCategory(int catId, String name, double cost, double price) {
        ContentValues cv = new ContentValues();
        cv.put("cat_id", catId);
        cv.put("name", name);
        cv.put("cost", cost);
        cv.put("price", price);
        return database.insert(DatabaseContents.TABLE_LOADING_SUBCATEGORY.toString(), cv);
    }

    @Override
    public boolean deleteSubCategory(int id) {
        return database.delete(DatabaseContents.TABLE_LOADING_SUBCATEGORY.toString(), id);
    }

    @Override
    public void addTransaction(LoadingTransaction t) {
        ContentValues cv = new ContentValues();
        cv.put("cat_id", t.getCategoryId());
        cv.put("subcat_id", t.getSubCategoryId());
        cv.put("type", t.getType());
        cv.put("amount_deducted", t.getAmountDeducted());
        cv.put("amount_sold", t.getAmountSold());
        cv.put("amount_paid", t.getAmountPaid());
        cv.put("date", t.getDate());
        cv.put("payment_method", t.getPaymentMethod());
        database.insert(DatabaseContents.TABLE_LOADING_TRANSACTION.toString(), cv);
    }

    @Override
    public List<LoadingTransaction> getTransactions(String type, String startDate, String endDate) {
        String query = "SELECT * FROM " + DatabaseContents.TABLE_LOADING_TRANSACTION + " WHERE type = '" + type + "' AND date BETWEEN '" + startDate + " 00:00:00' AND '" + endDate + " 23:59:59'";
        List<Object> objects = database.select(query);
        List<LoadingTransaction> list = new ArrayList<>();
        if (objects == null) return list;
        for (Object obj : objects) {
            ContentValues cv = (ContentValues) obj;
            list.add(new LoadingTransaction(
                cv.getAsInteger("_id"),
                cv.getAsInteger("cat_id"),
                cv.getAsInteger("subcat_id"),
                cv.getAsString("type"),
                cv.getAsDouble("amount_deducted"),
                cv.getAsDouble("amount_sold"),
                cv.getAsDouble("amount_paid"),
                cv.getAsString("date"),
                cv.getAsString("payment_method")
            ));
        }
        return list;
    }
}
