package processing.test.orbitcombat;

import processing.core.PApplet;

public class Bullet {
    private PApplet p;

    public float x;
    public float y;

    private float R;

    private float speedX;
    private float speedY;

    private float bulletSpeed = 20;

    Bullet(PApplet p, float x, float y, float R) {
        this.p = p;

        this.x = x;
        this.y = y;

        this.R = R;

        this.speedX = (-this.x)/bulletSpeed;
        this.speedY = (-this.y)/bulletSpeed;
    }

    public void show() {
        p.strokeWeight(1);
        p.circle(this.x, this.y, (float) (this.R*0.05f));

        this.x += this.speedX;
        this.y += this.speedY;
    }

    public boolean checkSelfDestruct() {
        if (this.x < -p.width/2 || this.x > p.width/2 || this.y < -p.height/2 || this.y > p.height) {
            return true;
        }
        return false;
    }
}
