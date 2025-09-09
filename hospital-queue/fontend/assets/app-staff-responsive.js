// Responsive Staff App - Hospital Queue System
(function() {
    'use strict';

    // Configuration
    const API_BASE = 'http://localhost:8080/api';
    
    // State management
    let isLoggedIn = false;
    let currentUser = null;
    let selectedDepartment = null;
    let currentTicket = null;
    let waitingTickets = [];
    
    // Storage keys
    const STORAGE_KEYS = {
        LOGGED_IN: 'staff_logged_in',
        CURRENT_USER: 'staff_current_user',
        CURRENT_TICKET: 'staff_current_ticket',
        SELECTED_DEPARTMENT: 'staff_selected_department'
    };
    
    // DOM Elements
    const elements = {
        loginForm: document.getElementById('loginForm'),
        staffLoginForm: document.getElementById('staffLoginForm'),
        mainContent: document.getElementById('mainContent'),
        username: document.getElementById('username'),
        password: document.getElementById('password'),
        departmentSelect: document.getElementById('departmentSelect'),
        currentNumber: document.getElementById('currentNumber'),
        currentDepartment: document.getElementById('currentDepartment'),
        btnCallNext: document.getElementById('btnCallNext'),
        btnComplete: document.getElementById('btnComplete'),
        btnSkip: document.getElementById('btnSkip'),
        btnRecall: document.getElementById('btnRecall'),
        btnRefresh: document.getElementById('btnRefresh'),
        btnSettings: document.getElementById('btnSettings'),
        btnLogout: document.getElementById('btnLogout'),
        totalToday: document.getElementById('totalToday'),
        completedToday: document.getElementById('completedToday'),
        waitingCount: document.getElementById('waitingCount'),
        avgTime: document.getElementById('avgTime'),
        waitingList: document.getElementById('waitingList'),
        waitingBadge: document.getElementById('waitingBadge'),
        notificationToast: document.getElementById('notificationToast')
    };

    // Utility functions
    function saveToStorage(key, value) {
        try {
            localStorage.setItem(key, JSON.stringify(value));
        } catch (error) {
            console.error('Error saving to localStorage:', error);
        }
    }
    
    function loadFromStorage(key) {
        try {
            const value = localStorage.getItem(key);
            return value ? JSON.parse(value) : null;
        } catch (error) {
            console.error('Error loading from localStorage:', error);
            return null;
        }
    }
    
    function clearStorage() {
        try {
            Object.values(STORAGE_KEYS).forEach(key => {
                localStorage.removeItem(key);
            });
        } catch (error) {
            console.error('Error clearing localStorage:', error);
        }
    }
    
    function checkExistingSession() {
        const savedLoginState = loadFromStorage(STORAGE_KEYS.LOGGED_IN);
        const savedUser = loadFromStorage(STORAGE_KEYS.CURRENT_USER);
        const savedTicket = loadFromStorage(STORAGE_KEYS.CURRENT_TICKET);
        const savedDepartment = loadFromStorage(STORAGE_KEYS.SELECTED_DEPARTMENT);
        
        if (savedLoginState && savedUser) {
            isLoggedIn = true;
            currentUser = savedUser;
            currentTicket = savedTicket;
            selectedDepartment = savedDepartment;
            
            showMainInterface();
            loadDashboardData();
            
            // Restore current ticket display if exists
            if (currentTicket) {
                elements.currentNumber.textContent = String(currentTicket.number).padStart(3, '0');
            }
            
            return true;
        }
        return false;
    }

    function showToast(title, message, type = 'success') {
        const toastHtml = `
            <div class="toast align-items-center text-white bg-${type === 'success' ? 'success' : type === 'warning' ? 'warning' : 'danger'} border-0" role="alert">
                <div class="d-flex">
                    <div class="toast-body">
                        <strong>${title}</strong><br>
                        ${message}
                    </div>
                    <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
                </div>
            </div>
        `;
        
        elements.notificationToast.insertAdjacentHTML('beforeend', toastHtml);
        const toastElement = elements.notificationToast.lastElementChild;
        const toast = new bootstrap.Toast(toastElement);
        toast.show();
        
        toastElement.addEventListener('hidden.bs.toast', () => {
            toastElement.remove();
        });
    }

    function formatTime(dateString) {
        const date = new Date(dateString);
        return date.toLocaleTimeString('vi-VN', { 
            hour: '2-digit', 
            minute: '2-digit' 
        });
    }

    function getWaitingTime(createdAt) {
        const now = new Date();
        const created = new Date(createdAt);
        const diffMinutes = Math.floor((now - created) / (1000 * 60));
        return diffMinutes;
    }

    // API calls
    async function login(username, password) {
        try {
            // Simple demo authentication
            if ((username === 'admin' && password === 'admin123') || 
                (username === 'staff' && password === 'staff123')) {
                
                currentUser = {
                    username: username,
                    role: username === 'admin' ? 'ADMIN' : 'STAFF',
                    fullName: username === 'admin' ? 'Administrator' : 'Staff User'
                };
                
                isLoggedIn = true;
                
                // Save to localStorage
                saveToStorage(STORAGE_KEYS.LOGGED_IN, true);
                saveToStorage(STORAGE_KEYS.CURRENT_USER, currentUser);
                
                showMainInterface();
                showToast('Đăng nhập thành công!', `Chào mừng ${currentUser.fullName}`);
                
                // Load initial data
                loadDashboardData();
                return true;
            } else {
                throw new Error('Tài khoản hoặc mật khẩu không đúng');
            }
            
        } catch (error) {
            showToast('Lỗi đăng nhập', error.message, 'danger');
            return false;
        }
    }

    async function loadDashboardData() {
        try {
            // Load departments first
            await loadDepartments();
            
            // Load stats
            await loadStats();
            
            // Load waiting list
            await loadWaitingList();
            
            // Load current ticket info
            await loadCurrentTicket();
            
        } catch (error) {
            console.error('Error loading dashboard data:', error);
            showToast('Lỗi', 'Không thể tải dữ liệu dashboard', 'danger');
        }
    }
    
    async function loadDepartments() {
        try {
            const response = await fetch(`${API_BASE}/departments`, {
                headers: { 'Accept': 'application/json' }
            });
            
            if (!response.ok) {
                throw new Error('Không thể tải danh sách khoa');
            }
            
            const departments = await response.json();
            
            // Clear existing options
            elements.departmentSelect.innerHTML = '<option value="">Chọn khoa...</option>';
            
            // Add departments to select
            departments.forEach(dept => {
                const option = document.createElement('option');
                option.value = dept.id;
                option.textContent = `${dept.name} (${dept.code})`;
                elements.departmentSelect.appendChild(option);
            });
            
            // Restore selected department if exists
            if (selectedDepartment) {
                elements.departmentSelect.value = selectedDepartment.id;
                elements.currentDepartment.textContent = selectedDepartment.name;
            }
            
        } catch (error) {
            console.error('Error loading departments:', error);
            showToast('Lỗi', 'Không thể tải danh sách khoa', 'warning');
        }
    }

    async function loadStats() {
        try {
            // Mock data for demo
            const stats = {
                totalToday: 45,
                completedToday: 38,
                waitingCount: 7,
                avgTime: 12
            };
            
            elements.totalToday.textContent = stats.totalToday;
            elements.completedToday.textContent = stats.completedToday;
            elements.waitingCount.textContent = stats.waitingCount;
            elements.avgTime.textContent = stats.avgTime;
            
        } catch (error) {
            console.error('Error loading stats:', error);
        }
    }

    async function loadWaitingList() {
        try {
            if (!selectedDepartment) {
                elements.waitingList.innerHTML = `
                    <div class="text-center py-4 text-muted">
                        <i class="bi bi-building" style="font-size: 2rem;"></i>
                        <p class="mt-2">Vui lòng chọn khoa để xem danh sách chờ</p>
                    </div>
                `;
                elements.waitingBadge.textContent = '0';
                return;
            }
            
            // Lấy danh sách tickets đang chờ từ API thật
            const response = await fetch(`${API_BASE}/tickets?status=WAITING&departmentId=${selectedDepartment.id}`, {
                headers: { 'Accept': 'application/json' }
            });
            
            if (!response.ok) {
                throw new Error('Không thể tải danh sách chờ');
            }
            
            waitingTickets = await response.json();
            renderWaitingList();
            
        } catch (error) {
            console.error('Error loading waiting list:', error);
            // Fallback to mock data nếu API fail
            const mockTickets = [
                {
                    id: 1,
                    number: 23,
                    holderName: 'Nguyễn Văn A',
                    createdAt: new Date(Date.now() - 15 * 60000).toISOString(),
                    status: 'WAITING'
                },
                {
                    id: 2,
                    number: 24,
                    holderName: 'Trần Thị B',
                    createdAt: new Date(Date.now() - 10 * 60000).toISOString(),
                    status: 'WAITING'
                },
                {
                    id: 3,
                    number: 25,
                    holderName: 'Lê Văn C',
                    createdAt: new Date(Date.now() - 5 * 60000).toISOString(),
                    status: 'WAITING'
                }
            ];
            
            waitingTickets = mockTickets;
            renderWaitingList();
            elements.waitingList.innerHTML = '<div class="text-center py-4 text-warning">Sử dụng dữ liệu demo</div>';
        }
    }

    async function loadCurrentTicket() {
        try {
            if (!selectedDepartment) {
                elements.currentNumber.textContent = '000';
                elements.currentDepartment.textContent = 'Chưa chọn khoa';
                return;
            }
            
            // Lấy ticket hiện tại đang được gọi
            const response = await fetch(`${API_BASE}/tickets?status=CALLED&departmentId=${selectedDepartment.id}`, {
                headers: { 'Accept': 'application/json' }
            });
            
            if (response.ok) {
                const calledTickets = await response.json();
                if (calledTickets && calledTickets.length > 0) {
                    // Lấy ticket được gọi gần nhất
                    const latestCalled = calledTickets[0];
                    currentTicket = latestCalled;
                    elements.currentNumber.textContent = String(latestCalled.number).padStart(3, '0');
                    saveToStorage(STORAGE_KEYS.CURRENT_TICKET, currentTicket);
                } else {
                    // Không có ticket nào đang được gọi
                    if (!currentTicket) {
                        elements.currentNumber.textContent = '000';
                    }
                }
            }
            
            elements.currentDepartment.textContent = selectedDepartment.name;
            
        } catch (error) {
            console.error('Error loading current ticket:', error);
            // Fallback to mock if needed
            if (!currentTicket) {
                elements.currentNumber.textContent = '000';
            }
            if (selectedDepartment) {
                elements.currentDepartment.textContent = selectedDepartment.name;
            }
        }
    }

    async function callNextTicket() {
        try {
            if (!selectedDepartment) {
                showToast('Lỗi', 'Vui lòng chọn khoa trước khi gọi số', 'warning');
                return;
            }
            
            // Gọi API để lấy ticket tiếp theo
            const response = await fetch(`${API_BASE}/tickets/call-next/${selectedDepartment.id}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' }
            });
            
            if (!response.ok) {
                if (response.status === 404) {
                    showToast('Thông báo', 'Không có vé nào đang chờ', 'warning');
                    return;
                }
                throw new Error('Không thể gọi số');
            }
            
            const nextTicket = await response.json();
            
            // Update current display
            elements.currentNumber.textContent = String(nextTicket.number).padStart(3, '0');
            currentTicket = nextTicket;
            
            // Save current ticket to localStorage
            saveToStorage(STORAGE_KEYS.CURRENT_TICKET, currentTicket);
            
            // Reload waiting list
            await loadWaitingList();
            
            showToast('Gọi số thành công!', `Đã gọi số ${nextTicket.number} - ${nextTicket.holderName || nextTicket.name}`);
            
        } catch (error) {
            console.error('Error calling next ticket:', error);
            showToast('Lỗi', 'Không thể gọi số tiếp theo', 'danger');
        }
    }

    async function completeCurrentTicket() {
        try {
            if (!currentTicket) {
                showToast('Thông báo', 'Không có bệnh nhân nào đang khám', 'warning');
                return;
            }
            
            // Gọi API để hoàn thành ticket
            const response = await fetch(`${API_BASE}/tickets/complete/${currentTicket.id}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' }
            });
            
            if (!response.ok) {
                throw new Error('Không thể hoàn thành khám');
            }
            
            showToast('Hoàn thành!', `Đã hoàn thành khám cho ${currentTicket.holderName || currentTicket.name}`);
            
            // Reset current ticket
            currentTicket = null;
            elements.currentNumber.textContent = '000';
            
            // Clear current ticket from localStorage
            saveToStorage(STORAGE_KEYS.CURRENT_TICKET, null);
            
            // Reload data
            await loadWaitingList();
            
        } catch (error) {
            console.error('Error completing ticket:', error);
            showToast('Lỗi', 'Không thể hoàn thành khám', 'danger');
        }
    }

    async function skipCurrentTicket() {
        try {
            showToast('Bỏ qua', 'Đã bỏ qua bệnh nhân hiện tại', 'warning');
            
            // Reset current ticket
            currentTicket = null;
            elements.currentNumber.textContent = '000';
            
            // Clear current ticket from localStorage
            saveToStorage(STORAGE_KEYS.CURRENT_TICKET, null);
            
            // Auto call next if available
            if (waitingTickets.length > 0) {
                setTimeout(callNextTicket, 1000);
            }
            
        } catch (error) {
            showToast('Lỗi', 'Không thể bỏ qua', 'danger');
        }
    }

    async function recallCurrentTicket() {
        try {
            const currentNum = elements.currentNumber.textContent;
            showToast('Gọi lại', `Đã gọi lại số ${currentNum}`, 'info');
            
        } catch (error) {
            showToast('Lỗi', 'Không thể gọi lại', 'danger');
        }
    }

    // UI functions
    function showMainInterface() {
        elements.loginForm.style.display = 'none';
        elements.mainContent.style.display = 'block';
    }

    function showLoginInterface() {
        elements.loginForm.style.display = 'block';
        elements.mainContent.style.display = 'none';
        isLoggedIn = false;
        currentUser = null;
        currentTicket = null;
        
        // Clear all localStorage
        clearStorage();
        
        // Reset form
        if (elements.username) elements.username.value = '';
        if (elements.password) elements.password.value = '';
    }

    function renderWaitingList() {
        if (waitingTickets.length === 0) {
            elements.waitingList.innerHTML = `
                <div class="text-center py-4 text-muted">
                    <i class="bi bi-inbox" style="font-size: 2rem;"></i>
                    <p class="mt-2">Không có bệnh nhân nào đang chờ</p>
                </div>
            `;
            elements.waitingBadge.textContent = '0';
            return;
        }

        const listHtml = waitingTickets.map(ticket => `
            <div class="ticket-item">
                <div class="ticket-number">${String(ticket.number).padStart(3, '0')}</div>
                <div class="ticket-info">
                    <div class="ticket-name">${ticket.holderName}</div>
                    <div class="ticket-time">
                        <i class="bi bi-clock"></i>
                        ${formatTime(ticket.createdAt)} - Chờ ${getWaitingTime(ticket.createdAt)} phút
                    </div>
                </div>
                <div class="ticket-actions">
                    <button class="btn btn-sm btn-success btn-sm-action" onclick="callSpecificTicket(${ticket.id})">
                        <i class="bi bi-megaphone"></i>
                    </button>
                    <button class="btn btn-sm btn-warning btn-sm-action" onclick="postponeTicket(${ticket.id})">
                        <i class="bi bi-clock"></i>
                    </button>
                </div>
            </div>
        `).join('');

        elements.waitingList.innerHTML = listHtml;
        elements.waitingBadge.textContent = waitingTickets.length;
    }

    // Event handlers
    function setupEventListeners() {
        // Login form
        if (elements.staffLoginForm) {
            elements.staffLoginForm.addEventListener('submit', async (e) => {
                e.preventDefault();
                const username = elements.username.value.trim();
                const password = elements.password.value.trim();
                
                if (username && password) {
                    await login(username, password);
                }
            });
        }

        // Department selection
        if (elements.departmentSelect) {
            elements.departmentSelect.addEventListener('change', async (e) => {
                const departmentId = e.target.value;
                
                if (departmentId) {
                    // Find selected department
                    const option = e.target.selectedOptions[0];
                    selectedDepartment = {
                        id: parseInt(departmentId),
                        name: option.textContent.split(' (')[0], // Get name without code
                        code: option.textContent.match(/\(([^)]+)\)/)?.[1] || ''
                    };
                    
                    // Save to localStorage
                    saveToStorage(STORAGE_KEYS.SELECTED_DEPARTMENT, selectedDepartment);
                    
                    // Update display
                    elements.currentDepartment.textContent = selectedDepartment.name;
                    
                    // Reload waiting list for this department
                    await loadWaitingList();
                    
                    showToast('Chọn khoa', `Đã chọn ${selectedDepartment.name}`, 'info');
                } else {
                    selectedDepartment = null;
                    saveToStorage(STORAGE_KEYS.SELECTED_DEPARTMENT, null);
                    elements.currentDepartment.textContent = 'Chưa chọn khoa';
                    await loadWaitingList();
                }
            });
        }

        // Control buttons
        if (elements.btnCallNext) {
            elements.btnCallNext.addEventListener('click', callNextTicket);
        }

        if (elements.btnComplete) {
            elements.btnComplete.addEventListener('click', completeCurrentTicket);
        }

        if (elements.btnSkip) {
            elements.btnSkip.addEventListener('click', skipCurrentTicket);
        }

        if (elements.btnRecall) {
            elements.btnRecall.addEventListener('click', recallCurrentTicket);
        }

        if (elements.btnRefresh) {
            elements.btnRefresh.addEventListener('click', loadDashboardData);
        }

        if (elements.btnLogout) {
            elements.btnLogout.addEventListener('click', () => {
                showToast('Đăng xuất', 'Đã đăng xuất thành công', 'info');
                showLoginInterface();
            });
        }

        // Keyboard shortcuts
        document.addEventListener('keydown', (e) => {
            if (!isLoggedIn) return;
            
            if (e.ctrlKey || e.metaKey) {
                switch (e.key) {
                    case '1':
                        e.preventDefault();
                        callNextTicket();
                        break;
                    case '2':
                        e.preventDefault();
                        completeCurrentTicket();
                        break;
                    case '3':
                        e.preventDefault();
                        skipCurrentTicket();
                        break;
                    case '4':
                        e.preventDefault();
                        recallCurrentTicket();
                        break;
                }
            }
        });
    }

    // Global functions for onclick handlers
    window.callSpecificTicket = async function(ticketId) {
        try {
            const ticket = waitingTickets.find(t => t.id === ticketId);
            if (!ticket) {
                showToast('Lỗi', 'Không tìm thấy vé', 'danger');
                return;
            }
            
            // Gọi API để cập nhật status thành CALLED
            const response = await fetch(`${API_BASE}/tickets/${ticketId}/call`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' }
            });
            
            if (!response.ok) {
                throw new Error('Không thể gọi số');
            }
            
            const calledTicket = await response.json();
            
            // Update current display
            elements.currentNumber.textContent = String(ticket.number).padStart(3, '0');
            currentTicket = calledTicket;
            
            // Save current ticket to localStorage
            saveToStorage(STORAGE_KEYS.CURRENT_TICKET, currentTicket);
            
            // Reload waiting list để cập nhật danh sách
            await loadWaitingList();
            
            showToast('Gọi số', `Đã gọi số ${ticket.number} - ${ticket.holderName}`);
            
        } catch (error) {
            console.error('Error calling specific ticket:', error);
            showToast('Lỗi', 'Không thể gọi số này', 'danger');
        }
    };

    window.postponeTicket = function(ticketId) {
        showToast('Hoãn lại', 'Đã hoãn vé này', 'warning');
    };

    // Auto-refresh functionality
    function startAutoRefresh() {
        setInterval(() => {
            if (isLoggedIn) {
                loadWaitingList();
                loadStats();
            }
        }, 30000); // Refresh every 30 seconds
    }

    // Responsive utilities
    function handleResize() {
        const isMobile = window.innerWidth < 768;
        
        if (isMobile) {
            // Mobile optimizations
            document.body.style.paddingBottom = '2rem';
        } else {
            // Desktop optimizations
            document.body.style.paddingBottom = '0';
        }
    }

    // Initialize app
    function init() {
        // Check for existing session first
        const hasSession = checkExistingSession();
        
        // Setup event listeners
        setupEventListeners();
        
        // Show appropriate interface
        if (!hasSession) {
            showLoginInterface();
        }
        
        // Start auto-refresh
        startAutoRefresh();
        
        // Handle responsive
        handleResize();
        window.addEventListener('resize', handleResize);
        
        console.log('✅ Responsive Staff App initialized successfully');
    }

    // Start when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

})();
