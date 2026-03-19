package kg.musabaev.command;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public non-sealed class KeyValueCommand extends Command {

    private final String value;

    public KeyValueCommand(String key, String value) {
        super(key);
        this.value = value;
    }

    @Override
    public String getPayload() {
        return "C:" + super.getKey() + ":" + value;
    }
}
