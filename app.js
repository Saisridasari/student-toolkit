document.addEventListener('DOMContentLoaded', () => {
    // API Configuration
    const API_BASE_URL = 'http://localhost:8080/api';

    // State Variables
    let token = localStorage.getItem('token') || null;
    let currentUser = null;
    if (localStorage.getItem('currentUser')) {
        try {
            currentUser = JSON.parse(localStorage.getItem('currentUser'));
        } catch (e) {
            localStorage.removeItem('currentUser');
        }
    }

    // Theme Toggle Logic
    const themeToggleBtn = document.getElementById('theme-toggle');
    const themeIcon = themeToggleBtn ? themeToggleBtn.querySelector('i') : null;
    
    // Check local storage for theme preference
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme === 'dark' && themeIcon) {
        document.body.setAttribute('data-theme', 'dark');
        themeIcon.classList.replace('fa-moon', 'fa-sun');
    }

    if (themeToggleBtn && themeIcon) {
        themeToggleBtn.addEventListener('click', () => {
            const currentTheme = document.body.getAttribute('data-theme');
            if (currentTheme === 'dark') {
                document.body.removeAttribute('data-theme');
                localStorage.setItem('theme', 'light');
                themeIcon.classList.replace('fa-sun', 'fa-moon');
            } else {
                document.body.setAttribute('data-theme', 'dark');
                localStorage.setItem('theme', 'dark');
                themeIcon.classList.replace('fa-moon', 'fa-sun');
            }
        });
    }

    // Navigation Logic (SPA feel)
    const views = document.querySelectorAll('.view');
    const navCards = document.querySelectorAll('.nav-card');
    const backBtns = document.querySelectorAll('.back-btn');

    function navigateTo(targetId) {
        // If trying to access profile, and not logged in, redirect to login!
        if (targetId === 'view-profile' && !token) {
            targetId = 'view-login';
        }

        // Hide all views
        views.forEach(view => {
            view.classList.remove('active');
        });

        // Show target view
        const targetView = document.getElementById(targetId);
        if (targetView) {
            targetView.classList.add('active');
            window.scrollTo(0, 0); // Scroll to top on navigation

            // Fetch latest data if navigated to authenticated view
            if (targetId === 'view-profile' && token) {
                fetchProfile();
            }
            if (targetId === 'view-attendance' && token) {
                fetchAttendanceHistory();
            }
            if (targetId === 'view-notes') {
                updateNotesUI();
                fetchNotes();
            }
        }
    }

    // Add click listeners to navigation cards
    navCards.forEach(card => {
        card.addEventListener('click', () => {
            const targetId = card.getAttribute('data-target');
            navigateTo(targetId);
        });
    });

    // Add click listeners to back buttons
    backBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            const targetId = btn.getAttribute('data-target');
            navigateTo(targetId);
        });
    });

    // Toast Notification Logic
    const showToast = (message, type = 'success') => {
        let container = document.getElementById('toast-container');
        if (!container) {
            container = document.createElement('div');
            container.id = 'toast-container';
            container.className = 'toast-container';
            document.body.appendChild(container);
        }

        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        
        let iconClass = 'fa-circle-check';
        if (type === 'error') iconClass = 'fa-circle-exclamation';
        if (type === 'info') iconClass = 'fa-circle-info';

        toast.innerHTML = `<i class="fa-solid ${iconClass}"></i> <span>${message}</span>`;
        container.appendChild(toast);

        // Trigger animation
        setTimeout(() => toast.classList.add('show'), 10);

        // Remove after 3 seconds
        setTimeout(() => {
            toast.classList.remove('show');
            setTimeout(() => toast.remove(), 300);
        }, 3000);
    };

    // CGPA Calculator Logic
    const cgpaSubjectsContainer = document.getElementById('cgpa-subjects');
    const addSubjectBtn = document.getElementById('add-subject-btn');
    const calcCgpaBtn = document.getElementById('calc-cgpa-btn');
    const resetCgpaBtn = document.getElementById('reset-cgpa-btn');
    const cgpaResultCard = document.getElementById('cgpa-result-card');
    const cgpaResultValue = document.getElementById('cgpa-result-value');
    const cgpaResultMessage = document.getElementById('cgpa-result-message');

    // Load last CGPA on initialization
    const lastCgpa = localStorage.getItem('lastCgpa');
    if (lastCgpa && cgpaResultCard) {
        const lastCgpaBanner = document.createElement('div');
        lastCgpaBanner.className = 'last-cgpa-banner text-muted';
        lastCgpaBanner.innerHTML = `<i class="fa-solid fa-clock-rotate-left"></i> Last calculated CGPA: <strong>${lastCgpa}</strong>`;
        lastCgpaBanner.style.marginBottom = '1rem';
        lastCgpaBanner.style.fontSize = '0.875rem';
        
        const calcCard = document.querySelector('#view-cgpa .calc-card');
        if (calcCard) calcCard.parentNode.insertBefore(lastCgpaBanner, calcCard);
    }

    // Subject Row Template
    const createSubjectRow = () => {
        const row = document.createElement('div');
        row.className = 'subject-row';
        row.innerHTML = `
            <input type="text" placeholder="Subject (Optional)" class="input-field subject-name">
            <input type="number" placeholder="Credits" class="input-field subject-credits" min="1" max="10">
            <select class="input-field subject-grade">
                <option value="" disabled selected>Grade</option>
                <option value="10">O (10)</option>
                <option value="9">A+ (9)</option>
                <option value="8">A (8)</option>
                <option value="7">B+ (7)</option>
                <option value="6">B (6)</option>
                <option value="5">C (5)</option>
                <option value="0">F (0)</option>
            </select>
            <button class="btn-icon remove-subject text-danger" title="Remove"><i class="fa-solid fa-trash"></i></button>
        `;

        // Add event listener to remove button
        const removeBtn = row.querySelector('.remove-subject');
        removeBtn.addEventListener('click', () => {
            row.remove();
        });

        return row;
    };

    // Attach event listeners to existing remove buttons
    const attachRemoveListeners = () => {
        const removeBtns = cgpaSubjectsContainer.querySelectorAll('.remove-subject');
        removeBtns.forEach(btn => {
            btn.addEventListener('click', function() {
                this.closest('.subject-row').remove();
            });
        });
    };

    if (cgpaSubjectsContainer) attachRemoveListeners();

    // Add new subject
    if (addSubjectBtn) {
        addSubjectBtn.addEventListener('click', () => {
            cgpaSubjectsContainer.appendChild(createSubjectRow());
        });
    }

    // Reset Calculator
    if (resetCgpaBtn) {
        resetCgpaBtn.addEventListener('click', () => {
            cgpaSubjectsContainer.innerHTML = '';
            for (let i = 0; i < 3; i++) {
                cgpaSubjectsContainer.appendChild(createSubjectRow());
            }
            cgpaResultCard.style.display = 'none';
        });
    }

    // Calculate CGPA
    if (calcCgpaBtn) {
        calcCgpaBtn.addEventListener('click', () => {
            const rows = cgpaSubjectsContainer.querySelectorAll('.subject-row');
            let totalCredits = 0;
            let totalGradePoints = 0;
            let hasValidInputs = false;

            rows.forEach(row => {
                const creditsInput = row.querySelector('.subject-credits').value;
                const gradeInput = row.querySelector('.subject-grade').value;

                if (creditsInput && gradeInput) {
                    const credits = parseFloat(creditsInput);
                    const grade = parseFloat(gradeInput);

                    if (credits > 0) {
                        totalCredits += credits;
                        totalGradePoints += (credits * grade);
                        hasValidInputs = true;
                    }
                }
            });

            if (!hasValidInputs) {
                showToast('Please enter valid credits and grades for at least one subject.', 'error');
                return;
            }

            const cgpa = (totalGradePoints / totalCredits).toFixed(2);
            
            // Display Result
            cgpaResultValue.textContent = cgpa;
            cgpaResultCard.style.display = 'block';

            // Save to localStorage
            localStorage.setItem('lastCgpa', cgpa);
            
            // Show Toast
            showToast('CGPA Calculated Successfully!', 'success');

            // Add some personalized message based on CGPA
            if (cgpa >= 9.0) cgpaResultMessage.textContent = 'Excellent! Keep it up! 🌟';
            else if (cgpa >= 8.0) cgpaResultMessage.textContent = 'Great job! 👏';
            else if (cgpa >= 7.0) cgpaResultMessage.textContent = 'Good work! 👍';
            else cgpaResultMessage.textContent = 'Keep pushing, you can do better! 💪';
            
            // Smooth scroll to result
            cgpaResultCard.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
        });
    }

    // SGPA Calculator Logic
    const sgpaSubjectsContainer = document.getElementById('sgpa-subjects');
    const addSgpaSubjectBtn = document.getElementById('add-sgpa-subject-btn');
    const calcSgpaBtn = document.getElementById('calc-sgpa-btn');
    const resetSgpaBtn = document.getElementById('reset-sgpa-btn');
    const sgpaResultCard = document.getElementById('sgpa-result-card');
    const sgpaResultValue = document.getElementById('sgpa-result-value');
    const sgpaResultMessage = document.getElementById('sgpa-result-message');

    // Attach event listeners to existing remove buttons for SGPA
    const attachSgpaRemoveListeners = () => {
        const removeBtns = sgpaSubjectsContainer.querySelectorAll('.remove-subject');
        removeBtns.forEach(btn => {
            btn.addEventListener('click', function() {
                this.closest('.subject-row').remove();
            });
        });
    };

    if (sgpaSubjectsContainer) attachSgpaRemoveListeners();

    // Add new subject
    if (addSgpaSubjectBtn) {
        addSgpaSubjectBtn.addEventListener('click', () => {
            sgpaSubjectsContainer.appendChild(createSubjectRow());
        });
    }

    // Reset Calculator
    if (resetSgpaBtn) {
        resetSgpaBtn.addEventListener('click', () => {
            sgpaSubjectsContainer.innerHTML = '';
            for (let i = 0; i < 3; i++) {
                sgpaSubjectsContainer.appendChild(createSubjectRow());
            }
            sgpaResultCard.style.display = 'none';
        });
    }

    // Calculate SGPA
    if (calcSgpaBtn) {
        calcSgpaBtn.addEventListener('click', () => {
            const rows = sgpaSubjectsContainer.querySelectorAll('.subject-row');
            let totalCredits = 0;
            let totalGradePoints = 0;
            let hasValidInputs = false;

            rows.forEach(row => {
                const creditsInput = row.querySelector('.subject-credits').value;
                const gradeInput = row.querySelector('.subject-grade').value;

                if (creditsInput && gradeInput) {
                    const credits = parseFloat(creditsInput);
                    const grade = parseFloat(gradeInput);

                    if (credits > 0) {
                        totalCredits += credits;
                        totalGradePoints += (credits * grade);
                        hasValidInputs = true;
                    }
                }
            });

            if (!hasValidInputs) {
                showToast('Please enter valid credits and grades for at least one subject.', 'error');
                return;
            }

            const sgpa = (totalGradePoints / totalCredits).toFixed(2);
            
            // Display Result
            sgpaResultValue.textContent = sgpa;
            sgpaResultCard.style.display = 'block';

            // Show Toast
            showToast('SGPA Calculated Successfully!', 'success');

            // Add some personalized message based on SGPA
            if (sgpa >= 9.0) sgpaResultMessage.textContent = 'Excellent! Keep it up! 🌟';
            else if (sgpa >= 8.0) sgpaResultMessage.textContent = 'Great job! 👏';
            else if (sgpa >= 7.0) sgpaResultMessage.textContent = 'Good work! 👍';
            else sgpaResultMessage.textContent = 'Keep pushing, you can do better! 💪';
            
            // Smooth scroll to result
            sgpaResultCard.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
        });
    }

    // ==========================================
    // PDF NOTES LOGIC (API-based)
    // ==========================================

    const notesGrid = document.getElementById('notes-grid');
    const searchInput = document.getElementById('notes-search');
    const notesLoginMessage = document.getElementById('notes-login-message');
    const showUploadBtn = document.getElementById('show-upload-btn');
    const notesUploadCard = document.getElementById('notes-upload-card');
    const notesUploadForm = document.getElementById('notes-upload-form');
    const cancelUploadBtn = document.getElementById('cancel-upload-btn');
    const toggleUploadFormBtn = document.getElementById('toggle-upload-form-btn');
    const fileUploadArea = document.getElementById('file-upload-area');
    const noteFileInput = document.getElementById('note-file-input');
    const fileNameDisplay = document.getElementById('file-name-display');
    const notesToggle = document.getElementById('notes-toggle');
    const subjectTabsContainer = document.getElementById('notes-subject-tabs');

    let currentNotesView = 'my-notes'; // 'my-notes' or 'public-notes'
    let currentSubject = 'all';
    let searchQuery = '';
    let allNotes = []; // Store fetched notes
    let selectedFile = null;
    let currentPdfNoteId = null;
    let currentPdfBlobUrl = null;

    // Make navigateTo available globally for onclick handlers
    window.navigateTo = navigateTo;

    // Update notes UI based on auth status
    function updateNotesUI() {
        if (notesLoginMessage) {
            notesLoginMessage.style.display = token ? 'none' : 'block';
        }
        if (showUploadBtn) {
            showUploadBtn.style.display = token ? 'inline-flex' : 'none';
        }
        if (notesToggle) {
            notesToggle.style.display = token ? 'flex' : 'none';
        }
    }

    // Show/hide upload form
    if (showUploadBtn) {
        showUploadBtn.addEventListener('click', () => {
            if (notesUploadCard) {
                notesUploadCard.style.display = 'block';
                showUploadBtn.style.display = 'none';
            }
        });
    }

    if (cancelUploadBtn) {
        cancelUploadBtn.addEventListener('click', () => {
            if (notesUploadCard) {
                notesUploadCard.style.display = 'none';
                showUploadBtn.style.display = 'inline-flex';
            }
            resetUploadForm();
        });
    }

    if (toggleUploadFormBtn) {
        toggleUploadFormBtn.addEventListener('click', () => {
            const form = notesUploadForm;
            if (form) {
                const isHidden = form.style.display === 'none';
                form.style.display = isHidden ? 'block' : 'none';
                toggleUploadFormBtn.innerHTML = isHidden
                    ? '<i class="fa-solid fa-chevron-up"></i>'
                    : '<i class="fa-solid fa-chevron-down"></i>';
            }
        });
    }

    // File upload area click and drag
    if (fileUploadArea) {
        fileUploadArea.addEventListener('click', () => {
            if (noteFileInput) noteFileInput.click();
        });

        fileUploadArea.addEventListener('dragover', (e) => {
            e.preventDefault();
            fileUploadArea.classList.add('drag-over');
        });

        fileUploadArea.addEventListener('dragleave', () => {
            fileUploadArea.classList.remove('drag-over');
        });

        fileUploadArea.addEventListener('drop', (e) => {
            e.preventDefault();
            fileUploadArea.classList.remove('drag-over');
            if (e.dataTransfer.files.length > 0) {
                selectedFile = e.dataTransfer.files[0];
                if (fileNameDisplay) {
                    fileNameDisplay.textContent = selectedFile.name;
                    fileNameDisplay.style.display = 'block';
                }
                fileUploadArea.classList.add('has-file');
            }
        });
    }

    if (noteFileInput) {
        noteFileInput.addEventListener('change', (e) => {
            if (e.target.files.length > 0) {
                selectedFile = e.target.files[0];
                if (fileNameDisplay) {
                    fileNameDisplay.textContent = selectedFile.name;
                    fileNameDisplay.style.display = 'block';
                }
                if (fileUploadArea) fileUploadArea.classList.add('has-file');
            }
        });
    }

    function resetUploadForm() {
        if (notesUploadForm) notesUploadForm.reset();
        selectedFile = null;
        if (fileNameDisplay) {
            fileNameDisplay.textContent = '';
            fileNameDisplay.style.display = 'none';
        }
        if (fileUploadArea) fileUploadArea.classList.remove('has-file');
    }

    // Upload form submission
    if (notesUploadForm) {
        notesUploadForm.addEventListener('submit', async (e) => {
            e.preventDefault();

            if (!token) {
                showToast('Please login to upload notes.', 'error');
                return;
            }

            if (!selectedFile) {
                showToast('Please select a file to upload.', 'error');
                return;
            }

            const title = document.getElementById('note-title').value.trim();
            const subject = document.getElementById('note-subject').value.trim();
            const semester = document.getElementById('note-semester').value.trim();
            const visibility = document.getElementById('note-visibility').value;
            const description = document.getElementById('note-description').value.trim();

            if (!title || !subject || !semester) {
                showToast('Title, subject, and semester are required.', 'error');
                return;
            }

            const formData = new FormData();
            formData.append('file', selectedFile);
            formData.append('title', title);
            formData.append('subject', subject);
            formData.append('semester', semester);
            formData.append('visibility', visibility);
            if (description) formData.append('description', description);

            try {
                const response = await fetch(`${API_BASE_URL}/notes`, {
                    method: 'POST',
                    headers: {
                        'Authorization': `Bearer ${token}`
                    },
                    body: formData
                });
                const result = await response.json();

                if (result.success) {
                    showToast('PDF uploaded successfully!', 'success');
                    resetUploadForm();
                    if (notesUploadCard) notesUploadCard.style.display = 'none';
                    if (showUploadBtn) showUploadBtn.style.display = 'inline-flex';
                    fetchNotes();
                } else {
                    showToast(result.message || 'Upload failed.', 'error');
                }
            } catch (error) {
                console.error('Upload Error:', error);
                showToast('Unable to connect to the server. Please try again.', 'error');
            }
        });
    }

    // Notes toggle (My Notes / Public Notes)
    if (notesToggle) {
        const toggleBtns = notesToggle.querySelectorAll('.tab-btn');
        toggleBtns.forEach(btn => {
            btn.addEventListener('click', () => {
                toggleBtns.forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                currentNotesView = btn.getAttribute('data-notes-view');
                fetchNotes();
            });
        });
    }

    // Subject tabs
    if (subjectTabsContainer) {
        const tabBtns = subjectTabsContainer.querySelectorAll('.tab-btn');
        tabBtns.forEach(btn => {
            btn.addEventListener('click', () => {
                tabBtns.forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                currentSubject = btn.getAttribute('data-subject');
                renderNotes();
            });
        });
    }

    // Search
    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            searchQuery = e.target.value;
            renderNotes();
        });
    }

    // Fetch notes from API
    async function fetchNotes() {
        if (!notesGrid) return;

        if (!token) {
            // Not logged in - show public notes or empty state
            notesGrid.innerHTML = `
                <div class="empty-state text-muted" style="grid-column: 1 / -1; text-align: center; padding: 3rem 1rem;">
                    <i class="fa-solid fa-folder-open" style="font-size: 3rem; margin-bottom: 1rem; opacity: 0.5;"></i>
                    <p>Login to view and manage your PDF notes.</p>
                </div>
            `;
            return;
        }

        try {
            let url;
            if (currentNotesView === 'my-notes') {
                url = `${API_BASE_URL}/notes`;
            } else {
                url = currentSubject !== 'all'
                    ? `${API_BASE_URL}/notes/public?subject=${encodeURIComponent(currentSubject)}`
                    : `${API_BASE_URL}/notes/public`;
            }

            const response = await fetch(url, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            const result = await response.json();

            if (result.success && result.data) {
                allNotes = result.data;
                updateSubjectTabs();
                renderNotes();
            } else {
                allNotes = [];
                renderNotes();
            }
        } catch (error) {
            console.error('Fetch Notes Error:', error);
            allNotes = [];
            notesGrid.innerHTML = `
                <div class="empty-state text-muted" style="grid-column: 1 / -1; text-align: center; padding: 3rem 1rem;">
                    <i class="fa-solid fa-exclamation-circle" style="font-size: 3rem; margin-bottom: 1rem; opacity: 0.5;"></i>
                    <p>Error loading notes. Please try again.</p>
                </div>
            `;
        }
    }

    // Dynamically update subject tabs based on available subjects
    function updateSubjectTabs() {
        if (!subjectTabsContainer) return;

        const subjects = [...new Set(allNotes.map(n => n.subject))].sort();
        
        subjectTabsContainer.innerHTML = '';
        const allTab = document.createElement('button');
        allTab.className = `tab-btn ${currentSubject === 'all' ? 'active' : ''}`;
        allTab.setAttribute('data-subject', 'all');
        allTab.textContent = 'All';
        allTab.addEventListener('click', () => {
            subjectTabsContainer.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
            allTab.classList.add('active');
            currentSubject = 'all';
            renderNotes();
        });
        subjectTabsContainer.appendChild(allTab);

        subjects.forEach(subject => {
            const tab = document.createElement('button');
            tab.className = `tab-btn ${currentSubject === subject ? 'active' : ''}`;
            tab.setAttribute('data-subject', subject);
            tab.textContent = subject;
            tab.addEventListener('click', () => {
                subjectTabsContainer.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
                tab.classList.add('active');
                currentSubject = subject;
                renderNotes();
            });
            subjectTabsContainer.appendChild(tab);
        });
    }

    // Render notes grid
    const renderNotes = () => {
        if (!notesGrid) return;
        
        notesGrid.innerHTML = '';
        
        const filteredNotes = allNotes.filter(note => {
            const matchesSubject = currentSubject === 'all' || note.subject === currentSubject;
            const matchesSearch = note.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
                                  (note.description && note.description.toLowerCase().includes(searchQuery.toLowerCase()));
            return matchesSubject && matchesSearch;
        });

        if (filteredNotes.length === 0) {
            notesGrid.innerHTML = `
                <div class="empty-state text-muted" style="grid-column: 1 / -1; text-align: center; padding: 3rem 1rem;">
                    <i class="fa-solid fa-folder-open" style="font-size: 3rem; margin-bottom: 1rem; opacity: 0.5;"></i>
                    <p>${currentNotesView === 'my-notes' ? 'You haven\'t uploaded any notes yet. Click "Upload New PDF" to add one!' : 'No public notes found matching your criteria.'}</p>
                </div>
            `;
            return;
        }

        filteredNotes.forEach(note => {
            const noteCard = document.createElement('div');
            noteCard.className = 'note-card';
            
            const fileSizeFormatted = formatFileSize(note.fileSize);
            const fileTypeIcon = getFileTypeIcon(note.fileType);
            const isOwner = token && currentUser && note.userId === currentUser.userId;
            const visibilityTag = note.visibility === 'PUBLIC'
                ? '<span class="note-visibility-tag tag-public"><i class="fa-solid fa-globe"></i> Public</span>'
                : '<span class="note-visibility-tag tag-private"><i class="fa-solid fa-lock"></i> Private</span>';
            
            let formattedDate = '';
            if (note.createdAt) {
                try {
                    const date = new Date(note.createdAt);
                    formattedDate = date.toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' });
                } catch (e) { formattedDate = ''; }
            }

            noteCard.innerHTML = `
                <div class="note-header">
                    <div class="note-tags">
                        <span class="note-subject-tag">${note.subject}</span>
                        ${visibilityTag}
                    </div>
                    <i class="${fileTypeIcon} note-icon"></i>
                </div>
                <div class="note-content">
                    <h3>${note.title}</h3>
                    <p>${note.description || 'No description'}</p>
                    <div class="note-meta">
                        <span><i class="fa-solid fa-user"></i> ${note.userName || 'Unknown'}</span>
                        <span><i class="fa-solid fa-calendar"></i> ${formattedDate}</span>
                        <span><i class="fa-solid fa-file"></i> ${note.originalFileName} (${fileSizeFormatted})</span>
                    </div>
                </div>
                <div class="note-actions">
                    <button class="btn btn-outline btn-sm view-note-btn" data-id="${note.id}" data-title="${note.title}">
                        <i class="fa-solid fa-eye"></i> View
                    </button>
                    <button class="btn btn-primary btn-sm download-note-btn" data-id="${note.id}" data-filename="${note.originalFileName}">
                        <i class="fa-solid fa-download"></i> Download
                    </button>
                    ${isOwner ? `<button class="btn-icon delete-note-btn text-danger" data-id="${note.id}" title="Delete"><i class="fa-solid fa-trash"></i></button>` : ''}
                </div>
            `;
            notesGrid.appendChild(noteCard);
        });

        // Attach event listeners
        notesGrid.querySelectorAll('.view-note-btn').forEach(btn => {
            btn.addEventListener('click', () => openPdfViewer(btn.dataset.id, btn.dataset.title));
        });
        notesGrid.querySelectorAll('.download-note-btn').forEach(btn => {
            btn.addEventListener('click', () => downloadPdf(btn.dataset.id, btn.dataset.filename));
        });
        notesGrid.querySelectorAll('.delete-note-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                if (confirm('Are you sure you want to delete this note?')) {
                    deleteNote(btn.dataset.id);
                }
            });
        });
    };

    // Format file size
    function formatFileSize(bytes) {
        if (!bytes) return '0 B';
        const sizes = ['B', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(1024));
        return (bytes / Math.pow(1024, i)).toFixed(i > 0 ? 1 : 0) + ' ' + sizes[i];
    }

    // Get file type icon
    function getFileTypeIcon(fileType) {
        if (!fileType) return 'fa-solid fa-file note-icon';
        switch (fileType.toLowerCase()) {
            case 'pdf': return 'fa-solid fa-file-pdf note-icon';
            case 'doc': case 'docx': return 'fa-solid fa-file-word note-icon-blue';
            case 'ppt': case 'pptx': return 'fa-solid fa-file-powerpoint note-icon-orange';
            case 'xls': case 'xlsx': return 'fa-solid fa-file-excel note-icon-green';
            case 'jpg': case 'jpeg': case 'png': case 'gif': return 'fa-solid fa-file-image note-icon-purple';
            case 'txt': return 'fa-solid fa-file-lines note-icon';
            default: return 'fa-solid fa-file note-icon';
        }
    }

    // Open PDF viewer modal
    async function openPdfViewer(noteId, title) {
        if (!token) {
            showToast('Please login to view notes.', 'error');
            return;
        }

        currentPdfNoteId = noteId;
        const modal = document.getElementById('pdf-viewer-modal');
        const iframe = document.getElementById('pdf-viewer-iframe');
        const titleEl = document.getElementById('pdf-viewer-title');

        if (modal && iframe) {
            if (titleEl) titleEl.textContent = title || 'PDF Viewer';
            modal.style.display = 'flex';
            iframe.src = ''; // Clear previous content

            // Fetch PDF with JWT auth and create blob URL for iframe viewing
            // This is necessary because iframe can't send Authorization headers
            try {
                const response = await fetch(`${API_BASE_URL}/notes/${noteId}/view`, {
                    headers: { 'Authorization': `Bearer ${token}` }
                });
                if (!response.ok) throw new Error('Failed to load PDF');
                const blob = await response.blob();
                // Revoke previous blob URL if exists
                if (currentPdfBlobUrl) {
                    URL.revokeObjectURL(currentPdfBlobUrl);
                }
                currentPdfBlobUrl = URL.createObjectURL(blob);
                iframe.src = currentPdfBlobUrl;
            } catch (error) {
                console.error('PDF View Error:', error);
                showToast('Failed to load PDF. Please try again.', 'error');
                modal.style.display = 'none';
            }
        }
    }

    // Download PDF
    function downloadPdf(noteId, filename) {
        if (!token) {
            showToast('Please login to download notes.', 'error');
            return;
        }

        // Create a temporary link to trigger download
        const link = document.createElement('a');
        link.href = `${API_BASE_URL}/notes/${noteId}/download`;
        link.download = filename || 'note.pdf';
        
        // For authenticated downloads, we need to fetch with token then create blob
        fetch(`${API_BASE_URL}/notes/${noteId}/download`, {
            headers: { 'Authorization': `Bearer ${token}` }
        })
        .then(response => {
            if (!response.ok) throw new Error('Download failed');
            return response.blob();
        })
        .then(blob => {
            const url = window.URL.createObjectURL(blob);
            link.href = url;
            link.click();
            window.URL.revokeObjectURL(url);
            showToast('Download started!', 'success');
        })
        .catch(error => {
            console.error('Download Error:', error);
            showToast('Download failed. Please try again.', 'error');
        });
    }

    // Delete note
    async function deleteNote(noteId) {
        if (!token) {
            showToast('Please login to delete notes.', 'error');
            return;
        }

        try {
            const response = await fetch(`${API_BASE_URL}/notes/${noteId}`, {
                method: 'DELETE',
                headers: { 'Authorization': `Bearer ${token}` }
            });
            const result = await response.json();

            if (result.success) {
                showToast('Note deleted successfully.', 'success');
                fetchNotes();
            } else {
                showToast(result.message || 'Failed to delete note.', 'error');
            }
        } catch (error) {
            console.error('Delete Note Error:', error);
            showToast('Unable to connect to the server. Please try again.', 'error');
        }
    }

    // PDF Viewer Modal close
    const pdfCloseBtn = document.getElementById('pdf-close-btn');
    const pdfDownloadBtn = document.getElementById('pdf-download-btn');
    const pdfViewerModal = document.getElementById('pdf-viewer-modal');

    if (pdfCloseBtn) {
        pdfCloseBtn.addEventListener('click', () => {
            if (pdfViewerModal) {
                pdfViewerModal.style.display = 'none';
                const iframe = document.getElementById('pdf-viewer-iframe');
                if (iframe) iframe.src = '';
                // Revoke blob URL to free memory
                if (currentPdfBlobUrl) {
                    URL.revokeObjectURL(currentPdfBlobUrl);
                    currentPdfBlobUrl = null;
                }
            }
        });
    }

    if (pdfDownloadBtn) {
        pdfDownloadBtn.addEventListener('click', () => {
            if (currentPdfNoteId) {
                downloadPdf(currentPdfNoteId, '');
            }
        });
    }

    // Close modal on clicking outside
    if (pdfViewerModal) {
        pdfViewerModal.addEventListener('click', (e) => {
            if (e.target === pdfViewerModal) {
                pdfViewerModal.style.display = 'none';
                const iframe = document.getElementById('pdf-viewer-iframe');
                if (iframe) iframe.src = '';
                // Revoke blob URL to free memory
                if (currentPdfBlobUrl) {
                    URL.revokeObjectURL(currentPdfBlobUrl);
                    currentPdfBlobUrl = null;
                }
            }
        });
    }

    // Initialize notes UI
    updateNotesUI();
    if (document.getElementById('view-notes')) {
        fetchNotes();
    }

    // Tools Logic
    const calcCgpaPctBtn = document.getElementById('calc-cgpa-pct-btn');
    if (calcCgpaPctBtn) {
        calcCgpaPctBtn.addEventListener('click', () => {
            const cgpa = parseFloat(document.getElementById('cgpa-val').value);
            const multiplier = parseFloat(document.getElementById('cgpa-multiplier').value);
            const resultEl = document.getElementById('cgpa-pct-result');
            
            if (!isNaN(cgpa) && cgpa >= 0 && cgpa <= 10) {
                const result = cgpa * multiplier;
                resultEl.textContent = `= ${result.toFixed(2)}%`;
            } else {
                showToast('Please enter a valid CGPA between 0 and 10.', 'error');
                resultEl.textContent = '= ?';
            }
        });
    }

    const calcUnitBtn = document.getElementById('calc-unit-btn');
    if (calcUnitBtn) {
        calcUnitBtn.addEventListener('click', () => {
            const val = parseFloat(document.getElementById('unit-val').value);
            const fromUnit = document.getElementById('unit-from').value;
            const toUnit = document.getElementById('unit-to').value;
            const resultEl = document.getElementById('unit-result');
            
            if (isNaN(val)) {
                resultEl.textContent = '= ?';
                return;
            }

            // Convert to base unit (meters) first
            let inMeters = val;
            if (fromUnit === 'cm') inMeters = val / 100;
            if (fromUnit === 'in') inMeters = val * 0.0254;
            if (fromUnit === 'ft') inMeters = val * 0.3048;

            // Convert from base unit to target unit
            let result = inMeters;
            if (toUnit === 'cm') result = inMeters * 100;
            if (toUnit === 'in') result = inMeters / 0.0254;
            if (toUnit === 'ft') result = inMeters / 0.3048;

            // Format appropriately
            const formattedResult = Number.isInteger(result) ? result : result.toFixed(3);
            resultEl.textContent = `= ${formattedResult}`;
        });
    }

    // ==========================================
    // AUTHENTICATION LOGIC
    // ==========================================

    // User header button navigation routing
    const userBtn = document.getElementById('user-btn');
    if (userBtn) {
        userBtn.addEventListener('click', () => {
            if (token) {
                navigateTo('view-profile');
            } else {
                navigateTo('view-login');
            }
        });
    }

    // Auth redirection links
    const loginToRegister = document.getElementById('login-to-register');
    if (loginToRegister) {
        loginToRegister.addEventListener('click', (e) => {
            e.preventDefault();
            navigateTo('view-register');
        });
    }

    const registerToLogin = document.getElementById('register-to-login');
    if (registerToLogin) {
        registerToLogin.addEventListener('click', (e) => {
            e.preventDefault();
            navigateTo('view-login');
        });
    }

    const profileLoginBtn = document.getElementById('profile-login-btn');
    if (profileLoginBtn) {
        profileLoginBtn.addEventListener('click', () => {
            navigateTo('view-login');
        });
    }

    const profileRegisterBtn = document.getElementById('profile-register-btn');
    if (profileRegisterBtn) {
        profileRegisterBtn.addEventListener('click', () => {
            navigateTo('view-register');
        });
    }

    // Update visibility of elements based on auth status
    function updateAuthUI() {
        const notLoggedInCard = document.getElementById('profile-not-logged-in');
        const loggedInCard = document.getElementById('profile-logged-in');
        const attendanceSaveActions = document.getElementById('attendance-save-actions');
        const attendanceHistoryCard = document.getElementById('attendance-history-card');

        if (token && currentUser) {
            if (notLoggedInCard) notLoggedInCard.style.display = 'none';
            if (loggedInCard) loggedInCard.style.display = 'block';
            if (attendanceSaveActions) attendanceSaveActions.style.display = 'block';
            if (attendanceHistoryCard) attendanceHistoryCard.style.display = 'block';

            const displayName = document.getElementById('profile-display-name');
            const displayEmail = document.getElementById('profile-display-email');
            const displayRole = document.getElementById('profile-display-role');

            if (displayName) displayName.textContent = currentUser.fullName || currentUser.email;
            if (displayEmail) displayEmail.textContent = currentUser.email;
            if (displayRole) displayRole.textContent = currentUser.role || 'STUDENT';
        } else {
            if (notLoggedInCard) notLoggedInCard.style.display = 'block';
            if (loggedInCard) loggedInCard.style.display = 'none';
            if (attendanceSaveActions) attendanceSaveActions.style.display = 'none';
            if (attendanceHistoryCard) attendanceHistoryCard.style.display = 'none';
        }

        // Update notes UI based on auth status
        updateNotesUI();
    }

    // Register submission
    const registerForm = document.getElementById('register-form');
    if (registerForm) {
        registerForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const fullName = document.getElementById('register-name').value.trim();
            const email = document.getElementById('register-email').value.trim();
            const password = document.getElementById('register-password').value;
            const phone = document.getElementById('register-phone').value.trim();
            const department = document.getElementById('register-department').value.trim();
            const college = document.getElementById('register-college').value.trim();
            const semester = document.getElementById('register-semester').value.trim();

            try {
                const response = await fetch(`${API_BASE_URL}/auth/register`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ fullName, email, password, phone, department, college, semester })
                });
                const result = await response.json();

                if (result.success) {
                    showToast(result.message || 'Registration successful!', 'success');
                    token = result.data.token;
                    currentUser = {
                        userId: result.data.userId,
                        email: result.data.email,
                        fullName: result.data.fullName,
                        role: result.data.role
                    };
                    localStorage.setItem('token', token);
                    localStorage.setItem('currentUser', JSON.stringify(currentUser));
                    
                    registerForm.reset();
                    updateAuthUI();
                    navigateTo('view-profile');
                } else {
                    showToast(result.message || 'Registration failed.', 'error');
                }
            } catch (error) {
                console.error('Registration Error:', error);
                showToast('Unable to connect to the server. Please try again.', 'error');
            }
        });
    }

    // Login submission
    const loginForm = document.getElementById('login-form');
    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const email = document.getElementById('login-email').value.trim();
            const password = document.getElementById('login-password').value;

            try {
                const response = await fetch(`${API_BASE_URL}/auth/login`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email, password })
                });
                const result = await response.json();

                if (result.success) {
                    showToast(result.message || 'Login successful!', 'success');
                    token = result.data.token;
                    currentUser = {
                        userId: result.data.userId,
                        email: result.data.email,
                        fullName: result.data.fullName,
                        role: result.data.role
                    };
                    localStorage.setItem('token', token);
                    localStorage.setItem('currentUser', JSON.stringify(currentUser));

                    loginForm.reset();
                    updateAuthUI();
                    navigateTo('view-profile');
                } else {
                    showToast(result.message || 'Login failed. Please check your credentials.', 'error');
                }
            } catch (error) {
                console.error('Login Error:', error);
                showToast('Unable to connect to the server. Please try again.', 'error');
            }
        });
    }

    // Logout logic
    const logoutBtn = document.getElementById('profile-logout-btn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', () => {
            token = null;
            currentUser = null;
            localStorage.removeItem('token');
            localStorage.removeItem('currentUser');
            showToast('Logged out successfully.', 'info');
            updateAuthUI();
            navigateTo('view-home');
        });
    }

    // ==========================================
    // PROFILE LOGIC
    // ==========================================

    async function fetchProfile() {
        if (!token) return;
        try {
            const response = await fetch(`${API_BASE_URL}/profile`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            const result = await response.json();
            if (result.success && result.data) {
                const profile = result.data;
                document.getElementById('profile-display-name').textContent = profile.fullName || '—';
                document.getElementById('profile-display-email').textContent = profile.email || '—';
                document.getElementById('profile-display-role').textContent = profile.role || 'STUDENT';
                document.getElementById('profile-display-phone').textContent = profile.phone || '—';
                document.getElementById('profile-display-department').textContent = profile.department || '—';
                document.getElementById('profile-display-college').textContent = profile.college || '—';
                document.getElementById('profile-display-semester').textContent = profile.semester || '—';

                // Sync local state
                currentUser.fullName = profile.fullName;
                localStorage.setItem('currentUser', JSON.stringify(currentUser));
            } else {
                showToast(result.message || 'Failed to fetch profile details.', 'error');
            }
        } catch (error) {
            console.error('Fetch Profile Error:', error);
            showToast('Error loading profile details.', 'error');
        }
    }

    const profileEditBtn = document.getElementById('profile-edit-btn');
    const profileCancelEditBtn = document.getElementById('profile-cancel-edit-btn');
    const profileSaveEditBtn = document.getElementById('profile-save-edit-btn');

    const detailsView = document.getElementById('profile-details-view');
    const editForm = document.getElementById('profile-edit-form');
    const actionsView = document.getElementById('profile-actions-view');

    if (profileEditBtn) {
        profileEditBtn.addEventListener('click', () => {
            document.getElementById('profile-edit-name').value = document.getElementById('profile-display-name').textContent.replace('—', '').trim();
            document.getElementById('profile-edit-phone').value = document.getElementById('profile-display-phone').textContent.replace('—', '').trim();
            document.getElementById('profile-edit-department').value = document.getElementById('profile-display-department').textContent.replace('—', '').trim();
            document.getElementById('profile-edit-college').value = document.getElementById('profile-display-college').textContent.replace('—', '').trim();
            document.getElementById('profile-edit-semester').value = document.getElementById('profile-display-semester').textContent.replace('—', '').trim();

            if (detailsView) detailsView.style.display = 'none';
            if (actionsView) actionsView.style.display = 'none';
            if (editForm) editForm.style.display = 'block';
        });
    }

    if (profileCancelEditBtn) {
        profileCancelEditBtn.addEventListener('click', () => {
            if (editForm) editForm.style.display = 'none';
            if (detailsView) detailsView.style.display = 'block';
            if (actionsView) actionsView.style.display = 'flex';
        });
    }

    if (profileSaveEditBtn) {
        profileSaveEditBtn.addEventListener('click', async () => {
            const fullName = document.getElementById('profile-edit-name').value.trim();
            const phone = document.getElementById('profile-edit-phone').value.trim();
            const department = document.getElementById('profile-edit-department').value.trim();
            const college = document.getElementById('profile-edit-college').value.trim();
            const semester = document.getElementById('profile-edit-semester').value.trim();

            if (!fullName) {
                showToast('Name is required.', 'error');
                return;
            }

            try {
                const response = await fetch(`${API_BASE_URL}/profile`, {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${token}`
                    },
                    body: JSON.stringify({ fullName, phone, department, college, semester })
                });
                const result = await response.json();
                if (result.success) {
                    showToast('Profile updated successfully!', 'success');
                    if (editForm) editForm.style.display = 'none';
                    if (detailsView) detailsView.style.display = 'block';
                    if (actionsView) actionsView.style.display = 'flex';
                    fetchProfile();
                } else {
                    showToast(result.message || 'Profile update failed.', 'error');
                }
            } catch (error) {
                console.error('Update Profile Error:', error);
                showToast('Unable to connect to the server. Please try again.', 'error');
            }
        });
    }

    // ==========================================
    // ATTENDANCE LOGIC
    // ==========================================

    const attendanceSubjectsContainer = document.getElementById('attendance-subjects');
    const addAttendanceSubjectBtn = document.getElementById('add-attendance-subject-btn');
    const calcAttendanceBtn = document.getElementById('calc-attendance-btn');
    const resetAttendanceBtn = document.getElementById('reset-attendance-btn');
    const attendanceResultCard = document.getElementById('attendance-result-card');
    const attendanceResultValue = document.getElementById('attendance-result-value');
    const attendanceResultMessage = document.getElementById('attendance-result-message');
    const attendanceBreakdown = document.getElementById('attendance-breakdown');
    const saveAttendanceBtn = document.getElementById('save-attendance-btn');
    const semesterNameInput = document.getElementById('attendance-semester-name');

    let lastAttendanceCalculation = null;

    // Attendance Row Template
    const createAttendanceRow = () => {
        const row = document.createElement('div');
        row.className = 'attendance-row';
        row.innerHTML = `
            <input type="text" placeholder="Subject Name" class="input-field attendance-name">
            <input type="number" placeholder="Total Classes" class="input-field attendance-total" min="0">
            <input type="number" placeholder="Attended" class="input-field attendance-attended" min="0">
            <button class="btn-icon remove-attendance text-danger" title="Remove"><i class="fa-solid fa-trash"></i></button>
        `;

        const removeBtn = row.querySelector('.remove-attendance');
        removeBtn.addEventListener('click', () => {
            row.remove();
        });

        return row;
    };

    const attachAttendanceRemoveListeners = () => {
        if (!attendanceSubjectsContainer) return;
        const removeBtns = attendanceSubjectsContainer.querySelectorAll('.remove-attendance');
        removeBtns.forEach(btn => {
            btn.addEventListener('click', function() {
                this.closest('.attendance-row').remove();
            });
        });
    };

    if (attendanceSubjectsContainer) attachAttendanceRemoveListeners();

    if (addAttendanceSubjectBtn) {
        addAttendanceSubjectBtn.addEventListener('click', () => {
            attendanceSubjectsContainer.appendChild(createAttendanceRow());
        });
    }

    if (resetAttendanceBtn) {
        resetAttendanceBtn.addEventListener('click', () => {
            attendanceSubjectsContainer.innerHTML = '';
            for (let i = 0; i < 3; i++) {
                attendanceSubjectsContainer.appendChild(createAttendanceRow());
            }
            if (attendanceResultCard) attendanceResultCard.style.display = 'none';
        });
    }

    if (calcAttendanceBtn) {
        calcAttendanceBtn.addEventListener('click', () => {
            const rows = attendanceSubjectsContainer.querySelectorAll('.attendance-row');
            let overallTotal = 0;
            let overallAttended = 0;
            let hasValidInputs = false;
            let subjectsDetailsObj = {};
            let breakdownHtml = '';

            for (let i = 0; i < rows.length; i++) {
                const row = rows[i];
                const nameInput = row.querySelector('.attendance-name').value.trim();
                const totalInput = row.querySelector('.attendance-total').value;
                const attendedInput = row.querySelector('.attendance-attended').value;

                if (totalInput && attendedInput) {
                    const total = parseInt(totalInput);
                    const attended = parseInt(attendedInput);
                    const subjectName = nameInput || `Subject ${i + 1}`;

                    if (isNaN(total) || isNaN(attended) || total < 0 || attended < 0) {
                        showToast('Total classes and attended classes must be positive numbers.', 'error');
                        return;
                    }

                    if (attended > total) {
                        showToast(`Attended classes cannot exceed total classes for "${subjectName}".`, 'error');
                        return;
                    }

                    overallTotal += total;
                    overallAttended += attended;
                    hasValidInputs = true;

                    subjectsDetailsObj[subjectName] = { total, attended };

                    const pct = total > 0 ? (attended / total) * 100 : 100;
                    const pctFormatted = pct.toFixed(1);

                    let suggestionText = '';
                    if (pct < 75) {
                        const required = Math.ceil((0.75 * total - attended) / 0.25);
                        suggestionText = `<i class="fa-solid fa-triangle-exclamation"></i> Must attend <strong>${required}</strong> more class${required > 1 ? 'es' : ''} consecutively.`;
                    } else {
                        const missable = Math.floor((attended - 0.75 * total) / 0.75);
                        suggestionText = missable > 0 
                            ? `<i class="fa-solid fa-circle-check"></i> Can afford to miss <strong>${missable}</strong> class${missable > 1 ? 'es' : ''}.`
                            : `<i class="fa-solid fa-circle-info"></i> On the line! Do not miss any classes.`;
                    }

                    breakdownHtml += `
                        <div class="attendance-breakdown-item">
                            <div>
                                <div class="subject-name-label">${subjectName}</div>
                                <div class="subject-detail-label">${attended}/${total} classes • ${suggestionText}</div>
                            </div>
                            <div class="subject-pct-label ${pct < 75 ? 'low' : ''}">${pctFormatted}%</div>
                        </div>
                    `;
                }
            }

            if (!hasValidInputs) {
                showToast('Please enter total and attended classes for at least one subject.', 'error');
                return;
            }

            const overallPct = overallTotal > 0 ? (overallAttended / overallTotal) * 100 : 0;
            const overallPctFormatted = overallPct.toFixed(2);

            lastAttendanceCalculation = {
                overallAttendancePercentage: parseFloat(overallPctFormatted),
                totalClasses: overallTotal,
                totalAttended: overallAttended,
                subjectDetails: JSON.stringify(subjectsDetailsObj)
            };

            attendanceResultValue.textContent = `${overallPctFormatted}%`;
            attendanceResultCard.style.display = 'block';

            if (overallPct >= 75) {
                attendanceResultMessage.innerHTML = `<span class="text-success"><i class="fa-solid fa-circle-check"></i> Great job! Your attendance is above the 75% threshold.</span>`;
            } else {
                const overallRequired = Math.ceil((0.75 * overallTotal - overallAttended) / 0.25);
                attendanceResultMessage.innerHTML = `<span class="text-danger"><i class="fa-solid fa-triangle-exclamation"></i> Low attendance! You need to attend <strong>${overallRequired}</strong> more classes consecutively to hit 75%.</span>`;
            }

            attendanceBreakdown.innerHTML = breakdownHtml;

            // Smooth scroll to result
            attendanceResultCard.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
        });
    }

    if (saveAttendanceBtn) {
        saveAttendanceBtn.addEventListener('click', async () => {
            if (!token || !lastAttendanceCalculation) {
                showToast('Please perform a calculation first and log in.', 'error');
                return;
            }

            const semesterName = semesterNameInput.value.trim();
            if (!semesterName) {
                showToast('Please enter a semester name.', 'error');
                return;
            }

            const payload = {
                semesterName: semesterName,
                overallAttendancePercentage: lastAttendanceCalculation.overallAttendancePercentage,
                totalClasses: lastAttendanceCalculation.totalClasses,
                totalAttended: lastAttendanceCalculation.totalAttended,
                subjectDetails: lastAttendanceCalculation.subjectDetails
            };

            try {
                const response = await fetch(`${API_BASE_URL}/attendance`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${token}`
                    },
                    body: JSON.stringify(payload)
                });
                const result = await response.json();

                if (result.success) {
                    showToast('Attendance history saved successfully!', 'success');
                    semesterNameInput.value = '';
                    fetchAttendanceHistory();
                } else {
                    showToast(result.message || 'Failed to save attendance history.', 'error');
                }
            } catch (error) {
                console.error('Save Attendance History Error:', error);
                showToast('Unable to connect to the server. Please try again.', 'error');
            }
        });
    }

    async function fetchAttendanceHistory() {
        if (!token) return;

        const historyListContainer = document.getElementById('attendance-history-list');
        if (!historyListContainer) return;

        try {
            const response = await fetch(`${API_BASE_URL}/attendance`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            const result = await response.json();

            if (result.success && result.data) {
                const historyData = result.data;
                
                if (historyData.length === 0) {
                    historyListContainer.innerHTML = `<p class="text-muted" style="text-align: center; padding: 1.5rem;">No saved attendance records found.</p>`;
                    return;
                }

                historyListContainer.innerHTML = '';
                historyData.forEach(item => {
                    let formattedDate = '';
                    if (item.createdAt) {
                        try {
                            const date = new Date(item.createdAt);
                            formattedDate = date.toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' });
                        } catch (e) {
                            formattedDate = item.createdAt;
                        }
                    }

                    const isLow = item.overallAttendancePercentage < 75;
                    const pctFormatted = item.overallAttendancePercentage.toFixed(1);

                    const historyItem = document.createElement('div');
                    historyItem.className = 'attendance-history-item';
                    historyItem.innerHTML = `
                        <div class="attendance-history-info">
                            <span class="history-semester">${item.semesterName}</span>
                            <span class="history-date"><i class="fa-solid fa-calendar-day"></i> ${formattedDate} • ${item.totalAttended}/${item.totalClasses} classes</span>
                        </div>
                        <div style="display: flex; align-items: center; gap: 1.5rem;">
                            <span class="attendance-history-pct ${isLow ? 'low' : ''}">${pctFormatted}%</span>
                            <div class="attendance-history-actions">
                                <button class="btn-icon delete-history-btn text-danger" title="Delete" data-id="${item.id}">
                                    <i class="fa-solid fa-trash"></i>
                                </button>
                            </div>
                        </div>
                    `;

                    const deleteBtn = historyItem.querySelector('.delete-history-btn');
                    deleteBtn.addEventListener('click', async (e) => {
                        e.stopPropagation();
                        if (confirm(`Are you sure you want to delete the attendance history for "${item.semesterName}"?`)) {
                            deleteAttendanceHistory(item.id);
                        }
                    });

                    historyListContainer.appendChild(historyItem);
                });
            } else {
                historyListContainer.innerHTML = `<p class="text-muted" style="text-align: center; padding: 1.5rem;">Failed to load attendance history.</p>`;
            }
        } catch (error) {
            console.error('Fetch Attendance History Error:', error);
            historyListContainer.innerHTML = `<p class="text-muted" style="text-align: center; padding: 1.5rem;">Error connecting to the server.</p>`;
        }
    }

    async function deleteAttendanceHistory(id) {
        try {
            const response = await fetch(`${API_BASE_URL}/attendance/${id}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            const result = await response.json();

            if (result.success) {
                showToast('Attendance record deleted successfully.', 'success');
                fetchAttendanceHistory();
            } else {
                showToast(result.message || 'Failed to delete attendance record.', 'error');
            }
        } catch (error) {
            console.error('Delete Attendance Error:', error);
            showToast('Unable to connect to the server. Please try again.', 'error');
        }
    }

    // Call UI updates on initialization
    updateAuthUI();
    if (token) {
        fetchProfile();
        fetchAttendanceHistory();
    }

    // Interstitial Ad Logic
    const interstitialAd = document.getElementById('interstitial-ad');
    const closeInterstitialBtn = document.getElementById('close-interstitial');
    
    if (closeInterstitialBtn && interstitialAd) {
        closeInterstitialBtn.addEventListener('click', () => {
            interstitialAd.classList.remove('active');
            // Wait for transition before hiding completely
            setTimeout(() => {
                interstitialAd.style.display = 'none';
            }, 300);
        });
        
        // Expose function globally for testing/future integration
        window.showInterstitialAd = () => {
            interstitialAd.style.display = 'flex';
            // Small delay to allow display block to apply before adding active class for opacity transition
            setTimeout(() => {
                interstitialAd.classList.add('active');
            }, 10);
        };
    }
});
