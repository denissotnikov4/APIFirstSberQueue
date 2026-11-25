const endpoint = '/api/v1/conversions';
const pollIntervalMs = 1000;
const pendingStatuses = new Set(['PENDING', 'PROCESSING']);
const conversions = new Map();

const form = document.querySelector('#conversion-form');
const xmlInput = document.querySelector('#xml');
const convertButton = document.querySelector('#convert');
const table = document.querySelector('#conversions');
const emptyState = document.querySelector('#empty-state');
const error = document.querySelector('#error');
const dialog = document.querySelector('#result-dialog');
const result = document.querySelector('#result');
const dateFormat = new Intl.DateTimeFormat(undefined, { dateStyle: 'medium', timeStyle: 'medium' });

let pollTimer;
let polling = false;

async function requestJson(url, options) {
    const response = await fetch(url, options);
    if (!response.ok) {
        const problem = await response.json().catch(() => null);
        const failure = new Error(problem?.detail || `Request failed (HTTP ${response.status}).`);
        failure.status = response.status;
        throw failure;
    }
    return response.json();
}

function showError(failure) {
    error.textContent = failure.message;
    error.hidden = false;
}

function addConversion(conversion) {
    const row = table.insertRow(0);
    row.insertCell().textContent = conversion.id;
    const badge = document.createElement('span');
    row.insertCell().append(badge);
    const submitted = document.createElement('time');
    row.insertCell().append(submitted);

    const view = document.createElement('button');
    view.type = 'button';
    view.textContent = 'View JSON';
    view.addEventListener('click', async () => {
        view.disabled = true;
        error.hidden = true;
        try {
            const json = await requestJson(`${endpoint}/${conversion.id}/result`);
            result.textContent = JSON.stringify(json, null, 2);
            dialog.showModal();
        } catch (failure) {
            showError(failure);
        } finally {
            view.disabled = false;
        }
    });
    row.insertCell().append(view);
    conversions.set(conversion.id, { row, badge, submitted, view });
    emptyState.hidden = true;
    updateConversion(conversion);
}

function updateConversion({ id, status, submittedAt }) {
    const entry = conversions.get(id);
    entry.status = status;
    entry.badge.className = `badge ${status.toLowerCase()}`;
    entry.badge.textContent = status;
    entry.view.disabled = status !== 'COMPLETED';
    if (submittedAt) {
        entry.submitted.dateTime = submittedAt;
        entry.submitted.textContent = dateFormat.format(new Date(submittedAt));
    }
}

function pendingIds() {
    return [...conversions].filter(([, entry]) => pendingStatuses.has(entry.status)).map(([id]) => id);
}

async function pollStatuses() {
    if (polling) return;
    polling = true;
    try {
        await Promise.all(pendingIds().map(async (id) => {
            try {
                updateConversion(await requestJson(`${endpoint}/${id}`));
            } catch (failure) {
                showError(failure);
                if (failure.status === 404) {
                    conversions.get(id).row.remove();
                    conversions.delete(id);
                    emptyState.hidden = conversions.size > 0;
                }
            }
        }));
    } finally {
        polling = false;
        if (pendingIds().length === 0) {
            clearInterval(pollTimer);
            pollTimer = undefined;
        }
    }
}

document.querySelector('#load-example').addEventListener('click', () => {
    xmlInput.value = xmlInput.defaultValue;
    xmlInput.focus();
});

form.addEventListener('submit', async (event) => {
    event.preventDefault();
    convertButton.disabled = true;
    error.hidden = true;
    try {
        const conversion = await requestJson(endpoint, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ xml: xmlInput.value })
        });
        addConversion({ ...conversion, submittedAt: new Date().toISOString() });
        pollTimer ??= setInterval(pollStatuses, pollIntervalMs);
    } catch (failure) {
        showError(failure);
    } finally {
        convertButton.disabled = false;
    }
});
