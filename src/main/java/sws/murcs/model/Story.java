package sws.murcs.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import sws.murcs.magic.tracking.TrackableValue;
import sws.murcs.magic.tracking.UndoRedoManager;

/**
 * A class representing a story in the backlog for a project.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Story extends Model {

    /**
     * Represents the current state of a story.
     */
    public enum StoryState {

        /**
         * Indicates that the story state has not yet been set.
         */
        None,

        /**
         * Indicates that a story is not yet ready to
         * be pulled into a sprint.
         */
        Ready
    }

    /**
     * Indicates the current state of the story
     * (e.g. ready, not ready, in progress)
     */
    @TrackableValue
    private StoryState storyState = StoryState.None;

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
     * The estimate for this story.
     */
    @TrackableValue
    private String estimate;

    /**
     * Creates and initializes a new story.
     */
    public Story() {
        acceptanceCriteria = new ArrayList<>();
        estimate = "Not Estimated";
    }

    /**
     * Gets an unmodifiable List containing all the Acceptance
     * Criteria for this story. To modify, use the dedicated
     * add and remove methods.
     * @return The acceptance criteria for this story
     */
    public final List<AcceptanceCondition> getAcceptanceCriteria() {
        return Collections.unmodifiableList(acceptanceCriteria);
    }

    /**
     * Adds a condition to the acceptance criteria for the story if it is not already one of the ACs. This is
     * not intended to stop ACs with the same text being added but to prevent the same object being added multiple
     * times.
     * @param condition The condition to add
     */
    public final void addAcceptanceCondition(final AcceptanceCondition condition) {
        if (!acceptanceCriteria.contains(condition)) {
            acceptanceCriteria.add(condition);
            //I'm not sure about this Matthew but if I don't have it
            //then the object is not tracked
            UndoRedoManager.add(condition);
        }
        commit("edit acceptance criteria");
    }

    /**
     * Moves an Acceptance Condition to a new position in the list.
     * @param condition The condition to move
     * @param newPosition The new position
     */
    public final void repositionCondition(final AcceptanceCondition condition, final int newPosition) {
        acceptanceCriteria.remove(condition);
        acceptanceCriteria.add(newPosition, condition);

        commit("edit acceptance criteria");
    }

    /**
     * Removes a condition from the list of acceptance.
     * @param condition The condition to remove.
     */
    public final void removeAcceptanceCriteria(final AcceptanceCondition condition) {
        acceptanceCriteria.remove(condition);
        commit("edit acceptance criteria");
    }

    /**
     * Gets the current state of the story.
     * @return The current state of the story
     */
    public final StoryState getStoryState() {
        return storyState;
    }

    /**
     * Sets the current state of a story. We're trusting you don't
     * do something silly here. Don't let us down.
     * @param newState The new state for the story.
     */
    public final void setStoryState(final StoryState newState) {
        if (storyState == newState) {
            return;
        }
        storyState = newState;
        commit("edit story");
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
        description = newDescription;
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
        creator = person;
        commit("edit story");
    }

    /**
     * Gets the estimate value.
     * @return The estimate value.
     */
    public final String getEstimate() {
        return estimate;
    }

    /**
     * Sets the estimate for the story.
     * @param newEstimate The estimate.
     */
    public final void setEstimate(final String newEstimate) {
        if (newEstimate == estimate) {
            return;
        }
        estimate = newEstimate;
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
