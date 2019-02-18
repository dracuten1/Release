package com.example.minhtuyen.mushroomcontroller;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Debug;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Chart extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    private Date minDay=  new Date();
/*    static String listFileUpdateTopic="MyMushroomHousetkb2e32/home/update/listFile";
    static String fileContentUpdateTopic="MyMushroomHousetkb2e32/home/update/content";
    static String listFireRequestTopic="4MyMushroomHousetkb2e32/home/listFile";
    static String fileContentRequestTopic="5MyMushroomHousetkb2e32/home/content";*/
    private MqttAndroidClient client;
    private LineChart mChart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        Button b=(Button)findViewById(R.id.btnPickDate);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment datePicker= new DateTimePickerDialog();
                datePicker.show(getSupportFragmentManager(),"date picker");
            }
        });
        minDay.setDate(1);
        minDay.setMonth(1);
        mChart=(LineChart)findViewById(R.id.chartLine);
        mChart.setScaleEnabled(true);
        mChart.setDragEnabled(true);

        SetUpClient("tcp://broker.hivemq.com:1883");//broker.hivemq.com port 1883
        ConnectServer();
        //PublishTop("/HOUSEDATA/",listFireRequestTopic);
        ArrayList<Entry> temps=new ArrayList<>();
        temps.add(new Entry(0,28));
        temps.add(new Entry(1,27));
        temps.add(new Entry(2,29));
        temps.add(new Entry(3,30));
        temps.add(new Entry(4,28));
        temps.add(new Entry(5,25));
        LineDataSet tempLine= new LineDataSet(temps,"Nhiệt độ");

        tempLine.setFillAlpha(50);
        tempLine.setLineWidth(3f);
        tempLine.setValueTextSize(10f);
        ArrayList<Entry> humis=new ArrayList<>();
        humis.add(new Entry(0,75));
        humis.add(new Entry(1,78));
        humis.add(new Entry(2,80));
        humis.add(new Entry(3,82));
        humis.add(new Entry(4,80));
        humis.add(new Entry(5,81));
        LineDataSet humisLine= new LineDataSet(humis,"Độ ẩm");

        humisLine.setFillAlpha(110);
        humisLine.setColor(Color.RED);
        humisLine.setLineWidth(3f);
        humisLine.setValueTextSize(10f);
        ArrayList<ILineDataSet> dataSet=new ArrayList<>();
        dataSet.add(tempLine);
        dataSet.add(humisLine);

        LineData data =new LineData(dataSet);

        mChart.setData(data);
        XAxis xAxis=mChart.getXAxis();
        //xAxis.setValueFormatter(new TimeFortmatter());

        xAxis.setGranularity(1f);
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
                if (topic.equals(MainActivity.topic.listFileUpdateTopic())) {


                } else if (topic.equals(MainActivity.topic.fileContentUpdateTopic())) {
                    String s = message.toString();
                    if(s.equals("Eror404")){
                        Toast.makeText(Chart.this, "Không có dữ liệu, vui lòng thử với ngày khác", Toast.LENGTH_LONG).show();
                        return;
                    }
                    ChartDataGetter dataGetter= new ChartDataGetter(s);
                    ArrayList<Float> humis=dataGetter.getHumiData();
                    ArrayList<Integer> lights=dataGetter.getLightData();
                    ArrayList<Float> temps=dataGetter.getTempData();
                    LineDataSet tempLine= createTempDataSet("Nhiệt độ",Color.RED);
                    LineDataSet humiLine= createTempDataSet("Độ ẩm",Color.BLUE);
                    LineDataSet lightLine= createTempDataSet("Ánh sáng",Color.BLUE);
                    for(int i=0;i<dataGetter.getTimeData().size();i++){
                        tempLine.addEntry(new Entry(i,temps.get(i)));
                        humiLine.addEntry(new Entry(i,humis.get(i)));
                        lightLine.addEntry(new Entry(i,lights.get(i)));
                    }
                    ArrayList<ILineDataSet> dataSet=new ArrayList<>();
                    dataSet.add(tempLine);
                    dataSet.add(humiLine);
                    dataSet.add(lightLine);

                    LineData data =new LineData(dataSet);
                    mChart.setData(data);

                    XAxis xAxis=mChart.getXAxis();
                    xAxis.setValueFormatter(new TimeFortmatter(dataGetter.getTimeData()));
                    xAxis.setGranularity(1f);

                    try {
                        mChart.notifyDataSetChanged();

                    }catch (Exception exeption){
                        System.out.println(exeption);
                    }
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }
    private LineDataSet createTempDataSet(String lineName, int color){
        LineDataSet dataSet=new LineDataSet(null,lineName);
        dataSet.setFillAlpha(50);
        dataSet.setLineWidth(3f);
        dataSet.setValueTextSize(10f);
        dataSet.setColor(color);
        return dataSet;
    }
    private boolean ConnectServer() {
        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                    SubTop(MainActivity.topic.listFileUpdateTopic());
                    SubTop(MainActivity.topic.fileContentUpdateTopic());
                    Toast.makeText(Chart.this, "Connected", Toast.LENGTH_LONG).show();
                    boolean isConnected = true;
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
            return false;
        }
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

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Button b=(Button)findViewById(R.id.btnPickDate);
        String s="";
        s+=dayOfMonth;
        s+="_";
        int realMonth=month+1;
        s+=realMonth;
        s+=".TXT";
        PublishTop(s,MainActivity.topic.fileContentRequestTopic());
    }

    public class TimeFortmatter implements IAxisValueFormatter{
        private ArrayList<String> _values;
        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            if((int)value<_values.size()) {
                return _values.get((int) value);
            }
            else return "0";
        }
        public TimeFortmatter(ArrayList<String> values){
            _values=values;
        }
    }
}
