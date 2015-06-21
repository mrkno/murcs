package sws.murcs.model;

import edu.emory.mathcs.backport.java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import sws.murcs.magic.tracking.TrackableValue;

/**
 * A class representing a story in the backlog for a project.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Story extends Model {
    /**
     * A list of the conditions that have to be met before this
     * story can be marked as done. This has been made a list
     * (as opposed to a Collection) as order is important.
     */
    @TrackableValue
    private List<AcceptanceCondition> acceptanceCriteria;

    /**
     * A description of the story.
     */
    @TrackableValue
    private String description;

    /**
     * The person who created this story. This should not be changed after
     * initial creation.
     */
    @TrackableValue
    private Person creator;

    /**
     * Creates and initializes a new story
     */
    public Story(){
        acceptanceCriteria = new ArrayList<>();
    }

    /**
     * Gets an unmodifiable List containing all the Acceptance
     * Criteria for this story. To modify, use the dedicated
     * add and remove methods.
     * @return The acceptance criteria for this story
     */
    public final List<AcceptanceCondition> getAcceptanceCriteria(){
        return Collections.unmodifiableList(acceptanceCriteria);
    }

    /**
     * Adds a condition to the acceptance criteria for the story
     * @param condition The condition to add
     */
    public final void addAcceptanceCondition(final AcceptanceCondition condition) {
        this.acceptanceCriteria.add(condition);
        commit("Added acceptance criteria");
    }

    /**
     * Removes a condition from the list of acceptance criteria
     * @param condition The condition to remove.
     */
    public final void removeAcceptanceCriteria(final AcceptanceCondition condition){
        this.acceptanceCriteria.remove(condition);
        commit("Removed acceptance criteria");
    }

    /**
     * Gets a description for the current story.
     * @return The description.
     */
    public final String getDescription() {
        return description;
    }

    /**
     * Sets the description of the story.
     * @param newDescription The new description.
     */
    public final void setDescription(final String newDescription) {
        this.description = newDescription;
        commit("edit story");
    }

    /**
     * Gets the creator of this story.
     * @return The creator
     */
    public final Person getCreator() {
        return creator;
    }

    /**
     * Sets the creator of the story.
     * This should not be changed after initially being set.
     * @param person The creator
     */
    public final void setCreator(final Person person) {
        this.creator = person;
        commit("edit story");
    }

    @Override
    public final int hashCode() {
        int c = 0;
        if (getShortName() != null) {
            c = getShortName().hashCode();
        }
        return getHashCodePrime() + c;
    }

    @Override
    public final boolean equals(final Object object) {
        if (object == null || !(object instanceof Story)) {
            return false;
        }
        String shortNameO = ((Story) object).getShortName();
        String shortName = getShortName();
        if (shortName == null || shortNameO == null) {
            return shortName == shortNameO;
        }
        return shortName.toLowerCase().equals(shortNameO.toLowerCase());
    }
}
