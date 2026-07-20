package alberto.cruz.mtz.glance.chat.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record CursorPage(String nextCursor) {

    public boolean hasNext() {
        return nextCursor != null;
    }
}
