package pc.set;

import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 
 * Non-concurrent hash set implementation.
 *
 */
public class LHashSet3<E> implements ISet<E> {

    private static final int NUMBER_OF_BUCKETS = 16; // should not be changed
    private LinkedList<E>[] table;
    ReentrantReadWriteLock[] reentrantReadWriteLockByEntry;

    /**
     * Constructor.
     */
    @SuppressWarnings("unchecked")
    public LHashSet3() {
        table = (LinkedList<E>[]) new LinkedList[NUMBER_OF_BUCKETS];
        reentrantReadWriteLockByEntry = new ReentrantReadWriteLock[NUMBER_OF_BUCKETS];

        for (int i = 0; i < table.length; i++) {
            table[i] = new LinkedList<>();
            reentrantReadWriteLockByEntry[i] = new ReentrantReadWriteLock(); 
        }

    }

    @Override
    public int size() {
        int size = 0;
        for (int i = 0; i < NUMBER_OF_BUCKETS; i++) {

            reentrantReadWriteLockByEntry[i].readLock().lock();
            try {
                size += table[i].size();
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            } finally {
                reentrantReadWriteLockByEntry[i].readLock().unlock();

            }
        }
        return size;

    }

    private LinkedList<E> getEntry(E elem) {
        return table[Math.abs(elem.hashCode() % table.length)];
    }
    
    private int getKey(E elem) {
        return Math.abs(elem.hashCode() % table.length);
    }

    @Override
    public boolean add(E elem) {
        int key  = getKey(elem);

        reentrantReadWriteLockByEntry[key].writeLock().lock();
        try {
            if (elem == null) {
                throw new IllegalArgumentException();
            }
            LinkedList<E> list = getEntry(elem);
            boolean r = !list.contains(elem);
            if (r) {
                list.addFirst(elem);
            }
            return r;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            reentrantReadWriteLockByEntry[key].writeLock().unlock();
        }

    }

    @Override
    public boolean remove(E elem) {
        int key = getKey(elem);
        reentrantReadWriteLockByEntry[key].writeLock().lock();
        try {
            if (elem == null) {
                throw new IllegalArgumentException();
            }
            boolean r = getEntry(elem).remove(elem);
        
            return r;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
              reentrantReadWriteLockByEntry[key].writeLock().unlock();
        }

    }

    @Override
    public boolean contains(E elem) {
        int key = getKey(elem);
        reentrantReadWriteLockByEntry[key].readLock().lock();
        try {
            if (elem == null) {
                throw new IllegalArgumentException();
            }
            return getEntry(elem).contains(elem);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            reentrantReadWriteLockByEntry[key].readLock().unlock();
        }

    }
}
