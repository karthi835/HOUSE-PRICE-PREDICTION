/**
 * PricePredictor.AI — Client-Side JavaScript
 * 
 * Features:
 * - Mobile navigation toggle
 * - Client-side form validation (mirrors server-side)
 * - Location rating slider live update
 * - Price count-up animation on result page
 * - Animated stat counters on home page
 * - Chart.js initialization for dashboard
 * - Form submit loading state
 * - Smooth scroll behavior
 */

// ======================== DOM READY ========================

document.addEventListener('DOMContentLoaded', () => {
    initNavToggle();
    initRangeSlider();
    initFormValidation();
    initFormSubmit();
    initStatCounters();
    initPriceAnimation();
    initDashboardCharts();
});

// ======================== MOBILE NAVIGATION ========================

/**
 * Toggles the mobile hamburger menu open/close.
 */
function initNavToggle() {
    const toggle = document.getElementById('nav-toggle');
    const navLinks = document.getElementById('nav-links');

    if (!toggle || !navLinks) return;

    toggle.addEventListener('click', () => {
        navLinks.classList.toggle('active');
        toggle.classList.toggle('active');
    });

    // Close menu when clicking a link
    navLinks.querySelectorAll('.nav-link').forEach(link => {
        link.addEventListener('click', () => {
            navLinks.classList.remove('active');
            toggle.classList.remove('active');
        });
    });
}

// ======================== RANGE SLIDER ========================

/**
 * Updates the location rating display value in real-time
 * as the user drags the range slider.
 */
function initRangeSlider() {
    const slider = document.getElementById('locationRating');
    const display = document.getElementById('rating-value');

    if (!slider || !display) return;

    // Set initial value
    display.textContent = parseFloat(slider.value).toFixed(1);

    slider.addEventListener('input', () => {
        display.textContent = parseFloat(slider.value).toFixed(1);
    });
}

// ======================== FORM VALIDATION ========================

/**
 * Client-side validation for the prediction form.
 * Validates each field in real-time (on blur) and on submit.
 * Mirrors the server-side Jakarta Bean Validation constraints.
 */
function initFormValidation() {
    const form = document.getElementById('prediction-form');
    if (!form) return;

    const validationRules = {
        area: {
            min: 500,
            max: 10000,
            message: 'Area must be between 500 and 10,000 sq.ft'
        },
        bedrooms: {
            min: 1,
            max: 10,
            message: 'Bedrooms must be between 1 and 10'
        },
        bathrooms: {
            min: 1,
            max: 8,
            message: 'Bathrooms must be between 1 and 8'
        },
        age: {
            min: 0,
            max: 100,
            message: 'Age must be between 0 and 100 years'
        }
    };

    // Validate on blur (when user leaves a field)
    Object.keys(validationRules).forEach(fieldId => {
        const input = document.getElementById(fieldId);
        if (!input) return;

        input.addEventListener('blur', () => {
            validateField(input, validationRules[fieldId]);
        });

        input.addEventListener('input', () => {
            // Clear error state while typing
            const group = document.getElementById('group-' + fieldId);
            if (group) {
                group.classList.remove('has-error');
            }
        });
    });

    // Validate all fields on form submit
    form.addEventListener('submit', (e) => {
        let isValid = true;

        Object.keys(validationRules).forEach(fieldId => {
            const input = document.getElementById(fieldId);
            if (input && !validateField(input, validationRules[fieldId])) {
                isValid = false;
            }
        });

        if (!isValid) {
            e.preventDefault();
            // Scroll to first error
            const firstError = form.querySelector('.has-error');
            if (firstError) {
                firstError.scrollIntoView({ behavior: 'smooth', block: 'center' });
            }
        }
    });
}

/**
 * Validates a single form field against its rules.
 * Shows/hides error messages and updates field styling.
 * 
 * @param {HTMLElement} input - The input element to validate
 * @param {Object} rules - Validation rules (min, max, message)
 * @returns {boolean} Whether the field is valid
 */
function validateField(input, rules) {
    const value = parseFloat(input.value);
    const group = document.getElementById('group-' + input.id);
    const errorId = 'error-' + input.id;
    let errorEl = document.getElementById(errorId);

    // Remove existing client-side error
    if (errorEl && errorEl.classList.contains('js-error')) {
        errorEl.remove();
    }

    if (isNaN(value) || value < rules.min || value > rules.max) {
        // Show error
        if (group) {
            group.classList.add('has-error');
        }

        // Create error element if it doesn't exist (server-side errors may already be shown)
        if (!document.getElementById(errorId)) {
            errorEl = document.createElement('div');
            errorEl.id = errorId;
            errorEl.className = 'form-error js-error';
            errorEl.innerHTML = `<span>${rules.message}</span>`;
            input.parentNode.appendChild(errorEl);
        }

        input.style.borderColor = '#ef4444';
        return false;
    }

    // Clear error
    if (group) {
        group.classList.remove('has-error');
    }
    input.style.borderColor = '';
    return true;
}

// ======================== FORM SUBMIT ========================

/**
 * Shows a loading spinner on the submit button when the form is submitted.
 */
function initFormSubmit() {
    const form = document.getElementById('prediction-form');
    const btnPredict = document.getElementById('btn-predict');

    if (!form || !btnPredict) return;

    form.addEventListener('submit', () => {
        const btnText = btnPredict.querySelector('.btn-text');
        const btnLoading = btnPredict.querySelector('.btn-loading');

        if (btnText && btnLoading) {
            btnText.style.display = 'none';
            btnLoading.style.display = 'inline-flex';
        }

        btnPredict.disabled = true;
        btnPredict.style.opacity = '0.7';
    });
}

// ======================== STAT COUNTERS ========================

/**
 * Animates stat numbers on the home page from 0 to their target value.
 * Uses Intersection Observer to trigger animation when visible.
 */
function initStatCounters() {
    const statNumbers = document.querySelectorAll('.stat-number[data-target]');
    if (statNumbers.length === 0) return;

    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                animateCounter(entry.target);
                observer.unobserve(entry.target);
            }
        });
    }, { threshold: 0.5 });

    statNumbers.forEach(el => observer.observe(el));
}

/**
 * Animates a single counter from 0 to its data-target value.
 * 
 * @param {HTMLElement} element - The counter element
 */
function animateCounter(element) {
    const target = parseInt(element.getAttribute('data-target'));
    if (isNaN(target) || target === 0) return;

    const duration = 1500; // ms
    const start = performance.now();

    function update(currentTime) {
        const elapsed = currentTime - start;
        const progress = Math.min(elapsed / duration, 1);

        // Ease-out cubic
        const eased = 1 - Math.pow(1 - progress, 3);
        const current = Math.round(eased * target);

        element.textContent = current.toLocaleString();

        if (progress < 1) {
            requestAnimationFrame(update);
        }
    }

    requestAnimationFrame(update);
}

// ======================== PRICE ANIMATION ========================

/**
 * Animates the predicted price with a count-up effect on the result page.
 */
function initPriceAnimation() {
    const priceEl = document.getElementById('price-amount');
    if (!priceEl) return;

    const rawPrice = parseFloat(priceEl.getAttribute('data-price'));
    if (isNaN(rawPrice)) return;

    const duration = 2000;
    const start = performance.now();

    function update(currentTime) {
        const elapsed = currentTime - start;
        const progress = Math.min(elapsed / duration, 1);

        // Ease-out expo
        const eased = progress === 1 ? 1 : 1 - Math.pow(2, -10 * progress);
        const current = eased * rawPrice;

        // Format as currency
        priceEl.querySelector('span').textContent = '₹' + Math.round(current).toLocaleString('en-IN');

        if (progress < 1) {
            requestAnimationFrame(update);
        } else {
            // Final formatted value
            priceEl.querySelector('span').textContent = new Intl.NumberFormat('en-IN', {
                style: 'currency',
                currency: 'INR',
                minimumFractionDigits: 2
            }).format(rawPrice);
        }
    }

    requestAnimationFrame(update);
}

// ======================== DASHBOARD CHARTS ========================

/**
 * Initializes Chart.js charts on the dashboard page.
 * Uses data injected by Thymeleaf into window.dashboardData.
 */
function initDashboardCharts() {
    if (!window.dashboardData) return;

    const { labels, prices, areas } = window.dashboardData;

    // Global Chart.js defaults for dark theme
    if (typeof Chart !== 'undefined') {
        Chart.defaults.color = '#94a3b8';
        Chart.defaults.borderColor = 'rgba(255, 255, 255, 0.06)';
        Chart.defaults.font.family = "'Inter', sans-serif";
    }

    initBarChart(labels, prices);
    initAreaChart(labels, prices, areas);
}

/**
 * Creates a bar chart showing predicted prices for each prediction.
 */
function initBarChart(labels, prices) {
    const canvas = document.getElementById('priceBarChart');
    if (!canvas || typeof Chart === 'undefined') return;

    new Chart(canvas.getContext('2d'), {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Predicted Price (₹)',
                data: prices,
                backgroundColor: createGradientArray(prices.length, 
                    'rgba(99, 102, 241, 0.6)', 'rgba(139, 92, 246, 0.6)'),
                borderColor: createGradientArray(prices.length,
                    'rgba(99, 102, 241, 1)', 'rgba(139, 92, 246, 1)'),
                borderWidth: 1,
                borderRadius: 6,
                borderSkipped: false
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    backgroundColor: 'rgba(17, 24, 39, 0.9)',
                    titleColor: '#f1f5f9',
                    bodyColor: '#94a3b8',
                    borderColor: 'rgba(99, 102, 241, 0.3)',
                    borderWidth: 1,
                    padding: 12,
                    cornerRadius: 8,
                    callbacks: {
                        label: function(context) {
                            return '  ₹' + context.parsed.y.toLocaleString('en-IN');
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    grid: {
                        color: 'rgba(255, 255, 255, 0.04)'
                    },
                    ticks: {
                        callback: function(value) {
                            if (value >= 10000000) return '₹' + (value / 10000000).toFixed(2) + 'Cr';
                            if (value >= 100000) return '₹' + (value / 100000).toFixed(2) + 'L';
                            if (value >= 1000) return '₹' + (value / 1000).toFixed(0) + 'K';
                            return '₹' + value.toLocaleString('en-IN');
                        }
                    }
                },
                x: {
                    grid: {
                        display: false
                    }
                }
            },
            animation: {
                duration: 1000,
                easing: 'easeOutQuart'
            }
        }
    });
}

/**
 * Creates a scatter/line chart showing price vs area relationship.
 */
function initAreaChart(labels, prices, areas) {
    const canvas = document.getElementById('priceAreaChart');
    if (!canvas || typeof Chart === 'undefined') return;

    // Create scatter data points
    const scatterData = areas.map((area, i) => ({
        x: area,
        y: prices[i]
    }));

    // Sort by area for the trend line
    const sorted = [...scatterData].sort((a, b) => a.x - b.x);

    new Chart(canvas.getContext('2d'), {
        type: 'scatter',
        data: {
            datasets: [
                {
                    label: 'Predictions',
                    data: scatterData,
                    backgroundColor: 'rgba(99, 102, 241, 0.7)',
                    borderColor: 'rgba(99, 102, 241, 1)',
                    pointRadius: 6,
                    pointHoverRadius: 9,
                    pointBorderWidth: 2,
                    pointHoverBorderWidth: 3
                },
                {
                    label: 'Trend',
                    data: sorted,
                    type: 'line',
                    borderColor: 'rgba(139, 92, 246, 0.5)',
                    borderWidth: 2,
                    borderDash: [5, 5],
                    pointRadius: 0,
                    fill: false,
                    tension: 0.4
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    labels: {
                        usePointStyle: true,
                        padding: 16
                    }
                },
                tooltip: {
                    backgroundColor: 'rgba(17, 24, 39, 0.9)',
                    titleColor: '#f1f5f9',
                    bodyColor: '#94a3b8',
                    borderColor: 'rgba(99, 102, 241, 0.3)',
                    borderWidth: 1,
                    padding: 12,
                    cornerRadius: 8,
                    callbacks: {
                        label: function(context) {
                            return `  Area: ${context.parsed.x.toLocaleString('en-IN')} sq.ft | Price: ₹${context.parsed.y.toLocaleString('en-IN')}`;
                        }
                    }
                }
            },
            scales: {
                x: {
                    title: {
                        display: true,
                        text: 'Area (sq.ft)',
                        color: '#94a3b8',
                        font: { weight: '600' }
                    },
                    grid: {
                        color: 'rgba(255, 255, 255, 0.04)'
                    }
                },
                y: {
                    title: {
                        display: true,
                        text: 'Price (₹)',
                        color: '#94a3b8',
                        font: { weight: '600' }
                    },
                    grid: {
                        color: 'rgba(255, 255, 255, 0.04)'
                    },
                    ticks: {
                        callback: function(value) {
                            if (value >= 10000000) return '₹' + (value / 10000000).toFixed(2) + 'Cr';
                            if (value >= 100000) return '₹' + (value / 100000).toFixed(2) + 'L';
                            if (value >= 1000) return '₹' + (value / 1000).toFixed(0) + 'K';
                            return '₹' + value.toLocaleString('en-IN');
                        }
                    }
                }
            },
            animation: {
                duration: 1200,
                easing: 'easeOutQuart'
            }
        }
    });
}

/**
 * Creates an array of colors for chart bars, interpolating between two colors.
 * 
 * @param {number} count - Number of colors to generate
 * @param {string} startColor - Starting color
 * @param {string} endColor - Ending color
 * @returns {string[]} Array of color strings
 */
function createGradientArray(count, startColor, endColor) {
    if (count <= 1) return [startColor];
    return Array.from({ length: count }, (_, i) => {
        const ratio = i / (count - 1);
        return ratio < 0.5 ? startColor : endColor;
    });
}
