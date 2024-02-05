# 설치 가이드

필요 소프트웨어

1. [eGovFramework 3.10](#1. eGovFramework 3.10 설치 및 실행)
2. [Apache Tomcat 9](#2. Apache Tomcat 9 설치)
3. [JDK 8 / JavaSE 1.8](#3. JDK 1.8 설치)
4. [Tesseract OCR 5.3.x](#4. Tesseract OCR 5.3.x 설치)
5. 

## 1. eGovFramework 3.10 설치 및 실행

해당 링크를 통해, 자신의 OS에 알맞게 전자정부프레임워크 3.10을 다운로드 받고 압축을 해제합니다.

[https://www.egovframe.go.kr/home/sub.do?menuNo=41](https://www.egovframe.go.kr/home/sub.do?menuNo=41)

![1707120887965](image/install/1707120887965.png)

### Window 환경

exe  파일을 실행 후 원하는 위치에 압축 해제 후 eclipse 풀더에서 eclipse 실행

![1707121092150](image/install/1707121092150.png)

### Mac 환경

용용 프로그램 풀더로 옮긴 후 실행

![1707121180341](image/install/1707121180341.png)

## 2. Apache Tomcat 9 설치

해당 링크를 통해, 자신의 OS에 알맞게 Apache Tomcat 9 압축 파일을 받고, 원하는 위치에 압축 해제를 합니다.

[https://tomcat.apache.org/download-90.cgi](https://tomcat.apache.org/download-90.cgi)

![1707127140500](image/install/1707127140500.png)

### Eclipse에서 Tomcat 서버 설정

임의의 프로젝트 생성 또는 OCR_summary 프로젝트를 import

이후 Package Explorer에서 우클릭 -> Run As -> Run on Server

![1707128028331](image/install/1707128028331.png)

Manually define a new server -> Tomcat v9.0 Server 선택 ->
Server runtime environment가 Apache Tomcat v9.0 확인. 아닐 경우 Add를 눌러, 이전에 압축 해제한 Tomcat 9의 풀더 위치를 지정

![1707128244333](image/install/1707128244333.png)

## 3. JDK 1.8 설치

해당 링크를 통해, 자신의 OS에 알맞게 JDK 1.8을 다운로드 받고 설치합니다.

[https://www.oracle.com/java/technologies/downloads/#java8](https://www.oracle.com/java/technologies/downloads/#java8)

## 4. Tesseract OCR 5.3.x 설치

### Window 환경

해당 링크에서 Tesseract OCR 설치 파일을 받고 실행합니다.

[https://github.com/UB-Mannheim/tesseract/wiki](https://github.com/UB-Mannheim/tesseract/wiki)

![1707129164260](image/install/1707129164260.png)

OK
![1707130088551](image/install/1707130088551.png)

Next
![1707130107175](image/install/1707130107175.png)

I Agree
![1707130124034](image/install/1707130124034.png)

Next
![1707130141765](image/install/1707130141765.png)

Additional script data -> Hangul script, Hangul vertical script 선택
![1707130216327](image/install/1707130216327.png)

Additional language data -> Korean 선택
![1707130278220](image/install/1707130278220.png)

Next
![1707130319143](image/install/1707130319143.png)

Install
![1707130327361](https://file+.vscode-resource.vscode-cdn.net/c%3A/GitHub/OCR_summary/docs/image/install/1707130327361.png)


### Mac 환경
