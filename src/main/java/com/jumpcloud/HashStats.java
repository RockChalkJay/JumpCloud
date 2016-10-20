package com.jumpcloud;

/**
 * Created by jharris on 10/19/16.
 */
public class HashStats {

    private long total;
    private double average;

    public HashStats(long total, double average) {
        this.total = total;
        this.average = average;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public double getAverage() {
        return average;
    }

    public void setAverage(long average) {
        this.average = average;
    }
}
