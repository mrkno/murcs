package sws.murcs.controller.controls.md;

import com.sun.javafx.scene.control.skin.ToggleButtonSkin;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.scene.control.Skin;
import javafx.scene.control.ToggleButton;
import javafx.scene.effect.BoxBlur;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Material design toggle button class.
 */
public class MaterialDesignToggleButton extends ToggleButton {

    /**
     * The millisecond duration of the ripple effect.
     */
    private static final int DURATION = 250;

    /**
     * The opacity of the ripple itself.
     */
    private static final double RIPPLE_OPACITY = 0.11;

    /**
     * The radius of the ripple effect.
     */
    private static final double RADIUS = 0.15;

    /**
     * The circle used to have the ripple effect.
     */
    private Circle circleRipple;

    /**
     * The rectangle used to constrain the ripple effect.
     */
    private Rectangle rippleClip = new Rectangle();

    /**
     * The duration of the ripple effect.
     */
    private Duration rippleDuration =  Duration.millis(DURATION);

    /**
     * The height of constraining rectangle the last time the ripple effect was displayed.
     */
    private double lastRippleHeight = 0;

    /**
     * The width of the constraining rectangle last time the ripple effect was displayed.
     */
    private double lastRippleWidth = 0;

    /**
     * The colour of the ripple effect.
     */
    private Color rippleColor = new Color(0, 0, 0, RIPPLE_OPACITY);

    /**
     * Creates a new Material Design Toggle Button with the given text.
     * @param text The text displayed on the button.
     */
    public MaterialDesignToggleButton(final String text) {
        super(text);

        getStyleClass().addAll("md-button");

        createRippleEffect();
    }

    @Override
    protected final Skin<?> createDefaultSkin() {
        final ToggleButtonSkin buttonSkin = new ToggleButtonSkin(this);
        // Adding circleRipple as fist node of button nodes to be on the bottom
        this.getChildren().add(0, circleRipple);
        return buttonSkin;
    }

    /**
     * Creates the ripple effect that is used when the button is clicked on.
     */
    private void createRippleEffect() {
        circleRipple = new Circle(RADIUS, rippleColor);
        circleRipple.setOpacity(0.0);
        // Optional box blur on ripple - smoother ripple effect
        final int three = 3;
        circleRipple.setEffect(new BoxBlur(three, three, 2));

        // Fade effect bit longer to show edges on the end
        final FadeTransition fadeTransition = new FadeTransition(rippleDuration, circleRipple);
        fadeTransition.setInterpolator(Interpolator.EASE_OUT);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.0);

        final Timeline scaleRippleTimeline = new Timeline();

        final SequentialTransition parallelTransition = new SequentialTransition();
        parallelTransition.getChildren().addAll(
                scaleRippleTimeline,
                fadeTransition
        );

        parallelTransition.setOnFinished(event1 -> {
            circleRipple.setOpacity(0.0);
            circleRipple.setRadius(RADIUS);
        });

        this.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            parallelTransition.stop();
            parallelTransition.getOnFinished().handle(null);

            circleRipple.setCenterX(event.getX());
            circleRipple.setCenterY(event.getY());

            // Recalculate ripple size if size of button from last time was changed
            if (getWidth() != lastRippleWidth || getHeight() != lastRippleHeight) {
                lastRippleWidth = getWidth();
                lastRippleHeight = getHeight();

                rippleClip.setWidth(lastRippleWidth);
                rippleClip.setHeight(lastRippleHeight);

                try {
                    rippleClip.setArcHeight(this.getBackground()
                            .getFills().get(0).getRadii().getTopLeftHorizontalRadius());
                    rippleClip.setArcWidth(this.getBackground()
                            .getFills().get(0).getRadii().getTopLeftHorizontalRadius());
                    circleRipple.setClip(rippleClip);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Getting 45% of longest button's length, because we want edge of ripple effect always visible
                final double percentage = 0.45;
                double circleRippleRadius = Math.max(getHeight(), getWidth()) * percentage;
                final KeyValue keyValue =
                        new KeyValue(circleRipple.radiusProperty(), circleRippleRadius, Interpolator.EASE_OUT);
                final KeyFrame keyFrame = new KeyFrame(rippleDuration, keyValue);
                scaleRippleTimeline.getKeyFrames().clear();
                scaleRippleTimeline.getKeyFrames().add(keyFrame);
            }

            parallelTransition.playFromStart();
        });
    }

    /**
     * Sets the ripple colour.
     * @param colour The colour of the ripple.
     */
    public final void setRippleColor(final Color colour) {
        circleRipple.setFill(colour);
    }

}
