package com.example.focusscrambler;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class BreakActivityAdapter extends RecyclerView.Adapter<BreakActivityAdapter.BreakActivityViewHolder> {

    private List<BreakActivity> breakActivities;
    private OnItemActionListener listener;
    private boolean isDoneList; // NEW: Track if this is pending or done list

    // ✅ UPDATED: Interface now passes isDoneList parameter
    public interface OnItemActionListener {
        void onDeleteClick(int position, boolean isDoneList);
    }

    // ✅ UPDATED: Constructor now accepts isDoneList parameter
    public BreakActivityAdapter(List<BreakActivity> breakActivities, OnItemActionListener listener, boolean isDoneList) {
        this.breakActivities = breakActivities;
        this.listener = listener;
        this.isDoneList = isDoneList;
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

        // ✅ UPDATED: Display duration in seconds format (handles 0-300s range)
        int durationSeconds = activity.getDurationSeconds();
        String durationText;
        if (durationSeconds >= 60) {
            int minutes = durationSeconds / 60;
            int seconds = durationSeconds % 60;
            durationText = String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
        } else {
            durationText = durationSeconds + "s";
        }
        holder.durationTextView.setText(durationText);

        // ✅ NEW: Different styling for pending vs done activities
        if (isDoneList) {
            // Done activities - green checkmark style
            holder.itemLayout.setBackgroundResource(R.drawable.break_activity_card_bg_done);
            holder.descriptionTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.darker_gray));
            holder.durationTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.darker_gray));
        } else {
            // Pending activities - normal style
            holder.itemLayout.setBackgroundResource(R.drawable.break_activity_card_bg_1);
            holder.descriptionTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white));
            holder.durationTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white));
        }

        // ✅ Delete button click with isDoneList parameter
        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(holder.getAdapterPosition(), isDoneList);
            }
        });
    }

    @Override
    public int getItemCount() {
        return breakActivities.size();
    }

    // ✅ NEW: Method to update list data
    public void updateData(List<BreakActivity> newActivities) {
        this.breakActivities = newActivities;
        notifyDataSetChanged();
    }

    public static class BreakActivityViewHolder extends RecyclerView.ViewHolder {
        LinearLayout itemLayout;
        TextView descriptionTextView;
        TextView durationTextView;
        ImageView deleteButton;

        public BreakActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            itemLayout = itemView.findViewById(R.id.break_activity_item_layout);
            descriptionTextView = itemView.findViewById(R.id.tv_break_activity_description);
            durationTextView = itemView.findViewById(R.id.tv_break_activity_duration);
            deleteButton = itemView.findViewById(R.id.btn_delete_break_activity);
        }
    }
}