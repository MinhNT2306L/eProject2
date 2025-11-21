// Main application logic
// Check authentication on load
if (!requireAuth()) {
    // Redirect will happen in requireAuth
    throw new Error('Not authenticated');
}

// Global state
let menuItems = [];
let filteredMenuItems = [];
let categories = [];
let tables = [];
let cart = [];
let activeOrders = [];
let currentOrderId = null;
let addItemsCart = []; // Cart for adding items to existing order
let currentView = 'menu';
const DESKTOP_BREAKPOINT = 1024;
let isCartOpen = false;

// Initialize app
document.addEventListener('DOMContentLoaded', () => {
    setupEventListeners();
    displayUserInfo();
    loadTables();
    loadMenu();
    loadActiveOrders();
    syncCartPanelState();
});

// Display user info
function displayUserInfo() {
    const user = getCurrentUser();
    const userInfo = document.getElementById('userInfo');
    if (user && userInfo) {
        userInfo.textContent = `👤 ${user.fullName || user.username}`;
    }
}

// Event listeners
function setupEventListeners() {
    // Navigation tabs
    document.querySelectorAll('.nav-tab').forEach(tab => {
        tab.addEventListener('click', (e) => {
            const view = e.target.getAttribute('data-view');
            switchView(view);
        });
    });

    // Logout
    document.getElementById('logoutBtn').addEventListener('click', () => {
        if (confirm('Bạn có chắc muốn đăng xuất?')) {
            clearAuth();
            window.location.href = 'login.html';
        }
    });

    // Menu view
    document.getElementById('refreshMenuBtn').addEventListener('click', () => {
        refreshMenu();
    });
    document.getElementById('clearCartBtn').addEventListener('click', clearCart);
    document.getElementById('submitOrderBtn').addEventListener('click', submitOrder);
    document.getElementById('menuSearch').addEventListener('input', filterMenu);

    // Orders view
    document.getElementById('refreshOrdersBtn').addEventListener('click', () => {
        refreshOrders();
    });

    // Order detail modal
    document.getElementById('addItemsBtn').addEventListener('click', addItemsToOrder);
    document.getElementById('addItemSearch').addEventListener('input', filterAddItemsMenu);
    document.getElementById('confirmPaymentBtn').addEventListener('click', processPayment);
    document.getElementById('amountPaid').addEventListener('input', updatePaymentAmount);

    // Success modal
    document.getElementById('closeModalBtn').addEventListener('click', () => {
        document.getElementById('successModal').classList.add('hidden');
    });

    // Cart interactions
    const cartFab = document.getElementById('cartFab');
    if (cartFab) {
        cartFab.addEventListener('click', () => {
            if (cart.length === 0) return;
            openCart();
        });
    }

    const cartBackdrop = document.getElementById('cartBackdrop');
    if (cartBackdrop) {
        cartBackdrop.addEventListener('click', closeCart);
    }

    const closeCartBtn = document.getElementById('closeCartBtn');
    if (closeCartBtn) {
        closeCartBtn.addEventListener('click', closeCart);
    }

    window.addEventListener('resize', syncCartPanelState);
}

// Switch between views
function switchView(view) {
    currentView = view;

    // Update tabs
    document.querySelectorAll('.nav-tab').forEach(tab => {
        if (tab.getAttribute('data-view') === view) {
            tab.classList.add('active', 'bg-white', 'text-slate-900', 'shadow-sm');
            tab.classList.remove('text-slate-600', 'hover:text-slate-900');
        } else {
            tab.classList.remove('active', 'bg-white', 'text-slate-900', 'shadow-sm');
            tab.classList.add('text-slate-600', 'hover:text-slate-900');
        }
    });

    // Update views
    document.getElementById('menuView').classList.toggle('hidden', view !== 'menu');
    document.getElementById('ordersView').classList.toggle('hidden', view !== 'orders');

    // Refresh data when switching to orders view
    if (view === 'orders') {
        refreshOrders();
    }
}

// Load tables
async function loadTables() {
    try {
        const response = await apiRequest('/api/tables');
        if (!response.ok) throw new Error('Failed to load tables');

        tables = await response.json();
        const select = document.getElementById('tableSelect');

        select.innerHTML = '<option value="">Chọn bàn...</option>';
        tables.forEach(table => {
            const option = document.createElement('option');
            option.value = table.id;

            const isOccupied = table.status !== 'TRONG';
            const statusText = isOccupied ? ' (Đang phục vụ)' : ' (Trống)';

            option.textContent = `Bàn ${table.number}${statusText}`;

            // Disable occupied tables to prevent duplicate orders
            if (isOccupied) {
                option.disabled = true;
                option.classList.add('text-slate-400', 'bg-slate-50'); // Optional styling hint
            }

            select.appendChild(option);
        });
    } catch (error) {
        console.error('Error loading tables:', error);
    }
}

// Load menu
async function loadMenu() {
    const loading = document.getElementById('menuLoading');
    const error = document.getElementById('menuError');
    const container = document.getElementById('menuContainer');

    loading.classList.remove('hidden');
    error.classList.add('hidden');
    container.classList.add('hidden');

    try {
        const response = await apiRequest('/api/menu');
        if (!response.ok) throw new Error('Failed to load menu');

        menuItems = await response.json();
        filteredMenuItems = [...menuItems];

        // Extract unique categories
        categories = [...new Set(menuItems.map(item => item.category).filter(c => c))];
        categories.sort();

        renderCategoryTabs();
        renderMenuItems();

        loading.classList.add('hidden');
        container.classList.remove('hidden');
    } catch (err) {
        loading.classList.add('hidden');
        error.classList.remove('hidden');
        error.textContent = `Lỗi: ${err.message}`;
    }
}

// Refresh menu with loading state
async function refreshMenu() {
    const btn = document.getElementById('refreshMenuBtn');
    const icon = btn.querySelector('.refresh-icon');

    btn.disabled = true;
    icon.classList.add('animate-spin');

    await loadMenu();
    await loadTables();

    btn.disabled = false;
    icon.classList.remove('animate-spin');
}

// Filter menu by search
function filterMenu() {
    const searchTerm = document.getElementById('menuSearch').value.toLowerCase().trim();

    if (searchTerm === '') {
        filteredMenuItems = [...menuItems];
    } else {
        filteredMenuItems = menuItems.filter(item =>
            item.name.toLowerCase().includes(searchTerm) ||
            (item.category && item.category.toLowerCase().includes(searchTerm)) ||
            (item.description && item.description.toLowerCase().includes(searchTerm))
        );
    }

    renderMenuItems();
}

// Render category tabs
function renderCategoryTabs() {
    const tabsContainer = document.querySelector('.category-tabs');
    tabsContainer.innerHTML = '<button class="category-tab px-5 py-2 bg-slate-900 text-white rounded-full text-sm font-medium shadow-lg shadow-slate-200/50 transition-all duration-200 whitespace-nowrap active" data-category="all">Tất cả</button>';

    categories.forEach(category => {
        const button = document.createElement('button');
        button.className = 'category-tab px-5 py-2 bg-white text-slate-600 border border-slate-200 rounded-full text-sm font-medium hover:bg-slate-50 hover:border-slate-300 transition-all duration-200 whitespace-nowrap';
        button.textContent = category;
        button.setAttribute('data-category', category);
        button.addEventListener('click', () => {
            // Reset all tabs
            document.querySelectorAll('.category-tab').forEach(tab => {
                tab.className = 'category-tab px-5 py-2 bg-white text-slate-600 border border-slate-200 rounded-full text-sm font-medium hover:bg-slate-50 hover:border-slate-300 transition-all duration-200 whitespace-nowrap';
            });
            // Activate clicked tab
            button.className = 'category-tab px-5 py-2 bg-slate-900 text-white rounded-full text-sm font-medium shadow-lg shadow-slate-200/50 transition-all duration-200 whitespace-nowrap active';
            filterMenuByCategory(category);
        });
        tabsContainer.appendChild(button);
    });

    // All tab click handler
    tabsContainer.querySelector('[data-category="all"]').addEventListener('click', (e) => {
        document.querySelectorAll('.category-tab').forEach(tab => {
            tab.className = 'category-tab px-5 py-2 bg-white text-slate-600 border border-slate-200 rounded-full text-sm font-medium hover:bg-slate-50 hover:border-slate-300 transition-all duration-200 whitespace-nowrap';
        });
        e.target.className = 'category-tab px-5 py-2 bg-slate-900 text-white rounded-full text-sm font-medium shadow-lg shadow-slate-200/50 transition-all duration-200 whitespace-nowrap active';
        filterMenuByCategory(null);
    });
}

// Filter menu by category
function filterMenuByCategory(category) {
    const searchTerm = document.getElementById('menuSearch').value.toLowerCase().trim();
    let filtered = category ? menuItems.filter(item => item.category === category) : menuItems;

    if (searchTerm) {
        filtered = filtered.filter(item =>
            item.name.toLowerCase().includes(searchTerm) ||
            (item.category && item.category.toLowerCase().includes(searchTerm)) ||
            (item.description && item.description.toLowerCase().includes(searchTerm))
        );
    }

    filteredMenuItems = filtered;
    renderMenuItems();
}

// Render menu items
function renderMenuItems() {
    const container = document.getElementById('menuItems');
    container.innerHTML = '';

    if (filteredMenuItems.length === 0) {
        container.innerHTML = '<div class="col-span-full text-center py-12 text-slate-400 text-sm">Không tìm thấy món ăn nào</div>';
        return;
    }

    // Setup Event Delegation if not already done
    if (!container.hasAttribute('data-delegated')) {
        container.setAttribute('data-delegated', 'true');
        container.addEventListener('click', (e) => {
            const target = e.target.closest('button');
            if (!target) return;

            const action = target.dataset.action;
            const id = parseInt(target.dataset.id);

            if (!action || !id) return;

            if (action === 'add') {
                addToCart(id);
            } else if (action === 'increase') {
                updateQuantity(id, 1);
            } else if (action === 'decrease') {
                updateQuantity(id, -1);
            }
        });
    }

    filteredMenuItems.forEach(item => {
        const cartItem = cart.find(ci => ci.foodId === item.id);
        const quantity = cartItem ? cartItem.quantity : 0;
        const isAvailable = item.status === 'CON_HANG';

        const itemDiv = document.createElement('div');
        itemDiv.id = `menu-item-${item.id}`;
        itemDiv.className = 'bg-white border border-slate-200 rounded-2xl p-4 flex gap-4 hover:border-primary-200 hover:shadow-lg hover:shadow-primary-500/5 transition-all duration-200 group';
        itemDiv.innerHTML = `
            <div class="flex-1 min-w-0">
                <div class="flex justify-between items-start mb-1">
                    <h3 class="text-base font-bold text-slate-900 truncate pr-2">${escapeHtml(item.name)}</h3>
                    <span class="text-lg font-bold text-primary-600 whitespace-nowrap">${formatPrice(item.price)}</span>
                </div>
                ${item.category ? `<div class="text-xs font-medium text-slate-400 uppercase tracking-wider mb-2">${escapeHtml(item.category)}</div>` : ''}
                ${item.description ? `<p class="text-sm text-slate-500 line-clamp-2 mb-3">${escapeHtml(item.description)}</p>` : ''}
                ${!isAvailable ? '<span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800">Hết hàng</span>' : ''}
            </div>
            <div id="action-${item.id}" class="flex flex-col justify-center items-end gap-2 min-w-[80px]">
                ${getMenuItemActionHtml(item, quantity)}
            </div>
        `;
        container.appendChild(itemDiv);
    });
}

function getMenuItemActionHtml(item, quantity) {
    const isAvailable = item.status === 'CON_HANG';

    if (quantity > 0) {
        return `
            <div class="flex items-center bg-slate-100 rounded-xl p-1">
                <button class="w-8 h-8 flex items-center justify-center bg-white text-slate-600 rounded-lg shadow-sm hover:text-primary-600 active:scale-95 transition-all" data-action="decrease" data-id="${item.id}" ${!isAvailable ? 'disabled' : ''}>-</button>
                <span class="w-8 text-center font-bold text-sm text-slate-900">${quantity}</span>
                <button class="w-8 h-8 flex items-center justify-center bg-primary-600 text-white rounded-lg shadow-sm shadow-primary-500/30 hover:bg-primary-700 active:scale-95 transition-all" data-action="increase" data-id="${item.id}" ${!isAvailable ? 'disabled' : ''}>+</button>
            </div>
        `;
    } else {
        return `
            <button class="w-full py-2 px-4 bg-slate-900 text-white rounded-xl text-sm font-semibold shadow-lg shadow-slate-900/10 hover:bg-slate-800 active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed transition-all" data-action="add" data-id="${item.id}" ${!isAvailable ? 'disabled' : ''}>
                ${isAvailable ? 'Thêm' : 'Hết'}
            </button>
        `;
    }
}

function updateMenuItem(foodId) {
    const item = menuItems.find(m => m.id === foodId);
    if (!item) return;

    const cartItem = cart.find(c => c.foodId === foodId);
    const quantity = cartItem ? cartItem.quantity : 0;

    const actionContainer = document.getElementById(`action-${foodId}`);
    if (actionContainer) {
        actionContainer.innerHTML = getMenuItemActionHtml(item, quantity);
    }
}

// Add to cart
function addToCart(foodId) {
    const tableSelect = document.getElementById('tableSelect');
    if (!tableSelect.value) {
        alert('Vui lòng chọn bàn trước khi gọi món!');
        tableSelect.focus();
        tableSelect.classList.add('ring-2', 'ring-red-500');
        setTimeout(() => tableSelect.classList.remove('ring-2', 'ring-red-500'), 2000);
        return;
    }

    const item = menuItems.find(m => m.id === foodId);
    if (!item || item.status !== 'CON_HANG') return;

    const existing = cart.find(c => c.foodId === foodId);
    if (existing) {
        existing.quantity++;
    } else {
        cart.push({
            foodId: item.id,
            name: item.name,
            price: item.price,
            quantity: 1
        });
    }

    updateCart();
    updateMenuItem(foodId);

    // Show toast instead of opening cart
    showToast(`Đã thêm ${item.name}`);

    // Animate FAB
    const fab = document.getElementById('cartFab');
    if (fab) {
        fab.classList.remove('bump');
        void fab.offsetWidth; // Trigger reflow
        fab.classList.add('bump');
    }
}

// Update quantity
function updateQuantity(foodId, delta) {
    const item = cart.find(c => c.foodId === foodId);
    if (!item) return;

    item.quantity += delta;
    if (item.quantity <= 0) {
        cart = cart.filter(c => c.foodId !== foodId);
    }

    updateCart();
    updateMenuItem(foodId);
}

// Update cart display
function updateCart() {
    const container = document.getElementById('cartItems');
    const totalElement = document.getElementById('cartTotal');
    const submitBtn = document.getElementById('submitOrderBtn');
    const cartPanel = document.getElementById('cartPanel');
    const cartBackdrop = document.getElementById('cartBackdrop');
    const cartFab = document.getElementById('cartFab');
    const cartFabCount = document.getElementById('cartFabCount');
    const cartFabTotal = document.getElementById('cartFabTotal');
    const cartSummaryLabel = document.getElementById('cartSummaryLabel');

    container.innerHTML = '';

    if (cart.length === 0) {
        container.innerHTML = '<div class="text-center py-8 text-slate-400 text-sm">Giỏ hàng trống</div>';
        totalElement.textContent = '0';
        submitBtn.disabled = true;
        if (cartFab) {
            cartFab.disabled = true;
            cartFab.classList.add('is-empty');
        }
        if (cartFabCount) {
            cartFabCount.textContent = '0 món';
        }
        if (cartFabTotal) {
            cartFabTotal.textContent = '0 đ';
        }
        cartSummaryLabel && (cartSummaryLabel.textContent = 'Chưa có món nào');
        // Removed auto-open/close cart logic when cart is empty
        return;
    }

    if (cartFab) {
        cartFab.disabled = false;
        cartFab.classList.remove('is-empty');
    }

    let total = 0;
    const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
    cart.forEach(item => {
        const itemTotal = item.price * item.quantity;
        total += itemTotal;

        const itemDiv = document.createElement('div');
        itemDiv.className = 'flex justify-between items-center py-2 border-b border-slate-50 last:border-0';
        itemDiv.innerHTML = `
            <div class="flex-1">
                <div class="font-medium text-slate-900 text-sm">${escapeHtml(item.name)}</div>
                <div class="text-xs text-slate-500">${item.quantity} x ${formatPrice(item.price)} đ</div>
            </div>
            <div class="flex items-center gap-3">
                <div class="flex items-center bg-slate-100 rounded-lg p-0.5">
                    <button class="w-6 h-6 flex items-center justify-center text-slate-500 hover:text-slate-900" onclick="updateQuantity(${item.foodId}, -1)">-</button>
                    <span class="w-6 text-center text-xs font-bold text-slate-900">${item.quantity}</span>
                    <button class="w-6 h-6 flex items-center justify-center text-slate-500 hover:text-slate-900" onclick="updateQuantity(${item.foodId}, 1)">+</button>
                </div>
                <div class="font-bold text-slate-900 text-sm min-w-[60px] text-right">${formatPrice(itemTotal)}</div>
            </div>
        `;
        container.appendChild(itemDiv);
    });

    totalElement.textContent = formatPrice(total);
    submitBtn.disabled = false;

    if (cartFabCount) {
        cartFabCount.textContent = `${totalItems} món`;
    }
    if (cartFabTotal) {
        cartFabTotal.textContent = `${formatPrice(total)} đ`;
    }
    if (cartSummaryLabel) {
        cartSummaryLabel.textContent = `${totalItems} món • ${formatPrice(total)} đ`;
    }

    if (isDesktopViewport()) {
        cartPanel?.classList.add('open');
        cartBackdrop?.classList.remove('visible');
    } else if (isCartOpen) {
        cartPanel?.classList.add('open');
        cartBackdrop?.classList.add('visible');
        document.body.classList.add('prevent-scroll');
    } else {
        cartPanel?.classList.remove('open');
        cartBackdrop?.classList.remove('visible');
        document.body.classList.remove('prevent-scroll');
    }
}

function isDesktopViewport() {
    return window.innerWidth >= DESKTOP_BREAKPOINT;
}

function openCart() {
    if (isDesktopViewport() || cart.length === 0) return;
    isCartOpen = true;
    const cartPanel = document.getElementById('cartPanel');
    const cartBackdrop = document.getElementById('cartBackdrop');
    cartPanel?.classList.add('open');
    cartBackdrop?.classList.add('visible');
    document.body.classList.add('prevent-scroll');
}

function closeCart() {
    isCartOpen = false;
    if (isDesktopViewport()) return;
    const cartPanel = document.getElementById('cartPanel');
    const cartBackdrop = document.getElementById('cartBackdrop');
    cartPanel?.classList.remove('open');
    cartBackdrop?.classList.remove('visible');
    document.body.classList.remove('prevent-scroll');
}

function syncCartPanelState() {
    if (isDesktopViewport()) {
        document.getElementById('cartPanel')?.classList.add('open');
        document.getElementById('cartBackdrop')?.classList.remove('visible');
        document.body.classList.remove('prevent-scroll');
        return;
    }

    if (isCartOpen && cart.length > 0) {
        openCart();
    } else {
        closeCart();
    }
}

// Clear cart
function clearCart() {
    if (confirm('Bạn có chắc muốn xóa tất cả món trong giỏ hàng?')) {
        cart = [];
        updateCart();
        renderMenuItems();
    }
}

// Submit order
async function submitOrder() {
    const submitBtn = document.getElementById('submitOrderBtn');
    const tableSelect = document.getElementById('tableSelect');

    if (!tableSelect.value) {
        alert('Vui lòng chọn bàn trước khi đặt món!');
        tableSelect.focus();
        return;
    }

    if (cart.length === 0) {
        alert('Giỏ hàng trống!');
        return;
    }

    submitBtn.disabled = true;
    submitBtn.textContent = 'Đang xử lý...';

    try {
        const orderData = {
            tableId: tableSelect.value ? parseInt(tableSelect.value) : null,
            items: cart.map(item => ({
                foodId: item.foodId,
                quantity: item.quantity
            }))
        };

        const response = await apiRequest('/api/orders', {
            method: 'POST',
            body: JSON.stringify(orderData)
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Failed to create order');
        }

        const result = await response.json();

        // Show success modal
        document.getElementById('successMessage').textContent =
            `Đơn hàng #${result.orderId} đã được tạo thành công!\nTổng tiền: ${formatPrice(result.total)} đ`;
        document.getElementById('successModal').classList.remove('hidden');

        // Clear cart and reset
        cart = [];
        updateCart();
        renderMenuItems();
        tableSelect.value = '';

        // Refresh orders if on orders view
        if (currentView === 'orders') {
            refreshOrders();
        }

    } catch (error) {
        alert(`Lỗi: ${error.message}`);
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Thanh toán / Đặt món';
    }
}

// Load active orders
async function loadActiveOrders() {
    const loading = document.getElementById('ordersLoading');
    const error = document.getElementById('ordersError');
    const container = document.getElementById('ordersList');

    if (currentView !== 'orders') return;

    loading.classList.remove('hidden');
    error.classList.add('hidden');
    container.innerHTML = '';

    try {
        const response = await apiRequest('/api/orders/active');
        if (!response.ok) throw new Error('Failed to load orders');

        activeOrders = await response.json();
        renderOrdersList();

        loading.classList.add('hidden');
    } catch (err) {
        loading.classList.add('hidden');
        error.classList.remove('hidden');
        error.textContent = `Lỗi: ${err.message}`;
    }
}

// Refresh orders with loading state
async function refreshOrders() {
    const btn = document.getElementById('refreshOrdersBtn');

    btn.disabled = true;
    btn.innerHTML = '<span>⏳</span> <span class="hidden sm:inline">Đang tải...</span>';

    await loadActiveOrders();

    btn.disabled = false;
    btn.innerHTML = '<span>🔄</span> <span class="hidden sm:inline">Làm mới</span>';
}

// Render orders list
function renderOrdersList() {
    const container = document.getElementById('ordersList');
    container.innerHTML = '';

    if (activeOrders.length === 0) {
        container.innerHTML = '<div class="text-center py-12 text-slate-400 text-sm bg-white rounded-2xl border border-slate-200 border-dashed">Không có đơn hàng nào đang phục vụ</div>';
        return;
    }

    activeOrders.forEach(order => {
        const orderDiv = document.createElement('div');
        orderDiv.className = 'bg-white border border-slate-200 rounded-2xl p-5 shadow-sm hover:shadow-md transition-all duration-200';
        orderDiv.innerHTML = `
            <div class="flex justify-between items-center mb-4 pb-4 border-b border-slate-100">
                <h3 class="text-base font-bold text-slate-900">Đơn hàng #${order.id}</h3>
                <span class="px-3 py-1 rounded-full text-xs font-bold bg-green-100 text-green-700 uppercase tracking-wide">${getStatusDisplay(order.status)}</span>
            </div>
            <div class="space-y-2 mb-5">
                <div class="flex justify-between text-sm">
                    <span class="text-slate-500">Bàn</span>
                    <span class="font-medium text-slate-900">${order.tableId > 0 ? `Bàn ${order.tableId}` : 'Không có'}</span>
                </div>
                <div class="flex justify-between text-sm">
                    <span class="text-slate-500">Tổng tiền</span>
                    <span class="font-bold text-primary-600">${formatPrice(order.total)} đ</span>
                </div>
                <div class="flex justify-between text-sm">
                    <span class="text-slate-500">Thời gian</span>
                    <span class="font-medium text-slate-900">${formatTime(order.time)}</span>
                </div>
            </div>
            <button class="w-full py-2.5 bg-white border border-slate-200 text-slate-700 rounded-xl font-semibold text-sm hover:bg-slate-50 hover:border-slate-300 active:scale-95 transition-all" onclick="viewOrderDetail(${order.id})">
                Xem chi tiết
            </button>
        `;
        container.appendChild(orderDiv);
    });
}

// View order detail
async function viewOrderDetail(orderId) {
    currentOrderId = orderId;
    const modal = document.getElementById('orderDetailModal');
    const backdrop = modal.querySelector('.modal-backdrop');
    const content = modal.querySelector('.modal-content');

    try {
        const response = await apiRequest(`/api/orders/${orderId}`);
        if (!response.ok) throw new Error('Failed to load order details');

        const order = await response.json();

        // Display order info
        document.getElementById('orderDetailId').textContent = order.id;
        document.getElementById('orderDetailTotal').textContent = formatPrice(order.total);

        const infoDiv = document.getElementById('orderDetailInfo');
        infoDiv.innerHTML = `
            <div class="grid grid-cols-2 gap-4">
                <div>
                    <span class="block text-xs text-slate-400 uppercase tracking-wider mb-1">Bàn</span>
                    <span class="font-medium text-slate-900">${order.tableId > 0 ? `Bàn ${order.tableId}` : 'Không có'}</span>
                </div>
                <div>
                    <span class="block text-xs text-slate-400 uppercase tracking-wider mb-1">Trạng thái</span>
                    <span class="font-medium text-slate-900">${getStatusDisplay(order.status)}</span>
                </div>
                <div class="col-span-2">
                    <span class="block text-xs text-slate-400 uppercase tracking-wider mb-1">Thời gian</span>
                    <span class="font-medium text-slate-900">${formatTime(order.time)}</span>
                </div>
            </div>
        `;

        // Display order items
        const itemsDiv = document.getElementById('orderDetailItems');
        itemsDiv.innerHTML = '';

        order.items.forEach(item => {
            const itemDiv = document.createElement('div');
            itemDiv.className = 'flex justify-between items-center py-2 border-b border-slate-50 last:border-0';
            itemDiv.innerHTML = `
                <div class="flex-1">
                    <div class="font-medium text-slate-900 text-sm">${escapeHtml(item.foodName)}</div>
                    <div class="text-xs text-slate-500">${item.quantity} x ${formatPrice(item.unitPrice)} đ</div>
                </div>
                <div class="font-bold text-slate-900 text-sm">${formatPrice(item.total)} đ</div>
            `;
            itemsDiv.appendChild(itemDiv);
        });

        // Update payment section based on order status
        updatePaymentSection(order);

        // Load menu for adding items (only if not paid)
        if (order.status !== 'DA_THANH_TOAN') {
            loadAddItemsMenu();
        } else {
            document.querySelector('.mt-8.pt-6.border-t').classList.add('hidden');
        }

        // Show modal with animation
        modal.classList.remove('hidden');
        // Trigger reflow
        void modal.offsetWidth;

        backdrop.classList.remove('opacity-0');
        content.classList.remove('opacity-0', 'translate-y-4', 'sm:translate-y-0', 'sm:scale-95');
        content.classList.add('opacity-100', 'translate-y-0', 'sm:scale-100');

    } catch (error) {
        alert(`Lỗi: ${error.message}`);
    }
}

// Update payment section
function updatePaymentSection(order) {
    const paymentStatus = document.getElementById('paymentStatus');
    const paymentForm = document.getElementById('paymentForm');
    const addItemsSection = document.querySelector('.mt-8.pt-6.border-t');

    if (order.status === 'DA_THANH_TOAN') {
        // Order is already paid
        paymentStatus.innerHTML = `
            <div class="flex items-center gap-2 p-3 bg-green-50 border border-green-100 rounded-xl text-green-700 text-sm font-medium">
                <span>✅</span>
                <span>Đã thanh toán</span>
            </div>
        `;
        paymentForm.classList.add('hidden');
        addItemsSection.classList.add('hidden');
    } else {
        // Order can be paid
        paymentStatus.innerHTML = '';
        paymentForm.classList.remove('hidden');
        addItemsSection.classList.remove('hidden');

        // Pre-fill amount with total
        const amountPaidInput = document.getElementById('amountPaid');
        amountPaidInput.value = Math.ceil(order.total);
    }
}

// Update payment amount (auto-fill with total)
function updatePaymentAmount() {
    const amountPaidInput = document.getElementById('amountPaid');
    const orderTotal = parseFloat(document.getElementById('orderDetailTotal').textContent.replace(/,/g, ''));

    // If user clears the input, auto-fill with total
    if (!amountPaidInput.value || amountPaidInput.value === '0') {
        amountPaidInput.value = Math.ceil(orderTotal);
    }
}

// Process payment
async function processPayment() {
    if (!currentOrderId) return;

    const btn = document.getElementById('confirmPaymentBtn');
    const paymentMethod = document.getElementById('paymentMethod').value;
    const amountPaidInput = document.getElementById('amountPaid');
    const note = document.getElementById('paymentNote').value.trim();

    const amountPaid = parseFloat(amountPaidInput.value);
    const orderTotal = parseFloat(document.getElementById('orderDetailTotal').textContent.replace(/,/g, ''));

    if (!amountPaid || amountPaid <= 0) {
        alert('Vui lòng nhập số tiền thanh toán');
        return;
    }

    if (amountPaid < orderTotal) {
        alert(`Số tiền thanh toán phải lớn hơn hoặc bằng tổng tiền (${formatPrice(orderTotal)} đ)`);
        return;
    }

    if (!confirm(`Xác nhận thanh toán ${formatPrice(amountPaid)} đ bằng ${paymentMethod === 'CASH' ? 'tiền mặt' : 'chuyển khoản'}?`)) {
        return;
    }

    btn.disabled = true;
    btn.textContent = 'Đang xử lý...';

    try {
        const response = await apiRequest(`/api/orders/${currentOrderId}/pay`, {
            method: 'POST',
            body: JSON.stringify({
                paymentMethod: paymentMethod,
                amountPaid: amountPaid,
                note: note || null
            })
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Failed to process payment');
        }

        const result = await response.json();

        // Show success message
        document.getElementById('successMessage').textContent =
            `Thanh toán thành công!\nĐơn hàng #${result.orderId}\nPhương thức: ${paymentMethod === 'CASH' ? 'Tiền mặt' : 'Chuyển khoản'}\nSố tiền: ${formatPrice(result.amountPaid)} đ`;
        document.getElementById('successModal').classList.remove('hidden');

        // Refresh order detail to show paid status
        await viewOrderDetail(currentOrderId);

        // Refresh orders list to remove paid order
        await refreshOrders();

        // Close modal after a moment
        setTimeout(() => {
            closeOrderDetail();
        }, 2000);

    } catch (error) {
        if (error.message.includes('ORDER_ALREADY_PAID')) {
            alert('Đơn hàng này đã được thanh toán rồi!');
            // Refresh order detail
            await viewOrderDetail(currentOrderId);
        } else {
            alert(`Lỗi: ${error.message}`);
        }
    } finally {
        btn.disabled = false;
        btn.textContent = 'Xác nhận thanh toán';
    }
}

// Close order detail modal
function closeOrderDetail() {
    const modal = document.getElementById('orderDetailModal');
    const backdrop = modal.querySelector('.modal-backdrop');
    const content = modal.querySelector('.modal-content');

    backdrop.classList.remove('opacity-100');
    backdrop.classList.add('opacity-0');

    content.classList.remove('opacity-100', 'translate-y-0', 'sm:scale-100');
    content.classList.add('opacity-0', 'translate-y-4', 'sm:translate-y-0', 'sm:scale-95');

    setTimeout(() => {
        modal.classList.add('hidden');
        currentOrderId = null;
        addItemsCart = [];
        document.getElementById('addItemSearch').value = '';
        document.getElementById('paymentNote').value = '';
    }, 200);
}

// Load menu for adding items to order
function loadAddItemsMenu() {
    const container = document.getElementById('addItemsMenu');
    const searchTerm = document.getElementById('addItemSearch').value.toLowerCase().trim();

    let filtered = menuItems;
    if (searchTerm) {
        filtered = menuItems.filter(item =>
            item.name.toLowerCase().includes(searchTerm) ||
            (item.category && item.category.toLowerCase().includes(searchTerm))
        );
    }

    container.innerHTML = '';

    if (filtered.length === 0) {
        container.innerHTML = '<div class="text-center py-4 text-slate-400 text-xs">Không tìm thấy món ăn</div>';
        return;
    }

    filtered.forEach(item => {
        if (item.status !== 'CON_HANG') return;

        const cartItem = addItemsCart.find(ci => ci.foodId === item.id);
        const quantity = cartItem ? cartItem.quantity : 0;

        const itemDiv = document.createElement('div');
        itemDiv.className = 'flex justify-between items-center p-3 bg-slate-50 rounded-xl border border-slate-100';
        itemDiv.innerHTML = `
            <div class="flex-1">
                <div class="font-medium text-slate-900 text-sm">${escapeHtml(item.name)}</div>
                <div class="text-xs text-slate-500">${formatPrice(item.price)} đ</div>
            </div>
            <div class="flex items-center gap-2">
                ${quantity > 0 ? `
                    <div class="flex items-center bg-white rounded-lg p-0.5 border border-slate-200">
                        <button class="w-6 h-6 flex items-center justify-center text-slate-500 hover:text-slate-900" onclick="updateAddItemQuantity(${item.id}, -1)">-</button>
                        <span class="w-6 text-center text-xs font-bold text-slate-900">${quantity}</span>
                        <button class="w-6 h-6 flex items-center justify-center text-slate-500 hover:text-slate-900" onclick="updateAddItemQuantity(${item.id}, 1)">+</button>
                    </div>
                ` : `
                    <button class="px-3 py-1.5 bg-white border border-slate-200 text-slate-700 rounded-lg text-xs font-medium hover:bg-slate-50 hover:border-slate-300 transition-all" onclick="addToAddItemsCart(${item.id})">Thêm</button>
                `}
            </div>
        `;
        container.appendChild(itemDiv);
    });

    updateAddItemsCart();
}

// Filter add items menu
function filterAddItemsMenu() {
    loadAddItemsMenu();
}

// Add to add items cart
function addToAddItemsCart(foodId) {
    const item = menuItems.find(m => m.id === foodId);
    if (!item || item.status !== 'CON_HANG') return;

    const existing = addItemsCart.find(c => c.foodId === foodId);
    if (existing) {
        existing.quantity++;
    } else {
        addItemsCart.push({
            foodId: item.id,
            name: item.name,
            price: item.price,
            quantity: 1
        });
    }

    loadAddItemsMenu();
}

// Update add item quantity
function updateAddItemQuantity(foodId, delta) {
    const item = addItemsCart.find(c => c.foodId === foodId);
    if (!item) return;

    item.quantity += delta;
    if (item.quantity <= 0) {
        addItemsCart = addItemsCart.filter(c => c.foodId !== foodId);
    }

    loadAddItemsMenu();
}

// Update add items cart display
function updateAddItemsCart() {
    const container = document.getElementById('addItemsCart');
    const btn = document.getElementById('addItemsBtn');

    container.innerHTML = '';

    if (addItemsCart.length === 0) {
        container.innerHTML = '<div class="text-center py-2 text-slate-400 text-xs italic">Chưa chọn món nào</div>';
        btn.disabled = true;
        return;
    }

    let total = 0;
    addItemsCart.forEach(item => {
        const itemTotal = item.price * item.quantity;
        total += itemTotal;

        const itemDiv = document.createElement('div');
        itemDiv.className = 'flex justify-between items-center text-sm py-1';
        itemDiv.innerHTML = `
            <span class="text-slate-600">${escapeHtml(item.name)} x ${item.quantity}</span>
            <span class="font-medium text-slate-900">${formatPrice(itemTotal)} đ</span>
        `;
        container.appendChild(itemDiv);
    });

    const totalDiv = document.createElement('div');
    totalDiv.className = 'flex justify-between items-center text-sm font-bold pt-2 mt-2 border-t border-slate-200';
    totalDiv.innerHTML = `
        <span class="text-slate-900">Tổng thêm</span>
        <span class="text-primary-600">${formatPrice(total)} đ</span>
    `;
    container.appendChild(totalDiv);

    btn.disabled = false;
}

// Add items to order
async function addItemsToOrder() {
    if (!currentOrderId || addItemsCart.length === 0) return;

    const btn = document.getElementById('addItemsBtn');
    btn.disabled = true;
    btn.textContent = 'Đang xử lý...';

    try {
        const response = await apiRequest(`/api/orders/${currentOrderId}/items`, {
            method: 'POST',
            body: JSON.stringify({
                items: addItemsCart.map(item => ({
                    foodId: item.foodId,
                    quantity: item.quantity
                }))
            })
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Failed to add items');
        }

        // Clear cart
        addItemsCart = [];
        document.getElementById('addItemSearch').value = '';

        // Refresh order detail
        await viewOrderDetail(currentOrderId);

        // Refresh orders list
        await refreshOrders();

    } catch (error) {
        alert(`Lỗi: ${error.message}`);
    } finally {
        btn.disabled = false;
        btn.textContent = 'Thêm vào đơn hàng';
    }
}

// Helper functions
function escapeHtml(unsafe) {
    if (!unsafe) return '';
    return unsafe
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

function formatPrice(price) {
    return new Intl.NumberFormat('vi-VN').format(price);
}

function formatTime(timeStr) {
    if (!timeStr) return '';
    const date = new Date(timeStr);
    return date.toLocaleString('vi-VN');
}

function getStatusDisplay(status) {
    const statusMap = {
        'MOI': 'Mới',
        'DANG_PHUC_VU': 'Đang phục vụ',
        'DA_THANH_TOAN': 'Đã thanh toán',
        'DA_HUY': 'Đã hủy'
    };
    return statusMap[status] || status;
}

// Toast Notification
function showToast(message, type = 'success') {
    const container = document.getElementById('toastContainer');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = 'toast-message';
    toast.innerHTML = `
        <span>${type === 'success' ? '✅' : 'ℹ️'}</span>
        <span>${escapeHtml(message)}</span>
    `;

    container.appendChild(toast);

    // Remove after 2 seconds
    setTimeout(() => {
        toast.classList.add('fade-out');
        toast.addEventListener('animationend', () => {
            toast.remove();
        });
    }, 2000);
}
