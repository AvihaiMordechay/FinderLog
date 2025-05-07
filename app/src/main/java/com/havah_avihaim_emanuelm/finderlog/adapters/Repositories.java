package com.havah_avihaim_emanuelm.finderlog.adapters;

public class Repositories {
    private static final ItemRepository foundRepo = new ItemRepository();
    private static final ItemRepository lostRepo = new ItemRepository();

    public static ItemRepository getFoundRepo() {
        return foundRepo;
    }

    public static ItemRepository getLostRepo() {
        return lostRepo;
    }
}

