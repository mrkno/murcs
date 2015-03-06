package sws.dontclick;


import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import org.junit.Test;
import org.loadui.testfx.GuiTest;

import java.io.IOException;

import static junit.framework.Assert.assertNotSame;

/**
 * Tests button click with Testfx
 *
 * Created by jayha_000 on 3/2/2015.
 */
public class ButtonClickStepDefsTest extends GuiTest {


    /**
     * When I click the button
     * Then the text changes
     * @throws Exception
     */
    @Test
    public void When_I_click_the_button_Then_the_text_changes() throws Exception{
        Button button = find("#button");
        click(button);
        assertNotSame("This is not working", button.getText());
    }

    @Override
    protected Parent getRootNode() {
        try {
            return FXMLLoader.load(getClass().getResource("/testwindow.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new IllegalArgumentException();
    }
}
