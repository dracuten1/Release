#include <ESP8266WiFi.h>
#include <PubSubClient.h>
#include <SD.h>
#include <SPI.h>
#include <time.h>
#include <stdio.h>
//DHT sensor
#include <DHT.h>

//Light
#include <Wire.h>
#include <BH1750.h>
#include <Adafruit_NeoPixel.h>

#define DHTPIN 2     // what pin we're connected to
#define DHTTYPE DHT22   // DHT 22  (AM2302)

//topic zone *******************************************
const char* autoTopic="1MyMushroomHousetkb2e32/home/auto";
const char* manualTopic="2MyMushroomHousetkb2e32/home/manual";
const char* requestTopic="3MyMushroomHousetkb2e32/home/request";
const char* configTimeTopic="MyMushroomHousetkb2e32/home/Configtime";
const char* statusTopic="MyMushroomHousetkb2e32/home/status";
const char* updateTopic="MyMushroomHousetkb2e32/home/info";
const char* contentRequestTopic="5MyMushroomHousetkb2e32/home/content";
const char* contentUpdateTopic="MyMushroomHousetkb2e32/home/update/content";
const char* listFileRequestTopic="4MyMushroomHousetkb2e32/home/listFile";
const char* listFileUpdateTopic="MyMushroomHousetkb2e32/home/update/listFile";
//end topic zone *******************************************

DHT dht(DHTPIN, DHTTYPE);
//Class zone ************************************
class Clock{
public:
  Clock(bool interNet){
    if(interNet){
      configTime(7 * 3600, 0, "pool.ntp.org", "time.nist.gov");
      //Serial.println("\nWaiting for time");
      while (!time(nullptr)) {
        //Serial.print(".");
        delay(1000);
      }
    }
  }
  void ConfigTime(){
    configTime(7 * 3600, 0, "pool.ntp.org", "time.nist.gov");
      //Serial.println("\nWaiting for time");
      while (!time(nullptr)) {
        //Serial.print(".");
        delay(1000);
      }
    }
  String GetTime(){
    time_t now = time(nullptr);
    return ctime(&now);
  }
  String GetDate(){
    String date="";
    time_t now = time(nullptr);
    struct tm tm = *localtime(&now);
    date+=tm.tm_mday;
    date+="_";
    date+=tm.tm_mon+1;
    date+=".txt";
    return date;
  }
  struct tm TimeStruct(){
     time_t now = time(nullptr);
     return *localtime(&now);
  }
};
class Controller{
protected:
    float condition;
public:
    Controller(float defaultCondition){
        
        condition=defaultCondition;
    }
    bool SetCondition(float newCondition){
        //Serial.print("Set condition: ");
        //Serial.println(newCondition);
        condition=newCondition;
        //Serial.print("Get condition");
        //Serial.println(newCondition);
        return true;
    }
    virtual void Control (float realStatus){}
};

class TemperatureController:public Controller{
private:
    //const int TEMP_RELAY=16;   //TODO: setup temperature relay port
public:
    TemperatureController():Controller(28){
        //pinMode(TEMP_RELAY,OUTPUT);
    }
    void Control(float currentTemp){
        String s="Set_Temp ";
        s+= currentTemp;
        Serial.println(s);
    }
};

class HumidityController:public Controller{
private:
   //const int WATER_RELAY=3;   //TODO: setup water relay port
   //const int WW=1;
public:
    HumidityController():Controller(70){
        //pinMode(WATER_RELAY,OUTPUT);
        //pinMode(WW,OUTPUT);
    }  
    void Control(float currentHumi){
        String s="Set_Humi ";
        s+= currentHumi;
        Serial.println(s);
    }
};
class LightController:public Controller{
private:
 
public:
     void SetColor(int r, int g, int b){
        String colorR="Light_R ";
        colorR+=r;
        Serial.println(colorR);
        String colorG="Light_G ";
        colorG+=g;
        Serial.println(colorG);
        String colorB="Light_B ";
        colorB+=b;
        Serial.println(colorB);
    }
    void Control(float currentLux){
        String s="Set_Lux ";
        s+= currentLux;
        Serial.println(s);
    }
    LightController():Controller(600){
        SetColor(255,255,255);
    }
};

class MemoryCard{
private:
    //CS_PIN D8
    //SCK D13
    //MISO D12
    //MOSI D11
    const int CS_PIN = 0; 
    bool isOpen=false;
public:
    MemoryCard(){
        
    }
    bool IsCardOpen(){
      return isOpen;
    }
    bool ConnectCard(){
        //Serial.print("Initializing SD card...");
        int attempCount=3;
        while(!SD.begin(CS_PIN)){
            if(--attempCount==0){
                //Serial.print("Initialize SD card fail");
                return false;
            }
                
        delay(500);
        }
        isOpen=true;
        Serial.println("card initialized.");
        return isOpen;
    }
    String GetListFile(String str) {
      //Serial.println("List file getting.");
      File dir= SD.open(str);
      String lstFiles="";
      while (true) {   
        File entry =  dir.openNextFile();
        if (! entry) {
          // no more files
          //Serial.println("No file");
          break;
        }
        //Serial.println("Found a file");
        lstFiles+="|";
        lstFiles+=entry.name();
        entry.close();
      }
      //Serial.println(lstFiles);
      return lstFiles;
    }
    bool WriteToFile(String line, String fileName){
        //Serial.println("writing file.");
        if(isOpen){
            File file = SD.open(fileName,FILE_WRITE);
            file.println(line);
            file.close();
            //Serial.println("writing done.");
            return true;
        }
        //Serial.println("writing fail.");
        return false;
    }
    String ReadFile(String fileName){
        if(isOpen){
          File entry =  SD.open(fileName);
          //Serial.println(fileName);
          String strResult="Eror404";
          if(entry){
            strResult="";
            while(entry.available()){
              char* tmp = strdup(entry.readStringUntil('\n').c_str());
              strResult+=tmp;
              strResult+="-";
              free(tmp); 
            }
            entry.close();
          }
          
          //Serial.println(strResult);
          return strResult;
        }
    }
    String GetSettingFile(){
        String settingStr="";
        if(isOpen){
            if (SD.exists("setting.txt")) {
            File dataFile = SD.open("setting.txt");
            while (dataFile.available()) {
                char* tmp = strdup(dataFile.readStringUntil('\n').c_str());
                settingStr=tmp;
                free(tmp);
            }
            dataFile.close();
            }
        }
        return settingStr; 
    }
    void WriteSetting(bool isAuto,String str){
      if(isOpen){
            //Serial.println("writing file.");
            if(SD.exists("setting.txt")){
              SD.remove("setting.txt");
            }
            File file = SD.open("setting.txt",FILE_WRITE);
            if(isAuto){
              file.print("1");
              file.println(str);
            }else{
              file.print("0");
            }
           
            file.close();
            //Serial.println("writing done.");
        }
    }
   void WriteNewFile(String fileName,String str){
      if(isOpen){
          //Serial.print("writing file: ");
          //Serial.println(fileName);
          if(SD.exists(fileName)){
            SD.remove(fileName);
          }
          File file = SD.open(fileName,FILE_WRITE);
          file.println(str);
          file.close();
          //Serial.println("writing done.");
      }
   }
};

class Logger{
private:
    MemoryCard *_m;
    String logFile;
    String currentLog="";
public:
    Logger(MemoryCard &m){
        //Serial.println("Initing logger.");
        _m=&m;
    }
    void SetLogName(String fileName){
        logFile=fileName;
    }
    void Log(String line){
        //Serial.println("Logged.");
        _m->WriteToFile(line, logFile);
    }
};



struct Condition{
    int fHour,fMinute,tHour,tMinute;    
    float lux;
    float temp;
    float humi;
    int R, G, B;
};
class ConditionController{
private:
    struct Condition currentCon={0,0,0,0,600,30,70,255,255,255};
    LightController* lightCtller;
    TemperatureController* tempCtrller;
    HumidityController* humiCtrller;
    Clock *myClock;
    bool isSdOpen=false;
    bool autoMode=true;
    String autoFile="";
    
public:
    ConditionController(LightController &l, TemperatureController &t, HumidityController &h,Clock &c){
        lightCtller=&l;
        tempCtrller=&t;
        humiCtrller=&h;
        myClock=&c;
    }
    void NotifyController(){
        lightCtller->SetCondition(currentCon.lux);
        String sL="Lux_Control ";
        int curLux=currentCon.lux;
        sL+=curLux;
        Serial.println(sL);
        tempCtrller->SetCondition(currentCon.temp);
        String sT="Temp_Relay ";
        float tempCon=currentCon.temp;
        sT+=tempCon;
        Serial.println(sT);
        humiCtrller->SetCondition(currentCon.humi);
        String s="Humi_Relay ";
        float humidity=currentCon.humi;
        s+= humidity;
        Serial.println(s);
        lightCtller->SetColor(currentCon.R,currentCon.G,currentCon.B);        
        //Serial.println("Control:");
    }
    void SetSD(bool sd){
        isSdOpen=sd;
    }
    void LoadSetting(String str){
        if(str[0]=='0'){ 
          autoMode=false;
          if (SD.exists("manual.txt")) {
            File dataFile = SD.open("manual.txt");
            if (dataFile.available()) {
              char* tmp = strdup(dataFile.readStringUntil('\n').c_str());
              currentCon=GetCondition(tmp);
              NotifyController();
            }
            dataFile.close();
          } 
          return;
        }
        autoMode=true;
        autoFile="";
        for(int i=1;i<str.length();i++){
            autoFile+=str[i];
        }
    }
    void SetMode(bool isAuto,String str){
        if(isAuto){ 
          autoMode=true;
          if(!str.equals(""))
              autoFile=str;
              SetCurrentCondition();
        }else{
            autoMode=false;
            char line[256];
            str.toCharArray(line,256);
            struct Condition con=GetCondition(line);
            //Serial.println("Get done");
            if(con.temp==0&&con.lux==0&&con.humi==0) return;
            //Serial.println("Set con done");
            currentCon=con;
        }
        NotifyController();
    }
    void RunAuto(){
        struct tm tm=myClock->TimeStruct();
        int value = tm.tm_hour * 60 + tm.tm_min;
    }
    bool CheckCondition(struct Condition old, struct tm tm) {
        int from = old.fHour * 60 + old.fMinute;
        int to = old.tHour * 60 + old.tMinute;
        int value = tm.tm_hour * 60 + tm.tm_min;
        return (from <= value && to > value);
    }
    void SetCurrentCondition(){
        if(isSdOpen){
            if(autoMode){
              if(CheckCondition(currentCon,myClock->TimeStruct())) return;
                if (SD.exists(autoFile)) {
                    File dataFile = SD.open(autoFile);
                    while (dataFile.available()) {
                        char* tmp = strdup(dataFile.readStringUntil('\n').c_str());
                        struct Condition newCondition=GetCondition(tmp);
                        if(CheckCondition(newCondition,myClock->TimeStruct())){                            
                            currentCon=newCondition;
                            NotifyController();
                            break;
                        }
                    }
                    dataFile.close();
                }
            }
        }
    }
    bool CheckLine(char *line){
        String s=line;
        int doubleDotCount=0;
        int hyphanCount=0;
        int collumCount=0;
        int xCount=0;
        int spaceCount=0;
        for(int i=0;i<s.length();i++){
            if(s[i]==':') doubleDotCount++;
            if(s[i]=='-') hyphanCount++;
            if(s[i]=='|') collumCount++;
            if(s[i]=='/') xCount++;
            if(s[i]==' ') spaceCount++;
        }
        return (doubleDotCount==2&&hyphanCount==1&&collumCount==1&&xCount==3&&spaceCount==2);
    };
    struct Condition GetCondition(char* line){
        Condition con={0,0,0,0,0,0,0,0,0,0};
        if(!CheckLine(line)) return con;
        //Serial.println("Begin get condition");
        //line: hh:mm-hh:mm|temp/humi/lux/R G B
        
        strtok(line, "|"); //Save first "" |.. from line to ftime
        //Get temp and humi
        char * info=strtok(NULL, "|");    //Save second |""| to info
        //Serial.print("Info: ");
        //Serial.println(info);
        info = strtok(info, "/");     //Save first ""/ from info to info  
        con.temp = atof(info);  
        con.humi = atof(strtok(NULL, "/"));//...
        con.lux= atoi(strtok(NULL,"/"));
        char *rgb=strtok(NULL,"/\n");
        con.R=atoi(strtok(rgb," "));
        con.G=atoi(strtok(NULL," "));
        con.B=atoi(strtok(NULL," \n"));
        //Get time range
        char* ftime = strtok(line, "-");
        char* toTime = strtok(NULL, "-\n");

        
        con.fHour = atoi(strtok(ftime,":"));

        con.fMinute = atoi(strtok(NULL, ":"));

        con.tHour = atoi(strtok(toTime, ":"));

        con.tMinute = atoi(strtok(NULL, ":"));
        return con;
    }
};

class EnviromentObserver{
private:
    BH1750 lightMeter;

    LightController* lightCtller;
    TemperatureController* tempCtrller;
    HumidityController* humiCtrller;
    float temperature=30;
    float humidity=70;
    float lux=600;
public:
    EnviromentObserver(LightController &l, TemperatureController &t, HumidityController &h){
        Wire.begin();
        dht.begin();
        lightMeter.begin();
        lightCtller=&l;
        tempCtrller=&t;
        humiCtrller=&h;
    }
    void Validate(){
        lux=lightMeter.readLightLevel();
        float t=dht.readTemperature();
        float h=dht.readHumidity();
        if (isnan(h) && isnan(t)) {
          //Serial.println("Failed to read from DHT sensor!"); 
        }else{
            temperature=t;
            humidity=h;
        }
        NotifyController();
    }
    String GetInfomation(){
        String str=String(temperature);
        str+="|";
        str+=humidity;
        str+="|";
        str+=lux;
        return str;
    };
    void NotifyController(){
        //Serial.println("Notify controller");
        lightCtller->Control(lux);
        //Serial.println("Notify lux");
        tempCtrller->Control(temperature);
        //Serial.println("Notify temp");
        humiCtrller->Control(humidity);
        //Serial.println("Notify controller done");
    }
};
class WifiConnector{
private:
  String _ssid="My Internet";
  String _password="1234598765";
  bool cnStatus=false;
public:
  WifiConnector(char *ssid, char* password){
    _ssid=ssid;
    _password=password;
  }
  void SetUpWifi(){
    if(SD.exists("wifi.txt")){
      File wifiFile = SD.open("wifi.txt");
      //char line[256];
      if(wifiFile.available()) {     
        _ssid=wifiFile.readStringUntil('\n');
        _ssid.trim();
        //Serial.print("SSID read: ");
        //Serial.print(_ssid);
        
        char pass[50];
        _password =wifiFile.readStringUntil('\n');
        _password.trim();
        //Serial.print("pass read: ");
        //Serial.print(_password);
        
      }
      wifiFile.close();
    }
  }
  bool Connect(){
    //Serial.println();
    //Serial.print("Connecting to ");
    //Serial.println(_ssid);
    //Serial.println(_password);
    WiFi.mode(WIFI_STA);
    
    char ssid[50];
    _ssid.toCharArray(ssid,50);
    //Serial.println(ssid);
    char pass[50];
    _password.toCharArray(pass,50);
    //Serial.println(pass);
      
    WiFi.begin(ssid, pass);
    int attempCount=10;
    while (WiFi.status() != WL_CONNECTED) {
      delay(1000);
      //Serial.print(".");
      if(attempCount--==0) return false;
    }
    cnStatus=true;
    //Serial.println("");
    //Serial.println("WiFi connected");
    //Serial.println("IP address: ");
    //Serial.println(WiFi.localIP());
    return cnStatus;
  }
  bool Status(){
    return cnStatus;
  }
};

WiFiClient espClient;
PubSubClient client(espClient);
class TimeSchedule{
private:
  long logStart=0;
  long logPeriod=1500000;
  long updateStart=0;
  long updatePeriod=10000;
  Logger* logger;
  EnviromentObserver *observer;
  Clock *myClock;
  int curDay=0;
public:
    TimeSchedule(Logger &lg,EnviromentObserver &ob,Clock &cl){
    logger=&lg;
    observer=&ob;
    myClock=&cl;
    logStart=millis();
    updateStart=millis();  
  }
  
  void Validate(){
    if(millis()-logStart>logPeriod){
      logger->SetLogName(myClock->GetDate());
      struct tm tm=myClock->TimeStruct();
      int value = tm.tm_hour * 60 + tm.tm_min;
      String logLine="";
      logLine+=tm.tm_hour;
      logLine+=":";
      logLine+=tm.tm_min;
      logLine+="|";
      logLine+=observer->GetInfomation();
      logger->Log(logLine);
      logStart=millis();
    }
    if(millis()-updateStart>updatePeriod){
      observer->Validate();
      //Serial.println("Public infomation: ");
      String str=observer->GetInfomation();
      //Serial.println(str);
      client.publish(updateTopic,str.c_str());
      updateStart=millis();
    }
  }
};

//End class zone            ************************************
WifiConnector wifiConnector("My Internet","1234598765");
Clock *myClock;
TimeSchedule *tScd;
MemoryCard sdCard;
Logger logger(sdCard);
EnviromentObserver *gOserver;
LightController light;
TemperatureController temp;
HumidityController humi;
ConditionController *conditionCtrler;

//MQTT zone         *****************************************************************************


long lastMsg = 0;
char msg[50];
int value = 0;

void callback(char* topic, byte* payload, unsigned int length) {
   //Serial.print("Message arrived [");
   //Serial.print(topic);
   //Serial.print("] ");
   String str="";
   for (int i = 0; i < length; i++) {
      str+=(char)payload[i];
   }
   //Serial.print(str);
   //Serial.println();
  char offset[1]={(char)topic[0]};
  
  switch(atoi(offset)){
    case 1:{
      //automatic
      //Serial.println("Automatic on... initilize new condition");
      sdCard.WriteSetting(true,"");//TODO:
      sdCard.WriteNewFile("auto.txt",str);
      conditionCtrler->SetMode(true,"auto.txt");
      //Serial.println("Setup auto done");
      break;
    }
    case 2:{
      //manual
      //Serial.println("Automatic off... initilize new condition");
      sdCard.WriteSetting(false,"");
      sdCard.WriteNewFile("manual.txt",str);
      conditionCtrler->SetMode(false,str);
      //Serial.println("Setup manual done");
      break;
    }
    case 3:{
      //serverRequest=true;
      //Serial.println("Public infomation: ");
      String str1=gOserver->GetInfomation();
      //Serial.println(str1);
      client.publish(updateTopic,str1.c_str());
      break;
    }
    case 4:{
      //period=(int)payload;
      String lstFiles=sdCard.GetListFile(str);
      //lstFiles="27_11.TXT|25_11.TXT|FOUND.000|AUTO.TXT|28_11.TXT|30_11.TXT";
      //Serial.println("------------------------------------------------");
      //Serial.println(lstFiles.c_str());
      client.publish(listFileUpdateTopic,lstFiles.c_str());
       client.publish(listFileUpdateTopic,"Allo");
      break;
    }
    case 5:{
      String content=sdCard.ReadFile(str);
      client.publish(contentUpdateTopic,content.c_str());
      break;
    }
    default:
    {
      break;
    }
    }
}

void reconnect() {
  // Loop until we're reconnected
    if(wifiConnector.Status()){         
    int attempt=3;
    while (!client.connected()) {
      //Serial.print("Attempting MQTT connection...");
      // Create a random client ID
      String clientId = "ESP8266Client-";
      clientId += String(random(0xffff), HEX);
      // Attempt to connect
      if (client.connect(clientId.c_str())) {
        //Serial.println("connected");
        // Once connected, publish an announcement...
        client.publish(statusTopic, clientId.c_str());
        // ... and resubscribe
        client.subscribe(autoTopic);
        client.subscribe(manualTopic);
        client.subscribe(requestTopic);
        client.subscribe(contentRequestTopic);
        client.subscribe(listFileRequestTopic);
      } else {
        if(--attempt<0) return;
        //Serial.print("failed, rc=");
        //Serial.print(client.state());
        //Serial.println(" try again in 5 seconds");
        // Wait 5 seconds before retrying
        delay(2000);
      }
    }
  }
}



//end zone            **********************************************************************************************************
void setup() {
    // Open serial communications and wait for port to open:
    Serial.begin(19200);
    while (!Serial) {
        ; // wait for serial port to connect. Needed for Leonardo only
    }
    sdCard.ConnectCard();
    wifiConnector.SetUpWifi();
    wifiConnector.Connect();
    client.setServer("broker.mqtt-dashboard.com", 1883);
    client.setCallback(callback);
    myClock=new Clock(wifiConnector.Status());
    gOserver = new EnviromentObserver(light,temp,humi);
    conditionCtrler= new ConditionController(light,temp,humi, *myClock);
    conditionCtrler->SetSD(sdCard.IsCardOpen());
    conditionCtrler->LoadSetting(sdCard.GetSettingFile());
    tScd=new TimeSchedule(logger, *gOserver,*myClock);
    gOserver->Validate();
}

void loop() {
  if(!wifiConnector.Status()){
    wifiConnector.Connect();
    if(wifiConnector.Status()){
      myClock->ConfigTime();
    }
  }
  if (!client.connected()) {
    reconnect();
  }
  if (client.connected()) {
      client.loop(); 
  }
    //Serial.println("Loop");
    tScd->Validate();
    //gOserver->NotifyController();
    conditionCtrler->SetCurrentCondition();
    delay(100);
}
