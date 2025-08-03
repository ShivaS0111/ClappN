// Global variables
let businessesData = [];
let storesData = [];
let servicesData = [];
let pricingData = [];

// API Base URL
const API_BASE = '';

// Initialize dashboard
document.addEventListener('DOMContentLoaded', function() {
    initializeDashboard();
    setupEventListeners();
});

// Setup event listeners
function setupEventListeners() {
    // Mobile menu toggle
    document.getElementById('mobile-menu-btn').addEventListener('click', toggleSidebar);
    
    // Forms
    document.getElementById('add-business-form').addEventListener('submit', handleAddBusiness);
    document.getElementById('add-store-form').addEventListener('submit', handleAddStore);
    
    // Search functionality
    document.getElementById('search-input').addEventListener('input', handleSearch);
    
    // Close modals when clicking outside
    document.addEventListener('click', function(e) {
        if (e.target.classList.contains('fixed') && e.target.classList.contains('inset-0')) {
            hideModals();
        }
    });
}

// Toggle sidebar for mobile
function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    sidebar.classList.toggle('sidebar-hidden');
}

// Show specific section
function showSection(sectionName) {
    // Hide all sections
    document.querySelectorAll('.section-content').forEach(section => {
        section.classList.add('hidden');
    });
    
    // Show selected section
    document.getElementById(sectionName + '-section').classList.remove('hidden');
    
    // Update navigation
    updateNavigation(sectionName);
    
    // Update page title
    updatePageTitle(sectionName);
    
    // Load section data
    loadSectionData(sectionName);
    
    // Close sidebar on mobile
    if (window.innerWidth < 1024) {
        document.getElementById('sidebar').classList.add('sidebar-hidden');
    }
}

// Update navigation active state
function updateNavigation(activeSection) {
    document.querySelectorAll('.nav-item').forEach(item => {
        item.classList.remove('bg-blue-100', 'text-blue-700', 'border', 'border-blue-200');
        item.classList.add('text-gray-600', 'hover:bg-gray-100', 'hover:text-gray-900');
    });
    
    const activeItem = document.querySelector(`[onclick="showSection('${activeSection}')"]`);
    if (activeItem) {
        activeItem.classList.remove('text-gray-600', 'hover:bg-gray-100', 'hover:text-gray-900');
        activeItem.classList.add('bg-blue-100', 'text-blue-700', 'border', 'border-blue-200');
    }
}

// Update page title
function updatePageTitle(section) {
    const titles = {
        dashboard: { title: 'Dashboard', subtitle: 'Overview of your multi-vendor marketplace' },
        businesses: { title: 'Businesses', subtitle: 'Manage your business partners' },
        stores: { title: 'Stores', subtitle: 'Manage individual stores' },
        services: { title: 'Services', subtitle: 'Manage store services' },
        pricing: { title: 'Pricing', subtitle: 'Manage store item pricing' }
    };
    
    const pageInfo = titles[section] || { title: 'Dashboard', subtitle: 'Overview' };
    document.getElementById('page-title').textContent = pageInfo.title;
    document.getElementById('page-subtitle').textContent = pageInfo.subtitle;
}

// Load section data
function loadSectionData(section) {
    switch(section) {
        case 'dashboard':
            loadDashboardData();
            break;
        case 'businesses':
            loadBusinesses();
            break;
        case 'stores':
            loadStores();
            break;
        case 'services':
            loadServices();
            break;
        case 'pricing':
            loadPricing();
            break;
    }
}

// Initialize dashboard
async function initializeDashboard() {
    await loadDashboardData();
    await loadBusinesses(); // Load businesses for modals
}

// Load dashboard data
async function loadDashboardData() {
    try {
        // Load all data for dashboard stats
        const [businessesResponse, storesResponse, servicesResponse, pricingResponse] = await Promise.all([
            fetch(`${API_BASE}/business/list`),
            fetch(`${API_BASE}/stores/list`),
            fetch(`${API_BASE}/store-offered-service/list`).catch(() => ({ ok: false })),
            fetch(`${API_BASE}/store-item-price/list`).catch(() => ({ ok: false }))
        ]);

        if (businessesResponse.ok) {
            const businessesData = await businessesResponse.json();
            updateDashboardStats('total-businesses', businessesData.data?.length || 0);
            createBusinessStatusChart(businessesData.data || []);
        }

        if (storesResponse.ok) {
            const storesData = await storesResponse.json();
            updateDashboardStats('total-stores', storesData.data?.length || 0);
            createStoreStatusChart(storesData.data || []);
        }

        if (servicesResponse.ok) {
            const servicesData = await servicesResponse.json();
            updateDashboardStats('total-services', servicesData.data?.length || 0);
        }

        if (pricingResponse.ok) {
            const pricingData = await pricingResponse.json();
            updateDashboardStats('total-prices', pricingData.data?.length || 0);
        }

    } catch (error) {
        console.error('Error loading dashboard data:', error);
        showNotification('Error loading dashboard data', 'error');
    }
}

// Update dashboard stats
function updateDashboardStats(elementId, value) {
    const element = document.getElementById(elementId);
    if (element) {
        element.textContent = value;
    }
}

// Create business status chart
function createBusinessStatusChart(businesses) {
    const ctx = document.getElementById('business-status-chart');
    if (!ctx) return;

    const statusCounts = businesses.reduce((acc, business) => {
        const status = business.status === 1 ? 'Active' : 'Inactive';
        acc[status] = (acc[status] || 0) + 1;
        return acc;
    }, {});

    new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: Object.keys(statusCounts),
            datasets: [{
                data: Object.values(statusCounts),
                backgroundColor: ['#10B981', '#EF4444'],
                borderWidth: 2,
                borderColor: '#fff'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom'
                }
            }
        }
    });
}

// Create store status chart
function createStoreStatusChart(stores) {
    const ctx = document.getElementById('store-status-chart');
    if (!ctx) return;

    const statusCounts = stores.reduce((acc, store) => {
        const status = store.status === 1 ? 'Active' : 'Inactive';
        acc[status] = (acc[status] || 0) + 1;
        return acc;
    }, {});

    new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: Object.keys(statusCounts),
            datasets: [{
                data: Object.values(statusCounts),
                backgroundColor: ['#3B82F6', '#F59E0B'],
                borderWidth: 2,
                borderColor: '#fff'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom'
                }
            }
        }
    });
}

// Load businesses
async function loadBusinesses() {
    try {
        showLoading('businesses-loading');
        const response = await fetch(`${API_BASE}/business/list`);
        
        if (!response.ok) {
            throw new Error('Failed to fetch businesses');
        }
        
        const data = await response.json();
        businessesData = data.data || [];
        renderBusinessesTable(businessesData);
        populateBusinessSelect();
        hideLoading('businesses-loading');
        
    } catch (error) {
        console.error('Error loading businesses:', error);
        showNotification('Error loading businesses', 'error');
        hideLoading('businesses-loading');
    }
}

// Render businesses table
function renderBusinessesTable(businesses) {
    const tbody = document.getElementById('businesses-tbody');
    const table = document.getElementById('businesses-table');
    
    if (!tbody || !table) return;
    
    tbody.innerHTML = '';
    
    if (businesses.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="6" class="px-6 py-8 text-center text-gray-500">
                    <i class="fas fa-building text-4xl mb-4 text-gray-300"></i>
                    <p>No businesses found</p>
                </td>
            </tr>
        `;
    } else {
        businesses.forEach(business => {
            const row = document.createElement('tr');
            row.className = 'hover:bg-gray-50';
            row.innerHTML = `
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${business.id}</td>
                <td class="px-6 py-4 whitespace-nowrap">
                    <div class="text-sm font-medium text-gray-900">${business.businessName || 'N/A'}</div>
                </td>
                <td class="px-6 py-4">
                    <div class="text-sm text-gray-900 max-w-xs truncate">${business.description || 'No description'}</div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                    <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${business.status === 1 ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}">
                        ${business.status === 1 ? 'Active' : 'Inactive'}
                    </span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${business.stores?.length || 0}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                    <button onclick="viewBusiness(${business.id})" class="text-blue-600 hover:text-blue-900 mr-3">
                        <i class="fas fa-eye"></i>
                    </button>
                    <button onclick="editBusiness(${business.id})" class="text-green-600 hover:text-green-900 mr-3">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button onclick="deleteBusiness(${business.id})" class="text-red-600 hover:text-red-900">
                        <i class="fas fa-trash"></i>
                    </button>
                </td>
            `;
            tbody.appendChild(row);
        });
    }
    
    table.classList.remove('hidden');
}

// Load stores
async function loadStores() {
    try {
        showLoading('stores-loading');
        const response = await fetch(`${API_BASE}/stores/list`);
        
        if (!response.ok) {
            throw new Error('Failed to fetch stores');
        }
        
        const data = await response.json();
        storesData = data.data || [];
        renderStoresTable(storesData);
        hideLoading('stores-loading');
        
    } catch (error) {
        console.error('Error loading stores:', error);
        showNotification('Error loading stores', 'error');
        hideLoading('stores-loading');
    }
}

// Render stores table
function renderStoresTable(stores) {
    const tbody = document.getElementById('stores-tbody');
    const table = document.getElementById('stores-table');
    
    if (!tbody || !table) return;
    
    tbody.innerHTML = '';
    
    if (stores.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="6" class="px-6 py-8 text-center text-gray-500">
                    <i class="fas fa-store text-4xl mb-4 text-gray-300"></i>
                    <p>No stores found</p>
                </td>
            </tr>
        `;
    } else {
        stores.forEach(store => {
            const row = document.createElement('tr');
            row.className = 'hover:bg-gray-50';
            row.innerHTML = `
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${store.id}</td>
                <td class="px-6 py-4 whitespace-nowrap">
                    <div class="text-sm font-medium text-gray-900">${store.storeName || 'N/A'}</div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                    <div class="text-sm text-gray-900">${store.business?.businessName || 'No Business'}</div>
                </td>
                <td class="px-6 py-4">
                    <div class="text-sm text-gray-900 max-w-xs truncate">${store.description || 'No description'}</div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                    <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${store.status === 1 ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}">
                        ${store.status === 1 ? 'Active' : 'Inactive'}
                    </span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                    <button onclick="viewStore(${store.id})" class="text-blue-600 hover:text-blue-900 mr-3">
                        <i class="fas fa-eye"></i>
                    </button>
                    <button onclick="editStore(${store.id})" class="text-green-600 hover:text-green-900 mr-3">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button onclick="deleteStore(${store.id})" class="text-red-600 hover:text-red-900">
                        <i class="fas fa-trash"></i>
                    </button>
                </td>
            `;
            tbody.appendChild(row);
        });
    }
    
    table.classList.remove('hidden');
}

// Load services
async function loadServices() {
    try {
        showLoading('services-loading');
        // Try to load services (endpoint might not exist yet)
        const response = await fetch(`${API_BASE}/store-offered-service/list`);
        
        if (response.ok) {
            const data = await response.json();
            servicesData = data.data || [];
            renderServicesGrid(servicesData);
        } else {
            // Show placeholder if endpoint doesn't exist
            renderServicesPlaceholder();
        }
        
        hideLoading('services-loading');
        document.getElementById('services-content').classList.remove('hidden');
        
    } catch (error) {
        console.error('Error loading services:', error);
        renderServicesPlaceholder();
        hideLoading('services-loading');
        document.getElementById('services-content').classList.remove('hidden');
    }
}

// Render services grid
function renderServicesGrid(services) {
    const grid = document.getElementById('services-grid');
    if (!grid) return;
    
    grid.innerHTML = '';
    
    if (services.length === 0) {
        grid.innerHTML = `
            <div class="col-span-full text-center py-8">
                <i class="fas fa-concierge-bell text-4xl mb-4 text-gray-300"></i>
                <p class="text-gray-500">No services found</p>
            </div>
        `;
    } else {
        services.forEach(service => {
            const card = document.createElement('div');
            card.className = 'bg-white border rounded-lg p-6 hover:shadow-md transition-shadow';
            card.innerHTML = `
                <div class="flex items-start justify-between mb-4">
                    <h3 class="text-lg font-semibold text-gray-900">${service.aliasName || 'Service'}</h3>
                    <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${service.status === 1 ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}">
                        ${service.status === 1 ? 'Active' : 'Inactive'}
                    </span>
                </div>
                <p class="text-sm text-gray-600 mb-4">Store ID: ${service.storeId}</p>
                <div class="flex justify-between items-center">
                    <span class="text-sm text-gray-500">ID: ${service.id}</span>
                    <div class="flex space-x-2">
                        <button onclick="viewService(${service.id})" class="text-blue-600 hover:text-blue-900">
                            <i class="fas fa-eye"></i>
                        </button>
                        <button onclick="editService(${service.id})" class="text-green-600 hover:text-green-900">
                            <i class="fas fa-edit"></i>
                        </button>
                    </div>
                </div>
            `;
            grid.appendChild(card);
        });
    }
}

// Render services placeholder
function renderServicesPlaceholder() {
    const grid = document.getElementById('services-grid');
    if (!grid) return;
    
    grid.innerHTML = `
        <div class="col-span-full text-center py-8">
            <i class="fas fa-concierge-bell text-4xl mb-4 text-gray-300"></i>
            <p class="text-gray-500">Services feature coming soon</p>
            <p class="text-sm text-gray-400 mt-2">The services API endpoint is not yet implemented</p>
        </div>
    `;
}

// Load pricing
async function loadPricing() {
    try {
        showLoading('pricing-loading');
        // Try to load pricing (endpoint might not exist yet)
        const response = await fetch(`${API_BASE}/store-item-price/list`);
        
        if (response.ok) {
            const data = await response.json();
            pricingData = data.data || [];
            renderPricingTable(pricingData);
        } else {
            renderPricingPlaceholder();
        }
        
        hideLoading('pricing-loading');
        
    } catch (error) {
        console.error('Error loading pricing:', error);
        renderPricingPlaceholder();
        hideLoading('pricing-loading');
    }
}

// Render pricing table
function renderPricingTable(pricing) {
    const tbody = document.getElementById('pricing-tbody');
    const table = document.getElementById('pricing-table');
    
    if (!tbody || !table) return;
    
    tbody.innerHTML = '';
    
    if (pricing.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="6" class="px-6 py-8 text-center text-gray-500">
                    <i class="fas fa-tags text-4xl mb-4 text-gray-300"></i>
                    <p>No pricing items found</p>
                </td>
            </tr>
        `;
    } else {
        pricing.forEach(item => {
            const row = document.createElement('tr');
            row.className = 'hover:bg-gray-50';
            row.innerHTML = `
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${item.id}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${item.itemName || 'N/A'}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${item.price || 'N/A'}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${item.currency?.name || 'N/A'}</td>
                <td class="px-6 py-4 whitespace-nowrap">
                    <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${item.status === 1 ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}">
                        ${item.status === 1 ? 'Active' : 'Inactive'}
                    </span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                    <button onclick="editPricing(${item.id})" class="text-green-600 hover:text-green-900 mr-3">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button onclick="deletePricing(${item.id})" class="text-red-600 hover:text-red-900">
                        <i class="fas fa-trash"></i>
                    </button>
                </td>
            `;
            tbody.appendChild(row);
        });
    }
    
    table.classList.remove('hidden');
}

// Render pricing placeholder
function renderPricingPlaceholder() {
    const tbody = document.getElementById('pricing-tbody');
    const table = document.getElementById('pricing-table');
    
    if (!tbody || !table) return;
    
    tbody.innerHTML = `
        <tr>
            <td colspan="6" class="px-6 py-8 text-center text-gray-500">
                <i class="fas fa-tags text-4xl mb-4 text-gray-300"></i>
                <p>Pricing feature coming soon</p>
                <p class="text-sm text-gray-400 mt-2">The pricing API endpoint is not yet implemented</p>
            </td>
        </tr>
    `;
    table.classList.remove('hidden');
}

// Populate business select for store modal
function populateBusinessSelect() {
    const select = document.getElementById('business-select');
    if (!select) return;
    
    select.innerHTML = '<option value="">Select a business</option>';
    
    businessesData.forEach(business => {
        const option = document.createElement('option');
        option.value = business.id;
        option.textContent = business.businessName || `Business ${business.id}`;
        select.appendChild(option);
    });
}

// Modal functions
function showAddBusinessModal() {
    document.getElementById('add-business-modal').classList.remove('hidden');
}

function hideAddBusinessModal() {
    document.getElementById('add-business-modal').classList.add('hidden');
    document.getElementById('add-business-form').reset();
}

function showAddStoreModal() {
    populateBusinessSelect();
    document.getElementById('add-store-modal').classList.remove('hidden');
}

function hideAddStoreModal() {
    document.getElementById('add-store-modal').classList.add('hidden');
    document.getElementById('add-store-form').reset();
}

function hideModals() {
    hideAddBusinessModal();
    hideAddStoreModal();
}

// Form handlers
async function handleAddBusiness(e) {
    e.preventDefault();
    
    const formData = new FormData(e.target);
    const businessData = {
        businessName: formData.get('businessName'),
        description: formData.get('description'),
        status: parseInt(formData.get('status'))
    };
    
    try {
        const response = await fetch(`${API_BASE}/business/add`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(businessData)
        });
        
        if (!response.ok) {
            throw new Error('Failed to add business');
        }
        
        showNotification('Business added successfully', 'success');
        hideAddBusinessModal();
        loadBusinesses();
        loadDashboardData();
        
    } catch (error) {
        console.error('Error adding business:', error);
        showNotification('Error adding business', 'error');
    }
}

async function handleAddStore(e) {
    e.preventDefault();
    
    const formData = new FormData(e.target);
    const storeData = {
        storeName: formData.get('storeName'),
        description: formData.get('description'),
        status: parseInt(formData.get('status')),
        businessId: parseInt(formData.get('businessId'))
    };
    
    try {
        const response = await fetch(`${API_BASE}/stores/add`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(storeData)
        });
        
        if (!response.ok) {
            throw new Error('Failed to add store');
        }
        
        showNotification('Store added successfully', 'success');
        hideAddStoreModal();
        loadStores();
        loadDashboardData();
        
    } catch (error) {
        console.error('Error adding store:', error);
        showNotification('Error adding store', 'error');
    }
}

// Action handlers (placeholders)
function viewBusiness(id) {
    showNotification(`Viewing business ${id}`, 'info');
}

function editBusiness(id) {
    showNotification(`Editing business ${id}`, 'info');
}

function deleteBusiness(id) {
    if (confirm('Are you sure you want to delete this business?')) {
        showNotification(`Deleting business ${id}`, 'info');
    }
}

function viewStore(id) {
    showNotification(`Viewing store ${id}`, 'info');
}

function editStore(id) {
    showNotification(`Editing store ${id}`, 'info');
}

function deleteStore(id) {
    if (confirm('Are you sure you want to delete this store?')) {
        showNotification(`Deleting store ${id}`, 'info');
    }
}

function viewService(id) {
    showNotification(`Viewing service ${id}`, 'info');
}

function editService(id) {
    showNotification(`Editing service ${id}`, 'info');
}

function editPricing(id) {
    showNotification(`Editing pricing ${id}`, 'info');
}

function deletePricing(id) {
    if (confirm('Are you sure you want to delete this pricing item?')) {
        showNotification(`Deleting pricing ${id}`, 'info');
    }
}

// Search handler
function handleSearch(e) {
    const query = e.target.value.toLowerCase();
    // Implement search functionality based on current section
    console.log('Searching for:', query);
}

// Utility functions
function showLoading(elementId) {
    const element = document.getElementById(elementId);
    if (element) {
        element.classList.remove('hidden');
    }
}

function hideLoading(elementId) {
    const element = document.getElementById(elementId);
    if (element) {
        element.classList.add('hidden');
    }
}

function showNotification(message, type = 'info') {
    // Create a simple notification
    const notification = document.createElement('div');
    notification.className = `fixed top-4 right-4 z-50 px-6 py-3 rounded-lg text-white font-medium transition-all duration-300 transform translate-x-full`;
    
    switch(type) {
        case 'success':
            notification.classList.add('bg-green-500');
            break;
        case 'error':
            notification.classList.add('bg-red-500');
            break;
        case 'warning':
            notification.classList.add('bg-yellow-500');
            break;
        default:
            notification.classList.add('bg-blue-500');
    }
    
    notification.textContent = message;
    document.body.appendChild(notification);
    
    // Animate in
    setTimeout(() => {
        notification.classList.remove('translate-x-full');
    }, 100);
    
    // Auto remove
    setTimeout(() => {
        notification.classList.add('translate-x-full');
        setTimeout(() => {
            document.body.removeChild(notification);
        }, 300);
    }, 3000);
}