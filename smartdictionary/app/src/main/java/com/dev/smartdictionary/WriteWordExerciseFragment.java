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

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.Objects;


public class WriteWordExerciseFragment extends Fragment {

    Toolbar toolbar;
    NavController navController;
    AppBarConfiguration appBarConfiguration;
    TextView exampleTextView;
    TextInputEditText inputText;
    ImageView questionMark;
    String answer;
    Integer wordId;

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
        toolbar = view.findViewById(R.id.toolbar_write_exercise);
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);
        Integer setId = getArguments().getInt("set_Id");
        Integer wordsType = getArguments().getInt("words_type");
        Integer wordsCount = getArguments().getInt("words_count");
        List<Word> words = getExerciseList(setId, wordsType, wordsCount);
        if (words.isEmpty()) {
            Toast.makeText(getActivity(), "Недостаточно слов для тренировки", Toast.LENGTH_SHORT).show();
            navController.popBackStack();
        }
        else
            searchTranslationExercise(words);
    }

    private void searchTranslationExercise(List<Word> words) {
        DBLab dbLab = DBLab.get(getActivity());
        final Integer[] number = {0};
        NextWord(number[0], words);
        inputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Word word = dbLab.findWord(wordId);
                if (editable.toString().equals(answer)){
                    word.setStatistics(word.getStatistics() + 8);
                    if(word.getStatistics() >= 100)
                        word.setLearn(1);
                    dbLab.changeWord(word);
                    number[0] = number[0] + 1;
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(number[0] == words.size())
                                navController.popBackStack();
                            else
                                NextWord(number[0], words);
                        }
                    }, 500);
                }
            }
        });
        questionMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Word word = dbLab.findWord(wordId);
                word.setStatistics(word.getStatistics() - 1);
                dbLab.changeWord(word);
                char[] answerArray = answer.toCharArray();
                if(inputText.getText().toString().equals("")) {
                    inputText.setText(String.valueOf(answerArray[0]));
                } else{
                    String input = inputText.getText().toString();
                    char[] inputArray = input.toCharArray();
//                    if (inputArray[0] != answerArray[0])
//                        inputText.setText(String.valueOf(answerArray[0]));
                    for (int i = 0; i < input.length(); i++) {
                        if (inputArray[i] != answerArray[i]) {
                            String substr = input.substring(0, i);
                            inputText.setText(substr + String.valueOf(answerArray[substr.length()]));
                            break;
                        } else{
                            inputText.setText(input + answerArray[i+1]);
                        }
                    }
                }
            }
        });
    }

    private void NextWord(Integer number, List<Word> words) {
        String translations = words.get(number).getTranslatedWord();
        inputText.setText("");
        exampleTextView.setText(translations.substring(0, translations.length()-1).replaceAll(";", ", "));
        answer = words.get(number).getEnglishWord();
        wordId = words.get(number).getId();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_write_word_exercise, container, false);
        exampleTextView = view.findViewById(R.id.example_textView);
        inputText = view.findViewById(R.id.input_text);
        questionMark =view.findViewById(R.id.question_image);
        return view;
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