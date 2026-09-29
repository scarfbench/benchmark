package io.github.raeperd.realworld.application.article;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/** A Pageable whose offset is an item count, as specified by the RealWorld API. */
final class OffsetPageRequest implements Pageable {
    private final int offset;
    private final int limit;

    OffsetPageRequest(int offset, int limit) {
        if (offset < 0 || limit < 1) {
            throw new IllegalArgumentException("offset must be nonnegative and limit must be positive");
        }
        this.offset = offset;
        this.limit = limit;
    }

    @Override public int getPageNumber() { return offset / limit; }
    @Override public int getPageSize() { return limit; }
    @Override public long getOffset() { return offset; }
    @Override public Sort getSort() { return Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("updatedAt")); }
    @Override public Pageable next() { return new OffsetPageRequest(offset + limit, limit); }
    @Override public Pageable previousOrFirst() { return hasPrevious() ? new OffsetPageRequest(Math.max(0, offset - limit), limit) : first(); }
    @Override public Pageable first() { return new OffsetPageRequest(0, limit); }
    @Override public Pageable withPage(int pageNumber) { return new OffsetPageRequest(pageNumber * limit, limit); }
    @Override public boolean hasPrevious() { return offset > 0; }
}
