package me.jounhee.chapter01.item01;

import java.util.Comparator;

public class ComEx implements Comparator<Integer> {
    @Override
    public int compare(Integer i1, Integer i2) {
        return i2 - i1;
    }

    @Override
    public Comparator reversed() {
        return Comparator.super.reversed();
    }
}
