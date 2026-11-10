/* Prerequisite page */

const prerequisiteForm = document.getElementById("prerequisite-form");

if (prerequisiteForm) {
    const prerequisiteIdInput =
        document.getElementById("prerequisiteId");
    const courseIdInput = document.getElementById("courseId");
    const requiredCourseIdInput =
        document.getElementById("requiredCourseId");
    const statusInput =
        document.getElementById("prerequisiteStatus");
    const messageElement =
        document.getElementById("prerequisiteMessage");
    const tableBody =
        document.getElementById("prerequisiteTableBody");
    const saveButton =
        document.getElementById("savePrerequisiteButton");
    const cancelButton =
        document.getElementById("cancelPrerequisiteButton");
    const refreshButton =
        document.getElementById("refreshPrerequisiteButton");

    async function loadPrerequisites() {
        try {
            showMessage(messageElement, "Loading prerequisite rules...");

            const data = await sendRequest("/admin/prerequisites");
            const prerequisites = data.items || [];

            tableBody.replaceChildren();

            for (const prerequisite of prerequisites) {
                const row = document.createElement("tr");

                const courseCell = document.createElement("td");
                courseCell.textContent = prerequisite.courseId;

                const requiredCourseCell = document.createElement("td");
                requiredCourseCell.textContent =
                    prerequisite.requiredCourseId;

                const statusCell = document.createElement("td");
                statusCell.textContent = prerequisite.status;

                const actionCell = document.createElement("td");
                const editButton = document.createElement("button");
                editButton.type = "button";
                editButton.textContent = "Edit";

                editButton.addEventListener("click", () => {
                    prerequisiteIdInput.value =
                        prerequisite.prerequisiteId;
                    courseIdInput.value = prerequisite.courseId;
                    courseIdInput.disabled = true;
                    requiredCourseIdInput.value =
                        prerequisite.requiredCourseId;
                    statusInput.value = prerequisite.status;
                    saveButton.textContent = "Update Prerequisite";

                    showMessage(messageElement, "Editing prerequisite.");
                });

                actionCell.appendChild(editButton);

                row.append(
                    courseCell,
                    requiredCourseCell,
                    statusCell,
                    actionCell
                );

                tableBody.appendChild(row);
            }

            showMessage(
                messageElement,
                `Loaded ${prerequisites.length} rule(s).`
            );
        } catch (error) {
            showMessage(messageElement, error.message);
        }
    }

    function resetPrerequisiteForm() {
        prerequisiteForm.reset();
        prerequisiteIdInput.value = "";
        courseIdInput.disabled = false;
        statusInput.value = "ACTIVE";
        saveButton.textContent = "Save Prerequisite";
    }

    prerequisiteForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        const id = prerequisiteIdInput.value;
        const prerequisiteData = {
            requiredCourseId: requiredCourseIdInput.value.trim()
        };

        let path = "/admin/prerequisites";
        let method = "POST";

        if (id) {
            path += `/${encodeURIComponent(id)}`;
            method = "PATCH";
            prerequisiteData.status = statusInput.value;
        } else {
            prerequisiteData.courseId = courseIdInput.value.trim();
        }

        try {
            await sendRequest(path, {
                method,
                body: JSON.stringify(prerequisiteData)
            });

            showMessage(
                messageElement,
                id ? "Prerequisite updated." : "Prerequisite added."
            );

            resetPrerequisiteForm();
            await loadPrerequisites();
        } catch (error) {
            showMessage(messageElement, error.message);
        }
    });

    cancelButton.addEventListener("click", () => {
        resetPrerequisiteForm();
        showMessage(messageElement, "Update cancelled.");
    });

    refreshButton.addEventListener("click", loadPrerequisites);

    loadPrerequisites();
}

async function sendRequest(path) {
    const response = await fetch(`/api/v1${path}`, {
        method: "GET",
        credentials: "same-origin"
    });

    const data = await response.json();

    if (!response.ok) {
        throw new Error(
            data.error?.message || `Request failed: ${response.status}`
        );
    }

    return data;
}

function showMessage(element, message) {
    if (element) {
        element.textContent = message;
    }
}
/* Student prerequisite page */

const studentPrerequisiteForm =
    document.getElementById("student-prerequisite-form");

if (studentPrerequisiteForm) {
    const courseIdInput =
        document.getElementById("studentCourseId");
    const messageElement =
        document.getElementById("studentPrerequisiteMessage");
    const tableBody =
        document.getElementById("studentPrerequisiteTableBody");

    studentPrerequisiteForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        const courseId = courseIdInput.value.trim();

        try {
            showMessage(messageElement, "Loading prerequisites...");
            tableBody.replaceChildren();

            const data = await sendRequest(
                `/courses/${encodeURIComponent(courseId)}/prerequisites`
            );

            const prerequisites = data.items || [];

            for (const prerequisite of prerequisites) {
                const row = document.createElement("tr");

                const courseCell = document.createElement("td");
                courseCell.textContent = prerequisite.courseId;

                const requiredCourseCell =
                    document.createElement("td");
                requiredCourseCell.textContent =
                    prerequisite.requiredCourseId;

                row.append(courseCell, requiredCourseCell);
                tableBody.appendChild(row);
            }

            if (prerequisites.length === 0) {
                showMessage(
                    messageElement,
                    "No prerequisite rules found for this course."
                );
            } else {
                showMessage(
                    messageElement,
                    `Found ${prerequisites.length} prerequisite rule(s).`
                );
            }
        } catch (error) {
            showMessage(messageElement, error.message);
        }
    });
}

/* Student timetable page */

const studentTimetableTableBody =
    document.getElementById("studentTimetableTableBody");

if (studentTimetableTableBody) {
    const messageElement =
        document.getElementById("studentTimetableMessage");
    const refreshButton =
        document.getElementById("refreshStudentTimetableButton");

    function showTimetableTime(time) {
        return time ? time.slice(0, 5) : "";
    }

    async function loadStudentTimetable() {
        try {
            showMessage(messageElement, "Loading timetable...");
            studentTimetableTableBody.replaceChildren();

            const data = await sendRequest("/students/me/timetable");
            const schedules = data.items || [];

            for (const schedule of schedules) {
                const row = document.createElement("tr");

                const offeringCell = document.createElement("td");
                offeringCell.textContent = schedule.offeringId;

                const dayCell = document.createElement("td");
                dayCell.textContent = schedule.dayOfWeek;

                const startCell = document.createElement("td");
                startCell.textContent =
                    showTimetableTime(schedule.startLocal);

                const endCell = document.createElement("td");
                endCell.textContent =
                    showTimetableTime(schedule.endLocal);

                const lecturerCell = document.createElement("td");
                lecturerCell.textContent = schedule.lecturerId;

                const roomCell = document.createElement("td");
                roomCell.textContent = schedule.room;

                row.append(
                    offeringCell,
                    dayCell,
                    startCell,
                    endCell,
                    lecturerCell,
                    roomCell
                );

                studentTimetableTableBody.appendChild(row);
            }

            showMessage(
                messageElement,
                schedules.length === 0
                    ? "No timetable entries found."
                    : `Loaded ${schedules.length} timetable entry(s).`
            );
        } catch (error) {
            showMessage(messageElement, error.message);
        }
    }

    refreshButton.addEventListener("click", loadStudentTimetable);

    loadStudentTimetable();
}
/* Student prerequisites page */
(() => {
    const form = document.getElementById("student-prerequisite-form");

    if (!form) {
        return;
    }

    const courseIdInput = document.getElementById("studentCourseId");
    const messageElement = document.getElementById(
        "studentPrerequisiteMessage"
    );
    const tableBody = document.getElementById(
        "studentPrerequisiteTableBody"
    );

    form.addEventListener("submit", async (event) => {
        event.preventDefault();

        const courseId = courseIdInput.value.trim();

        if (!courseId) {
            showMessage(messageElement, "Enter a Course ID.");
            return;
        }

        showMessage(messageElement, "Loading prerequisites...");
        tableBody.replaceChildren();

        try {
            const data = await sendRequest(
                `/courses/${encodeURIComponent(courseId)}/prerequisites`
            );

            const prerequisites = Array.isArray(data.items)
                ? data.items
                : [];

            prerequisites.forEach((prerequisite) => {
                const row = document.createElement("tr");

                const courseCell = document.createElement("td");
                courseCell.textContent = prerequisite.courseId;

                const requiredCourseCell = document.createElement("td");
                requiredCourseCell.textContent =
                    prerequisite.requiredCourseId;

                row.append(courseCell, requiredCourseCell);
                tableBody.appendChild(row);
            });

            showMessage(
                messageElement,
                prerequisites.length === 0
                    ? "No prerequisite rules found for this course."
                    : `Loaded ${prerequisites.length} prerequisite rule(s).`
            );
        } catch (error) {
            showMessage(messageElement, error.message);
        }
    });
})();

/* Admin prerequisites page */
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

    function showAdminMessage(text) {
        message.textContent = text;
    }

    async function sendAdminRequest(path, method = "GET", body = null) {
        const headers = {};

        if (body !== null) {
            headers["Content-Type"] = "application/json";
        }

        if (method !== "GET") {
            const csrfResponse = await fetch("/api/v1/auth/csrf", {
                credentials: "same-origin"
            });
            const csrfData = await csrfResponse.json();

            if (!csrfResponse.ok) {
                throw new Error("Could not get the security token.");
            }

            headers[csrfData.headerName] = csrfData.token;
        }

        const response = await fetch(`/api/v1${path}`, {
            method,
            credentials: "same-origin",
            headers,
            body: body === null ? undefined : JSON.stringify(body)
        });

        const data = response.status === 204
            ? null
            : await response.json().catch(() => null);

        if (!response.ok) {
            throw new Error(
                data?.error?.message || `Request failed: ${response.status}`
            );
        }

        return data;
    }

    function resetPrerequisiteForm() {
        form.reset();
        idInput.value = "";
        courseIdInput.disabled = false;
        statusInput.disabled = true;
        saveButton.textContent = "Save Prerequisite";
    }

    async function loadPrerequisites() {
        showAdminMessage("Loading prerequisite rules...");
        tableBody.replaceChildren();

        try {
            const courseId = courseIdInput.value.trim();
            const query = courseId
                ? `?courseId=${encodeURIComponent(courseId)}`
                : "";

            const data = await sendAdminRequest(
                `/admin/prerequisites${query}`
            );
            const rules = Array.isArray(data.items) ? data.items : [];

            rules.forEach((rule) => {
                const row = document.createElement("tr");

                [rule.courseId, rule.requiredCourseId, rule.status]
                    .forEach((value) => {
                        const cell = document.createElement("td");
                        cell.textContent = value ?? "";
                        row.appendChild(cell);
                    });

                const actionCell = document.createElement("td");
                const editButton = document.createElement("button");

                editButton.type = "button";
                editButton.textContent = "Edit";
                editButton.addEventListener("click", () => {
                    idInput.value = rule.prerequisiteId;
                    courseIdInput.value = rule.courseId;
                    courseIdInput.disabled = true;
                    requiredCourseIdInput.value = rule.requiredCourseId;
                    statusInput.value = rule.status;
                    statusInput.disabled = false;
                    saveButton.textContent = "Update Prerequisite";
                    showAdminMessage("Update the rule and save it.");
                });

                actionCell.appendChild(editButton);
                row.appendChild(actionCell);
                tableBody.appendChild(row);
            });

            showAdminMessage(
                rules.length === 0
                    ? "No prerequisite rules found."
                    : `Loaded ${rules.length} rule(s).`
            );
        } catch (error) {
            showAdminMessage(error.message);
        }
    }

    form.addEventListener("submit", async (event) => {
        event.preventDefault();

        const prerequisiteId = idInput.value;
        const isUpdating = prerequisiteId !== "";

        const body = isUpdating
            ? {
                requiredCourseId: requiredCourseIdInput.value.trim(),
                status: statusInput.value
            }
            : {
                courseId: courseIdInput.value.trim(),
                requiredCourseId: requiredCourseIdInput.value.trim()
            };

        try {
            if (isUpdating) {
                await sendAdminRequest(
                    `/admin/prerequisites/${encodeURIComponent(prerequisiteId)}`,
                    "PATCH",
                    body
                );
            } else {
                await sendAdminRequest(
                    "/admin/prerequisites",
                    "POST",
                    body
                );
            }

            resetPrerequisiteForm();
            await loadPrerequisites();
        } catch (error) {
            showAdminMessage(error.message);
        }
    });

    cancelButton.addEventListener("click", () => {
        resetPrerequisiteForm();
        showAdminMessage("Update cancelled.");
    });

    refreshButton.addEventListener("click", loadPrerequisites);

    statusInput.disabled = true;
    loadPrerequisites();
})();
