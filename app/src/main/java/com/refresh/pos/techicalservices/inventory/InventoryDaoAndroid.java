package com.refresh.pos.techicalservices.inventory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.ContentValues;

import com.refresh.pos.domain.inventory.Category;
import com.refresh.pos.domain.inventory.Product;
import com.refresh.pos.domain.inventory.ProductLot;
import com.refresh.pos.techicalservices.Database;
import com.refresh.pos.techicalservices.DatabaseContents;

/**
 * SQLite implementation of InventoryDao.
 * 
 * @author Refresh Team
 * 
 */
public class InventoryDaoAndroid implements InventoryDao {

	private Database database;

	/**
	 * Constructs a new InventoryDaoAndroid.
	 * @param database database for using in DAO.
	 */
	public InventoryDaoAndroid(Database database) {
		this.database = database;
	}

	@Override
	public int addCategory(Category category) {
		ContentValues content = new ContentValues();
		content.put("name", category.getName());
        content.put("color", category.getColor());
		return database.insert(DatabaseContents.TABLE_CATEGORY.toString(), content);
	}

	@Override
	public List<Category> getAllCategories() {
		String query = "SELECT * FROM " + DatabaseContents.TABLE_CATEGORY;
		List<Object> objects = database.select(query);
		List<Category> list = new ArrayList<Category>();
        if (objects == null) return list;
		for (Object object : objects) {
			ContentValues content = (ContentValues) object;
            Integer id = content.getAsInteger("_id");
            if (id == null) continue;
			list.add(new Category(
					id,
					content.getAsString("name"),
                    content.getAsString("color")
			));
		}
		return list;
	}

	@Override
	public Category getCategoryById(int id) {
		String query = "SELECT * FROM " + DatabaseContents.TABLE_CATEGORY + " WHERE _id = " + id;
		List<Object> objects = database.select(query);
		if (objects == null || objects.isEmpty()) {
			return null;
		}
		ContentValues content = (ContentValues) objects.get(0);
        Integer catId = content.getAsInteger("_id");
        if (catId == null) catId = id;
		return new Category(
				catId,
				content.getAsString("name"),
                content.getAsString("color")
		);
	}

	@Override
	public boolean editCategory(Category category) {
		ContentValues content = new ContentValues();
		content.put("_id", category.getId());
		content.put("name", category.getName());
        content.put("color", category.getColor());
		return database.update(DatabaseContents.TABLE_CATEGORY.toString(), content);
	}

	@Override
	public boolean deleteCategory(int id) {
		return database.delete(DatabaseContents.TABLE_CATEGORY.toString(), id);
	}

	@Override
	public int addProduct(Product product) {
		ContentValues content = new ContentValues();
		content.put("name", product.getName());
		content.put("barcode", product.getBarcode());
		content.put("unit_price", product.getUnitPrice());
        content.put("image_path", product.getImagePath());
        content.put("status", "ACTIVE");
        content.put("low_stock_threshold", product.getLowStockThreshold());
        content.put("category_id", product.getCategoryId());
        content.put("is_pack", product.isPack() ? 1 : 0);
        content.put("pieces_per_pack", product.getPiecesPerPack());
        content.put("piece_price", product.getPiecePrice());
        
        int id = database.insert(DatabaseContents.TABLE_PRODUCT_CATALOG.toString(), content);
        
        
    	ContentValues content2 = new ContentValues();
        content2.put("_id", id);
        content2.put("quantity", 0.0);
        database.insert(DatabaseContents.TABLE_STOCK_SUM.toString(), content2);
        
        return id;
	}
	
	/**
	 * Converts list of object to list of product.
	 * @param objectList list of object.
	 * @return list of product.
	 */
	private List<Product> toProductList(List<Object> objectList) {
		List<Product> list = new ArrayList<Product>();
		if (objectList == null) return list;
        for (Object object: objectList) {
        	ContentValues content = (ContentValues) object;
            Integer id = content.getAsInteger("_id");
            if (id == null) continue;
            
            Double price = content.getAsDouble("unit_price");
            if (price == null) price = 0.0;
            
            Integer threshold = content.getAsInteger("low_stock_threshold");
            if (threshold == null) threshold = 10;
            
            Integer isPack = content.getAsInteger("is_pack");
            Integer pieces = content.getAsInteger("pieces_per_pack");
            Double pPrice = content.getAsDouble("piece_price");

                list.add(new Product(
                		id,
                        content.getAsString("name"),
                        content.getAsString("barcode"),
                        price,
                        content.getAsString("image_path"),
                        threshold,
                        (content.get("category_id") != null ? content.getAsInteger("category_id") : -1),
                        (isPack != null && isPack == 1),
                        (pieces != null ? pieces : 1),
                        (pPrice != null ? pPrice : 0.0))
                );
        }
        return list;
	}

	@Override
	public List<Product> getAllProduct() {
		return getAllProduct(" WHERE status = 'ACTIVE'");
	}

	@Override
	public List<Product> getAllProduct(String condition) {
		String query = "SELECT * FROM " + DatabaseContents.TABLE_PRODUCT_CATALOG + condition + " ORDER BY name";
		List<Object> objects = database.select(query);
		return toProductList(objects);
	}

	private List<Product> getProductBy(String reference, String value) {
		String condition = " WHERE " + reference + " = '" + value + "' AND status = 'ACTIVE'";
		return getAllProduct(condition);
	}
	
	private List<Product> getSimilarProductBy(String reference, String value) {
		String condition = " WHERE " + reference + " LIKE '%" + value + "%' AND status = 'ACTIVE'";
		return getAllProduct(condition);
	}

	@Override
	public Product getProductByBarcode(String barcode) {
		List<Product> list = getProductBy("barcode", barcode);
		if (list.isEmpty()) return null;
		return list.get(0);
	}

	@Override
	public Product getProductById(int id) {
		List<Product> list = getProductBy("_id", id + "");
		if (list.isEmpty()) return null;
		return list.get(0);
	}

	@Override
	public boolean editProduct(Product product) {
		ContentValues content = new ContentValues();
		content.put("_id", product.getId());
		content.put("name", product.getName());
		content.put("barcode", product.getBarcode());
		content.put("unit_price", product.getUnitPrice());
        content.put("image_path", product.getImagePath());
        content.put("status", "ACTIVE");
        content.put("low_stock_threshold", product.getLowStockThreshold());
        content.put("category_id", product.getCategoryId());
        content.put("is_pack", product.isPack() ? 1 : 0);
        content.put("pieces_per_pack", product.getPiecesPerPack());
        content.put("piece_price", product.getPiecePrice());
		return database.update(DatabaseContents.TABLE_PRODUCT_CATALOG.toString(), content);
	}
	

	@Override
	public int addProductLot(ProductLot productLot) {
		ContentValues content = new ContentValues();
		content.put("product_id", productLot.getProduct().getId());
		content.put("quantity", productLot.getQuantity());
		content.put("cost", productLot.unitCost());
		content.put("date_added", productLot.getDateAdded());
		int id = database.insert(DatabaseContents.TABLE_STOCK.toString(), content);
		updateStockSum(productLot.getProduct().getId(), productLot.getQuantity());
		return id;
	}


	@Override
	public List<Product> getProductByName(String name) {
		return getSimilarProductBy("name", name);
	}

	@Override
	public List<Product> searchProduct(String search) {
		String condition = " WHERE (name LIKE '%" + search + "%' OR barcode LIKE '%" + search + "%') AND status = 'ACTIVE'";
		return getAllProduct(condition);
	}

	@Override
	public List<ProductLot> getAllProductLot(String condition) {
		String query = "SELECT * FROM " + DatabaseContents.TABLE_STOCK + condition;
		List<Object> objects = database.select(query);
		return toProductLotList(objects);
	}
	
	private List<ProductLot> toProductLotList(List<Object> objectList) {
		List<ProductLot> list = new ArrayList<ProductLot>();
		if (objectList == null) return list;
		for (Object object : objectList) {
			ContentValues content = (ContentValues) object;
            Integer id = content.getAsInteger("_id");
            Integer productId = content.getAsInteger("product_id");
            if (id == null || productId == null) continue;
            
			Product product = getProductById(productId);
			if (product != null) {
                Double qty = content.getAsDouble("quantity");
                Double cost = content.getAsDouble("cost");
				list.add(new ProductLot(
						id,
						content.getAsString("date_added"),
						qty != null ? qty : 0.0,
						product,
						cost != null ? cost : 0.0
				));
			}
		}
		return list;
	}

	@Override
	public List<ProductLot> getProductLotByProductId(int id) {
		return getAllProductLot(" WHERE product_id = " + id);
	}

	@Override
	public List<ProductLot> getProductLotById(int id) {
		return getAllProductLot(" WHERE _id = " + id);
	}

	@Override
	public List<ProductLot> getAllProductLot() {
		return getAllProductLot("");
	}

	@Override
	public List<ProductLot> getAllProductLotDuring(Calendar start, Calendar end) {
		String startBound = com.refresh.pos.domain.DateTimeStrategy.getSQLDateFormat(start);
		String endBound = com.refresh.pos.domain.DateTimeStrategy.getSQLDateFormat(end);
		return getAllProductLot(" WHERE date_added >= '" + startBound + "' AND date_added <= '" + endBound + " 23:59:59'");
	}

	@Override
	public int getStockSumById(int id) {
		String query = "SELECT quantity FROM " + DatabaseContents.TABLE_STOCK_SUM + " WHERE _id = " + id;
		List<Object> objects = database.select(query);
		if (objects == null || objects.isEmpty()) {
			return 0;
		}
		ContentValues content = (ContentValues) objects.get(0);
        Integer qty = content.getAsInteger("quantity");
		return qty != null ? qty : 0;
	}

	@Override
	public double getStockSumDoubleById(int id) {
		String query = "SELECT quantity FROM " + DatabaseContents.TABLE_STOCK_SUM + " WHERE _id = " + id;
		List<Object> objects = database.select(query);
		if (objects == null || objects.isEmpty()) {
			return 0.0;
		}
		ContentValues content = (ContentValues) objects.get(0);
        Double qty = content.getAsDouble("quantity");
		return qty != null ? qty : 0.0;
	}

	@Override
	public void updateStockSum(int productId, double quantity) {
		double currentQuantity = getStockSumDoubleById(productId);
		ContentValues content = new ContentValues();
		content.put("_id", productId);
		content.put("quantity", currentQuantity + quantity);
		database.update(DatabaseContents.TABLE_STOCK_SUM.toString(), content);
	}

	@Override
	public void clearProductCatalog() {
		database.execute("DELETE FROM " + DatabaseContents.TABLE_PRODUCT_CATALOG);
	}

	@Override
	public void clearStock() {
		database.execute("DELETE FROM " + DatabaseContents.TABLE_STOCK);
		database.execute("DELETE FROM " + DatabaseContents.TABLE_STOCK_SUM);
	}

	@Override
	public void suspendProduct(Product product) {
		ContentValues content = new ContentValues();
		content.put("_id", product.getId());
		content.put("status", "INACTIVE");
		database.update(DatabaseContents.TABLE_PRODUCT_CATALOG.toString(), content);
	}

	@Override
	public void removeProductCompletely(Product product) {
		int productId = product.getId();

		// 1. Delete from product_catalog
		database.execute("DELETE FROM " + DatabaseContents.TABLE_PRODUCT_CATALOG + " WHERE _id = " + productId);

		// 2. Delete from stock (removes expenses)
		database.execute("DELETE FROM " + DatabaseContents.TABLE_STOCK + " WHERE product_id = " + productId);

		// 3. Delete from stock_sum
		database.execute("DELETE FROM " + DatabaseContents.TABLE_STOCK_SUM + " WHERE _id = " + productId);

		// 4. Find all sales that contain this product
		String findSalesQuery = "SELECT DISTINCT sale_id FROM " + DatabaseContents.TABLE_SALE_LINEITEM + " WHERE product_id = " + productId;
		List<Object> saleIds = database.select(findSalesQuery);

		// 5. Delete line items for this product
		database.execute("DELETE FROM " + DatabaseContents.TABLE_SALE_LINEITEM + " WHERE product_id = " + productId);

		// 6. Recalculate and update totals for those sales
		if (saleIds != null) {
			for (Object obj : saleIds) {
				ContentValues cv = (ContentValues) obj;
				Integer saleId = cv.getAsInteger("sale_id");
                if (saleId == null) continue;

				// Get new total and order count
				String sumQuery = "SELECT SUM(quantity * unit_price) as new_total, SUM(quantity) as new_orders FROM " + DatabaseContents.TABLE_SALE_LINEITEM + " WHERE sale_id = " + saleId;
				List<Object> results = database.select(sumQuery);

				if (results != null && !results.isEmpty()) {
					ContentValues res = (ContentValues) results.get(0);
					String totalStr = res.getAsString("new_total");
					String ordersStr = res.getAsString("new_orders");

					if (totalStr == null || totalStr.equals("0") || totalStr.equals("0.0")) {
						// No more items in this sale, delete the sale record
						database.execute("DELETE FROM " + DatabaseContents.TABLE_SALE + " WHERE _id = " + saleId);
					} else {
						// Update the sale with new totals
						ContentValues updateSale = new ContentValues();
						updateSale.put("_id", saleId);
						updateSale.put("total", Double.parseDouble(totalStr));
                        try {
						    updateSale.put("orders", (int)Double.parseDouble(ordersStr));
                        } catch (Exception e) {
                            updateSale.put("orders", 0);
                        }
						database.update(DatabaseContents.TABLE_SALE.toString(), updateSale);
					}
				}
			}
		}
	}
}
