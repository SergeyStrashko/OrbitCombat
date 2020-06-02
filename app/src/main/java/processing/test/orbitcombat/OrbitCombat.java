package processing.test.orbitcombat;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import processing.core.*;
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*;

import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.ArrayList; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class OrbitCombat extends PApplet {
    Arena arena;
    Ship ship;
    Ship target;

    int backgroundColor = 152;

    boolean shooting = false;

    Context context;
    SensorManager manager;
    Sensor sensor;
    AccelerometerListener listener;
    float ax, ay, az;

    int port = 6868;

    InetAddress serverAddress = null;

    class AccelerometerListener implements SensorEventListener {
        public void onSensorChanged(SensorEvent event) {
            ax = event.values[0];
            ay = event.values[1];
            az = event.values[2];
        }
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    public void onResume() {
        super.onResume();
        if (manager != null) {
            manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    public void onPause() {
        super.onPause();
        if (manager != null) {
            manager.unregisterListener(listener);
        }
    }

    public void setup() {
        background(backgroundColor);

        arena = new Arena();
        ship = new Ship(arena.x, arena.y + arena.R, arena.R, 255);
        target = new Ship(arena.x, arena.y - arena.R, arena.R, 0);

        context = getActivity();
        manager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        sensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        listener = new AccelerometerListener();
        manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME);

        try {
            serverAddress = InetAddress.getByAddress(new byte[]{(byte) 10, (byte) 42, (byte) 0, (byte) 1});
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void draw() {
        background(backgroundColor);
        translate(width/2, height/2);
        arena.show();
        ship.show();
        target.show();

        arena.setScore(target.health);

        if (shooting) {
            ship.shoot();
            shooting = false;
        }

        if (ax >= 0.5) ship.move(2);
        if (ax <= -0.5) ship.move(-2);

        target.shoot();

        ArrayList<Ship.Bullet> shipBullets = ship.bullets;
        for (int i = 0; i < shipBullets.size(); i++) {
            if(target.checkHit(target.x, target.y, shipBullets.get(i).x, shipBullets.get(i).y)) ship.bullets.remove(i);
        }

        ArrayList<Ship.Bullet> targetBullets = target.bullets;
        for (int i = 0; i < targetBullets.size(); i++) {
            if(ship.checkHit(ship.x, ship.y, targetBullets.get(i).x, targetBullets.get(i).y)) target.bullets.remove(i);
        }

        try (Socket socket = new Socket(serverAddress, port)) {
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            writer.println(ship.x + " " + ship.y);

        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
    }

    public void touchStarted() {
        shooting = true;
    }

    public void touchEnded() {
        shooting = false;
    }

    class Arena {
        public float x;
        public float y;
        public float D;
        public float R;

        Arena() {
            this.x = 0;
            this.y = 0;

            this.D = (float) ((x >= y) ? height/2 * 0.85f : width/2 * 0.85f);
            this.R = D/2;
        }

        public void show() {
            stroke(0);
            strokeWeight(4);
            noFill();
            circle(x, y ,D);
        }

        public void setScore(int score) {
            textAlign(PConstants.CENTER, PConstants.CENTER);
            fill(255);
            textSize(this.R/2);
            text(score, 0, 0);
        }
    }


    class Ship {
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

        Ship(float x, float y, float R, int c) {
            this.x = x;
            this.y = y;
            this.R = R;

            this.shipSize = (int) (R*0.25f);

            this.colorTemp = c;
            this.baseColor = this.colorTemp;

            if (y < 0) this.barrierHeight = -100;
        }

        public void show() {
            fill(colorTemp);
            this.colorTemp = this.baseColor;

            noStroke();
            circle(x, y, this.shipSize);

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

        class Bullet {
            public float x;
            public float y;

            private float speedX;
            private float speedY;

            private float bulletSpeed = 20;

            Bullet(float x, float y) {
                this.x = x;
                this.y = y;

                this.speedX = (-this.x)/bulletSpeed;
                this.speedY = (-this.y)/bulletSpeed;
            }

            public void show() {
                strokeWeight(1);
                circle(this.x, this.y, (float) (R*0.05f));

                this.x += this.speedX;
                this.y += this.speedY;
            }

            public boolean checkSelfDestruct() {
                if (this.x < -width/2 || this.x > width/2 || this.y < -height/2 || this.y > height) {
                    return true;
                }
                return false;
            }
        }

        public void shoot() {
            if (bullets.size() < maxBulletsCount)
                bullets.add(new Bullet(this.x, this.y));
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

    public void settings() {  fullScreen(); }
}
