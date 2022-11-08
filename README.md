# jpa-basic
김영한 jpa 일반편

나만의 노트 /


연관관계의 주인을 외래키가 있는곳으로 지정

ManyToOne <-- 연관관계 주인(JoinColumn(name = "외래키 이름"))

OneToMany <-- 읽기만 가능(MappedBy = "주인쪽 값 이름(예제에선 team)")
