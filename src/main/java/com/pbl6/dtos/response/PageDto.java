package com.pbl6.dtos.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PageDto<T>{
    final List<T> items;
    final int page;
    final int size;
    final long totalElements;
    final int totalPages;

    public PageDto(Page<T> page) {
        this.items = page.getContent();
        this.page = page.getNumber()+1;
        this.size = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
    }
}

