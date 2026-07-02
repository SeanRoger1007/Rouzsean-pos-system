package com.refresh.pos.domain.inventory;

import java.util.HashMap;
import java.util.Map;

/**
 * LineItem of Sale.
 * 
 * @author Refresh Team
 * 
 */
public class LineItem {

	private final Product product;
	private double quantity;
	private int id;
	private double unitPriceAtSale;
	private boolean isPieceSale;

	/**
	 * Static value for UNDEFINED ID.
	 */
	public static final int UNDEFINED = -1;

	/**
	 * Constructs a new LineItem.
	 * @param product product of this LineItem.
	 * @param quantity product quantity of this LineItem.
	 */
	public LineItem(Product product, double quantity) {
		this(UNDEFINED, product, quantity, product.getUnitPrice());
	}

	/**
	 * Constructs a new LineItem.
	 * @param id ID of this LineItem, This value should be assigned from database.
	 * @param product product of this LineItem.
	 * @param quantity product quantity of this LineItem.
	 * @param unitPriceAtSale unit price at sale time. default is price from ProductCatalog.
	 */
	public LineItem(int id, Product product, double quantity,
			double unitPriceAtSale) {
		this(id, product, quantity, unitPriceAtSale, false);
	}

	/**
	 * Constructs a new LineItem.
	 * @param id ID of this LineItem.
	 * @param product product of this LineItem.
	 * @param quantity quantity.
	 * @param unitPriceAtSale price at sale.
	 * @param isPieceSale whether this is a piece sale.
	 */
	public LineItem(int id, Product product, double quantity, double unitPriceAtSale, boolean isPieceSale) {
		this.id = id;
		this.product = product;
		this.quantity = quantity;
		this.unitPriceAtSale = unitPriceAtSale;
		this.isPieceSale = isPieceSale;
	}

	/**
	 * Returns product in this LineItem.
	 * @return product in this LineItem.
	 */
	public Product getProduct() {
		return product;
	}

	/**
	 * Return quantity of product in this LineItem.
	 * @return quantity of product in this LineItem.
	 */
	public double getQuantity() {
		return quantity;
	}

	/**
	 * Sets quantity of product in this LineItem.
	 * @param quantity quantity of product in this LineItem.
	 */
	public void setQuantity(double quantity) {
		this.quantity = quantity;
	}

	/**
	 * Adds quantity of product in this LineItem.
	 * @param amount amount for add in quantity.
	 */
	public void addQuantity(double amount) {
		this.quantity += amount;
	}

	/**
	 * Returns total price of this LineItem.
	 * @return total price of this LineItem.
	 */
	public double getTotalPriceAtSale() {
		double price = isPieceSale ? product.getPiecePrice() : unitPriceAtSale;
		if (isPieceSale) {
			int actualPieces = (int) Math.round(quantity * product.getPiecesPerPack());
			return product.getPiecePrice() * actualPieces;
		}
		return unitPriceAtSale * quantity;
	}

	/**
	 * Returns whether this is a piece sale.
	 * @return true if piece sale.
	 */
	public boolean isPieceSale() {
		return isPieceSale;
	}

	/**
	 * Sets whether this is a piece sale.
	 * @param pieceSale piece sale status.
	 */
	public void setPieceSale(boolean pieceSale) {
		isPieceSale = pieceSale;
	}

	/**
	 * Returns the description of this LineItem in Map format.
	 * @return the description of this LineItem in Map format.
	 */
	public Map<String, String> toMap() {
		Map<String, String> map = new HashMap<String, String>();
		String displayName = product.getName();
		map.put("name", displayName);

		double qty = quantity;
		String unit = "";
		if (product.isPack()) {
			if (isPieceSale) {
				unit = " Pc";
				qty = Math.round(quantity * product.getPiecesPerPack());
			} else {
				unit = " Pk";
			}
		}

		String qtyStr = (qty == (int)qty) ? String.valueOf((int)qty) : String.format(java.util.Locale.US, "%.2f", qty);
		map.put("quantity", qtyStr + unit);
		map.put("price", String.format(java.util.Locale.US, "%.2f", getTotalPriceAtSale()));
		return map;
	}

	/**
	 * Returns id of this LineItem.
	 * @return id of this LineItem.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets id of this LineItem.
	 * @param id of this LineItem.
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Sets price product of this LineItem.
	 * @param unitPriceAtSale price product of this LineItem.
	 */
	public void setUnitPriceAtSale(double unitPriceAtSale) {
		this.unitPriceAtSale = unitPriceAtSale;
	}

	/**
	 * Returns price product of this LineItem.
	 * @return unitPriceAtSale price product of this LineItem.
	 */
	public Double getPriceAtSale() {
		return unitPriceAtSale;
	}

	/**
	 * Determines whether two objects are equal or not.
	 * @return true if Object is a LineItem with same ID ; otherwise false.
	 */
	@Override
	public boolean equals(Object object) {
		if (object == null)
			return false;
		if (!(object instanceof LineItem))
			return false;
		LineItem lineItem = (LineItem) object;
		return lineItem.getId() == this.getId();
	}
}
