package com.greffgreff.modfiddle.world.util;

import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

public class WeightedItems<T> {
    private final NavigableMap<Double, T> map = new TreeMap<Double, T>();
    private final Random random;
    private double total = 0;

    public WeightedItems() {
        this(new Random());
    }

    public WeightedItems(Random random) {
        this.random = random;
    }

    public WeightedItems<T> add(double weight, T result) {
        if (weight <= 0) return this;
        total += weight;
        map.put(total, result);
        return this;
    }

    public T next() {
        return map.higherEntry(random.nextDouble() * total).getValue();
    }
}