// Shared fetch helper + accounts module helpers.
const API = "/api/v1";

async function api(path, options = {}) {
    const opts = {
        method: options.method || "GET",
        credentials: "same-origin",
        headers: { "Content-Type": "application/json", ...(options.headers || {}) },
        body: options.body ? JSON.stringify(options.body) : undefined
    };
    const res = await fetch(API + path, opts);
    let data = null;
    const text = await res.text();
    if (text) { try { data = JSON.parse(text); } catch { data = { raw: text }; } }
    if (!res.ok) {
        const msg = data?.error?.message || data?.error || `HTTP ${res.status}`;
        throw new Error(msg);
    }
    return data;
}

function showMsg(elId, message, ok = true) {
    const el = document.getElementById(elId);
    if (!el) return;
    el.textContent = message;
    el.className = "msg show " + (ok ? "msg-ok" : "msg-err");
    if (ok) setTimeout(() => el.className = "msg", 3000);
}

function fmtStatus(s) {
    return s === "ACTIVE" ? '<span style="color:#059669;font-weight:600">ACTIVE</span>' : '<span style="color:#dc2626;font-weight:600">INACTIVE</span>';
}

async function onLogin(event) {
    event.preventDefault();
    const email = document.getElementById("loginEmail").value.trim();
    const password = document.getElementById("loginPassword").value;
    try {
        const res = await api("/auth/login", { method: "POST", body: { email, password } });
        showMsg("msg", `Welcome ${res.fullName} (${res.role})`);
        setTimeout(() => { window.location.href = res.role === "ADMIN" ? "/admin/students.html" : "/student/profile.html"; }, 600);
    } catch (e) { showMsg("msg", e.message, false); }
}

async function onRegister(event) {
    event.preventDefault();
    const body = {
        studentNo: document.getElementById("regStudentNo").value.trim(),
        fullName: document.getElementById("regFullName").value.trim(),
        email: document.getElementById("regEmail").value.trim(),
        password: document.getElementById("regPassword").value
    };
    try {
        const res = await api("/auth/register", { method: "POST", body });
        showMsg("msg", `Registered ${res.studentNo} (${res.email})`);
        setTimeout(() => window.location.href = "/student/login.html", 800);
    } catch (e) { showMsg("msg", e.message, false); }
}

async function loadProfile() {
    try {
        const me = await api("/students/me");
        document.getElementById("profStudentId").textContent = me.studentId;
        document.getElementById("profStudentNo").textContent = me.studentNo;
        document.getElementById("profFullName").value = me.fullName;
        document.getElementById("profEmail").value = me.email;
        document.getElementById("profStatus").textContent = me.status;
        const nameDisp = document.getElementById("profNameDisplay");
        const emailDisp = document.getElementById("profEmailDisplay");
        if (nameDisp) nameDisp.textContent = me.fullName;
        if (emailDisp) emailDisp.textContent = me.email;
    } catch (e) { showMsg("msg", e.message, false); }
}

async function onUpdateProfile(event) {
    event.preventDefault();
    const body = { fullName: document.getElementById("profFullName").value.trim(), email: document.getElementById("profEmail").value.trim() };
    try {
        await api("/students/me", { method: "PATCH", body });
        showMsg("msg", "Profile updated");
    } catch (e) { showMsg("msg", e.message, false); }
}

async function onChangePassword(event) {
    event.preventDefault();
    const body = { currentPassword: document.getElementById("pwdCurrent").value, newPassword: document.getElementById("pwdNew").value };
    try {
        await api("/students/me/password", { method: "POST", body });
        showMsg("msg", "Password changed");
    } catch (e) { showMsg("msg", e.message, false); }
}

async function onLogout() {
    try { await api("/auth/logout", { method: "POST" }); } catch (e) {}
    window.location.href = "/student/login.html";
}

async function loadStudents() {
    const q = document.getElementById("searchQuery").value.trim();
    try {
        const data = await api(`/admin/students?query=${encodeURIComponent(q)}`);
        const tbody = document.getElementById("studentsBody");
        tbody.innerHTML = "";
        data.items.forEach(s => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${s.studentNo}</td>
                <td>${s.fullName}</td>
                <td>${s.email}</td>
                <td>${fmtStatus(s.status)}</td>
                <td><a href="#" onclick="toggleStatus('${s.studentId}', '${s.status}'); return false;">
                    ${s.status === "ACTIVE" ? "Deactivate" : "Activate"}
                </a></td>
            `;
            tbody.appendChild(tr);
        });
    } catch (e) { showMsg("msg", e.message, false); }
}

async function onCreateStudent(event) {
    event.preventDefault();
    const body = {
        studentNo: document.getElementById("newStudentNo").value.trim(),
        fullName: document.getElementById("newFullName").value.trim(),
        email: document.getElementById("newEmail").value.trim(),
        password: document.getElementById("newPassword").value
    };
    try {
        await api("/admin/students", { method: "POST", body });
        showMsg("msg", "Student created");
        loadStudents();
    } catch (e) { showMsg("msg", e.message, false); }
}

async function toggleStatus(studentId, current) {
    const next = current === "ACTIVE" ? "INACTIVE" : "ACTIVE";
    try {
        await api(`/admin/students/${studentId}/status`, { method: "PATCH", body: { status: next } });
        loadStudents();
    } catch (e) { showMsg("msg", e.message, false); }
}