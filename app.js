document.addEventListener('DOMContentLoaded', () => {
    // Theme Toggle Logic
    const themeToggleBtn = document.getElementById('theme-toggle');
    const themeIcon = themeToggleBtn.querySelector('i');
    
    // Check local storage for theme preference
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme === 'dark') {
        document.body.setAttribute('data-theme', 'dark');
        themeIcon.classList.replace('fa-moon', 'fa-sun');
    }

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

    // Navigation Logic (SPA feel)
    const views = document.querySelectorAll('.view');
    const navCards = document.querySelectorAll('.nav-card');
    const backBtns = document.querySelectorAll('.back-btn');

    function navigateTo(targetId) {
        // Hide all views
        views.forEach(view => {
            view.classList.remove('active');
        });

        // Show target view
        const targetView = document.getElementById(targetId);
        if (targetView) {
            targetView.classList.add('active');
            window.scrollTo(0, 0); // Scroll to top on navigation
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

    // PDF Notes Logic
    const notesData = [
        { id: 1, title: 'Data Structures', description: 'Trees, Graphs, and Hash Tables', subject: 'cse', url: '#' },
        { id: 2, title: 'Operating Systems', description: 'Process management and memory', subject: 'cse', url: '#' },
        { id: 3, title: 'Analog Circuits', description: 'Diodes, Transistors, and Amplifiers', subject: 'ece', url: '#' },
        { id: 4, title: 'Digital Logic Design', description: 'Boolean algebra and logic gates', subject: 'ece', url: '#' },
        { id: 5, title: 'Database Systems', description: 'SQL, Normalization, and Transactions', subject: 'cse', url: '#' },
        { id: 6, title: 'Signals and Systems', description: 'Continuous and discrete time signals', subject: 'ece', url: '#' }
    ];

    const notesGrid = document.getElementById('notes-grid');
    const searchInput = document.getElementById('notes-search');
    const tabBtns = document.querySelectorAll('.tab-btn');
    
    let currentSubject = 'all';
    let searchQuery = '';

    const renderNotes = () => {
        if (!notesGrid) return;
        
        notesGrid.innerHTML = '';
        
        const filteredNotes = notesData.filter(note => {
            const matchesSubject = currentSubject === 'all' || note.subject === currentSubject;
            const matchesSearch = note.title.toLowerCase().includes(searchQuery.toLowerCase()) || 
                                  note.description.toLowerCase().includes(searchQuery.toLowerCase());
            return matchesSubject && matchesSearch;
        });

        if (filteredNotes.length === 0) {
            notesGrid.innerHTML = `
                <div class="empty-state text-muted" style="grid-column: 1 / -1; text-align: center; padding: 3rem 1rem;">
                    <i class="fa-solid fa-folder-open" style="font-size: 3rem; margin-bottom: 1rem; opacity: 0.5;"></i>
                    <p>No notes found matching your criteria.</p>
                </div>
            `;
            return;
        }

        filteredNotes.forEach(note => {
            const noteCard = document.createElement('div');
            noteCard.className = 'note-card';
            
            const subjectTagClass = note.subject === 'cse' ? 'tag-cse' : 'tag-ece';
            
            noteCard.innerHTML = `
                <div class="note-header">
                    <span class="note-subject-tag ${subjectTagClass}">${note.subject}</span>
                    <i class="fa-solid fa-file-pdf note-icon"></i>
                </div>
                <div class="note-content">
                    <h3>${note.title}</h3>
                    <p>${note.description}</p>
                </div>
                <div class="note-actions">
                    <button class="btn btn-outline" onclick="window.open('${note.url}', '_blank')">
                        <i class="fa-solid fa-eye"></i> View
                    </button>
                    <button class="btn btn-primary" onclick="window.open('${note.url}', '_blank')">
                        <i class="fa-solid fa-download"></i> Download
                    </button>
                </div>
            `;
            notesGrid.appendChild(noteCard);
        });
    };

    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            searchQuery = e.target.value;
            renderNotes();
        });
    }

    if (tabBtns) {
        tabBtns.forEach(btn => {
            btn.addEventListener('click', () => {
                tabBtns.forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                currentSubject = btn.getAttribute('data-subject');
                renderNotes();
            });
        });
    }

    // Initial render for notes
    if (document.getElementById('view-notes')) {
        renderNotes();
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
