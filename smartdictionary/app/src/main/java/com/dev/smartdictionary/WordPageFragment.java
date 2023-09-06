package com.dev.smartdictionary;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.button.MaterialButton;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class WordPageFragment extends Fragment {
    EditText originalWord;
    EditText translatedWord;
    EditText transcriptionEdit;
    EditText originalExampleEdit;
    EditText translatedExampleEdit;
    FlexboxLayout translatedVariants;
    Button addButton;
    MaterialButton saveButton;
    Toolbar toolbar;
    NavController navController;
    AppBarConfiguration appBarConfiguration;
    CheckBox isLearn;
    String v1 = "",v2 = "";
    boolean isOriginalExampleFocus = false;
    boolean isTranslatedExampleFocus = false;
    private boolean isWordNew;
    private int setId;
    private Word word;
    public static final String SAVE_DATA = "com.dev.smartdictionary.save_data";

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        navController = Navigation.findNavController(view);
        appBarConfiguration =
                new AppBarConfiguration.Builder(navController.getGraph()).build();
        toolbar = view.findViewById(R.id.toolbar_word_page);
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_word_page, container, false);
        originalWord = view.findViewById(R.id.originalWord);
        originalWord.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if(event.getAction() == KeyEvent.ACTION_DOWN &&
                        (keyCode == KeyEvent.KEYCODE_ENTER))
                {
                    String origWord = originalWord.getText().toString();
                    new FindTranslation().execute(origWord);
                    return true;
                }
                return false;
            }
        });
        translatedWord = view.findViewById(R.id.translatedText);
        transcriptionEdit = view.findViewById(R.id.transcription);
        originalExampleEdit = view.findViewById(R.id.orig_example);
        originalExampleEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                isOriginalExampleFocus = b;
                focusOrigExamples(b);
            }
        });
        translatedExampleEdit = view.findViewById(R.id.transl_example);
        translatedExampleEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                isTranslatedExampleFocus = b;
                focusTransExamples(b);
            }
        });
        translatedVariants = view.findViewById(R.id.translationsGrid);
        isLearn = view.findViewById(R.id.isLearn);
        isLearn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                    word.setLearn(1);
                else word.setLearn(0);
            }
        });
        isWordNew = getArguments().getBoolean("is_word_new");
        if(isWordNew) {
            setId = getArguments().getInt("word_id");
            word = new Word(setId);
        }
        else {
            int id = getArguments().getInt("word_id");
            DBLab dbLab = DBLab.get(getActivity());
            word = dbLab.findWord(id);
            originalWord.setText(word.getEnglishWord());
            transcriptionEdit.setText(word.getTranscription());
            isLearn.setChecked(word.isLearn() != 0);
            parseTranslation(word.getTranslatedWord());
            parseExample(word.getExample());
        }
        addButton = view.findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initializeWordView(translatedWord.getText().toString());
                word.setTranslatedWord(word.getTranslatedWord() + translatedWord.getText().toString() + ";");
                translatedWord.setText("");
            }
        });
        saveButton = view.findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                word.setTranscription(transcriptionEdit.getText().toString());
                word.setEnglishWord(originalWord.getText().toString());
                if (isTranslatedExampleFocus)
                    v2 = translatedExampleEdit.getText().toString();
                if (isOriginalExampleFocus)
                    v1 = originalExampleEdit.getText().toString();
                word.setExample(v1.replaceAll("\n", ";")
                        + "!" + v2.replaceAll("\n", ";"));

                DBLab dbLab = DBLab.get(getActivity());
                if(isWordNew) {
                    dbLab.addWord(word);
                }
                else{
                    dbLab.changeWord(word);
                }
                navController.popBackStack();
            }
        });
        return view;
    }

    private void focusOrigExamples(boolean b){
        if(originalExampleEdit.getText().toString().equals(""))
            return;
        if(b) {
            //  String[] examplesParts = word.getExample().split("!");
           // v1 = v1.replaceAll("\n", ";");
            String editVariant = v1.replaceAll(";", "\n");
            editVariant = editVariant.substring(0, editVariant.length());
            originalExampleEdit.setText(editVariant);
        }
        else
        {
            v1 = originalExampleEdit.getText().toString();
            Log.d(MainActivity.TAG, "V1 "+v1);
            String[] origEx = v1.split("\n");
            String origText = "";
            for(int i = 0; i < origEx.length; i++)
            {
                origText += (i+1) + ". " + origEx[i];
                if(i != origEx.length - 1)
                {
                    origText += "\n";
                }
            }
            originalExampleEdit.setText(origText);
            v1 = v1.replaceAll("\n", ";");
        }
    }
    private void focusTransExamples(boolean b){
        if(translatedExampleEdit.getText().toString().equals(""))
            return;
        if(b) {
           // String[] examplesParts = word.getExample().split("!");
            String editVariant = v2.replaceAll(";", "\n");
            editVariant = editVariant.substring(0, editVariant.length());
            translatedExampleEdit.setText(editVariant);
        }
        else
        {
            v2 = translatedExampleEdit.getText().toString();
            Log.d(MainActivity.TAG, "V2 "+v2);
            String[] origEx = v2.split("\n");
            String origText = "";
            for(int i = 0; i < origEx.length; i++)
            {
                origText += (i+1) + ". " + origEx[i];
                if(i != origEx.length - 1)
                {
                    origText += "\n";
                }
            }
            translatedExampleEdit.setText(origText);
            v2 = v2.replaceAll("\n", ";");
        }
    }

    private class FindTranslation extends AsyncTask<String, Void, Word> {

        String transcription = "";
        String translation = "";
        String originalExample = "";
        String translatedExample = "";

        @Override
        protected Word doInBackground(String... origWord) {
            Log.d(MainActivity.TAG, "OrigWord " + origWord[0]);
            String path = "https://dictionary.yandex.net/api/v1/dicservice/lookup?key=dict.1.1.20221101T085051Z.3a0f8a1364b70f6e.a06af2636d80f62bd69c981c62ef8772ebc7283e&lang=en-ru&text="+origWord[0];
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
                while ((line=reader.readLine()) != null) {
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

                if (!parseResult){
                    return null;
                }
                return new Word(0, 0, origWord[0], translation, transcription, originalExample+"!"+translatedExample, 0, 0, 0);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Word result) {
            super.onPostExecute(result);
            if(result == null) {
                Toast.makeText(getActivity(), "Не удалось найти перевод", Toast.LENGTH_SHORT).show();
                return;
            }
            word.setEnglishWord(result.getEnglishWord());
            word.setTranslatedWord(result.getTranslatedWord());
            word.setExample(result.getExample());
            word.setTranscription(result.getTranscription());
            if(!word.getTranscription().equals(""))
                transcriptionEdit.setText(word.getTranscription());
            parseExample(word.getExample());
            parseTranslation(word.getTranslatedWord());
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
                    switch (eventType){
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
                            if(inTr && !inEx){
                                translation += textValue + ";";
                                inTr = false;
                            }
                            if(inEx && !inTr){
                                originalExample += textValue + ";";
                            }
                            if(inEx && inTr){
                                translatedExample += textValue + ";";
                                inEx = false;
                                inTr = false;
                            }
                            break;
                        case XmlPullParser.END_TAG:
                            switch (tagName){
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

    private void parseTranslation(String trWord) {
        String[] variants = trWord.split(";");
        for (int i = 0; i < variants.length; i++) {
            initializeWordView(variants[i]);
        }
    }

    private void parseExample(String example) {
        if(example.equals("!"))
            return;
        String[] examplesParts = example.split("!");
        try{
            String[] origEx = examplesParts[0].split(";");
            v1 = examplesParts[0];
            String[] translEx = examplesParts[1].split(";");
            v2 = examplesParts[1];
            String origText = "";
            String translText = "";
            for(int i = 0; i < origEx.length; i++)
            {
                origText += (i+1) + ". " + origEx[i];
                if(i != origEx.length - 1)
                {
                    origText += "\n";
                }
            }
            for(int i = 0; i < translEx.length; i++)
            {
                translText += (i+1) + ". " + translEx[i];
                if(i != translEx.length - 1)
                {
                    translText += "\n";
                }
            }
            originalExampleEdit.setText(origText);
            translatedExampleEdit.setText(translText);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeWordView(String variant) {
        MaterialButton button = new MaterialButton(getActivity(), null, com.google.android.material.R.attr.materialButtonStyle);
        FlexboxLayout.LayoutParams layoutParams = new FlexboxLayout.LayoutParams(FlexboxLayout.LayoutParams.WRAP_CONTENT , FlexboxLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(5,5,5,5);
        button.setLayoutParams(layoutParams);
        button.setCornerRadius(35);
        button.setBackgroundColor(getResources().getColor(R.color.teal_200));
        button.setElevation(8F);
        button.setText(variant);
        button.setId(View.generateViewId());
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id = view.getId();
                MaterialButton btn = view.findViewById(id);
                String deletedWord = btn.getText().toString();
                translatedWord.setText(deletedWord);
                String newWordChain = word.getTranslatedWord().replace(deletedWord + ";", "");
                word.setTranslatedWord(newWordChain);
                btn.setVisibility(View.GONE);
            }
        });

        translatedVariants.addView(button);
    }

}