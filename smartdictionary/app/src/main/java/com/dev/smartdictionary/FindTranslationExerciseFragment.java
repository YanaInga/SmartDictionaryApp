package com.dev.smartdictionary;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class FindTranslationExerciseFragment extends Fragment {

    Toolbar toolbar;
    NavController navController;
    AppBarConfiguration appBarConfiguration;
    ListView variantsList;
    TextView wordView;
    int trueAnswer;
    int wordId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        navController = Navigation.findNavController(view);
        appBarConfiguration =
                new AppBarConfiguration.Builder(navController.getGraph()).build();
        toolbar = view.findViewById(R.id.toolbar_exercise_translation);
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);
        Integer exerciseType = getArguments().getInt("exercise_type");
        Integer setId = getArguments().getInt("set_Id");
        Integer wordsType = getArguments().getInt("words_type");
        Integer wordsCount = getArguments().getInt("words_count");
        List<Word> words = getExerciseList(setId, wordsType, wordsCount);
        if (words.isEmpty()) {
            Toast.makeText(getActivity(), "Недостаточно слов для тренировки", Toast.LENGTH_SHORT).show();
            navController.popBackStack();
        }
        else
            searchTranslationExercise(words, exerciseType);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_find_translation_exercise, container, false);
        variantsList = view.findViewById(R.id.list_words_vatiants);
        wordView = view.findViewById(R.id.word_label);
        return view;
    }

    private void searchTranslationExercise(List<Word> words, Integer exerciseType) {
        DBLab dbLab = DBLab.get(getActivity());
        final Integer[] number = {0};
        NextWord(number[0], words, exerciseType);
        variantsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Word word = dbLab.findWord(wordId);
                if (i == trueAnswer){
                    word.setStatistics(word.getStatistics() + 4);
                    if(word.getStatistics() >= 100)
                        word.setLearn(1);
                    dbLab.changeWord(word);
                    number[0] = number[0] + 1;
                    if(number[0] == words.size())
                        navController.popBackStack();
                    else
                        NextWord(number[0], words, exerciseType);
                } else {
                    word.setStatistics(word.getStatistics() - 4);
                    dbLab.changeWord(word);
                    Toast.makeText(getActivity(), "Неверный ответ!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void NextWord(Integer number, List<Word> words, Integer exerciseType) {
        final Random random = new Random();
        if(exerciseType == 0)
            wordView.setText(words.get(number).getEnglishWord());
        else {
            String translations = words.get(number).getTranslatedWord();
            wordView.setText(translations.substring(0, translations.length()-1).replaceAll(";", ", "));
        }
        ArrayList<String> variants = new ArrayList<String>();
        int count = 4;
        if(words.size() < 4)
            count = words.size();
        trueAnswer = random.nextInt(count);
        ArrayList<Integer> q = new ArrayList<>();
        q.add(number);
        do {
            int j = random.nextInt(words.size());
            if(!q.contains(j))
                q.add(j);
        }while (q.size() != count);
        int index = 1;
        for (int j = 0; j < count; j++) {
            if(exerciseType == 0) {
                if (j != trueAnswer) {
                    String translations = words.get(q.get(index)).getTranslatedWord();
                    variants.add(translations.substring(0, translations.length() - 1).replaceAll(";", ", "));
                    index = index + 1;
                } else {
                    wordId = words.get(number).getId();
                    String translations = words.get(number).getTranslatedWord();
                    variants.add(translations.substring(0, translations.length() - 1).replaceAll(";", ", "));
                }
            } else {
                if (j != trueAnswer) {
                    variants.add(words.get(q.get(index)).getEnglishWord());
                    index = index + 1;
                } else {
                    wordId = words.get(number).getId();
                    variants.add(words.get(number).getEnglishWord());
                }
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                R.layout.listview_custom, variants);
        adapter.notifyDataSetChanged();
        variantsList.setAdapter(adapter);
    }

    private List<Word> getExerciseList(Integer setId, Integer wordsType, Integer wordsCount) {
        DBLab dbLab = DBLab.get(getActivity());
        List<Word> words;
        if (setId != 0){
            if (wordsType != 2) {
                words = dbLab.getWordsBySetIdAndIsLearn(setId, wordsType);
            } else {
                words = dbLab.getWords(setId);
            }
        } else {
            words = dbLab.getAllWords();
        }
        if(wordsCount != 0){
            if (words.size() > wordsCount)
                words = words.subList(0, wordsCount);
        }
        return words;
    }
}