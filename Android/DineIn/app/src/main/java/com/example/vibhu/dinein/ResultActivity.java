package com.example.vibhu.dinein;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class ResultActivity extends AppCompatActivity {
    private ArrayAdapter arrayAdapter;
    private final ArrayList<String> reviews = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result_layout);

        ViewPager viewPager = findViewById(R.id.viewPager);
        ImageAdapter adapter = new ImageAdapter(this);
        viewPager.setAdapter(adapter);
reviews.add("Idly is very good. I like their chai tea too. If you order tea, unless you specify to server, they don't bring it to you till the end. ");
reviews.add("I've been here quite a few times and finally decided to write a review for this place. I've been in some what of a dosa frenzy lately and have been trying out many different dosa places trying to find my favorite one and lucky for me, this place which is the close to home for me, is my favorite!");
        ListView listView=(ListView)findViewById(R.id.listview_review);
        arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, reviews);
        listView.setAdapter(arrayAdapter);


    }
}