package com.dev.smartdictionary;

public class DictionaryDbSchema {
    public static final class SetsTable {
        public static final String TableNAME = "sets";
        public static final String NAME = "name";
        public static final String name_check = "namecheck";
    }
    public static final class WordsTable {
        public static final String TableNAME = "words";
        public static final String setID = "set_id";
        public static final String englishWord = "original_word";
        public static final String translatedWord = "translation";
        public static final String transcription = "transcription";
        public static final String example = "example";
        public static final String picture = "picture";
        public static final String statistics = "statistics";
        public static final String isLearn = "islearn";
    }
}
