package com.refresh.pos.domain.loading;

import com.refresh.pos.techicalservices.NoDaoSetException;
import com.refresh.pos.techicalservices.loading.LoadingDao;
import com.refresh.pos.domain.DateTimeStrategy;

import java.util.Calendar;
import java.util.List;

public class LoadingController {
    private static LoadingController instance = null;
    private static LoadingDao loadingDao = null;

    private LoadingController() throws NoDaoSetException {
        if (loadingDao == null) throw new NoDaoSetException();
    }

    public static boolean isDaoSet() { return loadingDao != null; }
    public static void setLoadingDao(LoadingDao dao) { loadingDao = dao; }

    public static LoadingController getInstance() throws NoDaoSetException {
        if (instance == null) instance = new LoadingController();
        return instance;
    }

    public List<LoadingCategory> getAllCategories() { return loadingDao.getAllCategories(); }
    public int addCategory(String name) { return loadingDao.addCategory(name); }
    public boolean deleteCategory(int id) { return loadingDao.deleteCategory(id); }

    public List<LoadingSubCategory> getSubCategories(int catId) { return loadingDao.getSubCategories(catId); }
    public int addSubCategory(int catId, String name, double cost, double price) {
        return loadingDao.addSubCategory(catId, name, cost, price);
    }
    public boolean deleteSubCategory(int id) { return loadingDao.deleteSubCategory(id); }

    public void restock(LoadingCategory cat, double amountToAdd, double amountPaid) {
        double newBalance = cat.getBalance() + amountToAdd;
        loadingDao.updateBalance(cat.getId(), newBalance);
        cat.setBalance(newBalance);

        LoadingTransaction t = new LoadingTransaction(
            0, cat.getId(), -1, "RESTOCK", amountToAdd, 0.0, amountPaid,
            DateTimeStrategy.getCurrentTime(), "CASH"
        );
        loadingDao.addTransaction(t);
    }

    public boolean sell(LoadingCategory cat, LoadingSubCategory sub, double amountDeducted, double amountSold, String paymentMethod) {
        if (cat.getBalance() < amountDeducted) return false;

        double newBalance = cat.getBalance() - amountDeducted;
        loadingDao.updateBalance(cat.getId(), newBalance);
        cat.setBalance(newBalance);

        LoadingTransaction t = new LoadingTransaction(
            0, cat.getId(), (sub != null ? sub.getId() : -1), "SALE", 
            amountDeducted, amountSold, 0.0,
            DateTimeStrategy.getCurrentTime(), paymentMethod
        );
        loadingDao.addTransaction(t);
        return true;
    }

    public List<LoadingTransaction> getLoadingProfits(String startDate, String endDate) {
        return loadingDao.getTransactions("SALE", startDate, endDate);
    }

    public List<LoadingTransaction> getLoadingRestocks(String startDate, String endDate) {
        return loadingDao.getTransactions("RESTOCK", startDate, endDate);
    }
}
