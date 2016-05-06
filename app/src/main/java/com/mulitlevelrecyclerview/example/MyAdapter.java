package com.mulitlevelrecyclerview.example;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mulitlevelrecyclerview.R;
import com.multilevelview.MultiLevelAdapter;
import com.multilevelview.models.RecyclerViewItem;

import java.util.List;

public class MyAdapter extends MultiLevelAdapter {

    public MyAdapter(List<RecyclerViewItem> recyclerViewItems) {
        super(recyclerViewItems);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout,parent,false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == 2){
            holder.itemView.setBackgroundColor(Color.parseColor("#efefef"));
        }else if(getItemViewType(position) == 3){
            holder.itemView.setBackgroundColor(Color.parseColor("#dedede"));
        }else{
            holder.itemView.setBackgroundColor(Color.parseColor("#ffffff"));
        }
        ((Holder)holder).tvPosition.setText("Position "+position);
    }

    public static class Holder extends RecyclerView.ViewHolder{

        TextView tvPosition;
        public Holder(View itemView) {
            super(itemView);
            tvPosition = (TextView) itemView.findViewById(R.id.tv_position);
        }
    }

}
