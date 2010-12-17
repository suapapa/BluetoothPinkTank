#define PIN_RESET 7
//#define PIN_ID 3
#define PIN_DBG 13

#define LEFT_PWM_A 11
#define LEFT_PIN_B 12
#define RIGHT_PWM_A 9
#define RIGHT_PIN_B 8

void __toqueMotor(int, int, int);
#define toqueLeft(t) __toqueMotor(LEFT_PWM_A, LEFT_PIN_B, t)
#define toqueRight(t) __toqueMotor(RIGHT_PWM_A, RIGHT_PIN_B, t)
int toqL = 0;
int toqR = 0;

#define serialWaitChars(n) while(Serial.available() < n){delay(10);}

void setup(){
  pinMode(LEFT_PWM_A, OUTPUT);
  pinMode(LEFT_PIN_B, OUTPUT);
  pinMode(RIGHT_PWM_A, OUTPUT);
  pinMode(RIGHT_PIN_B, OUTPUT);

  pinMode(PIN_DBG, OUTPUT);
  pinMode(PIN_RESET, OUTPUT);
  //pinMode(PIN_ID, INPUT);
  //digitalWrite(PIN_ID, HIGH);

  Serial.begin(38400);
}

int getMultiple(char direction);

void loop(){
  if (Serial.available() > 1) {
    if (Serial.read() == '#') {
      char cmd_buf[3];
      digitalWrite(PIN_DBG, HIGH);
      serialWaitChars(3);

      cmd_buf[0] = Serial.read();
      cmd_buf[1] = Serial.read();
      cmd_buf[2] = Serial.read();

      if (cmd_buf[0] == 'M') {
        int dL = getMultiple(cmd_buf[1]);
        int dR = getMultiple(cmd_buf[2]);
        toqL = 0;
        toqR = 0;
        if (dL != 0) {
          serialWaitChars(1);
          toqL = dL * Serial.read();
        }
        if (dR != 0) {
          serialWaitChars(1);
          toqR = dR * Serial.read();
        }
        toqueLeft(toqL);
        toqueRight(toqR);
        Serial.println("@"); // ACK
      } else if (cmd_buf[0] == 'S') {
        Serial.print("@");
        Serial.print("Left:"); // ACK with 4 chars
        Serial.print(toqL);
        Serial.print(" Right:");
        Serial.print(toqR);
        Serial.println();
      } else if (cmd_buf[0] == 'R') {
        digitalWrite(PIN_RESET, HIGH);
      }
      delay(10);
      digitalWrite(PIN_DBG, LOW);
    }
  }
}

void __toqueMotor(int pwmA, int pinB, int toq)
{
  if (-255 > toq) toq = 0;
  if (255 < toq) toq = 0;

  if (0 < toq) {
    /* Forward */
    digitalWrite(pinB, LOW);
    analogWrite(pwmA, toq);
  } else if (0 > toq) {
    /* Backward */
    digitalWrite(pinB, HIGH);
    analogWrite(pwmA, 255 + toq);
  } else /*if (0 == toq)*/ {
    /* Stop */
    digitalWrite(pwmA, LOW);
    digitalWrite(pinB, LOW);
  }
}


int getMultiple(char direction)
{
  if (direction == 'F') return 1;
  else if (direction == 'B') return -1;
  return 0;
}
/* vim: set sw=2 et: */
