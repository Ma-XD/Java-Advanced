package info.kgeorgiy.ja.dziubenko.arrayset;

import java.util.*;

@SuppressWarnings("unused")
public class ArraySet<E> extends AbstractSet<E> implements SortedSet<E> {

    private final List<E> elementData;
    private final Comparator<? super E> comparator;

    public static <E> List<E> asList(final SortedSet<E> sortedSet) {
        return new ArrayList<>(sortedSet);
    }

    public ArraySet(final SortedSet<E> sortedSet) {
        this.elementData = asList(sortedSet);
        this.comparator = sortedSet.comparator();
    }

    public ArraySet(final Collection<? extends E> collection, final Comparator<? super E> comparator) {
        SortedSet<E> set = new TreeSet<>(comparator);
        set.addAll(collection);
        elementData = asList(set);
        this.comparator = comparator;
    }

    public ArraySet(final Collection<? extends E> collection) {
        this(collection, null);
    }

    public ArraySet() {
        this(Collections.emptyList());
    }

    //constructor for subSet
    private ArraySet(final List<E> subSetList, final Comparator<? super E> comparator) {
        this.elementData = subSetList;
        this.comparator = comparator;
    }

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    @Override
    public ArraySet<E> subSet(E fromElement, E toElement) {
        subSetBoundsCheck(fromElement, toElement, comparator);
        return subSetOfRange(getInsertionPoint(fromElement), getInsertionPoint(toElement));
    }

    @SuppressWarnings("unchecked")
    private static <E> void subSetBoundsCheck(final E fromElement, final E toElement, Comparator<E> comparator) {
        if (comparator == null) {
            comparator = (Comparator<E>) Comparator.naturalOrder();
        }
        if (comparator.compare(fromElement, toElement) > 0) {
            throw new IllegalArgumentException("fromElement > toElement");
        }
    }

    @Override
    public ArraySet<E> headSet(E toElement) {
        return subSetOfRange(0, getInsertionPoint(toElement));
    }

    @Override
    public ArraySet<E> tailSet(E fromElement) {
        return subSetOfRange(getInsertionPoint(fromElement), size());
    }

    private int getInsertionPoint(final E e) {
        int index = indexOf(e);
        return index < 0 ? -index - 1 : index;
    }

    private ArraySet<E> subSetOfRange(final int fromIndex, final int toIndex) {
        return new ArraySet<>(elementData.subList(fromIndex, toIndex), comparator);
    }

    @Override
    public E first() {
        return elementData(0);
    }

    @Override
    public E last() {
        return elementData(size() - 1);
    }

    private E elementData(final int index) {
        if (size() == 0) {
            throw new NoSuchElementException();
        }
        return elementData.get(index);
    }

    @Override
    public int size() {
        return elementData.size();
    }

    private int indexOf(final E e) {
        return Collections.binarySearch(elementData, e, comparator);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        return indexOf((E) o) >= 0;
    }

    @Override
    public Iterator<E> iterator() {
        return Collections.unmodifiableList(elementData).iterator();
    }

    @Override
    public String toString() {
        return "ArraySet{" +
            "comparator=" + comparator +
            ", elementData=" + elementData +
            '}';
    }
}
