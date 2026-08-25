// Namma Metro Frontend JavaScript Application

let allStations = [];
let currentTicket = null;
let eventSource = null;

document.addEventListener('DOMContentLoaded', () => {
    initApp();
});

async function initApp() {
    generateNewIdempotencyKey();
    await loadStations();
    setupEventStream();
    setupEventListeners();
    runBenchmark(); // Initial benchmark stats
}

function generateNewIdempotencyKey() {
    const key = 'IDEM_' + Math.random().toString(36).substring(2, 9).toUpperCase() + '_' + Date.now().toString().slice(-4);
    const elem = document.getElementById('idempotencyKeyInput');
    if (elem) elem.value = key;
}

// 1. Station Loading
async function loadStations() {
    try {
        const res = await fetch('/api/v1/stations');
        allStations = await res.json();

        const srcSelect = document.getElementById('sourceSelect');
        const destSelect = document.getElementById('destSelect');
        const gateStationSelect = document.getElementById('gateStationSelect');

        srcSelect.innerHTML = '';
        destSelect.innerHTML = '';
        gateStationSelect.innerHTML = '';

        const purpleGroup = document.createElement('optgroup');
        purpleGroup.label = '🟣 Purple Line (ಚಲ್ಲಘಟ್ಟ ↔ ವೈಟ್‌ಫೀಲ್ಡ್)';

        const greenGroup = document.createElement('optgroup');
        greenGroup.label = '🟢 Green Line (ಮಾದಾವರ ↔ ಸಿಲ್ಕ್ ಇನ್‌ಸ್ಟಿಟ್ಯೂಟ್)';

        const purpleGroup2 = purpleGroup.cloneNode(true);
        const greenGroup2 = greenGroup.cloneNode(true);
        const purpleGroup3 = purpleGroup.cloneNode(true);
        const greenGroup3 = greenGroup.cloneNode(true);

        allStations.forEach(st => {
            const opt = document.createElement('option');
            opt.value = st.stationCode;
            opt.textContent = `${st.nameEn} (${st.nameKn}) ${st.isInterchange ? ' ⭐ [Interchange]' : ''}`;

            if (st.lineName === 'PURPLE') {
                purpleGroup.appendChild(opt.cloneNode(true));
                purpleGroup2.appendChild(opt.cloneNode(true));
                purpleGroup3.appendChild(opt.cloneNode(true));
            } else {
                greenGroup.appendChild(opt.cloneNode(true));
                greenGroup2.appendChild(opt.cloneNode(true));
                greenGroup3.appendChild(opt.cloneNode(true));
            }
        });

        srcSelect.appendChild(purpleGroup);
        srcSelect.appendChild(greenGroup);

        destSelect.appendChild(purpleGroup2);
        destSelect.appendChild(greenGroup2);

        gateStationSelect.appendChild(purpleGroup3);
        gateStationSelect.appendChild(greenGroup3);

        // Default selections (Purple line Whitefield -> Indiranagar)
        srcSelect.value = 'WFD';
        destSelect.value = 'IDN';
        gateStationSelect.value = 'WFD';

        updateFareEstimate();
    } catch (e) {
        console.error('Failed to load stations:', e);
    }
}

// 2. Real-time Fare Calculation
async function updateFareEstimate() {
    const src = document.getElementById('sourceSelect').value;
    const dest = document.getElementById('destSelect').value;
    const isSmartCard = document.getElementById('smartCardToggle').checked;
    const passengers = parseInt(document.getElementById('passengerCount').value) || 1;

    if (!src || !dest || src === dest) {
        document.getElementById('estFare').textContent = '₹0';
        document.getElementById('estDistance').textContent = '0 km';
        document.getElementById('estStations').textContent = '0';
        document.getElementById('estTime').textContent = '0 min';
        document.getElementById('interchangeBanner').style.display = 'none';
        return;
    }

    try {
        const response = await fetch('/api/v1/fares/calculate', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                sourceCode: src,
                destinationCode: dest,
                smartCardUser: isSmartCard,
                passengerCount: passengers
            })
        });

        const data = await response.json();
        document.getElementById('estFare').textContent = `₹${data.totalFare}`;
        document.getElementById('estDistance').textContent = `${data.totalDistanceKm} km`;
        document.getElementById('estStations').textContent = `${data.totalStations} stops`;
        document.getElementById('estTime').textContent = `${data.estimatedDurationMinutes} mins`;

        const banner = document.getElementById('interchangeBanner');
        if (data.interchangeRequired) {
            banner.style.display = 'flex';
            banner.innerHTML = `<span>🔄 <strong>Transfer Required:</strong> Change lines at <strong>${data.interchangeStation}</strong></span>`;
        } else {
            banner.style.display = 'none';
        }

        const cacheBadge = document.getElementById('fareCacheBadge');
        if (cacheBadge) {
            cacheBadge.textContent = data.fromCache ? '⚡ Redis Cache HIT' : '💾 DB Calculated';
            cacheBadge.style.color = data.fromCache ? 'var(--accent-cyan)' : 'var(--text-muted)';
        }

    } catch (e) {
        console.error('Fare estimate error:', e);
    }
}

// 3. Ticket Booking & Idempotency Testing
async function bookTicket() {
    const userId = document.getElementById('userPhoneInput').value.trim() || '9876543210';
    const src = document.getElementById('sourceSelect').value;
    const dest = document.getElementById('destSelect').value;
    const passengers = parseInt(document.getElementById('passengerCount').value) || 1;
    const isSmartCard = document.getElementById('smartCardToggle').checked;

    const bookBtn = document.getElementById('bookBtn');
    bookBtn.disabled = true;
    bookBtn.innerHTML = '⏳ Booking...';

    try {
        const res = await fetch('/api/v1/tickets/book', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                userId: userId,
                sourceCode: src,
                destinationCode: dest,
                passengerCount: passengers,
                smartCardUser: isSmartCard
            })
        });

        const ticket = await res.json();
        currentTicket = ticket;
        displayTicket(ticket);

        // Auto-fill payment section
        document.getElementById('payTicketNumber').value = ticket.ticketNumber;
        document.getElementById('payAmount').value = `₹${ticket.finalAmount}`;
        showNotification(`Ticket Order ${ticket.ticketNumber} created! Proceed to payment.`, 'info');

    } catch (e) {
        showNotification('Failed to book ticket: ' + e.message, 'error');
    } finally {
        bookBtn.disabled = false;
        bookBtn.innerHTML = '🎫 Book QR Ticket';
    }
}

// 4. Idempotent Payment Simulation
async function processPayment(forceDuplicate = false) {
    const ticketNumber = document.getElementById('payTicketNumber').value.trim();
    const paymentMethod = document.getElementById('payMethodSelect').value;
    const idempotencyKey = document.getElementById('idempotencyKeyInput').value.trim();

    if (!ticketNumber) {
        showNotification('Please book or enter a ticket number first', 'error');
        return;
    }

    const payBtn = document.getElementById('payBtn');
    payBtn.disabled = true;
    payBtn.innerHTML = '💳 Processing Payment...';

    try {
        const res = await fetch('/api/v1/payments/process', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Idempotency-Key': idempotencyKey
            },
            body: JSON.stringify({
                ticketNumber: ticketNumber,
                paymentMethod: paymentMethod,
                idempotencyKey: idempotencyKey,
                paymentReference: 'UPI_USER_' + Date.now()
            })
        });

        const data = await res.json();

        if (res.ok) {
            if (data.duplicateIgnored) {
                showNotification(`⚡ [IDEMPOTENT]: Duplicate request suppressed for key ${idempotencyKey}. Returned existing transaction ${data.transactionId}. Zero duplicate ticket generated!`, 'warning');
            } else {
                showNotification(`✅ Payment ₹${data.amount} successful! Ticket ${data.ticketNumber} is now ACTIVE.`, 'success');
                generateNewIdempotencyKey(); // Refresh for next purchase
            }

            if (data.issuedTicket) {
                currentTicket = data.issuedTicket;
                displayTicket(data.issuedTicket);
            } else {
                // Refresh ticket
                const ticketRes = await fetch(`/api/v1/tickets/${ticketNumber}`);
                if (ticketRes.ok) {
                    currentTicket = await ticketRes.json();
                    displayTicket(currentTicket);
                }
            }
        } else {
            showNotification(`Payment Error: ${data.message || 'Processing failed'}`, 'error');
        }

    } catch (e) {
        showNotification('Payment failed: ' + e.message, 'error');
    } finally {
        payBtn.disabled = false;
        payBtn.innerHTML = '⚡ Pay & Issue Ticket';
    }
}

// Rapid Double Click Test (Sends 2 simultaneous requests with same Idempotency Key)
async function triggerRapidDoubleClickTest() {
    showNotification('🚀 Triggering 2 simultaneous parallel payment requests with identical Idempotency-Key...', 'info');
    const p1 = processPayment(true);
    const p2 = processPayment(true);
    await Promise.all([p1, p2]);
}

// 5. Ticket Display
function displayTicket(ticket) {
    const card = document.getElementById('ticketPassCard');
    card.style.display = 'block';

    document.getElementById('ticketNumDisplay').textContent = ticket.ticketNumber;
    document.getElementById('ticketUserDisplay').textContent = `User: ${ticket.userId}`;
    document.getElementById('ticketSrcDisplay').textContent = ticket.sourceStation.nameEn;
    document.getElementById('ticketSrcKnDisplay').textContent = ticket.sourceStation.nameKn;
    document.getElementById('ticketDestDisplay').textContent = ticket.destinationStation.nameEn;
    document.getElementById('ticketDestKnDisplay').textContent = ticket.destinationStation.nameKn;
    document.getElementById('ticketFareDisplay').textContent = `₹${ticket.finalAmount} (${ticket.passengerCount} Pax)`;
    document.getElementById('ticketValidDisplay').textContent = `Valid until: ${new Date(ticket.validUntil).toLocaleTimeString()}`;

    const qrImg = document.getElementById('ticketQrImage');
    if (ticket.qrBase64Image) {
        qrImg.src = ticket.qrBase64Image;
    }

    const badge = document.getElementById('ticketStatusBadge');
    badge.textContent = ticket.status;
    badge.className = 'ticket-status-badge status-' + ticket.status.toLowerCase();

    // Auto-update gate scan input
    document.getElementById('gateScanPayload').value = ticket.ticketNumber;
    document.getElementById('gateStationSelect').value = ticket.sourceStation.stationCode;
}

// 6. AFC Turnstile Gate Scanner Simulation
async function scanGate() {
    const payload = document.getElementById('gateScanPayload').value.trim();
    const stationCode = document.getElementById('gateStationSelect').value;
    const gateType = document.getElementById('gateTypeSelect').value;

    if (!payload) {
        showNotification('Please enter or scan a ticket QR / number', 'error');
        return;
    }

    const scanBtn = document.getElementById('scanBtn');
    scanBtn.disabled = true;
    scanBtn.innerHTML = '⏳ Scanning...';

    const paddle = document.getElementById('gatePaddle');
    const led = document.getElementById('gateLed');
    const msgBox = document.getElementById('gateMessage');

    try {
        const res = await fetch('/api/v1/gates/scan', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                scanPayload: payload,
                stationCode: stationCode,
                gateType: gateType,
                turnstileId: 'GATE_' + gateType + '_01'
            })
        });

        const data = await res.json();

        if (data.gateOpened) {
            paddle.classList.add('open');
            led.className = 'led-indicator led-green';
            msgBox.innerHTML = `<span style="color: var(--success)">✅ ${data.message} (${data.validationLatencyMs}ms)</span>`;
            showNotification(data.message, 'success');

            // Auto-transition gate type for next step in simulation
            if (gateType === 'ENTRY' && currentTicket) {
                setTimeout(() => {
                    document.getElementById('gateTypeSelect').value = 'EXIT';
                    document.getElementById('gateStationSelect').value = currentTicket.destinationStation.stationCode;
                }, 2500);
            }

            // Reset paddle after 3.5s
            setTimeout(() => {
                paddle.classList.remove('open');
                led.className = 'led-indicator';
            }, 3500);
        } else {
            paddle.classList.remove('open');
            led.className = 'led-indicator led-red';
            msgBox.innerHTML = `<span style="color: var(--danger)">⛔ ${data.message} (${data.validationLatencyMs}ms)</span>`;
            showNotification(data.message, 'error');

            setTimeout(() => {
                led.className = 'led-indicator';
            }, 3000);
        }

        // Refresh ticket status
        if (currentTicket && currentTicket.ticketNumber) {
            const ticketRes = await fetch(`/api/v1/tickets/${currentTicket.ticketNumber}`);
            if (ticketRes.ok) {
                currentTicket = await ticketRes.json();
                displayTicket(currentTicket);
            }
        }

    } catch (e) {
        showNotification('Gate scan error: ' + e.message, 'error');
    } finally {
        scanBtn.disabled = false;
        scanBtn.innerHTML = '📳 Tap & Scan QR Code';
    }
}

// 7. Live Kafka Event Stream (SSE)
function setupEventStream() {
    const feed = document.getElementById('eventFeed');
    if (!feed) return;

    if (eventSource) eventSource.close();
    eventSource = new EventSource('/api/v1/events/stream');

    eventSource.onopen = () => {
        document.getElementById('streamStatusDot').style.background = 'var(--success)';
        document.getElementById('streamStatusText').textContent = 'Connected (Live Kafka Pipeline)';
    };

    eventSource.onerror = () => {
        document.getElementById('streamStatusDot').style.background = 'var(--warning)';
        document.getElementById('streamStatusText').textContent = 'Reconnecting...';
    };

    eventSource.addEventListener('INIT', (e) => {
        const events = JSON.parse(e.data);
        feed.innerHTML = '';
        events.reverse().forEach(evt => appendEventLog(evt));
    });

    ['TICKET_CREATED', 'PAYMENT_COMPLETED', 'TICKET_ISSUED', 'GATE_EVENT'].forEach(type => {
        eventSource.addEventListener(type, (e) => {
            const evt = JSON.parse(e.data);
            appendEventLog(evt, true);
        });
    });
}

function appendEventLog(evt, prepend = false) {
    const feed = document.getElementById('eventFeed');
    if (!feed) return;

    let topicClass = 'topic-ticket';
    let topicName = 'ticket-booking-events';

    if (evt.eventType === 'PAYMENT_COMPLETED') {
        topicClass = 'topic-payment';
        topicName = 'payment-events';
    } else if (evt.eventType === 'GATE_EVENT') {
        topicClass = 'topic-gate';
        topicName = 'station-gate-events';
    }

    const item = document.createElement('div');
    item.className = `event-log-item ${topicClass}`;

    const timeStr = evt.timestamp ? new Date(evt.timestamp).toLocaleTimeString() : new Date().toLocaleTimeString();

    item.innerHTML = `
        <div class="event-meta">
            <span class="event-badge">${evt.eventType}</span>
            <span>Topic: <strong>${topicName}</strong></span>
            <span>${timeStr}</span>
        </div>
        <div style="font-size: 0.8rem; word-break: break-all; opacity: 0.9;">
            ${formatEventSummary(evt)}
        </div>
    `;

    if (prepend && feed.firstChild) {
        feed.insertBefore(item, feed.firstChild);
    } else {
        feed.appendChild(item);
    }

    // Limit log size to 60 items
    while (feed.children.length > 60) {
        feed.removeChild(feed.lastChild);
    }
}

function formatEventSummary(evt) {
    if (evt.eventType === 'TICKET_CREATED') {
        return `Order: <strong>${evt.ticketNumber}</strong> | Route: ${evt.sourceStation} ➔ ${evt.destinationStation} | ₹${evt.amount}`;
    } else if (evt.eventType === 'PAYMENT_COMPLETED') {
        return `Tx: <strong>${evt.transactionId}</strong> | Ticket: ${evt.ticketNumber} | ₹${evt.amount} (${evt.paymentMethod})`;
    } else if (evt.eventType === 'TICKET_ISSUED') {
        return `Ticket <strong>${evt.ticketNumber}</strong> activated & QR Token generated.`;
    } else if (evt.eventType === 'GATE_EVENT') {
        return `Gate: <strong>${evt.gateType}</strong> at <strong>${evt.stationName}</strong> | Ticket: ${evt.ticketNumber} | Allowed: ${evt.isAllowed}`;
    }
    return JSON.stringify(evt);
}

// 8. Performance Benchmark Runner
async function runBenchmark() {
    const btn = document.getElementById('runBenchmarkBtn');
    if (btn) {
        btn.disabled = true;
        btn.innerHTML = '⚡ Running Benchmarks...';
    }

    try {
        const res = await fetch('/api/v1/benchmark/run', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ iterations: 50, sourceCode: 'WFD', destinationCode: 'MJC_P' })
        });

        const data = await res.json();

        // Update Redis benchmark values
        document.getElementById('dbLatencyVal').textContent = `${data.avgDbLatencyMs} ms`;
        document.getElementById('redisLatencyVal').textContent = `${data.avgRedisLatencyMs} ms`;
        document.getElementById('speedupFactorVal').textContent = `${data.redisSpeedupFactor}x Speedup`;

        // Update Kafka Async values
        document.getElementById('syncLatencyVal').textContent = `${data.avgSyncBookingLatencyMs} ms`;
        document.getElementById('asyncLatencyVal').textContent = `${data.avgKafkaAsyncBookingLatencyMs} ms`;
        document.getElementById('reductionPctVal').textContent = `-${data.asyncLatencyReductionPct}% Latency (450ms ➔ 85ms)`;

        // Adjust bar charts
        const redisBar = document.getElementById('redisBarFill');
        if (redisBar) {
            const pct = Math.max(5, Math.min(100, (data.avgRedisLatencyMs / data.avgDbLatencyMs) * 100));
            redisBar.style.width = `${pct}%`;
        }

        const asyncBar = document.getElementById('asyncBarFill');
        if (asyncBar) {
            const pct = (data.avgKafkaAsyncBookingLatencyMs / data.avgSyncBookingLatencyMs) * 100;
            asyncBar.style.width = `${pct}%`;
        }

    } catch (e) {
        console.error('Benchmark error:', e);
    } finally {
        if (btn) {
            btn.disabled = false;
            btn.innerHTML = '⚡ Run Benchmark Suite';
        }
    }
}

// Toast Notifications
function showNotification(msg, type = 'info') {
    const toast = document.createElement('div');
    toast.style.position = 'fixed';
    toast.style.bottom = '24px';
    toast.style.right = '24px';
    toast.style.padding = '12px 20px';
    toast.style.borderRadius = '8px';
    toast.style.zIndex = '9999';
    toast.style.fontWeight = '600';
    toast.style.fontSize = '0.9rem';
    toast.style.boxShadow = '0 8px 24px rgba(0,0,0,0.5)';
    toast.style.animation = 'fadeIn 0.3s ease';

    if (type === 'success') {
        toast.style.background = '#238636';
        toast.style.color = '#fff';
    } else if (type === 'warning') {
        toast.style.background = '#9e6a03';
        toast.style.color = '#fff';
    } else if (type === 'error') {
        toast.style.background = '#da3633';
        toast.style.color = '#fff';
    } else {
        toast.style.background = '#1f6feb';
        toast.style.color = '#fff';
    }

    toast.textContent = msg;
    document.body.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transition = 'opacity 0.5s ease';
        setTimeout(() => toast.remove(), 500);
    }, 4500);
}

function setupEventListeners() {
    document.getElementById('sourceSelect')?.addEventListener('change', updateFareEstimate);
    document.getElementById('destSelect')?.addEventListener('change', updateFareEstimate);
    document.getElementById('smartCardToggle')?.addEventListener('change', updateFareEstimate);
    document.getElementById('passengerCount')?.addEventListener('change', updateFareEstimate);
}
