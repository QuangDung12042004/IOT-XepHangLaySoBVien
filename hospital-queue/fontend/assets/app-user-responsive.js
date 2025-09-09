// Responsive User App - Hospital Queue System
(function() {
    'use strict';

    // Configuration
    const API_BASE = 'http://localhost:8080/api';
    
    // State management
    let selectedDepartment = null;
    let departments = [];
    let currentTicket = null;
    
    // DOM Elements
    const elements = {
        departmentDropdown: document.getElementById('departmentDropdown'),
        departmentMenu: document.getElementById('departmentMenu'),
        loadingDepartments: document.getElementById('loadingDepartments'),
        patientName: document.getElementById('patientName'),
        queueInfo: document.getElementById('queueInfo'),
        currentQueue: document.getElementById('currentQueue'),
        waitingCount: document.getElementById('waitingCount'),
        avgWaitTime: document.getElementById('avgWaitTime'),
        estimatedTime: document.getElementById('estimatedTime'),
        btnTakeNumber: document.getElementById('btnTakeNumber'),
        errorMessage: document.getElementById('errorMessage'),
        errorText: document.getElementById('errorText'),
        ticketResult: document.getElementById('ticketResult'),
        ticketNumber: document.getElementById('ticketNumber'),
        ticketDepartment: document.getElementById('ticketDepartment'),
        ticketTime: document.getElementById('ticketTime'),
        queuePosition: document.getElementById('queuePosition'),
        notificationToast: document.getElementById('notificationToast')
    };

    // Utility functions
    function showError(message) {
        if (elements.errorText && elements.errorMessage) {
            elements.errorText.textContent = message;
            elements.errorMessage.style.display = 'block';
            setTimeout(() => {
                elements.errorMessage.style.display = 'none';
            }, 5000);
        }
    }

    function showToast(title, message, type = 'success') {
        if (!elements.notificationToast) return;
        
        const toastHtml = `
            <div class="toast align-items-center text-white bg-${type === 'success' ? 'success' : 'danger'} border-0" role="alert">
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
        
        // Auto remove after showing
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

    function getDepartmentIcon(deptCode) {
        const iconMap = {
            'K01': 'heart-pulse',
            'K02': 'scissors',
            'K03': 'emoji-smile',
            'K04': 'person-hearts',
            'K05': 'eye',
            'K06': 'activity',
            'K07': 'brain',
            'K08': 'bandaid',
            'EMG01': 'hospital',
            'VIP01': 'star',
            'REG01': 'clipboard',
            'PAY01': 'cash-coin'
        };
        return iconMap[deptCode] || 'hospital';
    }

    // API calls
    async function loadDepartments() {
        if (!elements.departmentMenu) return;
        
        try {
            if (elements.loadingDepartments) {
                elements.loadingDepartments.style.display = 'block';
            }
            
            const response = await fetch(`${API_BASE}/departments`, {
                headers: { 'Accept': 'application/json' }
            });
            if (!response.ok) {
                throw new Error('Không thể tải danh sách khoa');
            }
            departments = await response.json();
            renderDepartmentDropdown();
        } catch (error) {
            console.error('Error loading departments:', error);
            showError('Không thể tải danh sách khoa. Vui lòng thử lại.');
            if (elements.departmentMenu) {
                elements.departmentMenu.innerHTML = '<li><a class="dropdown-item disabled" href="#">Lỗi tải danh sách khoa</a></li>';
            }
        } finally {
            if (elements.loadingDepartments) {
                elements.loadingDepartments.style.display = 'none';
            }
        }
    }

    async function loadQueueInfo(departmentId) {
        try {
            const response = await fetch(`${API_BASE}/departments/${departmentId}/queue`);
            if (!response.ok) {
                throw new Error('Không thể tải thông tin hàng đợi');
            }
            const queueData = await response.json();
            updateQueueDisplay(queueData);
        } catch (error) {
            console.error('Error loading queue info:', error);
            showError('Không thể tải thông tin hàng đợi');
        }
    }

    async function takeNumber() {
        if (!selectedDepartment || !elements.patientName || !elements.patientName.value.trim()) {
            showError('Vui lòng chọn khoa và nhập tên');
            return;
        }

        try {
            if (elements.btnTakeNumber) {
                elements.btnTakeNumber.disabled = true;
                elements.btnTakeNumber.innerHTML = '<i class="bi bi-hourglass-split"></i> Đang xử lý...';
            }

            const response = await fetch(`${API_BASE}/tickets/take-number`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    departmentId: selectedDepartment.id,
                    name: elements.patientName.value.trim()
                })
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Không thể lấy số thứ tự');
            }

            const ticket = await response.json();
            showTicketResult(ticket);
            showToast('Thành công!', `Đã lấy số ${ticket.number} cho khoa ${selectedDepartment.name}`);

        } catch (error) {
            console.error('Error taking number:', error);
            showError(error.message);
        } finally {
            if (elements.btnTakeNumber) {
                elements.btnTakeNumber.disabled = false;
                elements.btnTakeNumber.innerHTML = '<i class="bi bi-plus-circle"></i> Lấy số thứ tự';
            }
        }
    }

    // UI rendering functions
    function renderDepartmentDropdown() {
        if (!elements.departmentMenu) return;
        
        if (!departments || departments.length === 0) {
            elements.departmentMenu.innerHTML = '<li><a class="dropdown-item disabled" href="#">Không có khoa/phòng khám</a></li>';
            return;
        }
        
        elements.departmentMenu.innerHTML = departments.map(dept => 
            `<li><a class="dropdown-item" href="#" data-dept-id="${dept.id}" data-dept-name="${dept.name}" data-dept-code="${dept.code || ''}">
                <i class="bi bi-${getDepartmentIcon(dept.code)} me-2"></i>${dept.name}
            </a></li>`
        ).join('');
        
        // Add click event listeners to dropdown items
        elements.departmentMenu.querySelectorAll('.dropdown-item:not(.disabled)').forEach(item => {
            item.addEventListener('click', function(e) {
                e.preventDefault();
                selectDepartment({
                    id: this.getAttribute('data-dept-id'),
                    name: this.getAttribute('data-dept-name'),
                    code: this.getAttribute('data-dept-code') || ''
                });
            });
        });
    }

    function selectDepartment(dept) {
        selectedDepartment = dept;
        
        // Update dropdown button text
        if (elements.departmentDropdown) {
            elements.departmentDropdown.innerHTML = `<i class="bi bi-${getDepartmentIcon(dept.code)}"></i> ${dept.name}`;
        }
        
        // Load queue info for selected department
        loadQueueInfo(dept.id);
        
        // Show queue info section
        if (elements.queueInfo) {
            elements.queueInfo.style.display = 'block';
        }
        
        // Enable take number button if name is entered
        checkFormValid();
        
        // Smooth scroll to queue info
        if (elements.queueInfo) {
            elements.queueInfo.scrollIntoView({ 
                behavior: 'smooth', 
                block: 'center' 
            });
        }
    }

    function updateQueueDisplay(queueData) {
        if (elements.currentQueue) {
            elements.currentQueue.textContent = queueData.currentNumber || '0';
        }
        if (elements.waitingCount) {
            elements.waitingCount.textContent = queueData.waitingCount || '0';
        }
        if (elements.avgWaitTime) {
            elements.avgWaitTime.textContent = queueData.avgWaitTime || '5';
        }
        if (elements.estimatedTime) {
            elements.estimatedTime.textContent = queueData.estimatedTime || '15';
        }
    }

    function showTicketResult(ticket) {
        if (!elements.ticketResult) return;
        
        currentTicket = ticket;
        
        if (elements.ticketNumber) {
            elements.ticketNumber.textContent = `${selectedDepartment.code}${String(ticket.number).padStart(3, '0')}`;
        }
        if (elements.ticketDepartment) {
            elements.ticketDepartment.textContent = selectedDepartment.name;
        }
        if (elements.ticketTime) {
            const now = new Date();
            elements.ticketTime.textContent = now.toLocaleTimeString('vi-VN', {
                hour: '2-digit',
                minute: '2-digit',
                second: '2-digit'
            });
        }
        if (elements.queuePosition) {
            elements.queuePosition.textContent = `${ticket.number}`;
        }
        
        // Bắt đầu đồng hồ đếm thời gian chờ
        startWaitingTimer();
        
        // Hide form sections and show result
        document.querySelectorAll('.form-card').forEach(card => {
            card.style.display = 'none';
        });
        
        elements.ticketResult.style.display = 'block';
        
        // Smooth scroll to result
        elements.ticketResult.scrollIntoView({ 
            behavior: 'smooth', 
            block: 'center' 
        });
    }

    // Đồng hồ đếm thời gian chờ
    let waitingInterval = null;
    let startTime = null;

    function startWaitingTimer() {
        startTime = new Date();
        const timerElement = document.getElementById('waitingTimer');
        
        if (!timerElement) return;
        
        // Clear previous interval if exists
        if (waitingInterval) {
            clearInterval(waitingInterval);
        }
        
        waitingInterval = setInterval(() => {
            const now = new Date();
            const elapsed = Math.floor((now - startTime) / 1000);
            
            const hours = Math.floor(elapsed / 3600);
            const minutes = Math.floor((elapsed % 3600) / 60);
            const seconds = elapsed % 60;
            
            const timeString = `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
            timerElement.innerHTML = `<i class="bi bi-clock me-2"></i>${timeString}`;
        }, 1000);
    }

    function checkFormValid() {
        const nameValid = elements.patientName && elements.patientName.value.trim().length > 0;
        const deptValid = selectedDepartment !== null;
        
        if (elements.btnTakeNumber) {
            elements.btnTakeNumber.disabled = !(nameValid && deptValid);
        }
    }

    // Event listeners
    function setupEventListeners() {
        // Name input validation
        if (elements.patientName) {
            elements.patientName.addEventListener('input', () => {
                checkFormValid();
                if (elements.errorMessage && elements.errorMessage.style.display === 'block') {
                    elements.errorMessage.style.display = 'none';
                }
            });

            elements.patientName.addEventListener('keypress', (e) => {
                if (e.key === 'Enter' && elements.btnTakeNumber && !elements.btnTakeNumber.disabled) {
                    takeNumber();
                }
            });
        }

        // Take number button
        if (elements.btnTakeNumber) {
            elements.btnTakeNumber.addEventListener('click', takeNumber);
        }

        // Handle mobile keyboard
        if (elements.patientName) {
            elements.patientName.addEventListener('focus', () => {
                // Scroll input into view on mobile
                setTimeout(() => {
                    elements.patientName.scrollIntoView({ 
                        behavior: 'smooth', 
                        block: 'center' 
                    });
                }, 300);
            });
        }
    }

    // Auto-refresh functionality
    function startAutoRefresh() {
        setInterval(() => {
            if (selectedDepartment) {
                loadQueueInfo(selectedDepartment.id);
            }
        }, 30000); // Refresh every 30 seconds
    }

    // Responsive utilities
    function handleResize() {
        // Adjust layout based on screen size
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
        // Check if all required elements exist
        const requiredElements = ['departmentDropdown', 'departmentMenu', 'patientName', 'btnTakeNumber'];
        const missingElements = requiredElements.filter(id => !document.getElementById(id));
        
        if (missingElements.length > 0) {
            console.error('Missing required elements:', missingElements);
            return;
        }

        // Setup event listeners
        setupEventListeners();
        
        // Load initial data
        loadDepartments();
        
        // Start auto-refresh
        startAutoRefresh();
        
        // Handle responsive
        handleResize();
        window.addEventListener('resize', handleResize);
        
        console.log('✅ Responsive User App initialized successfully');
    }

    // Start when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

})();
