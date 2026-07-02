package com.refresh.pos.domain.finance;

public class FinanceTransaction {
    private int id;
    private int categoryId;
    private double amount;
    private String date;
    private String account;
    private String notes;
    private String categoryName;

    public FinanceTransaction(int id, int categoryId, double amount, String date, String account, String notes) {
        this(id, categoryId, amount, date, account, notes, "");
    }

    public FinanceTransaction(int id, int categoryId, double amount, String date, String account, String notes, String categoryName) {
        this.id = id;
        this.categoryId = categoryId;
        this.amount = amount;
        this.date = date;
        this.account = account;
        this.notes = notes;
        this.categoryName = categoryName;
    }

    public int getId() { return id; }
    public int getCategoryId() { return categoryId; }
    public double getAmount() { return amount; }
    public String getDate() { return date; }
    public String getAccount() { return account; }
    public String getNotes() { return notes; }
    public String getCategoryName() { return categoryName; }
}
