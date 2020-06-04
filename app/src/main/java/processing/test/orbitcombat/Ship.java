package processing.test.orbitcombat;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PConstants;

public class Ship {
    PApplet p;

    public float x;
    public float y;
    private float R;
    private int shipSize;
    public boolean defeted = false;

    private int baseColor;
    public int colorTemp;

    private int barrierHeight = 100;
    public int health = 100;
    private int maxBulletsCount = 5;

    public ArrayList<Bullet> bullets = new ArrayList<Bullet>();

    Ship(PApplet p, float x, float y, float R, int c) {
        this.p = p;
        this.x = x;
        this.y = y;
        this.R = R;

        this.shipSize = (int) (R*0.25f);

        this.colorTemp = c;
        this.baseColor = this.colorTemp;

        if (y < 0) this.barrierHeight = -100;
    }

    public void show() {
        p.fill(colorTemp);
        this.colorTemp = this.baseColor;

        p.ellipseMode(PConstants.CENTER);
        p.noStroke();
        p.circle(x, y, this.shipSize);

        for (Bullet bullet : bullets) {
            bullet.show();
        }

        for (int i = 0; i < bullets.size(); i++) {
            if (bullets.get(i).checkSelfDestruct()) bullets.remove(i);
        }
    }

    public void move(int direction) {
        double angle = Math.toRadians(direction);

        float tempX = -x;
        float tempY = -y;

        this.x = (float)(tempX * Math.cos(angle) - tempY * Math.sin(angle));
        this.y = (float)(tempY * Math.cos(angle) + tempX * Math.sin(angle));

        this.x *= -1;
        this.y *= -1;

        double distance = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
        if (R - distance > 1) {
            this.y += R - distance;
        }

        if ((this.y <= barrierHeight) && (this.y > 0) || (this.y >= barrierHeight) && (this.y < 0))
        {
            this.y = -tempY;
            this.x = -tempX;
        }
    }

    public Bullet shoot() {
        Bullet b = null;
        if (bullets.size() < maxBulletsCount) {
            b = new Bullet(p, this.x, this.y, this.R);
            bullets.add(b);
        }
        return b;
    }

    public boolean checkHit(float tX, float tY, float bX, float bY) {
        if ((bX - tX)*(bX - tX) + (bY - tY)*(bY - tY) < (this.shipSize/2)*(this.shipSize/2)) {
            if (this.health > 0) this.health--;
            else this.defeted = true;

            this.colorTemp = (this.baseColor == 255) ? 0 : 255;
            return true;
        }
        return false;
    }
}
