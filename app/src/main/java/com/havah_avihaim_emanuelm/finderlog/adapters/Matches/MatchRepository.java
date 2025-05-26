package com.havah_avihaim_emanuelm.finderlog.adapters.Matches;

import com.havah_avihaim_emanuelm.finderlog.firebase.firestore.Item;
import com.havah_avihaim_emanuelm.finderlog.firebase.firestore.LostItem;
import com.havah_avihaim_emanuelm.finderlog.firebase.firestore.Match;

import java.util.ArrayList;
import java.util.List;

public class MatchRepository {
    private final List<Match> matches = new ArrayList<>();
    private boolean needsLoading = true;

    public boolean needsLoading() {
        return needsLoading;
    }

    public void setMatches(List<Match> matchList) {
        matches.clear();
        matches.addAll(matchList);
        needsLoading = false;
    }

    public void addMatch(Match match) {
        matches.add(0, match);
    }

    public void removeMatchAt(int position) {
        if (position >= 0 && position < matches.size()) {
            matches.remove(position);
        }
    }

    public Match getMatchAt(int position) {
        if (position >= 0 && position < matches.size()) {
            return matches.get(position);
        }
        return null;
    }

    public void addLostItem(LostItem lostItem, Match matchInstance) {
        for (Match match : matches) {
            if (match == matchInstance) {
                match.addLostItem(lostItem);
                return;
            }
        }
    }

    public void removeLostItem(Item lostItem) {
        for (Match match : matches) {
            match.deleteLostItem(lostItem);
        }

    }

    public int getSize() {
        return matches.size();
    }

    public List<Match> getMatches() {
        return matches;
    }

    public void clear() {
        matches.clear();
        needsLoading = true;
    }
}
