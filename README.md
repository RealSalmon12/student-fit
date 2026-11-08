# 스튜던트 핏 (StudentFit)

학생을 위한 체형 교정 운동 안드로이드 앱이에요. 갤럭시 S25와 갤럭시 워치 8을 기준으로 만들어요.

## 기술 스택
- Kotlin + Jetpack Compose (Material 3)
- minSdk 26 / targetSdk 35

## 진행 단계
| 단계 | 내용 | 상태 |
|---|---|---|
| 1 | 앱 뼈대, 화면 이동, 체형 직접 입력과 BMI·자세 분류 | ✅ |
| 2 | 카메라 체형 인식 (ML Kit 포즈 인식) | 준비 중 |
| 3 | 체형별 교정 운동 추천과 3D 동작 예시 | 준비 중 |
| 4 | 갤럭시 워치 연동 (Health Connect) | 준비 중 |

## 폴더 구조
```
app/src/main/java/com/realsalmon/studentfit/
├── domain/      체형 정보 모델과 분류 로직 (안드로이드 없이 테스트 가능)
├── data/        기기 안 저장소
└── ui/          화면 (home, bodytype, comingsoon, navigation, theme)
```

## 실행 방법
1. Android Studio에서 이 폴더를 열어요.
2. 갤럭시 S25를 USB로 연결하고 개발자 옵션의 USB 디버깅을 켜요.
3. ▶ 실행을 누르면 폰에 설치돼요.

GitHub Actions가 push할 때마다 테스트를 돌리고 디버그 APK를 만들어요. Actions 탭의 실행 결과에서 `student-fit-debug-apk`를 내려받아 폰에 바로 설치할 수도 있어요.

체형 정보는 폰 안에만 저장되고 서버로 보내지 않아요.
