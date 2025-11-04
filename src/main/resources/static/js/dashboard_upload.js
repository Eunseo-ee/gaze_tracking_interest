document.getElementById("analyzeBtn").addEventListener("click", async () => {
    const driveLink = document.getElementById("driveLink").value.trim();
    const resultBox = document.getElementById("result");

    if (!driveLink) {
        alert("Please enter a valid Google Drive link.");
        return;
    }

    resultBox.classList.remove("d-none");
    resultBox.textContent = "⏳ Processing video... This may take a while.";

    try {
        const response = await fetch("/api/analyze?driveLink=" + encodeURIComponent(driveLink), {
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