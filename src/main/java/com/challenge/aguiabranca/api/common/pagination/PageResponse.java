package com.challenge.aguiabranca.api.common.pagination;

import java.util.List;
import org.springframework.data.domain.Page;

public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        List<String> sort) {
    public static <T> PageResponse<T> from(Page<T> source) {
        List<String> sort = source.getSort().stream()
                .map(order -> order.getProperty() + "," + order.getDirection().name().toLowerCase())
                .toList();
        return new PageResponse<>(source.getContent(), source.getNumber(), source.getSize(),
                source.getTotalElements(), source.getTotalPages(), sort);
    }
}

