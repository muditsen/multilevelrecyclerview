package com.multilevelview;

import android.view.View;

import com.multilevelview.models.RecyclerViewItem;

interface OnRecyclerItemClickListener {
    void onItemClick(View view, RecyclerViewItem item, int position);
}