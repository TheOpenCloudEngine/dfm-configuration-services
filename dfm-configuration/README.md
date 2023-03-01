# DFM Configuration

## NiFi Dependency

Maven Archetype으로 NiFi Controller Service를 생성했을 때 다음의 Dependency를 추가하도록 합니다.

```xml
<dependency>
    <groupId>org.apache.nifi</groupId>
    <artifactId>nifi-utils</artifactId>
    <version>1.19.1</version>
    <scope>provided</scope>
</dependency>
```