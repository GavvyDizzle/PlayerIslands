package com.github.gavvydizzle.playerislands.utils;

public class Pair<E,F>{
    private final E a;
    private final F b;

    public Pair(E a, F b) {
        this.a = a;
        this.b = b;
    }

    public E getA() {
        return a;
    }

    public F getB() {
        return b;
    }
}
