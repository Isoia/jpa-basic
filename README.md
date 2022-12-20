# jpa-basic
김영한 jpa 일반편

나만의 노트 /


연관관계의 주인을 외래키가 있는곳으로 지정

ManyToOne <-- 연관관계 주인(JoinColumn(name = "외래키 이름"))

OneToMany <-- 읽기만 가능(MappedBy = "주인쪽 값 이름(예제에선 team)")(주인이 아닌쪽은 읽기만 가능 쓰기 불가능)

양방향보다는 단방향으로 설계하는게 훨씬 좋음!(설계할때 단방향으로 하고 나중에 양방향을 추가해도 코드에 문제X)

ManyToMany, ManyToOne는 JoinColumn사용

OneToOne, OneToMany는 mapped by 사용

다대다는 실무에서 사용하면 안됨! 

ManyToOne에선 항상 다쪽에 외래키가 있어야함, 다대일은 가장 많이 사용함, 양방향을 사용해도 반대쪽은 읽기만 가능하기에 테이블에 영향x

일대일 관계는 외래키를 아무곳에서나 넣을 수 있음 (맴버랑 팀이 있으면 맴버나 팀에 외래키 주입 가능), 주 테이블에 외래키 or 대상 테이블에 외래키

일대일 단방향이나 양방향 관계는 다대일 단방향 양방향 관계와 비슷하다 (외래키 주입, 반대편 mapped by)

상속관계 매핑 : 객체의 상속과 구조와 DB의 슈퍼타입 서브타입 관계를 매핑

각각의 테이블로 변환하는 조인전략, 한 테이블로 변환하는(싱글테이블) 단일테이블전략, 서브타입 테이블로 변환하는 구현 클래스마다 테이블 전략이 있음

부모 Entity에 어노테이션 @Inheritance 추가 (strategy 중 JOINED(조인), SINGLE_TABLE(단일테이블)(기본값), TABLE_PER_CLASS(구현테이블마다)

@DiscriminatorColumn는 DTYPE을 부모 Entity에 생성하고 DTYPE이 무슨 자식객체인지 값을 넣어서 알려줌

@DiscriminatorValue("a")는 자식 Entity에 추가하는 것으로 부모 DTYPE의 값을 @DiscriminatorValue("a")의 'a'의 값으로 들어감

@DiscriminatorColumn는 싱글테이블전략에 무조건 들어가야함(그래서 단일테이블은 @DiscriminatorColumn가 들어가지 않아도 DTYPE이 필수로 들어감)

단일테이블의 치명적인 단점은 찾는 entity의 컬럼을 제외한 나머지 서브 entity의 값에 null허용을 해야함!

구현테이블 전략은 쓰면 안되는 전략!

비즈니스적으로 중요하면 조인 전략, 단순하면 단일테이블 전략을 주로 사용

@MappedSuperClass 공통 매핑 정보가 필요할 때 사용(id, name)

여러개의 중복되는 컬럼들을 클래스 하나를 생성하여 해당 컬럼들을 입력 후 @MappedSuperClass 입력, 이후 해당 컬럼이 필요한 클래스에 extends 클래스이름 을 입력하면 됨

@MappedSuperclass
public class BaseEntity{
    private String createBy;
    private LocalDateTime createdDate;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;
    }
public class Member extends BaseEntity

MappedSuperClass는 상속관계 매핑이 아님, 엔티티도 아니기에 테이블과 메핑이 불가능! (em.find(BaseEntity.class) 사용 불가능!)

참고 @Entity는 엔티티나 MappedSuperClass로 지정한 클래스만 상속이 가능!

--------프록시--------

ex) Member를 호출할때 Team도 같이 호출 해야할까?

Member만 필요한데 Team도 같이 호출될때 지연로딩으로 해결할 수 있다.

jpa는 em.find말고 em.getReference()라는 참조를 가져오는 메소드가 제공되고있음.

find는 데이터베이스를 통해 실제 엔티티 객체를 조회, 반면 getReference는 데이버테이스 조회를 미루는 가짜 엔티티 객체 조회(db에 쿼리가 안나가는데 객체가 조회됨)
ex) Member findMember = em.getReference(Member.class, member.getId());

getReference를 호출하는 시점에는 db에 쿼리를 보내지 않음

Member findMember = em.getReference(Member.class, member.getId());
System.out.println("findMember.getId() = " + findMember.getId());
System.out.println("findMember.getName() = " + findMember.getName());

getId를 할때는 select 쿼리문이 호출되지 않음, getId는 getReference를 할때 이미 존재하는 값을 사용하였기 때문에 select문 호출이 안됨(getReference에서 getId를 사용하였기 때문)
getName은 db에 있는 값이다, getId의 경우 db에서 가지고 오지 않아도 값을 알 수 있지만 getName은 값이 없기에 이때 System.out.println("findMember.getName() = " + findMember.getName());호출 시점에 jpa가 select문을 실행

getReference로 호출 후 System.out.println("findMember.getClass() = " + findMember.getClass());를 호출해보면 
findMember.getClass() = class hellojpa.Member$HibernateProxy$rM5X1Nqx 라고 나오는걸 볼 수 있다. 이건 하이버네이트가 만든 가짜클래스(프록시클래스)이다.
getReference로 생성하면 껍데기는 똑같은데 안의 값은 비었다 ex)getId(), getName() 그리고 target이 있는데 이게 진짜 레퍼런스를 가르킴(처음에는 비어있음)

프록시의 특징
실제 entity를 상속받아서 만들어짐, 그래서 겉 모양이 같다. 사용하는 입장에선 진짜 객체인지 프록시 객체인지 구분하지 않고 사용하면 됨
프록시 객체는 실제 객체의 참조(target)를 보관, ex) getName를 호출하면 target이 가르키는 실제 target에있는 getName을 호출

프록시 객체의 초기화
getName을 호출 -> target에 값이 없으니 jpa가 영속성 컨텍스트에 진짜 맴버를 가져오라고 호출 -> db에 실제 entity 객체를 생성해서 프록시 내부에 있는 target에 방금 생성한 진짜 entity객체를 연결해줌 -> getName을 호출할떄 target의 연결되있는 진짜 Member의 getName이 반환됨(target.getName())

프록시 객체는 처음 사용할 때 한번만 초기화
프록시 객체가 초기화 할때 프록시 객체가 실제 엔티티로 바뀌는게 아님 초기화 되면 프록시 객체를 통해 실제 엔티티에 접근 가능
프록시 객체는 원본 엔티티를 상속받음
영속성 컨텍스트에 찾는 엔티티가 이미 있으면 em.getReference를 호출해도 실제 엔티티를 반환
em.gerReference로 프록시를 한번 반환하면 이후에 em.find를 호출해도 entity가 반환되는것이 아닌 프록시가 반환
영속성 컨텍스트의 도움을 받을 수 없는 준영속 상태일 때 프록시를 초기화 하면 문제 발생 

----지연로딩(LAZY)----(즉시로딩 : EAGER)

@ManyToOne(fetch = FetchType.LAZY) 이렇게 해주면 LAZY가 해당된 엔티티는 조회가 되지 않음(Member의 경우 Team이 같이 조회되지 않고 Member만 조회됨)
호출해봤자 지연로딩이 걸린 엔티티는 나오지 않고 getClass()를 해보면 프록시 객체로 조회가 됨
만약 LAZY를 건 엔티티를 실제로 사용하는 시점에서 지연로딩이 걸린 entity에 프록시 객체가 초기화되면서 db에서 조회해서 가져옴(엔티티로 변함)

실무에서는 즉시로딩을 사용x(가급적 지연로딩만 사용)
즉시로딩을 사용하면 예상치 못한 sql문이 발생(모든 컬럼을 다 가져오기 때문에 나중에 테이블이 쌓이면 불러오는 값이 너무 많아져서 성능이 떨어짐)
@ManyToOne과 @OneToOne은 기본이 즉시로딩이므로 꼭 (fetch = FetchType.LAZY) 적용하기!!!
@OneToMany, @ManyToMany는 기본이 지연로딩이다

----영속성 전이: CASCADE ----

특정 엔티티를 영속 상태로 만들 때 연관된 엔티티도 함께 영속 상태로 만들고 싶을때 사용(삭제도 가능)
ex) 부모 엔티티를 저장할 때 자식 엔티티도 함께 저장

부모쪽 @OneToMany(cascade = CascadeType.ALL)속성을 추가해준다!
그러면 부모쪽에 연관되어있는(parent.addChild(child1); parent.addChild(child2);) 밑에있는 컬럼들에도 persist를 시켜버림
엔티티를 영속화할 때 연관된 엔티티도 함께 영속화 하는 편리함을 제공
엮을려는 엔티티가 부모와 자식 한가지의 관계가 아닌 다른 관계로도 엮여 있으면 엮인 관계까지 같이 실행시키므로 잘 보고 사용!

---- 고아 객체 ----
부모 엔티티와 연관관계가 끊긴 자식 엔티티를 자동으로 삭제하는 기능 @OneToMany(orphanRemoval = true)
주의사항 : 참조하는 곳이 하나일 때 사용해야함!, 특정 엔티티가 개인 소유할 때 사용(Parent가 child를 혼자 사용할 때)

cascade = CascadeType.ALL, orphanRemoval = true 둘의 옵션을 활성화 시키면 부모 엔티티를 사용해 자식 엔티티의 생명주기를 관리 가능(영속화, 제거)

---- 기본값 타입 -----

jpa는 데이터타입을 엔티티타입과 값타입으로 분류함
엔티티타입 : @Entity로 정의한 객체, 데이터가 변해도 식별자(id)로 추적 가능
값타입 : int,String처럼 단순한 값으로 사용하는 자바 기본타입or객체, 식별자가 없고 값만 있어 변경시 추적 불가

값타입 분류 : 기본값타입, 임베디드타입, 컬렉션 값 타입
기본값 타입 : 자바 기본타입(int,double) 래퍼클래스 (Integer, Long) String
    생명주기를 엔티티에 의존, 값타입은 공유x

임베디드 타입
@Embeddable : 값 타입을 정의하는 곳에 표시
@Embedded : 값 타입을 사용하는 곳에 표시
기본생성자 필수

@Embeddable
public class Period {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}

사용할 @Entity 안에서
@Embedded
    private Period workPeriod;
 만약 한 엔티티에서 같은 값 타입을 사용하면?(컬럼이 중복)
 @AttributeOverrides 사용
    
---- 값타입과 불변 객체 ----

임베디드같은 값타입을 여러 엔티티에서 공유하면 위험함
Address address = new Address("city");
이 address를 member1, member2에 넣었다고 쳤을 때 member1.getAddress()를 치면 member2도 같이 변경
대신 값을 복사해서 사용 Address newAddress = new Address(address.getCity);
member1.setAddress(newAddress.getCity);를 사용

---- 값 타입 컬렉션 ----

값 타입을 하나 이상 저장할때 사용, @ElementCollection, @CollectionTable 사용,같은 테이블에 저장할 수 없어 1:n 방식으로 저장(별도의 테이블이 필요)
값 타입 컬렉션은 기본적으로 지연로딩으로 설정되어있다
값 타입 컬렉션을 수정하면 해당 테이블의 모든 값을 삭제하고 조회할려는 아이디의 값만 살려냄(사용x)
그러니 Entity를 만들어서 해당 테이블과 양방향 매핑을 해주어야함

---- jpql ----

테이블이 아닌 객체를 대상으로 검색하는 객체 지향 쿼리
select m from Member m where m.age > 18; 여기서 Member는 테이블이 아닌 객체이다
QueryDSL을 할줄 알면 sql을 짜기 쉬워진다!(jsql만 잘하면 querysql과 문법과 같기 떄문에 jpql을 잘하자!)

jqpl은 테이블이 아닌 엔티티 객체를 대상으로 쿼리한다.

jpql은 sql을 추상화해서 특정 데이터베이스 sql에 의존하지 않는다

jpql은 결국 sql로 변환된다.

jpql로 쿼리문을 짤떄 테이블 명이 아닌 엔티티 이름을 사용해야함

별칭은 필수 (select m from Member as m 에서 m (as는 스킵 가능))

TypeQuery : 반환타입이 명확할때 사용
Query : 반환타입이 명확하지 않을때 사용

TypedQuery<Member> query1 = em.createQuery("select m from Member as m ", Member.class) 반환타입을 적어야 하기에 쿼리문 다음에 Member.class를 작성

TypedQuery<String> query2 = em.createQuery("select m.username from Member as m ", String.class); Member의 이름을 반환하는거기에 String.class 작성

위의 두가지 경우는 반환타입이 중복되지 않고 명확하기 떄문에 TypeQuery라 볼 수 있다

Query query3 = em.createQuery("select m.username, m.age from Member as m ") 이 쿼리문의 경우 username(String)과 age(Integer)가 같이 있기에 반환타입이 명확하지 않아 query를 사용

---- 결과 조회 API -----

List<Member> resultList = query1.getResultList(); query1의 쿼리문에서 getResultList()를 사용하여 List로 값을 조회

TypedQuery<Member> query1 = em.createQuery("select m from Member as m ", Member.class) 으로 만약에 값이 하나만 존재하는 경우에는

query1.getSingleResult()라는 문이 있다. 얘의 반환은 하나이기에 Member result = query1.getSingleResult() 이렇게 된다

getResultList로 결과가 하나 이상일떄 리스트를 반환하고 결과가 없으면 빈 리스트를 반환한다

하지만 getSingleResult는 결과가 없거나 둘 이상이면 Exception을 발생시킨다

고로 getSingleResult는 무조건 결과가 하나 있을때만 사용해야한다!!

---- 파라미터 바인딩 ----

TypedQuery<Member> query1 = em.createQuery("select m from Member as m where m.username = :username", Member.class);
            
query1.setParameter("username", "member1");

where m.username = :username 에서 :username으로 설정하고 query1.setParameter("username", "member1")로 query1에 설정된 setParameter로 "username"의 파라미터값을 "member1"을 대입하겟다 로 설정

em.createQuery("select m from Member m where m.username = :username", Member.class)
    .setParameter("username", "member1")
    .getSingleResult();
    이렇게 묶어서 한번에 사용함
 
 
 --- 프로젝션(select) ---
 
 select 절에 조회할 대상을 지정
 
 프로젝션 대상 : 엔티티, 임베디드 타입, 스칼라 타입
 
 select m from member m -> 엔티티 프로젝션
 select m.team from Member m -> 엔티티 프로젝션
 select m.address from Member m -> 임베디드 타입 프로젝션
 select m.username, m.age from Member m -> 스칼라 타입 프로젝션
 
 em.createQuery("select m.team from Member as m", Team.class)
                .getResultList();
  
 이렇게 받을 경우 Membe.class가 아닌 Team.class으로 받아야한다

스칼라 타입 프로젝션은 받고싶은 타입들을 가져오는 것이다

em.createQuery("select distinct m.username, m.age from Member m"); (distinct는 중복 제거)

기존에 타입이 다른 2개를 조회하면 에러가 나온다고 했다

new 명령어로 조회하면 간단하게 해결 가능

public class MemberDTO {
    private String name;
    private int age;

    public MemberDTO(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}

가져오고 싶은것들을 클래스로 만들고

List<MemberDTO> resultList = em.createQuery("select new jpql.MemberDTO(m.username, m.age) from Member as m", MemberDTO.class)
                    .getResultList();
       
new jpql.클래스이름(출력할것들)을 입력하면 가능

--- 페이징 API ---

JPA는 페이징을 두 API로 추상화
setFirstResult(조회시작위치), setMaxResults(조회할 데이터 수)

List<Member> result = em.createQuery("select m from Member as m order by m.age desc", Member.class)
                    .setFirstResult(0)
                    .setMaxResults(10)
                    .getResultList();
                    
 --- 조인 ---
 내부조인 : select m from Member m [INNER] join m.team t
 외부조인 : select m from Member m left [OUTER] join m.team t
 세타조인 : select count(m) from Member m, Team t where m.username = t.name
 조인 on 사용 : select m, t from Member m  left outer join m.team t on t.name = 'A'
 아웃조인을 하면서 팀의 이름이 'a'인 경우
 
 --- 서브쿼리 ---
 나이가 평균보다 많은 회원
 
 select m from Member m 
 where m.age > (select avg(m2.age) from Member m2)
 
 jpa는 where, having절에서만 서브쿼리 사용 가능
 select 절도 가능(하이버네이트에서 지원)
 from절의 서브쿼리는 현재 jpql에서 불가능
 
 --- jpql 타입 표현 ---
 
 문자 : 'HELLO', 'She''s'
 숫자 : 10L(Long), 10D(Double), 10F(Float)
 Boolean : TRUE,FALSE
 ENUM : jpabook.MemberType.Admin(패키지명 포함해야함)
 엔티티 타입 : TYPE(m) = Member(상속관계에서 사용)
 
 --- 조건식(case...) ---
 
 String query = "select " +
                        "case when m.age <= 10 then '학생요금'" +
                             "when m.age >= 60 then '경료요금'" +
                             "else '일반요금'" +
                        "end "+ (end 이후 띄어쓰기 필수!!!)
                        "from Member m";
            List<String> result = em.createQuery(query, String.class)
                    .getResultList();

em.createQuery("select coalesce(m.username, '이름 없는 회원') from Member m") : 사용자 이름이 없으면 '이름 없는 회원'을 반환

em.createQuery("select nullif(m.username, '관리자') from Member m") : 사용자 이름이 '관리자'면 null을 반환하고 나머지는 본인의 이름을 반환

--- jpql함수 ---
CONCAT, SUBSTRING, TRIM, LOWER UPPER, LENGTH, LOCATE, ABS SQRT MOD, SIZE INDEX
em.createQuery("select concat('a','b') From Member m",String.class)

--- 경로 표현식 ---
.(점)을 찍어 객체 그래프를 탐색하는것
상태필드 : 단순히 값을 저장하기 위한 필드, 경로 탐색의 끝, 탐색x(m.username의 경우는 username이 객체가 아닌 필드이기에 더이상 탐색이 불가능)
연관필드 : 연관관계를 위한 필드
    단일값 연관 필드 : @ManyToOne, OneToOne, 대상이 엔티티(ex: m.team), 묵시적 내부 조인(inner join)발생, 탐색o
    컬렉션값 연관 필드 : @OneToMany, ManyToMany, 대상이 컬렉션(ex.m.orders) 묵시적 내부 조인 발생, 탐색x
실무에서는 묵시적 조인을 사용하지 말것!! 무조건 명시적 조인을 사용해야함

--- jpql 페치조인 ---
실무에서 정말중요!!!
sql 조인 종류x, jqpl에서 성능 최적화를 위해 제공하는 기능, 연관된 엔티티나 컬렉션을 sql 한번에 함께 조회 가능, 'join fetch' 명령어 사용
예시
sql : select M.* T.* from member m inner join team t on m.tead_id = t.id
jpql : select m from Member m join fetch m.team(회원을 조회하는데 팀도 같이 조회하고싶을때 join fetch 조인할 필드(m.team)
join fetch를 사용할떄는 지연로딩이 설정되있어도 무조건 즉시로딩으로 진행됨(@ManyToOne(fetch = FetchType.LAZY)가 무시됨)

컬렉션 페치 조인
컬렉션 조인 시 팀A가 2명 있다고 할때 프린트를 하면 팀A가 2번 호출됨

2팀과 3명의 맴버가 있다는 가정 하에
select t from team t : 결과값 2개 나옴
select t from team t join fetch t.members : 결과값 3개 나옴(내부 조인이 되면서 팀은 2개지만 소속된 인원은 3명이기에 3개가 나옴)

이런 중복을 해결하고 싶을때 select 뒤에 distinct 적용

페치조인과 일반조인의 차이
일반조인은 지연로딩이 걸린건 바로 데이터를 불러오지 않고 데이터를 호출하는 시점에 불러옴
페치조인은 로딩시점에 전부 호출하기 떄문에 지연로딩이 걸려있어도 바로 호출됨

일반조인
jpql은 결과를 반환할 때 연관관계 고려x
단지 select절에 지정된 엔티티만 조회할 뿐
여기서는 팀 엔티티만 조회하고, 회원 엔티티는 조회x

페치조인
페치조인을 사용할 때만 연관된 엔티티도 함께 조회(즉시로딩)
페치 조인은 객체 그래프를 sql 한번에 조회되는 개념

--- 페치 조인의 한계 ---
페치 조인 대상에는 별칭을 줄 수없다.
t.members as m 사용x
둘 이상의 컬렉션은 페치 조인 할 수 없다.
컬렉션을 페치 조인 하면 페이징 API(setFirstResult,setMaxResult) 사용 불가능(일대일,다대일은 사용 가능, 일대다,다대다는 데이터 뻥튀기가 되기에 불가능)

--- 엔티티 직접 사용 ---
jpql에서 엔티티를 직접 사용하면 sql에서 해당 엔티티의 기본키 값을 사용

jpql : select count(m) from member m
sql : select count(m.id) as cnt from member m

select m from member m where m.team = :team 에서 m.team이 가르키는것은 키의 기본키값
