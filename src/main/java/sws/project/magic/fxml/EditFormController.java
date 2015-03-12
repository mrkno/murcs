package sws.project.magic.fxml;

import javafx.beans.value.ChangeListener;

import java.util.function.Predicate;

/**
 *
 */
public interface EditFormController<T> {
    void setTitle(String text);
    void addValidator(Predicate<T> predicate);
    void addChangeListener(ChangeListener<T> listener);
}
