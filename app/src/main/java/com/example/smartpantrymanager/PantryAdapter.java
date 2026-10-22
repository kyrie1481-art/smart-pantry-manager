package com.example.smartpantrymanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Custom adapter: turns each PantryItem in the list into a card on the screen.
 * The RecyclerView re-uses a small number of card views while you scroll, which keeps it fast.
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    /** The screen that owns the list implements this to react to taps. */
    public interface OnItemActionListener {
        void onEdit(PantryItem item);
        void onDelete(PantryItem item);
    }

    private List<PantryItem> items;
    private boolean showExpiryAlerts = true;
    private final OnItemActionListener listener;

    public PantryAdapter(List<PantryItem> items, OnItemActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    /** Replaces the data and redraws the list. */
    public void setItems(List<PantryItem> newItems, boolean showExpiryAlerts) {
        this.items = newItems;
        this.showExpiryAlerts = showExpiryAlerts;
        notifyDataSetChanged();
    }

    /** Creates a new card view (only called a few times, then views are recycled). */
    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new PantryViewHolder(view);
    }

    /** Fills a card with the data of the item at this position. */
    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        final PantryItem item = items.get(position);

        holder.tvName.setText(item.getName());
        holder.tvQuantity.setText(AppUtils.formatQuantity(item.getQuantity()) + " " + item.getUnit());

        int secondary = ContextCompat.getColor(holder.itemView.getContext(), R.color.text_secondary);
        int warning = ContextCompat.getColor(holder.itemView.getContext(), R.color.warning_amber);
        int error = ContextCompat.getColor(holder.itemView.getContext(), R.color.error_red);

        String expiry = item.getExpiryDate();
        if (expiry == null || expiry.isEmpty()) {
            holder.tvExpiry.setText("No expiry date");
            holder.tvExpiry.setTextColor(secondary);
        } else {
            int days = AppUtils.daysUntilExpiry(expiry);
            if (showExpiryAlerts && days < 0 && days != AppUtils.NO_EXPIRY) {
                holder.tvExpiry.setText("Expired: " + expiry);
                holder.tvExpiry.setTextColor(error);
            } else if (showExpiryAlerts && AppUtils.isExpiringSoon(expiry)) {
                holder.tvExpiry.setText("Expires soon: " + expiry);
                holder.tvExpiry.setTextColor(warning);
            } else {
                holder.tvExpiry.setText("Expires: " + expiry);
                holder.tvExpiry.setTextColor(secondary);
            }
        }

        holder.itemView.setOnClickListener(v -> listener.onEdit(item));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** Holds the views of one card so they do not have to be looked up again and again. */
    static class PantryViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName;
        final TextView tvQuantity;
        final TextView tvExpiry;
        final ImageButton btnDelete;

        PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvItemName);
            tvQuantity = itemView.findViewById(R.id.tvItemQuantity);
            tvExpiry = itemView.findViewById(R.id.tvItemExpiry);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
