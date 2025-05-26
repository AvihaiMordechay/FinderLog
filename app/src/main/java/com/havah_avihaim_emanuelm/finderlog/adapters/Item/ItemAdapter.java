package com.havah_avihaim_emanuelm.finderlog.adapters.Item;

import static com.havah_avihaim_emanuelm.finderlog.adapters.Repositories.getMatchRepo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.havah_avihaim_emanuelm.finderlog.R;
import com.havah_avihaim_emanuelm.finderlog.firebase.firestore.FirestoreService;
import com.havah_avihaim_emanuelm.finderlog.firebase.firestore.FoundItem;
import com.havah_avihaim_emanuelm.finderlog.firebase.firestore.Item;
import com.havah_avihaim_emanuelm.finderlog.firebase.firestore.LostItem;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    private static final int TYPE_FOUND = 0;
    private static final int TYPE_LOST = 1;

    private final ItemRepository repository;

    protected static final FirestoreService firestoreService = FirestoreService.getSharedInstance();

    private final OnItemListChangedListener listChangedListener;

    public interface OnItemListChangedListener {
        void onListChanged();
    }
    public ItemAdapter(ItemRepository repository, OnItemListChangedListener listener) {
        this.repository = repository;
        this.listChangedListener = listener;
    }



    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, status, reportDate;
        ImageView imageView; // Only for FoundItem

        TextView clientName, clientPhone, lostDate;
        View expandedLayout;
        View deleteButton;
        int type;

        public ViewHolder(View view, int type) {
            super(view);
            this.type = type;
            title = view.findViewById(R.id.itemTitle);
            description = view.findViewById(R.id.itemDescription);
            status = view.findViewById(R.id.itemStatus);
            reportDate = view.findViewById(R.id.itemReportDate);


            if (type == TYPE_FOUND) {
                imageView = view.findViewById(R.id.itemImage);
            } else {
                // For LostItem
                clientName = view.findViewById(R.id.itemClientName);
                clientPhone = view.findViewById(R.id.itemClientPhone);
                lostDate = view.findViewById(R.id.itemLostDate);
                deleteButton = view.findViewById(R.id.deleteButton);
                expandedLayout = view.findViewById(R.id.expandedLayout);
            }
        }

        public void bind(Item item, int position, ItemRepository repository,
                         RecyclerView.Adapter adapter, OnItemListChangedListener listChangedListener) {
            title.setText(item.getTitle());
            String desc = item.getDescription();
            description.setText((desc == null || desc.isEmpty()) ? "No description available." : desc);
            status.setText("Status: " + item.getStatus());

            if (type == TYPE_FOUND && item instanceof FoundItem) {
                FoundItem foundItem = (FoundItem) item;
                reportDate.setText("Found: " + foundItem.getReportDate());

                Glide.with(itemView.getContext())
                        .load(foundItem.getImgPath())
                        .into(imageView);
            }
            if (type == TYPE_LOST && item instanceof LostItem) {
                LostItem lostItem = (LostItem) item;

                clientName.setText("Name: " + lostItem.getClientName());
                clientPhone.setText("Phone: " + lostItem.getClientPhone());
                lostDate.setText("Lost: " + lostItem.getLostDate());
                reportDate.setText("Reported: " + lostItem.getReportDate());

                // Toggle expand/collapse
                itemView.setOnClickListener(v ->
                        expandedLayout.setVisibility(expandedLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE));

                // Delete button
                deleteButton.setOnClickListener(v -> {
                    repository.removeItemAt(position);
                    adapter.notifyItemRemoved(position);
                    firestoreService.deleteLostItemFromMatches(lostItem);
                    firestoreService.deleteItem(LostItem.class ,lostItem.getId());
                    getMatchRepo().removeLostItem(lostItem);
                    if (repository.getSize() == 0 && listChangedListener != null) {
                        listChangedListener.onListChanged();
                    }

                });
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        Item item = repository.getItemAt(position);
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
        holder.bind(repository.getItemAt(position), position, repository, this, listChangedListener);
    }

    @Override
    public int getItemCount() {
        return repository.getSize();
    }
}
