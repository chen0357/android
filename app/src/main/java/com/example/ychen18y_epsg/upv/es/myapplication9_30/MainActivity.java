package com.example.ychen18y_epsg.upv.es.myapplication9_30;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.content.pm.PackageManager;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;


public class MainActivity extends AppCompatActivity {

    private static final String ETIQUETA_LOG = ">>>>";
    private static final int CODIGO_PETICION_PERMISOS = 11223344;
    private BluetoothLeScanner elEscanner;
    private ScanCallback callbackDelEscaneo = null;
    private Intent elIntentDelServicio = null;
    private TextView elTextoMinor;
    private TextView elTextoMajor;
    private TextView salidaTexto;
    private EditText temperaturaInput;
    private EditText coInput;
    private Button elBotonEnviar;

    private Button elBotonPrueba;

    // _______________________________________________________________
    // Diseño: buscarTodosLosDispositivosBTLE()
    // Descripción:Empieza el scanner y se establece un callback para
    // diferentes caso, si obtiene resultado, lo muestra en logcat
    // _______________________________________________________________
    @SuppressLint("MissingPermission")
    private void buscarTodosLosDispositivosBTLE() {
        this.callbackDelEscaneo = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult resultado) {
                super.onScanResult(callbackType, resultado);
                Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): onScanResult() ");

                mostrarInformacionDispositivoBTLE(resultado);
            }
            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): onBatchScanResults() ");

            }
            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): onScanFailed() ");

            }
        };
        Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): empezamos a escanear ");
        //Empieza el escanner a escanear, si obtiene resultado, llama el callback del escaneo
        this.elEscanner.startScan(this.callbackDelEscaneo);
    }

    // _______________________________________________________________
    // Diseño: ScanResult ---> mostrarInformacionDispositivosBTLE()
    // Descripción: Recibe un objeto de tipo ScanResult y muestra datos
    // en el logcat
    // _______________________________________________________________
    @SuppressLint("MissingPermission")
    private void mostrarInformacionDispositivoBTLE(ScanResult resultado) {
        BluetoothDevice bluetoothDevice = resultado.getDevice();
        byte[] bytes = resultado.getScanRecord().getBytes();
        int rssi = resultado.getRssi();

        Log.d(ETIQUETA_LOG, " ****************************************************");
        Log.d(ETIQUETA_LOG, " ****** DISPOSITIVO DETECTADO BTLE ****************** ");
        Log.d(ETIQUETA_LOG, " ****************************************************");
        Log.d(ETIQUETA_LOG, " nombre = " + bluetoothDevice.getName());
        Log.d(ETIQUETA_LOG, " toString = " + bluetoothDevice.toString());

        Log.d(ETIQUETA_LOG, " dirección = " + bluetoothDevice.getAddress());
        Log.d(ETIQUETA_LOG, " rssi = " + rssi);

        Log.d(ETIQUETA_LOG, " bytes = " + new String(bytes));
        Log.d(ETIQUETA_LOG, " bytes (" + bytes.length + ") = " + Utilidades.bytesToHexString(bytes));

        TramaIBeacon tib = new TramaIBeacon(bytes);

        Log.d(ETIQUETA_LOG, " ----------------------------------------------------");
        Log.d(ETIQUETA_LOG, " prefijo  = " + Utilidades.bytesToHexString(tib.getPrefijo()));
        Log.d(ETIQUETA_LOG, "          advFlags = " + Utilidades.bytesToHexString(tib.getAdvFlags()));
        Log.d(ETIQUETA_LOG, "          advHeader = " + Utilidades.bytesToHexString(tib.getAdvHeader()));
        Log.d(ETIQUETA_LOG, "          companyID = " + Utilidades.bytesToHexString(tib.getCompanyID()));
        Log.d(ETIQUETA_LOG, "          iBeacon type = " + Integer.toHexString(tib.getiBeaconType()));
        Log.d(ETIQUETA_LOG, "          iBeacon length 0x = " + Integer.toHexString(tib.getiBeaconLength()) + " ( "
                + tib.getiBeaconLength() + " ) ");
        Log.d(ETIQUETA_LOG, " uuid  = " + Utilidades.bytesToHexString(tib.getUUID()));
        Log.d(ETIQUETA_LOG, " uuid  = " + Utilidades.bytesToString(tib.getUUID()));

        // Extracting major and minor values
        int major = Utilidades.bytesToInt(tib.getMajor());
        int minor = Utilidades.bytesToInt(tib.getMinor());

        Log.d(ETIQUETA_LOG, " major  = " + Utilidades.bytesToHexString(tib.getMajor()) + "( " + major + " ) ");
        Log.d(ETIQUETA_LOG, " minor  = " + Utilidades.bytesToHexString(tib.getMinor()) + "( " + minor + " ) ");

        // Update the major and minor text views
        runOnUiThread(() -> {
            elTextoMajor.setText("Major: " + major);
            elTextoMinor.setText("Minor: " + minor);
        });

        // Check if the data type is correct for temperature or CO
        int dataType = (major >> 8); // Extracting the high byte from major
        int counter = major & 0xFF;  // Extracting the low byte from major

        if (dataType == 11) {
            // This is CO data
            Log.d(ETIQUETA_LOG, "CO value received: " + minor + ", counter: " + counter);
            runOnUiThread(() -> coInput.setText(String.valueOf(minor))); // Update the CO value on the main thread
        } else if (dataType == 12) {
            // This is temperature data
            Log.d(ETIQUETA_LOG, "Temperature value received: " + minor + ", counter: " + counter);
            runOnUiThread(() -> temperaturaInput.setText(String.valueOf(minor))); // Update the temperature value on the main thread
        }

        Log.d(ETIQUETA_LOG, " txPower  = " + Integer.toHexString(tib.getTxPower()) + " ( " + tib.getTxPower() + " )");
        Log.d(ETIQUETA_LOG, " ****************************************************");
    }


    // _______________________________________________________________
    // Diseño: String --->buscarEsteDispositivoBTLE()
    // Descripción: Recibe el nombre de dispositivo que quiere buscar
    // y se filtra en los resultados
    // _______________________________________________________________
    @SuppressLint("MissingPermission")
    private void buscarEsteDispositivoBTLE(final String dispositivoBuscado) {

        if(this.elEscanner==null){
            Log.d(ETIQUETA_LOG, "buscarEsteDispositivoBTLE: No existe el scanner");
            return;
        }
        this.callbackDelEscaneo = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult resultado) {
                super.onScanResult(callbackType, resultado);
                Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): onScanResult() ");

                mostrarInformacionDispositivoBTLE(resultado);
            }
            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): onBatchScanResults() ");

            }
            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): onScanFailed() ");
            }
        };

        Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): empezamos a escanear buscando: " + dispositivoBuscado);

        //Crea un scanfilter y añade el nombre de dispositivo
        ScanFilter scanFilter = new ScanFilter.Builder()
                .setDeviceName(dispositivoBuscado)
                .build();

        //Configurar el setting del scanner
        ScanSettings scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        //Empieza el escaneo con el filtro
        this.elEscanner.startScan(Collections.singletonList(scanFilter), scanSettings, this.callbackDelEscaneo);

    }

    // _______________________________________________________________
    // Diseño: detenerBusquedaDispositivosBTLE()
    // Descripción: Parar la busqueda
    // _______________________________________________________________
    @SuppressLint("MissingPermission")
    private void detenerBusquedaDispositivosBTLE() {

        //Parar el escanner y anular el callback del escaneo
        if (this.callbackDelEscaneo == null) {
            return;
        }else {

            this.elEscanner.stopScan(this.callbackDelEscaneo);
            this.callbackDelEscaneo = null;
        }

    }

    // --------------------------BOTON--------------------------------

    public void botonBuscarDispositivosBTLEPulsado(View v) {

        Log.d(ETIQUETA_LOG, " boton arrancar servicio Pulsado" );

        if ( this.elIntentDelServicio != null ) {
            // ya estaba arrancado
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            return;
        }

        Log.d(ETIQUETA_LOG, " MainActivity.constructor : voy a arrancar el servicio");
        this.elIntentDelServicio = new Intent(this, ServicioEscuharBeacons.class);
        this.elIntentDelServicio.putExtra("tiempoDeEspera", (long) 5000);
        startService( this.elIntentDelServicio );

        Log.d(ETIQUETA_LOG, " boton buscar dispositivos BTLE Pulsado");
        this.buscarTodosLosDispositivosBTLE();
    }

    // _______________________________________________________________
    public void botonBuscarNuestroDispositivoBTLEPulsado(View v) {
        Log.d(ETIQUETA_LOG, " boton nuestro dispositivo BTLE Pulsado");
        //this.buscarEsteDispositivoBTLE( Utilidades.stringToUUID( "EPSG-GTI-PROY-3A" ) );

        if ( this.elIntentDelServicio != null ) {
            // ya estaba arrancado
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            return;
        }

        Log.d(ETIQUETA_LOG, " MainActivity.constructor : voy a arrancar el servicio");
        this.elIntentDelServicio = new Intent(this, ServicioEscuharBeacons.class);
        this.elIntentDelServicio.putExtra("tiempoDeEspera", (long) 5000);
        startService( this.elIntentDelServicio );

        Log.d(ETIQUETA_LOG, " iniciamos la buscaqueda epsg-gti" );
        this.buscarEsteDispositivoBTLE( "GTI-3A_CHEN" );
        //this.buscarEsteDispositivoBTLE("fistro");

    }

    // _______________________________________________________________
    public void botonDetenerBusquedaDispositivosBTLEPulsado(View v) {

        if ( this.elIntentDelServicio == null ) {
            // no estaba arrancado
            return;
        }

        stopService( this.elIntentDelServicio );
        Log.d(ETIQUETA_LOG, " boton detener servicio Pulsado" );

        this.elIntentDelServicio = null;
        Log.d(ETIQUETA_LOG, " boton detener busqueda dispositivos BTLE Pulsado");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            return;
        }
        this.detenerBusquedaDispositivosBTLE();
    }

    // _______________________________________________________________
    // Diseño: inicializarBlueTooth()
    // Descripción: Pedir los permisos de BLUETOOTH y lo enciende
    // _______________________________________________________________
    @SuppressLint("MissingPermission")
    private void inicializarBlueTooth() {
        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): obtenemos adaptador BT ");
        //Coger el bluetooth manager del servicio BLUETOOTH_SERVICE
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): habilitamos adaptador BT ");
        //Coger el adapter desde el manager del bluetooth y lo enciende
        BluetoothAdapter bta = bluetoothManager.getAdapter();
        bta.enable();

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): habilitado =  " + bta.isEnabled() );
        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): estado =  " + bta.getState() );

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): obtenemos escaner btle ");
        this.elEscanner = bta.getBluetoothLeScanner();

        if ( this.elEscanner == null ) {
            Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): Socorro: NO hemos obtenido escaner btle  !!!!");
            return;
        }

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): voy a perdir permisos (si no los tuviera) !!!!");
        //Check si tengo todos los permisos necesarios, si no los tengo, pido todas las permisiones necesarias
        if (
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED
        )
        {
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{Manifest.permission.BLUETOOTH,Manifest.permission.BLUETOOTH_ADMIN,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.BLUETOOTH_CONNECT,Manifest.permission.BLUETOOTH_SCAN,Manifest.permission.INTERNET},
                    CODIGO_PETICION_PERMISOS);
        }
        else {
            Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): parece que YA tengo los permisos necesarios !!!!");
        }
    }

    // _______________________________________________________________
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button buttonEnviar = findViewById(R.id.elBotonEnviar);
        Button buttonPrueba = findViewById(R.id.elBotonPrueba);

        // 设置点击事件监听器
        buttonEnviar.setOnClickListener(this::boton_enviar_pulsado_client);
        buttonPrueba.setOnClickListener(this::boton_prueba_pulsado);
//        this.elBotonEnviar =(Button) findViewById(R.id.elBotonEnviar);
//        this.elBotonPrueba =(Button) findViewById(R.id.elBotonPrueba);
        this.elTextoMinor = (TextView) findViewById(R.id.elTextoMinor);
        this.elTextoMajor =(TextView) findViewById(R.id.elTextoMajor);
//        this.salidaTexto = (TextView) findViewById(R.id.salidaTexto);

        this.temperaturaInput = (EditText) findViewById(R.id.temperaturaInput);
        this.coInput = (EditText) findViewById(R.id.coInput);

        ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            return;
        }else {

            Toast.makeText(getApplicationContext(),"GPS Activado",Toast.LENGTH_SHORT).show();
        }

        Log.d(ETIQUETA_LOG, " onCreate(): empieza ");

        inicializarBlueTooth();

        Log.d(ETIQUETA_LOG, " onCreate(): termina ");

        Log.d("clienterestandroid", "fin onCreate()");
    }

    // _______________________________________________________________
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult( requestCode, permissions, grantResults);

        switch (requestCode) {
            case CODIGO_PETICION_PERMISOS:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.d(ETIQUETA_LOG, " onRequestPermissionResult(): permisos concedidos  !!!!");
                    // Permission is granted. Continue the action or workflow
                    // in your app.

                }  else {

                    Log.d(ETIQUETA_LOG, " onRequestPermissionResult(): Socorro: permisos NO concedidos  !!!!");
                }
                return;
        }

    }

    // _______________________________________________________________
    public void boton_prueba_pulsado(View quien) {
        Log.d("clienterestandroid", "boton_prueba_pulsado");

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        // Make the GET request
        Call<JSONObject> call = apiService.checkMeasurement(
                temperaturaInput.getText().toString(),
                coInput.getText().toString()
        );

        call.enqueue(new retrofit2.Callback<JSONObject>() {
            @Override
            public void onResponse(Call<JSONObject> call, retrofit2.Response<JSONObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject jsonResponse = response.body();
                        String success = jsonResponse.getString("success");
                        String message = jsonResponse.getString("message");

                        Log.d("clienterestandroid", "Server response success: " + success);
                        Log.d("clienterestandroid", "Server message: " + message);

                        // Display a Toast message for user feedback
                        Toast.makeText(getApplicationContext(), "Response: " + message, Toast.LENGTH_LONG).show();

                    } catch (JSONException e) {
                        Log.e("clienterestandroid", "JSON parsing error: " + e.getMessage());
                    }
                } else {
                    Log.d("clienterestandroid", "Request failed with response code: " + response.code());
                    Log.d("clienterestandroid", "Response error message: " + response.message());
                    Toast.makeText(getApplicationContext(), "Error: " + response.message(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<JSONObject> call, Throwable t) {
                Log.e("clienterestandroid", "Network request failed: " + t.getMessage());
                Toast.makeText(getApplicationContext(), "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });


    }

    // _______________________________________________________________
    public void boton_enviar_pulsado_client(View quien) {
        Log.d("clienterestandroid", "boton_enviar_pulsado_client");

        LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        @SuppressLint("MissingPermission") Location loc = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (loc == null) {
            Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show();
            return;
        }

        // 获取输入数据
        String temperaturaStr = temperaturaInput.getText().toString();
        String coStr = coInput.getText().toString();

        if (temperaturaStr.isEmpty() || coStr.isEmpty()) {
            Toast.makeText(this, "Please enter both temperature and CO values", Toast.LENGTH_SHORT).show();
            return;
        }

        double temperatura = Double.parseDouble(temperaturaStr);
        double co = Double.parseDouble(coStr);

        // 创建POST数据的JSON对象
        JSONObject postData = new JSONObject();
        try {
            postData.put("sensorType", 1); // 1表示CO，或根据实际情况传递值
            postData.put("value", co);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        // 使用Retrofit进行POST请求
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<JSONObject> call = apiService.insertMeasurement(postData);

        call.enqueue(new retrofit2.Callback<JSONObject>() {
            @Override
            public void onResponse(Call<JSONObject> call, retrofit2.Response<JSONObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("clienterestandroid", "Server response: " + response.body().toString());
                    Toast.makeText(getApplicationContext(), "Data sent successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("clienterestandroid", "Request failed with code: " + response.code());
                    Toast.makeText(getApplicationContext(), "Data sending failed with code: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JSONObject> call, Throwable t) {
                Log.e("clienterestandroid", "Request failed: " + t.getMessage());
                Toast.makeText(getApplicationContext(), "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // 发送数据的方法
    private void sendSensorDataToServer(int sensorType, double value) {
        // 创建POST数据的JSON对象
        JSONObject postData = new JSONObject();
        try {
            postData.put("sensorType", sensorType);
            postData.put("value", value);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        // 使用Retrofit进行POST请求
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<JSONObject> call = apiService.insertMeasurement(postData);

        call.enqueue(new retrofit2.Callback<JSONObject>() {
            @Override
            public void onResponse(Call<JSONObject> call, retrofit2.Response<JSONObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject jsonResponse = response.body();
                        String success = jsonResponse.getString("success");
                        String message = jsonResponse.getString("message");

                        if ("1".equals(success)) {
                            Log.d(ETIQUETA_LOG, "Data saved successfully: " + message);
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d(ETIQUETA_LOG, "Data saving failed: " + message);
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d(ETIQUETA_LOG, "Data saving failed ");
                }
            }

            @Override
            public void onFailure(Call<JSONObject> call, Throwable t) {
                Log.d(ETIQUETA_LOG, "Error: " + t.getMessage());
            }
        });
    }


}