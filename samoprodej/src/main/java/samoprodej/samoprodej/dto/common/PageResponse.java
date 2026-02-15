package samoprodej.samoprodej.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Generic paginated response wrapper
 * @param <T> Type of data items
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> data;
    private PageMeta meta;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageMeta {
        private PageInfo page;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageInfo {
        /**
         * Current page number (0-based)
         */
        private int number;

        /**
         * Number of items per page
         */
        private int size;

        /**
         * Total number of elements across all pages
         */
        private long totalElements;

        /**
         * Total number of pages
         */
        private int totalPages;
    }

    /**
     * Creates a PageResponse from Spring Data Page object
     */
    public static <T> PageResponse<T> of(org.springframework.data.domain.Page<T> page) {
        return PageResponse.<T>builder()
                .data(page.getContent())
                .meta(PageMeta.builder()
                        .page(PageInfo.builder()
                                .number(page.getNumber())
                                .size(page.getSize())
                                .totalElements(page.getTotalElements())
                                .totalPages(page.getTotalPages())
                                .build())
                        .build())
                .build();
    }
}