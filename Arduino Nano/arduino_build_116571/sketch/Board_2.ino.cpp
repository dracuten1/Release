#include <Arduino.h>
#line 1 "F:\\Git\\AnhQuoc\\Release\\Arduino Nano\\Board_2\\Board_2.ino"
#line 1 "F:\\Git\\AnhQuoc\\Release\\Arduino Nano\\Board_2\\Board_2.ino"
#include <SerialCommand.h> // Thêm vào sketch thư viện Serial Command
#include <Adafruit_NeoPixel.h>
#include <Wire.h> 
#include <LiquidCrystal_I2C.h>

#define CELL_RELAY 6
#define TEMP_RELAY 4
#define WATER_RELAY 5
#define PIN 7
SerialCommand sCmd; // Khai báo biến sử dụng thư viện Serial Command
LiquidCrystal_I2C lcd(0x27, 20, 4);//Thêm màn hình lcd


float tempCon=30;
float humiCon=75;
int luxCon=600;
 
class LightController{
private:   
    const int NUMPIXELS =8*4;

    int _currentLedOff=0;
    int _r=255;
    int _g=255;
    int _b=255;

    Adafruit_NeoPixel pixels = Adafruit_NeoPixel(NUMPIXELS, PIN, NEO_GRB + NEO_KHZ800);

    void TurnOnALed(){
        if(_currentLedOff>0){
            _currentLedOff = _currentLedOff - 1;
        }
    }
    void TurnOffALed(){
        if(_currentLedOff<NUMPIXELS){
            _currentLedOff =_currentLedOff+ 1;
        }
    }
    
public:
    void ControlNeoColor() {
          for(int i=0;i<NUMPIXELS;i++){
//              if(i==32){
//                  int temp=_r;
//                  _r=_g;
//                  _g=temp;  
//              }
//              if(i==40){
//                int temp=_r;
//                _r=_g;
//                _g=temp;
//             }
              if(i<_currentLedOff){
                  pixels.setPixelColor(i, pixels.Color(0,0,0));  
              }else{
                  pixels.setPixelColor(i, pixels.Color(_r,_g,_b));
              }   
          
              
          }
          pixels.show();
      }
     void SetColor(int r, int g, int b){
        //Serial.println("Color");
        //Serial.print(r);
        //Serial.println(g);
        //Serial.println(b);
        
        _r=r;
        _g=g;
        _b=b;
        ControlNeoColor();
    }
     void SetRColor(int r){      
        _r=r;       
    }
    void SetGColor(int r){      
        _g=r;       
    }
     void SetBColor(int r){      
        _b=r;       
    }
    void Control(int currentLux){
        //Serial.println("LightController");
        if(currentLux==0){          
            TurnOffALed();
        }else {
            TurnOnALed();
        }
        ControlNeoColor();
    }
    LightController(){
        pixels.begin();
        SetColor(255,0,0);
    }
};

LightController *light;
#line 99 "F:\\Git\\AnhQuoc\\Release\\Arduino Nano\\Board_2\\Board_2.ino"
void setup();
#line 137 "F:\\Git\\AnhQuoc\\Release\\Arduino Nano\\Board_2\\Board_2.ino"
void loop();
#line 144 "F:\\Git\\AnhQuoc\\Release\\Arduino Nano\\Board_2\\Board_2.ino"
void HumiCon();
#line 151 "F:\\Git\\AnhQuoc\\Release\\Arduino Nano\\Board_2\\Board_2.ino"
void SetTempCon();
#line 160 "F:\\Git\\AnhQuoc\\Release\\Arduino Nano\\Board_2\\Board_2.ino"
void SetLuxCon();
#line 171 "F:\\Git\\AnhQuoc\\Release\\Arduino Nano\\Board_2\\Board_2.ino"
void Water();
#line 195 "F:\\Git\\AnhQuoc\\Release\\Arduino Nano\\Board_2\\Board_2.ino"
void Temp();
#line 223 "F:\\Git\\AnhQuoc\\Release\\Arduino Nano\\Board_2\\Board_2.ino"
void Lux();
#line 246 "F:\\Git\\AnhQuoc\\Release\\Arduino Nano\\Board_2\\Board_2.ino"
void LightR();
#line 260 "F:\\Git\\AnhQuoc\\Release\\Arduino Nano\\Board_2\\Board_2.ino"
void LightG();
#line 274 "F:\\Git\\AnhQuoc\\Release\\Arduino Nano\\Board_2\\Board_2.ino"
void LightB();
#line 99 "F:\\Git\\AnhQuoc\\Release\\Arduino Nano\\Board_2\\Board_2.ino"
void setup() {
  //Khởi tạo Serial ở baudrate 9600 (trùng với HOST)

  Serial.begin(19200);
  while (!Serial) {
        ; // wait for serial port to connect. Needed for Leonardo only
    }
  //Serial.println("Restart");
  lcd.begin();
  // Print a message to the LCD.
  lcd.backlight();
    String s="Set up....";
  lcd.setCursor(0,0);
  lcd.print(s);
  //pinMode 2 đèn LED là OUTPUT
  light=new LightController();
  pinMode(TEMP_RELAY,OUTPUT);
  pinMode(WATER_RELAY,OUTPUT);
  pinMode(CELL_RELAY,OUTPUT);

  sCmd.addCommand("Light_R", LightR);
    sCmd.addCommand("Light_G", LightG);
    sCmd.addCommand("Light_B", LightB);
    sCmd.addCommand("Set_Temp",Temp);
     sCmd.addCommand("Temp_Relay", SetTempCon);
     sCmd.addCommand("Set_Lux",Lux);
   sCmd.addCommand("Lux_Control", SetLuxCon);
   sCmd.addCommand("Set_Humi",Water);
   sCmd.addCommand("Humi_Relay", HumiCon);
    

   s="Dang cai dat";
  lcd.setCursor(0,0);
  lcd.print(s);
  delay(2000);
  //Serial.println("Setup done");
}
 
void loop() {
  sCmd.readSerial();
  light->ControlNeoColor();
  //Serial.println("Loop over");
  //Bạn không cần phải thêm bất kỳ dòng code nào trong hàm loop này cả
}

void HumiCon(){
    //Đoạn code này dùng để đọc TỪNG tham số. Các tham số mặc định có kiểu dữ liệu là "chuỗi"
  char *arg;
  arg = sCmd.next();
  float value = atof(arg); 
  humiCon=value;
}
void SetTempCon(){
    char *arg;
  arg = sCmd.next();
  //Serial.print("Water");
  
  float value = atof(arg); // Chuyển chuỗi thành số
  tempCon=value;
  //Serial.print("Set temp control");
}
void SetLuxCon(){

    char *arg;
  arg = sCmd.next();
  //Serial.print("Water");
  
  int value = atoi(arg); // Chuyển chuỗi thành số
  luxCon=value;
  //Serial.print("Set lux control");
}
// hàm led_red sẽ được thực thi khi gửi hàm LED_RED
void Water() {
  //Đoạn code này dùng để đọc TỪNG tham số. Các tham số mặc định có kiểu dữ liệu là "chuỗi"
  char *arg;
  arg = sCmd.next();
  //Serial.print("Water");
  
  float value = atof(arg); // Chuyển chuỗi thành số
  //Serial.println(value);
  String s="             ";
  lcd.setCursor(0,0);
  lcd.print(s);
  lcd.setCursor(0,0);
  s="Do am: ";
  s+=arg;
  lcd.print(s);
  if(value>humiCon+2){
    digitalWrite(WATER_RELAY,LOW);
  }
  else if(value<humiCon-2){
  digitalWrite(WATER_RELAY,HIGH);
  }
}

// hàm led_blue sẽ được thực thi khi gửi hàm LED_BLUE
void Temp() { 
  //Đoạn code này dùng để đọc TỪNG tham số. Các tham số mặc định có kiểu dữ liệu là "chuỗi"
  char *arg;
  arg = sCmd.next();
  
  int value = atoi(arg); // Chuyển chuỗi thành số
  //Serial.print("Temp: ");
  //Serial.println(value);
  String s="             ";
  lcd.setCursor(0,1);
  lcd.print(s);
  lcd.setCursor(0,1);
  s="Nhiet do: ";
  s+=arg;
  lcd.print(s);
  if(value>tempCon+0.5){
    digitalWrite(TEMP_RELAY,LOW);
    digitalWrite(CELL_RELAY,HIGH);
  }
  else if(value<tempCon-0.5){
    digitalWrite(TEMP_RELAY,HIGH);
    digitalWrite(CELL_RELAY,LOW);
  }
  else if(value<=tempCon+0.2||value>=tempCon-0.2){
    digitalWrite(TEMP_RELAY,LOW);
    digitalWrite(CELL_RELAY,LOW);
  }
}
void Lux(){
  //Đoạn code này dùng để đọc TỪNG tham số. Các tham số mặc định có kiểu dữ liệu là "chuỗi"
  char *arg;
  arg = sCmd.next();
  
  int value = atof(arg); // Chuyển chuỗi thành số
  String s="             ";
  lcd.setCursor(0,2);
  lcd.print(s);
  lcd.setCursor(0,2);
  s="Do sang: ";
  s+=arg;
  lcd.print(s);
   //Serial.println("LightController");
        if(value > luxCon+ 30){          
          //Serial.println("Lux_control 0");
          light->Control(0);
        }else if(value < luxCon - 30){
         //Serial.println("Lux_control 1");
          light->Control(1);
        }
  
}
void LightR(){
  //Đoạn code này dùng để đọc TỪNG tham số. Các tham số mặc định có kiểu dữ liệu là "chuỗi"
  char *arg;
  arg = sCmd.next();
  int r = atoi(arg); // Chuyển chuỗi thành số
  light->SetRColor(r);
  String s="      ";
  lcd.setCursor(0,3);
  lcd.print(s);
  lcd.setCursor(0,3);
  s="R: ";
  s+=arg;
  lcd.print(s);
}
void LightG(){
  //Đoạn code này dùng để đọc TỪNG tham số. Các tham số mặc định có kiểu dữ liệu là "chuỗi"
  char *arg;
  arg = sCmd.next();
  int r = atoi(arg); // Chuyển chuỗi thành số
  light->SetGColor(r);
   String s="      ";
  lcd.setCursor(7,3);
  lcd.print(s);
  lcd.setCursor(7,3);
  s="G: ";
  s+=arg;
  lcd.print(s);
}
void LightB(){
  
  //Đoạn code này dùng để đọc TỪNG tham số. Các tham số mặc định có kiểu dữ liệu là "chuỗi"
  char *arg;
  arg = sCmd.next();
  int r = atoi(arg); // Chuyển chuỗi thành số
  light->SetBColor(r);
   String s="      ";
  lcd.setCursor(14,3);
  lcd.print(s);
  lcd.setCursor(14,3);
  s="B: ";
  s+=arg;
  lcd.print(s);
}

