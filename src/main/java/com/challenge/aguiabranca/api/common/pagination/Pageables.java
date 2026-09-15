package com.challenge.aguiabranca.api.common.pagination;

import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class Pageables {
    private Pageables() {}

    public static Pageable of(int page, int size, String sort, Set<String> allowed, String fallback) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(100, Math.max(1, size));
        String[] parts = sort == null ? new String[] {fallback, "desc"} : sort.split(",", 2);
        String field = allowed.contains(parts[0]) ? parts[0] : fallback;
        Sort.Direction direction = parts.length > 1 && "asc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(safePage, safeSize, Sort.by(direction, field));
    }
}
