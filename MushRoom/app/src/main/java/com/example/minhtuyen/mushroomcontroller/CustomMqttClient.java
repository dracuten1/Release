package com.example.minhtuyen.mushroomcontroller;

import android.content.Context;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttClient;

public class CustomMqttClient {
    MqttAndroidClient client;
    public CustomMqttClient(){};
    public void SetUpClient(String server,int port){
        String clientId = MqttClient.generateClientId();
        client =new MqttAndroidClient(null,server,clientId);

        
    }
}
