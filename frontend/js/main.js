// Sidebar Toggle for Mobile
function toggleSidebar() {
    const sidebar = document.querySelector('.sidebar');
    sidebar.classList.toggle('active');
}

// Toast Notification System
function showToast(message, type = 'info') {
    const container = document.querySelector('.toast-container') || createToastContainer();

    const toast = document.createElement('div');
    toast.className = `toast-custom toast-${type}`;

    const icon = {
        success: 'fa-check-circle',
        error: 'fa-exclamation-circle',
        warning: 'fa-exclamation-triangle',
        info: 'fa-info-circle'
    }[type];

    toast.innerHTML = `
        <i class="fas ${icon}"></i>
        <span>${message}</span>
    `;

    container.appendChild(toast);

    setTimeout(() => {
        toast.style.animation = 'slideOut 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

function createToastContainer() {
    const container = document.createElement('div');
    container.className = 'toast-container';
    document.body.appendChild(container);
    return container;
}

// Confirmation Modal
function confirmAction(message, callback) {
    if (confirm(message)) {
        callback();
    }
}

// Photo Upload Preview
function setupPhotoUpload(inputId, previewId) {
    const input = document.getElementById(inputId);
    const preview = document.getElementById(previewId);
    const uploadArea = input?.closest('.photo-upload-area');

    if (!input) return;

    // Click to upload
    uploadArea?.addEventListener('click', () => input.click());

    // File selection
    input.addEventListener('change', (e) => {
        const file = e.target.files[0];
        if (file) {
            displayPhotoPreview(file, preview);
        }
    });

    // Drag and drop
    uploadArea?.addEventListener('dragover', (e) => {
        e.preventDefault();
        uploadArea.classList.add('dragover');
    });

    uploadArea?.addEventListener('dragleave', () => {
        uploadArea.classList.remove('dragover');
    });

    uploadArea?.addEventListener('drop', (e) => {
        e.preventDefault();
        uploadArea.classList.remove('dragover');
        const file = e.dataTransfer.files[0];
        if (file && file.type.startsWith('image/')) {
            input.files = e.dataTransfer.files;
            displayPhotoPreview(file, preview);
        }
    });
}

function displayPhotoPreview(file, previewElement) {
    const reader = new FileReader();
    reader.onload = (e) => {
        if (previewElement) {
            previewElement.src = e.target.result;
            previewElement.style.display = 'block';
        }
    };
    reader.readAsDataURL(file);
}

// Form Validation
function validateForm(formId) {
    const form = document.getElementById(formId);
    if (!form) return false;

    const inputs = form.querySelectorAll('input[required], select[required]');
    let isValid = true;

    inputs.forEach(input => {
        if (!input.value.trim()) {
            input.classList.add('is-invalid');
            isValid = false;
        } else {
            input.classList.remove('is-invalid');
        }
    });

    return isValid;
}

// Dynamic Role Fields
function handleRoleChange(roleSelect) {
    const selectedRole = roleSelect.value;
    const dynamicFields = document.getElementById('dynamic-fields');

    if (!dynamicFields) return;

    dynamicFields.innerHTML = '';

    if (selectedRole === 'Parent') {
        dynamicFields.innerHTML = `
            <div class="mb-3">
                <label class="form-label">Associer enfant</label>
                <select class="form-select">
                    <option value="">Sélectionner un apprenant</option>
                </select>
            </div>
        `;
    } else if (selectedRole === 'Educateur') {
        dynamicFields.innerHTML = `
            <div class="mb-3">
                <label class="form-label">Spécialité</label>
                <input type="text" class="form-control" placeholder="Ex: Orthophonie">
            </div>
        `;
    }
}

// Search and Filter
function setupTableFilter(searchInputId, tableId) {
    const searchInput = document.getElementById(searchInputId);
    const table = document.getElementById(tableId);

    if (!searchInput || !table) return;

    searchInput.addEventListener('input', (e) => {
        const searchTerm = e.target.value.toLowerCase();
        const rows = table.querySelectorAll('tbody tr');

        rows.forEach(row => {
            const text = row.textContent.toLowerCase();
            row.style.display = text.includes(searchTerm) ? '' : 'none';
        });
    });
}

async function handleDashPhotoUpload(input) {
    const file = input.files[0];
    if (!file) return;

    const preview = document.getElementById('dashPhotoPreview');
    const placeholder = document.getElementById('dashPhotoPlaceholder');

    // Show preview immediately
    const reader = new FileReader();
    reader.onload = (e) => {
        preview.src = e.target.result;
        preview.style.display = 'block';
        placeholder.style.display = 'none';
    };
    reader.readAsDataURL(file);

    // Upload to API
    try {
        const formData = new FormData();
        formData.append('file', file);
        const response = await fetch('http://localhost:8082/api/upload/photo', {
            method: 'POST',
            body: formData
        });

        if (response.ok) {
            showToast('Photo mise à jour !', 'success');
        } else {
            showToast('Erreur lors du téléchargement', 'error');
        }
    } catch (error) {
        showToast('Erreur serveur', 'error');
    }
}


// API Helper Functions (Ready for backend integration)
const API_BASE_URL = 'http://localhost:8082';

async function apiRequest(endpoint, method = 'GET', data = null) {
    const options = {
        method,
        headers: {
            'Content-Type': 'application/json',
        }
    };

    if (data) {
        options.body = JSON.stringify(data);
    }

    try {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, options);
        return await response.json();
    } catch (error) {
        console.error('API Error:', error);
        showToast('Une erreur est survenue', 'error');
        throw error;
    }
}

// Example API calls (uncomment when backend is ready)
/*
async function loadUsers() {
    const users = await apiRequest('/users');
    // Populate table with users
}

async function createUser(userData) {
    const result = await apiRequest('/users/save', 'POST', userData);
    showToast('Utilisateur créé avec succès', 'success');
    return result;
}

async function deleteUser(userId) {
    await apiRequest(`/users/delete/${userId}`, 'GET');
    showToast('Utilisateur supprimé', 'success');
}
*/
