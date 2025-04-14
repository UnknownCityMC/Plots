package de.unknowncity.plots.util;

import java.util.ArrayList;
import java.util.Arrays;

public class AstraArrays {

    public static <T> T[] merge(T[] a1, T[] a2) {
        var list = new ArrayList<>();
        list.addAll(Arrays.asList(a1));
        list.addAll(Arrays.asList(a2));
        return list.toArray(a1);
    }
}
