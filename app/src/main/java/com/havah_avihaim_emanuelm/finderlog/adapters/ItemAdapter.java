package com.havah_avihaim_emanuelm.finderlog.adapters;

import static com.havah_avihaim_emanuelm.finderlog.repositories.Repositories.getMatchRepo;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.havah_avihaim_emanuelm.finderlog.R;
import com.havah_avihaim_emanuelm.finderlog.repositories.ItemRepository;
import com.havah_avihaim_emanuelm.finderlog.firebase.FirestoreService;
import com.havah_avihaim_emanuelm.finderlog.items.FoundItem;
import com.havah_avihaim_emanuelm.finderlog.items.Item;
import com.havah_avihaim_emanuelm.finderlog.items.LostItem;
import com.havah_avihaim_emanuelm.finderlog.utils.NetworkAwareDataLoader;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {
    private static final int TYPE_FOUND = 0;
    private static final int TYPE_LOST = 1;
    private final ItemRepository repository;
    protected static final FirestoreService firestoreService = FirestoreService.getSharedInstance();
    private final OnItemListChangedListener listChangedListener;
    private final boolean showDeleteButton;

    public ItemAdapter(ItemRepository repository, OnItemListChangedListener listener,boolean showDeleteButton) {
        this.repository = repository;
        this.listChangedListener = listener;
        this.showDeleteButton = showDeleteButton;
    }

    // Called when the item list changes.
    public interface OnItemListChangedListener {
        void onListChanged();
    }

    // ViewHolder class holds references to UI components for found and lost items and binds data to them.
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

        // Binds an Item to the ViewHolder UI, setting text, images, and handling expand/collapse and delete actions.
        public void bind(Item item, int position, ItemRepository repository,
                         RecyclerView.Adapter<ViewHolder>  adapter, OnItemListChangedListener listChangedListener,boolean showDeleteButton) {
            title.setText(item.getTitle());
            String desc = item.getDescription();
            description.setText((desc == null || desc.isEmpty()) ? "No description available." : desc);
            status.setText(itemView.getContext().getString(R.string.status_label, item.getStatus()));

            if (type == TYPE_FOUND && item instanceof FoundItem) {
                FoundItem foundItem = (FoundItem) item;
                reportDate.setText(itemView.getContext().getString(R.string.found_label, foundItem.getReportDate()));

                Glide.with(itemView.getContext())
                        .load(foundItem.getImgPath())
                        .into(imageView);
            }
            if (type == TYPE_LOST && item instanceof LostItem) {
                LostItem lostItem = (LostItem) item;

                clientName.setText(itemView.getContext().getString(R.string.name_label, lostItem.getClientName()));
                clientPhone.setText(itemView.getContext().getString(R.string.phone_label, lostItem.getClientPhone()));
                lostDate.setText(itemView.getContext().getString(R.string.lost_label, lostItem.getLostDate()));
                reportDate.setText(itemView.getContext().getString(R.string.reported_label, lostItem.getReportDate()));

                // Toggle expand/collapse
                itemView.setOnClickListener(v ->
                        expandedLayout.setVisibility(expandedLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE));

                if (showDeleteButton) {
                    deleteButton.setVisibility(View.VISIBLE);
                    deleteButton.setOnClickListener(v -> {
                        if (NetworkAwareDataLoader.isNetworkAvailable(v.getContext())) {
                            firestoreService.deleteLostItemFromMatches(lostItem); //remove item from match in db
                            firestoreService.deleteItem(LostItem.class, lostItem.getId()); //remove item from lost in db
                            getMatchRepo().removeLostItem(lostItem);
                            repository.removeItemAt(position); // remove item from lostItem
                            adapter.notifyItemRemoved(position);
                            if (repository.getSize() == 0 && listChangedListener != null) {
                                listChangedListener.onListChanged();
                            }

                        } else {
                            new AlertDialog.Builder(v.getContext())
                                    .setTitle("No internet connection")
                                    .setMessage("Failed to delete. \nPlease check your internet connection and try again.")
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    });
                }
            }
        }
    }


    // Returns the view type (found or lost) based on the item at the given position.
    @Override
    public int getItemViewType(int position) {
        Item item = repository.getItemAt(position);
        if (item instanceof FoundItem) return TYPE_FOUND;
        else return TYPE_LOST;
    }

    // Creates a new ViewHolder based on the view type, inflating the corresponding layout.
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

    // Binds the item data at the given position to the provided ViewHolder.
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(repository.getItemAt(position), position, repository, this, listChangedListener,showDeleteButton);
    }

    // Returns the total number of items in the repository.
    @Override
    public int getItemCount() {
        return repository.getSize();
    }

}
