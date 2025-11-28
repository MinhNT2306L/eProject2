// Kitchen View Logic

// Initialize Socket Manager
const socket = SocketManager.getInstance();

// DOM Elements
const ticketGrid = document.getElementById('ticketGrid');
const emptyState = document.getElementById('emptyState');
const connectionStatus = document.getElementById('connectionStatus');

// State
let tickets = [];
let hideCompleted = false; // Toggle state

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    // Check auth
    if (!isAuthenticated()) {
        window.location.href = 'login.html';
        return;
    }

    // Check role (optional, but good practice)
    const user = getCurrentUser();
    if (user && user.role_id !== 3) { // Assuming 3 is the ID for 'Bếp' role we just created. 
        // Note: We might need to fetch role ID dynamically or just rely on username check in login.
        // For now, let's trust the redirect logic.
    }

    // Add "Hide Completed" toggle button to header
    const header = document.querySelector('header .flex.items-center.gap-4');
    if (header) {
        const toggleBtn = document.createElement('button');
        toggleBtn.id = 'toggleCompletedBtn';
        toggleBtn.className = 'text-sm text-gray-600 hover:text-gray-900 font-medium';
        toggleBtn.textContent = 'Ẩn món đã xong';
        toggleBtn.onclick = toggleCompleted;
        header.insertBefore(toggleBtn, header.firstChild);
    }

    // Setup WebSocket listeners
    setupSocketListeners();

    // Load active orders
    loadActiveKitchenOrders();
});

function toggleCompleted() {
    hideCompleted = !hideCompleted;
    const btn = document.getElementById('toggleCompletedBtn');
    if (btn) btn.textContent = hideCompleted ? 'Hiện món đã xong' : 'Ẩn món đã xong';
    renderTickets();
}

async function loadActiveKitchenOrders() {
    try {
        console.log('Fetching active kitchen orders...');
        const response = await apiRequest('/api/kitchen/orders');
        console.log('API Response status:', response.status);

        if (response.ok) {
            const activeBatches = await response.json();
            console.log('Loaded active batches:', activeBatches);

            if (activeBatches.length === 0) {
                console.log('No active batches found.');
            }

            // Clear existing tickets to avoid duplicates if any
            tickets = [];
            activeBatches.forEach(batch => addTicket(batch));
        } else {
            console.error('API Error:', await response.text());
        }
    } catch (error) {
        console.error('Failed to load active orders:', error);
    }
}

function setupSocketListeners() {
    // Connection status updates
    socket.on('open', () => {
        updateConnectionStatus(true);
    });

    socket.on('close', () => {
        updateConnectionStatus(false);
    });

    // Listen for NEW_BATCH_ORDER
    socket.on('NEW_BATCH_ORDER', (data) => {
        console.log('Received new batch order:', data);
        addTicket(data);
    });
}

function updateConnectionStatus(isConnected) {
    const dot = connectionStatus.querySelector('div');
    const text = connectionStatus.childNodes[2]; // The text node

    if (isConnected) {
        dot.className = 'w-2 h-2 rounded-full bg-green-500';
        connectionStatus.className = 'flex items-center gap-2 px-3 py-1 rounded-full bg-green-50 text-sm text-green-700';
        text.textContent = ' Connected';
    } else {
        dot.className = 'w-2 h-2 rounded-full bg-red-500';
        connectionStatus.className = 'flex items-center gap-2 px-3 py-1 rounded-full bg-red-50 text-sm text-red-700';
        text.textContent = ' Disconnected';
    }
}

function addTicket(data) {
    // Group by tableId ONLY (ignore batchId for grouping)
    const existingIndex = tickets.findIndex(t => t.tableId === data.tableId);

    if (existingIndex >= 0) {
        // Merge items into existing ticket
        const existingTicket = tickets[existingIndex];

        // Add new items
        data.items.forEach(newItem => {
            // Optional: Check for duplicates if needed, but usually we just append
            existingTicket.items.push({
                ...newItem,
                status: newItem.status || 'PENDING' // Ensure status exists
            });
        });

        // Update timestamp to latest
        existingTicket.timestamp = data.timestamp;

        // Update status: if ANY item is PENDING, ticket is PENDING
        existingTicket.status = existingTicket.items.every(i => ['READY', 'SERVED'].includes(i.status)) ? 'READY' : 'PENDING';

        // Move to top
        tickets.splice(existingIndex, 1);
        tickets.unshift(existingTicket);

    } else {
        // Create new ticket
        const ticket = {
            id: data.tableId.toString(), // ID is just tableId now
            tableId: data.tableId,
            tableName: data.tableName,
            batchId: -1, // Represents "All Batches"
            items: data.items.map(i => ({ ...i, status: i.status || 'PENDING' })),
            timestamp: data.timestamp,
            status: data.items.every(i => ['READY', 'SERVED'].includes(i.status)) ? 'READY' : 'PENDING'
        };
        tickets.unshift(ticket);
    }
    renderTickets();
}

function renderTickets() {
    let displayTickets = tickets;
    if (hideCompleted) {
        displayTickets = tickets.filter(t => t.status !== 'READY');
    }

    if (displayTickets.length === 0) {
        ticketGrid.innerHTML = '';
        emptyState.classList.remove('hidden');
        if (tickets.length > 0 && hideCompleted) {
            emptyState.querySelector('p').textContent = 'Tất cả đơn hàng đã hoàn thành';
        } else {
            emptyState.querySelector('p').textContent = 'Chưa có đơn hàng nào';
        }
        return;
    }

    emptyState.classList.add('hidden');
    ticketGrid.innerHTML = displayTickets.map(ticket => createTicketHTML(ticket)).join('');
}

function createTicketHTML(ticket) {
    const timeString = new Date(ticket.timestamp).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
    const isReady = ticket.status === 'READY';

    const cardClass = isReady ? 'bg-gray-50 opacity-75' : 'bg-white shadow-sm border-gray-100 ticket-new';
    const btnClass = isReady ? 'hidden' : 'w-full bg-white border border-gray-300 text-gray-700 font-medium py-2 px-4 rounded-lg hover:bg-gray-50';

    return `
        <div class="rounded-xl border overflow-hidden ticket-card ${cardClass}">
            <!-- Header -->
            <div class="px-4 py-3 border-b border-gray-100 flex justify-between items-center ${isReady ? 'bg-gray-100' : 'bg-gray-50'}">
                <div>
                    <span class="text-xs font-medium text-gray-500 uppercase tracking-wider">Bàn</span>
                    <div class="text-2xl font-bold text-gray-900 flex items-center gap-2">
                        ${ticket.tableName || 'Bàn ' + ticket.tableId}
                        ${isReady ? '<span class="text-green-500 text-lg">✓</span>' : ''}
                    </div>
                </div>
                <div class="text-right">
                    <!-- Removed Batch ID display as it's now merged -->
                    <div class="text-sm font-medium text-gray-900">${timeString}</div>
                </div>
            </div>
            
            <!-- Items -->
            <div class="p-4 space-y-3">
                ${ticket.items.map(item => {
        const itemReady = item.status === 'READY' || isReady;
        return `
                    <div class="flex justify-between items-center ${itemReady ? 'opacity-50' : ''}">
                        <span class="text-gray-700 font-medium ${!itemReady ? 'text-lg' : ''}">${item.name}</span>
                        <div class="flex items-center gap-2">
                            ${itemReady ? '<span class="text-green-500">✓</span>' : ''}
                            <span class="bg-blue-100 text-blue-800 text-sm font-bold px-2.5 py-0.5 rounded-full">x${item.quantity}</span>
                        </div>
                    </div>
                    `;
    }).join('')}
            </div>
            
            <!-- Actions -->
            <div class="px-4 py-3 bg-gray-50 border-t border-gray-100 ${isReady ? 'hidden' : ''}">
                <button onclick="completeTicket('${ticket.id}')" class="${btnClass}">
                    Hoàn thành
                </button>
            </div>
        </div>
    `;
}

async function completeTicket(ticketId) {
    const ticket = tickets.find(t => t.id === ticketId);
    if (!ticket) return;

    try {
        const response = await apiRequest('/api/kitchen/complete', {
            method: 'POST',
            body: JSON.stringify({
                tableId: ticket.tableId,
                batchId: -1 // Signal to complete ALL batches for this table
            })
        });

        if (response.ok) {
            // Update local state to READY without removing
            ticket.status = 'READY';
            ticket.items.forEach(i => i.status = 'READY');
            renderTickets();
        } else {
            console.error('Failed to complete ticket');
            alert('Lỗi khi hoàn thành đơn hàng');
        }
    } catch (error) {
        console.error('Error completing ticket:', error);
        alert('Lỗi kết nối');
    }
}

function logout() {
    clearAuth();
    window.location.href = 'login.html';
}
