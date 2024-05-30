package Others;

import java.io.Serializable;

/**
 * Generic Tuple implementation
 * @param <X> left value to be stored
 * @param <Y> right value to be stored
 */
public class Tuple<X, Y> implements Serializable {

    private final X left;
    private final Y right;

    /**
     * Tuple constructor
     *
     * @param left value to be stored
     * @param right value to be stored
     */
    public Tuple(X left, Y right) {
        this.left = left;
        this.right = right;
    }

    /**
     * Get left value
     *
     * @return left value
     */
    public X getLeft() {
        return left;
    }

    /**
     * Get right value
     *
     * @return right value
     */
    public Y getRight() {
        return right;
    }
}
