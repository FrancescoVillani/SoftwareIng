package com.example.softwareing;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

public class Communication extends AppCompatActivity{

    //Contiene l'id dell'endpoint a cui si vuole inviare il Payload
    private String ep;
    //private String newName;
    //private String selectedDevice;
    //private Coordinata coordinate = new Coordinata(-1,-1);
    private static final Strategy STRATEGY = Strategy.P2P_STAR;
    private static final String SERVICE_ID = /*"com.example.ingegneriadelsoftware";*/ "it.unive.dais.nearby.apps.SERVICE_ID";
    private static final String NOME_GRUPPO = "Legolas Mindstorm";
    //private Payload msg = Payload.fromBytes("Coordinate obiettivo:12;110;".getBytes());
    /*Handler for Nearby Connections*/
    private ConnectionsClient connectionsClient;
    /*Nome del dispositivo*/
    private String codeName = "";
    /** The devices we've discovered near us. */
    private final Map<String, Endpoint> discoveredEndpoints = new HashMap<>();
    //???? capacità massima??? cosa mettere??
    private Queue<Coordinata> coord_list = new ArrayBlockingQueue<>(100);

    private static final String[] REQUIRED_PERMISSIONS =
            new String[] {
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
            };

    private static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 1;

    /*Listener invoked during endpoint discovery. */
    private EndpointDiscoveryCallback endpointDiscoveryCallback = new EndpointDiscoveryCallback() {
        @Override
        public void onEndpointFound(@NonNull String endpointId, @NonNull DiscoveredEndpointInfo discoveredEndpointInfo) {
            if (getServiceId().equals(discoveredEndpointInfo.getServiceId())) {
                Endpoint endpoint = new Endpoint(endpointId, discoveredEndpointInfo.getEndpointName());
                discoveredEndpoints.put(endpointId, endpoint);
                tv.append("Individuato dispostivo: ID = " + endpoint.getId() + " Nome endpoint = " + endpoint.getName() + "\n");
                /**Stampa il numero di elementi presenti nella lista*/
                /*Integer numElem = discoveredEndpoints.size();
                tv.append(numElem.toString());*/
                onEndpointDiscovered(endpoint);
            }
        }
        /*Servirebbe quando un endpoint prima viene visto e poi non viene più visto. Possiamo tranquillamente lavarcene le mani :)*/
        @Override
        public void onEndpointLost(@NonNull String s) {}
    };

    /*Callbacks for finding other devices*/
    private final ConnectionLifecycleCallback connectionLifecycleCallback = new ConnectionLifecycleCallback() {
        /*A basic encrypted channel has been created between you and the endpoint.*/
        @Override
        public void onConnectionInitiated(@NonNull String endpointId, @NonNull ConnectionInfo connectionInfo) {
            //Log.i(TAG, "onConnectionInitiated: accepting connection");
            connectionsClient.acceptConnection(endpointId, payloadCallback);
        }

        /*Questa viene chiamata dopo che le due parti hanno accettato o rifiutato la connessione
         * i log ci aiuteranno a capire (si spera)*/
        @Override
        public void onConnectionResult(@NonNull String endpointId, @NonNull ConnectionResolution connectionResolution) {
            if (connectionResolution.getStatus().isSuccess()) {
                tv.setText("");
                connectionsClient.sendPayload(endpointId, Payload.fromBytes(("Benvenuto sono"+NOME_GRUPPO).getBytes()));
                ep=endpointId; //Salva l'id dell'altro EndPoint, necessario per l'invio dei Payloads
            } else {
                tv.append("\nConnessione fallita\n");
            }
        }

        /*Chiamata quando due dispositivi si disconnettono*/
        @Override
        public void onDisconnected(@NonNull String endpointId) {
            //Log.i(TAG, "onDisconnected: disconnesso");
            tv.append("\nDisconnessione\n");
        }
    };

    /*Listener for incoming/outgoing Payloads between connected endpoints.*/
    private final PayloadCallback payloadCallback = new PayloadCallback() {
        /*Cosa fare quando riceviamo un payload da un endpoint.
        In teoria convertiamo da byte in stringa, elaboriamo, mandiamo una risposta*/
        @Override
        public void onPayloadReceived(@NonNull String s, @NonNull Payload payload) {
            String msg = new String(payload.asBytes());
            if(msg.contains("STOP")){

            }else if(msg.contains("RESUME")){
                msg.split("RESUME");
                tv.append(msg);
            }
            else if(msg.contains("Coordinate")){
                convert2int(msg);
                //tv.append("Coordinate:"+coordinate.getX()+","+coordinate.getY());
            }
            /*tv.append("\nRicevuto messaggio\n");
            tv.append("Il dispositivo" + s + "mi ha inviato il messaggio:" + msg + "\n");*/
            //convert2int(msg);
            //tv.append("Coordinate:"+coordinate.getX()+","+coordinate.getY());
        }

        /*Chiamato con informazioni su un trasferimento di payload che sta avvenendo
        in questo istante sia in entrata che in uscita*/
        @Override
        public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {
            /*int status=payloadTransferUpdate.getStatus();
            tv.append("\n"+int2status(status)+"\n");*/

        }
    };

    private Button advertising;
    private Button discovery;
    private Button endAdvertising;
    private Button endDiscovery;
    private Button endAll;
    private TextView tv;
    private Button sender;
    private Button sendName;
    private Button sendDevice;
    private EditText changeName;
    private EditText connectionId;
    private Button stampaLista;


    /**PARTE INERENTE ALLA RICHIESTA DEI PERMESSI*/
    @Override
    protected void onStart() {
        super.onStart();

        if (!hasPermissions(this, REQUIRED_PERMISSIONS)) {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_REQUIRED_PERMISSIONS);
        }
    }

    /** Returns true if the app was granted all the permissions. Otherwise, returns false.*/
    public static boolean hasPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /** Handles user acceptance (or denial) of our permission request.*/
    @CallSuper
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != REQUEST_CODE_REQUIRED_PERMISSIONS) {
            return;
        }

        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, R.string.error_missing_permissions, Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }
        recreate();
    }

    /** Accepts a connection request. */
    /**In teoria la facciamo già sopra nella onConnectionInitiated dentro connectionLifecycleCallback*/
    /*protected void acceptConnection(final Endpoint endpoint) {
        connectionsClient
                .acceptConnection(endpoint.getId(), payloadCallback)
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.W("acceptConnection() failed.", e);
                            }
                        });
    }*/

    /** Rejects a connection request. Forse non serve a nulla*/
    protected void rejectConnection(Endpoint endpoint) {
        connectionsClient.rejectConnection(endpoint.getId());
    }

    /* ELIMINARE
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communication);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        advertising = findViewById(R.id.advertising);
        discovery = findViewById(R.id.discovery);
        endAdvertising = findViewById(R.id.stopAd);
        endDiscovery = findViewById(R.id.stopDi);
        endAll = findViewById(R.id.stopAll);
        tv = findViewById(R.id.textView);
        tv.setMovementMethod(new ScrollingMovementMethod());
        sender = findViewById(R.id.sender);
        sendName = findViewById(R.id.submitName);
        sendDevice = findViewById(R.id.submitDevice);
        changeName = findViewById(R.id.setName);
        connectionId = findViewById(R.id.selectId);
        stampaLista = findViewById(R.id.printdevice);


        connectionsClient = Nearby.getConnectionsClient(this);

        advertising.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv.setText("");
                startAdvertising();

                /*onAdvertisingStarted();
            }
        });

        discovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv.setText("");
                startDiscovery();
                /*onDiscoveryStarted();

            }
        });

        endAdvertising.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAdvertising();
                tv.setText("");
            }
        });

        endDiscovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopDiscovering();
                tv.setText("");
            }
        });

        endAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAllConnections();
                tv.setText("");
            }
        });

        sender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectionsClient.sendPayload(ep, msg);
                tv.append("\nInvio messaggio in corso....\n");
            }
        });

        sendName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newName = changeName.getText().toString();
                setName(newName);
            }
        });

        sendDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedDevice = connectionId.getText().toString();
                tv.append("Hai selezionato il dispositivo " + selectedDevice + " provo a connettermi\n");
            }
        });

        Qui ci va il fottutissimo foreach che tanto è sbagliato
        stampaLista.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv.append("\n\n ****************** \n");
                if (discoveredEndpoints.containsKey(selectedDevice)) {
                    tv.append("Dispositivo trovato\n");
                    Endpoint e = discoveredEndpoints.get(selectedDevice);

                    connectionsClient.requestConnection(getName(), e.getId(), connectionLifecycleCallback);
                }
                else {
                    tv.append("Dispositivo non trovato. Id sbagliato\n");
                }
                String msg = "Coordinate obiettivo:12;34;";
                eliminami(msg);
                stampaCoord();
                //tv.append("Coordinate: "+coordinate.getX()+","+coordinate.getY());
            }
        });
    }*/

    /** Starts looking for other players using Nearby Connections. */
    public void startDiscovery() {
        //Potrebbero venire sollevate eccezioni, se vogliamo controllarle (penso sia consigliato
        //onde evitare casini in fase di esecuzione) fare riferimento all'esempio del walkie tolkie
        tv.setText("");
        tv.append("Sto cominciando la fase di discovery \n");
        connectionsClient.startDiscovery(
                getPackageName(), endpointDiscoveryCallback,
                new DiscoveryOptions.Builder().setStrategy(STRATEGY).build());
    }

    /** Broadcasts our presence using Nearby Connections so other players can find us. */
    public void startAdvertising() {
        //Potrebbero venire sollevate eccezioni, se vogliamo controllarle (penso sia consigliato
        //onde evitare casini in fase di esecuzione) fare riferimento all'esempio del walkie tolkie
        tv.setText("");
        tv.append("Sto cominciando la fase di advertising \n");
        connectionsClient.startAdvertising(
                codeName, getPackageName(), connectionLifecycleCallback,
                new AdvertisingOptions.Builder().setStrategy(STRATEGY).build());

    }

    /*private void eliminami(String s){
        if(s.contains("STOP")){

        }else if(s.contains("RESUME")){
            String[] str=s.split("RESUME");
            tv.append(str[0]);
        }
        else if(s.contains("Coordinate")){
            convert2int(s);
            tv.append("Coordinate:"+coordinate.getX()+","+coordinate.getY());
        }
    }*/

    /**Termina la fase di discovery*/
    private void stopDiscovering() {
        //Potrebbero venire sollevate eccezioni, se vogliamo controllarle (penso sia consigliato
        //onde evitare casini in fase di esecuzione) fare riferimento all'esempio del walkie tolkie
        connectionsClient.stopDiscovery();
    }

    /**Termina la fase di advertising*/
    private void stopAdvertising() {
        //Potrebbero venire sollevate eccezioni, se vogliamo controllarle (penso sia consigliato
        //onde evitare casini in fase di esecuzione) fare riferimento all'esempio del walkie tolkie
        connectionsClient.stopAdvertising();
    }

    /**Termina tutte le connessioni attive*/
    private void stopAllConnections() {
        tv.setText("");
        connectionsClient.stopAllEndpoints();
        discoveredEndpoints.clear();
    }

    protected String getServiceId() {
        return SERVICE_ID;
    }

    protected Strategy getStrategy() {
        return STRATEGY;
    }

    private void setName(String s) {
        codeName = s;
    }

    private String getName() {
        return codeName;
    }

    private void convert2int(String s) {
        String[] splitArray = s.split(":");
        String[] coord = splitArray[1].split(";");
        Coordinata c= new Coordinata(Integer.parseInt(coord[0]), Integer.parseInt(coord[1]));
        /*old
        coordinate.setX(Integer.parseInt(coord[0]));
        coordinate.setY(Integer.parseInt(coord[1]));*/
        coord_list.offer(c);

        /*char[] charArray = s.toCharArray();
        String x = new String("");
        String y = new String("");
        int i=0;
        while(charArray[i]!=':') {
            i++;
        }
        i++;
        if(charArray[i]>='0' && charArray[i]<='9'){
            while(charArray[i]!=';'){
                Character c = charArray[i];
                tv.append(c.toString());
                x=x.concat(c.toString());
                i++;
            }
            i++;
            while(charArray[i]!=';'){
                Character c = charArray[i];
                tv.append(c.toString());
                y=y.concat(c.toString());
                i++;
            }
        }
        else{
            tv.append("Sono nell'else \n");
        }
        tv.append("Coordinata X: "+x+"\n");
        tv.append("Coordinata Y: "+y+"\n");
        coordinate.setX(Integer.valueOf(x));
        coordinate.setY(Integer.valueOf(y));*/
    }

    //PROVA
    protected void stampaCoord(){
        while(coord_list.size()!=0){
            Coordinata k = coord_list.poll();
            tv.append("x: "+k.getX()+" y: "+k.getY()+"\n");
        }

    }

    protected void onEndpointDiscovered(Endpoint endpoint) {
        tv.setText("");
        tv.append("Provo a connettermi all'endpoint con Id: " + endpoint.getId() + " e nome: " + endpoint.getName() + "\n");
        connectionsClient.requestConnection(getName(), endpoint.getId(), connectionLifecycleCallback);
    }
    protected void onConnectionFailed(Endpoint endpoint) {
        tv.setText("");
        tv.append("LA CONNESSIONE E' FALLITA!!!! \n");
    }

    protected void onEndpointDisconnected(Endpoint endpoint) {
        tv.setText("");
        tv.append("Ti sei disconnesso da: " + endpoint.getId() + " " + endpoint.getName() + "\n");
    }

    protected void onEndpointConnected(Endpoint endpoint) {
        tv.setText("");
        tv.append("Ti sei connesso a: " + endpoint.getId() + " " + endpoint.getName() + "\n");
    }

    protected void onDiscoveryStarted() {
        tv.setText("");
        tv.append("Sto cominciando la fase di discovery \n");
    }

    protected void onDiscoveryFailed() {
        tv.setText("");
        tv.append("Fase di discovery fallita \n");
    }

    protected void onAdvertisingStarted() {
        tv.setText("");
        tv.append("Sto cominciando la fase di advertising \n");
    }

    protected void onAdvertisingFailed() {
        tv.setText("");
        tv.append("Fase di advertising fallita \n");
    }

    protected static class Coordinata{
        private int x;
        private int y;

        public Coordinata(int x, int y){
            this.x=x;
            this.y=y;
        }

        public int getX(){
            return x;
        }

        public int getY(){
            return y;
        }

        public void setX(int x){
            this.x=x;
        }

        public void setY(int y){
            this.y=y;
        }

    }

    /** Represents a device we can talk to. */
    protected static class Endpoint {
        @NonNull private final String id;
        @NonNull private final String name;

        private Endpoint(@NonNull String id, @NonNull String name) {
            this.id = id;
            this.name = name;
        }

        @NonNull
        public String getId() {
            return id;
        }

        @NonNull
        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Endpoint) {
                Endpoint other = (Endpoint) obj;
                return id.equals(other.getId());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

        @Override
        public String toString() {
            return String.format("Endpoint{id=%s, name=%s}", id, name);
        }
    }

    //Codifica il codice dello stato nella corrispondente stringa, per aiutare nel debug
    /*private String int2status(int n){
        switch(n){
            case 1:
                return "Success";
                break;
            case 2:
                return "Failure";
                break;
            case 3:
                return "In Progress";
                break;
            case 4:
                return "Canceled";
            break;
        }
    }*/
}
