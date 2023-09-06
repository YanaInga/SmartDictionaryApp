package com.dev.smartdictionary;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

public class AddWordsFromTextFragment extends Fragment {

    RecyclerView addWordsView;
    private AddWordsAdapter mAdapter;
    Toolbar toolbar;
    NavController navController;
    AppBarConfiguration appBarConfiguration;
    MaterialButton addButton;
    List<AddWords> words;
    AddWords wordClass;
    List<String> chosenWords;
    Integer set_id;

    public static final String TAG = "MainActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wordClass = new AddWords(getArguments().getString("answer_data"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_words_from_text, container, false);
        addWordsView = view.findViewById(R.id.add_words_recycler_view);
        addWordsView.setLayoutManager(new LinearLayoutManager(getActivity()));
        chosenWords = new ArrayList<String>();
        addButton = view.findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.listOfSetsFragment);
            }
        });
        updateUI();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        navController = Navigation.findNavController(view);
        MutableLiveData<Integer> liveData = navController.getCurrentBackStackEntry()
                .getSavedStateHandle()
                .getLiveData("set_number");
        liveData.observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                set_id = integer;
                addWordsToSet();
                navController.getCurrentBackStackEntry()
                        .getSavedStateHandle().remove("set_number");
            }
        });

        appBarConfiguration =
                new AppBarConfiguration.Builder(navController.getGraph()).build();
        toolbar = view.findViewById(R.id.toolbar_add_word);
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);
    }

    private void addWordsToSet(){
        for (AddWords a: words) {
            if (a.getChecked()) {
                new AddTranslation().execute(a.getWord());
            }
        }
        navController.getPreviousBackStackEntry().getSavedStateHandle().set("ok_state", true);
        navController.popBackStack();
    }

    private void updateUI() {
        words = AddWords.getWords();
        mAdapter = new AddWordsAdapter(words);
        addWordsView.setAdapter(mAdapter);
    }

    private class AddTranslation extends AsyncTask<String, Void, Word> {

        String transcription = "";
        String translation = "";
        String originalExample = "";
        String translatedExample = "";

        @Override
        protected Word doInBackground(String... origWord) {
            Log.d(MainActivity.TAG, "OrigWord " + origWord[0]);
            String path = "https://dictionary.yandex.net/api/v1/dicservice/lookup?key=dict.1.1.20221101T085051Z.3a0f8a1364b70f6e.a06af2636d80f62bd69c981c62ef8772ebc7283e&lang=en-ru&text=" + origWord[0];
            StringBuilder xmlResult = new StringBuilder();
            BufferedReader reader = null;
            InputStream stream = null;
            HttpsURLConnection connection = null;
            try {
                URL url = new URL(path);
                connection = (HttpsURLConnection) url.openConnection();
                stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                String line;
                while ((line = reader.readLine()) != null) {
                    xmlResult.append(line);
                }
                if (reader != null) {
                    reader.close();
                }
                if (stream != null) {
                    stream.close();
                }
                if (connection != null) {
                    connection.disconnect();
                }
                boolean parseResult = parse(xmlResult.toString());
                if (!parseResult) {
                    return null;
                }
                return new Word(0, set_id, origWord[0], translation, transcription, originalExample + "!" + translatedExample, 0, 0, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Word result) {
            super.onPostExecute(result);
            if (result == null) {
                return;
            }
            DBLab dbLab = DBLab.get(getActivity());
            dbLab.addWord(result);
        }

        private boolean parse(String xmlData) {
            String textValue = "";
            boolean inTr = false;
            boolean inEx = false;
            boolean find = false;
            try {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(new StringReader(xmlData));
                int eventType = xpp.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    String tagName = xpp.getName();
                    switch (eventType) {
                        case XmlPullParser.START_TAG:
                            switch (tagName) {
                                case "def":
                                    find = true;
                                    if (transcription.equals("")) {
                                        for (int i = 0; i < xpp.getAttributeCount(); i++) {
                                            if (xpp.getAttributeName(i).equals("ts")) {
                                                transcription = xpp.getAttributeValue(i);
                                            }
                                        }
                                    }
                                    break;
                                case "tr":
                                    inTr = true;
                                    break;
                                case "ex":
                                    inEx = true;
                                    break;
                            }
                            break;
                        case XmlPullParser.TEXT:
                            textValue = xpp.getText();
                            if (inTr && !inEx) {
                                translation += textValue + ";";
                                inTr = false;
                            }
                            if (inEx && !inTr) {
                                originalExample += textValue + ";";
                            }
                            if (inEx && inTr) {
                                translatedExample += textValue + ";";
                                inEx = false;
                                inTr = false;
                            }
                            break;
                        case XmlPullParser.END_TAG:
                            switch (tagName) {
                                case "syn":
                                    translation += textValue + ";";
                                    break;
                            }
                            break;
                    }
                    eventType = xpp.next();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return find;
        }
    }

    private class AddWordsHolder extends  RecyclerView.ViewHolder{

        private MaterialCheckBox checkBox;
        private TextView textViewWord;
        private TextView textViewRepeats;
        private TextView textViewLevel;
        private AddWords addWords;

        public AddWordsHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.add_word_item, parent, false));

            checkBox = itemView.findViewById(R.id.checkbox_add_word);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    for (AddWords addword: words) {
                        if (Objects.equals(addword.getWord(), addWords.getWord())) {
                            addword.setChecked(b);
                            break;
                        }

                    }
                }
            });
            textViewWord = itemView.findViewById(R.id.add_word_name);
            textViewRepeats = itemView.findViewById(R.id.repeats_textview);
            textViewLevel = itemView.findViewById(R.id.level_textview);
        }
        public void bind(AddWords maddWords){
            addWords = maddWords;
            checkBox.setChecked(addWords.getChecked());
            textViewWord.setText(addWords.getWord());
            textViewRepeats.setText("Количество повторов: " + Integer.toString(addWords.getRepeats()));
            textViewLevel.setText("Уровень: " + addWords.getLevel());
        }
    }

    private class AddWordsAdapter extends RecyclerView.Adapter<AddWordsHolder>{
        private List<AddWords> mWords;
        public AddWordsAdapter(List<AddWords> words){
            mWords = words;
        }

        @NonNull
        @Override
        public AddWordsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new AddWordsHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull AddWordsHolder holder, int position) {

            AddWords addWords = mWords.get(position);
            holder.bind(addWords);
        }

        @Override
        public int getItemCount() {
            return mWords.size();
        }
    }
}
