const html5QrCode = new Html5Qrcode("reader");
const config = { fps: 10, qrbox: 250 }; // 바코드 박스 사이즈

html5QrCode.start(
    { facingMode: "environment" }, // 후면 카메라
    config,
    (decodedText, decodedResult) => {
        alert("📦 바코드 인식됨: " + decodedText);
        html5QrCode.stop();
    },
    (errorMessage) => {
        // 인식 실패시 로그 (선택)
        console.warn("스캔 실패:", errorMessage);
    }
);