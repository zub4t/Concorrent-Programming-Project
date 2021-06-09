package pc.set;

import scala.concurrent.stm.Ref;
import scala.concurrent.stm.TArray;
import scala.concurrent.stm.japi.STM;

/**
 * 
 * 
 *
 */
public class STMHashSet<E> implements ISet<E> {

  private static class Node<T> {
    T value;
    Ref.View<Node<T>> prev = STM.newRef(null);
    Ref.View<Node<T>> next = STM.newRef(null);
  }

  private static final int NUMBER_OF_BUCKETS = 16; // should not be changed
  private final TArray.View<Node<E>> table;
  private final Ref.View<Integer> size;

  /**
   * Constructor.
   */
  public STMHashSet() {
    table = STM.newTArray(NUMBER_OF_BUCKETS);
    size = STM.newRef(0);

  }

  @Override
  public int size() {
    return size.get();
  }

  @Override
  public boolean add(E elem) {
    if (elem == null) {
      throw new IllegalArgumentException();
    }
    return STM.atomic(() -> {
      try {
        if (!contains(elem)) {
          Node<E> newNode = new Node<>();
          newNode.value = elem;
          int key = this.getKey(elem);
          Node<E> node = table.apply(key);
          if (node != null) {
            node.prev.set(newNode);
            newNode.next.set(node);
          }
          table.update(key, newNode);
          size.update(size.get() + 1);
          return true;
        }
        return false;
      } catch (Exception e) {
        e.printStackTrace();
        return false;
      }

    });
  }

  @Override
  public boolean remove(E elem) {
    if (elem == null) {
      throw new IllegalArgumentException();
    }
    return STM.atomic(() -> {
      try {
        int key = this.getKey(elem);
        Node<E> node = table.apply(key);
        while (node != null) {
          if (node.value.equals(elem)) {
            if(node.next.get() != null)
              node.next.get().prev.set(node.prev.get());

            if (node.prev.get() != null)
              node.prev.get().next.update(node.next.get());
            else
              table.update(key, node.next.get());

              
            size.update(size.get() - 1);
            return true;
          }

          node = node.next.get();
        }

        return false;
      } catch (Exception e) {
        e.printStackTrace();
        return false;
      }

    });
  }

  @Override
  public boolean contains(E elem) {
    if (elem == null) {
      throw new IllegalArgumentException();
    }
    return STM.atomic(() -> {
      try {
        int key = this.getKey(elem);
        Node<E> node = table.apply(key);

        while (node != null) {
          if (elem.equals(node.value))
            return true;
          node = node.next.get();
        }

        return false;
      } catch (Exception e) {
        e.printStackTrace();
        return false;
      }

    });
  }

  private int getKey(E elem) {
    return STM.atomic(() -> {
      return Math.abs(elem.hashCode() % table.length());
    });
  }

}
