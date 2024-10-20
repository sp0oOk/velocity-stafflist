package me.spook.stafflist.obj;

public class Pair<L, R> {

    // ------------------------------------------------- //
    // FIELDS
    // ------------------------------------------------- //

    public L left;
    public R right;

    /**
     * Creates a simple pair
     *
     * @param left  The left object
     * @param right The right object
     */

    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }
}
