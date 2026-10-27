package com.lifelink.common.dto;

import java.util.List;

/**
 * Generic pagination response record.
 */
public record PageResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean last
) {
    public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = size == 0 ? 1 : (int) Math.ceil((double) totalElements / (double) size);
        boolean last = page >= totalPages - 1;
        return new PageResponse<>(content, page, size, totalElements, totalPages, last);
    }
}
