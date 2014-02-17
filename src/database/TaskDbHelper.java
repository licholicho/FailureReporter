package database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class TaskDbHelper extends SQLiteOpenHelper {
	 public static final String DBNAME = "taskdb";
	    public static final int DBVERSION = 1;
	    public static final String TABLE_TASKS = "tasks";

	    public TaskDbHelper(Context context) {
	        super(context, DBNAME, null, DBVERSION);
	    }

	    @Override
	    public void onCreate(SQLiteDatabase db) {
	        Log.d("topics.database", "Creating new database...");
	        StringBuilder sqlBuilder = new StringBuilder();
	        sqlBuilder.append("CREATE TABLE ").append(TABLE_TASKS).append(" (");
	        sqlBuilder.append("_id INTEGER PRIMARY KEY, ");
	        sqlBuilder.append("title TEXT NOT NULL, ");
	        sqlBuilder.append("description TEXT, ");
	        sqlBuilder.append("reminder REAL, ");
	        sqlBuilder.append("latitude REAL, ");
	        sqlBuilder.append("longitude REAL, ");
	        sqlBuilder.append("place TEXT, ");
	        sqlBuilder.append("done INT");
	        sqlBuilder.append("b_date STRING");
	        sqlBuilder.append("e_date STRING");
	        sqlBuilder.append("photo_1 BLOB");
	        sqlBuilder.append("photo_2 BLOB");
	        sqlBuilder.append("photo_3 BLOB");
	        sqlBuilder.append(");");

	        try {
	            db.execSQL(sqlBuilder.toString());
	        } catch (SQLException ex) {
	            Log.e("topics.database", "Error creating application database.", ex);
	        }
	        Log.d("topics.database", "... database creation finished.");
	    }

	    @Override
	    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	        // No update so far
	    }

	    @Override
	    public void onOpen(SQLiteDatabase db) {
	        super.onOpen(db);
	    }
}
