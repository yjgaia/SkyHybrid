# Hybrid App - iOS

## 국제화 처리
* `File - New - File - Strings File`로 InfoPlist 파일을 생성합니다.

## Hybrid App 업데이트 하기
Hybrid App 프로젝트가 업데이트 된 경우 다음 파일들을 복사합니다.
* `hybridapp/hybridapp/AppDelegate.swift`
* `hybridapp/hybridapp/ViewController.swift`

## 웹 페이지를 불러와 앱에 표시하는 경우


## Status Bar를 숨기고자 하는 경우
`ViewController.swift` 파일 하단에 다음 코드를 추가합니다.
```swift
override var prefersStatusBarHidden: Bool {
return true
}
```
