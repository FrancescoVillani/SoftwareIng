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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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

        //log.append("\n---resetting gyroscope :D---");
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.i("error", "LA PRIMA SLEEP è STRONZA");
        }
        this.my_angle = orientation.getRotation();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.i("error", "LA PRIMA SLEEP è STRONZA");
        }
    }

    /* Ritorna la distanza letta dal proximity sensor*/
    private float read_proximity_sensor () {

        /* interroga sensore di prossimità */
        Future<Float> future_distance = null;
        Float distance = (float) 0;
        try {

            future_distance = proximity_sensor.getDistance();
            distance = future_distance.get();

        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        /* se distance <= 5 allora la pallina si puo raccogliere */
        return distance;
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
        double offset = 0.5;
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
        resetGiroscope();
        this.my_angle = (this.my_angle + 90) % 360;
        //log.append("\ngiro a destra verso l'angolo:" + this.my_angle);
        this.orientamento = (int) mod(this.orientamento+1, 4);

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
        resetGiroscope();
        this.my_angle = mod((this.my_angle - 90),360);
        //log.append("\ngiro a sinistra verso l'angolo:" + this.my_angle);
        this.orientamento = (int) mod(this.orientamento-1, 4);

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
        int power = 360;
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
    int orientamento;
    int contatore;

    public void logCampo(){
        String string = "";
        for(int i = 0; i < ROW; i++){
            for(int j = 0; j < COL; j++){
                string += campo[i][j] + "   ";
            }
            Log.i("mytag",string);
            string = "";
        }
    }

    public void setCampo() {
        /*setto il contatore*/
        contatore = 1;
        /* setto l'orientamento */
        if(my_row == 0) //Watching SOUTH
            this.orientamento = 2;
        else if (my_row == this.ROW -1)//Watching NORTH
            this.orientamento = 0;
        else if (my_col == 0)//Watching EST
            this.orientamento = 1;
        else//Watching WEST
            this.orientamento = 3;

        //se sto guardando verso sud
        if(this.orientamento == 2){
            for(int i = this.my_col; i < this.COL; ++i)
                campo[0][i] = contatore++;
            for(int i = this.my_col-1; i >= 0; --i)
                campo[0][i] = contatore++;

            for(int i = 1; i < this.ROW; ++i){
                /*pari verso sinistra e dispari verso destra*/
                if(i % 2 == 0){
                    for(int j = this.COL -1; j >= 0; --j)
                        campo[i][j] = contatore++;
                }
                else{
                    for(int j = 0; j < this.COL; ++j)
                        campo[i][j] = contatore++;
                }
            }
        }

        //se sto guardando verso est
        else if(this.orientamento == 1) {
            for (int i = this.my_row; i >= 0; --i)
                campo[i][0] = contatore++;
            for (int i = this.my_row+1; i < this.ROW; ++i)
                campo[i][0] = contatore++;
            for (int i = this.my_col + 1; i < this.COL; ++i) {
                /*dispari verso su e pari verso giù*/
                if (i % 2 != 0) {
                    for (int j = this.ROW - 1; j >= 0; --j)
                        campo[j][i] = contatore++;
                } else {
                    for (int j = 0; j < this.ROW; ++j)
                        campo[j][i] = contatore++;
                }
            }
        }

        //se sto guardando verso nord
        else if(this.orientamento == 0){
            for(int i = this.my_col; i >= 0; --i)
                campo[this.my_row][0] = contatore++;
            for(int i = this.my_col+1; i < this.COL; ++i)
                campo[this.my_row][0] = contatore++;
            for(int i = this.my_row -1; i >= 0; --i){
                /*pari verso destra e dispari verso sinistra, però devo stare attento al numero di righe che ho*/
                if((i+this.ROW) % 2 == 0){
                    for(int j = this.COL -1; j >= 0; --j)
                        campo[i][j] = contatore++;
                }
                else{
                    for(int j = 0; j < this.COL; ++j)
                        campo[i][j] = contatore++;
                }
            }
        }

        //se sto guardando verso ovest
        else {
            for (int i = this.my_row; i < this.ROW; ++i)
                campo[i][this.my_col] = contatore++;
            for(int i = this.my_row-1; i >= 0; --i)
                campo[i][this.my_col] = contatore++;
            for (int i = this.my_col - 1; i >= 0; --i) {
                /*pari verso su e dispari verso giù, però devo stare attento al numero di righe che ho*/
                if ((i+this.COL) % 2 != 0) {
                    for (int j = this.ROW - 1; j >= 0; --j)
                        campo[j][i] = contatore++;
                } else {
                    for (int j = 0; j < this.ROW; ++j)
                        campo[j][i] = contatore++;
                }
            }
        }
    }

    /* Capisce com'è in base alla destinazione e ritorna la mossa da fare*/
    private int capisci_mossa(int row_dest, int col_dest) {
        int mov;
        if(row_dest == my_row && col_dest == my_col){
            mov = -1;

            if((read_proximity_sensor() <= 7 || read_proximity_sensor() >= 80)&& azione == 1){
                azione = -1;
                return -1;
            }

            azione = (azione + 1) % 3;
            log.append("\nla mossa da fare è: " + mov + " ");
            return mov;
        }
        if (row_dest < this.my_row) {
            mov = (int) mod(0 - this.orientamento, 4); // row_dest e' a NORD
            log.append("\nla mossa da fare è: " + mov + " ");
            return mov;
        }
        else if (row_dest > this.my_row) {
            mov = (int) mod( 2 - this.orientamento ,4); // row_dest e' a SUD
            log.append("\nla mossa da fare è: " + mov + " ");
            return mov;
        }
        else if (col_dest > this.my_col) {
            mov = (int) mod(1 - this.orientamento,4); // col_dest e' a EST
            log.append("\nla mossa da fare è: " + mov + " ");
            return mov;
        }
        else{
            mov = (int) mod(3 - this.orientamento,4); // col_dest e' a OVEST
            log.append("\nla mossa da fare è: " + mov + " ");
            return mov;
        }
    }

    private void registra_movimento(){
        if (orientamento == 0)
            my_row--;
        else if(orientamento == 2)
            my_row++;
        else if(orientamento == 1)
            my_col++;
        else
            my_col--;
        log.append("\nsono in posizione: " + my_row + " " + my_col);

    }

    private void esegui_mossa(EV3.Api api, int mossa){
        if(mossa==-1){}
        else if(mossa == 0){
            casella_avanti(api);
            registra_movimento();
        }
        else if(mossa == 1){
            rotazione_plus_90(api);
            casella_avanti(api);
            registra_movimento();
        }
        else if(mossa == 2){
            rotazione_plus_90(api);
            rotazione_plus_90(api);
            casella_avanti(api);
            registra_movimento();
        }
        else{
            rotazione_minus_90(api);
            casella_avanti(api);
            registra_movimento();
        }
    }

    private int algoritmo_zigzag(){

        if(read_proximity_sensor() <= 7){

            this.ultima_bomba = this.campo[this.my_row][this.my_col];

            /* usa il motore di raccolta */
            applyMotorFunctional((m) -> {
                m.waitCompletion();
                m.waitUntilReady();

                m.setTimeSpeed(100,200,500,200, true);
                m.start();

                m.waitCompletion();
                m.waitUntilReady();
            });

            return capisci_mossa(my_row, my_col);
        }

        int next_row = this.my_row, next_col = this.my_col;

        int startPosX = (this.my_row - 1 < 0) ? this.my_row : this.my_row-1;
        if(campo[startPosX][my_col] == highest_visited+1){
            next_row = startPosX;
            next_col = my_col;
        }
        int startPosY = (this.my_col - 1 < 0) ? this.my_col : this.my_col-1;
        if(campo[my_row][startPosY]  == highest_visited+1){
            next_row = my_row;
            next_col = startPosY;
        }
        int endPosX =   (this.my_row + 1 > this.ROW-1) ? this.my_row : this.my_row+1;
        if(campo[endPosX][my_col]  == highest_visited+1){
            next_row = endPosX;
            next_col = my_col;
        }
        int endPosY =   (this.my_col + 1 > this.COL-1) ? this.my_col : this.my_col+1;
        if(campo[my_row][endPosY]  == highest_visited+1){
            next_row = my_row;
            next_col = endPosY;
        }
        highest_visited++;
        log.append("\nhighest visited is now: " + highest_visited);
        return capisci_mossa(next_row, next_col);
    }

    private int algoritmo_ritorno_inizio(){

        int min_row = this.my_row, min_col = this.my_col;

        int startPosX = (this.my_row - 1 < 0) ? this.my_row : this.my_row-1;
        if(campo[startPosX][my_col] < campo[min_row][min_col]){
            min_row = startPosX;
            min_col = my_col;
        }
        int startPosY = (this.my_col - 1 < 0) ? this.my_col : this.my_col-1;
        if(campo[my_row][startPosY] < campo[min_row][min_col]){
            min_row = my_row;
            min_col = startPosY;
        }
        int endPosX =   (this.my_row + 1 > this.ROW-1) ? this.my_row : this.my_row+1;
        if(campo[endPosX][my_col] < campo[min_row][min_col]){
            min_row = endPosX;
            min_col = my_col;
        }
        int endPosY =   (this.my_col + 1 > this.COL-1) ? this.my_col : this.my_col+1;
        if(campo[my_row][endPosY] < campo[min_row][min_col]){
            min_row = my_row;
            min_col = endPosY;
        }

        return capisci_mossa(min_row, min_col);
    }

    private int  algoritmo_ritorno_casella(){
        if (campo[my_row][my_col] == ultima_bomba){
            return capisci_mossa(my_row, my_col);
        }
        else{
            int max_row = this.my_row, max_col = this.my_col;

            int startPosX = (this.my_row - 1 < 0) ? this.my_row : this.my_row-1;
            if(campo[startPosX][my_col] > campo[max_row][max_col] && campo[startPosX][my_col] <= highest_visited){
                max_row = startPosX;
                max_col = my_col;
            }
            int startPosY = (this.my_col - 1 < 0) ? this.my_col : this.my_col-1;
            if(campo[my_row][startPosY] > campo[max_row][max_col] && campo[my_row][startPosY] <= highest_visited){
                max_row = my_row;
                max_col = startPosY;
            }
            int endPosX =   (this.my_row + 1 > this.ROW-1) ? this.my_row : this.my_row+1;
            if(campo[endPosX][my_col] > campo[max_row][max_col] && campo[endPosX][my_col] <= highest_visited){
                max_row = endPosX;
                max_col = my_col;
            }
            int endPosY =   (this.my_col + 1 > this.COL-1) ? this.my_col : this.my_col+1;
            if(campo[my_row][endPosY] > campo[max_row][max_col] && campo[my_row][endPosY] <= highest_visited){
                max_row = my_row;
                max_col = endPosY;
            }
            return capisci_mossa(max_row, max_col);
        }
    }


    /*------------------------------------ALGORITMI VARI-----------------------------------------*/

    int azione = 0;
    int highest_visited = 1;
    int ultima_bomba = 1;
    /* programma effettivo */
    private void complete_task1(EV3.Api api) {
        setCampo();
        resetGiroscope();

        /*apri la pinza*/
        applyMotorFunctional((m) -> {
            m.waitCompletion();
            m.waitUntilReady();

            m.setTimeSpeed(-100,200,500,200, true);
            m.start();

            m.waitCompletion();
            m.waitUntilReady();
        });


        while(tot_balls > 0){
            if(azione == -1){
                //rilascia_bomba();
            }
            if(azione == 0){
                esegui_mossa(api, algoritmo_zigzag());
            }
            else if(azione == 1){
                esegui_mossa(api, algoritmo_ritorno_inizio());
            }
            else{
                algoritmo_ritorno_casella();
            }
        }

        log.append("FINITO!");
    }
}
