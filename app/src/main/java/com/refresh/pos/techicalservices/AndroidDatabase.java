package com.refresh.pos.techicalservices;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLite implementation of Database.
 * 
 * @author Refresh Team
 *
 */
public class AndroidDatabase extends SQLiteOpenHelper implements Database {

	private static final String DATABASE_NAME = "POS.db";
	private static final int DATABASE_VERSION = 26;
	private SQLiteDatabase database;

	public AndroidDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL("CREATE TABLE " + DatabaseContents.TABLE_PRODUCT_CATALOG + "("
				+ "_id INTEGER PRIMARY KEY,"
				+ "name TEXT,"
				+ "barcode TEXT,"
				+ "unit_price DOUBLE,"
				+ "image_path TEXT,"
				+ "low_stock_threshold INTEGER DEFAULT 10,"
				+ "category_id INTEGER DEFAULT -1,"
				+ "is_pack INTEGER DEFAULT 0,"
				+ "pieces_per_pack INTEGER DEFAULT 1,"
				+ "piece_price DOUBLE DEFAULT 0.0,"
				+ "status TEXT DEFAULT 'ACTIVE'"
				+ ");");

		database.execSQL("CREATE TABLE " + DatabaseContents.TABLE_STOCK + "("
				+ "_id INTEGER PRIMARY KEY,"
				+ "product_id INTEGER,"
				+ "quantity DOUBLE,"
				+ "cost DOUBLE,"
				+ "date_added TEXT"
				+ ");");

		database.execSQL("CREATE TABLE " + DatabaseContents.TABLE_STOCK_SUM + "("
				+ "_id INTEGER PRIMARY KEY,"
				+ "quantity DOUBLE"
				+ ");");

		database.execSQL("CREATE TABLE " + DatabaseContents.TABLE_SALE + "("
				+ "_id INTEGER PRIMARY KEY,"
				+ "start_time TEXT,"
				+ "end_time TEXT,"
				+ "status TEXT,"
				+ "payment TEXT,"
				+ "total DOUBLE,"
				+ "orders INTEGER,"
				+ "seller_name TEXT DEFAULT 'Owner'"
				+ ");");

		database.execSQL("CREATE TABLE " + DatabaseContents.TABLE_SALE_LINEITEM + "("
				+ "_id INTEGER PRIMARY KEY,"
				+ "sale_id INTEGER,"
				+ "product_id INTEGER,"
				+ "quantity DOUBLE,"
				+ "unit_price DOUBLE,"
				+ "is_piece_sale INTEGER DEFAULT 0"
				+ ");");

		database.execSQL("CREATE TABLE " + DatabaseContents.TABLE_STAFF + "("
				+ "_id INTEGER PRIMARY KEY,"
				+ "name TEXT,"
				+ "role TEXT,"
				+ "daily_salary DOUBLE,"
				+ "default_hours DOUBLE DEFAULT 12.0"
				+ ");");

		database.execSQL("CREATE TABLE " + DatabaseContents.TABLE_STAFF_WORK_LOG + "("
				+ "_id INTEGER PRIMARY KEY,"
				+ "staff_id INTEGER,"
				+ "date TEXT,"
				+ "hours_worked DOUBLE DEFAULT 8.0"
				+ ");");

		database.execSQL("CREATE TABLE " + DatabaseContents.TABLE_CATEGORY + "("
				+ "_id INTEGER PRIMARY KEY,"
				+ "name TEXT,"
				+ "color TEXT"
				+ ");");

		database.execSQL("CREATE TABLE " + DatabaseContents.TABLE_FINANCE_CATEGORY + "("
				+ "_id INTEGER PRIMARY KEY,"
				+ "name TEXT,"
				+ "type TEXT"
				+ ");");

		database.execSQL("CREATE TABLE " + DatabaseContents.TABLE_FINANCE_TRANSACTION + "("
				+ "_id INTEGER PRIMARY KEY,"
				+ "category_id INTEGER,"
				+ "amount DOUBLE,"
				+ "date TEXT,"
				+ "account TEXT,"
				+ "notes TEXT,"
				+ "type TEXT"
				+ ");");

		database.execSQL("CREATE TABLE " + DatabaseContents.TABLE_CAPITAL + "("
				+ "key TEXT PRIMARY KEY,"
				+ "value DOUBLE DEFAULT 0.0"
				+ ");");

		database.execSQL("CREATE TABLE " + DatabaseContents.TABLE_LOADING_CATEGORY + "("
				+ "_id INTEGER PRIMARY KEY,"
				+ "name TEXT,"
				+ "balance DOUBLE DEFAULT 0.0"
				+ ");");

		database.execSQL("CREATE TABLE " + DatabaseContents.TABLE_LOADING_SUBCATEGORY + "("
				+ "_id INTEGER PRIMARY KEY,"
				+ "cat_id INTEGER,"
				+ "name TEXT,"
				+ "cost DOUBLE,"
				+ "price DOUBLE"
				+ ");");

		database.execSQL("CREATE TABLE " + DatabaseContents.TABLE_LOADING_TRANSACTION + "("
				+ "_id INTEGER PRIMARY KEY,"
				+ "cat_id INTEGER,"
				+ "subcat_id INTEGER,"
				+ "type TEXT," // SALE, RESTOCK
				+ "amount_deducted DOUBLE,"
				+ "amount_sold DOUBLE,"
				+ "amount_paid DOUBLE,"
				+ "date TEXT,"
				+ "payment_method TEXT DEFAULT 'CASH'"
				+ ");");

		// Seed initial data safely
		seedDefaultFinanceCategories(database);
		seedDefaultLoadingCategories(database);

		// Default capital keys
		database.execSQL("INSERT OR IGNORE INTO " + DatabaseContents.TABLE_CAPITAL + " (key, value) VALUES ('store_cash', 0.0);");
		database.execSQL("INSERT OR IGNORE INTO " + DatabaseContents.TABLE_CAPITAL + " (key, value) VALUES ('gcash_balance', 0.0);");

		database.execSQL("CREATE TABLE " + DatabaseContents.LANGUAGE + "("
				+ "_id INTEGER PRIMARY KEY,"
				+ "language TEXT"
				+ ");");
	}

	private void seedDefaultLoadingCategories(SQLiteDatabase db) {
		String[] cats = {"Globe", "Smart"};
		for (String name : cats) {
			db.execSQL("INSERT INTO " + DatabaseContents.TABLE_LOADING_CATEGORY + " (name, balance) " +
					"SELECT '" + name + "', 0.0 WHERE NOT EXISTS (SELECT 1 FROM " + DatabaseContents.TABLE_LOADING_CATEGORY + " WHERE name = '" + name + "');");
		}
	}

	private void seedDefaultFinanceCategories(SQLiteDatabase db) {
		String[] expenses = {"Batelec", "Water Utility", "Online Shopping", "Inventory", "OWNER_USE"};
		for (String name : expenses) {
			db.execSQL("INSERT INTO " + DatabaseContents.TABLE_FINANCE_CATEGORY + " (name, type) " +
					"SELECT '" + name + "', 'EXPENSE' WHERE NOT EXISTS (SELECT 1 FROM " + DatabaseContents.TABLE_FINANCE_CATEGORY + " WHERE name = '" + name + "' AND type = 'EXPENSE');");
		}
		db.execSQL("INSERT INTO " + DatabaseContents.TABLE_FINANCE_CATEGORY + " (name, type) " +
				"SELECT 'Other Income', 'INCOME' WHERE NOT EXISTS (SELECT 1 FROM " + DatabaseContents.TABLE_FINANCE_CATEGORY + " WHERE name = 'Other Income' AND type = 'INCOME');");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion < 26) {
			// Comprehensive Schema Repair and Cleanup
			try {
				// 1. Ensure all tables exist
				db.execSQL("CREATE TABLE IF NOT EXISTS " + DatabaseContents.TABLE_STAFF + " (_id INTEGER PRIMARY KEY, name TEXT, role TEXT, daily_salary DOUBLE, default_hours DOUBLE DEFAULT 12.0);");
				db.execSQL("CREATE TABLE IF NOT EXISTS " + DatabaseContents.TABLE_STAFF_WORK_LOG + " (_id INTEGER PRIMARY KEY, staff_id INTEGER, date TEXT, hours_worked DOUBLE DEFAULT 8.0);");
				db.execSQL("CREATE TABLE IF NOT EXISTS " + DatabaseContents.TABLE_FINANCE_CATEGORY + " (_id INTEGER PRIMARY KEY, name TEXT, type TEXT);");
				db.execSQL("CREATE TABLE IF NOT EXISTS " + DatabaseContents.TABLE_FINANCE_TRANSACTION + " (_id INTEGER PRIMARY KEY, category_id INTEGER, amount DOUBLE, date TEXT, account TEXT, notes TEXT, type TEXT);");
				db.execSQL("CREATE TABLE IF NOT EXISTS " + DatabaseContents.TABLE_CATEGORY + " (_id INTEGER PRIMARY KEY, name TEXT, color TEXT);");
				db.execSQL("CREATE TABLE IF NOT EXISTS " + DatabaseContents.TABLE_CAPITAL + " (key TEXT PRIMARY KEY, value DOUBLE DEFAULT 0.0);");
				
				// Loading Station Tables
				db.execSQL("CREATE TABLE IF NOT EXISTS " + DatabaseContents.TABLE_LOADING_CATEGORY + " (_id INTEGER PRIMARY KEY, name TEXT, balance DOUBLE DEFAULT 0.0);");
				db.execSQL("CREATE TABLE IF NOT EXISTS " + DatabaseContents.TABLE_LOADING_SUBCATEGORY + " (_id INTEGER PRIMARY KEY, cat_id INTEGER, name TEXT, cost DOUBLE, price DOUBLE);");
				db.execSQL("CREATE TABLE IF NOT EXISTS " + DatabaseContents.TABLE_LOADING_TRANSACTION + " (_id INTEGER PRIMARY KEY, cat_id INTEGER, subcat_id INTEGER, type TEXT, amount_deducted DOUBLE, amount_sold DOUBLE, amount_paid DOUBLE, date TEXT, payment_method TEXT DEFAULT 'CASH');");

				// 2. Add all missing columns to existing tables
				String[] repairs = {
					"ALTER TABLE " + DatabaseContents.TABLE_PRODUCT_CATALOG + " ADD COLUMN is_pack INTEGER DEFAULT 0;",
					"ALTER TABLE " + DatabaseContents.TABLE_PRODUCT_CATALOG + " ADD COLUMN pieces_per_pack INTEGER DEFAULT 1;",
					"ALTER TABLE " + DatabaseContents.TABLE_PRODUCT_CATALOG + " ADD COLUMN piece_price DOUBLE DEFAULT 0.0;",
					"ALTER TABLE " + DatabaseContents.TABLE_PRODUCT_CATALOG + " ADD COLUMN status TEXT DEFAULT 'ACTIVE';",
					"ALTER TABLE " + DatabaseContents.TABLE_SALE + " ADD COLUMN seller_name TEXT DEFAULT 'Owner';",
					"ALTER TABLE " + DatabaseContents.TABLE_SALE + " ADD COLUMN payment TEXT DEFAULT 'n/a';",
                    "ALTER TABLE " + DatabaseContents.TABLE_SALE + " ADD COLUMN payment_method TEXT DEFAULT 'n/a';",
					"ALTER TABLE " + DatabaseContents.TABLE_SALE + " ADD COLUMN total DOUBLE DEFAULT 0.0;",
					"ALTER TABLE " + DatabaseContents.TABLE_SALE + " ADD COLUMN orders INTEGER DEFAULT 0;",
					"ALTER TABLE " + DatabaseContents.TABLE_SALE_LINEITEM + " ADD COLUMN is_piece_sale INTEGER DEFAULT 0;",
					"ALTER TABLE " + DatabaseContents.TABLE_STAFF + " ADD COLUMN default_hours DOUBLE DEFAULT 12.0;",
					"ALTER TABLE " + DatabaseContents.TABLE_STAFF_WORK_LOG + " ADD COLUMN hours_worked DOUBLE DEFAULT 8.0;",
					"ALTER TABLE " + DatabaseContents.TABLE_FINANCE_TRANSACTION + " ADD COLUMN account TEXT DEFAULT 'CASH';",
					"ALTER TABLE " + DatabaseContents.TABLE_FINANCE_TRANSACTION + " ADD COLUMN type TEXT DEFAULT 'EXPENSE';",
					"ALTER TABLE " + DatabaseContents.TABLE_LOADING_TRANSACTION + " ADD COLUMN payment_method TEXT DEFAULT 'CASH';",
					"DROP TABLE IF EXISTS product_variation;"
				};
				
				for (String sql : repairs) {
					try { db.execSQL(sql); } catch (Exception e) {}
				}
				
				// 3. One-time deduplication of Finance Categories
				db.execSQL("DELETE FROM " + DatabaseContents.TABLE_FINANCE_CATEGORY + " WHERE _id NOT IN (SELECT MIN(_id) FROM " + DatabaseContents.TABLE_FINANCE_CATEGORY + " GROUP BY name, type);");

				// 4. Ensure default categories exist safely
				seedDefaultFinanceCategories(db);
				seedDefaultLoadingCategories(db);

				// 5. Ensure default capital keys exist
				db.execSQL("INSERT OR IGNORE INTO " + DatabaseContents.TABLE_CAPITAL + " (key, value) VALUES ('store_cash', 0.0);");
				db.execSQL("INSERT OR IGNORE INTO " + DatabaseContents.TABLE_CAPITAL + " (key, value) VALUES ('gcash_balance', 0.0);");

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean execute(String query) {
		database = this.getWritableDatabase();
		try {
			database.execSQL(query);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public List<Object> select(String query) {
		database = this.getReadableDatabase();
		Cursor cursor = database.rawQuery(query, null);
		List<Object> res = new ArrayList<Object>();
		while (cursor.moveToNext()) {
			ContentValues contentValues = new ContentValues();
			for (int i = 0; i < cursor.getColumnCount(); i++) {
				String colName = cursor.getColumnName(i);
				switch (cursor.getType(i)) {
				case Cursor.FIELD_TYPE_INTEGER:
					contentValues.put(colName, cursor.getInt(i));
					break;
				case Cursor.FIELD_TYPE_FLOAT:
					contentValues.put(colName, cursor.getDouble(i));
					break;
				case Cursor.FIELD_TYPE_STRING:
					contentValues.put(colName, cursor.getString(i));
					break;
				case Cursor.FIELD_TYPE_BLOB:
					contentValues.put(colName, cursor.getBlob(i));
					break;
				case Cursor.FIELD_TYPE_NULL:
					contentValues.putNull(colName);
					break;
				}
			}
			res.add(contentValues);
		}
		cursor.close();
		return res;
	}

	@Override
	public int insert(String table, Object contentValues) {
		database = this.getWritableDatabase();
		int id = (int) database.insert(table, null, (ContentValues) contentValues);
		return id;
	}

	@Override
	public boolean update(String table, Object contentValues) {
		database = this.getWritableDatabase();
		ContentValues cv = (ContentValues) contentValues;
		int row = database.update(table, cv, "_id = ?",
				new String[] { cv.get("_id") + "" });
		return row > 0;
	}

	@Override
	public boolean delete(String table, int id) {
		database = this.getWritableDatabase();
		int row = database.delete(table, "_id = ?", new String[] { id + "" });
		return row > 0;
	}

}
