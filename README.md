# 요구사항
1. jpa 변경감지를 통해 업데이트를 진행한다.
2. jpa 내에 상태변경 메서드를 만들지 않는다.
3. 무분별한 setter 사용 방지를 위해 setter 를 만들지 않는다.
---
# UpdateSupport 의 updateObject() 메서드에 대한설명
- 
1. 업데이트될 값을 가진 대상 변수(Resource) 위에 @UpdateColumn 어노테이션을 적용해서 업데이트 되어야할 대상(Target)의 필드명을 updateFieldName 값에 적어준다.
2. 이렇게 적용한 updateFieldName 은 key 값이 되어 맵에 저장되고, @UpdateColumn 어노테이 업데이트에 필요한 값이 value 에 저장된다.
3. 업데이트 되어야할 대상(Target)은 이 맵에서 자신의 변수명으로 값을꺼내 값을 변경한다.

[블로그에 자세히 적은 설명](https://hooneats.tistory.com/48)
