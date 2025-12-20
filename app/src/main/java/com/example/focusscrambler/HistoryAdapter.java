package com.example.focusscrambler;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<HistoryItem> historyItems;

    public HistoryAdapter(List<HistoryItem> historyItems) {
        this.historyItems = historyItems;
    }

    @Override
    public int getItemViewType(int position) {
        return historyItems.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == HistoryItem.TYPE_DATE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history_date_header, parent, false);
            return new DateHeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history_activity, parent, false);
            return new ActivityEntryViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        HistoryItem item = historyItems.get(position);

        if (holder instanceof DateHeaderViewHolder) {
            DateHeaderViewHolder dateHolder = (DateHeaderViewHolder) holder;
            HistoryItem.DateHeader dateHeader = (HistoryItem.DateHeader) item;
            dateHolder.tvDateHeader.setText(dateHeader.getDateString());
        } else {
            ActivityEntryViewHolder activityHolder = (ActivityEntryViewHolder) holder;
            HistoryItem.ActivityEntry activityEntry = (HistoryItem.ActivityEntry) item;

            activityHolder.tvDescription.setText(activityEntry.getDescription());
            activityHolder.tvDuration.setText(activityEntry.getFormattedDuration());

            // Set icon and color based on activity type
            if (activityEntry.isFocusSession()) {
                // Focus session icon (use your stopwatch or focus icon)
                activityHolder.ivIcon.setImageResource(android.R.drawable.ic_menu_info_details);
                activityHolder.ivIcon.setColorFilter(ContextCompat.getColor(activityHolder.itemView.getContext(), android.R.color.holo_blue_dark));
            } else {
                // Break activity icon
                activityHolder.ivIcon.setImageResource(android.R.drawable.ic_menu_info_details);
                activityHolder.ivIcon.setColorFilter(ContextCompat.getColor(activityHolder.itemView.getContext(), android.R.color.holo_green_dark));
            }
        }
    }

    @Override
    public int getItemCount() {
        return historyItems.size();
    }

    static class DateHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvDateHeader;

        public DateHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDateHeader = itemView.findViewById(R.id.tv_date_header);
        }
    }

    static class ActivityEntryViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvDescription;
        TextView tvDuration;

        public ActivityEntryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_activity_icon);
            tvDescription = itemView.findViewById(R.id.tv_activity_description);
            tvDuration = itemView.findViewById(R.id.tv_activity_duration);
        }
    }
}