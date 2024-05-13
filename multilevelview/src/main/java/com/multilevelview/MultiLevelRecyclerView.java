package com.multilevelview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.multilevelview.animators.DefaultItemAnimator;
import com.multilevelview.models.RecyclerViewItem;

import java.util.List;


public class MultiLevelRecyclerView extends RecyclerView implements OnRecyclerItemClickListener {

    public static final String TAG = MultiLevelRecyclerView.class.getName();

    Context mContext;

    private boolean isExpanded = false, accordion = false;

    private int prevClickedPosition = -1, numberOfItemsAdded = 0;

    private MultiLevelAdapter mMultiLevelAdapter;

    private boolean isToggleItemOnClick = true;

    private RecyclerItemClickListener recyclerItemClickListener;

    private OnRecyclerItemClickListener onRecyclerItemClickListener;

    public void setAccordion(boolean accordion) {
        this.accordion = accordion;
    }

    public void setOnItemClick(OnRecyclerItemClickListener onItemClick) {
        this.onRecyclerItemClickListener = onItemClick;
    }

    public void setToggleItemOnClick(boolean toggleItemOnClick) {
        isToggleItemOnClick = toggleItemOnClick;
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

    public void removeItemClickListeners() {
        if (recyclerItemClickListener != null) {
            removeOnItemTouchListener(recyclerItemClickListener);
        }
    }

    @Override
    public void setItemAnimator(RecyclerView.ItemAnimator animator) {
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
        for (RecyclerViewItem i : list) {
            if (i.isExpanded()) {
                i.setExpanded(false);
                removeAllChildren(i.getChildren());
                removePrevItems(mMultiLevelAdapter.getRecyclerViewItemList(), i.getPosition(), i.getChildren().size());
            }
        }
    }

    private int getExpandedPosition(int level) {
        List<RecyclerViewItem> adapterList = mMultiLevelAdapter.getRecyclerViewItemList();
        for (RecyclerViewItem i : adapterList) {
            if (level == i.getLevel() && i.isExpanded()) {
                return adapterList.indexOf(i);
            }
        }

        return -1;
    }

    private int getItemsToBeRemoved(int level) {
        List<RecyclerViewItem> adapterList = mMultiLevelAdapter.getRecyclerViewItemList();
        int itemsToRemove = 0;
        for (RecyclerViewItem i : adapterList) {
            if (level < i.getLevel()) {
                itemsToRemove++;
            }
        }
        return itemsToRemove;
    }

    public void openTill(int... positions) {
        if(mMultiLevelAdapter ==null){
            return;
        }
        List<RecyclerViewItem> adapterList = mMultiLevelAdapter.getRecyclerViewItemList();
        if (adapterList == null || positions.length <=0) {
            return;
        }
        int posToAdd = 0;
        int insidePosStart =  -1;
        int insidePosEnd = adapterList.size();
        for (int position : positions) {
            posToAdd += position;
            if(posToAdd > insidePosStart && posToAdd < insidePosEnd){
                addItems(adapterList.get(posToAdd), adapterList, posToAdd);
                insidePosStart= posToAdd;
                if(adapterList.get(posToAdd).getChildren()==null){
                    break;
                }
                insidePosEnd = adapterList.get(posToAdd).getChildren().size();
                posToAdd+=1;
            }
        }
    }

    public void toggleItemsGroup(int position){
        if (position == -1) return;

        List<RecyclerViewItem> adapterList = mMultiLevelAdapter.getRecyclerViewItemList();

        if(position < adapterList.size()){
            RecyclerViewItem clickedItem = adapterList.get(position);

            if (accordion) {
                if (clickedItem.isExpanded()) {
                    clickedItem.setExpanded(false);
                    removeAllChildren(clickedItem.getChildren());
                    removePrevItems(adapterList, position, clickedItem.getChildren().size());
                    prevClickedPosition = -1;
                    numberOfItemsAdded = 0;
                }else{
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
                    }else{
                        addItems(clickedItem, adapterList, position);
                    }
                }
            }else{
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
            }
        }
    }


    @Override
    public void onItemClick(View view, RecyclerViewItem clickedItem, int position) {
        if(isToggleItemOnClick){
            toggleItemsGroup(position);
        }
        if (onRecyclerItemClickListener != null)
            onRecyclerItemClickListener.onItemClick(view, clickedItem, position);
    }

    private void removePrevItems(List<RecyclerViewItem> tempList, int position, int numberOfItemsAdded) {
        for (int i = 0; i < numberOfItemsAdded; i++) {
            if((position + 1) <= (tempList.size() - 1)){
                tempList.get(position + 1).setExpanded(false);
                tempList.remove(position + 1);
            }else{
                tempList.get(position).setExpanded(false);
                tempList.remove(position);
            }

        }
        isExpanded = false;
        mMultiLevelAdapter.setRecyclerViewItemList(tempList);
        mMultiLevelAdapter.notifyItemRangeRemoved(position + 1, numberOfItemsAdded);

        refreshPosition();
    }

    public void refreshPosition() {
        int position = 0;
        for (RecyclerViewItem i : mMultiLevelAdapter.getRecyclerViewItemList()) {
            i.setPosition(position++);
        }
    }

    public RecyclerViewItem getParentOfItem(RecyclerViewItem item) {
        try {
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
        }catch (ArrayIndexOutOfBoundsException e){
            return null;
        }
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

            smoothScrollToPosition(position);
            refreshPosition();

        }
    }

    private final class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector mGestureDetector;

        private OnRecyclerItemClickListener onItemClick;

        void setOnItemClick(OnRecyclerItemClickListener onItemClick) {
            this.onItemClick = onItemClick;
        }

        RecyclerItemClickListener(Context context) {

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
                childView.performClick();
                int position = view.getChildAdapterPosition(childView);
                if (Constants.IS_LOG_ENABLED) {
                    Log.e(TAG, position + " Clicked On RecyclerView");
                }
                if (onItemClick != null) {
                    onItemClick.onItemClick(childView, mMultiLevelAdapter.getRecyclerViewItemList().get(position), position);
                }


                return isToggleItemOnClick;
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
