package com.refresh.pos.domain.capital;

import com.refresh.pos.techicalservices.capital.CapitalDao;

public class CapitalController {
    private static CapitalController instance;
    private CapitalDao capitalDao;

    private CapitalController(CapitalDao capitalDao) {
        this.capitalDao = capitalDao;
    }

    public static void init(CapitalDao capitalDao) {
        instance = new CapitalController(capitalDao);
    }

    public static CapitalController getInstance() {
        return instance;
    }

    public double getStoreCash() {
        return capitalDao.getCapital("store_cash");
    }

    public void setStoreCash(double value) {
        capitalDao.setCapital("store_cash", value);
    }

    public double getGcashBalance() {
        return capitalDao.getCapital("gcash_balance");
    }

    public void setGcashBalance(double value) {
        capitalDao.setCapital("gcash_balance", value);
    }
}
