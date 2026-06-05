package alberto.cruz.mtz.glance.chat.backend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "conversations")
@CompoundIndexes({
        // Garantiza unicidad del par (a, b) — donde a < b siempre
        @CompoundIndex(
                name = "idx_participants_unique",
                def = "{'participant_a': 1, 'participant_b': 1}",
                unique = true
        ),
        // Para listar las conversaciones de un usuario ordenadas por actividad
        @CompoundIndex(name = "idx_a_last_msg", def = "{'participant_a': 1, 'last_message_at': -1}"),
        @CompoundIndex(name = "idx_b_last_msg", def = "{'participant_b': 1, 'last_message_at': -1}")
})
public class Conversation {

    @Id
    private String id;


    @Field("participants_id")
    private List<String> participantIds;

    /**
     * ID del último mensaje enviado en esta conversación.
     * Desnormalizado para evitar un lookup extra al listar chats.
     * Puede ser null si la conversación no tiene mensajes aún.
     */
    @Field("last_message_id")
    private String lastMessageId;

    /**
     * Timestamp del último mensaje. Usado para ordenar la lista de chats
     * del usuario de más reciente a más antiguo.
     * Puede ser null si la conversación no tiene mensajes aún.
     */
    @Field("last_message_at")
    private Instant lastMessageAt;

    @CreatedDate
    @Field("created_at")
    private Instant createdAt;

    /**
     * Devuelve los dos IDs de participantes ordenados [menor, mayor].
     * Usar el resultado para construir el documento:
     * <pre>
     *   String[] p = Conversation.orderedParticipants(userIdA, userIdB);
     *   Conversation conv = Conversation.builder()
     *       .participantA(p[0])
     *       .participantB(p[1])
     *       .build();
     * </pre>
     */
    public static List<String> orderedParticipants(String userIdA, String userIdB) {
        return userIdA.compareTo(userIdB) <= 0
                ? List.of(userIdA, userIdB)
                : List.of(userIdB, userIdA);
    }

    /**
     * Comprueba si el userId dado es participante de esta conversación.
     */
    public boolean hasParticipant(String userId) {
        return this.participantIds.contains(userId);
    }

    /**
     * Devuelve el ID del otro participante dado el ID del usuario actual.
     *
     * @throws IllegalArgumentException si userId no pertenece a esta conversación
     */
    public String otherParticipant(String userId) {
        if (this.participantIds.getFirst().equals(userId)) return this.participantIds.getFirst();
        if (this.participantIds.getLast().equals(userId)) return this.participantIds.getLast();

        throw new IllegalArgumentException("El usuario " + userId + " no es participante de la conversación " + id);
    }
}
