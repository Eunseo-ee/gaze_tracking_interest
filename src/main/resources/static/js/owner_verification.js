function showBizVerification() {
    document.getElementById("bizVerificationForm").style.display = "block";
}

function submitPassword() {
    const password = document.getElementById("passwordInput").value;
    const storeCode = getStoreCodeFromURL();

    fetch("/api/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            storeCode: getStoreCodeFromURL(),  // 추출한 storeCode 사용
            password: password
        })
    }).then(res => {
        if (res.ok) {
            window.location.href = `/store/${storeCode}/owner_dashboard`;
        } else {
            alert("비밀번호가 틀렸습니다");
        }
    });
}

function getStoreCodeFromURL() {
    const pathParts = window.location.pathname.split("/");

    // URL: /store/A001/owner → ['', 'store', 'A001', 'owner']
    //           0      1         2        3

    if (pathParts.length >= 3 && pathParts[1] === "store") {
        return pathParts[2]; // 'A001'
    }

    return null;
}

function verifyBiz() {
    const input = document.getElementById("bizNumInput");
    if (!input) {
        console.error("❌ bizNumInput 요소를 찾을 수 없습니다.");
        return;
    }

    const inputBizNum = input.value;
    const storeCode = getStoreCodeFromURL();  // ✅ 여기서 storeCode 추출

    if (!storeCode) {
        alert("storeCode를 URL에서 찾을 수 없습니다.");
        return;
    }

    fetch(`/api/verify-biznum?storeCode=${storeCode}&inputBizNum=${inputBizNum}`)
        .then(res => res.json())
        .then(data => {
            if (data.verified) {
                document.getElementById("passwordSetForm").style.display = "block"; // ✅ 비번폼 보여주기
            } else {
                alert("사업자번호가 일치하지 않습니다.");
            }
        })
        .catch(err => {
            console.error("요청 실패:", err);
        });
}

function savePassword() {
    const newPassword = document.getElementById("newPassword").value;

    fetch('/api/set-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            storeCode: getStoreCodeFromURL(),
            password: newPassword
        })
    })
        .then(response => response.text())
        .then(msg => {
            document.getElementById("savePasswordMsg").innerText = "비밀번호가 저장되었습니다!";
            alert("이제부터 비밀번호로 로그인하실 수 있습니다.");
        });
}
