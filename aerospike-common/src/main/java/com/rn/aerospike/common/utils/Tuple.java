package com.rn.aerospike.common.utils;

/**
 * Created by rahul
 */
public class Tuple<X,Y> {

    private X first;

    private Y second;

    public Tuple() {
    }

    public Tuple(X first, Y second) {
        this.first = first;
        this.second = second;
    }

    public X getFirst() {
        return first;
    }

    public void setFirst(X first) {
        this.first = first;
    }

    public Y getSecond() {
        return second;
    }

    public void setSecond(Y second) {
        this.second = second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tuple<?, ?> tuple = (Tuple<?, ?>) o;

        if (getFirst() != null ? !getFirst().equals(tuple.getFirst()) : tuple.getFirst() != null) return false;
        return getSecond() != null ? getSecond().equals(tuple.getSecond()) : tuple.getSecond() == null;
    }

    @Override
    public int hashCode() {
        int result = getFirst() != null ? getFirst().hashCode() : 0;
        result = 31 * result + (getSecond() != null ? getSecond().hashCode() : 0);
        return result;
    }
}
