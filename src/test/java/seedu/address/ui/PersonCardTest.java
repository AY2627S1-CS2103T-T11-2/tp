package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class PersonCardTest {

    private static final String LONG_TAG = "NeedsAdditionalConsultation";

    @BeforeAll
    public static void setUp() throws InterruptedException {
        CountDownLatch toolkitStarted = new CountDownLatch(1);
        Platform.startup(toolkitStarted::countDown);
        assertTrue(toolkitStarted.await(10, TimeUnit.SECONDS));
    }

    @AfterAll
    public static void tearDown() {
        Platform.exit();
    }

    @Test
    public void constructor_personWithLongTag_createsResponsiveLabelWithTooltip() throws InterruptedException {
        runOnFxThread(() -> {
            Person person = new PersonBuilder().withTags(LONG_TAG).build();

            PersonCard personCard = new PersonCard(person, 1);
            FlowPane tags = (FlowPane) personCard.getRoot().lookup("#tags");
            Label tagLabel = (Label) tags.getChildren().getFirst();

            assertEquals(LONG_TAG, tagLabel.getText());
            assertTrue(tagLabel.isWrapText());
            assertEquals(0, tagLabel.getMinWidth());
            assertEquals(LONG_TAG, tagLabel.getTooltip().getText());

            tags.resize(160, 40);
            assertEquals(160, tagLabel.getMaxWidth());
            tags.resize(0, 40);
            assertEquals(1, tagLabel.getMaxWidth());
        });
    }

    @Test
    public void rename_caseOnlyChange_refreshesDisplayedTag() throws Exception {
        Model model = new ModelManager();
        model.addPerson(new PersonBuilder().withTags("Student").build());
        Command rename = new AddressBookParser().parseCommand("edit 1 t/student,STUDENT");
        runOnFxThread(() -> {
            PersonListPanel panel = new PersonListPanel(model.getFilteredPersonList());
            new Scene(panel.getRoot(), 500, 500);
            panel.getRoot().applyCss();
            panel.getRoot().layout();
            assertTrue(panel.getRoot().lookupAll(".label").stream()
                    .anyMatch(node -> node instanceof Label label && label.getText().equals("Student")));
            try {
                rename.execute(model);
            } catch (CommandException exception) {
                throw new AssertionError(exception);
            }
            panel.getRoot().layout();
            assertTrue(panel.getRoot().lookupAll(".label").stream()
                    .anyMatch(node -> node instanceof Label label && label.getText().equals("STUDENT")));
        });
    }

    @Test
    public void constructor_longNameInNarrowList_showsCompleteNameWithTooltip() throws InterruptedException {
        String longName = "Muhammad Abdul Rahman Bin Mohamed Ismail S/O Krishnan Ramasamy Pillai Subramaniam";
        Model model = new ModelManager();
        model.addPerson(new PersonBuilder().withName(longName).build());
        runOnFxThread(() -> {
            PersonListPanel panel = new PersonListPanel(model.getFilteredPersonList());
            new Scene(panel.getRoot(), 450, 500);
            panel.getRoot().applyCss();
            panel.getRoot().layout();
            Label nameLabel = (Label) panel.getRoot().lookup("#name");
            String renderedName = nameLabel.lookupAll(".text").stream()
                    .map(node -> ((Text) node).getText())
                    .collect(Collectors.joining())
                    .replace("\n", " ")
                    .trim();

            assertTrue(nameLabel.isWrapText());
            assertEquals(longName, renderedName);
            assertEquals(longName, nameLabel.getTooltip().getText());
        });
    }

    private static void runOnFxThread(Runnable action) throws InterruptedException {
        CountDownLatch actionCompleted = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable throwable) {
                failure.set(throwable);
            } finally {
                actionCompleted.countDown();
            }
        });

        assertTrue(actionCompleted.await(10, TimeUnit.SECONDS));
        if (failure.get() != null) {
            fail(failure.get());
        }
    }
}
