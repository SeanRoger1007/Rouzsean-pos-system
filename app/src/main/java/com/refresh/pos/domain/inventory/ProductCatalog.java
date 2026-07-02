package com.refresh.pos.domain.inventory;

import java.util.List;

import com.refresh.pos.techicalservices.NoDaoSetException;
import com.refresh.pos.techicalservices.inventory.InventoryDao;

/**
 * ProductCatalog manages product in store.
 * 
 * @author Refresh Team
 *
 */
public class ProductCatalog {

	private static ProductCatalog instance = null;
	private InventoryDao inventoryDao = null;
	
	public ProductCatalog(InventoryDao inventoryDao) {
		this.inventoryDao = inventoryDao;
	}
	
	private ProductCatalog() throws NoDaoSetException {
		if (!isDaoSet()) {
			throw new NoDaoSetException();
		}
	}
	
	/**
	 * Determines whether the DAO already set or not.
	 * @return true if the DAO already set; otherwise false.
	 */
	public boolean isDaoSet() {
		return inventoryDao != null;
	}
	
	/**
	 * Returns the static instance of this class.
	 * @return static instance of this class.
	 * @throws NoDaoSetException
	 */
	public static ProductCatalog getInstance() throws NoDaoSetException {
		if (instance == null) instance = new ProductCatalog();
		return instance;
	}

	/**
	 * Injects its inventory DAO.
	 * @param dao DAO of inventory.
	 */
	public void setInventoryDao(InventoryDao dao) {
		this.inventoryDao = dao;
	}

	/**
	 * Adds category to catalog.
	 * @param name name of category.
	 * @return ID of category just added.
	 */
	public int addCategory(String name) {
		return addCategory(name, "#888888", "");
	}

	public int addCategory(String name, String color, String icon) {
		return inventoryDao.addCategory(new Category(name, color, icon));
	}
	
	/**
	 * Returns all categories in catalog.
	 * @return all categories in catalog.
	 */
	public List<Category> getAllCategories() {
		return inventoryDao.getAllCategories();
	}
	
	/**
	 * Returns category from catalog by specific ID.
	 * @param id specific ID of category.
	 * @return category from catalog by specific ID.
	 */
	public Category getCategoryById(int id) {
		return inventoryDao.getCategoryById(id);
	}
	
	/**
	 * Edits category in catalog.
	 * @param category category to be edited.
	 * @return true if edit success ; otherwise false.
	 */
	public boolean editCategory(Category category) {
		return inventoryDao.editCategory(category);
	}
	
	/**
	 * Deletes category from catalog.
	 * @param id ID of category to be deleted.
	 */
	public void deleteCategory(int id) {
		inventoryDao.deleteCategory(id);
	}
	
	/**
	 * Adds product to catalog.
	 * @param product product to be added.
	 * @return ID of product just added.
	 */
	public int addProduct(Product product) {
		return inventoryDao.addProduct(product);
	}
	
	/**
	 * Returns all products in catalog.
	 * @return all products in catalog.
	 */
	public List<Product> getAllProduct() {
		return inventoryDao.getAllProduct();
	}
	
	/**
	 * Returns products from catalog by specific name.
	 * @param name name of product.
	 * @return products from catalog by specific name.
	 */
	public List<Product> getProductByName(String name) {
		return inventoryDao.getProductByName(name);
	}
	
	/**
	 * Returns product from catalog by specific ID.
	 * @param id ID of product.
	 * @return product from catalog by specific ID.
	 */
	public Product getProductById(int id) {
		return inventoryDao.getProductById(id);
	}
	
	/**
	 * Returns product from catalog by specific barcode.
	 * @param barcode barcode of product.
	 * @return product from catalog by specific barcode.
	 */
	public Product getProductByBarcode(String barcode) {
		return inventoryDao.getProductByBarcode(barcode);
	}
	
	/**
	 * Edits product in catalog.
	 * @param product product to be edited.
	 * @return true if edit success ; otherwise false.
	 */
	public boolean editProduct(Product product) {
		return inventoryDao.editProduct(product);
	}
	
	/**
	 * Returns all products that has similar name or barcode.
	 * @param search String for searching products.
	 * @return all products that has similar name or barcode.
	 */
	public List<Product> searchProduct(String search) {
		return inventoryDao.searchProduct(search);
	}

	/**
	 * Sets status of product in catalog to inactive.
	 * @param product product to be suspended.
	 */
	public void suspendProduct(Product product) {
		inventoryDao.suspendProduct(product);
	}

	/**
	 * Removes all products from catalog.
	 */
	public void clearProductCatalog() {
		inventoryDao.clearProductCatalog();
	}

	/**
	 * Remove product from system completely.
	 * @param product The product to be removed.
	 */
	public void removeProductCompletely(Product product) {
		inventoryDao.removeProductCompletely(product);
	}
}
