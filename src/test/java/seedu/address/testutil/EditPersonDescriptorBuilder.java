package seedu.address.testutil;

import java.util.stream.Stream;

import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.commands.TagEdit;
import seedu.address.model.person.Email;
import seedu.address.model.person.Group;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Subject;
import seedu.address.model.person.TelegramHandle;

/**
 * A utility class to help with building EditPersonDescriptor objects.
 */
public class EditPersonDescriptorBuilder {

    private EditPersonDescriptor descriptor;

    public EditPersonDescriptorBuilder() {
        descriptor = new EditPersonDescriptor();
    }

    public EditPersonDescriptorBuilder(EditPersonDescriptor descriptor) {
        this.descriptor = new EditPersonDescriptor(descriptor);
    }

    /**
     * Returns an {@code EditPersonDescriptor} with fields containing {@code person}'s details
     */
    public EditPersonDescriptorBuilder(Person person) {
        descriptor = new EditPersonDescriptor();
        descriptor.setName(person.getName());
        descriptor.setPhone(person.getPhone());
        descriptor.setEmail(person.getEmail());
        descriptor.setSubject(person.getSubject());
        descriptor.setGroup(person.getGroup());
        descriptor.setTelegramHandle(person.getTelegramHandle());
        descriptor.setTags(person.getTags());
    }

    /**
     * Sets the {@code Name} of the {@code EditPersonDescriptor} that we are building.
     */
    public EditPersonDescriptorBuilder withName(String name) {
        descriptor.setName(new Name(name));
        return this;
    }

    /**
     * Sets the {@code Phone} of the {@code EditPersonDescriptor} that we are building.
     */
    public EditPersonDescriptorBuilder withPhone(String phone) {
        descriptor.setPhone(new Phone(phone));
        return this;
    }

    /**
     * Sets the {@code Email} of the {@code EditPersonDescriptor} that we are building.
     */
    public EditPersonDescriptorBuilder withEmail(String email) {
        descriptor.setEmail(new Email(email));
        return this;
    }

    /**
     * Sets the {@code TelegramHandle} of the {@code EditPersonDescriptor} that we are building.
     */
    public EditPersonDescriptorBuilder withTelegramHandle(String telegramHandle) {
        descriptor.setTelegramHandle(new TelegramHandle(telegramHandle));
        return this;
    }

    /**
     * Sets the {@code Subject} of the {@code EditPersonDescriptor} that we are building.
     */
    public EditPersonDescriptorBuilder withSubject(String validSubject) {
        descriptor.setSubject(new Subject(validSubject));
        return this;
    }

    /**
     * Sets the {@code Group} of the {@code EditPersonDescriptor} that we are building.
     */
    public EditPersonDescriptorBuilder withGroup(String validGroup) {
        descriptor.setGroup(new Group(validGroup));
        return this;
    }
    /**
     * Sets tag additions in entry order on the {@code EditPersonDescriptor}
     * that we are building.
     */
    public EditPersonDescriptorBuilder withTags(String... tags) {
        descriptor.setTagEdits(Stream.of(tags).map(tag -> new TagEdit(null, tag)).toList());
        return this;
    }

    public EditPersonDescriptor build() {
        return descriptor;
    }

}
