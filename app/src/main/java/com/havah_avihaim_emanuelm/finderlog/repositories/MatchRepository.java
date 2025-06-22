package com.havah_avihaim_emanuelm.finderlog.repositories;

import com.havah_avihaim_emanuelm.finderlog.items.Item;
import com.havah_avihaim_emanuelm.finderlog.items.LostItem;
import com.havah_avihaim_emanuelm.finderlog.matches.Match;

import java.util.ArrayList;
import java.util.List;

public class MatchRepository {
    private final List<Match> matches = new ArrayList<>();
    private boolean needsLoading = true;

    public boolean needsLoading() {
        return needsLoading;
    }

    // Replaces the current match list with a new one and marks loading as complete.
    public void setMatches(List<Match> matchList) {
        matches.clear();
        matches.addAll(matchList);
        needsLoading = false;
    }

    // Adds a new match to the beginning of the match list.
    public void addMatch(Match match) {
        matches.add(0, match);
    }

    // Adds a LostItem to a specific existing match instance.
    public void addLostItem(LostItem lostItem, Match matchInstance) {
        for (Match match : matches) {
            if (match == matchInstance) {
                match.addLostItem(lostItem);
                return;
            }
        }
    }

    // Removes a LostItem from all matches in which it appears.
    public void removeLostItem(Item lostItem) {
        List<Match> toRemove = new ArrayList<>();
        for (Match match : matches) {
            match.deleteLostItem(lostItem);
            if (match.getLostItems().isEmpty()) {
                toRemove.add(match);
            }
        }
        matches.removeAll(toRemove);
    }

    // Returns the full list of matches.
    public List<Match> getMatches() {
        return matches;
    }

}
