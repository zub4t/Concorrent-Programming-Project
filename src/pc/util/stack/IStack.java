package pc.util.stack;

public interface IStack<E> {
    int size();
    boolean contains(E elem);
    void push(E elem);

    E pop() throws InterruptedException;
}
