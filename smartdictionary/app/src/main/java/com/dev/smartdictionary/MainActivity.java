package com.dev.smartdictionary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    NavController navController;
    public static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "activity");
        setContentView(R.layout.activity_main);
        Log.d(TAG, "setcontentview");
        navController = Navigation.findNavController(this, R.id.NavFragment);
        Log.d(TAG, "findController");
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        Log.d(TAG, "findbotton");
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
        Log.d(TAG, "setupNavController");
        DBLab dbLab = DBLab.get(this);
    }

}