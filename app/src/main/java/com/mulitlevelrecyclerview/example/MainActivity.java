package com.mulitlevelrecyclerview.example;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.mulitlevelrecyclerview.R;
import com.multilevelview.MultiLevelRecyclerView;
import com.multilevelview.models.RecyclerViewItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        MultiLevelRecyclerView multiLevelRecyclerView = (MultiLevelRecyclerView) findViewById(R.id.rv_list);
        multiLevelRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Item> itemList = (List<Item>) recursivePopulateFakeData(0, 12);

        MyAdapter myAdapter = new MyAdapter(this, itemList, multiLevelRecyclerView);

        multiLevelRecyclerView.setAdapter(myAdapter);
        multiLevelRecyclerView.removeItemClickListeners();
    }


    private List<?> recursivePopulateFakeData(int levelNumber, int depth) {
        List<RecyclerViewItem> itemList = new ArrayList<>();

        String title;
        switch (levelNumber){
            case 1:
                title = "PQRST %d";
                break;
            case 2:
                title = "XYZ %d";
                break;
            default:
                title = "ABCDE %d";
                break;
        }

        for (int i = 0; i < depth; i++) {
            Item item = new Item(levelNumber);
            item.setText(String.format(Locale.ENGLISH, title, i));
            item.setSecondText(String.format(Locale.ENGLISH, title.toLowerCase(), i));
            if(depth % 2 == 0){
                item.addChildren((List<RecyclerViewItem>) recursivePopulateFakeData(levelNumber + 1, depth/2));
            }
            itemList.add(item);
        }

        return itemList;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
