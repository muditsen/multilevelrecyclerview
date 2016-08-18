package com.multilevelview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.multilevelview.animators.DefaultItemAnimator;
import com.multilevelview.models.RecyclerViewItem;

import java.util.List;


public class MultiLevelRecyclerView extends RecyclerView implements OnRecyclerItemClickListener {

    public static final String TAG = MultiLevelRecyclerView.class.getName();

    Context mContext;

    private boolean isExpanded = false;

    private boolean accordion = false;

    private int prevClickedPosition = -1;

    private int numberOfItemsAdded = 0;

    MultiLevelAdapter mMultiLevelAdapter;

    RecyclerItemClickListener recyclerItemClickListener;

    private OnRecyclerItemClickListener onRecyclerItemClickListener;

    public void setAccordion(boolean accordion) {
        this.accordion = accordion;
    }

    public void setOnItemClick(OnRecyclerItemClickListener onItemClick) {
        this.onRecyclerItemClickListener = onItemClick;
    }

    public MultiLevelRecyclerView(Context context) {
        super(context);
        mContext = context;
        setUp(context);
    }

    public MultiLevelRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setUp(context);
    }

    public MultiLevelRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setUp(context);
    }

    private void setUp(Context context) {
        recyclerItemClickListener = new RecyclerItemClickListener(context);

        recyclerItemClickListener.setOnItemClick(this);

        addOnItemTouchListener(recyclerItemClickListener);

        setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public void setItemAnimator(ItemAnimator animator) {
        super.setItemAnimator(animator);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        if (!(adapter instanceof MultiLevelAdapter)) {
            throw new IllegalStateException("Please Set Adapter Of the MultiLevelAdapter Class.");
        }
        mMultiLevelAdapter = (MultiLevelAdapter) adapter;
        super.setAdapter(adapter);
    }


    public void removeAllChildren(List<RecyclerViewItem> list) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).isExpanded()) {
                removeAllChildren(list.get(i).getChildren());
                removePrevItems(mMultiLevelAdapter.getRecyclerViewItemList(), list.get(i).getPosition(), list.get(i).getChildren().size());
            }
        }
    }

    private int getExpandedPosition(int level) {

        List<RecyclerViewItem> adapterList = mMultiLevelAdapter.getRecyclerViewItemList();
        for (int i = 0; i < adapterList.size(); i++) {

            if (level == adapterList.get(i).getLevel()) {

                if (adapterList.get(i).isExpanded()) {
                    return i;
                }
            }
        }

        return -1;
    }

    private int getItemsToBeRemoved(int level) {
        List<RecyclerViewItem> adapterList = mMultiLevelAdapter.getRecyclerViewItemList();
        int itemsToRemove = 0;
        for (int j = 0; j < adapterList.size(); j++) {
            if (level < adapterList.get(j).getLevel()) {
                itemsToRemove++;
            }
        }
        return itemsToRemove;
    }


    @Override
    public void onItemClick(View view, RecyclerViewItem clickedItem, int position) {

        List<RecyclerViewItem> adapterList = mMultiLevelAdapter.getRecyclerViewItemList();

        if (accordion && prevClickedPosition != -1) {

            if (clickedItem.isExpanded()) {
                clickedItem.setExpanded(false);
                removeAllChildren(clickedItem.getChildren());
                removePrevItems(adapterList, position, clickedItem.getChildren().size());
                if (onRecyclerItemClickListener != null)
                    onRecyclerItemClickListener.onItemClick(view, clickedItem, position);
                return;
            }

            int i = getExpandedPosition(clickedItem.getLevel());

            int itemsToRemove = getItemsToBeRemoved(clickedItem.getLevel());

            if (i != -1) {
                removePrevItems(adapterList, i, itemsToRemove);

                adapterList.get(i).setExpanded(false);

                if (clickedItem.getPosition() > adapterList.get(i).getPosition()) {
                    addItems(clickedItem, adapterList, position - itemsToRemove);
                } else {
                    addItems(clickedItem, adapterList, position);
                }
                return;
            }

        }


        if (clickedItem.isExpanded()) {
            clickedItem.setExpanded(false);
            removeAllChildren(clickedItem.getChildren());
            removePrevItems(adapterList, position, clickedItem.getChildren().size());
            prevClickedPosition = -1;
            numberOfItemsAdded = 0;
        } else {
            if (clickedItem.isExpanded()) {
                removePrevItems(adapterList, prevClickedPosition, numberOfItemsAdded);
                addItems(clickedItem, adapterList, clickedItem.getPosition());
            } else {
                addItems(clickedItem, adapterList, position);
            }
        }

        if (onRecyclerItemClickListener != null)
            onRecyclerItemClickListener.onItemClick(view, clickedItem, position);
    }

    private void removePrevItems(List<RecyclerViewItem> tempList, int position, int numberOfItemsAdded) {
        for (int i = 0; i < numberOfItemsAdded; i++) {
            tempList.remove(position + 1);
        }
        isExpanded = false;
        mMultiLevelAdapter.setRecyclerViewItemList(tempList);
        mMultiLevelAdapter.notifyItemRangeRemoved(position + 1, numberOfItemsAdded);

        refreshPosition();
    }

    public void refreshPosition() {
        for (int i = 0; i < mMultiLevelAdapter.getRecyclerViewItemList().size(); i++) {
            mMultiLevelAdapter.getRecyclerViewItemList().get(i).setPosition(i);
        }
    }

    public RecyclerViewItem getParentOfItem(RecyclerViewItem item) {

        int i;
        List<RecyclerViewItem> list = mMultiLevelAdapter.getRecyclerViewItemList();
        if (item.getLevel() == 1) {
            return list.get(item.getPosition());
        } else {
            int l;
            for (i = item.getPosition(); ; i--) {
                l = list.get(i).getLevel();
                if (l == item.getLevel() - 1) {
                    break;
                }
            }
        }
        return list.get(i);
    }

    private void addItems(RecyclerViewItem clickedItem, List<RecyclerViewItem> tempList, int position) {

        if (clickedItem.hasChildren()) {
            isExpanded = true;

            prevClickedPosition = position;

            tempList.addAll(position + 1, clickedItem.getChildren());

            clickedItem.setExpanded(true);

            numberOfItemsAdded = clickedItem.getChildren().size();

            mMultiLevelAdapter.setRecyclerViewItemList(tempList);

            mMultiLevelAdapter.notifyItemRangeInserted(position + 1, clickedItem.getChildren().size());

            refreshPosition();

        }
    }

    private final class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector mGestureDetector;

        private OnRecyclerItemClickListener onItemClick;

        public void setOnItemClick(OnRecyclerItemClickListener onItemClick) {
            this.onItemClick = onItemClick;
        }

        public RecyclerItemClickListener(Context context) {

            mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
            View childView = view.findChildViewUnder(e.getX(), e.getY());
            if (childView != null && mGestureDetector.onTouchEvent(e)) {
                int position = view.getChildAdapterPosition(childView);
                if (Constants.IS_LOG_ENABLED) {
                    Log.e(TAG, position + " Clicked On RecyclerView");
                }
                if (onItemClick != null) {
                    onItemClick.onItemClick(childView, mMultiLevelAdapter.getRecyclerViewItemList().get(position), position);
                }


                return true;
            }
            return false;
        }


        @Override
        public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean arg0) {

        }

    }

}
