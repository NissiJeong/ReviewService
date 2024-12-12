package com.task.review.dto;

import java.util.List;

public class CursorResult<T> {
    private List<T> values;
    private Boolean hasNext;

    public CursorResult(List<T> values, Boolean hasNext) {
        this.values = values;
        this.hasNext = hasNext;
    }
}
