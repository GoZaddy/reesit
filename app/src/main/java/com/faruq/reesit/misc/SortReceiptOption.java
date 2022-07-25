package com.faruq.reesit.misc;


import org.parceler.Parcel;

@Parcel
public class SortReceiptOption {
    private String keyToSort;
    private SortOrder sortOrder;
    private String title;


    public enum SortOrder {
        ASCENDING,
        DESCENDING
    }

    public SortReceiptOption(){}

    public SortReceiptOption(String keyToSort, SortOrder sortOrder, String title) {
        this.keyToSort = keyToSort;
        this.sortOrder = sortOrder;
        this.title = title;
    }

    public String getKeyToSort() {
        return keyToSort;
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public void setKeyToSort(String keyToSort) {
        this.keyToSort = keyToSort;
    }

    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "order: " + sortOrder.toString() + ", key: " + keyToSort;
    }
}
