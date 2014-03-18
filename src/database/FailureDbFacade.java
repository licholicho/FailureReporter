package database;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import failure.Failure;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class FailureDbFacade {
	private SQLiteDatabase db;

    public FailureDbFacade(SQLiteDatabase db) {
        this.db = db;
    }

    public void dispose() {
        if (db != null && db.isOpen()) {
            db.close();
        }
        db = null;
    }

    public void insert(Failure f) {
        validate();
        ContentValues v = new ContentValues();
        v.put("title", f.getTitle());
        v.put("description", f.getDescription());
        v.put("latitude", f.getLatitude());
        v.put("longitude", f.getLongitude());
        v.put("place", f.getNameOfPlace());
        v.put("done", 0);
        v.put("b_date", f.getBeginDateInString());
        v.put("e_date", f.getEndDateInString());
        if (!f.getPhotos().isEmpty()) {
        if (f.getPhotos().size() > 0)
        	v.put("photo_1", f.getPhotoInBytes(0));
        if (f.getPhotos().size() > 1)
            v.put("photo_2", f.getPhotoInBytes(1));
        if (f.getPhotos().size() > 2)
            v.put("photo_3", f.getPhotoInBytes(2));
        }
        long id = db.insert(FailureDbHelper.TABLE_REPORTS, null, v);
        if (id >= 0) {
            f.setId(id);
        }
    }

    public boolean update(Failure f) {
        validate();
        ContentValues v = new ContentValues();
        v.put("title", f.getTitle());
        v.put("description", f.getDescription());
        v.put("latitude", f.getLatitude());
        v.put("longitude", f.getLongitude());
        v.put("place", f.getNameOfPlace());
        v.put("done", f.done());
        v.put("b_date", f.getBeginDateInString());
        v.put("e_date", f.getEndDateInString());
        if (!f.getPhotos().isEmpty()) {
        if (f.getPhotos().size() > 0)
        v.put("photo_1", f.getPhotoInBytes(0));
        if (f.getPhotos().size() > 1)
            v.put("photo_2", f.getPhotoInBytes(1));
        if (f.getPhotos().size() > 2)
            v.put("photo_3", f.getPhotoInBytes(2));
        }
        
        int rowsAffected = db.update(FailureDbHelper.TABLE_REPORTS, v, "_id="
               + f.getId(), null);

        return (rowsAffected == 1);
    }

    public boolean delete(Failure f) {
        return delete(f.getId());
    }

    public boolean delete(long id) {
    	Log.i("lol","usuwa");
        validate();
        return (1 == db
                .delete(FailureDbHelper.TABLE_REPORTS, "_id=" + id, null));
    }

    public Failure getById(long id) {
        validate();
        Cursor cur = null;
        try {
            cur = db.query(true, FailureDbHelper.TABLE_REPORTS, null /* all */,
                    "_id=" + id, null, null, null, null, null);
            List<Failure> tmpList = new LinkedList<Failure>();
            extractReportsFromCursor(tmpList, cur);
            if (tmpList != null && !tmpList.isEmpty()) {
                return tmpList.get(0);
            } else {
                return null;
            }
        } catch (SQLException e) {
            Log.e("topics.database", "Error searching application database.", e);
            return null;
        } finally {
            if (cur != null && !cur.isClosed()) {
                cur.close();
            }
        }
    }

    public List<Failure> findByTitle(String title) {
        validate();
        List<Failure> result = new LinkedList<Failure>();
        Cursor cur = null;
        try {
            cur = db.query(true, FailureDbHelper.TABLE_REPORTS, null /* all */,
                    "title='" + title + "'", null, null, null, "title", null);
            extractReportsFromCursor(result, cur);
        } catch (SQLException e) {
            Log.e("topics.database", "Error searching application database.", e);
        } finally {
            if (cur != null && !cur.isClosed()) {
                cur.close();
            }
        }

        return Collections.unmodifiableList(result);
    }

    public List<Failure> listAll() {
        validate();
        List<Failure> result = new LinkedList<Failure>();
        Cursor cur = null;
        try {
            cur = db.query(true, FailureDbHelper.TABLE_REPORTS, null /* all */,
            		"done = 0", null, null, null, "title", null);
            extractReportsFromCursor(result, cur);
        } catch (SQLException e) {
            Log.e("topics.database", "Error searching application database.", e);
        } finally {
            if (cur != null && !cur.isClosed()) {
                cur.close();
            }
        }

        return Collections.unmodifiableList(result);
    }

    public List<Failure> listAllDone() {
        validate();
        List<Failure> result = new LinkedList<Failure>();
        Cursor cur = null;
        try {
            cur = db.query(true, FailureDbHelper.TABLE_REPORTS, null/* all */,
            		"done = 1", null, null, null, "title", null);
            extractReportsFromCursor(result, cur);
        } catch (SQLException e) {
            Log.e("topics.database", "Error searching application database.", e);
        } finally {
            if (cur != null && !cur.isClosed()) {
                cur.close();
            }
        }

        return Collections.unmodifiableList(result);
    }
    
    public List<Failure> listAllSortedBy(String field, int done) {
        validate();
        List<Failure> result = new LinkedList<Failure>();
        Cursor cur = null;
        try {
            cur = db.query(true, FailureDbHelper.TABLE_REPORTS, null /* all */,
            		"done = " + String.valueOf(done), null, null, null, field + " desc", null);
            extractReportsFromCursor(result, cur);
        } catch (SQLException e) {
            Log.e("topics.database", "Error searching application database.", e);
        } finally {
            if (cur != null && !cur.isClosed()) {
                cur.close();
            }
        }

        return Collections.unmodifiableList(result);
    }
    
    public Cursor getCursorForAllReports() {
        validate();
        Cursor cur = null;
        try {
            cur = db.query(true, FailureDbHelper.TABLE_REPORTS, null /* all */,
                    null, null, null, null, "title", null);
        } catch (SQLException e) {
            Log.e("topics.database", "Error searching application database.", e);
            cur = null;
        }
        return cur;
    }

    private void extractReportsFromCursor(List<Failure> list, Cursor cur) {
        if (cur.moveToFirst()) {
            for (int i = 0; i < cur.getCount(); i++) {
            	Failure a = new Failure();
                a.setId(cur.getLong(0));
                a.setTitle(cur.getString(1));
                a.setDescription(cur.getString(2));
                a.setLatitude(cur.getFloat(4));
                a.setLongitude(cur.getFloat(5));
                a.setNameOfPlace(cur.getString(6));
                a.setDone(cur.getInt(7));
                a.setBeginDate(cur.getString(8));
                a.setEndDate(cur.getString(9));
                if (cur.getBlob(10) != null)
                	a.addPhoto(0, cur.getBlob(10));
                if (cur.getBlob(11) != null)
                	a.addPhoto(1, cur.getBlob(11));
                if (cur.getBlob(12) != null)
                	a.addPhoto(2, cur.getBlob(12));
                	
                list.add(a);

                cur.moveToNext();
            }
        }
    }

    private void validate() {
        if (db == null) {
            throw new IllegalStateException(
                    "Illegal access to the disposed MovieDbHelper object.");
        }
    }
}
