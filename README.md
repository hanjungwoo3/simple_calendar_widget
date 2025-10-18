# Android Calendar Widget (캘린더 위젯)

구글 캘린더의 일정을 투명한 배경에 작은 글씨로 표시하는 Android 위젯입니다.

## 기능

- 📅 오늘부터 3개월까지의 일정 표시
- 🔍 날짜, 시간, 제목을 간단하게 표시
- 👆 클릭하면 구글 캘린더 앱으로 연결
- 🎨 투명한 배경과 작은 글씨
- 📱 리사이즈 가능한 위젯

## 설치 방법

1. Android Studio에서 프로젝트 열기
2. 실제 Android 기기 또는 에뮬레이터에 연결
3. Run 버튼 클릭하여 앱 설치

## 위젯 추가 방법

1. 홈 화면에서 빈 공간을 길게 누르기
2. "위젯" 선택
3. "Calendar Widget" 찾아서 추가
4. 원하는 크기로 조정

## 권한 설정

위젯이 캘린더 데이터를 읽으려면 권한이 필요합니다:

1. 설정 > 앱 > Calendar Widget
2. 권한 > 캘린더 권한 허용

## 요구사항

- Android 8.0 (API 26) 이상
- 구글 캘린더 앱 또는 캘린더 데이터

## 기술 스택

- Kotlin
- Android AppWidget
- RemoteViews
- Calendar Provider API

## 파일 구조

```
app/
├── src/main/
│   ├── java/com/calendar/widget/
│   │   ├── MainActivity.kt              # 메인 액티비티 (권한 요청)
│   │   ├── CalendarWidgetProvider.kt    # 위젯 프로바이더
│   │   └── CalendarWidgetService.kt     # 캘린더 데이터 서비스
│   ├── res/
│   │   ├── layout/
│   │   │   ├── activity_main.xml        # 메인 화면 레이아웃
│   │   │   ├── calendar_widget.xml      # 위젯 레이아웃
│   │   │   └── event_item.xml           # 일정 아이템 레이아웃
│   │   ├── xml/
│   │   │   └── calendar_widget_info.xml # 위젯 설정
│   │   └── values/
│   │       └── strings.xml              # 문자열 리소스
│   └── AndroidManifest.xml
└── build.gradle
```

## 위젯 특징

### 투명한 배경
- 배경색: `#00000000` (완전 투명)
- 텍스트에 그림자 효과 추가로 가독성 확보

### 작은 글씨
- 제목: 12sp
- 날짜/시간/제목: 10sp

### 데이터 표시
- 날짜: MM/dd 형식
- 시간: HH:mm 형식 (종일 일정은 "종일"로 표시)
- 제목: 한 줄로 표시 (길면 ... 으로 생략)

### 업데이트
- 자동 업데이트: 30분마다
- 수동 업데이트: 위젯 클릭 시

## 문제 해결

### 위젯에 일정이 표시되지 않는 경우
1. 캘린더 권한이 허용되었는지 확인
2. 구글 캘린더에 실제 일정이 있는지 확인
3. 위젯을 제거하고 다시 추가

### 위젯이 업데이트되지 않는 경우
1. 위젯을 탭하여 수동 업데이트
2. 기기 재부팅

## 라이선스

MIT License


