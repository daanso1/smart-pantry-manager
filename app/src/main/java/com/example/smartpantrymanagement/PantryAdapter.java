package com.example.smartpantrymanagement;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Custom RecyclerView adapter that displays pantry items from the database.
 * Each row can be tapped to edit, or deleted with the bin button.
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolder> {

    public interface Listener {
        void onEdit(PantryItem item);
        void onDelete(PantryItem item);
    }

    private static final int EXPIRING_SOON_DAYS = 3;
    private static final int RED = 0xFFD32F2F;

    private List<PantryItem> items;
    private boolean alertsEnabled;
    private final Listener listener;

    public PantryAdapter(List<PantryItem> items, boolean alertsEnabled, Listener listener) {
        this.items = items;
        this.alertsEnabled = alertsEnabled;
        this.listener = listener;
    }

    /** Replaces the data and redraws the list. */
    public void setItems(List<PantryItem> items, boolean alertsEnabled) {
        this.items = items;
        this.alertsEnabled = alertsEnabled;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        final PantryItem item = items.get(position);
        h.tvName.setText(item.getName());
        h.tvQty.setText(IngredientMatcher.formatQty(item.getQuantity()) + " " + item.getUnit());

        String exp = item.getExpiryDate();
        h.tvExpiry.setTextColor(h.defaultColor);
        if (exp == null || exp.isEmpty()) {
            h.tvExpiry.setText("No expiry date");
        } else {
            h.tvExpiry.setText("Expires: " + exp);
            if (alertsEnabled) {
                long days = daysUntil(exp);
                if (days < 0) {
                    h.tvExpiry.setText("EXPIRED: " + exp);
                    h.tvExpiry.setTextColor(RED);
                } else if (days <= EXPIRING_SOON_DAYS) {
                    h.tvExpiry.setText("Expiring soon: " + exp);
                    h.tvExpiry.setTextColor(RED);
                }
            }
        }

        h.itemView.setOnClickListener(v -> listener.onEdit(item));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** Whole days from today until the given yyyy-MM-dd date. */
    private long daysUntil(String date) {
        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(date);
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            return Math.round((d.getTime() - today.getTimeInMillis()) / 86400000.0);
        } catch (ParseException e) {
            return Long.MAX_VALUE;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName, tvQty, tvExpiry;
        final ImageButton btnDelete;
        final int defaultColor;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_item_name);
            tvQty = itemView.findViewById(R.id.tv_item_qty);
            tvExpiry = itemView.findViewById(R.id.tv_item_expiry);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            defaultColor = tvExpiry.getCurrentTextColor();
        }
    }
}

