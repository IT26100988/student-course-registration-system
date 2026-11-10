// admin.js — M4 Admin Frontend Logic

// ================= LOGIN (For Testing UI) =================

async function doAdminLogin() {
    const email = document.getElementById("loginEmail").value;
    const password = document.getElementById("loginPassword").value;

    try {
        const response = await fetch("/api/v1/auth/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email: email, password: password })
        });

        if (!response.ok) throw new Error("Login failed. Check credentials.");

        document.getElementById("loginSection").style.display = "none";

        // Reload data
        loadAdminRequests();

    } catch (error) {
        alert("Login Error: " + error.message);
    }
}

// ================= ADMIN QUEUE FUNCTIONS =================

async function loadAdminRequests() {
    const tbody = document.getElementById("requestTableBody");
    tbody.innerHTML = "<tr><td colspan='6'>Loading...</td></tr>";

    try {
        const response = await fetch("/api/v1/admin/requests");

        if (!response.ok) throw new Error("Failed to load. Are you logged in as Admin?");

        const data = await response.json();
        tbody.innerHTML = "";

        let nextPending = null;

        if (!data.items || data.items.length === 0) {
            tbody.innerHTML = "<tr><td colspan='6'>No requests found.</td></tr>";
            document.getElementById("nextRequestDisplay").innerText = "No requests.";
            return;
        }

        // Sort by sequenceNo ASC (FIFO display)
        data.items.sort((a, b) => a.sequenceNo - b.sequenceNo);

        data.items.forEach(req => {
            // Find the oldest pending for the display
            if (req.status === "PENDING" && nextPending === null) {
                nextPending = req;
            }

            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${req.requestId}</td>
                <td>${req.studentId}</td>
                <td>${req.offeringId}</td>
                <td>${req.sequenceNo}</td>
                <td class="status-${req.status}">${req.status}</td>
                <td>${new Date(req.requestedAt).toLocaleString()}</td>
            `;
            tbody.appendChild(tr);
        });

        // Update the "Next" Display and Buttons
        const btnApprove = document.querySelector('.btn-approve');
        const btnReject = document.querySelector('.btn-reject');

        if (nextPending) {
            document.getElementById("nextRequestDisplay").innerHTML =
                `Next in Queue: <br> <b>${nextPending.requestId}</b> <br> Student: ${nextPending.studentId} <br> Offering: ${nextPending.offeringId}`;

            // Enable buttons
            if (btnApprove) btnApprove.disabled = false;
            if (btnReject) btnReject.disabled = false;
        } else {
            document.getElementById("nextRequestDisplay").innerText = "No pending requests in queue.";

            // Disable buttons
            if (btnApprove) btnApprove.disabled = true;
            if (btnReject) btnReject.disabled = true;
        }

    } catch (error) {
        tbody.innerHTML = `<tr><td colspan='6' style='color:red'>${error.message}</td></tr>`;
        document.getElementById("nextRequestDisplay").innerText = "Error loading queue.";
    }
}

async function processDecision(decisionType) {
    const reason = document.getElementById("reasonInput").value.trim();
    const isPaid = document.getElementById("isPaidCheckbox").checked;
    const msgDiv = document.getElementById("message");
    const errDiv = document.getElementById("error");

    msgDiv.innerText = "";
    errDiv.innerText = "";

    if (decisionType === "REJECT" && !reason) {
        errDiv.innerText = "Please enter a reason for rejection.";
        return;
    }

    try {
        const response = await fetch("/api/v1/admin/requests/next/decision", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                decision: decisionType,
                reason: reason,
                isPaid: isPaid
            })
        });

        if (!response.ok) {
            const errData = await response.json();
            throw new Error(errData.message || errData.error || "Decision failed");
        }

        const data = await response.json();
        msgDiv.innerText = `Success! Processed Request: ${data.requestId} (Status: ${data.status})`;

        // Clear inputs
        document.getElementById("reasonInput").value = "";

        // Refresh table
        loadAdminRequests();

    } catch (error) {
        errDiv.innerText = "Error: " + error.message;
    }
}
// ================= ADMIN ENROLLMENTS FUNCTIONS =================

async function loadAllEnrollments() {
    const tbody = document.getElementById("enrollmentTableBody");
    tbody.innerHTML = "<tr><td colspan='6'>Loading...</td></tr>";

    try {
        // Note: We need an endpoint to get ALL enrollments.
        // Since we only have /students/me/enrollments, we will fetch from a new endpoint later.
        // For now, we assume the admin can see all if the backend is updated.
        // Let's pretend we added a GET /api/v1/admin/enrollments endpoint in the controller.
        const response = await fetch("/api/v1/admin/enrollments");

        if (!response.ok) throw new Error("Failed to load. Are you logged in?");

        const data = await response.json();
        tbody.innerHTML = "";

        if (!data.items || data.items.length === 0) {
            tbody.innerHTML = "<tr><td colspan='6'>No enrollments found.</td></tr>";
            return;
        }

        data.items.forEach(enr => {
            const tr = document.createElement("tr");

            // Create the withdraw button only if status is ACTIVE
            let actionBtn = "-";
            if (enr.status === "ACTIVE") {
                actionBtn = `<button class="btn-withdraw" onclick="withdrawEnrollment('${enr.enrollmentId}')">Withdraw</button>`;
            }

            tr.innerHTML = `
                <td>${enr.enrollmentId}</td>
                <td>${enr.requestId}</td>
                <td>${enr.studentId}</td>
                <td>${enr.offeringId}</td>
                <td class="status-${enr.status}">${enr.status}</td>
                <td>${actionBtn}</td>
            `;
            tbody.appendChild(tr);
        });

    } catch (error) {
        tbody.innerHTML = `<tr><td colspan='6' style='color:red'>${error.message}</td></tr>`;
    }
}

async function withdrawEnrollment(enrollmentId) {
    const reason = prompt("Enter reason for withdrawal:");
    if (!reason || reason.trim() === "") {
        alert("Withdrawal cancelled. Reason is required.");
        return;
    }

    try {
        const response = await fetch(`/api/v1/admin/enrollments/${enrollmentId}/withdraw`, {
            method: "PATCH",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ reason: reason })
        });

        if (!response.ok) {
            const errData = await response.json();
            throw new Error(errData.message || errData.error || "Withdrawal failed");
        }

        alert("Enrollment withdrawn successfully!");
        loadAllEnrollments(); // Refresh list

    } catch (error) {
        alert("Error: " + error.message);
    }
}