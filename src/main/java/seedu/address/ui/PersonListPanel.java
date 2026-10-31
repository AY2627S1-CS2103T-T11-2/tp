package seedu.address.ui;

import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.Person;

/**
 * Panel containing the list of persons.
 */
public class PersonListPanel extends UiPart<Region> {
    private static final String FXML = "PersonListPanel.fxml";
    private final Logger logger = LogsCenter.getLogger(PersonListPanel.class);

    @FXML
    private ListView<Person> personListView;
    private int lastClickedIndex = -1;

    /**
     * Creates a {@code PersonListPanel} with the given {@code ObservableList}.
     */
    public PersonListPanel(ObservableList<Person> personList) {
        super(FXML);
        personListView.setItems(personList);
        personListView.setCellFactory(listView -> new PersonListViewCell());
        personListView.setOnMouseClicked(event -> toggleSelectedCard());
    }

    /** Clears the selection when the user clicks the same card a second time. */
    private void toggleSelectedCard() {
        int selectedIndex = personListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex == lastClickedIndex) {
            personListView.getSelectionModel().clearSelection();
            lastClickedIndex = -1;
        } else {
            lastClickedIndex = selectedIndex;
        }
    }

    /**
     * Custom {@code ListCell} that displays the graphics of a {@code Person} using a {@code PersonCard}.
     */
    class PersonListViewCell extends ListCell<Person> {
        private Person displayedPerson;
        private int displayedIndex = -1;
        private Region card;
        private boolean heightUpdateScheduled;

        PersonListViewCell() {
            widthProperty().addListener((observable, oldWidth, newWidth) -> scheduleHeightUpdate());
        }

        @Override
        protected void updateItem(Person person, boolean empty) {
            super.updateItem(person, empty);

            if (empty || person == null) {
                setGraphic(null);
                setText(null);
                displayedPerson = null;
                displayedIndex = -1;
                card = null;
                setPrefHeight(Region.USE_COMPUTED_SIZE);
            } else {
                int currentIndex = getIndex();
                if (person != displayedPerson || currentIndex != displayedIndex) {
                    card = new PersonCard(person, currentIndex + 1).getRoot();
                    card.prefWidthProperty().bind(personListView.widthProperty());
                    card.maxWidthProperty().bind(personListView.widthProperty());
                    setGraphic(card);
                    if (getWidth() > 0) {
                        card.applyCss();
                        card.resize(getWidth(), card.prefHeight(getWidth()));
                        card.layout();
                        setPrefHeight(computePrefHeight(getWidth()));
                    }
                    displayedPerson = person;
                    displayedIndex = currentIndex;
                    scheduleHeightUpdate();
                }
            }
        }

        @Override
        protected double computePrefHeight(double width) {
            if (card == null) {
                return super.computePrefHeight(width);
            }

            Insets insets = getInsets();
            double cardWidth = Math.max(0, width - insets.getLeft() - insets.getRight());
            return insets.getTop() + card.prefHeight(cardWidth) + insets.getBottom();
        }

        private void scheduleHeightUpdate() {
            if (heightUpdateScheduled) {
                return;
            }
            heightUpdateScheduled = true;
            Platform.runLater(() -> {
                heightUpdateScheduled = false;
                if (card == null) {
                    return;
                }

                setPrefHeight(computePrefHeight(getWidth()));
                requestLayout();
                personListView.requestLayout();
            });
        }
    }

}
