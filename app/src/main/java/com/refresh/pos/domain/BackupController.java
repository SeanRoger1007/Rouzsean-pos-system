package com.refresh.pos.domain;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class BackupController {

    private static final String DATABASE_NAME = "POS.db";

    public static boolean exportDatabase(Context context, Uri destUri) {
        try {
            File dbFile = context.getDatabasePath(DATABASE_NAME);
            if (!dbFile.exists()) {
                Log.e("BackupController", "Database file not found");
                return false;
            }

            InputStream in = new FileInputStream(dbFile);
            OutputStream out = context.getContentResolver().openOutputStream(destUri);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean importDatabase(Context context, Uri sourceUri) {
        try {
            File dbFile = context.getDatabasePath(DATABASE_NAME);
            
            // Close database before overwriting (Handled by restart in UI)
            InputStream in = context.getContentResolver().openInputStream(sourceUri);
            OutputStream out = new FileOutputStream(dbFile);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
