package pc.set;

import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 
 * Non-concurrent hash set implementation.
 *
 */
public class LHashSet2<E> implements ISet<E> {

    private static final int NUMBER_OF_BUCKETS = 16; // should not be changed
    private LinkedList<E>[] table;
    ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
    private int size;

    /**
     * Constructor.
     */
    @SuppressWarnings("unchecked")
    public LHashSet2() {
        table = (LinkedList<E>[]) new LinkedList[NUMBER_OF_BUCKETS];
        for (int i = 0; i < table.length; i++) {
            table[i] = new LinkedList<>();
        }
        size = 0;
    }

    @Override
    public int size() {
        reentrantReadWriteLock.readLock().lock();
        try {
            return size;
        } catch (Exception e) {
            return 0;
        } finally {
            reentrantReadWriteLock.readLock().unlock();

        }

    }

    private LinkedList<E> getEntry(E elem) {
        return table[Math.abs(elem.hashCode() % table.length)];
    }

    @Override
    public boolean add(E elem) {
        reentrantReadWriteLock.writeLock().lock();
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
            reentrantReadWriteLock.writeLock().unlock();
        }

    }

    @Override
    public boolean remove(E elem) {
        reentrantReadWriteLock.writeLock().lock();
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
            reentrantReadWriteLock.writeLock().unlock();
        }

    }

    @Override
    public boolean contains(E elem) {
        reentrantReadWriteLock.readLock().lock();
        try {
            if (elem == null) {
                throw new IllegalArgumentException();
            }
            return getEntry(elem).contains(elem);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            reentrantReadWriteLock.readLock().unlock();
        }

    }
}
