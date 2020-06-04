package processing.test.orbitcombat;

import processing.core.PApplet;
import processing.core.PConstants;

public class Menu {
    private PApplet p;
    public boolean isEnabled = true;

    private float[] multiplayerButton;
    private float[] singleplayerButton;

    public Menu(PApplet p) {
        this.p = p;

        this.multiplayerButton = new float[]{ 0, 0, (float) (p.width*0.60), (float) (p.height*0.1), 10 };
        this.singleplayerButton = new float[]{ 0, (float) (p.height*0.15), (float) (p.width*0.60), (float) (p.height*0.1), 10 };
    }

    private void showLogo() {
        p.strokeWeight(4);
        p.stroke(255);
        p.noFill();
        p.circle(0, (float) (-p.height*0.3), (float) (p.width*0.5));

        p.noStroke();
        p.fill(0);
        p.circle(0, (float) (-p.height*0.3), (float) ((p.width*0.5)*0.3));

        p.noStroke();
        p.fill(255);
        p.circle(0, (float) (-p.height*0.3 + p.width*0.25), (float) ((p.width*0.5)*0.2));
    }

    private void showButton(float[] parameters, String text) {
        p.rectMode(PConstants.CENTER);
        p.stroke(255);
        p.strokeWeight(4);
        p.noFill();
        p.rect(parameters[0], parameters[1], parameters[2], parameters[3], parameters[4]);

        p.textAlign(PConstants.CENTER, PConstants.CENTER);
        p.textSize((float) (p.height*0.04));
        p.text(text, parameters[0], parameters[1]);
    }

    public void show() {
        showLogo();
        showButton(multiplayerButton, "MULTIPLAYER");
        showButton(singleplayerButton, "SINGLEPLAYER");
    }

    public boolean isClickedOnButton(float[] parameters, float x, float y) {
        x -= p.width/2;
        y -= p.height/2;

        float rectXOffset = parameters[2]/2;
        float rectYOffset = parameters[3]/2;
        if      (x > parameters[0] - rectXOffset &&
                x < parameters[0] + rectXOffset &&
                y > parameters[1] - rectYOffset &&
                y < parameters[1] + rectYOffset)
            return true;
        return false;
    }

    public boolean isClickedMultiplayerButton(float x, float y) {
        return isClickedOnButton(multiplayerButton, x ,y);
    }

    public boolean isClickedSingleplayerButton(float x, float y) {
        return isClickedOnButton(singleplayerButton, x, y);
    }
}

