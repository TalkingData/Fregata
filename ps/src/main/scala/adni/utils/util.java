package adni.utils;

import java.util.*;
import java.util.Map.Entry;

/**
 * Created by chris on 11/8/17.
 */
public class util {
    public static <K extends Comparable<? super K>,V extends Comparable<? super V>>
    void entriesSortedByValues(List<Entry<K,Entry<V,V>>> sortedEntries) {

        Collections.sort(sortedEntries,
                new Comparator<Entry<K,Entry<V,V>>>() {
                    @Override
                    public int compare(Entry<K,Entry<V,V>> e1, Entry<K,Entry<V,V>> e2) {
                        return e2.getValue().getValue().compareTo(e1.getValue().getValue());
                    }
                }
        );
    }

    public static <K extends Comparable<? super K>,V extends Comparable<? super V>>
    List<Entry<K,Entry<V,V>>> mergeMultipleLists(List<List<Entry<K,Entry<V,V>>>> entryLists) {
        LinkedList<List<Entry<K,Entry<V,V>>>> queue = new LinkedList<>();
        queue.addAll(entryLists);
        while(queue.size() > 1) {
            List<Entry<K,Entry<V,V>>> firstList = queue.poll();
            List<Entry<K,Entry<V,V>>> secondList = queue.poll();
            List<Entry<K,Entry<V,V>>> mergedList = merge(firstList,secondList);
            queue.add(mergedList);
        }
        return queue.poll();

    }


    // descending sort
    public static <K extends Comparable<? super K>,V extends Comparable<? super V>>
    List<Entry<K,Entry<V,V>>> merge(List<Entry<K,Entry<V,V>>> firstList, List<Entry<K,Entry<V,V>>> secondList) {
        List<Entry<K,Entry<V,V>>> mergedList = new ArrayList<>();
        int firstListIndex = 0;
        int secondListIndex = 0;
        while (firstList.size() > firstListIndex && secondList.size() > secondListIndex) {
            Entry<K,Entry<V,V>> currentFirst = firstList.get(firstListIndex);
            Entry<K,Entry<V,V>> currentSecond = secondList.get(secondListIndex);
            if(currentFirst.getValue().getValue().compareTo(currentSecond.getValue().getValue()) > 0) {
                mergedList.add(currentFirst);
                firstListIndex ++;
            } else if(currentFirst.getValue().getValue().compareTo(currentSecond.getValue().getValue()) == 0 && currentFirst.getKey().compareTo(currentSecond.getKey()) < 0 ){
                mergedList.add(currentFirst);
                firstListIndex ++;
            } else {
                mergedList.add(currentSecond);
                secondListIndex++;
            }
        }

        while (firstListIndex <firstList.size()) {
            Entry<K,Entry<V,V>> current = firstList.get(firstListIndex);
            mergedList.add(current);
            firstListIndex++;
        }

        while (secondListIndex < secondList.size()) {
            Entry<K,Entry<V,V>> current = secondList.get(secondListIndex);
            mergedList.add(current);
            secondListIndex++;
        }
        return mergedList;

    }



}
