package com.dev.smartdictionary;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.dev.smartdictionary.DictionaryDbSchema.WordsTable;

import java.util.ArrayList;
import java.util.List;

public class DBLab {

    private static DBLab dbLab;
    private Context context;
    private SQLiteDatabase database;


    private DBLab(Context _context){
        context = _context.getApplicationContext();
        database = new DictionaryDataBaseHelper(context).getWritableDatabase();
        Log.d(MainActivity.TAG, "Create DbLab");
    }

    public static DBLab get(Context context){
        if(dbLab == null){
            dbLab = new DBLab(context);
        }
        return dbLab;
    }

    private ContentValues putInfo(Word word){
        ContentValues wordValues = new ContentValues();
        wordValues.put(WordsTable.setID, word.getSetID());
        wordValues.put(WordsTable.englishWord, word.getEnglishWord());
        wordValues.put(WordsTable.translatedWord, word.getTranslatedWord());
        wordValues.put(WordsTable.transcription, word.getTranscription());
        wordValues.put(WordsTable.example, word.getExample());
        wordValues.put(WordsTable.statistics, word.getStatistics());
        wordValues.put(WordsTable.isLearn, word.isLearn());
        return wordValues;
    }

    public void deleteWord(int id) {
        database.delete(WordsTable.TableNAME,
                "_id = ?",
                new String[]{String.valueOf(id)});
    }
    public void deleteSet(int id) {
        database.delete(DictionaryDbSchema.SetsTable.TableNAME,
                "_id = ?",
                new String[]{String.valueOf(id)});
    }

    public void addWord(Word word){
        database.insert(WordsTable.TableNAME, null, putInfo(word));
        Log.d(MainActivity.TAG, "AddWord " + word.getSetID());
    }

    public void changeWord(Word word) {
        database.update(WordsTable.TableNAME, putInfo(word),
                "_id = ?",
                new String[] {String.valueOf(word.getId())});
    }

    public boolean addSet(String name) {
        ContentValues setValues = new ContentValues();
        setValues.put(DictionaryDbSchema.SetsTable.NAME, name);
        database.insert(DictionaryDbSchema.SetsTable.TableNAME, null, setValues);
        return true;
    }

    public Word findWord(int id){
        Cursor cursor = database.query(WordsTable.TableNAME,
                null,
                "_id = ?",
                new String[]{String.valueOf(id)},
                null, null, null);
        cursor.moveToFirst();
        Word word = new Word(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getString(3),
                cursor.getString(4), cursor.getString(5), cursor.getInt(6), cursor.getDouble(7), cursor.getInt(8));
        return word;
    }

    public int findSet(String setName){
        Cursor cursor = database.query(DictionaryDbSchema.SetsTable.TableNAME,
                null,
                DictionaryDbSchema.SetsTable.NAME + " = ?",
                 new String[]{setName},
                 null, null, null);
        cursor.moveToFirst();
        return cursor.getInt(0);
    }

    public List<Word> getWords(int setid){
        List<Word> words = new ArrayList<Word>();
    Cursor cursor = database.query(WordsTable.TableNAME,
               null, WordsTable.setID + " = ?",
                new String[]{String.valueOf(setid)}, null, null, null);
        try{
            cursor.moveToFirst();
            while (!cursor.isAfterLast()){
                Word word = new Word(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getString(3),
                        cursor.getString(4), cursor.getString(5), cursor.getInt(6), cursor.getDouble(7), cursor.getInt(8));
                words.add(word);
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return words;
    }
    public List<Word> getWordsBySetIdAndIsLearn(int setid, int islearn){
        List<Word> words = new ArrayList<Word>();
        Cursor cursor = database.query(WordsTable.TableNAME,
                null, WordsTable.setID + " = ? AND " + WordsTable.isLearn + " = ?",
                new String[]{String.valueOf(setid), String.valueOf(islearn)}, null, null, null);
        try{
            cursor.moveToFirst();
            while (!cursor.isAfterLast()){
                Word word = new Word(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getString(3),
                        cursor.getString(4), cursor.getString(5), cursor.getInt(6), cursor.getDouble(7), cursor.getInt(8));
                words.add(word);
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return words;
    }

    public List<Word> getAllWords(){
        List<Word> words = new ArrayList<Word>();
        Cursor cursor = database.query(WordsTable.TableNAME,
                null, null,
                null, null, null, null);
        try{
            cursor.moveToFirst();
            while (!cursor.isAfterLast()){
                Word word = new Word(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getString(3),
                        cursor.getString(4), cursor.getString(5), cursor.getInt(6), cursor.getDouble(7), cursor.getInt(8));
                words.add(word);
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return words;
    }

    public List<Set> getSets() {
        List<Set> sets = new ArrayList<Set>();
        Cursor cursor = database.query(DictionaryDbSchema.SetsTable.TableNAME,
                null, null, null, null, null, null);
        try{
            cursor.moveToFirst();
            while (!cursor.isAfterLast()){
              int id = cursor.getInt(0);
              String name = cursor.getString(1);
              Set set = new Set(id, name);
              sets.add(set);
              cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return sets;
    }
}
