# DFM Configuration Services

AWS, Azure 등의 Public Cloud의 Storage 서비스에서 Configuration 정보를 로딩하여 제공하는 DFM 서비스

## 지원하는 Public Cloud

* AWS
* Azure

## 지원하는 주요 기능

* YAML
* XML
* Properties
* JSON
* Text

## 사용상 제약사항

* Configuration 파일을 로딩한 후 캐슁을 진행하므로 캐슁된 내용을 변경하고자 하는 경우 
  * Configuration Service를 재시작하거나
  * Flow File의 Attribute에 `configuration.service.update`를 `true`로 전송하는 경우 
