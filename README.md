# 👀 시선추적 기반 관심도 분석 & 마케팅 프로그램

개발 기간: **2025.04 ~ 진행 중**  
개발 형태: **팀 프로젝트**  
담당 역할: **웹 개발 전반 (프론트엔드 + 백엔드)**

---

## 📌 프로젝트 소개

**시선추적 기반 관심도 분석 시스템**은  
**소비자의 시선 데이터를 분석하여 상품별 주목도 순위를 시각화**하고,  
이를 통해 **점주는 마케팅 전략 수립**, **손님은 인기 상품 확인 및 구매 편의성 향상**을 경험할 수 있는 웹 기반 솔루션입니다.

---

## ⚙️ 사용 기술

### 🖥 백엔드 - Java Spring Boot
- RESTful API 기반 설계로 **시선 분석, 바코드 처리, 점주 인증 등 서버 로직** 구현
- `Controller-Service-Repository` 구조로 역할 분리
- **경험**: 클라이언트와의 통신 흐름 및 구조적인 백엔드 아키텍처 설계 능력 강화

### 🧩 템플릿 엔진 - Thymeleaf
- 서버 측에서 **초기 HTML을 동적으로 구성**
- Spring에서 전달된 데이터(CSV, 사용자 정보 등)를 템플릿에 바인딩
- **역할 분리**: JS는 인터랙션 처리에 집중, 템플릿은 초기 데이터 렌더링 담당

### 💻 프론트엔드 - JavaScript (Vanilla)
- 실시간 **바코드 인식**, UI 렌더링, 사용자 이벤트 처리
- `fetch()` 기반 서버 통신 구현
- **경험**: 사용자 반응에 따른 동적 UI 제어 및 서버-클라이언트 간 흐름 직접 구현

### 🌐 마크업 - HTML/CSS
- HTML 시맨틱 태그를 기반으로 **명확한 구조 설계**
- Flex/Grid로 테이블 중심의 UI와 반응형 버튼 레이아웃 구현
- **경험**: 사용자 중심의 실서비스 수준 UI 설계 능력 향상

---

## 🔑 주요 기능

### 📊 메인 랭킹 페이지
- **1시간 주기 자동 업로드된 CSV 파일 중 최신 데이터 표시**
- 상품 카테고리 및 가격 기반 **필터링 기능 제공**
- 테이블 형태로 **주목도 순위 시각화**

### 🎯 프로모션 페이지
- 페이지 진입 시 **카메라 자동 실행 및 바코드 인식**
- *(예정)* 해당 바코드에 연동된 **진행 중인 프로모션 정보 표시**

### 🛠 점주 페이지
- 점주 **인증 및 비밀번호 설정 기능**
- 로그인 후 점주 전용 대시보드 제공:
  - 시선 분석이 적용된 **비디오 목록 확인**
  - 상품별 주목도 순위 **CSV 다운로드**

---

## 🏗 시스템 아키텍처
![Image](https://github.com/user-attachments/assets/bfc43480-4249-4bf9-8c80-75b6d9c3da86)


---

### 스크린샷 이미지

- 메인 랭킹 페이지
  
  ![Image](https://github.com/user-attachments/assets/c0fb2d91-1fdd-4a8b-bf28-a1102c067bc1)
- 프로모션 페이지
  
  ![Image](https://github.com/user-attachments/assets/0d6f1a6f-af50-419e-aad6-e098671fc010)
- 점주 인증 페이지
  
  ![Image](https://github.com/user-attachments/assets/a5143f23-0ff6-497b-9699-3478c08f71b9)
- 점주 대시보드 페이지
  
  ![Image](https://github.com/user-attachments/assets/4fa1b392-7e34-4385-ab3d-e12c88ce47f0)
