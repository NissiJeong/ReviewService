# 💻 리뷰 서비스
## 일정 계획
|12.11(수)|12.12(목)|12.13(금)|
|:---:|:---:|:---:|
|도커,스프링 기본세팅<br>기본구현 완료|테스트 코드 작성<br>동시성 처리 구현|readme 작성|

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
## 💡 프로젝트 설명 및 기술적 요구사항
1) 프로젝트 설명
   - 상품에 대한 review 를 작성하고, 상품별 review 점수, 개수, 그리고 리뷰 내용을 관리한다.
2) 기술적 요구사항
   - MySQL 조회 시 인덱스를 잘 탈 수 있게 설계
   - 상품 테이블에 reviewCount 와 score가 정상 반영
   - (Optional) 동시성을 고려한 설계. 많은 유저들이 동시에 리뷰를 작성할 때, 발생할 수 있는 문제를 고려
   - (Optional) 테스트 코드를 작성
## 💡 리뷰 서비스 구현 중 고민과 트러블 슈팅
1) **Dcoker 설정**<br>
   해당 프로젝트는 구현에 필요한 서비스가 3개였다. 스프링부트 백엔드 서버, 데이터 저장을 위한 MySQL, 동시성 처리 및 빠른 데이터 처리를 위한 Redis.<br>
   3개의 서비스 컨테이너를 한번에 도커에서 구동하기 위하여 docker-compose.yml에 해당 내용을 설정해줬다. 개발을 진행하다보니 Spring appliction 은 코드가 바뀔때마다 재실행을 해줘야 하고 MySQL 및 Redis 는 굳이 재실행이 필요하지 않아서 이 부분은 docker-compose.yml 파일을 분리시키는 것으로 수정예정.
   <br><br>
2) **review 저장 시 product가 존재하지 않는 경우 처리**<br>
   개발 및 테스트의 용이성을 위하여 hibernate ddl-auto 옵션을 create로 설정하였고 11개의 product 데이터가 스프링 부트 실행 시 데이터가 입력이 되도록 설정하였다.<br>
   또한, User 엔티티와 테이블은 따로 구현하지 않았고 요청이 들어오는 모든 사용자는 인증 및 인가가 완료된 사용자라고 가정 후 개발을 진행.
   ```
   spring.jpa.hibernate.ddl-auto=create
   spring.sql.init.mode=always
   spring.sql.init.data-locations=classpath:data.sql
   ```
   <br>
3) **리뷰 카운트 및 스코어 저장**<br>
   동시성 처리를 고려하기 전 리뷰 등록 기능에서 MySQL 에 매번 product 의 리뷰수와 스코어를 계산하여 업데이트를 처리해줬다. jpa의 dirty checking 장점을 살리기 위해 한 트랜잭션 안에서 리뷰 저장과 동시에 product 의 리뷰수와 스코어를 저장했다. 아래의 동시성 처리 부분에서 더 자세히 다루겠지만 동시에 많은 요청이 들어올 때는 트랜잭션 안에서 flush 처리까지 완료가 되어야 하는데 그렇게 되면 서버와 database 에 부하가 심할 것이라고 판단하여 레디스에 저장하고 MySQL과 동기화 하는 것으로 수정하였다.
   ```java
   Product product = productRepository.findById(productId).orElseThrow(() ->
      new NullPointerException("해당 상품을 찾을 수 없습니다.")
   );
   // 리뷰 저장 후 프로덕트 리뷰수, 스코어 업데이트
   // 리뷰수는  +1 하면 되고 스코어 = ((스코어*리뷰수) + 입력된 스코어) / 리뷰수 + 1);
   double newScore = (product.getScore()*product.getReviewCount()+ requestDto.getScore())/(product.getReviewCount()+1);
   product.update(product.getReviewCount()+1, newScore);
   ```
   <br>
4) **리뷰 카운트+1, 스코어 SUM 기능의 동시성 처리**<br>
- 문제<br>
  동시에 여러 요청이 들어왔을 때, product 의 리뷰 카운트와 스코어가 정확하게 저장되지 않음
- 원인<br>
  하나의 트랜잭션이 처리되고 있을 때, 다른 쓰레드의 요청이 들어올 경우 공유 데이터에 엑세스 하는 경우에 의해서 레이스 컨디션이 발생되고 있기 때문
- 해결<br>
   - Redis의 RedissonLock(pub/sub 방식) + MySQL 저장<br>
     우선, Transaction 의 전파 속성을 Propagation.REQUIRES_NEW 로 하여 트랜잭션을 독립적으로 관리하였고 데이터를 업데이트 하자마자 바로 MySQL 에 저장하기 위하여 jpa 의 SaveAndFlush 메서드를 사용하였다.<br>
     아래와 같이 RedissonLock 을 시도할 때 최대 10초 동안 락을 기다리고 5초 동안 락을 유지하게 처리 후 테스트 코드를 실행하였는데 테스트 실패
     ```java
     lock.tryLock(10, 5, TimeUnit.SECONDS);
     ```
     5초 동안 락을 유지하는 것을 1초로 수정하여 테스트를 했더니 성공
- 1번 해결책의 문제<br>
  위 1번의 로직은 쓰기 작업이 빈번하게 일어난다고 가정했을 때, 성능상 좋지 않고 서버와 MySQL 에 부하를 줄 수 있음.
- 해결<br>
   - Redis의 RedissonLock(pub/sub 방식) + Redis 저장 + MySQL 과 Redis 데이터 주기적 동기화<br>
     리뷰 저장 시 리뷰 자체는 MySQL 에 저장하고 리뷰 카운트와 스코어는 Redis 에 저장하게 하였다. 또한, 1분 단위로 Redis 와 MySQL 을 동기화 하게 하여 데이터 정합성을 유지했다. 데이터 동기화 전 프로덕트와 리뷰를 조회할 경우가 있기 때문에 프로덕트, 리뷰 데이터 조회 시 Redis 에 데이터가 있다면 해당 데이터로 리뷰 카운트와 스코어를 사용하게 했다.<br>
     해당 내용으로 수정 후 2개의 테스트 코드를 작성하여 테스트를 진행하였고 두 개의 테스트 모두 통과 
     - 동시 100개 리뷰 작성 후 product 데이터 조회 시 Redis 의 값으로 리뷰 카운트, 스코어 변경
     - 동시 100개 리뷰 작성 후 product 데이터 조회 전 Redis 와 MySQL 동기화 처리
- **동시성 처리 문제에서 더 고려해야 할 것**
  1. 서버가 의도치 않게 죽을 경우 Redis 와 MySQL 동기화
     
