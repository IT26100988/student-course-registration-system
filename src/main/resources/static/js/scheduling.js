(() => {
    "use strict";

    const API_BASE = "/api/v1";

    function showMessage(element, message) {
        if (element) {
            element.textContent = message;
        }
    }

    function makeError(message, status) {
        const error = new Error(message);
        error.status = status;
        return error;
    }

    async function readResponse(response) {
        const text = await response.text();

        if (!text) {
            return null;
        }

        try {
            return JSON.parse(text);
        } catch {
            // Spring's fallback error page is HTML, not a useful API message.
            if (text.trimStart().startsWith("<")) {
                return null;
            }

            return { message: text.slice(0, 300) };
        }
    }

    function responseMessage(data, status) {
        if (status === 401) {
            return "Please log in before using this page (401).";
        }

        if (status === 403) {
            return "This account does not have permission for this action (403).";
        }

        if (status === 404) {
            return "The requested API route was not found (404).";
        }

        return data?.message
            || data?.error?.message
            || `Request failed: ${status}`;
    }

    async function getCsrfHeader() {
        const response = await fetch(`${API_BASE}/auth/csrf`, {
            method: "GET",
            credentials: "same-origin",
            headers: {
                Accept: "application/json"
            }
        });

        const data = await readResponse(response);

        if (!response.ok) {
            if (response.status === 404) {
                throw makeError(
                    "CSRF endpoint not found (404). Backend team needs to add it.",
                    404
                );
            }

            throw makeError(responseMessage(data, response.status), response.status);
        }

        const token = data?.token || data?.csrfToken;
        const headerName = data?.headerName || "X-CSRF-TOKEN";

        if (!token) {
            throw makeError(
                "CSRF response has no token. Check the backend response.",
                response.status
            );
        }

        return { [headerName]: token };
    }

    async function sendRequest(path, options = {}) {
        const method = (options.method || "GET").toUpperCase();
        const headers = {
            Accept: "application/json"
        };

        if (options.body !== undefined) {
            headers["Content-Type"] = "application/json";
        }

        if (!["GET", "HEAD", "OPTIONS"].includes(method)) {
            const csrfHeaders = await getCsrfHeader();
            Object.assign(headers, csrfHeaders);
        }

        const response = await fetch(`${API_BASE}${path}`, {
            method,
            credentials: "same-origin",
            headers,
            body: options.body === undefined
                ? undefined
                : JSON.stringify(options.body)
        });

        const data = await readResponse(response);

        if (!response.ok) {
            throw makeError(
                responseMessage(data, response.status),
                response.status
            );
        }

        return data;
    }

    function appendCell(row, value) {
        const cell = document.createElement("td");
        cell.textContent = value ?? "";
        row.appendChild(cell);
        return cell;
    }

    function showTime(value) {
        return value ? value.slice(0, 5) : "";
    }

    // Admin: Lecturer management
    (() => {
        const form = document.getElementById("lecturer-form");

        if (!form) {
            return;
        }

        const idInput = document.getElementById("lecturerId");
        const nameInput = document.getElementById("fullName");
        const emailInput = document.getElementById("email");
        const departmentInput = document.getElementById("department");
        const statusInput = document.getElementById("status");
        const saveButton = document.getElementById("saveButton");
        const cancelButton = document.getElementById("cancelButton");
        const refreshButton = document.getElementById("refreshButton");
        const message = document.getElementById("message");
        const tableBody = document.getElementById("lecturersTableBody");

        function resetForm() {
            form.reset();
            idInput.value = "";
            statusInput.value = "ACTIVE";
            statusInput.disabled = true;
            saveButton.textContent = "Save Lecturer";
        }

        async function loadLecturers(messageAfterLoad = "") {
            showMessage(message, "Loading lecturers...");
            tableBody.replaceChildren();

            try {
                const data = await sendRequest("/admin/lecturers");
                const lecturers = Array.isArray(data?.items) ? data.items : [];

                lecturers.forEach((lecturer) => {
                    const row = document.createElement("tr");

                    appendCell(
                        row,
                        `${lecturer.fullName || ""} (ID: ${lecturer.lecturerId || ""})`
                    );
                    appendCell(row, lecturer.email);
                    appendCell(row, lecturer.department);
                    appendCell(row, lecturer.status);

                    const actionCell = appendCell(row, "");
                    const editButton = document.createElement("button");

                    editButton.type = "button";
                    editButton.textContent = "Edit";
                    editButton.addEventListener("click", () => {
                        idInput.value = lecturer.lecturerId || "";
                        nameInput.value = lecturer.fullName || "";
                        emailInput.value = lecturer.email || "";
                        departmentInput.value = lecturer.department || "";
                        statusInput.value = lecturer.status || "ACTIVE";
                        statusInput.disabled = false;
                        saveButton.textContent = "Update Lecturer";
                        showMessage(message, "Update the lecturer, then save.");
                    });

                    actionCell.appendChild(editButton);
                    tableBody.appendChild(row);
                });

                showMessage(
                    message,
                    messageAfterLoad
                    || (lecturers.length === 0
                        ? "No lecturers found."
                        : `Loaded ${lecturers.length} lecturer(s).`)
                );
            } catch (error) {
                showMessage(message, error.message);
            }
        }

        form.addEventListener("submit", async (event) => {
            event.preventDefault();

            const lecturerId = idInput.value.trim();
            const body = {
                fullName: nameInput.value.trim(),
                email: emailInput.value.trim(),
                department: departmentInput.value.trim()
            };

            let path = "/admin/lecturers";
            let method = "POST";

            if (lecturerId) {
                path += `/${encodeURIComponent(lecturerId)}`;
                method = "PATCH";
                body.status = statusInput.value;
            }

            try {
                const result = await sendRequest(path, { method, body });
                const successMessage = lecturerId
                    ? "Lecturer updated."
                    : `Lecturer saved. ID: ${result?.lecturerId || "saved"}`;

                resetForm();
                await loadLecturers(successMessage);
            } catch (error) {
                showMessage(message, error.message);
            }
        });

        cancelButton.addEventListener("click", () => {
            resetForm();
            showMessage(message, "Update cancelled.");
        });

        refreshButton.addEventListener("click", () => loadLecturers());

        statusInput.disabled = true;
        loadLecturers();
    })();

    // Admin: Schedule management
    (() => {
        const form = document.getElementById("schedule-form");

        if (!form) {
            return;
        }

        const idInput = document.getElementById("scheduleId");
        const offeringInput = document.getElementById("offeringId");
        const lecturerInput = document.getElementById("lecturerId");
        const dayInput = document.getElementById("dayOfWeek");
        const startInput = document.getElementById("startLocal");
        const endInput = document.getElementById("endLocal");
        const timeZoneInput = document.getElementById("timeZone");
        const roomInput = document.getElementById("room");
        const statusInput = document.getElementById("scheduleStatus");
        const saveButton = document.getElementById("saveScheduleButton");
        const cancelButton = document.getElementById("cancelScheduleButton");
        const refreshButton = document.getElementById("refreshScheduleButton");
        const message = document.getElementById("scheduleMessage");
        const tableBody = document.getElementById("scheduleTableBody");

        function resetForm() {
            form.reset();
            idInput.value = "";
            offeringInput.disabled = false;
            timeZoneInput.value = "Asia/Colombo";
            statusInput.value = "ACTIVE";
            statusInput.disabled = true;
            saveButton.textContent = "Save Schedule";
        }

        async function loadSchedules(messageAfterLoad = "") {
            showMessage(message, "Loading schedules...");
            tableBody.replaceChildren();

            try {
                const data = await sendRequest("/admin/schedules");
                const schedules = Array.isArray(data?.items) ? data.items : [];

                schedules.forEach((schedule) => {
                    const row = document.createElement("tr");

                    appendCell(row, schedule.offeringId);
                    appendCell(row, schedule.lecturerId);
                    appendCell(row, schedule.dayOfWeek);
                    appendCell(
                        row,
                        `${showTime(schedule.startLocal)} - ${showTime(schedule.endLocal)}`
                    );
                    appendCell(row, schedule.room);
                    appendCell(row, schedule.status);

                    const actionCell = appendCell(row, "");
                    const editButton = document.createElement("button");

                    editButton.type = "button";
                    editButton.textContent = "Edit";
                    editButton.addEventListener("click", () => {
                        idInput.value = schedule.scheduleId || "";
                        offeringInput.value = schedule.offeringId || "";
                        offeringInput.disabled = true;
                        lecturerInput.value = schedule.lecturerId || "";
                        dayInput.value = schedule.dayOfWeek || "";
                        startInput.value = showTime(schedule.startLocal);
                        endInput.value = showTime(schedule.endLocal);
                        timeZoneInput.value = schedule.timeZone || "Asia/Colombo";
                        roomInput.value = schedule.room || "";
                        statusInput.value = schedule.status || "ACTIVE";
                        statusInput.disabled = false;
                        saveButton.textContent = "Update Schedule";
                        showMessage(message, "Update the schedule, then save.");
                    });

                    actionCell.appendChild(editButton);
                    tableBody.appendChild(row);
                });

                showMessage(
                    message,
                    messageAfterLoad
                    || (schedules.length === 0
                        ? "No schedules found."
                        : `Loaded ${schedules.length} schedule(s).`)
                );
            } catch (error) {
                showMessage(message, error.message);
            }
        }

        form.addEventListener("submit", async (event) => {
            event.preventDefault();

            const scheduleId = idInput.value.trim();
            const body = {
                lecturerId: lecturerInput.value.trim(),
                dayOfWeek: dayInput.value,
                startLocal: startInput.value,
                endLocal: endInput.value,
                timeZone: timeZoneInput.value.trim(),
                room: roomInput.value.trim()
            };

            let path = "/admin/schedules";
            let method = "POST";

            if (scheduleId) {
                path += `/${encodeURIComponent(scheduleId)}`;
                method = "PATCH";
                body.status = statusInput.value;
            } else {
                body.offeringId = offeringInput.value.trim();
            }

            try {
                await sendRequest(path, { method, body });
                resetForm();
                await loadSchedules(
                    scheduleId ? "Schedule updated." : "Schedule saved."
                );
            } catch (error) {
                showMessage(message, error.message);
            }
        });

        cancelButton.addEventListener("click", () => {
            resetForm();
            showMessage(message, "Update cancelled.");
        });

        refreshButton.addEventListener("click", () => loadSchedules());

        statusInput.disabled = true;
        loadSchedules();
    })();

    // Admin: Prerequisite management
    (() => {
        const form = document.getElementById("prerequisite-form");

        if (!form) {
            return;
        }

        const idInput = document.getElementById("prerequisiteId");
        const courseIdInput = document.getElementById("courseId");
        const requiredCourseIdInput =
            document.getElementById("requiredCourseId");
        const statusInput = document.getElementById("prerequisiteStatus");
        const saveButton = document.getElementById("savePrerequisiteButton");
        const cancelButton =
            document.getElementById("cancelPrerequisiteButton");
        const refreshButton =
            document.getElementById("refreshPrerequisiteButton");
        const message = document.getElementById("prerequisiteMessage");
        const tableBody = document.getElementById("prerequisiteTableBody");

        function resetForm() {
            form.reset();
            idInput.value = "";
            courseIdInput.disabled = false;
            statusInput.value = "ACTIVE";
            statusInput.disabled = true;
            saveButton.textContent = "Save Prerequisite";
        }

        async function loadPrerequisites(messageAfterLoad = "") {
            showMessage(message, "Loading prerequisite rules...");
            tableBody.replaceChildren();

            try {
                const data = await sendRequest("/admin/prerequisites");
                const rules = Array.isArray(data?.items) ? data.items : [];

                rules.forEach((rule) => {
                    const row = document.createElement("tr");

                    appendCell(row, rule.courseId);
                    appendCell(row, rule.requiredCourseId);
                    appendCell(row, rule.status);

                    const actionCell = appendCell(row, "");
                    const editButton = document.createElement("button");

                    editButton.type = "button";
                    editButton.textContent = "Edit";
                    editButton.addEventListener("click", () => {
                        idInput.value = rule.prerequisiteId || "";
                        courseIdInput.value = rule.courseId || "";
                        courseIdInput.disabled = true;
                        requiredCourseIdInput.value =
                            rule.requiredCourseId || "";
                        statusInput.value = rule.status || "ACTIVE";
                        statusInput.disabled = false;
                        saveButton.textContent = "Update Prerequisite";
                        showMessage(message, "Update the rule, then save.");
                    });

                    actionCell.appendChild(editButton);
                    tableBody.appendChild(row);
                });

                showMessage(
                    message,
                    messageAfterLoad
                    || (rules.length === 0
                        ? "No prerequisite rules found."
                        : `Loaded ${rules.length} rule(s).`)
                );
            } catch (error) {
                showMessage(message, error.message);
            }
        }

        form.addEventListener("submit", async (event) => {
            event.preventDefault();

            const prerequisiteId = idInput.value.trim();
            const isUpdating = prerequisiteId !== "";
            const body = {
                requiredCourseId: requiredCourseIdInput.value.trim()
            };

            let path = "/admin/prerequisites";
            let method = "POST";

            if (isUpdating) {
                path += `/${encodeURIComponent(prerequisiteId)}`;
                method = "PATCH";
                body.status = statusInput.value;
            } else {
                body.courseId = courseIdInput.value.trim();
            }

            try {
                await sendRequest(path, { method, body });
                resetForm();
                await loadPrerequisites(
                    isUpdating
                        ? "Prerequisite updated."
                        : "Prerequisite saved."
                );
            } catch (error) {
                showMessage(message, error.message);
            }
        });

        cancelButton.addEventListener("click", () => {
            resetForm();
            showMessage(message, "Update cancelled.");
        });

        refreshButton.addEventListener("click", () => loadPrerequisites());

        statusInput.disabled = true;
        loadPrerequisites();
    })();

    // Student: Prerequisite search
    (() => {
        const form = document.getElementById("student-prerequisite-form");

        if (!form) {
            return;
        }

        const courseIdInput = document.getElementById("studentCourseId");
        const message = document.getElementById("studentPrerequisiteMessage");
        const tableBody =
            document.getElementById("studentPrerequisiteTableBody");

        form.addEventListener("submit", async (event) => {
            event.preventDefault();

            const courseId = courseIdInput.value.trim();
            tableBody.replaceChildren();

            if (!courseId) {
                showMessage(message, "Enter a Course ID.");
                return;
            }

            showMessage(message, "Loading prerequisites...");

            try {
                const data = await sendRequest(
                    `/courses/${encodeURIComponent(courseId)}/prerequisites`
                );
                const rules = Array.isArray(data?.items) ? data.items : [];

                rules.forEach((rule) => {
                    const row = document.createElement("tr");
                    appendCell(row, rule.courseId);
                    appendCell(row, rule.requiredCourseId);
                    tableBody.appendChild(row);
                });

                showMessage(
                    message,
                    rules.length === 0
                        ? "No prerequisite rules found for this course."
                        : `Found ${rules.length} prerequisite rule(s).`
                );
            } catch (error) {
                showMessage(message, error.message);
            }
        });
    })();

    // Student: Timetable
    (() => {
        const tableBody =
            document.getElementById("studentTimetableTableBody");

        if (!tableBody) {
            return;
        }

        const message = document.getElementById("studentTimetableMessage");
        const refreshButton =
            document.getElementById("refreshStudentTimetableButton");

        async function loadTimetable() {
            tableBody.replaceChildren();
            showMessage(message, "Loading timetable...");

            try {
                const data = await sendRequest("/students/me/timetable");
                const schedules = Array.isArray(data?.items) ? data.items : [];

                schedules.forEach((schedule) => {
                    const row = document.createElement("tr");

                    appendCell(row, schedule.offeringId);
                    appendCell(row, schedule.dayOfWeek);
                    appendCell(row, showTime(schedule.startLocal));
                    appendCell(row, showTime(schedule.endLocal));
                    appendCell(row, schedule.lecturerId);
                    appendCell(row, schedule.room);

                    tableBody.appendChild(row);
                });

                showMessage(
                    message,
                    schedules.length === 0
                        ? "No timetable entries found."
                        : `Loaded ${schedules.length} timetable entry(s).`
                );
            } catch (error) {
                showMessage(
                    message,
                    error.status === 404
                        ? "Student timetable API route is missing on the backend (404)."
                        : error.message
                );
            }
        }

        refreshButton.addEventListener("click", loadTimetable);
        loadTimetable();
    })();
})();
