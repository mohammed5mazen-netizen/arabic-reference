package com.mrsoft.arabicreference.learning.domain;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class DisplayOrder {

    private DisplayOrder() {
    }

    public static void requireContiguous(List<Integer> orders) {
        Set<Integer> seen = new HashSet<>();
        for (Integer order : orders) {
            if (order == null || order < 1 || !seen.add(order)) {
                throw new IllegalArgumentException("order");
            }
        }
        for (int expected = 1; expected <= orders.size(); expected++) {
            if (!seen.contains(expected)) {
                throw new IllegalArgumentException("order");
            }
        }
    }
}
