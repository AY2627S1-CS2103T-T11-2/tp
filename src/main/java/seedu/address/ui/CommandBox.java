package seedu.address.ui;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * The UI component that is responsible for receiving user command inputs.
 */
public class CommandBox extends UiPart<Region> {

    public static final String ERROR_STYLE_CLASS = "error";
    private static final String FXML = "CommandBox.fxml";
    private static final int MAX_COMMAND_HISTORY = 100;

    private final CommandExecutor commandExecutor;

    private final List<String> commandHistory = new ArrayList<>();
    private int commandHistoryIndex;

    @FXML
    private TextField commandTextField;

    /**
     * Creates a {@code CommandBox} with the given {@code CommandExecutor}.
     */
    public CommandBox(CommandExecutor commandExecutor) {
        super(FXML);
        this.commandExecutor = commandExecutor;
        // calls #setStyleToDefault() whenever there is a change to the text of the command box.
        commandTextField.textProperty().addListener((unused1, unused2, unused3) -> setStyleToDefault());
    }

    /**
     * Handles the Enter button pressed event.
     */
    @FXML
    private void handleCommandEntered() {
        String commandText = commandTextField.getText();
        if (commandText.equals("")) {
            return;
        }

        rememberCommand(commandText);

        try {
            commandExecutor.execute(commandText);
            commandTextField.setText("");
        } catch (CommandException | ParseException e) {
            setStyleToIndicateCommandFailure();
        }
    }

    /**
     * Stores a command for keyboard history navigation.
     *
     * <p>Consecutive duplicates are ignored and the oldest entry is discarded when
     * the configured history limit is exceeded.</p>
     *
     * @param commandText command to retain
     */
    private void rememberCommand(String commandText) {
        if (commandHistory.isEmpty() || !commandText.equalsIgnoreCase(commandHistory.get(commandHistory.size() - 1))) {
            commandHistory.add(commandText);

            if (commandHistory.size() > MAX_COMMAND_HISTORY) {
                commandHistory.remove(0);
            }
        }

        commandHistoryIndex = commandHistory.size();
    }

    /**
     * Handles keyboard navigation through previously entered commands.
     *
     * @param event key event raised by the command text field
     */
    @FXML
    private void handleHistoryNavigation(KeyEvent event) {
        if (event.getCode() == KeyCode.UP) {
            showPreviousUserInput();
            event.consume();
        } else if (event.getCode() == KeyCode.DOWN) {
            showNextUserInput();
            event.consume();
        }
    }

    /**
     * Displays the previous command in the command text field.
     */
    private void showPreviousUserInput() {
        assert commandHistoryIndex >= 0
                && commandHistoryIndex <= commandHistory.size()
                : "Command history index must remain within valid bounds";

        if (commandHistoryIndex > 0) {
            commandHistoryIndex--;
            displayHistoryEntry(commandHistory.get(commandHistoryIndex));
        }
    }

    /**
     * Displays the next command in the command text field.
     */
    private void showNextUserInput() {
        assert commandHistoryIndex >= 0
                && commandHistoryIndex <= commandHistory.size()
                : "Command history index must remain within valid bounds";

        if (commandHistoryIndex < commandHistory.size() - 1) {
            commandHistoryIndex++;
            displayHistoryEntry(commandHistory.get(commandHistoryIndex));
        } else if (commandHistoryIndex == commandHistory.size() - 1) {
            commandHistoryIndex++;
            commandTextField.clear();
        }
    }

    /**
     * Displays the specified command and moves the caret to its end.
     */
    private void displayHistoryEntry(String commandText) {
        commandTextField.setText(commandText);
        commandTextField.positionCaret(commandTextField.getLength());
    }

    /**
     * Sets the command box style to use the default style.
     */
    private void setStyleToDefault() {
        commandTextField.getStyleClass().remove(ERROR_STYLE_CLASS);
    }

    /**
     * Sets the command box style to indicate a failed command.
     */
    private void setStyleToIndicateCommandFailure() {
        ObservableList<String> styleClass = commandTextField.getStyleClass();

        if (styleClass.contains(ERROR_STYLE_CLASS)) {
            return;
        }

        styleClass.add(ERROR_STYLE_CLASS);
    }

    /**
     * Represents a function that can execute commands.
     */
    @FunctionalInterface
    public interface CommandExecutor {
        /**
         * Executes the command and returns the result.
         *
         * @see seedu.address.logic.Logic#execute(String)
         */
        CommandResult execute(String commandText) throws CommandException, ParseException;
    }

}
