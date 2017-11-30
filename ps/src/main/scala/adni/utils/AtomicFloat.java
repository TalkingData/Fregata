package adni.utils;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by chris on 11/15/17.
 */

public class AtomicFloat extends Number implements java.io.Serializable {
    /**
     * Constructs an atomic float with initial value zero.
     */
    public AtomicFloat() {
        this(0.0f);
    }

    /**
     * Constructs an atomic float with specified initial value.
     * @param value the initial value.
     */
    public AtomicFloat(float value) {
        _ai = new AtomicInteger(i(value));
    }

    /**
     * Gets the current value of this float.
     * @return the current value.
     */
    public final float get() {
        return f(_ai.get());
    }

    /**
     * Sets the value of this float.
     * @param value the new value.
     */
    public final void set(float value) {
        _ai.set(i(value));
    }

    /**
     * Atomically sets the value of this float and returns its old value.
     * @param value the new value.
     * @return the old value.
     */
    public final float getAndSet(float value) {
        return f(_ai.getAndSet(i(value)));
    }

    /**
     * Atomically sets this float to the specified updated value
     * if the current value equals the specified expected value.
     * @param expect the expected value.
     * @param update the updated value.
     * @return true, if successfully set; false, if the current
     *  value was not equal to the expected value.
     */
    public final boolean compareAndSet(float expect, float update) {
        return _ai.compareAndSet(i(expect),i(update));
    }

    /**
     * Atomically sets this float to the specified updated value
     * if the current value equals the specified expected value.
     * <p>
     * My fail spuriously, and does not provide ordering guarantees, so
     * is only rarely useful.
     * @param expect the expected value.
     * @param update the updated value.
     * @return true, if successfully set; false, if the current
     *  value was not equal to the expected value.
     */
    public final boolean weakCompareAndSet(float expect, float update) {
        return _ai.weakCompareAndSet(i(expect),i(update));
    }

    /**
     * Atomically increments by one the value of this float.
     * @return the previous value of this float.
     */
    public final float getAndIncrement() {
        return getAndAdd(1.0f);
    }

    /**
     * Atomically decrements by one the value of this float.
     * @return the previous value of this float.
     */
    public final float getAndDecrement() {
        return getAndAdd(-1.0f);
    }

    /**
     * Atomically adds a specified value to the value of this float.
     * @param delta the value to add.
     * @return the previous value of this float.
     */
    public final float getAndAdd(float delta) {
        for (;;) {
            int iexpect = _ai.get();
            float expect = f(iexpect);
            float update = expect+delta;
            int iupdate = i(update);
            if (_ai.compareAndSet(iexpect,iupdate))
                return expect;
        }
    }

    /**
     * Atomically increments by one the value of this float.
     * @return the updated value of this float.
     */
    public final float incrementAndGet() {
        return addAndGet(1.0f);
    }

    /**
     * Atomically decrements by one the value of this float.
     * @return the updated value of this float.
     */
    public final float decrementAndGet() {
        return addAndGet(-1.0f);
    }

    /**
     * Atomically adds a specified value to the value of this float.
     * @param delta the value to add.
     * @return the updated value of this float.
     */
    public final float addAndGet(float delta) {
        for (;;) {
            int iexpect = _ai.get();
            float expect = f(iexpect);
            float update = expect+delta;
            int iupdate = i(update);
            if (_ai.compareAndSet(iexpect,iupdate))
                return update;
        }
    }

    public String toString() {
        return Float.toString(get());
    }

    public int intValue() {
        return (int)get();
    }

    public long longValue() {
        return (long)get();
    }

    public float floatValue() {
        return get();
    }

    public double doubleValue() {
        return (double)get();
    }

    ///////////////////////////////////////////////////////////////////////////
    // private

    private AtomicInteger _ai;

    private static int i(float f) {
        return Float.floatToIntBits(f);
    }
    private static float f(int i) {
        return Float.intBitsToFloat(i);
    }
}