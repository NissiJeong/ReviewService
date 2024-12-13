# 💻기업과제1. 리뷰 서비스
## 일정 계획
|12.11(수)|12.12(목)|12.13(금)|
|:---:|:---:|:---:|
|기본세팅, 기본구현 완료|동시성 처리 구현|readme 작성|

## 💡 설정 구성
1) Spring Boot 프로젝트 구축
   - Java17
   - Spring Boot 3.3.0
   - Gradle
   - JPA
   - MySQL
   - Redis
2) Dockerfile 및 docker-compose.yml 세팅
   - Spring boot
   - MySQL
   - Redis
## 💡 과제 설명 및 기술적 요구사항
1) 과제 설명
   - 상품에 대한 review 를 작성하고, 상품별 review 점수, 개수, 그리고 리뷰 내용을 관리한다.
2) 기술적 요구사항
   - MySQL 조회 시 인덱스를 잘 탈 수 있게 설계
   - 상품 테이블에 reviewCount 와 score가 정상 반영
   - (Optional) 동시성을 고려한 설계. 많은 유저들이 동시에 리뷰를 작성할 때, 발생할 수 있는 문제를 고려
   - (Optional) 테스트 코드를 작성
## 💡 리뷰 서비스 구현 중 고민과 트러블 슈팅 해결
1) Dcoker 설정
   해당 프로젝트는 구현에 필요한 서비스가 3개였다. 스프링부트 백엔드 서버, 데이터 저장을 위한 MySQL, 동시성 처리 및 빠른 데이터 처리를 위한 Redis.<br>
   3개의 서비스 컨테이너를 한번에 도커에서 구동하기 위하여 docker-compose.yml에 해당 내용을 설정해줬다. 개발을 진행하다보니 Spring appliction 은 코드가 바뀔때마다 재실행을 해줘야 하고 MySQL 및 Redis 는 굳이 재실행을 하지 않아도 될 것 같아서 이 부분은 docker-compose.yml 파일을 분리시키려고 한다.
3) review 저장 시 product가 존재하지 않는 경우 처리
4) 동시성 처리
