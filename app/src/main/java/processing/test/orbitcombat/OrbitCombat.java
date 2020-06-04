package processing.test.orbitcombat;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

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
import java.util.stream.Collectors;

public class OrbitCombat extends PApplet {
    private PApplet                 processing      = this;

    private Arena                   arena;
    private Ship                    ship;
    private Ship                    target;

    private Bullet                  bulletNew;

    private int                     backgroundColor = 152;

    private boolean                 shooting        = false;

    private Context                 context;
    private SensorManager           manager;
    private Sensor sensor;
    private AccelerometerListener   listener;

    private int                     port            = 6868;
    private String                  address         = "192.168.1.100";
    private InetAddress             serverAddress   = null;
    private Socket                  socket;

    private JSONObject              shipInfo        = new JSONObject();

    public void settings() {  fullScreen(); }

    public void setup() {
        arena       = new Arena(processing);
        ship        = new Ship(processing, arena.x, arena.y + arena.R, arena.R, 255);
        target      = new Ship(processing, arena.x, arena.y - arena.R, arena.R, 0);

        context     = getActivity();
        manager     = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        sensor      = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        listener    = new AccelerometerListener();
        manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME);

        showWaitingLabel();
        setUpConnectionToServer();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void draw() {
        new DataExchanger().run();
        new Drawer().run();

        arena.show();
        ship.show();
        target.show();
    }

    private void showWaitingLabel() {
        background(backgroundColor);

        textAlign(CENTER, CENTER);
        textSize((float) (width*0.2));
        text("Waiting...", width/2, height/2);
    }

    private void setUpConnectionToServer() {
        try {
            serverAddress   = InetAddress.getByName(address);
            socket          = new Socket(serverAddress, port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void touchStarted() {
        shooting = true;
    }

    public void touchEnded() {
        shooting = false;
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

    class DataExchanger extends Thread {
        public void run() {
            try {
                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);

                shipInfo.put("x", ship.x);
                shipInfo.put("y", ship.y);
                shipInfo.put("health", ship.health);

                JSONArray bullets = new JSONArray();

                if (bulletNew != null) {
                    JSONObject bj = new JSONObject();
                    bj.put("x", bulletNew.x);
                    bj.put("y", bulletNew.y);
                    bullets.append(bj);
                    bulletNew = null;
                }

                shipInfo.put("bullets", bullets);

                String s = shipInfo.toString().replaceAll("\\n", "");

                writer.println(s);

                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                String res = reader.readLine();

                JSONObject json = JSONObject.parse(res);

                target.x = -json.getFloat("x");
                target.y = -json.getFloat("y");

                bullets = json.getJSONArray("bullets");

                for (int i = 0; i < bullets.size(); i++) {
                    target.bullets.add(new Bullet(processing, -bullets.getJSONObject(i).getFloat("x"), -bullets.getJSONObject(i).getFloat("y"), arena.R));
                }

                ship.health = json.getInt("health");
            } catch (UnknownHostException ex) {
                System.out.println("Server not found: " + ex.getMessage());
            } catch (IOException ex) {
                System.out.println("I/O error: " + ex.getMessage());
            }
        }
    }

    class Drawer extends Thread {
        @RequiresApi(api = Build.VERSION_CODES.N)
        public void run() {
            background(backgroundColor);
            translate(width/2, height/2);

            ship.bullets = ship.bullets.stream().filter(bullet -> !target.checkHit(target.x, target.y, bullet.x, bullet.y)).collect(Collectors.toCollection(ArrayList::new));

            target.bullets = target.bullets.stream().filter(bullet -> !ship.checkHit(ship.x, ship.y, bullet.x, bullet.y)).collect(Collectors.toCollection(ArrayList::new));

            arena.setScore(target.health);

            if (shooting) {
                bulletNew = ship.shoot();
                shooting = false;
            }

            if (listener.getOffsetX() >= 0.5)   ship.move(2);
            if (listener.getOffsetX() <= -0.5)  ship.move(-2);
        }
    }
}
