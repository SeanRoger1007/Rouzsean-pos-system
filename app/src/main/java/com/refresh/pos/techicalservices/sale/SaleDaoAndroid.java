package com.refresh.pos.techicalservices.sale;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.ContentValues;

import com.refresh.pos.domain.DateTimeStrategy;
import com.refresh.pos.domain.inventory.LineItem;
import com.refresh.pos.domain.inventory.Product;
import com.refresh.pos.domain.sale.QuickLoadSale;
import com.refresh.pos.domain.sale.Sale;
import com.refresh.pos.domain.sale.SaleSummary;
import com.refresh.pos.techicalservices.Database;
import com.refresh.pos.techicalservices.DatabaseContents;


/**
 * DAO used by android for Sale process.
 * 
 * @author Refresh Team
 *
 */
public class SaleDaoAndroid implements SaleDao {

	Database database;
	public SaleDaoAndroid(Database database) {
		this.database = database;
	}

	@Override
	public SaleSummary getSaleSummaryDuring(Calendar start, Calendar end) {
		String startBound = DateTimeStrategy.getSQLDateFormat(start);
		String endBound = DateTimeStrategy.getSQLDateFormat(end);
		
		// 1. Get Total Revenue directly from Sale table
		String revenueQuery = "SELECT SUM(total) as total_revenue FROM " + DatabaseContents.TABLE_SALE + 
				" WHERE status = 'ENDED' AND end_time BETWEEN '" + startBound + " 00:00:00' AND '" + endBound + " 23:59:59'";
		
		List<Object> revResults = database.select(revenueQuery);
		double totalRevenue = 0;
		if (revResults != null && !revResults.isEmpty()) {
			ContentValues cv = (ContentValues) revResults.get(0);
			Double val = cv.getAsDouble("total_revenue");
			if (val != null) totalRevenue = val;
		}

		// 2. Get Total Cost (COGS)
		// We join SaleLineItem with the latest ProductLot cost for each product
		// This is a bit complex in SQLite but more efficient than Java loops
		String costQuery = "SELECT SUM(li.quantity * COALESCE(lot.cost, 0)) as total_cost " +
				"FROM " + DatabaseContents.TABLE_SALE_LINEITEM + " li " +
				"JOIN " + DatabaseContents.TABLE_SALE + " s ON li.sale_id = s._id " +
				"LEFT JOIN (SELECT product_id, cost FROM " + DatabaseContents.TABLE_STOCK + " GROUP BY product_id HAVING _id = MAX(_id)) lot " +
				"ON li.product_id = lot.product_id " +
				"WHERE s.status = 'ENDED' AND s.end_time BETWEEN '" + startBound + " 00:00:00' AND '" + endBound + " 23:59:59'";

		List<Object> costResults = database.select(costQuery);
		double totalCost = 0;
		if (costResults != null && !costResults.isEmpty()) {
			ContentValues cv = (ContentValues) costResults.get(0);
			Double val = cv.getAsDouble("total_cost");
			if (val != null) totalCost = val;
		}

		return new SaleSummary(totalRevenue, totalCost);
	}

	@Override
	public Sale initiateSale(String startTime) {
		ContentValues content = new ContentValues();
        content.put("start_time", startTime.toString());
        content.put("status", "ON PROCESS");
        content.put("payment", "n/a");
        content.put("total", 0.0);
        content.put("orders", 0);
        content.put("end_time", startTime.toString());
        
        int id = database.insert(DatabaseContents.TABLE_SALE.toString(), content);
		return new Sale(id,startTime);
	}

	@Override
	public void endSale(Sale sale, String endTime) {
		ContentValues content = new ContentValues();
        content.put("_id", sale.getId());
        content.put("status", "ENDED");
        content.put("payment", sale.getPaymentMethod());
        content.put("total", sale.getTotal());
        content.put("orders", sale.getOrders());
        content.put("start_time", sale.getStartTime());
        content.put("end_time", endTime);
        content.put("seller_name", sale.getSellerName());
		database.update(DatabaseContents.TABLE_SALE.toString(), content);
	}
	
	@Override
	public int addLineItem(int saleId, LineItem lineItem) {
		ContentValues content = new ContentValues();
        content.put("sale_id", saleId);
        content.put("product_id", lineItem.getProduct().getId());
        content.put("quantity", lineItem.getQuantity());
        content.put("unit_price", lineItem.getPriceAtSale());
        content.put("is_piece_sale", lineItem.isPieceSale() ? 1 : 0);
        int id = database.insert(DatabaseContents.TABLE_SALE_LINEITEM.toString(), content);
        return id;
	}

	@Override
	public void updateLineItem(int saleId, LineItem lineItem) {
		ContentValues content = new ContentValues();		
		content.put("_id", lineItem.getId());
		content.put("sale_id", saleId);
		content.put("product_id", lineItem.getProduct().getId());
		content.put("quantity", lineItem.getQuantity());
		content.put("unit_price", lineItem.getPriceAtSale());
		content.put("is_piece_sale", lineItem.isPieceSale() ? 1 : 0);
		database.update(DatabaseContents.TABLE_SALE_LINEITEM.toString(), content);
	}

	@Override
	public List<Sale> getAllSale() {
		return getAllSale(" WHERE status = 'ENDED'");
	}
	
	@Override
	public List<Sale> getAllSaleDuring(Calendar start, Calendar end) {
		String startBound = DateTimeStrategy.getSQLDateFormat(start);
		String endBound = DateTimeStrategy.getSQLDateFormat(end);
		List<Sale> list = getAllSale(" WHERE end_time BETWEEN '" + startBound + " 00:00:00' AND '" + endBound + " 23:59:59' AND status = 'ENDED' ORDER BY end_time ASC");
		return list;
	}
	
	/**
	 * This method get all Sale *BUT* no LineItem will be loaded.
	 * @param condition
	 * @return
	 */
	public List<Sale> getAllSale(String condition) {
		String queryString = "SELECT * FROM " + DatabaseContents.TABLE_SALE + condition;
        List<Object> objectList = database.select(queryString);
        List<Sale> list = new ArrayList<Sale>();
        if (objectList == null) return list;
        
        for (Object object: objectList) {
        	ContentValues content = (ContentValues) object;
            
            Integer id = content.getAsInteger("_id");
            if (id == null) continue;
            
            // Handle both payment and payment_method columns for legacy support
            String payment = content.getAsString("payment");
            if (payment == null) payment = content.getAsString("payment_method");
            if (payment == null) payment = "n/a";

            Double total = content.getAsDouble("total");
            if (total == null) total = 0.0;
            
            Integer orders = content.getAsInteger("orders");
            if (orders == null) orders = 0;

        	list.add(new QuickLoadSale(
        			id,
        			content.getAsString("start_time"),
        			content.getAsString("end_time"),
        			content.getAsString("status"),
        			payment,
        			content.getAsString("seller_name"),
        			total,
        			orders     
        			)
        	);
        }
        return list;
	}
	
	/**
	 * This load complete data of Sale.
	 * @param id Sale ID.
	 * @return Sale of specific ID.
	 */
	@Override
	public Sale getSaleById(int id) {
		String queryString = "SELECT * FROM " + DatabaseContents.TABLE_SALE + " WHERE _id = " + id;
        List<Object> objectList = database.select(queryString);
        if (objectList == null || objectList.isEmpty()) return null;
        
        List<Sale> list = new ArrayList<Sale>();
        for (Object object: objectList) {
        	ContentValues content = (ContentValues) object;
            
            Integer saleId = content.getAsInteger("_id");
            if (saleId == null) saleId = id;

            String payment = content.getAsString("payment");
            if (payment == null) payment = content.getAsString("payment_method");
            if (payment == null) payment = "n/a";

        	list.add(new Sale(
        			saleId,
        			content.getAsString("start_time"),
        			content.getAsString("end_time"),
        			content.getAsString("status"),
        			payment,
        			content.getAsString("seller_name"),
        			getLineItem(saleId))
        			);
        }
        return list.isEmpty() ? null : list.get(0);
	}

	@Override
	public List<LineItem> getLineItem(int saleId) {
		String queryString = "SELECT * FROM " + DatabaseContents.TABLE_SALE_LINEITEM + " WHERE sale_id = " + saleId;
		List<Object> objectList = database.select(queryString);
		List<LineItem> list = new ArrayList<LineItem>();
		if (objectList == null) return list;
        
		for (Object object: objectList) {
			ContentValues content = (ContentValues) object;
            
            Integer lineId = content.getAsInteger("_id");
            if (lineId == null) continue;

			Integer productId = content.getAsInteger("product_id");
            if (productId == null) continue;
			
			com.refresh.pos.domain.inventory.Product product = null;
			try {
				product = com.refresh.pos.domain.inventory.Inventory.getInstance().getProductCatalog().getProductById(productId);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if (product == null) {
				// Fallback to minimal product info if not in catalog
				product = new Product(productId, "Unknown Product", "", content.getAsDouble("unit_price"));
			}

            Double qty = content.getAsDouble("quantity");
            if (qty == null) qty = 0.0;
            Double price = content.getAsDouble("unit_price");
            if (price == null) price = 0.0;

			LineItem item = new LineItem(lineId , product, qty, price);
            Integer isPiece = content.getAsInteger("is_piece_sale");
			item.setPieceSale(isPiece != null && isPiece == 1);
			list.add(item);
		}
		return list;
	}

	@Override
	public void clearSaleLedger() {
		database.execute("DELETE FROM " + DatabaseContents.TABLE_SALE);
		database.execute("DELETE FROM " + DatabaseContents.TABLE_SALE_LINEITEM);
	}

	@Override
	public void cancelSale(Sale sale,String endTime) {
		ContentValues content = new ContentValues();
        content.put("_id", sale.getId());
        content.put("status", "CANCELED");
        content.put("payment", "n/a");
        content.put("total", sale.getTotal());
        content.put("orders", sale.getOrders());
        content.put("start_time", sale.getStartTime());
        content.put("end_time", endTime);
		database.update(DatabaseContents.TABLE_SALE.toString(), content);
		
	}

	@Override
	public void removeLineItem(int id) {
		database.delete(DatabaseContents.TABLE_SALE_LINEITEM.toString(), id);
	}

}
