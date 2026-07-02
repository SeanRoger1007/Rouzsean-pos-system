package com.refresh.pos.techicalservices.loading;

import com.refresh.pos.domain.loading.LoadingCategory;
import com.refresh.pos.domain.loading.LoadingSubCategory;
import com.refresh.pos.domain.loading.LoadingTransaction;

import java.util.List;

public interface LoadingDao {
    List<LoadingCategory> getAllCategories();
    int addCategory(String name);
    boolean deleteCategory(int id);
    void updateBalance(int catId, double newBalance);

    List<LoadingSubCategory> getSubCategories(int catId);
    int addSubCategory(int catId, String name, double cost, double price);
    boolean deleteSubCategory(int id);

    void addTransaction(LoadingTransaction transaction);
    List<LoadingTransaction> getTransactions(String type, String startDate, String endDate);
}
