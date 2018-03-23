package de.vent_projects.ffg_planner.main.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

public class MainReplacementRecyclerView extends RecyclerView {
    private ArrayList<View> emptyViews;

    private final AdapterDataObserver observer = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            checkIfEmpty();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            checkIfEmpty();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            super.onItemRangeRemoved(positionStart, itemCount);
            checkIfEmpty();
        }
    };

    public MainReplacementRecyclerView(Context context) {
        super(context);
        this.emptyViews = new ArrayList<>();
    }

    public MainReplacementRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.emptyViews = new ArrayList<>();
    }

    public MainReplacementRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.emptyViews = new ArrayList<>();
    }

    private void checkIfEmpty() {
        if (emptyViews != null && getAdapter() != null) {
            for (View view : emptyViews) {
                view.setVisibility(getAdapter().getItemCount() == 0 ? VISIBLE : INVISIBLE);
            }
            setVisibility(getAdapter().getItemCount() == 0 ? INVISIBLE : VISIBLE);
        }
    }

    public void addEmptyView(View emptyView) {
        this.emptyViews.add(emptyView);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        Adapter oldAdapter = getAdapter();
        if (oldAdapter != null) oldAdapter.unregisterAdapterDataObserver(observer);
        super.setAdapter(adapter);
        if (adapter != null) adapter.registerAdapterDataObserver(observer);
        checkIfEmpty();
    }
}
