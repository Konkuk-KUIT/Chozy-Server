## Chozy BE Repository

### Info
- Java 17, Spring boot 3.4.1, JPA
- DB: MySQL
- Infra: EC2, Docker Compose, nginx, RDS

### Commit Convention
- [Feat]: 새로운 기능 추가
- [Chore]: 설정 정보 수정, 기능과 무관 
- [Fix]: 오류 수정
- [Refactor]: 기능 변화 없는 코드 구조 개선
- [Test]: 테스트 코드 관련 
- [Docs]: 문서 수정 (readme, 주석 등) 


### Branch Convention
1. issue 생성
2. branch 생성
   - `feat/00-{short-description}`: 기능 구현
   - `fix/00-{short-description}`: 오류 수정
   - `refactor/00-{short-description}`: 리팩토링
4. PR 시 issue 연결
   - ex: `closes #00`
