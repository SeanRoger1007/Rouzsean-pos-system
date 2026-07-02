package com.refresh.pos.domain.inventory;

import java.util.HashMap;
import java.util.Map;

/**
 * Product or item represents the real product in store.
 * 
 * @author Refresh Team
 *
 */
public class Product {

	private int id;
	private String name;
	private String barcode;
	private double unitPrice;
	private String imagePath;
	private int lowStockThreshold;
	private int categoryId;
	private boolean isPack;
	private int piecesPerPack;
	private double piecePrice;
	
	/**
	 * Static value for UNDEFINED ID.
	 */
	public static final int UNDEFINED_ID = -1;

	/**
	 * Constructs a new Product.
	 * @param id ID of the product, This value should be assigned from database.
	 * @param name name of this product.
	 * @param barcode barcode (any standard format) of this product.
	 * @param salePrice price for using when doing sale.
	 */
	public Product(int id, String name, String barcode, double salePrice) {
		this(id, name, barcode, salePrice, null, 10, -1);
	}

	/**
	 * Constructs a new Product.
	 * @param id ID of the product, This value should be assigned from database.
	 * @param name name of this product.
	 * @param barcode barcode (any standard format) of this product.
	 * @param salePrice price for using when doing sale.
	 * @param imagePath path of image of this product.
	 */
	public Product(int id, String name, String barcode, double salePrice, String imagePath) {
		this(id, name, barcode, salePrice, imagePath, 10, -1);
	}

	/**
	 * Constructs a new Product.
	 * @param id ID of the product, This value should be assigned from database.
	 * @param name name of this product.
	 * @param barcode barcode (any standard format) of this product.
	 * @param salePrice price for using when doing sale.
	 * @param imagePath path of image of this product.
	 * @param lowStockThreshold threshold for low stock alert.
	 */
	public Product(int id, String name, String barcode, double salePrice, String imagePath, int lowStockThreshold) {
		this(id, name, barcode, salePrice, imagePath, lowStockThreshold, -1);
	}

	/**
	 * Constructs a new Product.
	 * @param id ID of the product, This value should be assigned from database.
	 * @param name name of this product.
	 * @param barcode barcode (any standard format) of this product.
	 * @param salePrice price for using when doing sale.
	 * @param imagePath path of image of this product.
	 * @param lowStockThreshold threshold for low stock alert.
	 * @param categoryId ID of the category of this product.
	 */
	public Product(int id, String name, String barcode, double salePrice, String imagePath, int lowStockThreshold, int categoryId) {
		this(id, name, barcode, salePrice, imagePath, lowStockThreshold, categoryId, false, 1, 0.0);
	}

	/**
	 * Constructs a new Product.
	 * @param id ID of the product.
	 * @param name name of this product.
	 * @param barcode barcode of this product.
	 * @param salePrice price for using when doing sale.
	 * @param imagePath path of image of this product.
	 * @param lowStockThreshold threshold for low stock alert.
	 * @param categoryId ID of the category of this product.
	 * @param isPack whether this is a pack product.
	 * @param piecesPerPack number of pieces in one pack.
	 * @param piecePrice price for a single piece.
	 */
	public Product(int id, String name, String barcode, double salePrice, String imagePath, int lowStockThreshold, int categoryId, boolean isPack, int piecesPerPack, double piecePrice) {
		this.id = id;
		this.name = name;
		this.barcode = barcode;
		this.unitPrice = salePrice;
		this.imagePath = imagePath;
		this.lowStockThreshold = lowStockThreshold;
		this.categoryId = categoryId;
		this.isPack = isPack;
		this.piecesPerPack = piecesPerPack;
		this.piecePrice = piecePrice;
	}
	
	/**
	 * Constructs a new Product.
	 * @param name name of this product.
	 * @param barcode barcode (any standard format) of this product.
	 * @param salePrice price for using when doing sale.
	 */
	public Product(String name, String barcode, double salePrice) {
		this(UNDEFINED_ID, name, barcode, salePrice, null, 10, -1);
	}

	/**
	 * Constructs a new Product.
	 * @param name name of this product.
	 * @param barcode barcode (any standard format) of this product.
	 * @param salePrice price for using when doing sale.
	 * @param imagePath path of image of this product.
	 */
	public Product(String name, String barcode, double salePrice, String imagePath) {
		this(UNDEFINED_ID, name, barcode, salePrice, imagePath, 10, -1);
	}

	/**
	 * Returns name of this product.
	 * @return name of this product.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets name of this product.
	 * @param name name of this product.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets barcode of this product.
	 * @param barcode barcode of this product.
	 */
	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

	/**
	 * Sets price of this product.
	 * @param unitPrice price of this product.
	 */
	public void setUnitPrice(double unitPrice) {
		this.unitPrice = unitPrice;
	}

	/**
	 * Returns id of this product.
	 * @return id of this product.
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Returns barcode of this product.
	 * @return barcode of this product.
	 */
	public String getBarcode() {
		return barcode;
	}
	
	/**
	 * Returns price of this product.
	 * @return price of this product.
	 */
	public double getUnitPrice() {
		return unitPrice;
	}

	/**
	 * Returns image path of this product.
	 * @return image path of this product.
	 */
	public String getImagePath() {
		return imagePath;
	}

	/**
	 * Sets image path of this product.
	 * @param imagePath image path of this product.
	 */
	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	/**
	 * Returns low stock threshold of this product.
	 * @return low stock threshold.
	 */
	public int getLowStockThreshold() {
		return lowStockThreshold;
	}

	/**
	 * Sets low stock threshold of this product.
	 * @param lowStockThreshold low stock threshold.
	 */
	public void setLowStockThreshold(int lowStockThreshold) {
		this.lowStockThreshold = lowStockThreshold;
	}

	/**
	 * Returns category ID of this product.
	 * @return category ID.
	 */
	public int getCategoryId() {
		return categoryId;
	}

	/**
	 * Sets category ID of this product.
	 * @param categoryId category ID.
	 */
	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}

	/**
	 * Returns whether this product is a pack.
	 * @return true if pack.
	 */
	public boolean isPack() {
		return isPack;
	}

	/**
	 * Sets whether this product is a pack.
	 * @param pack pack status.
	 */
	public void setPack(boolean pack) {
		isPack = pack;
	}

	/**
	 * Returns pieces per pack.
	 * @return pieces per pack.
	 */
	public int getPiecesPerPack() {
		return piecesPerPack;
	}

	/**
	 * Sets pieces per pack.
	 * @param piecesPerPack pieces per pack.
	 */
	public void setPiecesPerPack(int piecesPerPack) {
		this.piecesPerPack = piecesPerPack;
	}

	/**
	 * Returns price per piece.
	 * @return price per piece.
	 */
	public double getPiecePrice() {
		return piecePrice;
	}

	/**
	 * Sets price per piece.
	 * @param piecePrice price per piece.
	 */
	public void setPiecePrice(double piecePrice) {
		this.piecePrice = piecePrice;
	}

	/**
	 * Returns the description of this Product in Map format. 
	 * @return the description of this Product in Map format.
	 */
	public Map<String, String> toMap() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("id", id + "");
		map.put("name", name);
		map.put("barcode", barcode);
		map.put("unitPrice", unitPrice + "");
		map.put("imagePath", imagePath);
		map.put("lowStockThreshold", lowStockThreshold + "");
		map.put("categoryId", categoryId + "");
		map.put("isPack", String.valueOf(isPack));
		map.put("piecesPerPack", piecesPerPack + "");
		map.put("piecePrice", piecePrice + "");
		return map;
		
	}
	
}
