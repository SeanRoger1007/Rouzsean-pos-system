package com.refresh.pos.domain.inventory;

import java.util.HashMap;
import java.util.Map;

/**
 * Category represents a group of products.
 */
public class Category {

    private int id;
    private String name;
    private String color;
    private String icon;

    public static final int UNDEFINED_ID = -1;

    public Category(int id, String name, String color, String icon) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.icon = icon;
    }

    public Category(String name, String color, String icon) {
        this(UNDEFINED_ID, name, color, icon);
    }

    public Category(int id, String name, String color) {
        this(id, name, color, "");
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("id", id + "");
        map.put("name", name);
        map.put("color", color);
        map.put("icon", icon);
        return map;
    }
}
