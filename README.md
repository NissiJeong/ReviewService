# 💻기업과제1. 리뷰 서비스
## 일정 계획
|12.11|12.12|12.13|
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
2) Dockerfile 및 Docker-compose.yml 세팅
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
## 💡 트러블 슈팅
1) 동시성 처리
