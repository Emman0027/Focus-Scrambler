package com.example.focusscrambler; // Adjust package name

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BreakActivityAdapter extends RecyclerView.Adapter<BreakActivityAdapter.BreakActivityViewHolder> {

    private List<BreakActivity> breakActivities;
    private OnItemActionListener listener;

    public interface OnItemActionListener {
        void onDeleteClick(int position);
        // void onItemClick(int position); // Optional
    }

    public BreakActivityAdapter(List<BreakActivity> breakActivities, OnItemActionListener listener) {
        this.breakActivities = breakActivities;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BreakActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_break_activity, parent, false);
        return new BreakActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BreakActivityViewHolder holder, int position) {
        BreakActivity activity = breakActivities.get(position);
        holder.descriptionTextView.setText(activity.getDescription());

        // Set alternating background colors (optional, if you want that effect)
        if (position % 2 == 0) {
            holder.itemLayout.setBackgroundResource(R.drawable.break_activity_card_bg_1);
        } else {
            // holder.itemLayout.setBackgroundResource(R.drawable.break_activity_card_bg_2); // Use another color if you make bg_2
            holder.itemLayout.setBackgroundResource(R.drawable.break_activity_card_bg_1); // For now, just use one
        }

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return breakActivities.size();
    }

    public static class BreakActivityViewHolder extends RecyclerView.ViewHolder {
        LinearLayout itemLayout;
        TextView descriptionTextView;
        ImageView deleteButton;

        public BreakActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            itemLayout = itemView.findViewById(R.id.break_activity_item_layout);
            descriptionTextView = itemView.findViewById(R.id.tv_break_activity_description);
            deleteButton = itemView.findViewById(R.id.btn_delete_break_activity);
        }
    }
}