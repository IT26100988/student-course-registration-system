// registration.js — M4 Frontend Logic

// ================= LOGIN (For Testing UI) =================

async function doLogin() {
    const email = document.getElementById("loginEmail").value;
    const password = document.getElementById("loginPassword").value;

    try {
        const response = await fetch("/api/v1/auth/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email: email, password: password })
        });

        if (!response.ok) throw new Error("Login failed. Check credentials.");

        // Hide login section after success
        document.getElementById("loginSection").style.display = "none";

        // Reload data
        loadMyRequests();
        loadMyEnrollments();

    } catch (error) {
        alert("Login Error: " + error.message);
    }
}

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
            tr.innerHTML = `
                <td>${req.requestId}</td>
                <td>${req.offeringId}</td>
                <td>${req.sequenceNo}</td>
                <td class="status-${req.status}">${req.status}</td>
                <td>${new Date(req.requestedAt).toLocaleString()}</td>
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