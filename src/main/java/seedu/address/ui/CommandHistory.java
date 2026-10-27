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
    private String draft = "";

    /**
     * Stores a command for later navigation.
     * Consecutive exact duplicates are stored only once.
     * Resets navigation and discards any previously saved draft.
     *
     * @param command processed command to store
     * @throws NullPointerException if {@code command} is null
     */
    public void add(String command) {
        requireNonNull(command);

        if (commands.isEmpty()
                || !command.equals(commands.get(commands.size() - 1))) {
            commands.add(command);

            if (commands.size() > MAX_COMMAND_HISTORY) {
                commands.remove(0);
            }
        }

        currentIndex = commands.size();
        draft = "";
    }

    /**
     * Replaces the current unprocessed draft.
     *
     * @param draft command currently being edited
     * @throws NullPointerException if {@code draft} is null
     */
    public void saveDraft(String draft) {
        requireNonNull(draft);

        this.draft = draft;
    }

    /**
     * Moves backwards through the history, if navigation can move further backwards.
     *
     * @return the previous command, or an empty optional at the oldest command
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
     * Moves forwards through the history, if navigation can move further forwards.
     * The saved draft is returned after navigating past the newest command.
     *
     * @return the next command or saved draft, or an empty optional when already at the draft
     */
    public Optional<String> getNext() {
        assertIndexIsValid();

        if (currentIndex == commands.size()) {
            return Optional.empty();
        }

        currentIndex++;
        if (currentIndex == commands.size()) {
            return Optional.of(draft);
        }

        return Optional.of(commands.get(currentIndex));
    }

    private void assertIndexIsValid() {
        assert currentIndex >= 0 && currentIndex <= commands.size()
                : "Command history index must remain within valid bounds";
    }
}
