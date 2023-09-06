package com.dev.smartdictionary;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class SetFragment extends Fragment implements CreateSetDialogFragment.OkListener {

    private int setId;
    private static final int REQUEST_CODE_SAVE = 1;
    private FloatingActionButton addButton;
    private RecyclerView wordsRecyclerView;
    private WordsAdapter wordsAdapter;
    Spinner spinnerSets;
    Toolbar toolbar;
    NavController navController;
    AppBarConfiguration appBarConfiguration;
    ArrayList<String> titles;

    public static final String TAG = "MainActivity";
    private static final String DIALOG_NEW_SET = "DialogNewSet";

//    @Override
//    public void onViewCreated(@NonNull View view,
//                              @Nullable Bundle savedInstanceState) {
//        navController = Navigation.findNavController(view);
//        appBarConfiguration =
//                new AppBarConfiguration.Builder(navController.getGraph()).build();
//        toolbar = view.findViewById(R.id.toolbar_set);
//        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);

//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setId = 1;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_set, container, false);
        wordsRecyclerView = view.findViewById(R.id.words_recycler_view);
        wordsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        navController = NavHostFragment.findNavController(this);
        registerForContextMenu(wordsRecyclerView);
        spinnerSets = view.findViewById(R.id.spinner_sets);
        updateSets();
        AdapterView.OnItemSelectedListener itemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        CreateSetDialogFragment dialog = new CreateSetDialogFragment();
                        dialog.show(getChildFragmentManager(), DIALOG_NEW_SET);
                        addButton.setVisibility(View.GONE);
                        break;
                    case 1:
                        updateUI(true);
                        addButton.setVisibility(View.GONE);
                        break;
                    default:
                        DBLab dbLab = DBLab.get(getActivity());
                        setId = dbLab.findSet((String) adapterView.getItemAtPosition(i));
                        updateUI(false);
                        addButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        };
        spinnerSets.setOnItemSelectedListener(itemSelectedListener);
        updateUI(true);
        return view;
    }

    private void updateSets() {
        DBLab dbLab = DBLab.get(getActivity());
        List<Set> sets = dbLab.getSets();
        titles = new ArrayList<String>();
        titles.add("Добавить новый набор");
        titles.add("Все наборы");
        for(int i=0; i< sets.size();i++) {
            titles.add(sets.get(i).getName());
        }
        CustomAdapter adapter = new CustomAdapter(getActivity(), R.layout.spinner_custom, titles);
        spinnerSets.setAdapter(adapter);
        spinnerSets.setSelection(1, true);
    }

    private void updateUI(boolean b) {
        DBLab dbLab = DBLab.get(getActivity());
        List<Word> words;
        if (!b) {
            words = dbLab.getWords(setId);
        }
        else {
            words = dbLab.getAllWords();
        }
        wordsAdapter = new WordsAdapter(words);
        wordsRecyclerView.setAdapter(wordsAdapter);
    }

    @Override
    public void dialogResult(String name){
        if (name.equals(""))
        {
            Toast.makeText(getActivity(), "Набор не был создан", Toast.LENGTH_SHORT).show();
            return;
        }
        DBLab dbLab = DBLab.get(getActivity());
        boolean b = dbLab.addSet(name);
        if (b){
            updateSets();
        }
    }

    public class CustomAdapter extends ArrayAdapter<String> {

        public CustomAdapter(Context context, int textViewResourceId,
                               ArrayList<String> objects) {
            super(context, textViewResourceId, objects);
        }

        @Override
        public View getDropDownView(int position, View convertView,
                                    ViewGroup parent) {

            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            return getCustomView(position, convertView, parent);
        }

        public View getCustomView(int position, View convertView,
                                  ViewGroup parent) {

            LayoutInflater inflater = getLayoutInflater();
            View row = inflater.inflate(R.layout.spinner_custom, parent, false);
            TextView label = (TextView) row.findViewById(R.id.sets_textView);
            label.setText(titles.get(position));

            ImageView icon = (ImageView) row.findViewById(R.id.icon_delete);

            if (position > 1) {
                icon.setImageResource(R.drawable.delete_forever);
                icon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.delete_set);
                builder.setMessage(R.string.dialog_about_message);
                builder.setCancelable(true);
                builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                dialog.dismiss();
                            }
                        });
                        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DBLab dbLab = DBLab.get(getActivity());
                                setId = dbLab.findSet(String.valueOf(spinnerSets.getItemAtPosition(position)));
                                dbLab.deleteSet(setId);
                                updateSets();
                                updateUI(true);
                                dialog.dismiss(); // Отпускает диалоговое окно

                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
                });
            }
            return row;
        }
    }

    public class WordsHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener {

        private TextView originalWord;
        private TextView transcription;
        private TextView translatedWord;
        private ImageView wordIsLearnt;
        private TextView statisticsResult;
        private Word word;

        public WordsHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.word_item,
                    parent, false));
            originalWord = (TextView) itemView.findViewById(R.id.originalView);
            transcription = (TextView) itemView.findViewById(R.id.transcriptionView);
            translatedWord = (TextView) itemView.findViewById(R.id.translationView);
            wordIsLearnt = (ImageView)itemView.findViewById(R.id.word_learnt);
            statisticsResult = itemView.findViewById(R.id.statistics);
            itemView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);
        }

        public void bind(Word word){
            this.word = word;
            originalWord.setText(this.word.getEnglishWord());
            if(!this.word.getTranscription().equals(""))
                transcription.setText("[" + this.word.getTranscription() + "]");
            if(!this.word.getTranslatedWord().equals(""))
                translatedWord.setText(this.word.getTranslatedWord().substring(0, this.word.getTranslatedWord().length() - 1).replaceAll(";", ", "));
            if(word.isLearn() == 0){
                wordIsLearnt.setVisibility(View.GONE);
                statisticsResult.setVisibility(View.VISIBLE);
                statisticsResult.setText(String.valueOf((int)word.getStatistics()) + "%");
            } else{
                wordIsLearnt.setVisibility(View.VISIBLE);
                statisticsResult.setVisibility(View.GONE);
            }

        }

        @Override
        public void onClick(View view) {
            Bundle bundle = new Bundle();
            bundle.putInt("word_id", word.getId());
            bundle.putBoolean("is_word_new", false);
            navController.navigate(R.id.wordPageFragment, bundle);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                                        ContextMenu.ContextMenuInfo menuInfo) {
            MenuItem Delete = menu.add(Menu.NONE, 1,1, "Удалить слово");
            Delete.setOnMenuItemClickListener(onDeleteMenu);
        }

        private final MenuItem.OnMenuItemClickListener onDeleteMenu = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case 1:
                        DBLab dbLab = DBLab.get(getActivity());
                        dbLab.deleteWord(word.getId());
                        updateUI(true);
                        spinnerSets.setSelection(1);
                        break;
                }
                return true;
            }
        };

    }

    public class WordsAdapter extends RecyclerView.Adapter<WordsHolder>{

        private List<Word> words;
        private int position;

        public WordsAdapter(List<Word> _words){
            words = _words;
        }

        @NonNull
        @Override
        public WordsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new WordsHolder(layoutInflater, parent);
        }

        public void setPosition(int position) {
            this.position = position;
        }


        @Override
        public void onBindViewHolder(@NonNull WordsHolder holder, int position) {
            Word word = words.get(position);
            holder.bind(word);

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    setPosition(holder.getAdapterPosition());
                    return false;
                }
            });
        }

        @Override
        public void onViewRecycled(WordsHolder holder){
            holder.itemView.setOnLongClickListener(null);
            super.onViewRecycled(holder);
        }

        @Override
        public int getItemCount() {
            return words.size();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        View view = getView();
        if (view != null) {
            addButton = view.findViewById(R.id.floatingAddButton);
            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("word_id", setId);
                    bundle.putBoolean("is_word_new", true);
                    navController.navigate(R.id.wordPageFragment, bundle);
                }
            });
        }

    }

    public void setSetId(int setId) {
        this.setId = setId;
    }
}