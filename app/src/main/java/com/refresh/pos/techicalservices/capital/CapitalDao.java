package com.refresh.pos.techicalservices.capital;

public interface CapitalDao {
    double getCapital(String key);
    void setCapital(String key, double value);
}
