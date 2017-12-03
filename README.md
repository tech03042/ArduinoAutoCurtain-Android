# ArduinoAutoCurtain-Android
호서대학교 컴퓨터공학과 2학년 2학기, 컴퓨터구조2, 아두이노 자동 커튼 제어 프로젝트

![구동 스크린샷 이미지](https://raw.githubusercontent.com/tech03042/ArduinoAutoCurtain-Android/master/Screenshot.png)

##### 안드로이드 -> 아두이노 전송 데이터 ( Byte )
데이터 | 기능
--- | ---
0 | 수동 조작 모드
1 | 자동 조작 모드 ( 조도에 따라서 조절 )
2 | 소리 미사용
3 | 소리 사용
4 | 커튼 올림 ( 수동 조작 모드 )
5 | 커튼 내림 ( 수동 조작 모드 )
7 | 커튼 멈춤 ( 수동 조작 모드 )
6 | 초기화
8 ~ 255 | 모터 속도 제어 및 추가 기능 삽입용
