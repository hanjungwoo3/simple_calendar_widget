# Android Calendar Widget (캘린더 위젯)

구글 캘린더의 일정을 투명한 배경에 작은 글씨로 표시하는 Android 위젯입니다.

## 기능

- 📅 오늘부터 3개월까지의 일정 표시
- 🔍 날짜, 시간, 제목을 간단하게 표시
- 👆 클릭하면 구글 캘린더 앱으로 연결
- 🎨 투명한 배경과 작은 글씨
- 📱 리사이즈 가능한 위젯
- 🟢 오늘 날짜 일정은 녹색 배경으로 강조
- 🎯 날짜별로 회색/투명 배경 교대 표시로 구분
- 🔗 앱 내에서 현재 버전 확인 및 GitHub 릴리즈 페이지 바로가기

## 설치 방법

### 옵션 1: GitHub Release에서 다운로드 (권장)

1. [Releases 페이지](https://github.com/hanjungwoo3/simple_calendar_widget/releases)에서 최신 APK 다운로드 (현재 최신: [v1.0.5](https://github.com/hanjungwoo3/simple_calendar_widget/releases/tag/v1.0.5))
2. 다운로드한 `app-debug.apk` 파일을 Android 기기에 설치
3. "알 수 없는 출처" 앱 설치 허용 필요

### 옵션 2: 직접 빌드

1. Android Studio에서 프로젝트 열기
2. 실제 Android 기기 또는 에뮬레이터에 연결
3. Run 버튼 클릭하여 앱 설치

### 옵션 3: 명령줄에서 빌드

```bash
# 빌드
./gradlew assembleDebug

# 설치 (adb 필요)
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

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

## 개발자를 위한 정보

### GitHub Actions 자동 빌드 & 릴리스

이 프로젝트는 GitHub Actions를 사용하여 자동으로 APK를 빌드하고 릴리스합니다.

#### 릴리스 만들기 (완전 자동 배포)

1. **build.gradle에서 버전 업데이트**
   ```gradle
   defaultConfig {
       versionCode 7
       versionName "1.0.7"
   }
   ```

2. **변경사항 커밋 및 푸시 (이게 끝!)**
   ```bash
   git add app/build.gradle
   git commit -m "Bump version to 1.0.7"
   git push origin main
   ```

3. **자동 처리 (GitHub Actions가 알아서!)**
   - `build.gradle`에서 자동으로 버전 추출
   - 해당 버전의 태그가 없으면 자동 생성 (예: `v1.0.7`)
   - APK 자동 빌드
   - Release 자동 생성 및 APK 업로드
   - APK 파일명: `calendar-widget-v1.0.7.apk`

**더 이상 수동으로 태그를 만들 필요가 없습니다!** 🎉

#### 워크플로우 파일

`.github/workflows/release.yml` 파일이 빌드 프로세스를 정의합니다.

- **트리거**: 
  - `app/build.gradle` 파일이 변경되어 main 브랜치에 푸시될 때 자동 실행
  
- **작동 방식**:
  1. `build.gradle`에서 `versionName` 추출
  2. 해당 버전의 태그가 이미 있는지 확인
  3. 태그가 없으면 자동으로 생성 및 푸시
  4. APK 빌드 및 Release 생성
  
- **빌드 환경**:
  - Ubuntu Latest
  - JDK 17
  - Android SDK 자동 설치

- **산출물**:
  - Release APK: `calendar-widget-v{version}.apk`

#### 앱 내 버전 확인

앱을 실행하면 현재 설치된 버전을 확인할 수 있습니다:
- 현재 버전 표시
- "GitHub 릴리즈 페이지" 버튼으로 최신 버전 확인 및 다운로드 가능

#### APK 서명하기 (선택사항)

프로덕션 릴리스를 위해 APK에 서명하려면:

1. **키스토어 생성**
   ```bash
   keytool -genkey -v -keystore my-release-key.jks \
     -keyalg RSA -keysize 2048 -validity 10000 \
     -alias my-key-alias
   ```

2. **GitHub Secrets 설정**
   - 저장소 > Settings > Secrets and variables > Actions
   - 다음 secrets 추가:
     - `KEYSTORE_FILE`: Base64로 인코딩된 키스토어 파일
     - `KEYSTORE_PASSWORD`: 키스토어 비밀번호
     - `KEY_ALIAS`: 키 별칭
     - `KEY_PASSWORD`: 키 비밀번호

3. **build.gradle 수정**
   - 서명 설정 추가 필요

### 버전 관리

- 태그 형식: `v1.0.0`, `v1.1.0`, `v2.0.0`
- [Semantic Versioning](https://semver.org/) 사용 권장

## 라이선스

MIT License


