package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
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
