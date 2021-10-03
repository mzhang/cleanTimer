package com.mzhang.cleantimer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.GestureDetector;

import android.view.View;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;


import java.lang.System;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


import android.annotation.SuppressLint;


public class MainActivity extends AppCompatActivity {
    static class Move {
        static boolean isMirror(char move1, char move2) {
            switch (move1) {
                case 'L':
                    return move2 == 'R';
                case 'R':
                    return move2 == 'L';
                case 'U':
                    return move2 == 'D';
                case 'D':
                    return move2 == 'U';
                case 'F':
                    return move2 == 'B';
                case 'B':
                    return move2 == 'F';
                default:
                    return false;
            }
        }

        static boolean isUseless(char move, char prevMove, char prePrevMove) {
            return (move == prevMove) || (move == prePrevMove && isMirror(move, prevMove));
        }
    }


    long startTime = 0;
    long recorded = 0;
    int time;
    TextView timer;
    Handler handler = new Handler();
    boolean isMainTimerOn = false;
    boolean isDark = true;
    boolean isPrimed = false;
    ArrayList<Integer> solvesList = new ArrayList<Integer>();
    GestureDetector detector;


    Runnable updateMainTimer = new Runnable() {
        @Override
        public void run() {
            recorded = (System.nanoTime() - startTime) / 1000000;
            time = (int) recorded;
            timer.setText(formatTime(time));
            handler.postDelayed(this, 0);
        }
    };

    Runnable updateInspectTimer = new Runnable() {
        @Override
        public void run() {
            recorded = (System.nanoTime() - startTime) / 1000000;
            time = 15000-(int) recorded;
            if (time < 0) {
                startMainTimer();
                handler.removeCallbacks(updateInspectTimer);
            } else {
                timer.setText(formatTime(time));
                handler.postDelayed(this, 0);
            }
        }
    };

    String newScramble() {
        Random rand = new Random();
        String textBox = "";
        char[] moves = {'U', 'R', 'F', 'D', 'L', 'B'};
        int numberOfMoves = rand.nextInt(6) + 20; //rand # 20..25
        char prevMove = '0';
        char prePrevMove = '0';

        for (int i = 0; i < numberOfMoves; i++) {
            int moveKey;
            do {
                moveKey = rand.nextInt(6);
            } while (Move.isUseless(moves[moveKey], prevMove, prePrevMove));
            prePrevMove = prevMove;
            prevMove = moves[moveKey];
            textBox += moves[moveKey];

            int moveType = rand.nextInt(3);
            switch (moveType) {
                case 0:
                    textBox += "'";
                    break;
                case 1:
                    textBox += "2";
                    break;
                default:
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
        try {
            return (int) sum / inputList.size();
        } catch(Exception e) {
            return (int) sum;
        }
    }

    void displayLastFive(ArrayList<Integer> solvesList, TextView displayedSolves) {
        TextView lastFiveAverage = (TextView) findViewById(R.id.lastFiveAverage);
        List<Integer> lastFiveList = solvesList.subList(Math.max(solvesList.size() - 5, 0), solvesList.size());

        if (lastFiveList.size() > 0) {
            lastFiveAverage.setText(formatTime(listAverage(lastFiveList)));
            String toPrint = "";
            for (int i = 0; i < lastFiveList.size(); i++) {
                toPrint += formatTime((lastFiveList.get(i))) + "\n";
                displayedSolves.setText(toPrint);
            }
        } else {
            lastFiveAverage.setText("");
            displayedSolves.setText("");
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

    void saveSolvesList(ArrayList<Integer> solvesList) {
        SharedPreferences pref = getSharedPreferences("com.mzhang.cleantimer.solvesList", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        StringBuilder toSave = new StringBuilder();
        String prefix = "";
        for (int i=0; i<solvesList.size(); i++) {
            toSave.append(prefix);
            prefix = ",";
            toSave.append(solvesList.get(i));
        }

        editor.putString("list", toSave.toString());
        editor.apply();
    }

    ArrayList<Integer> loadSolvesList(Integer numberOfMostRecent) {
        SharedPreferences pref = getSharedPreferences("com.mzhang.cleantimer.solvesList", Context.MODE_PRIVATE);

        ArrayList<Integer> toReturn = new ArrayList<>();

        try {
            String toParse = pref.getString("list", "1337");
            String[] tokens = toParse.split(",");
            int startIndex = Math.max(tokens.length - numberOfMostRecent, 0);
            for (int i = startIndex; i < tokens.length; i++) {
                toReturn.add(Integer.parseInt(tokens[i]));
            }
        } catch (Exception e) {

        }

        return toReturn;
    }

    void removeLastSolve(ArrayList<Integer> solvesList)
    {
        try {
            solvesList.remove(solvesList.size() - 1);
        } catch(Exception e) {
            System.out.println("No solves left in list!");
        }
    }
    
    void vibrate() {
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(25);
    }

    void startMainTimer() {
        vibrate();
        TextView scramble = findViewById(R.id.scramble);
        scramble.setText("Go!");
        startTime = System.nanoTime();
        handler.post(updateMainTimer);
        isMainTimerOn = !isMainTimerOn;
        isPrimed = false;
    }

    void startInspectionTimer() {
        vibrate();
        handler.removeCallbacks(updateMainTimer);
        startTime = System.nanoTime();
        handler.post(updateInspectTimer);
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
        setContentView(R.layout.activity_main);

        timer = findViewById(R.id.timer);
        final ConstraintLayout layout = findViewById(R.id.layout);
        final TextView displayedSolves = findViewById(R.id.displayedSolves);
        final TextView scramble = findViewById(R.id.scramble);
        scramble.setText(newScramble());

        solvesList = loadSolvesList(5);
        displayLastFive(solvesList, displayedSolves);

        class LayoutGestureDetector extends GestureDetector.SimpleOnGestureListener {
            @Override
            public boolean onDown(MotionEvent motionEvent) {
                if (isMainTimerOn) {
                    handler.removeCallbacks(updateMainTimer);
                    isMainTimerOn = !isMainTimerOn;
                    scramble.setText(newScramble());

                    solvesList.add(time);
                    displayLastFive(solvesList, displayedSolves);
                }
                return true;
            }

            @Override
            public void onLongPress(MotionEvent motionEvent) {
                isPrimed = true;
                final TextView scramble = findViewById(R.id.scramble);
                scramble.setText("Begin inspection!");
                startInspectionTimer();
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                final int SWIPE_MIN_DISTANCE = 40;
                final int SWIPE_THRESHOLD_VELOCITY = 100;

                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    return false; // Right to left
                }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    return false; // Left to right
                }

                if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                    startActivity(new Intent(MainActivity.this, stats.class));
                    return false; // Bottom to top
                }  else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                    startActivity(new Intent(MainActivity.this, stats.class));
                    return false; // Top to bottom
                }
                return true;
            }
        }

        detector = new GestureDetector(this, new LayoutGestureDetector());
        layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    handler.removeCallbacks(updateInspectTimer);
                    if (!isMainTimerOn && isPrimed) {
                        startMainTimer();
                    }
                }
                return detector.onTouchEvent(event);
            }
        });

        ImageView lightSwitch = findViewById(R.id.lightSwitch);
        lightSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDark = !isDark;
                finish();
                startActivity(new Intent(MainActivity.this, MainActivity.this.getClass()));
            }
        });

        ImageView discard = findViewById(R.id.discard);
        discard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeLastSolve(solvesList);
                displayLastFive(solvesList, displayedSolves);
            }
        });
    }

    protected void onPause() {
        super.onPause();
        saveDarkStatus();
        saveSolvesList(solvesList);
    }
}
