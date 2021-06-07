package pc.util.stack;

import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

public class Stack<E> implements IStack<E> {
    private final LinkedList<E> elems;
    private final ReentrantLock lock;
    private final Condition notEmpty;

    public Stack() {
        elems = new LinkedList<>();
        lock = new ReentrantLock();
        notEmpty = lock.newCondition();
    }

    public int size() {
        return elems.size();
    }

    public void push(E elem) {
        try {
            lock.lock();
            elems.add(elem);
            // System.out.println(Thread.currentThread().getName());
            notEmpty.signal();
        } finally {
            lock.unlock();
        }

    }

    public boolean contains(E elem) {
        try {
            lock.lock();
            for (E e : elems) {
                if (e.equals(elem)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            lock.unlock();
        }
        return false;
    }

    public E pop() throws InterruptedException {
        lock.lock();
        try {

            while (elems.isEmpty()) {
                notEmpty.await();
                // System.out.println(Thread.currentThread().getName());
            }
            return elems.pop();
        } finally {
            lock.unlock();
        }

    }
}
