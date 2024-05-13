package com.multilevelview.animators;


import android.view.View;

import androidx.core.view.ViewPropertyAnimatorListener;
import androidx.recyclerview.widget.RecyclerView;

public abstract class AnimateViewHolder extends RecyclerView.ViewHolder {

  public AnimateViewHolder(View itemView) {
    super(itemView);
  }

  public void preAnimateAddImpl() {
  }

  public void preAnimateRemoveImpl() {
  }

  public abstract void animateAddImpl(ViewPropertyAnimatorListener listener);

  public abstract void animateRemoveImpl(ViewPropertyAnimatorListener listener);
}
