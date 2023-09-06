package com.dev.smartdictionary;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

public class ExercisesFragment extends Fragment {

    Toolbar toolbar;
    NavController navController;
    AppBarConfiguration appBarConfiguration;
    Spinner spinnerSets;
    ArrayList<String> titles;
    Integer setId;
    Integer wordIsLearn;
    Integer countMax;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wordIsLearn = 0;
        countMax = 0;

    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        navController = Navigation.findNavController(view);
//        appBarConfiguration =
//                new AppBarConfiguration.Builder(navController.getGraph()).build();
//        toolbar = view.findViewById(R.id.toolbar_exercises);
//        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exercises, container, false);
        spinnerSets = view.findViewById(R.id.spinner_set_type);
        titles = updateSets();
        Log.d(MainActivity.TAG, "Get");
        ArrayAdapter<String> adapter = new ArrayAdapter(view.getContext(), android.R.layout.simple_spinner_item, titles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSets.setAdapter(adapter);
        spinnerSets.setSelection(0, true);
        spinnerSets.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                DBLab dbLab = DBLab.get(getActivity());
                setId = dbLab.findSet((String) spinnerSets.getItemAtPosition(i));;
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {            }
        });
        Spinner wordsLearnType = view.findViewById(R.id.spinner_word_types);
        wordsLearnType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                wordIsLearn = i;
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {            }
        });
        Spinner spinnerCountMax = view.findViewById(R.id.spinner_count_type);
        spinnerCountMax.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i > 0)
                    countMax = Integer.parseInt(String.valueOf(spinnerCountMax.getItemAtPosition(i)));
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {           }
        });
        ListView listView = view.findViewById(R.id.list_exercises);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Bundle bundle = new Bundle();
                bundle.putInt("set_Id", setId);
                bundle.putInt("words_type", wordIsLearn);
                bundle.putInt("words_count", countMax);
                if (i == 2) {
                    navController.navigate(R.id.writeWordExerciseFragment, bundle);
                } else {
                    bundle.putInt("exercise_type", i);
                    navController.navigate(R.id.findTranslationExerciseFragment, bundle);
                }
            }
        });
        return view;
    }

    private ArrayList<String> updateSets() {
        DBLab dbLab = DBLab.get(getActivity());
        List<Set> sets_ = dbLab.getSets();
        ArrayList<String> sets = new ArrayList<String>();
        sets.add("Все");
        for(int i=0; i< sets_.size();i++) {
            sets.add(sets_.get(i).getName());
        }
        setId = 0;
        return sets;
    }
}