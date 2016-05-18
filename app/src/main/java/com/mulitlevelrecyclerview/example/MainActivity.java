package com.mulitlevelrecyclerview.example;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.mulitlevelrecyclerview.R;
import com.multilevelview.MultiLevelRecyclerView;
import com.multilevelview.models.RecyclerViewItem;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        MultiLevelRecyclerView multiLevelRecyclerView = (MultiLevelRecyclerView) findViewById(R.id.rv_list);

        multiLevelRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Item> itemList = new ArrayList<>();
        for(int i=0;i<10;i++){
            Item item = new Item(1);
            List<RecyclerViewItem> itemList2 = new ArrayList<>();
            for(int j=0;j<5;j++){
                Item item2 = new Item(2);
                List<RecyclerViewItem> itemList3 = new ArrayList<>();
                for(int k=0;k<3;k++){
                    Item item3 = new Item(3);
                    item3.setText("XYZ "+k);
                    item3.setSecondText("xyz "+k);
                    itemList3.add(item3);
                }
                item2.setText("PQRST "+j);
                item2.setSecondText("pqrst "+j);
                item2.addChildren(itemList3);
                itemList2.add(item2);
            }
            item.setText("ABCDE "+i);
            item.setSecondText("abcde "+i);
            item.addChildren(itemList2);
            itemList.add(item);
        }
        MyAdapter myAdapter = new MyAdapter(itemList);

        multiLevelRecyclerView.setAdapter(myAdapter);

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
