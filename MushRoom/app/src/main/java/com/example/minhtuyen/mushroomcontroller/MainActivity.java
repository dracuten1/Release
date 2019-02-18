package com.example.minhtuyen.mushroomcontroller;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

import java.io.UnsupportedEncodingException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    static final String PUT_STRING = "Condition";
    static final String GET_STRING = "NewCondition";
    private SQLiteDatabaseHandler db;
    private MqttAndroidClient client;
    private boolean isConnected = false;
    private Spinner houseSpiner;
    private Button btnChange;
    private Button btnChart;
/*    private String[] curInfo = {"0", "0", "0"};
    String autoTopic = "1MyMushroomHousetkb2e32/home/auto";
    String manualTopic = "2MyMushroomHousetkb2e32/home/manual";
    String requestTopic = "3MyMushroomHousetkb2e32/home/request";
    //String configTimeTopic="4MyMushroomHousetkb2e32/home/Configtime";
    String statusTopic = "MyMushroomHousetkb2e32/home/status";
    String updateTopic = "MyMushroomHousetkb2e32/home/info";*/
    static ITopic topic=new Topic1();
    private Condition curCondition=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = new SQLiteDatabaseHandler(this);
        btnChange = (Button) findViewById(R.id.btnChange);
        btnChart = (Button) findViewById(R.id.btnChart);
        curCondition=GetCurrentCondition();
        if (curCondition == null) {
            curCondition = new Condition("Current");
            db.addCondition(curCondition);
            curCondition = GetCurrentCondition();
        }
        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Clicked", Toast.LENGTH_LONG).show();
                Intent controlActiveIntent = new Intent(MainActivity.this, Controller.class);
                controlActiveIntent.putExtra(PUT_STRING, curCondition);
                startActivityForResult(controlActiveIntent, 1);
            }
        });
        btnChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chartIntent=new Intent(MainActivity.this, Chart.class);
                startActivity(chartIntent);
            }
        });
        btnChange.setVisibility(View.GONE);
        SetUpClient("tcp://broker.hivemq.com:1883");//broker.hivemq.com port 1883
        ConnectServer();
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.topic_array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }
    private Condition GetCurrentCondition(){
        List<Condition> condtions = db.allConditions();
        if (condtions != null) {
            for(Condition con : condtions){
                if(con.getName().equals("Current")){
                    curCondition=con;
                    return con;
                }
            }
        }
        return null;
    }
    private void SetUpClient(String server) {
        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), server, clientId);
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                //Show to screen
                if (topic.equals(MainActivity.this.topic.statusTopic())) {
                    Toast.makeText(MainActivity.this, message.toString() + " connected to server", Toast.LENGTH_LONG).show();
                } else if (topic.equals(MainActivity.this.topic.updateTopic())) {
                    String s = message.toString();
                    s = s.replace("\n", "").replace("\r", "");
                    String[] fullMessage = s.split("\\|", 3);
                    Toast.makeText(MainActivity.this, message.toString(), Toast.LENGTH_LONG).show();

                    if (fullMessage.length != 3) return;
                    ((TextView) findViewById(R.id.tVTemp)).setText(fullMessage[0] + " °C");
                    ((TextView) findViewById(R.id.tVHumi)).setText(fullMessage[1] + " %");
                    ((TextView) findViewById(R.id.tVLight)).setText(fullMessage[2] + " Lux");
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            curCondition = (Condition)data.getSerializableExtra(GET_STRING);
            String msg=curCondition.toString();
            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
            PublishTop(msg, topic.manualTopic());
            PublishTop("1", topic.requestTopic());

            db.updateCondition(curCondition);
        }
    }

    private boolean ConnectServer() {
        final ProgressDialog pDiaglog = new ProgressDialog(MainActivity.this);
        pDiaglog.setMessage("Vui lòng chờ trong giây lát.");
        pDiaglog.show();
        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(MainActivity.this, "Connected to server", Toast.LENGTH_LONG).show();
                    SubTop(MainActivity.this.topic.updateTopic());
                    SubTop(MainActivity.this.topic.statusTopic());

                    boolean isConnected = true;
                    btnChange.setVisibility(View.VISIBLE);
                    PublishTop("1", MainActivity.this.topic.requestTopic());
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Toast.makeText(MainActivity.this, "Connect to server failed", Toast.LENGTH_LONG).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
            return false;
        }
        pDiaglog.dismiss();
        return true;
    }

    private void SubTop(String topic) {
        try {
            client.subscribe(topic, 1);
        } catch (MqttSecurityException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void PublishTop(String msg, String topic) {
        ;
        try {
            byte[] encodedPayload = msg.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            message.setRetained(true);
            client.publish(topic, message);
        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
        }
    }

    public void AutoClick(View view) {
        String msg="00:00-12:30|30/75/600/255 200 230\n12:30-15:30|25/35/600/255 201 230\n15:30-20:30|30/70/600/255 201 230\n20:30-00:00|31/60/600/255 201 230";
        PublishTop(msg,MainActivity.this.topic.autoTopic());
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position){
            case 0:
                if(topic.getClass().equals(Topic1.class)) return;
                unSubcribeAll();
                topic =new Topic1();
                SubTop(topic.updateTopic());
                SubTop(topic.statusTopic());
                break;
            default:
                if(topic.getClass().equals(Topic2.class)) return;
                unSubcribeAll();
                topic =new Topic2();
                SubTop(topic.updateTopic());
                SubTop(topic.statusTopic());
                break;
        }
    }
    private void unSubcribeAll() {

        try {
            client.unsubscribe(topic.updateTopic());
            client.unsubscribe(topic.statusTopic());
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
