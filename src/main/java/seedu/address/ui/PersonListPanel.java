package seedu.address.ui;

import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.FlowPane;
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
                    card.prefWidthProperty().bind(Bindings.createDoubleBinding(this::getCardWidth, widthProperty()));
                    card.maxWidthProperty().bind(card.prefWidthProperty());
                    FlowPane tags = (FlowPane) card.lookup("#tags");
                    tags.widthProperty().addListener((observable, oldWidth, newWidth) -> scheduleHeightUpdate());
                    setGraphic(card);
                    if (getWidth() > 0) {
                        card.applyCss();
                        card.resize(getCardWidth(), card.prefHeight(getCardWidth()));
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
            return insets.getTop() + card.prefHeight(getCardWidth(width)) + insets.getBottom();
        }

        private double getCardWidth() {
            return getCardWidth(getWidth());
        }

        private double getCardWidth(double cellWidth) {
            Insets insets = getInsets();
            return Math.max(0, cellWidth - insets.getLeft() - insets.getRight());
        }

        private void scheduleHeightUpdate() {
            if (heightUpdateScheduled) {
                return;
            }
            heightUpdateScheduled = true;
            Platform.runLater(() -> {
                if (card == null) {
                    heightUpdateScheduled = false;
                    return;
                }

                card.applyCss();
                double cardWidth = getCardWidth();
                card.resize(cardWidth, card.prefHeight(cardWidth));
                card.layout();

                double updatedHeight = computePrefHeight(getWidth());
                if (Math.abs(getPrefHeight() - updatedHeight) > 0.5) {
                    setPrefHeight(updatedHeight);
                    requestLayout();
                    personListView.requestLayout();
                }
                heightUpdateScheduled = false;
            });
        }
    }

}
