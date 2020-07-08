package com.mzhang.cleantimer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;

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
    boolean isDark = true;
    boolean isPrimed = false;
    ArrayList<Integer> solvesList = new ArrayList<Integer>();
    GestureDetector detector;

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

    void displayLastFive(List<Integer> lastFiveList, TextView displayedSolves) {
        String toPrint = "";
        for (int i = 0; i < lastFiveList.size(); i++) {
            toPrint += formatTime((lastFiveList.get(i))) + "\n";
            displayedSolves.setText(toPrint);
        }
    }

    void saveDarkStatus() {
        SharedPreferences pref = getSharedPreferences("com.mzhang.cleantimer.isDark", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        if (isDark) {
            editor.putBoolean("isDark", true);
        } else {
            editor.putBoolean("isDark", false);
        }

        editor.apply();
    }



    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        SharedPreferences pref = getSharedPreferences("com.mzhang.cleantimer.isDark", Context.MODE_PRIVATE);
        isDark = pref.getBoolean("isDark", true);
        if (isDark) {
            setTheme(R.style.DarkTheme);
        } else {
            setTheme(R.style.LightTheme);
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
        final ConstraintLayout layout = findViewById(R.id.layout);
        final TextView displayedSolves = findViewById(R.id.displayedSolves);
        final TextView scramble = findViewById(R.id.scramble);
        scramble.setText(newScramble());

        detector = new GestureDetector(this, new LayoutGestureDetector());
        layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (!isOn && isPrimed) {
                        startTime = System.nanoTime();
                        handler.post(updateTimer);
                        isOn = !isOn;
                        isPrimed = false;
                    }
                }
                return detector.onTouchEvent(event);
            }
        });

        TextView lightSwitch = findViewById(R.id.lightSwitch);
        lightSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDark = !isDark;
                finish();
                startActivity(new Intent(MainActivity.this, MainActivity.this.getClass()));
            }
        });
    }

    protected void onPause() {
        super.onPause();
        saveDarkStatus();
    }

    class LayoutGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent motionEvent) {
            final TextView displayedSolves = findViewById(R.id.displayedSolves);
            final TextView scramble = findViewById(R.id.scramble);
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
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent motionEvent) {
            timer.setText("PRIMED");
            isPrimed = true;
        }


    }

}

