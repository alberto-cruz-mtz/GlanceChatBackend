package alberto.cruz.mtz.glance.chat.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Encapsula las propiedades dinámicas de los archivos.
 * NON_NULL evita que se envíen campos en "null" al frontend.
 * Si un audio no tiene 'width', simplemente no aparecerá en el JSON.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MessageMetadata(
        String fileName,
        Long sizeBytes,
        Integer width,
        Integer height,
        Integer durationSeconds,
        String mimeType
) {
}
