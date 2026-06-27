package alberto.cruz.mtz.glance.chat.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;

@AllArgsConstructor
public class Device {

    @Getter
    @Setter
    private String username;

    private final String name;
    private final String os;
    private final Instant expiration;
    private String status;


    public String name() {
        return this.name;
    }

    public String os() {
        return this.os;
    }

    public Instant expiration() {
        return this.expiration;
    }

    public boolean isPending() {
        return this.status.equals("PENDING");
    }

    public void authorize() {
        this.status = "AUTHORIZED";
    }

    public static Device create(String name, String os, int expirationInSeconds) {
        return new Device(null, name, os, Instant.now().plusSeconds(expirationInSeconds), "PENDING");
    }
}
