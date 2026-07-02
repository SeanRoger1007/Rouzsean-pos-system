package com.refresh.pos.domain.sale;

public class SaleSummary {
    private double totalRevenue;
    private double totalCost;

    public SaleSummary(double totalRevenue, double totalCost) {
        this.totalRevenue = totalRevenue;
        this.totalCost = totalCost;
    }

    public double getTotalRevenue() { return totalRevenue; }
    public double getTotalCost() { return totalCost; }
    public double getTotalProfit() { return totalRevenue - totalCost; }
}
