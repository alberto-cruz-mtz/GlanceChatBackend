package alberto.cruz.mtz.glance.chat.backend.util;

import java.security.SecureRandom;

public class PublicIdGenerator {

    /**
     * Allowed characters for public IDs.
     * Excludes visually ambiguous and easily confused letters.
     */
    private static final char[] PUBLIC_ID_CHARACTERS = "23456789ADEFGHJQRSTUWXY".toCharArray();

    private static final int PUBLIC_ID_LENGTH = 8;
    private static final int DISPLAY_GROUP_SIZE = 2;
    private static final char DISPLAY_SEPARATOR = '-';

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Generates a new random public ID in storage format.
     *
     * @return 8-character uppercase ID without separators. Example: "4A7X9M2D"
     */
    public static String generatePublicId() {
        char[] id = new char[PUBLIC_ID_LENGTH];
        for (int i = 0; i < PUBLIC_ID_LENGTH; i++) {
            id[i] = PUBLIC_ID_CHARACTERS[SECURE_RANDOM.nextInt(PUBLIC_ID_CHARACTERS.length)];
        }
        return new String(id);
    }

    /**
     * Formats a public ID for display with separators.
     *
     * @param publicId 8-character raw public ID. Example: "4A7X9M2D"
     * @return Formatted ID for the user. Example: "4A-7X-9M-2D"
     * @throws IllegalArgumentException if the ID does not have exactly 8 characters.
     */
    public static String formatPublicIdForDisplay(String publicId) {
        validatePublicIdLength(publicId);

        StringBuilder display = new StringBuilder(PUBLIC_ID_LENGTH + (PUBLIC_ID_LENGTH / DISPLAY_GROUP_SIZE) - 1);
        for (int i = 0; i < PUBLIC_ID_LENGTH; i += DISPLAY_GROUP_SIZE) {
            if (i > 0) display.append(DISPLAY_SEPARATOR);
            display.append(publicId, i, i + DISPLAY_GROUP_SIZE);
        }

        return display.toString();
    }

    /**
     * Normalizes a displayed public ID into storage format.
     *
     * @param displayPublicId ID with separators. Example: "4A-7X-9M-2D"
     * @return Raw uppercase ID. Example: "4A7X9M2D"
     * @throws IllegalArgumentException if the result does not have exactly 8 characters.
     */
    public static String normalizePublicId(String displayPublicId) {
        if (displayPublicId == null) throw new IllegalArgumentException("Public ID cannot be null.");
        String raw = displayPublicId.replace(String.valueOf(DISPLAY_SEPARATOR), "").toUpperCase();
        validatePublicIdLength(raw);
        return raw;
    }

    /**
     * Validates that a public ID is syntactically correct.
     *
     * @param publicId Raw ID to validate.
     * @return {@code true} if valid, {@code false} otherwise.
     */
    public static boolean isValidPublicId(String publicId) {
        if (publicId == null || publicId.length() != PUBLIC_ID_LENGTH) return false;
        for (char c : publicId.toCharArray()) {
            if (!isAllowedPublicIdCharacter(c)) return false;
        }
        return true;
    }

    private static void validatePublicIdLength(String publicId) {
        if (publicId == null || publicId.length() != PUBLIC_ID_LENGTH) {
            throw new IllegalArgumentException(
                    "Public ID must have exactly " + PUBLIC_ID_LENGTH + " characters. Received: '"
                            + publicId + "'"
            );
        }
    }

    private static boolean isAllowedPublicIdCharacter(char c) {
        for (char valid : PUBLIC_ID_CHARACTERS) {
            if (c == valid) return true;
        }
        return false;
    }
}
