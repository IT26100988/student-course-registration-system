// registration.js — M4 Frontend Logic

// ================= INITIAL LOAD (Page Load වෙද්දී වැඩ කරන්න) =================

document.addEventListener("DOMContentLoaded", () => {

    // 1. M1 ගේ /auth/me එකෙන් ඔයාගේ නම ගන්න
    fetch("/api/v1/auth/me")
        .then(res => {
            if (!res.ok) throw new Error("Not logged in");
            return res.json();
        })
        .then(data => {
            const nameDisplay = document.getElementById("studentNameDisplay");
            if (nameDisplay && data.fullName) {
                nameDisplay.innerText = data.fullName + " (" + data.userId + ")";
            }
        })
        .catch(err => {
            const nameDisplay = document.getElementById("studentNameDisplay");
            if (nameDisplay) nameDisplay.innerText = "Not logged in (Please login from M1)";
        });

    // 2. Tables වල Data load කරන්න (මේක කලින් තිබ්බා නම් ආයෙත් දාන්න ඕන නෑ)
    // ඔයාගේ File එකේ loadMyRequests() සහ loadMyEnrollments() කියන functions දෙක තියෙනවා නම් මේක වැඩ කරයි.
    if (typeof loadMyRequests === "function") loadMyRequests();
    if (typeof loadMyEnrollments === "function") loadMyEnrollments();
});

// ================= STUDENT FUNCTIONS =================

async function submitRequest() {
    const offeringId = document.getElementById("offeringIdInput").value.trim();
    const msgDiv = document.getElementById("message");
    const errDiv = document.getElementById("error");

    msgDiv.innerText = "";
    errDiv.innerText = "";

    if (!offeringId) {
        errDiv.innerText = "Please enter an Offering ID.";
        return;
    }

    try {
        const response = await fetch("/api/v1/students/me/requests", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ offeringId: offeringId })
        });

        if (!response.ok) {
            const errData = await response.json();
            throw new Error(errData.message || errData.error || "Request failed");
        }

        const data = await response.json();
        msgDiv.innerText = `Success! Request ID: ${data.requestId} (Status: ${data.status})`;
        document.getElementById("offeringIdInput").value = "";

        // Refresh both tables
        loadMyRequests();
        loadMyEnrollments();

    } catch (error) {
        errDiv.innerText = "Error: " + error.message;
    }
}

async function loadMyRequests() {
    const tbody = document.getElementById("requestTableBody");
    tbody.innerHTML = "<tr><td colspan='5'>Loading...</td></tr>";

    try {
        const response = await fetch("/api/v1/students/me/requests");

        if (!response.ok) throw new Error("Failed to load. Are you logged in?");

        const data = await response.json();
        tbody.innerHTML = "";

        if (!data.items || data.items.length === 0) {
            tbody.innerHTML = "<tr><td colspan='5'>No requests found.</td></tr>";
            return;
        }

        // Sort latest first
        data.items.sort((a, b) => b.sequenceNo - a.sequenceNo);

        data.items.forEach(req => {
            const tr = document.createElement("tr");

            let actionBtn = "-";
            if (req.status === "PENDING") {
                actionBtn = `<button style="background:#e74c3c; padding: 5px 10px; font-size: 12px;" onclick="cancelRequest('${req.requestId}')">Cancel</button>`;
            }

            tr.innerHTML = `
                <td>${req.requestId}</td>
                <td>${req.offeringId}</td>
                <td>${req.sequenceNo}</td>
                <td class="status-${req.status}">${req.status}</td>
                <td>${new Date(req.requestedAt).toLocaleString()}</td>
                <td>${actionBtn}</td>
            `;
            tbody.appendChild(tr);
        });

    } catch (error) {
        tbody.innerHTML = `<tr><td colspan='5' style='color:red'>${error.message}</td></tr>`;
    }
}

async function loadMyEnrollments() {
    const tbody = document.getElementById("enrollmentTableBody");
    tbody.innerHTML = "<tr><td colspan='4'>Loading...</td></tr>";

    try {
        const response = await fetch("/api/v1/students/me/enrollments");

        if (!response.ok) throw new Error("Failed to load. Are you logged in?");

        const data = await response.json();
        tbody.innerHTML = "";

        if (!data.items || data.items.length === 0) {
            tbody.innerHTML = "<tr><td colspan='4'>No enrollments found.</td></tr>";
            return;
        }

        data.items.forEach(enr => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${enr.enrollmentId}</td>
                <td>${enr.offeringId}</td>
                <td class="status-${enr.status}">${enr.status}</td>
                <td>${new Date(enr.createdAt).toLocaleString()}</td>
            `;
            tbody.appendChild(tr);
        });

    } catch (error) {
        tbody.innerHTML = `<tr><td colspan='4' style='color:red'>${error.message}</td></tr>`;
    }
}
// ================= CANCEL REQUEST =================

async function cancelRequest(requestId) {
    if (!confirm("Are you sure you want to cancel this request?")) {
        return;
    }

    try {
        const response = await fetch(`/api/v1/students/me/requests/${requestId}/cancel`, {
            method: "PATCH"
        });

        if (!response.ok) {
            throw new Error("Cancel failed");
        }

        alert("Request cancelled successfully!");

        // Table එක Refresh කරන්න
        await loadMyRequests();

    } catch (error) {
        alert("Error: " + error.message);
    }
}