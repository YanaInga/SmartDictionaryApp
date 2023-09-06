package com.dev.smartdictionary;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.dev.smartdictionary.DictionaryDbSchema.SetsTable;
import com.dev.smartdictionary.DictionaryDbSchema.WordsTable;

public class DictionaryDataBaseHelper extends SQLiteOpenHelper {

    private final static String DB_NAME = "SmartDictionaryNew";
    private final static int DB_VERSION = 1;

    DictionaryDataBaseHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + SetsTable.TableNAME + "(" +
         "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SetsTable.NAME + " TEXT UNIQUE NOT NULL, "
                + " CONSTRAINT " + SetsTable.name_check +
                " CHECK (" + SetsTable.NAME + " != '')" + ")");
        sqLiteDatabase.execSQL("CREATE TABLE " + WordsTable.TableNAME + "(" +
                        "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                WordsTable.setID + " INTEGER, " +
                WordsTable.englishWord + " TEXT NOT NULL, " +
                WordsTable.translatedWord + " TEXT NOT NULL, " +
                WordsTable.transcription + " TEXT, " +
                WordsTable.example + " TEXT, " +
                WordsTable.picture + " INTEGER, " +
                WordsTable.statistics + " REAL, " +
                WordsTable.isLearn + " INTEGER DEFAULT 0, " +
                "FOREIGN KEY (" + WordsTable.setID + ") REFERENCES " + SetsTable.TableNAME + " (_id) ON DELETE CASCADE)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
