package sws.murcs.exceptions;

import sws.murcs.view.App;

/**
 * The class to use if you don't want to try catch your exceptions. And therefore Force choke the life out of someone
 */
public class ImperialException extends RuntimeException {

    /**
     * Very import marching song.
     */
    private static final String MARCH = "Dion Vader!! DA DA DA, DUN DADAA DUN DADAA, DEIN DEIN DEIN, DEIN DADUN, DUN DADAA, DIN DUN DINDIN DEIN DIN DUNDUNDUN... Oh FUCK IT!";

    @Override
    public String getMessage() {
        App.invade();
        return MARCH;
    }
}
