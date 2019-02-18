package com.example.minhtuyen.mushroomcontroller;

import java.security.Key;
import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteDatabaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "MushroomCondition";
    private static final String TABLE_NAME = "Conditions";
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_TEMP = "temperature";
    private static final String KEY_HUMI = "humidity";
    private static final String KEY_LUX = "lux";
    private static final String KEY_R = "R";
    private static final String KEY_G = "G";
    private static final String KEY_B = "B";
    private static final String[] COLUMNS = { KEY_ID, KEY_NAME, KEY_TEMP,
            KEY_HUMI,KEY_LUX,KEY_R,KEY_G,KEY_B };

    public SQLiteDatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATION_TABLE = "CREATE TABLE Conditions("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, " + "name TEXT, "
                + "temperature REAL, " + "humidity REAL, " + "lux INTEGER, "+ "R INTEGER, "
                + "G INTEGER, " + "B INTEGER)";
        db.execSQL(CREATION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // you can implement here migration process
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        this.onCreate(db);
    }

    public void deleteOne(Condition con) {
        // Get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, "id = ?", new String[] { String.valueOf(con.getId()) });
        db.close();
    }

    public Condition getCondition(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, // a. table
                COLUMNS, // b. column names
                " id = ?", // c. selections
                new String[] { String.valueOf(id) }, // d. selections args
                null, // e. group by
                null, // f. having
                null, // g. order by
                null); // h. limit

        if (cursor != null)
            cursor.moveToFirst();

        Condition condition = new Condition("");
        condition.setId(Integer.parseInt(cursor.getString(0)));
        condition.setName(cursor.getString(1));
        condition.setTemp(Float.parseFloat(cursor.getString(2)));
        condition.setHumi(Float.parseFloat(cursor.getString(3)));
        condition.setLux(Integer.parseInt(cursor.getString(4)));
        condition.setR(Integer.parseInt(cursor.getString(5)));
        condition.setG(Integer.parseInt(cursor.getString(6)));
        condition.setB(Integer.parseInt(cursor.getString(7)));
        return condition;
    }

    public List<Condition> allConditions() {

        List<Condition> conditions = new LinkedList<Condition>();
        String query = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        Condition player = null;

        if (cursor.moveToFirst()) {
            do {
                Condition condition = new Condition("");
                condition.setId(Integer.parseInt(cursor.getString(0)));
                condition.setName(cursor.getString(1));
                condition.setTemp(Float.parseFloat(cursor.getString(2)));
                condition.setHumi(Float.parseFloat(cursor.getString(3)));
                condition.setLux(Integer.parseInt(cursor.getString(4)));
                condition.setR(Integer.parseInt(cursor.getString(5)));
                condition.setG(Integer.parseInt(cursor.getString(6)));
                condition.setB(Integer.parseInt(cursor.getString(7)));
                conditions.add(condition);
            } while (cursor.moveToNext());
        }

        return conditions;
    }

    public void addCondition(Condition condition) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, condition.getName());
        values.put(KEY_TEMP, condition.getTemp());
        values.put(KEY_HUMI, condition.getHumi());
        values.put(KEY_LUX, condition.getLux());
        values.put(KEY_R, condition.getR());
        values.put(KEY_G, condition.getG());
        values.put(KEY_B, condition.getB());
        // insert
        db.insert(TABLE_NAME,null, values);
        db.close();
    }

    public int updateCondition(Condition condition) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, condition.getName());
        values.put(KEY_TEMP, condition.getTemp());
        values.put(KEY_HUMI, condition.getHumi());
        values.put(KEY_LUX, condition.getLux());
        values.put(KEY_R, condition.getR());
        values.put(KEY_G, condition.getG());
        values.put(KEY_B, condition.getB());

        int i = db.update(TABLE_NAME, // table
                values, // column/value
                "id = ?", // selections
                new String[] { String.valueOf(condition.getId()) });

        db.close();

        return i;
    }
}
