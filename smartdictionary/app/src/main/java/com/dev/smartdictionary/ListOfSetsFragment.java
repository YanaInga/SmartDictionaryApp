package com.dev.smartdictionary;

import android.app.FragmentTransaction;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;


public class ListOfSetsFragment extends Fragment implements CreateSetDialogFragment.OkListener {

    Toolbar toolbar;
    NavController navController;
    AppBarConfiguration appBarConfiguration;
    ListView listView;
    MaterialButton choosebotton;

    Integer numberSet;

    private static final String DIALOG_NEW_SET = "DialogNewSet";

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        navController = Navigation.findNavController(view);
        appBarConfiguration =
                new AppBarConfiguration.Builder(navController.getGraph()).build();
        toolbar = view.findViewById(R.id.toolbar_list_sets);
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_of_sets, null);
        TextView addset = view.findViewById(R.id.text_view_set);
        addset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateSetDialogFragment dialog = new CreateSetDialogFragment();
                dialog.show(getChildFragmentManager(), DIALOG_NEW_SET);
            }
        });
        listView = view.findViewById(R.id.list);

        updateDrawer();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                DBLab dbLab = DBLab.get(getActivity());
                numberSet = dbLab.findSet((String) listView.getItemAtPosition(i));
                choosebotton.setVisibility(View.VISIBLE);
            }
        });

        choosebotton = view.findViewById(R.id.choose_button);
        choosebotton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.getPreviousBackStackEntry().getSavedStateHandle().set("set_number", numberSet);
                navController.popBackStack();
           }
        });
        return view;
    }

    private void updateDrawer() {
        DBLab dbLab = DBLab.get(getActivity());
        List<Set> sets = dbLab.getSets();
        ArrayList<String> titles = new ArrayList<String>();
        for(int i=0; i< sets.size();i++) {
            titles.add(sets.get(i).getName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_activated_1, titles);
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);
    }

    @Override
    public void dialogResult(String name){
        if (name.equals(""))
        {
            Toast.makeText(getActivity(), "Набор не был создан", Toast.LENGTH_SHORT).show();
            return;
        }
        DBLab dbLab = DBLab.get(getActivity());
        dbLab.addSet(name);
        updateDrawer();
    }
}