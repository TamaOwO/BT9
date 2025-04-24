package com.example.bt9.BT2.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bt9.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class LightControlClientActivity extends AppCompatActivity {
    private EditText etServerIp, etServerPort;
    private Button btnConnect, btnTurnOn, btnTurnOff, btnCheckStatus;
    private TextView tvConnectionStatus, tvLightStatus;
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_control_client);

        // Initialize UI components
        etServerIp = findViewById(R.id.etServerIp);
        etServerPort = findViewById(R.id.etServerPort);
        btnConnect = findViewById(R.id.btnConnect);
        btnTurnOn = findViewById(R.id.btnTurnOn);
        btnTurnOff = findViewById(R.id.btnTurnOff);
        btnCheckStatus = findViewById(R.id.btnCheckStatus);
        tvConnectionStatus = findViewById(R.id.tvConnectionStatus);
        tvLightStatus = findViewById(R.id.tvLightStatus);

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isConnected) {
                    connectToServer();
                } else {
                    disconnectFromServer();
                }
            }
        });

        btnTurnOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("ON");
            }
        });

        btnTurnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("OFF");
            }
        });

        btnCheckStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLightStatus();
            }
        });
    }

    private void connectToServer() {
        final String serverIp = etServerIp.getText().toString().trim();
        final int serverPort;

        try {
            serverPort = Integer.parseInt(etServerPort.getText().toString().trim());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid port number", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket(serverIp, serverPort);
                    writer = new PrintWriter(socket.getOutputStream(), true);
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    isConnected = true;
                    updateConnectionStatus("Connected to " + serverIp + ":" + serverPort);
                    enableControls(true);
                } catch (IOException e) {
                    e.printStackTrace();
                    updateConnectionStatus("Connection failed: " + e.getMessage());
                    enableControls(false);
                }
            }
        }).start();
    }

    private void disconnectFromServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                    }
                    isConnected = false;
                    updateConnectionStatus("Disconnected");
                    enableControls(false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void sendCommand(final String command) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (writer != null) {
                    writer.println(command);

                    // Update light status in UI
                    if (command.equals("ON") || command.equals("OFF")) {
                        updateLightStatus(command);
                    }
                }
            }
        }).start();
    }

    private void checkLightStatus() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (writer != null && reader != null) {
                        writer.println("STATUS");
                        String response = reader.readLine();
                        if (response != null) {
                            updateLightStatus(response);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    updateConnectionStatus("Error reading from server: " + e.getMessage());
                }
            }
        }).start();
    }

    private void updateConnectionStatus(final String status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvConnectionStatus.setText("Connection Status: " + status);
                btnConnect.setText(isConnected ? "Disconnect" : "Connect to Server");
            }
        });
    }

    private void updateLightStatus(final String status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvLightStatus.setText("Light Status: " + status);
            }
        });
    }

    private void enableControls(final boolean enable) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnTurnOn.setEnabled(enable);
                btnTurnOff.setEnabled(enable);
                btnCheckStatus.setEnabled(enable);
                etServerIp.setEnabled(!enable);
                etServerPort.setEnabled(!enable);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnectFromServer();
    }
}
