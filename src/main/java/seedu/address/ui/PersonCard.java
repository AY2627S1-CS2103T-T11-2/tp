package seedu.address.ui;


import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.person.Person;

/**
 * A UI component that displays information of a {@code Person}.
 */
public class PersonCard extends UiPart<Region> {

    private static final String FXML = "PersonListCard.fxml";

    /**
     * Note: Certain keywords such as "location" and "resources" are reserved keywords in JavaFX.
     * As a consequence, UI elements' variable names cannot be set to such keywords
     * or an exception will be thrown by JavaFX during runtime.
     *
     * @see <a href="https://github.com/se-edu/addressbook-level4/issues/336">The issue on AddressBook level 4</a>
     */

    public final Person person;

    @FXML
    private VBox cardPane;
    @FXML
    private Label id;
    @FXML
    private Label name;
    @FXML
    private Label phone;
    @FXML
    private Label telegramHandle;
    @FXML
    private Label email;
    @FXML
    private Label subject;
    @FXML
    private Label group;
    @FXML
    private VBox phoneSection;
    @FXML
    private VBox telegramSection;
    @FXML
    private FlowPane tags;

    /**
     * Creates a {@code PersonCard} with the given {@code Person} and index to display.
     */
    public PersonCard(Person person, int displayedIndex) {
        super(FXML);
        this.person = person;
        id.setText(displayedIndex + ".");
        name.setText(person.getName().fullName);
        phone.setText(person.getPhone().value);
        telegramHandle.setText(person.getTelegramHandle().value);
        email.setText(person.getEmail().value);
        subject.setText(person.getSubject().subject);
        group.setText(person.getGroup().group);
        boolean hasPhone = !person.getPhone().isEmpty();
        boolean hasTelegramHandle = !person.getTelegramHandle().isEmpty();
        setVisibleAndManaged(phoneSection, hasPhone);
        setVisibleAndManaged(telegramSection, hasTelegramHandle);
        GridPane.setColumnIndex(phoneSection, PersonCardData.getPhoneColumnIndex(person));
        person.getTags().stream()
                .forEach(tag -> tags.getChildren().add(createTagLabel(tag.tagName)));
    }

    /**
     * Creates a styled tag label that wraps within the card and shows its full text on hover.
     */
    private Label createTagLabel(String tagName) {
        Label tag = new Label(tagName);
        tag.getStyleClass().add("tag-" + tagName.toLowerCase());
        tag.setWrapText(true);
        tag.setMinWidth(0);
        tag.maxWidthProperty().bind(Bindings.max(1, tags.widthProperty()));
        tag.setTooltip(new Tooltip(tagName));
        return tag;
    }

    private void setVisibleAndManaged(Region region, boolean isVisible) {
        region.setVisible(isVisible);
        region.setManaged(isVisible);
    }

    @FXML
    private void copyEmail() {
        copyToClipboard(PersonCardData.getCopyValue(person, PersonCardData.CopyableDetail.EMAIL));
    }

    @FXML
    private void copyTelegramHandle() {
        copyToClipboard(PersonCardData.getCopyValue(person, PersonCardData.CopyableDetail.TELEGRAM_HANDLE));
    }

    @FXML
    private void copyPhone() {
        copyToClipboard(PersonCardData.getCopyValue(person, PersonCardData.CopyableDetail.PHONE));
    }

    /** Copies the selected contact detail to the operating system clipboard. */
    private void copyToClipboard(String value) {
        ClipboardContent content = new ClipboardContent();
        content.putString(value);
        Clipboard.getSystemClipboard().setContent(content);
    }
}
