package com.refresh.pos.techicalservices.inventory;

import java.util.List;

import com.refresh.pos.domain.inventory.Product;
import com.refresh.pos.domain.inventory.ProductLot;
import com.refresh.pos.domain.inventory.Category;

/**
 * DAO for Inventory.
 * 
 * @author Refresh Team
 *
 */
public interface InventoryDao {

	int addCategory(Category category);
	List<Category> getAllCategories();
	Category getCategoryById(int id);
	boolean editCategory(Category category);
	boolean deleteCategory(int id);
	
	int addProduct(Product product);
	List<Product> getAllProduct();
	List<Product> getAllProduct(String condition);
	List<Product> getProductByName(String name);
	Product getProductById(int id);
	Product getProductByBarcode(String barcode);
	boolean editProduct(Product product);
	List<Product> searchProduct(String search);
	
	int addProductLot(ProductLot productLot);
	List<ProductLot> getAllProductLot();
	List<ProductLot> getAllProductLot(String condition);
	List<ProductLot> getProductLotByProductId(int id);
	List<ProductLot> getProductLotById(int id);
	List<ProductLot> getAllProductLotDuring(java.util.Calendar start, java.util.Calendar end);

	int getStockSumById(int id);
	double getStockSumDoubleById(int id);
	void updateStockSum(int productId, double quantity);

	void clearProductCatalog();
	void clearStock();
	void suspendProduct(Product product);
	void removeProductCompletely(Product product);
}
