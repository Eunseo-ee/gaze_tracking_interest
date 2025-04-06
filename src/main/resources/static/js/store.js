// 현재 페이지 URL에서 storeCode 추출
const pathParts = window.location.pathname.split("/");
const storeCode = pathParts[pathParts.length - 1]; // 예: 'A001'

console.log("접속한 storeCode:", storeCode);
console.log()

// 필요한 곳에 storeCode를 사용해 fetch 등에 전달할 수 있음
window.getStoreCode = () => storeCode; // 전역에서 사용 가능