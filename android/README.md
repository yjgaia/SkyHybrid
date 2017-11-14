# Hybrid App - Android
프로젝트 자체를 복사하여 본인의 프로젝트에 맞게 수정하여 사용합니다.

## 구글 서비스 파일 교체
* `app/google-services.json` 파일을 본인의 것으로 교체합니다.

## Android Studio에서 설정
* `bulid.gradle` 파일 내용 중 `applicationId`와 `versionCode`, `versionName`을 알맞게 수정합니다.
* `app/manifests/AndroidManifest.xml` 파일 상단의 `package`을 변경합니다.
* `app/java/co.hanul.hybridapp/Settings` 파일의 `R`을 import 합니다.
* `app/res/values/strings.xml` 파일 내용을 수정합니다. `app_id`는 구글 게임 서비스에서의 ID입니다.
* `app/assets/config.js` 파일을 수정합니다.

## 업데이트 하기
Hybrid App 프로젝트가 업데이트 된 경우 다음 파일들을 복사합니다.
* `app/src/main/assets/java/co/hanul/hybridapp` 폴더 중 `Settings.java`를 제외한 전체
* `app/src/main/assets/Native.js`