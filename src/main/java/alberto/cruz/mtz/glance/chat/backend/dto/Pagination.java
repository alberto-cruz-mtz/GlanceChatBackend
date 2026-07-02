package alberto.cruz.mtz.glance.chat.backend.dto;

public record Pagination(
        int count,
        int perPage,
        int currentPage,
        boolean hasNext,
        PaginationLinks links
) {
}
