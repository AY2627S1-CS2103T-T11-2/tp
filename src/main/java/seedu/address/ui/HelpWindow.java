package seedu.address.ui;

import java.util.Locale;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import seedu.address.commons.core.LogsCenter;

/**
 * Controller for a help page
 */
public class HelpWindow extends UiPart<Stage> {

    public static final String USERGUIDE_URL = "https://ay2627s1-cs2103t-t11-2.github.io/tp/UserGuide.html";
    public static final String HELP_MESSAGE = "Refer to the user guide (Pressing \"Enter\" copies URL): "
            + USERGUIDE_URL;

    private static final Logger logger = LogsCenter.getLogger(HelpWindow.class);
    private static final String FXML = "HelpWindow.fxml";
    private static final boolean IS_MAC_OS = System.getProperty("os.name")
            .toLowerCase(Locale.ROOT).contains("mac");

    private Runnable onClose = () -> { };

    @FXML
    private Button copyButton;

    @FXML
    private Label helpMessage;

    /**
     * Creates a new HelpWindow.
     *
     * @param root Stage to use as the root of the HelpWindow.
     */
    public HelpWindow(Stage root) {
        super(FXML, root);
        helpMessage.setText(HELP_MESSAGE);
        configureKeyboardShortcuts();
        // This fires for every closing route, including the window manager's close control.
        getRoot().setOnHidden(event -> onClose.run());
    }

    /**
     * Creates a new HelpWindow.
     */
    public HelpWindow() {
        this(new Stage());
    }

    /**
     * Shows the help window.
     * @throws IllegalStateException
     *     <ul>
     *         <li>
     *             if this method is called on a thread other than the JavaFX Application Thread.
     *         </li>
     *         <li>
     *             if this method is called during animation or layout processing.
     *         </li>
     *         <li>
     *             if this method is called on the primary stage.
     *         </li>
     *         <li>
     *             if {@code dialogStage} is already showing.
     *         </li>
     *     </ul>
     */
    public void show() {
        logger.fine("Showing help page about the application.");
        getRoot().show();
        getRoot().centerOnScreen();
    }

    /**
     * Returns true if the help window is currently being shown.
     */
    public boolean isShowing() {
        return getRoot().isShowing();
    }

    /**
     * Hides the help window.
     */
    public void hide() {
        getRoot().hide();
    }

    /**
     * Runs {@code onClose} after the help window has been hidden by any route.
     */
    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }

    /**
     * Focuses on the help window.
     */
    public void focus() {
        getRoot().setIconified(false);
        getRoot().toFront();
        getRoot().requestFocus();
    }

    /**
     * Adds application-level fallbacks for conventional window-close shortcuts. Native window-manager close
     * behaviour is deliberately left unchanged.
     */
    private void configureKeyboardShortcuts() {
        getRoot().getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            boolean isMacCloseShortcut = IS_MAC_OS && event.getCode() == KeyCode.W && event.isMetaDown();
            boolean isAltF4 = !IS_MAC_OS && event.getCode() == KeyCode.F4 && event.isAltDown();

            if (isMacCloseShortcut || isAltF4) {
                hide();
                event.consume();
            }
        });
    }

    /**
     * Copies the URL to the user guide to the clipboard.
     */
    @FXML
    private void copyUrl() {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent url = new ClipboardContent();
        url.putString(USERGUIDE_URL);
        clipboard.setContent(url);
    }
}
