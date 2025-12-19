package com.example.focusscrambler; // Use your actual package name

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CalendarDateAdapter extends RecyclerView.Adapter<CalendarDateAdapter.DateViewHolder> {

    private List<CalendarDate> dateList;

    public CalendarDateAdapter(List<CalendarDate> dateList) {
        this.dateList = dateList;
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_date, parent, false);
        return new DateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        CalendarDate date = dateList.get(position);

        holder.tvDayOfWeek.setText(date.getDayOfWeek());
        holder.tvDateNumber.setText(String.valueOf(date.getDateNumber()));

        // Handle "TODAY" marker visibility
        if (date.isToday()) {
            holder.tvTodayMarker.setVisibility(View.VISIBLE);
            holder.itemView.setSelected(true); // Set item as selected to trigger background selector
            // Optional: Change text color for today
            holder.tvDayOfWeek.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.white));
            holder.tvDateNumber.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.white));
        } else {
            holder.tvTodayMarker.setVisibility(View.GONE);
            holder.itemView.setSelected(false); // Not today, so not selected by default
            // Optional: Reset text color for non-today items
            holder.tvDayOfWeek.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.calendar_text_color_selector)); // Use your selector
            holder.tvDateNumber.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.calendar_text_color_selector)); // Use your selector
        }

        // Future: If you implement a single selected item logic, you'd handle it here
        // if (date.isSelected()) {
        //    holder.itemView.setBackgroundResource(R.drawable.calendar_item_bg_selected);
        // } else {
        //    holder.itemView.setBackgroundResource(R.drawable.calendar_item_bg_default);
        // }
    }

    @Override
    public int getItemCount() {
        return dateList.size();
    }

    public static class DateViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayOfWeek;
        TextView tvDateNumber;
        TextView tvTodayMarker; // New: Today marker

        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayOfWeek = itemView.findViewById(R.id.tvDayOfWeek);
            tvDateNumber = itemView.findViewById(R.id.tvDateNumber);
            tvTodayMarker = itemView.findViewById(R.id.tvTodayMarker); // Bind the new TextView
        }
    }
}