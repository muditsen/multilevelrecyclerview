package com.mulitlevelrecyclerview.example;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mulitlevelrecyclerview.R;
import com.multilevelview.MultiLevelAdapter;
import com.multilevelview.MultiLevelRecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MyAdapter extends MultiLevelAdapter {

    private Holder mViewHolder;
    private Context mContext;
    private List<Item> mListItems = new ArrayList<>();
    private Item mItem;
    private MultiLevelRecyclerView mMultiLevelRecyclerView;

    MyAdapter(Context mContext, List<Item> mListItems, MultiLevelRecyclerView mMultiLevelRecyclerView) {
        super(mListItems);
        this.mListItems = mListItems;
        this.mContext = mContext;
        this.mMultiLevelRecyclerView = mMultiLevelRecyclerView;
    }

    private void setExpandButton(ImageView expandButton, boolean isExpanded) {
        // set the icon based on the current state
        //change state without duration because we don't need animation while scrolling
        if(!isExpanded)
            expandButton.animate().setDuration(0).rotation(0F).start();
        else
            expandButton.animate().setDuration(0).rotation(-180F).start();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        mViewHolder = (Holder) holder;
        mItem = mListItems.get(position);

        switch (getItemViewType(position)) {
            case 1:
                holder.itemView.setBackgroundColor(Color.parseColor("#efefef"));
                break;
            case 2:
                holder.itemView.setBackgroundColor(Color.parseColor("#dedede"));
                break;
            default:
                holder.itemView.setBackgroundColor(Color.parseColor("#ffffff"));
                break;
        }
        mViewHolder.mTitle.setText(mItem.getText());
        mViewHolder.mSubtitle.setText(mItem.getSecondText());

        if (mItem.hasChildren() && mItem.getChildren().size() > 0) {
            setExpandButton(mViewHolder.mExpandIcon, mItem.isExpanded());
            mViewHolder.mExpandButton.setVisibility(View.VISIBLE);
        } else {
            mViewHolder.mExpandButton.setVisibility(View.GONE);
        }

        Log.e("MuditLog",mItem.getLevel()+" "+mItem.getPosition()+" "+mItem.isExpanded()+"");

        // indent child items
        // Note: the parent item should start at zero to have no indentation
        // e.g. in populateFakeData(); the very first Item shold be instantiate like this: Item item = new Item(0);
        float density = mContext.getResources().getDisplayMetrics().density;
        ((ViewGroup.MarginLayoutParams) mViewHolder.mTextBox.getLayoutParams()).leftMargin = (int) ((getItemViewType(position) * 20) * density + 0.5f);
    }

    private class Holder extends RecyclerView.ViewHolder {

        TextView mTitle, mSubtitle;
        ImageView mExpandIcon;
        LinearLayout mTextBox, mExpandButton;

        Holder(View itemView) {
            super(itemView);
            mTitle = (TextView) itemView.findViewById(R.id.title);
            mSubtitle = (TextView) itemView.findViewById(R.id.subtitle);
            mExpandIcon = (ImageView) itemView.findViewById(R.id.image_view);
            mTextBox = (LinearLayout) itemView.findViewById(R.id.text_box);
            mExpandButton = (LinearLayout) itemView.findViewById(R.id.expand_field);

            // The following code snippets are only necessary if you set multiLevelRecyclerView.removeItemClickListeners(); in MainActivity.java
            // this enables more than one click event on an item (e.g. Click Event on the item itself and click event on the expand button)
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //set click event on item here
                    Toast.makeText(mContext, String.format(Locale.ENGLISH, "Item at position %d was clicked!", getAdapterPosition()), Toast.LENGTH_SHORT).show();
                }
            });

            //set click listener on LinearLayout because the click area is bigger than the ImageView
            mExpandButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // set click event on expand button here
                    mMultiLevelRecyclerView.toggleItemsGroup(getAdapterPosition());
                    // rotate the icon based on the current state
                    // animate with duration
                    mExpandIcon.animate().setDuration(200).rotation(mListItems.get(getAdapterPosition()).isExpanded() ? -180 : 0).start();

                    Toast.makeText(mContext, String.format(Locale.ENGLISH, "Item at position %d is expanded: %s", getAdapterPosition(), mItem.isExpanded()), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}
