package alberto.cruz.mtz.glance.chat.backend.dto;

import java.util.Collection;

public record DataResponse<T>(
        Collection<?> data
) {
}
