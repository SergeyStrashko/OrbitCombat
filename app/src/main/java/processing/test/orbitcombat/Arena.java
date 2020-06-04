package processing.test.orbitcombat;

import processing.core.PApplet;
import processing.core.PConstants;

public class Arena {
    private PApplet p;
    public float x;
    public float y;
    public float D;
    public float R;

    Arena(PApplet p) {
        this.p = p;

        this.x = 0;
        this.y = 0;

        this.D = (float) ((x >= y) ? p.height/2 * 0.85f : p.width/2 * 0.85f);
        this.R = D/2;
    }

    public void show() {
        p.stroke(0);
        p.strokeWeight(4);
        p.noFill();
        p.circle(x, y ,D);
    }

    public void setScore(int score) {
        p.textAlign(PConstants.CENTER, PConstants.CENTER);
        p.fill(255);
        p.textSize(this.R/2);
        p.text(score, 0, 0);
    }
}
