package com.multilevelview.animators;
/*
 * Copyright (C) 2015 Wasabeef
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import androidx.core.view.ViewCompat;
import androidx.core.view.ViewPropertyAnimatorCompat;
import androidx.core.view.ViewPropertyAnimatorListener;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseItemAnimator extends SimpleItemAnimator {

    private static final boolean DEBUG = false;

    private ArrayList<RecyclerView.ViewHolder> mPendingRemovals = new ArrayList<>();
    private ArrayList<RecyclerView.ViewHolder> mPendingAdditions = new ArrayList<>();
    private ArrayList<MoveInfo> mPendingMoves = new ArrayList<>();
    private ArrayList<ChangeInfo> mPendingChanges = new ArrayList<>();

    private ArrayList<ArrayList<RecyclerView.ViewHolder>> mAdditionsList = new ArrayList<>();
    private ArrayList<ArrayList<MoveInfo>> mMovesList = new ArrayList<>();
    private ArrayList<ArrayList<ChangeInfo>> mChangesList = new ArrayList<>();

    protected ArrayList<RecyclerView.ViewHolder> mAddAnimations = new ArrayList<>();
    private ArrayList<RecyclerView.ViewHolder> mMoveAnimations = new ArrayList<>();
    protected ArrayList<RecyclerView.ViewHolder> mRemoveAnimations = new ArrayList<>();
    private ArrayList<RecyclerView.ViewHolder> mChangeAnimations = new ArrayList<>();

    protected Interpolator mInterpolator = new LinearInterpolator();

    private static class MoveInfo {

        public RecyclerView.ViewHolder holder;
        public int fromX, fromY, toX, toY;

        private MoveInfo(RecyclerView.ViewHolder holder, int fromX, int fromY, int toX, int toY) {
            this.holder = holder;
            this.fromX = fromX;
            this.fromY = fromY;
            this.toX = toX;
            this.toY = toY;
        }
    }

    private static class ChangeInfo {

        public RecyclerView.ViewHolder oldHolder, newHolder;
        public int fromX, fromY, toX, toY;

        private ChangeInfo(RecyclerView.ViewHolder oldHolder, RecyclerView.ViewHolder newHolder) {
            this.oldHolder = oldHolder;
            this.newHolder = newHolder;
        }

        private ChangeInfo(RecyclerView.ViewHolder oldHolder, RecyclerView.ViewHolder newHolder, int fromX, int fromY, int toX,
                           int toY) {
            this(oldHolder, newHolder);
            this.fromX = fromX;
            this.fromY = fromY;
            this.toX = toX;
            this.toY = toY;
        }

        @Override
        public String toString() {
            return "ChangeInfo{" +
                    "oldHolder=" + oldHolder +
                    ", newHolder=" + newHolder +
                    ", fromX=" + fromX +
                    ", fromY=" + fromY +
                    ", toX=" + toX +
                    ", toY=" + toY +
                    '}';
        }
    }

    public BaseItemAnimator() {
        super();
        setSupportsChangeAnimations(false);
    }

    public void setInterpolator(Interpolator mInterpolator) {
        this.mInterpolator = mInterpolator;
    }

    @Override
    public void runPendingAnimations() {
        boolean removalsPending = !mPendingRemovals.isEmpty();
        boolean movesPending = !mPendingMoves.isEmpty();
        boolean changesPending = !mPendingChanges.isEmpty();
        boolean additionsPending = !mPendingAdditions.isEmpty();
        if (!removalsPending && !movesPending && !additionsPending && !changesPending) {
            // nothing to animate
            return;
        }
        // First, remove stuff
        for (RecyclerView.ViewHolder holder : mPendingRemovals) {
            doAnimateRemove(holder);
        }
        mPendingRemovals.clear();
        // Next, move stuff
        if (movesPending) {
            final ArrayList<MoveInfo> moves = new ArrayList<MoveInfo>();
            moves.addAll(mPendingMoves);
            mMovesList.add(moves);
            mPendingMoves.clear();
            Runnable mover = new Runnable() {
                @Override
                public void run() {
                    for (MoveInfo moveInfo : moves) {
                        animateMoveImpl(moveInfo.holder, moveInfo.fromX, moveInfo.fromY, moveInfo.toX,
                                moveInfo.toY);
                    }
                    moves.clear();
                    mMovesList.remove(moves);
                }
            };
            if (removalsPending) {
                View view = moves.get(0).holder.itemView;
                ViewCompat.postOnAnimationDelayed(view, mover, getRemoveDuration());
            } else {
                mover.run();
            }
        }
        // Next, change stuff, to run in parallel with move animations
        if (changesPending) {
            final ArrayList<ChangeInfo> changes = new ArrayList<ChangeInfo>();
            changes.addAll(mPendingChanges);
            mChangesList.add(changes);
            mPendingChanges.clear();
            Runnable changer = new Runnable() {
                @Override
                public void run() {
                    for (ChangeInfo change : changes) {
                        animateChangeImpl(change);
                    }
                    changes.clear();
                    mChangesList.remove(changes);
                }
            };
            if (removalsPending) {
                RecyclerView.ViewHolder holder = changes.get(0).oldHolder;
                ViewCompat.postOnAnimationDelayed(holder.itemView, changer, getRemoveDuration());
            } else {
                changer.run();
            }
        }
        // Next, add stuff
        if (additionsPending) {
            final ArrayList<RecyclerView.ViewHolder> additions = new ArrayList<RecyclerView.ViewHolder>();
            additions.addAll(mPendingAdditions);
            mAdditionsList.add(additions);
            mPendingAdditions.clear();
            Runnable adder = new Runnable() {
                public void run() {
                    for (RecyclerView.ViewHolder holder : additions) {
                        doAnimateAdd(holder);
                    }
                    additions.clear();
                    mAdditionsList.remove(additions);
                }
            };
            if (removalsPending || movesPending || changesPending) {
                long removeDuration = removalsPending ? getRemoveDuration() : 0;
                long moveDuration = movesPending ? getMoveDuration() : 0;
                long changeDuration = changesPending ? getChangeDuration() : 0;
                long totalDelay = removeDuration + Math.max(moveDuration, changeDuration);
                View view = additions.get(0).itemView;
                ViewCompat.postOnAnimationDelayed(view, adder, totalDelay);
            } else {
                adder.run();
            }
        }
    }

    protected void preAnimateRemoveImpl(final RecyclerView.ViewHolder holder) {
    }

    protected void preAnimateAddImpl(final RecyclerView.ViewHolder holder) {
    }

    protected abstract void animateRemoveImpl(final RecyclerView.ViewHolder holder);

    protected abstract void animateAddImpl(final RecyclerView.ViewHolder holder);

    private void preAnimateRemove(final RecyclerView.ViewHolder holder) {
        ViewHelper.clear(holder.itemView);

        if (holder instanceof AnimateViewHolder) {
            ((AnimateViewHolder) holder).preAnimateRemoveImpl();
        } else {
            preAnimateRemoveImpl(holder);
        }
    }

    private void preAnimateAdd(final RecyclerView.ViewHolder holder) {
        ViewHelper.clear(holder.itemView);

        if (holder instanceof AnimateViewHolder) {
            ((AnimateViewHolder) holder).preAnimateAddImpl();
        } else {
            preAnimateAddImpl(holder);
        }
    }

    private void doAnimateRemove(final RecyclerView.ViewHolder holder) {
        if (holder instanceof AnimateViewHolder) {
            ((AnimateViewHolder) holder).animateRemoveImpl(new DefaultRemoveVpaListener(holder));
        } else {
            animateRemoveImpl(holder);
        }

        mRemoveAnimations.add(holder);
    }

    private void doAnimateAdd(final RecyclerView.ViewHolder holder) {
        if (holder instanceof AnimateViewHolder) {
            ((AnimateViewHolder) holder).animateAddImpl(new DefaultAddVpaListener(holder));
        } else {
            animateAddImpl(holder);
        }

        mAddAnimations.add(holder);
    }

    @Override
    public boolean animateRemove(final RecyclerView.ViewHolder holder) {
        endAnimation(holder);
        preAnimateRemove(holder);
        mPendingRemovals.add(holder);
        return true;
    }

    protected long getRemoveDelay(final RecyclerView.ViewHolder holder) {
        return Math.abs(holder.getOldPosition() * getRemoveDuration() / 4);
    }

    @Override
    public boolean animateAdd(final RecyclerView.ViewHolder holder) {
        endAnimation(holder);
        preAnimateAdd(holder);
        mPendingAdditions.add(holder);
        return true;
    }

    protected long getAddDelay(final RecyclerView.ViewHolder holder) {
        return Math.abs(holder.getAdapterPosition() * getAddDuration() / 4);
    }

    @Override
    public boolean animateMove(final RecyclerView.ViewHolder holder, int fromX, int fromY, int toX, int toY) {
        final View view = holder.itemView;
        fromX += ViewCompat.getTranslationX(holder.itemView);
        fromY += ViewCompat.getTranslationY(holder.itemView);
        endAnimation(holder);
        int deltaX = toX - fromX;
        int deltaY = toY - fromY;
        if (deltaX == 0 && deltaY == 0) {
            dispatchMoveFinished(holder);
            return false;
        }
        if (deltaX != 0) {
            ViewCompat.setTranslationX(view, -deltaX);
        }
        if (deltaY != 0) {
            ViewCompat.setTranslationY(view, -deltaY);
        }
        mPendingMoves.add(new MoveInfo(holder, fromX, fromY, toX, toY));
        return true;
    }

    private void animateMoveImpl(final RecyclerView.ViewHolder holder, int fromX, int fromY, int toX, int toY) {
        final View view = holder.itemView;
        final int deltaX = toX - fromX;
        final int deltaY = toY - fromY;
        if (deltaX != 0) {
            ViewCompat.animate(view).translationX(0);
        }
        if (deltaY != 0) {
            ViewCompat.animate(view).translationY(0);
        }
        // TODO: make EndActions end listeners instead, since end actions aren't called when
        // vpas are canceled (and can't end them. why?)
        // need listener functionality in VPACompat for this. Ick.
        mMoveAnimations.add(holder);
        final ViewPropertyAnimatorCompat animation = ViewCompat.animate(view);
        animation.setDuration(getMoveDuration()).setListener(new VpaListenerAdapter() {
            @Override
            public void onAnimationStart(View view) {
                dispatchMoveStarting(holder);
            }

            @Override
            public void onAnimationCancel(View view) {
                if (deltaX != 0) {
                    ViewCompat.setTranslationX(view, 0);
                }
                if (deltaY != 0) {
                    ViewCompat.setTranslationY(view, 0);
                }
            }

            @Override
            public void onAnimationEnd(View view) {
                animation.setListener(null);
                dispatchMoveFinished(holder);
                mMoveAnimations.remove(holder);
                dispatchFinishedWhenDone();
            }
        }).start();
    }

    @Override
    public boolean animateChange(RecyclerView.ViewHolder oldHolder, RecyclerView.ViewHolder newHolder, int fromX, int fromY,
                                 int toX, int toY) {
        final float prevTranslationX = ViewCompat.getTranslationX(oldHolder.itemView);
        final float prevTranslationY = ViewCompat.getTranslationY(oldHolder.itemView);
        final float prevAlpha = ViewCompat.getAlpha(oldHolder.itemView);
        endAnimation(oldHolder);
        int deltaX = (int) (toX - fromX - prevTranslationX);
        int deltaY = (int) (toY - fromY - prevTranslationY);
        // recover prev translation state after ending animation
        ViewCompat.setTranslationX(oldHolder.itemView, prevTranslationX);
        ViewCompat.setTranslationY(oldHolder.itemView, prevTranslationY);
        ViewCompat.setAlpha(oldHolder.itemView, prevAlpha);
        if (newHolder != null && newHolder.itemView != null) {
            // carry over translation values
            endAnimation(newHolder);
            ViewCompat.setTranslationX(newHolder.itemView, -deltaX);
            ViewCompat.setTranslationY(newHolder.itemView, -deltaY);
            ViewCompat.setAlpha(newHolder.itemView, 0);
        }
        mPendingChanges.add(new ChangeInfo(oldHolder, newHolder, fromX, fromY, toX, toY));
        return true;
    }

    private void animateChangeImpl(final ChangeInfo changeInfo) {
        final RecyclerView.ViewHolder holder = changeInfo.oldHolder;
        final View view = holder == null ? null : holder.itemView;
        final RecyclerView.ViewHolder newHolder = changeInfo.newHolder;
        final View newView = newHolder != null ? newHolder.itemView : null;
        if (view != null) {
            mChangeAnimations.add(changeInfo.oldHolder);
            final ViewPropertyAnimatorCompat oldViewAnim =
                    ViewCompat.animate(view).setDuration(getChangeDuration());
            oldViewAnim.translationX(changeInfo.toX - changeInfo.fromX);
            oldViewAnim.translationY(changeInfo.toY - changeInfo.fromY);
            oldViewAnim.alpha(0).setListener(new VpaListenerAdapter() {
                @Override
                public void onAnimationStart(View view) {
                    dispatchChangeStarting(changeInfo.oldHolder, true);
                }

                @Override
                public void onAnimationEnd(View view) {
                    oldViewAnim.setListener(null);
                    ViewCompat.setAlpha(view, 1);
                    ViewCompat.setTranslationX(view, 0);
                    ViewCompat.setTranslationY(view, 0);
                    dispatchChangeFinished(changeInfo.oldHolder, true);
                    mChangeAnimations.remove(changeInfo.oldHolder);
                    dispatchFinishedWhenDone();
                }
            }).start();
        }
        if (newView != null) {
            mChangeAnimations.add(changeInfo.newHolder);
            final ViewPropertyAnimatorCompat newViewAnimation = ViewCompat.animate(newView);
            newViewAnimation.translationX(0).translationY(0).setDuration(getChangeDuration()).
                    alpha(1).setListener(new VpaListenerAdapter() {
                @Override
                public void onAnimationStart(View view) {
                    dispatchChangeStarting(changeInfo.newHolder, false);
                }

                @Override
                public void onAnimationEnd(View view) {
                    newViewAnimation.setListener(null);
                    ViewCompat.setAlpha(newView, 1);
                    ViewCompat.setTranslationX(newView, 0);
                    ViewCompat.setTranslationY(newView, 0);
                    dispatchChangeFinished(changeInfo.newHolder, false);
                    mChangeAnimations.remove(changeInfo.newHolder);
                    dispatchFinishedWhenDone();
                }
            }).start();
        }
    }

    private void endChangeAnimation(List<ChangeInfo> infoList, RecyclerView.ViewHolder item) {
        for (int i = infoList.size() - 1; i >= 0; i--) {
            ChangeInfo changeInfo = infoList.get(i);
            if (endChangeAnimationIfNecessary(changeInfo, item)) {
                if (changeInfo.oldHolder == null && changeInfo.newHolder == null) {
                    infoList.remove(changeInfo);
                }
            }
        }
    }

    private void endChangeAnimationIfNecessary(ChangeInfo changeInfo) {
        if (changeInfo.oldHolder != null) {
            endChangeAnimationIfNecessary(changeInfo, changeInfo.oldHolder);
        }
        if (changeInfo.newHolder != null) {
            endChangeAnimationIfNecessary(changeInfo, changeInfo.newHolder);
        }
    }

    private boolean endChangeAnimationIfNecessary(ChangeInfo changeInfo, RecyclerView.ViewHolder item) {
        boolean oldItem = false;
        if (changeInfo.newHolder == item) {
            changeInfo.newHolder = null;
        } else if (changeInfo.oldHolder == item) {
            changeInfo.oldHolder = null;
            oldItem = true;
        } else {
            return false;
        }
        ViewCompat.setAlpha(item.itemView, 1);
        ViewCompat.setTranslationX(item.itemView, 0);
        ViewCompat.setTranslationY(item.itemView, 0);
        dispatchChangeFinished(item, oldItem);
        return true;
    }

    @Override
    public void endAnimation(RecyclerView.ViewHolder item) {
        final View view = item.itemView;
        // this will trigger end callback which should set properties to their target values.
        ViewCompat.animate(view).cancel();
        // TODO if some other animations are chained to end, how do we cancel them as well?
        for (int i = mPendingMoves.size() - 1; i >= 0; i--) {
            MoveInfo moveInfo = mPendingMoves.get(i);
            if (moveInfo.holder == item) {
                ViewCompat.setTranslationY(view, 0);
                ViewCompat.setTranslationX(view, 0);
                dispatchMoveFinished(item);
                mPendingMoves.remove(i);
            }
        }
        endChangeAnimation(mPendingChanges, item);
        if (mPendingRemovals.remove(item)) {
            ViewHelper.clear(item.itemView);
            dispatchRemoveFinished(item);
        }
        if (mPendingAdditions.remove(item)) {
            ViewHelper.clear(item.itemView);
            dispatchAddFinished(item);
        }

        for (int i = mChangesList.size() - 1; i >= 0; i--) {
            ArrayList<ChangeInfo> changes = mChangesList.get(i);
            endChangeAnimation(changes, item);
            if (changes.isEmpty()) {
                mChangesList.remove(i);
            }
        }
        for (int i = mMovesList.size() - 1; i >= 0; i--) {
            ArrayList<MoveInfo> moves = mMovesList.get(i);
            for (int j = moves.size() - 1; j >= 0; j--) {
                MoveInfo moveInfo = moves.get(j);
                if (moveInfo.holder == item) {
                    ViewCompat.setTranslationY(view, 0);
                    ViewCompat.setTranslationX(view, 0);
                    dispatchMoveFinished(item);
                    moves.remove(j);
                    if (moves.isEmpty()) {
                        mMovesList.remove(i);
                    }
                    break;
                }
            }
        }
        for (int i = mAdditionsList.size() - 1; i >= 0; i--) {
            ArrayList<RecyclerView.ViewHolder> additions = mAdditionsList.get(i);
            if (additions.remove(item)) {
                ViewHelper.clear(item.itemView);
                dispatchAddFinished(item);
                if (additions.isEmpty()) {
                    mAdditionsList.remove(i);
                }
            }
        }

        // animations should be ended by the cancel above.
        if (mRemoveAnimations.remove(item) && DEBUG) {
            throw new IllegalStateException(
                    "after animation is cancelled, item should not be in " + "mRemoveAnimations list");
        }

        if (mAddAnimations.remove(item) && DEBUG) {
            throw new IllegalStateException(
                    "after animation is cancelled, item should not be in " + "mAddAnimations list");
        }

        if (mChangeAnimations.remove(item) && DEBUG) {
            throw new IllegalStateException(
                    "after animation is cancelled, item should not be in " + "mChangeAnimations list");
        }

        if (mMoveAnimations.remove(item) && DEBUG) {
            throw new IllegalStateException(
                    "after animation is cancelled, item should not be in " + "mMoveAnimations list");
        }
        dispatchFinishedWhenDone();
    }

    @Override
    public boolean isRunning() {
        return (!mPendingAdditions.isEmpty() ||
                !mPendingChanges.isEmpty() ||
                !mPendingMoves.isEmpty() ||
                !mPendingRemovals.isEmpty() ||
                !mMoveAnimations.isEmpty() ||
                !mRemoveAnimations.isEmpty() ||
                !mAddAnimations.isEmpty() ||
                !mChangeAnimations.isEmpty() ||
                !mMovesList.isEmpty() ||
                !mAdditionsList.isEmpty() ||
                !mChangesList.isEmpty());
    }

    /**
     * Check the state of currently pending and running animations. If there are none
     * pending/running, call #dispatchAnimationsFinished() to notify any
     * listeners.
     */
    private void dispatchFinishedWhenDone() {
        if (!isRunning()) {
            dispatchAnimationsFinished();
        }
    }

    @Override
    public void endAnimations() {
        int count = mPendingMoves.size();
        for (int i = count - 1; i >= 0; i--) {
            MoveInfo item = mPendingMoves.get(i);
            View view = item.holder.itemView;
            ViewCompat.setTranslationY(view, 0);
            ViewCompat.setTranslationX(view, 0);
            dispatchMoveFinished(item.holder);
            mPendingMoves.remove(i);
        }
        count = mPendingRemovals.size();
        for (int i = count - 1; i >= 0; i--) {
            RecyclerView.ViewHolder item = mPendingRemovals.get(i);
            dispatchRemoveFinished(item);
            mPendingRemovals.remove(i);
        }
        count = mPendingAdditions.size();
        for (int i = count - 1; i >= 0; i--) {
            RecyclerView.ViewHolder item = mPendingAdditions.get(i);
            ViewHelper.clear(item.itemView);
            dispatchAddFinished(item);
            mPendingAdditions.remove(i);
        }
        count = mPendingChanges.size();
        for (int i = count - 1; i >= 0; i--) {
            endChangeAnimationIfNecessary(mPendingChanges.get(i));
        }
        mPendingChanges.clear();
        if (!isRunning()) {
            return;
        }

        int listCount = mMovesList.size();
        for (int i = listCount - 1; i >= 0; i--) {
            ArrayList<MoveInfo> moves = mMovesList.get(i);
            count = moves.size();
            for (int j = count - 1; j >= 0; j--) {
                MoveInfo moveInfo = moves.get(j);
                RecyclerView.ViewHolder item = moveInfo.holder;
                View view = item.itemView;
                ViewCompat.setTranslationY(view, 0);
                ViewCompat.setTranslationX(view, 0);
                dispatchMoveFinished(moveInfo.holder);
                moves.remove(j);
                if (moves.isEmpty()) {
                    mMovesList.remove(moves);
                }
            }
        }
        listCount = mAdditionsList.size();
        for (int i = listCount - 1; i >= 0; i--) {
            ArrayList<RecyclerView.ViewHolder> additions = mAdditionsList.get(i);
            count = additions.size();
            for (int j = count - 1; j >= 0; j--) {
                RecyclerView.ViewHolder item = additions.get(j);
                View view = item.itemView;
                ViewCompat.setAlpha(view, 1);
                dispatchAddFinished(item);
                //this check prevent exception when removal already happened during finishing animation
                if (j < additions.size()) {
                    additions.remove(j);
                }
                if (additions.isEmpty()) {
                    mAdditionsList.remove(additions);
                }
            }
        }
        listCount = mChangesList.size();
        for (int i = listCount - 1; i >= 0; i--) {
            ArrayList<ChangeInfo> changes = mChangesList.get(i);
            count = changes.size();
            for (int j = count - 1; j >= 0; j--) {
                endChangeAnimationIfNecessary(changes.get(j));
                if (changes.isEmpty()) {
                    mChangesList.remove(changes);
                }
            }
        }

        cancelAll(mRemoveAnimations);
        cancelAll(mMoveAnimations);
        cancelAll(mAddAnimations);
        cancelAll(mChangeAnimations);

        dispatchAnimationsFinished();
    }

    void cancelAll(List<RecyclerView.ViewHolder> viewHolders) {
        for (int i = viewHolders.size() - 1; i >= 0; i--) {
            ViewCompat.animate(viewHolders.get(i).itemView).cancel();
        }
    }

    private static class VpaListenerAdapter implements ViewPropertyAnimatorListener {

        @Override
        public void onAnimationStart(View view) {
        }

        @Override
        public void onAnimationEnd(View view) {
        }

        @Override
        public void onAnimationCancel(View view) {
        }
    }

    protected class DefaultAddVpaListener extends VpaListenerAdapter {

        RecyclerView.ViewHolder mViewHolder;

        public DefaultAddVpaListener(final RecyclerView.ViewHolder holder) {
            mViewHolder = holder;
        }

        @Override
        public void onAnimationStart(View view) {
            dispatchAddStarting(mViewHolder);
        }

        @Override
        public void onAnimationCancel(View view) {
            ViewHelper.clear(view);
        }

        @Override
        public void onAnimationEnd(View view) {
            ViewHelper.clear(view);
            dispatchAddFinished(mViewHolder);
            mAddAnimations.remove(mViewHolder);
            dispatchFinishedWhenDone();
        }
    }

    protected class DefaultRemoveVpaListener extends VpaListenerAdapter {

        RecyclerView.ViewHolder mViewHolder;

        public DefaultRemoveVpaListener(final RecyclerView.ViewHolder holder) {
            mViewHolder = holder;
        }

        @Override
        public void onAnimationStart(View view) {
            dispatchRemoveStarting(mViewHolder);
        }

        @Override
        public void onAnimationCancel(View view) {
            ViewHelper.clear(view);
        }

        @Override
        public void onAnimationEnd(View view) {
            ViewHelper.clear(view);
            dispatchRemoveFinished(mViewHolder);
            mRemoveAnimations.remove(mViewHolder);
            dispatchFinishedWhenDone();
        }
    }
}
