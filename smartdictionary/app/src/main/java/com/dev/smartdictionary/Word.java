package com.dev.smartdictionary;

public class Word {
    private int id;
    private int setID;
    private String englishWord;
    private String translatedWord;
    private String transcription;
    private String example;
    private int picture;
    private double statistics;
    private int isLearn;

    public Word(int id, int setID, String englishWord, String translatedWord, String trascription, String example, int picture, double statistics, int isLearn) {
        this.id = id;
        this.setID = setID;
        this.englishWord = englishWord;
        this.translatedWord = translatedWord;
        this.transcription = trascription;
        this.example = example;
        this.picture = picture;
        this.statistics = statistics;
        this.isLearn = isLearn;
    }

    public Word(int setId) {
        id = 0;
        this.setID = setId;
        englishWord = "";
        translatedWord = "";
        transcription = "";
        example = "";
        picture = 0;
        statistics = 0;
        isLearn = 0;
    }

    public int getId() {
        return id;
    }

    public int getSetID() {
        return setID;
    }

    public void setSetID(int setID) {
        this.setID = setID;
    }

    public String getEnglishWord() {
        return englishWord;
    }

    public void setEnglishWord(String englishWord) {
        this.englishWord = englishWord;
    }

    public String getTranslatedWord() {
        return translatedWord;
    }

    public void setTranslatedWord(String translatedWord) {
        this.translatedWord = translatedWord;
    }

    public String getTranscription() {
        return transcription;
    }

    public void setTranscription(String transcription) {
        this.transcription = transcription;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public int getPicture() {
        return picture;
    }

    public void setPicture(int picture) {
        this.picture = picture;
    }

    public double getStatistics() {
        return statistics;
    }

    public void setStatistics(double statistics) {
        this.statistics = statistics;
    }

    public int isLearn() {
        return isLearn;
    }

    public void setLearn(int learn) {
        isLearn = learn;
    }
}
