package com.mzhang.cleantimer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.MotionEvent;
import android.widget.TextView;
import android.content.Intent;

import java.lang.System;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.annotation.SuppressLint;


public class MainActivity extends AppCompatActivity {

    long startTime = 0;
    long recorded = 0;
    int time;
    TextView timer;
    Handler handler = new Handler();
    boolean isOn = false;
    ArrayList<Integer> solvesList = new ArrayList<Integer>();

    Runnable updateTimer = new Runnable() {
        @Override
        public void run() {
            recorded = (System.nanoTime() - startTime) / 1000000;
            time = (int) recorded;
            timer.setText(formatTime(time));
            handler.postDelayed(this, 0);
        }
    };

    String newScramble() {
        Random rand = new Random();
        String textBox = "";
        char[] moves = {'U', 'R', 'F', 'D', 'L', 'B'};
        int numberOfMoves = rand.nextInt(6) + 20; //rand # 20..25

        for (int i = 0; i < numberOfMoves; i++) {
            int moveKey = rand.nextInt(6);
            int moveType = rand.nextInt(4);
            switch (moveType) {
                case 0:
                    textBox += moves[moveKey] + "'";
                    break;
                case 1:
                    textBox += moves[moveKey] + "2";
                    break;
                default:
                    textBox += moves[moveKey];
                    break;
            }
            textBox += " ";
        }
        return textBox;
    }

    String formatTime(int input) {
        int secs = (int) (input / 1000);
        int mins = secs / 60;
        secs %= 60;
        int mills = (int) (input % 1000);
        return String.format("%02d", mins) + ":" + String.format("%02d", secs)
                + ":" + String.format("%03d", mills);

    }

    int listAverage(List<Integer> inputList) {
        double sum = 0;
        for (Integer value : inputList) {
            sum += value;
        }
        return (int) sum / inputList.size();
    }

    void displaySolveHistory(List<Integer> lastFiveList, TextView displayedSolves) {
        String toPrint = "";
        for (int i = 0; i < lastFiveList.size(); i++) {
            toPrint += formatTime((lastFiveList.get(i))) + "\n";
            displayedSolves.setText(toPrint);
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.LightTheme);
        } else {
            setTheme(R.style.DarkTheme);
        }

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        timer = findViewById(R.id.timer);
        ConstraintLayout layout = findViewById(R.id.layout);
        final TextView displayedSolves = findViewById(R.id.displayedSolves);
        final TextView scramble = findViewById(R.id.scramble);
        scramble.setText(newScramble());


        if (savedInstanceState != null) {
            solvesList.add(savedInstanceState.getInt("SOLVE"));
        }


        layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (isOn) {
                            handler.removeCallbacks(updateTimer);
                            isOn = !isOn;
                            scramble.setText(newScramble());

                            solvesList.add(time);
                            List<Integer> lastFiveList = solvesList.subList(Math.max(solvesList.size() - 5, 0), solvesList.size());

                            String toPrint = "";
                            for (int i = 0; i < lastFiveList.size(); i++) {
                                toPrint += formatTime((lastFiveList.get(i))) + "\n";
                                displayedSolves.setText(toPrint);
                            }

                            TextView lastFiveAverage = (TextView) findViewById(R.id.lastFiveAverage);
                            lastFiveAverage.setText(formatTime(listAverage(lastFiveList)));
                        } else {
                            startTime = System.nanoTime();
                            handler.post(updateTimer);
                            isOn = !isOn;
                        }
                        return true;
                    case MotionEvent.ACTION_UP:

                        return true;
                }

                return false;

            }
        });

        TextView lightSwitch = findViewById(R.id.lightSwitch);
        lightSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }

                finish();
                startActivity(new Intent(MainActivity.this, MainActivity.this.getClass()));
            }
        });
    }

    protected void onPause() {
        super.onPause();

        SharedPreferences pref = getSharedPreferences("com.mzhang.cleantimer.solvesList", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        for (int i = 0; i < solvesList.size(); i++) {
            editor.putInt("SOLVE", solvesList.get(i));
        }

        editor.apply();
    }

}

