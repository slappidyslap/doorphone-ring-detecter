package kg.musabaev.doorphone.core.command;

import lombok.Data;

@Data
public class CommandResponse {

    private final boolean isOk;
    private final String data;
}
