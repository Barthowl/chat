package com.example.chatclient;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    
    private Button btsend , btexit;
    private EditText etText;
    private TextView tvText;
    private String text;
    private String nombre ="";
    private String nomb;

    private boolean run = true;
    private Thread listeningThread;
    private Socket cliente;
    private DataInputStream flujoE;
    private DataOutputStream flujoS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    public String setNombre(String nombre){
        this.nombre = nombre;
        return nombre;
    }

    private void init() {
        btsend = findViewById(R.id.btenviar);
        btexit = findViewById(R.id.btsalir);
        btexit.setEnabled(false);

        etText = findViewById(R.id.et);
        tvText = findViewById(R.id.tvtext);

        btexit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    flujoE.close();
                    flujoS.close();
                    cliente.close();
                    finish();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        btsend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                text = etText.getText().toString();
                new Thread(){
                    @Override
                    public void run() {
                            if (nombre.equals("")) {
                                setNombre(text);
                                nomb = text;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        btexit.setEnabled(true);
                                    }
                                });
                            }
                            sendText(text);
                    }
                }.start();
                etText.setText("");
            }
        });
        Thread thread = new Thread(){
            @Override
            public void run() {
                startClient("10.0.2.2",5000);
            }
        };
        thread.start();
    }


    private void sendText(String text){
        try {
            flujoS.writeUTF(text);
            flujoS.flush();
        } catch (IOException ex) {
            System.out.println("btsend: " + ex.getLocalizedMessage());
            run = false;
        }
    }

    public void startClient(String host, int port){
        try {
            cliente = new Socket(host,port);
            flujoE = new DataInputStream(cliente.getInputStream());
            flujoS = new DataOutputStream(cliente.getOutputStream());
            listeningThread = new Thread() {
                @Override
                public void run(){
                    while(run){
                        try {
                            text = flujoE.readUTF();
                            tvText.post(new Runnable() {
                                @Override
                                public void run() {
                                 tvText.append(text + "\n");
                                }
                            });
                        } catch (IOException ex) {
                            System.out.println("run: " + ex.getLocalizedMessage());
                        }
                    }
                }
            };
            listeningThread.start();
        } catch (IOException ex) {
            System.out.println("start client: " + ex.getMessage());
            finish();
        }
    }
}