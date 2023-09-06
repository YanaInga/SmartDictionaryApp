package com.dev.smartdictionary;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;


public class SearchWordsFragment extends Fragment {

    Spinner spinnerLevel;
    Spinner spinnerRepeats;
    MaterialButton applyButton;
    NavController navController;
    EditText inputText;
    Integer repeatsCount = 1;
    Integer levelInt = 0;

    Thread Thread1 = null;
   // private String HOST = "192.168.3.9";
   private String HOST = "192.168.243.209";
    private int PORT = 8888;
    private String LOG_TAG = "SOCKET";

    String[] levels = { "Начальный", "Средний", "Продвинутый"};
    String[] repeats = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "15", "20", "25", "30", "40", "50", "60"};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        navController = NavHostFragment.findNavController(this);
        MutableLiveData<Boolean> liveData = navController.getCurrentBackStackEntry()
                .getSavedStateHandle()
                .getLiveData("ok_state");
        liveData.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean b) {
                if (b) {
                    inputText.setText("");
                    spinnerLevel.setSelection(0);
                    spinnerRepeats.setSelection(0);
                }
                navController.getCurrentBackStackEntry()
                        .getSavedStateHandle().remove("ok_state");
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_words, container, false);
        inputText = view.findViewById(R.id.editText_search_word);
        spinnerLevel = view.findViewById(R.id.spinner_level);
        ArrayAdapter<String> adapter = new ArrayAdapter(view.getContext(), android.R.layout.simple_spinner_item, levels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLevel.setAdapter(adapter);
        spinnerLevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                levelInt = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        spinnerRepeats = view.findViewById(R.id.spinner_repeats);
        adapter = new ArrayAdapter(view.getContext(), android.R.layout.simple_spinner_item, repeats);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRepeats.setAdapter(adapter);
        spinnerRepeats.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                repeatsCount = Integer.parseInt(String.valueOf(adapterView.getItemAtPosition(i)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        applyButton = view.findViewById(R.id.apply_button);
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!inputText.getText().toString().equals("")) {
                    String txt = repeatsCount.toString() + "≈" + levelInt.toString() + "≈" + inputText.getText().toString();
                    new Connection().execute(txt);
                    }

            }
        });
        MaterialButton openButton = view.findViewById(R.id.open_button);
        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("text/plain");
                startActivityForResult(intent, 2);
            }
        });

        MaterialButton clearButton = view.findViewById(R.id.clear_button);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputText.setText("");
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        String text = "";
        if (requestCode == 2 && resultCode == getActivity().RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                try {
                   text = readTextFromUri(uri);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (!text.equals(""))
                        inputText.setText(text);
                }
            }
        }
    }

    private String readTextFromUri(Uri uri) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream inputStream =
                     getContext().getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(Objects.requireNonNull(inputStream)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line + "\n");
            }
        }
        return stringBuilder.toString();
    }

    private class Connection extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... strings) {
            String outMsg = strings[0];
            String inMsg = "";
            try {
                Log.d(LOG_TAG, "Start");
                Socket socketClient = new Socket(HOST, PORT);
                Log.d(LOG_TAG, "Новый сокет действителен");
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socketClient.getOutputStream(), "UTF8"));
                Log.d(LOG_TAG, "Получить отправленные данные");
                out.write(outMsg);// Отправить данные
                out.flush();
                out.write("outMsg");// Отправить данные
                out.flush();
                Log.d(LOG_TAG, "sended ");
                BufferedReader in = new BufferedReader(new InputStreamReader(socketClient.getInputStream()));
                inMsg = in.readLine();
                Log.d(LOG_TAG, "receive ");
                socketClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return inMsg;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (!result.equals("")) {
                Bundle bundle = new Bundle();
                bundle.putString("answer_data", result);
                navController.navigate(R.id.addWordsFromTextFragment, bundle);
            } else
                Toast.makeText(getActivity(), "Не удалось найти слова", Toast.LENGTH_SHORT).show();

        }
    }
}