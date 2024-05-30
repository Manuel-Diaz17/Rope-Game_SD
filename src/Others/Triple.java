package Interfaces;

import java.io.Serializable;

/**
 * Generic Triple implementation
 * 
 * @param <X> 1st value
 * @param <Y> 2nd value
 * @param <Z> 3rd value
 */
public class Triple<X, Y, Z> implements Serializable {
    
    private final X first;
    private final Y second;
    private final Z third;

    public Triple(X first, Y second, Z third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public X getFirst() {
        return first;
    }

    public Y getSecond() {
        return second;
    }

    public Z getThird() {
        return third;
    }
}
