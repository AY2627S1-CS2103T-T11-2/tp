package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Stores previously entered commands and provides sequential navigation through them.
 */
public class CommandHistory {

    private static final int MAX_COMMAND_HISTORY = 100;

    private final List<String> commands = new ArrayList<>();
    private int currentIndex;

    /**
     * Stores a command for later navigation.
     * Consecutive duplicates are compared case-insensitively and stored only once.
     */
    public void add(String command) {
        requireNonNull(command);

        if (commands.isEmpty()
                || !command.equalsIgnoreCase(commands.get(commands.size() - 1))) {
            commands.add(command);

            if (commands.size() > MAX_COMMAND_HISTORY) {
                commands.remove(0);
            }
        }

        currentIndex = commands.size();
    }

    /**
     * Returns the previous command, if navigation can move further backwards.
     */
    public Optional<String> getPrevious() {
        assertIndexIsValid();

        if (currentIndex == 0) {
            return Optional.empty();
        }

        currentIndex--;
        return Optional.of(commands.get(currentIndex));
    }

    /**
     * Returns the next command, if navigation can move further forwards.
     * An empty string represents the blank entry immediately after the newest command.
     */
    public Optional<String> getNext() {
        assertIndexIsValid();

        if (currentIndex == commands.size()) {
            return Optional.empty();
        }

        currentIndex++;
        if (currentIndex == commands.size()) {
            return Optional.of("");
        }

        return Optional.of(commands.get(currentIndex));
    }

    private void assertIndexIsValid() {
        assert currentIndex >= 0 && currentIndex <= commands.size()
                : "Command history index must remain within valid bounds";
    }
}
