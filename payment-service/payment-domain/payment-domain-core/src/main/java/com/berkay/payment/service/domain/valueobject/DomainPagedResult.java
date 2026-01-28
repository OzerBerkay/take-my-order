package com.berkay.payment.service.domain.valueobject;

import java.util.List;

public class DomainPagedResult<T> {
    private final List<T> data;
    private final int pageNumber;
    private final int pageSize;
    private final long totalElements;
    private final int totalPages;

    public DomainPagedResult(List<T> data, int pageNumber, int pageSize, long totalElements, int totalPages) {
        this.data = data;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    // Getter'lar...
    public List<T> getData() { return data; }
    public int getPageNumber() { return pageNumber; }
    public int getPageSize() { return pageSize; }
    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
}
