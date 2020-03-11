package com.example.barfinder;

public class Pair2<L,R> {

    private final L left;
    private final R right;

    public Pair2(L left, R right) {
        assert left != null;
        assert right != null;

        this.left = left;
        this.right = right;
    }

    public L getDistance() { return left; }
    public R getAddress() { return right; }

    @Override
    public int hashCode() { return left.hashCode() ^ right.hashCode(); }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pair)) return false;
        Pair pairo = (Pair) o;
        return this.left.equals(pairo.getLati()) &&
                this.right.equals(pairo.getLongi());
    }

}