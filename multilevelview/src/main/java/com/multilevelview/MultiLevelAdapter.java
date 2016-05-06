package com.multilevelview;


import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;


import com.multilevelview.models.RecyclerViewItem;

import java.util.ArrayList;
import java.util.List;


public abstract class MultiLevelAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    List<RecyclerViewItem> recyclerViewItemList = new ArrayList<>();

    public MultiLevelAdapter(List<RecyclerViewItem> recyclerViewItems){
        this.recyclerViewItemList = recyclerViewItems;
    }

    public void setRecyclerViewItemList(List<RecyclerViewItem> recyclerViewItemList) {
        this.recyclerViewItemList = recyclerViewItemList;
    }

    @Override
    public abstract RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) ;

    @Override
    public abstract void onBindViewHolder(RecyclerView.ViewHolder holder, int position) ;

    @Override
    public int getItemCount() {
        return recyclerViewItemList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return recyclerViewItemList.get(position).getLevel();
    }

    public List<RecyclerViewItem> getRecyclerViewItemList() {
        return recyclerViewItemList;
    }
}
