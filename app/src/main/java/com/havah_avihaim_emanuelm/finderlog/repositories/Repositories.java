package com.havah_avihaim_emanuelm.finderlog.repositories;

public class Repositories {
    private static final ItemRepository foundRepo = new ItemRepository();
    private static final ItemRepository lostRepo = new ItemRepository();
    private static final MatchRepository matchRepo = new MatchRepository();

    public static ItemRepository getFoundRepo() {
        return foundRepo;
    }

    public static ItemRepository getLostRepo() {
        return lostRepo;
    }

    public static MatchRepository getMatchRepo() {
        return matchRepo;
    }

}

