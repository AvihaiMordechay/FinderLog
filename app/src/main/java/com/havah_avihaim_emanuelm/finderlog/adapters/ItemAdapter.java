package com.havah_avihaim_emanuelm.finderlog.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.havah_avihaim_emanuelm.finderlog.R;
import com.havah_avihaim_emanuelm.finderlog.firebase.firestore.FoundItem;
import com.havah_avihaim_emanuelm.finderlog.firebase.firestore.Item;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    private static final int TYPE_FOUND = 0;
    private static final int TYPE_LOST = 1;

    private final List<Item> items;

    public ItemAdapter(List<Item> items) {
        this.items = items;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, description;
        ImageView imageView; // Only for FoundItem
        int type;

        public ViewHolder(View view, int type) {
            super(view);
            this.type = type;
            title = view.findViewById(R.id.itemTitle);
            description = view.findViewById(R.id.itemDescription);
            if (type == TYPE_FOUND) {
                imageView = view.findViewById(R.id.itemImage);
            }
        }

        public void bind(Item item) {
            String desc = item.getDescription();
            description.setText((desc == null || desc.isEmpty()) ? "No description available." : desc);

            if (type == TYPE_FOUND && item instanceof FoundItem) {
                FoundItem foundItem = (FoundItem) item;
                Glide.with(itemView.getContext())
                        .load(foundItem.getImgPath())
                        .into(imageView);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        Item item = items.get(position);
        if (item instanceof FoundItem) return TYPE_FOUND;
        else return TYPE_LOST;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == TYPE_FOUND) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_found, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_lost, parent, false);
        }
        return new ViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
