package com.dev.smartdictionary;

import java.util.ArrayList;
import java.util.List;

public class AddWords {

    String word;
    int repeats;
    String level;
    boolean isChecked;
    static List<AddWords> words;

    public AddWords() {
        this.setChecked(false);
    }

    public static List<AddWords> getWords() {

        return words;
    }

    public AddWords(String listOfWords) {
        words = new ArrayList<>();
        String[] _words = listOfWords.split("\\^");
        for (int i = 0; i < _words.length; i++) {
            String[] word = _words[i].split(":");
            AddWords addWords = new AddWords();
            addWords.setWord(word[0]);
            addWords.setRepeats(Integer.parseInt(word[1]));
            switch (Integer.parseInt(word[2])) {
                case 0:
                    addWords.setLevel("Начальный");
                    break;
                case 1:
                    addWords.setLevel("Средний");
                    break;
                case 2:
                    addWords.setLevel("Продвинутый");
                    break;
            }
            words.add(addWords);
        }
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getRepeats() {
        return repeats;
    }

    public void setRepeats(int repeats) {
        this.repeats = repeats;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public boolean getChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }


}
