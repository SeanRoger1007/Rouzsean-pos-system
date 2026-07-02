package com.refresh.pos.domain;

import java.util.List;

import android.content.ContentValues;

import com.refresh.pos.techicalservices.Database;
import com.refresh.pos.techicalservices.DatabaseContents;

/**
 * Saves and loads language preference from database.
 * 
 * @author Refresh Team
 *
 */
public class LanguageController {
	
	private static final String DEFAULT_LANGUAGE = "en";
	private static Database database;
	private static LanguageController instance;
	
	private LanguageController() {
		
	}
	
	public static LanguageController getInstance() {
		if (instance == null)
			instance = new LanguageController();
		return instance;
	}
	
	/**
	 * Sets database for use in this class.
	 * @param db database. 
	 */
	public static void setDatabase(Database db) {
		database = db;
	}
	
	/**
	 * Sets language for use in application.
	 * @param localeString local string of country.
	 */
	public void setLanguage(String localeString) {
		// Only English is supported now.
	}
	
	/**
	 * Returns current language. 
	 * @return current language.
	 */
	public String getLanguage() {
		return DEFAULT_LANGUAGE;
	}

}
