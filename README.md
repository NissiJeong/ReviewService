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
1) **Dcoker 설정**<br>
   해당 프로젝트는 구현에 필요한 서비스가 3개였다. 스프링부트 백엔드 서버, 데이터 저장을 위한 MySQL, 동시성 처리 및 빠른 데이터 처리를 위한 Redis.<br>
   3개의 서비스 컨테이너를 한번에 도커에서 구동하기 위하여 docker-compose.yml에 해당 내용을 설정해줬다. 개발을 진행하다보니 Spring appliction 은 코드가 바뀔때마다 재실행을 해줘야 하고 MySQL 및 Redis 는 굳이 재실행이 필요하지 않아서 이 부분은 docker-compose.yml 파일을 분리시키는 것으로 수정예정.
   <br>
3) **review 저장 시 product가 존재하지 않는 경우 처리**<br>
   개발 및 테스트의 용이성을 위하여 hibernate ddl-auto 옵션을 create로 설정하였고 11개의 product 데이터가 스프링 부트 실행 시 데이터가 입력이 되도록 설정하였다.<br>
   또한, User 엔티티와 테이블은 따로 구현하지 않았고 요청이 들어오는 모든 사용자는 인증 및 인가가 완료된 사용자라고 가정 후 개발을 진행.
   ```
   spring.jpa.hibernate.ddl-auto=create
   spring.sql.init.mode=always
   spring.sql.init.data-locations=classpath:data.sql
   ```
   <br>
4) **리뷰 카운트 및 스코어 저장**<br>
   동시성 처리를 고려하기 전 리뷰 등록 기능에서 MySQL 에 매번 product의 리뷰수와 스코어를 계산하여 업데이트를 처리해줬다. jpa의 dirty checking 장점을 살리기 위해 한 트랜잭션 안에서 리뷰 저장과 동시에 product 의 리뷰수와 스코어를 저장했다. 아래의 동시성 처리 부분에서 더 자세히 다루겠지만 동시에 많은 요청이 들어올 때는 트랜잭션 안에서 flush 처리까지 완료가 되어야 하는데 그렇게 되면 서버와 database 에 부하가 심할 것이라고 판단하여 수정하였다.
   ```java
   Product product = productRepository.findById(productId).orElseThrow(() ->
      new NullPointerException("해당 상품을 찾을 수 없습니다.")
   );
   // 리뷰 저장 후 프로덕트 리뷰수, 스코어 업데이트
   // 리뷰수는  +1 하면 되고 스코어 = ((스코어*리뷰수) + 입력된 스코어) / 리뷰수 + 1);
   double newScore = (product.getScore()*product.getReviewCount()+ requestDto.getScore())/(product.getReviewCount()+1);
   product.update(product.getReviewCount()+1, newScore);
   ```
6) 동시성 처리
