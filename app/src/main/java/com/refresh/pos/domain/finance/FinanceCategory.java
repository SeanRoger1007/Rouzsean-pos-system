package com.refresh.pos.domain.finance;

public class FinanceCategory {
    private int id;
    private String name;
    private String type; // EXPENSE or INCOME

    public FinanceCategory(int id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
}
