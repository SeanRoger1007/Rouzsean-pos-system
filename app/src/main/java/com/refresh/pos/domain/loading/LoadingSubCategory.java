package com.refresh.pos.domain.loading;

public class LoadingSubCategory {
    private int id;
    private int categoryId;
    private String name;
    private double cost;
    private double price;

    public LoadingSubCategory(int id, int categoryId, String name, double cost, double price) {
        this.id = id;
        this.categoryId = categoryId;
        this.name = name;
        this.cost = cost;
        this.price = price;
    }

    public int getId() { return id; }
    public int getCategoryId() { return categoryId; }
    public String getName() { return name; }
    public double getCost() { return cost; }
    public double getPrice() { return price; }
}
