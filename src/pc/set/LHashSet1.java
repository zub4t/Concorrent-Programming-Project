package pc.set;

import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 
 * Non-concurrent hash set implementation.
 *
 */
public class LHashSet1<E> implements ISet<E> {

    private static final int NUMBER_OF_BUCKETS = 16; // should not be changed
    private LinkedList<E>[] table;
    ReentrantLock reentrantLock = new ReentrantLock();
    private int size;

    /**
     * Constructor.
     */
    @SuppressWarnings("unchecked")
    public LHashSet1() {
        table = (LinkedList<E>[]) new LinkedList[NUMBER_OF_BUCKETS];
        for (int i = 0; i < table.length; i++) {
            table[i] = new LinkedList<>();
        }
        size = 0;
    }

    @Override
    public int size() {
        reentrantLock.lock();
        try {
            return size;
        } catch (Exception e) {
            return 0;
        } finally {
            reentrantLock.unlock();

        }

    }

    private LinkedList<E> getEntry(E elem) {
        return table[Math.abs(elem.hashCode() % table.length)];
    }

    @Override
    public boolean add(E elem) {
        reentrantLock.lock();
        try {
            if (elem == null) {
                throw new IllegalArgumentException();
            }
            LinkedList<E> list = getEntry(elem);
            boolean r = !list.contains(elem);
            if (r) {
                list.addFirst(elem);
                size++;
            }
            return r;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            reentrantLock.unlock();
        }

    }

    @Override
    public boolean remove(E elem) {
        reentrantLock.lock();
        try {
            if (elem == null) {
                throw new IllegalArgumentException();
            }
            boolean r = getEntry(elem).remove(elem);
            if (r) {
                size--;
            }
            return r;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            reentrantLock.unlock();

        }

    }

    @Override
    public  boolean contains(E elem) {
        reentrantLock.lock();
        try {
            if (elem == null) {
                throw new IllegalArgumentException();
            }
            return getEntry(elem).contains(elem);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            reentrantLock.unlock();
        }
       
    }
}
