package com.example.softwareing;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.GenEV3;
import it.unive.dais.legodroid.lib.comm.BluetoothConnection;
import it.unive.dais.legodroid.lib.plugs.TachoMotor;
import it.unive.dais.legodroid.lib.plugs.UltrasonicSensor;
import it.unive.dais.legodroid.lib.util.Prelude;
import it.unive.dais.legodroid.lib.util.ThrowingConsumer;

public class Task1 extends AppCompatActivity {

    /*-----------------------------NOT IMPORTANT--------------------------------------------*/

    private static class MyCustomApi extends EV3.Api {
        private MyCustomApi(@NonNull GenEV3<? extends EV3.Api> ev3) {
            super(ev3);
        }

        public void mySpecialCommand() {
        }
    }

    private void legoMainCustomApi(MyCustomApi api) {
        final String TAG = Prelude.ReTAG("legoMainCustomApi");
        // specialized methods can be safely called
        api.mySpecialCommand();
        // stub the other main
        legoMain(api);
    }


    /*giro*/
    private double mod(double x, double y)
    {
        double result = x % y;
        return result < 0? result + y : result;
    }

    private void legoMain(EV3.Api api) {
        /* Motor Setup */
        motorDx = api.getTachoMotor(EV3.OutputPort.C);
        motorFunctional = api.getTachoMotor(EV3.OutputPort.B);
        motorSx = api.getTachoMotor(EV3.OutputPort.A);

        if (motorDx != null && motorSx != null && motorFunctional != null){
            log.append("motori collegati\n");
        } else {
            log.append("motori non collegati\n");
        }

        /* Proximity Sensor Setup */
        proximity_sensor = api.getUltrasonicSensor(EV3.InputPort._1);

        if (proximity_sensor != null){
            log.append("sensore prossimita' collegato\n");
        } else {
            log.append("sensore prossimita' non collegati\n");
        }
    }

    /* Take initial input information */
    private void take_initial_data() {

        /* Take input: numero righe e colonne */
        accept_input.setOnClickListener(
                v -> {
                    ROW = Integer.parseInt(input_row.getText().toString());
                    COL = Integer.parseInt(input_col.getText().toString());
                    log.append("\nIl campo e' : " + String.valueOf(ROW) + "x" + String.valueOf(COL));
                    campo = new int[ROW][COL];
                }
        );

        /* Take input: posizione di partenza */
        accept_start.setOnClickListener(
                v -> {
                    my_row = Integer.parseInt(start_row.getText().toString());
                    my_col = Integer.parseInt(start_col.getText().toString());
                    log.append("\nInizio alla posizione : " + String.valueOf(my_row) + "x" + String.valueOf(my_col));

                }
        );

        /* Take input: totale palline nel campo */
        accept_balls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tot_balls = Integer.parseInt(balls.getText().toString());
                log.append("\nTot palline da prelevare " + String.valueOf(tot_balls));
            }
        });

    }

    /* motors wrapper */
    private void applyMotorFunctional(@NonNull ThrowingConsumer<TachoMotor, Throwable> f) {
        if (motorFunctional != null)
            Prelude.trap(() -> f.call(motorFunctional));
        else {
            log.append("Motore funzionale non connesso\n");
        }
    }

    private void applyMotorDx(@NonNull ThrowingConsumer<TachoMotor, Throwable> f) {
        if (motorDx != null)
            Prelude.trap(() -> f.call(motorDx));
        else {
            log.append("Motore destro non connesso\n");
        }
    }

    private void applyMotorSx(@NonNull ThrowingConsumer<TachoMotor, Throwable> f) {
        if (motorSx != null)
            Prelude.trap(() -> f.call(motorSx));
        else {
            log.append("Motore sinistro non connesso\n");
        }
    }


    private void resetGiroscope(){
        orientation =new OrientationListener(sensorManager);

        log.append("\n---resetting gyroscope :D---");
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.i("error", "LA PRIMA SLEEP è STRONZA");
        }
        this.my_angle = orientation.getRotation();
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.i("error", "LA PRIMA SLEEP è STRONZA");
        }
    }
    /*-----------------------------NOT IMPORTANT--------------------------------------------*/

    /* button Start Task1: avvia la funzione complete_task1*/
    private Button avvio;

    /* log for see all actions */
    private TextView log;

    /* motori */
    private TachoMotor motorDx;
    private TachoMotor motorSx;
    private TachoMotor motorFunctional;

    /* sensori per orientarsi */
    private SensorManager sensorManager;
    private OrientationListener orientation;
    private TextView gyro_text;
    private double my_angle;

    /* sensore prossimita pallina */
    private UltrasonicSensor proximity_sensor;

    /* costanti velocita: range da [-100, 100] */
    int VELOCITY = 40;

    /* Take input: numero righe e colonne */
    private Button accept_input;
    private EditText input_row;
    private EditText input_col;
    private int ROW;
    private int COL;

    /* Take input: posizione di partenza */
    private Button accept_start;
    private EditText start_row;
    private EditText start_col;
    private int my_row;
    private int my_col;

    /* Take input: numero palline totali nel campo da gioco */
    private Button accept_balls;
    private EditText balls;
    private int tot_balls;

    /* campo da gioco */
    private int[][] campo;

//log.setMovementMethod(new ScrollingMovementMethod());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task1);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /* Log for read all actions */
        log = findViewById(R.id.log);
        log.setMovementMethod(new ScrollingMovementMethod());

        /* Button Avvia Task 1 */
        avvio = findViewById(R.id.task1);

        /* Text view per giroscopio legodroid */
        gyro_text = findViewById(R.id.giro);

        /* sensor gyroscope */
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        orientation = new OrientationListener(sensorManager);

        /* Take input: numero righe e colonne */
        accept_input = (Button) findViewById(R.id.accept_input);
        input_row = (EditText) findViewById(R.id.input_row);
        input_col = (EditText) findViewById(R.id.input_col);

        /* Take input: numero palline totali nel campo da gioco */
        accept_balls = (Button) findViewById(R.id.accept_balls);
        balls = (EditText) findViewById(R.id.tot_balls);

        /* Take input: posizione di partenza */
        accept_start = (Button) findViewById(R.id.accept_start);
        start_row = (EditText) findViewById(R.id.start_row);
        start_col = (EditText) findViewById(R.id.start_col);

        try {
            /* Connessione al Robot */
            BluetoothConnection.BluetoothChannel conn = new BluetoothConnection("k3k").connect();
            GenEV3<MyCustomApi> ev3 = new GenEV3<>(conn);
            log.append("\nconnessione avvenuta\n");

            /* Premendo il Button Avvio si esegue la funzione complete_task1 */
            avvio.setOnClickListener((v -> Prelude.trap(() -> {
                ev3.run(this::complete_task1, MyCustomApi::new);
            })));

            /* Inizializza tutte le componenti del robot (motori, sensori..) */
            legoMainCustomApi(new MyCustomApi(ev3));

        } catch (IOException e) {
            e.printStackTrace();
            log.append("\nerrore nella connessione\n");
        }

        take_initial_data();
    }

    /*-----------------------------------FUNZIONI MOTORI----------------------------------------*/
    /* Rimette in posizione esatta il robot tramite il giroscopio */
    /* Rimette in posizione esatta il robot tramite il giroscopio */
    private void restore_direction(EV3.Api api) {

        double angle = (double)Math.round(orientation.getRotation()*100)/100;
        my_angle = (double)Math.round(my_angle*100)/100;
        double offset = 0.3;
        int flag = (mod((angle - my_angle),360) > 180) ? /*destra : sinistra*/ 1 : -1;

        while (mod(-flag*(angle-my_angle),360) > offset){
            /*nell' if faccio una differenza tra i due e vedo quando la differenza è < offset

            /*debug*/
            gyro_text.setText(angle + " -> " + my_angle);

            int finalFlag = flag;
            applyMotorDx((m) -> {
                m.setPower(10*(-finalFlag));
                // m.setStepPower(-50,20,200,20,false);
                m.start();
            });
            applyMotorSx((m) -> {
                m.setPower(10*(finalFlag));
                //m.setStepPower(50,20,200,20,false);
                m.start();

            });

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            angle = (double)Math.round(orientation.getRotation()*100)/100;
            flag = (mod((angle - my_angle),360) > 180) ? /*destra : sinistra*/ 1 : -1;

        }

        applyMotorDx(TachoMotor::brake);
        applyMotorSx(TachoMotor::brake);

    }

    /* Muove il robot di 90 gradi e chiama restore_direction */
    private void rotazione_plus_90(EV3.Api api) {

        this.my_angle = (this.my_angle + 90) % 360;
        log.append("\ngiro a destra verso l'angolo:" + this.my_angle);
        //this.orientamento = (int) mod(this.orientamento+1, 4) == 0 ? 4 : (int) mod(this.orientamento+1, 4);

        /*applica una Velocita per un certo numero di millisecondi tot = step1 + step2 + step3 */
        //log.append("mi ruoto di 90 gradi in senso orario");
        applyMotorDx((m) -> {
            m.waitCompletion();
            m.waitUntilReady();
            m.setTimeSpeed(-VELOCITY, 200, 650, 200, true);
            m.start();
        });

        applyMotorSx((m) -> {
            m.setTimeSpeed(+VELOCITY, 200, 650, 200, true);
            m.start();
            m.waitCompletion();
            m.waitUntilReady();
        });

        restore_direction(api);
    }

    /* Muove il robot di -90 gradi e chiama restore_direction */
    private void rotazione_minus_90(EV3.Api api) {

        this.my_angle = mod((this.my_angle - 90),360);
        log.append("\ngiro a sinistra verso l'angolo:" + this.my_angle);
        //this.orientamento = (int) mod(this.orientamento-1, 4) == 0 ? 4 : (int) mod(this.orientamento-1, 4);

        applyMotorDx((m) -> {
            m.waitCompletion();
            m.waitUntilReady();

            m.setTimeSpeed(+VELOCITY, 200, 650, 200, true);
            m.start();
        });

        applyMotorSx((m) -> {
            m.setTimeSpeed(-VELOCITY, 200, 650, 200, true);
            m.start();

            m.waitCompletion();
            m.waitUntilReady();
        });

        restore_direction(api);
    }

    private void casella_avanti(EV3.Api api) {
        meta_casella_avanti(api);
        meta_casella_avanti(api);
    }
    private void meta_casella_avanti(EV3.Api api) {

        int movimenti = 0;
        int power = 350;
        int acc = 300;
        int adjust_dx = 0;
        int adjust_sx = 0;
        double angle = (double)Math.round(orientation.getRotation()*100)/100;
        my_angle = (double)Math.round(my_angle*100)/100;
        int flag = (mod((angle - my_angle),360) > 180) ? /*destra : sinistra*/ 1 : -1;
        double offset = 0.5;

        while (movimenti < 2) {
            if( (mod(-flag*(angle-my_angle),360) > offset)) {
                if (flag == -1) {
                    adjust_dx = 3 * (int) Math.abs(Math.abs(angle) - Math.abs(my_angle));
                } else {
                    adjust_sx = 3 * (int) Math.abs(Math.abs(angle) - Math.abs(my_angle));
                }
            }

            int finalAdjust_sx = adjust_sx;
            int finalAdjust_dx = adjust_dx;

            gyro_text.setText(String.valueOf(angle));
            applyMotorDx((m) -> {
                m.waitCompletion();
                m.waitUntilReady();

                m.setTimeSpeed(-VELOCITY- finalAdjust_dx , acc,  power, acc, true);
                m.start();
            });

            applyMotorSx((m) -> {
                m.setTimeSpeed(-VELOCITY-finalAdjust_sx , acc, power, acc, true);
                m.start();

                m.waitCompletion();
                m.waitUntilReady();
            });

            movimenti += 1;
            angle = orientation.getRotation();
            flag = (mod((angle - my_angle),360) > 180) ? /*destra : sinistra*/ 1 : -1;
            adjust_dx = 0;
            adjust_sx = 0;
            restore_direction(api);

        }
    }
    /*-----------------------------------FUNZIONI MOTORI----------------------------------------*/


    /*------------------------------------ALGORITMI VARI-----------------------------------------*/
    /* programma effettivo */
    private void complete_task1(EV3.Api api) {
        this.my_angle = orientation.getRotation();

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.i("error", "LA PRIMA SLEEP è STRONZA");
        }

        for(int i = 0; i < 1; ++i) {
            casella_avanti(api);
            casella_avanti(api);
            casella_avanti(api);
            casella_avanti(api);
            resetGiroscope();
        }

        rotazione_plus_90(api);
        rotazione_plus_90(api);
        resetGiroscope();

        for(int i = 0; i < 1; ++i) {
            casella_avanti(api);
            casella_avanti(api);
            casella_avanti(api);
            casella_avanti(api);
            resetGiroscope();
        }

    }

}
