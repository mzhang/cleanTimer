package com.mzhang.cleantimer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class stats extends AppCompatActivity {
    boolean isDark = true;
    ArrayList<Integer> solvesList = new ArrayList<Integer>();

    GestureDetector detector;


    String formatTime(int input) {
        int secs = (int) (input / 1000);
        int mins = secs / 60;
        secs %= 60;
        int mills = (int) (input % 1000);
        return String.format("%02d", mins) + ":" + String.format("%02d", secs)
                + ":" + String.format("%03d", mills);
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

    String returnAverageOf(List<Integer> inputList, int range) {
        List sublist = inputList.subList(Math.max(inputList.size() - range, 0), inputList.size());
        return formatTime(listAverage(sublist));
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
        SharedPreferences pref = getSharedPreferences("com.mzhang.cleantimer.isDark", Context.MODE_PRIVATE);
        isDark = pref.getBoolean("isDark", true);
        if (isDark) {
            setTheme(R.style.DarkTheme);
        } else {
            setTheme(R.style.LightTheme);
        }
        setContentView(R.layout.activity_stats);

        class LayoutGestureDetector extends GestureDetector.SimpleOnGestureListener {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                final int SWIPE_MIN_DISTANCE = 80;
                final int SWIPE_THRESHOLD_VELOCITY = 200;

                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    return false; // Right to left
                }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    return false; // Left to right
                }
                if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                    startActivity(new Intent(stats.this, MainActivity.class));
                    return false; // Bottom to top
                }  else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                    startActivity(new Intent(stats.this, MainActivity.class));
                    return false; // Top to bottom
                }
                return true;
            }
        }

        LinearLayout layout = findViewById(R.id.statsLayout);
        layout.setLongClickable(true);
        detector = new GestureDetector(this, new LayoutGestureDetector());
        layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return detector.onTouchEvent(event);
            }
        });

        solvesList = loadSolvesList(1000);

        final TextView averageof5value = findViewById(R.id.averageof5value);
        final TextView averageof25value = findViewById(R.id.averageof25value);
        final TextView averageof100value = findViewById(R.id.averageof100value);
        final TextView averageofcareervalue = findViewById(R.id.averageofcareervalue);

        averageof5value.setText(returnAverageOf(solvesList, 5));
        averageof25value.setText(returnAverageOf(solvesList, 25));
        averageof100value.setText(returnAverageOf(solvesList, 100));
        averageofcareervalue.setText(formatTime(listAverage(solvesList)));

        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        final ArrayList<String> solvesListString = new ArrayList<String>();
        for (int i=0; i < solvesList.size(); i++) {
            solvesListString.add(formatTime(solvesList.get(i)));
        }

        SharedPreferences solvesMasterList = getSharedPreferences("com.mzhang.cleantimer.solvesList", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = solvesMasterList.edit();

        final RecyclerView.Adapter adapter = new CustomAdapter(solvesListString);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL));

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target){
                return false;
            }
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                // Remove item from backing list here
                solvesListString.remove(viewHolder.getAdapterPosition());
                solvesList.remove(viewHolder.getAdapterPosition());
                adapter.notifyDataSetChanged();

                averageof5value.setText(returnAverageOf(solvesList, 5));
                averageof25value.setText(returnAverageOf(solvesList, 25));
                averageof100value.setText(returnAverageOf(solvesList, 100));
                averageofcareervalue.setText(formatTime(listAverage(solvesList)));
            }
        });

        itemTouchHelper.attachToRecyclerView(recyclerView);
    }
    protected void onPause() {
        super.onPause();

        saveSolvesList(solvesList);
    }

}
