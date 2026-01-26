// API Configuration
const API_BASE_URL = 'http://localhost:8080/api';

// DOM Elements
const tabs = document.querySelectorAll('.tab-btn');
const tabContents = document.querySelectorAll('.tab-content');

// Tab Switching
tabs.forEach(tab => {
    tab.addEventListener('click', () => {
        const tabName = tab.dataset.tab;
        
        // Update active tab
        tabs.forEach(t => t.classList.remove('active'));
        tab.classList.add('active');
        
        // Update active content
        tabContents.forEach(content => {
            content.classList.remove('active');
        });
        document.getElementById(`${tabName}-tab`).classList.add('active');
    });
});

// ========== DELAYS TAB ==========

// Fetch Delays Form
document.getElementById('fetch-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const type = document.getElementById('type').value;
    const iataCode = document.getElementById('iataCode').value.toUpperCase();
    const minDelay = document.getElementById('minDelay').value;
    
    await fetchDelays(type, iataCode, minDelay);
});

// Search Form
document.getElementById('search-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const iataCode = document.getElementById('searchIata').value;
    const airline = document.getElementById('searchAirline').value;
    const depIata = document.getElementById('searchDepIata').value;
    
    await searchDelays(iataCode, airline, depIata);
});

// Fetch Delays Function
async function fetchDelays(type, iataCode, minDelay) {
    const loading = document.getElementById('loading');
    const error = document.getElementById('error');
    const resultsSection = document.getElementById('results-section');
    
    // Show loading, hide others
    loading.classList.remove('hidden');
    error.classList.add('hidden');
    resultsSection.classList.add('hidden');
    
    try {
        const response = await fetch(`${API_BASE_URL}/delays/fetch?type=${type}&iataCode=${iataCode}&minDelay=${minDelay}`, {
            method: 'POST'
        });
        
        const data = await response.json();
        
        if (data.success) {
            displayFlights(data.data, data.count, data.responseTime);
        } else {
            throw new Error(data.error || 'Failed to fetch delays');
        }
        
    } catch (err) {
        showError(err.message);
    } finally {
        loading.classList.add('hidden');
    }
}

// Search Delays Function
async function searchDelays(iataCode, airline, depIata) {
    const loading = document.getElementById('loading');
    const error = document.getElementById('error');
    const resultsSection = document.getElementById('results-section');
    
    loading.classList.remove('hidden');
    error.classList.add('hidden');
    resultsSection.classList.add('hidden');
    
    try {
        const params = new URLSearchParams();
        if (iataCode) params.append('iataCode', iataCode);
        if (airline) params.append('airline', airline);
        if (depIata) params.append('depIata', depIata);
        
        const response = await fetch(`${API_BASE_URL}/delays/search?${params}`);
        const data = await response.json();
        
        if (data.success) {
            displayFlights(data.data, data.count);
        } else {
            throw new Error(data.error || 'Search failed');
        }
        
    } catch (err) {
        showError(err.message);
    } finally {
        loading.classList.add('hidden');
    }
}

// Display Flights
function displayFlights(flights, count, responseTime) {
    const resultsSection = document.getElementById('results-section');
    const container = document.getElementById('flights-container');
    const countBadge = document.getElementById('results-count');
    const timeBadge = document.getElementById('response-time');
    
    countBadge.textContent = `${count} flights`;
    
    if (responseTime) {
        timeBadge.textContent = `⚡ ${responseTime}`;
        timeBadge.classList.remove('hidden');
    }
    
    container.innerHTML = '';
    
    if (flights.length === 0) {
        container.innerHTML = '<p style="color:white;text-align:center;">No flights found</p>';
    } else {
        flights.forEach(flight => {
            const card = createFlightCard(flight);
            container.appendChild(card);
        });
    }
    
    resultsSection.classList.remove('hidden');
}

// Create Flight Card
function createFlightCard(flight) {
    const card = document.createElement('div');
    card.className = 'flight-card';
    
    card.innerHTML = `
        <h3>${flight.flightIata || flight.flightNumber || 'N/A'}</h3>
        <div class="flight-info">
            <div class="flight-info-row">
                <span class="label">Airline:</span>
                <span class="value">${flight.airlineIata || 'N/A'}</span>
            </div>
            <div class="flight-info-row">
                <span class="label">Route:</span>
                <span class="value">${flight.depIata || 'N/A'} → ${flight.arrIata || 'N/A'}</span>
            </div>
            <div class="flight-info-row">
                <span class="label">Departure:</span>
                <span class="value">${flight.depTime || 'N/A'}</span>
            </div>
            <div class="flight-info-row">
                <span class="label">Arrival:</span>
                <span class="value">${flight.arrTime || 'N/A'}</span>
            </div>
            ${flight.delayMinutes ? `
            <div class="flight-info-row">
                <span class="label">Delay:</span>
                <span class="delay-value">${flight.delayMinutes} min</span>
            </div>
            ` : ''}
        </div>
    `;
    
    return card;
}

// Show Error
function showError(message) {
    const error = document.getElementById('error');
    error.textContent = `❌ Error: ${message}`;
    error.classList.remove('hidden');
}

// ========== REALTIME TAB ==========

document.getElementById('realtime-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const depIata = document.getElementById('depIata').value.toUpperCase();
    await fetchRealtimeFlights(depIata);
});

async function fetchRealtimeFlights(depIata) {
    const loading = document.getElementById('realtime-loading');
    const error = document.getElementById('realtime-error');
    const results = document.getElementById('realtime-results');
    
    loading.classList.remove('hidden');
    error.classList.add('hidden');
    results.classList.add('hidden');
    
    try {
        const response = await fetch(`${API_BASE_URL}/realtime-flights?dep_iata=${depIata}`);
        const data = await response.json();
        
        if (data.success) {
            displayRealtimeFlights(data.data, data.count);
        } else {
            throw new Error(data.error || 'Failed to fetch realtime flights');
        }
        
    } catch (err) {
        error.textContent = `❌ Error: ${err.message}`;
        error.classList.remove('hidden');
    } finally {
        loading.classList.add('hidden');
    }
}

function displayRealtimeFlights(flights, count) {
    const results = document.getElementById('realtime-results');
    const container = document.getElementById('realtime-container');
    const countBadge = document.getElementById('realtime-count');
    
    countBadge.textContent = `${count} flights`;
    container.innerHTML = '';
    
    if (flights.length === 0) {
        container.innerHTML = '<p style="color:white;text-align:center;">No flights found</p>';
    } else {
        flights.forEach(flight => {
            const card = createRealtimeCard(flight);
            container.appendChild(card);
        });
    }
    
    results.classList.remove('hidden');
}

function createRealtimeCard(flight) {
    const card = document.createElement('div');
    card.className = 'flight-card';
    
    const statusClass = flight.status ? `status-${flight.status.toLowerCase().replace('-', '')}` : '';
    
    card.innerHTML = `
        <h3>${flight.flightIata || 'N/A'}</h3>
        <div class="flight-info">
            <div class="flight-info-row">
                <span class="label">Status:</span>
                <span class="status-badge ${statusClass}">${flight.status || 'Unknown'}</span>
            </div>
            <div class="flight-info-row">
                <span class="label">Route:</span>
                <span class="value">${flight.depIata || 'N/A'} → ${flight.arrIata || 'N/A'}</span>
            </div>
            <div class="flight-info-row">
                <span class="label">Position:</span>
                <span class="value position-badge">${flight.lat?.toFixed(2) || 'N/A'}, ${flight.lng?.toFixed(2) || 'N/A'}</span>
            </div>
            <div class="flight-info-row">
                <span class="label">Altitude:</span>
                <span class="value">${flight.alt ? flight.alt + ' ft' : 'N/A'}</span>
            </div>
            <div class="flight-info-row">
                <span class="label">Speed:</span>
                <span class="value">${flight.speed ? flight.speed + ' kts' : 'N/A'}</span>
            </div>
            <div class="flight-info-row">
                <span class="label">Direction:</span>
                <span class="value">${flight.dir ? flight.dir.toFixed(1) + '°' : 'N/A'}</span>
            </div>
        </div>
    `;
    
    return card;
}

// ========== CACHE STATS TAB ==========

document.getElementById('load-cache-btn').addEventListener('click', loadCacheStats);
document.getElementById('clear-cache-btn').addEventListener('click', clearCache);

async function loadCacheStats() {
    try {
        const response = await fetch(`${API_BASE_URL}/delays/cache-info`);
        const data = await response.json();
        
        if (data.success) {
            displayCacheStats(data.cache);
        }
    } catch (err) {
        alert('Failed to load cache stats: ' + err.message);
    }
}

function displayCacheStats(cache) {
    const statsSection = document.getElementById('cache-stats');
    
    // Update stat cards
    document.getElementById('hit-rate').textContent = cache.hitRate;
    document.getElementById('cache-size').textContent = `${cache.size}/${cache.capacity}`;
    document.getElementById('evictions').textContent = cache.evictionCount;
    document.getElementById('total-hits').textContent = cache.hitCount;
    
    // Display cache entries
    const entriesContainer = document.getElementById('cache-entries');
    
    if (cache.entries && cache.entries.length > 0) {
        let tableHTML = `
            <table class="cache-table">
                <thead>
                    <tr>
                        <th>Cache Key</th>
                        <th>Flight Count</th>
                        <th>Size (KB)</th>
                        <th>Hit Count</th>
                    </tr>
                </thead>
                <tbody>
        `;
        
        cache.entries.forEach(entry => {
            tableHTML += `
                <tr>
                    <td>${entry.key}</td>
                    <td>${entry.flightCount}</td>
                    <td>${entry.sizeKB.toFixed(2)}</td>
                    <td>${entry.hitCount}</td>
                </tr>
            `;
        });
        
        tableHTML += `</tbody></table>`;
        entriesContainer.innerHTML = tableHTML;
    } else {
        entriesContainer.innerHTML = '<p>No cache entries</p>';
    }
    
    statsSection.classList.remove('hidden');
}

async function clearCache() {
    if (!confirm('Are you sure you want to clear all cache?')) {
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/delays/clear-cache`, {
            method: 'POST'
        });
        
        const data = await response.json();
        
        if (data.success) {
            alert('✅ Cache cleared successfully!');
            loadCacheStats();
        }
    } catch (err) {
        alert('❌ Failed to clear cache: ' + err.message);
    }
}

// Initial load message
console.log('✅ Flight Schedules Frontend loaded');
console.log('📡 API Base URL:', API_BASE_URL);
console.log('💡 Make sure backend is running on http://localhost:8080');
