package com.havah_avihaim_emanuelm.finderlog.firebase.firestore;

public interface Listener<T> {
    void onResult(T data);
}
