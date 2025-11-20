document.getElementById("analyzeBtn").addEventListener("click", async () => {
    const driveLink = document.getElementById("driveLink").value.trim();
    const date = document.getElementById("date").value.trim();
    const start = document.getElementById("start").value.trim();
    const end = document.getElementById("end").value.trim();
    const resultBox = document.getElementById("result");

    // 입력 검증
    if (!driveLink) {
        alert("Please enter a valid Google Drive link.");
        return;
    }
    if (!date || !start || !end) {
        alert("Please enter date, start time, and end time.");
        return;
    }

    // UI 업데이트
    resultBox.classList.remove("d-none");
    resultBox.textContent = "⏳ Processing video... This may take a while.";

    try {
        // QueryString 방식 (Spring @RequestParam 그대로 사용 가능)
        const url = `/api/analyze?driveLink=${encodeURIComponent(driveLink)}`
                  + `&date=${encodeURIComponent(date)}`
                  + `&start=${encodeURIComponent(start)}`
                  + `&end=${encodeURIComponent(end)}`;

        const response = await fetch(url, {
            method: "POST"
        });

        if (!response.ok) {
            resultBox.textContent = "❌ Error: " + response.status + " " + response.statusText;
            return;
        }

        const data = await response.json();
        resultBox.textContent = JSON.stringify(data, null, 2);

    } catch (err) {
        resultBox.textContent = "⚠️ Request failed: " + err.message;
    }
});
