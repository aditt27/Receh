package com.adibu.receh.data;

import java.util.Objects;

/**
 * Created by AdityaBudi on 17/09/2017.
 */

public class RecehItem {
    private String title;
    private Integer values;
    private boolean transaction;
    private String date;
    private String time;
    private String description;
    private String key;

    //transaction
    //true = income
    //false = outcome

    public RecehItem() {
    }

    public RecehItem(String title, Integer values, boolean transaction, String date, String time, String description) {
        this.title = title;
        this.values = values;
        this.transaction = transaction;
        this.date = date;
        this.time = time;
        this.description = description;
    }

    public RecehItem(String title, Integer values, boolean transaction, String date, String time, String description, String key) {
        this.title = title;
        this.values = values;
        this.transaction = transaction;
        this.date = date;
        this.time = time;
        this.description = description;
        this.key = key;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getValues() {
        return values;
    }

    public void setValues(Integer values) {
        this.values = values;
    }

    public boolean isTransaction() {
        return transaction;
    }

    public void setTransaction(boolean transaction) {
        this.transaction = transaction;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RecehItem)) {return false;}
        else if (this == obj) {return true;}
        return Objects.equals(title, ((RecehItem) obj).getTitle()) &&
                Objects.equals(values, ((RecehItem) obj).getValues()) &&
                Objects.equals(transaction, ((RecehItem) obj).isTransaction()) &&
                Objects.equals(date, ((RecehItem) obj).getDate()) &&
                Objects.equals(time, ((RecehItem) obj).getTime()) &&
                Objects.equals(description, ((RecehItem) obj).getDescription());
                //KEY TIDAK DIMASUKKAN KARENA TIDAK AKAN EQUALS PADA DATA DARI SERVER KE CLIENT
    }

    @Override
    public String toString() {
        return "Title: " + title + " " +
                "Values: " + String.valueOf(values) + " " +
                "Transaction: " + String.valueOf(transaction) + " " +
                "Date: " + date + " " +
                "Time: " + time + " " +
                "Description: " + description + " " +
                "Key: " + key;
    }
}
