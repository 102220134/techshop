package com.pbl6.dtos.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PageDto<T> {
    final List<T> items;
    final int page;
    final int size;
    final long totalElements;
    final int totalPages;

    public PageDto(Page<T> page) {
        this.items = page.getContent();
        this.page = page.getNumber() + 1;
        this.size = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
    }
    // ✅ Empty page
    public static <T> PageDto<T> empty(Pageable pageable) {
        return new PageDto<>(
                Collections.emptyList(),
                pageable.getPageNumber() + 1,
                pageable.getPageSize(),
                0L,
                0
        );
    }

    // 🔒 private constructor cho empty
    private PageDto(List<T> items, int page, int size, long totalElements, int totalPages) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

}

