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

## Processor에서 사용하기

Processor에서 이 Controller Service를 호출하기 위해서 Maven POM에 API를 dependency로 추가하도록 합니다.

## 사용상 제약사항

* Configuration 파일을 로딩한 후 캐슁을 진행하므로 캐슁된 내용을 변경하고자 하는 경우
    * Configuration Service를 재시작하거나
    * `org.opencloudengine.dfm.services.configuration.ConfigurationService.reload()` 메소드를 호출한다.

