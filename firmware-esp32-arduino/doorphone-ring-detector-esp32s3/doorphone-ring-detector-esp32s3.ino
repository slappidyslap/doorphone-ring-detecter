#include <WiFi.h>
#include <WiFiMulti.h>

const char* WIFI_SSID = "WIFI_SSID";
const char* WIFI_PASSWORD = "WIFI_PASSWORD";
const char* DEVICE_SERVER_HOST = "DEVICE_SERVER_HOST"; // mdns
const uint16_t DEVICE_SERVER_PORT = 9126;

const int BUILDIN_LED = 48;
const int SOUND_PIN_ANALOG = A0;
const int SOUND_PIN_DIGITAL = 2;

const int RING_COUNT_REQUIRED = 4;
const int RING_THRESHOLD = 300;
const long RING_WINDOW_MS = 3000;
const long RING_COOLDOWN_MS = 5000;

WiFiMulti wifiMulti;
NetworkClient network;

int ringCount = 0;
long firstRingTime = 0;
long lastDetectedAt = 0;

volatile bool ringPulseDetected = false;  // флаг из прерывания

void IRAM_ATTR onRingPulse() {
  ringPulseDetected = true;
}

void connectToWifi() {
  wifiMulti.addAP(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Connecting to WiFi");
  while (wifiMulti.run() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println();
  Serial.print("WiFi connected, IP: ");
  Serial.println(WiFi.localIP());
}

void connectToServer() {
  Serial.print("Connecting to server ");
  Serial.print(DEVICE_SERVER_HOST);
  Serial.print(":");
  Serial.println(DEVICE_SERVER_PORT);

  while (!network.connect(DEVICE_SERVER_HOST, DEVICE_SERVER_PORT)) {
    Serial.println("Connection failed, retry in 5s...");
    delay(5000);
  }
  Serial.println("Connected to server!");
}

void ensureConnected() {
  if (!network.connected()) {
    Serial.println("Lost connection, reconnecting...");
    network.stop();
    connectToServer();
  }
}

void sendRing() {
  ensureConnected();
  network.println("E:RING");
  Serial.println("Sent: E:RING");
  digitalWrite(BUILDIN_LED, HIGH);
  delay(200);
  digitalWrite(BUILDIN_LED, LOW);
}

// Детектор звука домофона
void handleRingPulse() {
  ringPulseDetected = false;
  long now = millis();

  if (now - lastDetectedAt < RING_COOLDOWN_MS) return;

  if (ringCount == 0) firstRingTime = now;

  ringCount++;
  Serial.print("Ring pulse #");
  Serial.println(ringCount);

  if (ringCount >= RING_COUNT_REQUIRED) {
    if (now - firstRingTime <= RING_WINDOW_MS) {
      Serial.println("Doorphone ring detected");
      sendRing();
      lastDetectedAt = now;
    }
    ringCount = 0;
    firstRingTime = 0;
    return;
  }

  // Сброс если окно звонка истекло
  if (now - firstRingTime > RING_WINDOW_MS) {
    Serial.println("Window expired, reset");
    ringCount = 0;
    firstRingTime = 0;
  }
}

// Обработка команд от сервера
void handleServerCommands() {
  if (!network.available()) return;

  String cmd = network.readStringUntil('\n');
  cmd.trim();
  Serial.print("Received: ");
  Serial.println(cmd);

  if (cmd == "C:PING") {
    network.println("R:PONG");
    Serial.println("Sent: PONG");
  }
}

void setup() {
  Serial.begin(115200);
  pinMode(BUILDIN_LED, OUTPUT);
  pinMode(SOUND_PIN_DIGITAL, INPUT);

  // Прерывание на цифровой выход KY-038
  attachInterrupt(digitalPinToInterrupt(SOUND_PIN_DIGITAL), onRingPulse, RISING);

    connectToWifi();
    connectToServer();
}

void loop() {
    if (ringPulseDetected) handleRingPulse();
    handleServerCommands();
}
