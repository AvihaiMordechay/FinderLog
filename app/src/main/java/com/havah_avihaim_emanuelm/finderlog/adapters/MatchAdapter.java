package com.havah_avihaim_emanuelm.finderlog.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.havah_avihaim_emanuelm.finderlog.R;
import com.havah_avihaim_emanuelm.finderlog.repositories.ItemRepository;
import com.havah_avihaim_emanuelm.finderlog.matches.Match;

import java.util.List;

public class MatchAdapter extends RecyclerView.Adapter<MatchAdapter.MatchViewHolder> {
    private final List<Match> matchList;
    private final Context context;

    public MatchAdapter(Context context, List<Match> matchList) {
        this.context = context;
        this.matchList = matchList;
    }

    // Inflates the match item layout and creates a new MatchViewHolder.
    @NonNull
    @Override
    public MatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_match, parent, false);
        return new MatchViewHolder(view);
    }

    // Binds a Match to the ViewHolder, sets title, image, and initializes nested RecyclerView for lost items with expand/collapse behavior.
    @Override
    public void onBindViewHolder(@NonNull MatchViewHolder holder, int position) {
        Match match = matchList.get(position);
        holder.textViewTitle.setText(match.getTitle());

        String imagePath = match.getImgPath();
        if (imagePath != null && !imagePath.isEmpty()) {
            Glide.with(holder.imageViewMatch.getContext())
                    .load(imagePath)
                    .into(holder.imageViewMatch);
        }

        holder.lostItemsRecycler.setLayoutManager(new LinearLayoutManager(context));
        ItemRepository lostItemsRepo = new ItemRepository();
        lostItemsRepo.setItems(match.getLostItems());
        holder.lostItemsRecycler.setAdapter(new ItemAdapter(lostItemsRepo, null,false));

        holder.lostItemsRecycler.setVisibility(holder.isExpanded ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            holder.isExpanded = !holder.isExpanded;
            holder.lostItemsRecycler.setVisibility(holder.isExpanded ? View.VISIBLE : View.GONE);
        });
    }

    // Returns the total number of matches in the list.
    @Override
    public int getItemCount() {
        return matchList.size();
    }

    // Holds references to UI components for a match item and tracks expanded state for showing lost items.
    public static class MatchViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle;
        ImageView imageViewMatch;
        RecyclerView lostItemsRecycler;
        boolean isExpanded = false;

        public MatchViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            imageViewMatch = itemView.findViewById(R.id.imageViewMatch);
            lostItemsRecycler = itemView.findViewById(R.id.lostItemsContainer);
        }
    }

}
