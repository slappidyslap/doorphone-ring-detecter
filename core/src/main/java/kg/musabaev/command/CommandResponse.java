package kg.musabaev.command;

import lombok.Data;

@Data
public class CommandResponse {

    private final boolean isOk;
    private final String data;
}
