package com.refresh.pos.domain.loading;

public class LoadingTransaction {
    private int id;
    private int categoryId;
    private int subCategoryId;
    private String type; // SALE, RESTOCK
    private double amountDeducted;
    private double amountSold;
    private double amountPaid;
    private String date;
    private String paymentMethod;

    public LoadingTransaction(int id, int categoryId, int subCategoryId, String type, double amountDeducted, double amountSold, double amountPaid, String date, String paymentMethod) {
        this.id = id;
        this.categoryId = categoryId;
        this.subCategoryId = subCategoryId;
        this.type = type;
        this.amountDeducted = amountDeducted;
        this.amountSold = amountSold;
        this.amountPaid = amountPaid;
        this.date = date;
        this.paymentMethod = paymentMethod;
    }

    public int getId() { return id; }
    public int getCategoryId() { return categoryId; }
    public int getSubCategoryId() { return subCategoryId; }
    public String getType() { return type; }
    public double getAmountDeducted() { return amountDeducted; }
    public double getAmountSold() { return amountSold; }
    public double getAmountPaid() { return amountPaid; }
    public String getDate() { return date; }
    public String getPaymentMethod() { return paymentMethod; }
}
