package processing.test.orbitcombat;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

public class AccelerometerListener implements SensorEventListener {
    private float offsetX;
    private float offsetY;
    private float offsetZ;

    public void onSensorChanged(SensorEvent event) {
        this.offsetX = event.values[0];
        this.offsetY = event.values[1];
        this.offsetZ = event.values[2];
    }
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    public float getOffsetX() {
        return offsetX;
    }
}
