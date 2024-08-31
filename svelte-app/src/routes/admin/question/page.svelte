<script>
    import { onMount, onDestroy } from 'svelte';
    import Star from './Star.svelte';
    import Chart from 'chart.js/auto';

    const questionsPerPage = 8;

    import { backendBaseUrl } from '../../../store';

    let canvas;
    let chartInstance = null;

    let question = null;
    let answers = [];
    let displayedAnswers = [];
    let message = '';
    let currentPage = 1;

    let selectedIncident = '';
    let selectedEHelpDesk = '';
    let selectedUser = '';
    let selectedServiceMan = '';
    let selectedResponse = '';

    let selectedStartDate = '';
    let selectedEndDate = '';

    let showIncidentFilter = false;
    let showEHelpDeskFilter = false;
    let showUserFilter = false;
    let showServiceManFilter = false;
    let showResponseFilter = false;

    let availableIncidents = [];
    let availableEHelpDesks = [];
    let availableUsers = [];
    let availableServiceMen = [];
    let availableResponses = [];

    let totalPagesCount = 1;

    let sentForms = 0;
    let filledForms = 0;
    let filledPercentage = 0;

    let averageRating = 0;

    let employeeStarRanking = [];
    let employeeRadioRanking = [];

    let allAnswerAmount = 0;
    let noAnswerAmount = 0;
    let percentageRadio = 0;
    let percentageNoRadio = 0;
    let showTrueAnswers = true;

    // BLOCK 1: Functions used only in main1
    const nextPage = () => {
        if (currentPage < totalPagesCount) {
            currentPage++;
            updateDisplayedAnswers();
        }
    };

    const prevPage = () => {
        if (currentPage > 1) {
            currentPage--;
            updateDisplayedAnswers();
        }
    };

    const goToPage = (pageNumber) => {
        currentPage = pageNumber;
        updateDisplayedAnswers();
    };

    const toggleFilter = (filterType) => {
        showIncidentFilter = false;
        showEHelpDeskFilter = false;
        showUserFilter = false;
        showServiceManFilter = false;
        showResponseFilter = false;

        switch (filterType) {
            case 'incident':
                showIncidentFilter = !showIncidentFilter;
                break;
            case 'ehelpdesk':
                showEHelpDeskFilter = !showEHelpDeskFilter;
                break;
            case 'user':
                showUserFilter = !showUserFilter;
                break;
            case 'serviceman':
                showServiceManFilter = !showServiceManFilter;
                break;
            case 'response':
                showResponseFilter = !showResponseFilter;
                break;
        }
    };

    const selectFilter = (filterType, value) => {
        switch (filterType) {
            case 'incident':
                selectedIncident = value;
                break;
            case 'ehelpdesk':
                selectedEHelpDesk = value;
                calculateAverageRating();
                calculateEmployeeStarRanking();
                calculateRadioStats();
                break;
            case 'user':
                selectedUser = value;
                calculateAverageRating();
                calculateEmployeeStarRanking();
                calculateRadioStats();
                break;
            case 'serviceman':
                selectedServiceMan = value;
                break;
            case 'response':
                selectedResponse = value.toString();
                break;
        }
        currentPage = 1;
        updateAvailableFilters();
        updateDisplayedAnswers();
        totalPagesCount = totalPages();
        closeAllFilters();
    };

    const closeAllFilters = () => {
        showIncidentFilter = false;
        showEHelpDeskFilter = false;
        showUserFilter = false;
        showServiceManFilter = false;
        showResponseFilter = false;
    };

    const resetDates = () => {
        selectedStartDate = '';
        selectedEndDate = '';
        currentPage = 1;
        fetchQuestionAndAnswers();
    };

    const resetAllFilters = () => {
        selectedIncident = '';
        selectedEHelpDesk = '';
        selectedUser = '';
        selectedServiceMan = '';
        selectedResponse = '';

        currentPage = 1;
        updateAvailableFilters();
        updateDisplayedAnswers();
        calculateAverageRating();
        calculateEmployeeStarRanking();
        calculateRadioStats();
        totalPagesCount = totalPages();
    };

    const handleDateChange = () => {
        if (selectedStartDate && selectedEndDate && new Date(selectedStartDate) > new Date(selectedEndDate)) {
            selectedEndDate = selectedStartDate;
        }
        if (selectedStartDate && selectedEndDate) {
            currentPage = 1;
            fetchQuestionAndAnswers();
        }
    };

    // BLOCK 2: Functions used only in main2
    const calculateEmployeeStarRanking = () => {
        const filtered = filteredAnswers();
        const employeeScores = {};

        filtered.forEach(answer => {
            const serviceMan = answer.serviceman;
            const rating = Number(answer.value);

            if (!isNaN(rating)) {
                if (!employeeScores[serviceMan]) {
                    employeeScores[serviceMan] = { total: 0, count: 0 };
                }

                employeeScores[serviceMan].total += rating;
                employeeScores[serviceMan].count += 1;
            }
        });

        employeeStarRanking = Object.keys(employeeScores)
            .map(serviceMan => ({
                serviceMan,
                averageRating: (employeeScores[serviceMan].total / employeeScores[serviceMan].count).toFixed(1),
                count: employeeScores[serviceMan].count
            }))
            .sort((a, b) => {
                if (b.averageRating === a.averageRating) {
                    return b.count - a.count;
                }
                return b.averageRating - a.averageRating;
            });
    };

    const calculateEmployeeRadioRanking = () => {
        const filtered = filteredAnswers();
        const employeeScores = {};

        filtered.forEach(answer => {
            const serviceMan = answer.serviceman;
            const isTrueAnswer = answer.value === 'true';

            const considerAnswer = showTrueAnswers ? isTrueAnswer : !isTrueAnswer;

            if (!employeeScores[serviceMan]) {
                employeeScores[serviceMan] = { trueCount: 0, totalCount: 0 };
            }

            if (considerAnswer) {
                employeeScores[serviceMan].trueCount += 1;
            }

            employeeScores[serviceMan].totalCount += 1;
        });

        employeeRadioRanking = Object.keys(employeeScores)
            .map(serviceMan => {
                const totalCount = employeeScores[serviceMan].totalCount;
                const trueCount = employeeScores[serviceMan].trueCount;
                const percentage = totalCount > 0 ? Math.floor(((trueCount / totalCount) * 100)) : 0;
                return {
                    serviceMan,
                    percentage: `${percentage}%`,
                    totalCount
                };
            })
            .sort((a, b) => {
                const aPercentage = parseFloat(a.percentage);
                const bPercentage = parseFloat(b.percentage);
                if (bPercentage === aPercentage) {
                    return b.totalCount - a.totalCount;
                }
                return bPercentage - aPercentage;
            });
    };

    const changeLeadingAnswer = () => {
        showTrueAnswers = !showTrueAnswers;
        calculateRadioStats();
        updateDisplayedAnswers();
    };

    // BLOCK 3: Functions used only in main3
    function calculateCounts() {
        const counts = {};
        const dates = [];

        answers.forEach(answer => {
            const date = new Date(answer.date).toISOString().split('T')[0];
            if (!counts[date]) {
                counts[date] = 0;
                dates.push(date);
            }
            counts[date]++;
        });

        dates.sort((a, b) => new Date(a) - new Date(b));
        return { dates, counts: dates.map(date => counts[date]) };
    }

    function setupChart() {
        if (!canvas) {
            return;
        }
        const ctx = canvas.getContext('2d');
        const { dates, counts } = calculateCounts();

        if (chartInstance) {
            chartInstance.destroy();
        }

        chartInstance = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: dates,
                datasets: [{
                    label: 'Ilość odpowiedzi',
                    data: counts,
                    backgroundColor: 'rgba(75, 192, 192, 0.2)',
                    borderColor: 'rgba(75, 192, 192, 1)',
                    borderWidth: 1,
                    maxBarThickness: 20
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                aspectRatio: 2,
                scales: {
                    x: {
                        title: {
                            display: true,
                            text: 'Data'
                        }
                    },
                    y: {
                        beginAtZero: true,
                        title: {
                            display: true,
                            text: 'Ilość odpowiedzi'
                        },
                        ticks: {
                            stepSize: 1
                        }
                    }
                }
            }
        });
    }

    // BLOCK 4: Shared functions used in two or more mains
    const fetchQuestionAndAnswers = async () => {
        const urlParams = new URLSearchParams(window.location.search);
        const id = urlParams.get('id');

        if (id) {
            try {
                let url = backendBaseUrl + '/api/admin/question/' + id;

                if (selectedStartDate && selectedEndDate) {
                    const dateParams = new URLSearchParams();
                    dateParams.append('start_date', selectedStartDate);
                    dateParams.append('end_date', selectedEndDate);
                    url += `?${dateParams.toString()}`;
                }

                const response = await fetch(url, {
                    method: 'GET',
                    credentials: 'include'
                });

                const data = await response.json();

                if (response.ok || response.status === 404) {
                   
                    question = {
                        id: data.id,
                        version: data.version,
                        questionText: data.questionText,
                        questionType: data.questionType
                    };
                    sentForms = data.sent || 0;
                    filledForms = data.filled || 0;
                    filledPercentage = sentForms > 0 ? Math.round((filledForms / sentForms) * 100) : 0;

                    if (data.answers && data.answers.length > 0) {
                        answers = data.answers;
                        updateAvailableFilters();
                        updateDisplayedAnswers();
                        calculateAverageRating();
                        calculateEmployeeStarRanking();
                        calculateRadioStats();
                        setupChart();
                        message = '';
                    } else {
                        answers = [];
                        displayedAnswers = [];
                        message = data.message;
                        employeeStarRanking = [];
                        employeeRadioRanking = [];
                        averageRating = 0;
                        allAnswerAmount = 0;
                        noAnswerAmount = 0;
                        percentageRadio = 0;
                        percentageNoRadio = 0;
                        setupChart();
                    }

                }
                else {
                    throw new Error('Network response was not ok');
                }
            } catch (error) {
                console.error('Error fetching data:', error);
            }
        } else {
            message = 'Brak ID w URL';
            console.error('ID not found in URL');
        }
    };

    const calculateAverageRating = () => {
        const filtered = filteredAnswers();
        const ratings = filtered
            .filter(answer => !isNaN(answer.value))
            .map(answer => Number(answer.value));

        if (ratings.length > 0) {
            const sum = ratings.reduce((acc, rating) => acc + rating, 0);
            averageRating = (sum / ratings.length).toFixed(2);
        } else {
            averageRating = 0;
        }
    };

    const calculateRadioStats = () => {
        const filtered = filteredAnswers();

        allAnswerAmount = filtered.filter(answer => answer.value === 'true').length;
        noAnswerAmount = filtered.filter(answer => answer.value === 'false').length;

        const totalAnswers = allAnswerAmount + noAnswerAmount;
        if (totalAnswers > 0) {
            percentageRadio = Math.round(((allAnswerAmount / totalAnswers) * 100));
            percentageNoRadio = Math.round(((noAnswerAmount / totalAnswers) * 100));
        } else {
            percentageRadio = 0;
            percentageNoRadio = 0;
        }

        calculateEmployeeRadioRanking();
    };

    const updateDisplayedAnswers = () => {
        const start = (currentPage - 1) * questionsPerPage;
        const end = start + questionsPerPage;
        displayedAnswers = filteredAnswers().slice(start, end);

        totalPagesCount = totalPages();
    };

    const filteredAnswers = () => {
        return answers.filter(answer => {
            return (
                (selectedIncident === '' || answer.incidentNumber.toString().includes(selectedIncident)) &&
                (selectedEHelpDesk === '' || answer.ehelpdesk == selectedEHelpDesk) &&
                (selectedUser === '' || answer.user == selectedUser) &&
                (selectedServiceMan === '' || answer.serviceman == selectedServiceMan) &&
                (selectedResponse === '' || 
                    (selectedResponse === 'true' && answer.value === 'true') ||
                    (selectedResponse === 'false' && answer.value === 'false') ||
                    (Number(selectedResponse) === Number(answer.value)))
            );
        });
    };


    const updateAvailableFilters = () => {
        const filtered = filteredAnswers();

        availableIncidents = Array.from(new Set(filtered.map(a => a.incidentNumber)));
        availableEHelpDesks = Array.from(new Set(filtered.map(a => a.ehelpdesk)));
        availableUsers = Array.from(new Set(filtered.map(a => a.user)));
        availableServiceMen = Array.from(new Set(filtered.map(a => a.serviceman)));
        
        availableResponses = Array.from(new Set(filtered.map(a => {
            if (a.value === 'true') return 'TAK';
            if (a.value === 'false') return 'NIE';
            return Number(a.value);
        })));
    };

    const totalPages = () => {
        return Math.ceil(filteredAnswers().length / questionsPerPage);
    };

    const handleClickOutside = (event) => {
        const filterElements = [
            '.version-filter-popup',
            'th'
        ];

        const isClickInsideFilter = filterElements.some(selector => {
            return event.target.closest(selector);
        });

        if (!isClickInsideFilter) {
            closeAllFilters();
        }
    };

    onMount(() => {
        document.addEventListener('click', handleClickOutside);
        fetchQuestionAndAnswers();
    });

    onDestroy(() => {
        document.removeEventListener('click', handleClickOutside);
        if (chartInstance) {
            chartInstance.destroy();
        }
    });
</script>

<div class="main-container">
    <main class="main1">
        {#if question}
            <div>
                <p><strong>Wersja:</strong> {question.version}</p>
                <p><strong>Treść pytania:</strong> {question.questionText}</p>
            </div>

            <h2>Odpowiedzi</h2>

            <div class="date-filter">
                <label>
                    Data od:
                    <input
                        type="date"
                        bind:value={selectedStartDate}
                        on:change={handleDateChange}
                    />
                </label>
                <label>
                    Data do:
                    <input
                        type="date"
                        bind:value={selectedEndDate}
                        min={selectedStartDate || ''}
                        on:change={handleDateChange}
                    />
                </label>
                <button class="upperButtons" on:click={resetDates}>Resetuj daty</button>
                <button class="upperButtons" on:click={resetAllFilters}>Resetuj filtry</button>
            </div>

            {#if message !== ''}
                <p>{message}</p>
            {:else}
                <div class="scrollable-table-container table-container-main1">
                    <table>
                        <thead>
                            <tr>
                                <th>
                                    <input 
                                        type="text" 
                                        placeholder="Wpisz numer zgłoszenia" 
                                        bind:value={selectedIncident} 
                                        on:input={updateDisplayedAnswers} />
                                </th>
                                <th>
                                    Data
                                </th>
                                <th on:click={() => toggleFilter('ehelpdesk')}>
                                    eHelpDesk
                                    {#if showEHelpDeskFilter}
                                        <div class="version-filter-popup">
                                            <ul>
                                                <li on:click={(event) => { event.stopPropagation(); selectFilter('ehelpdesk', ''); }}>Wszystkie</li>
                                                {#each availableEHelpDesks as ehelpdesk}
                                                    <li on:click={(event) => { event.stopPropagation(); selectFilter('ehelpdesk', ehelpdesk); }}>{ehelpdesk}</li>
                                                {/each}
                                            </ul>
                                        </div>
                                    {/if}
                                </th>
                                <th on:click={() => toggleFilter('user')}>
                                    Użytkownik
                                    {#if showUserFilter}
                                        <div class="version-filter-popup">
                                            <ul>
                                                <li on:click={(event) => { event.stopPropagation(); selectFilter('user', ''); }}>Wszystkie</li>
                                                {#each availableUsers as user}
                                                    <li on:click={(event) => { event.stopPropagation(); selectFilter('user', user); }}>{user}</li>
                                                {/each}
                                            </ul>
                                        </div>
                                    {/if}
                                </th>
                                <th on:click={() => toggleFilter('serviceman')}>
                                    Serwisant
                                    {#if showServiceManFilter}
                                        <div class="version-filter-popup">
                                            <ul>
                                                <li on:click={(event) => { event.stopPropagation(); selectFilter('serviceman', ''); }}>Wszystkie</li>
                                                {#each availableServiceMen as serviceMan}
                                                    <li on:click={(event) => { event.stopPropagation(); selectFilter('serviceman', serviceMan); }}>{serviceMan}</li>
                                                {/each}
                                            </ul>
                                        </div>
                                    {/if}
                                </th>
                                <th on:click={() => toggleFilter('response')}>
                                    Odpowiedź
                                    {#if showResponseFilter}
                                        <div class="version-filter-popup">
                                            <ul>
                                                <li on:click={(event) => { event.stopPropagation(); selectFilter('response', ''); }}>Wszystkie</li>
                                                {#if availableResponses.includes('TAK')}
                                                    <li on:click={(event) => { event.stopPropagation(); selectFilter('response', 'true'); }}>TAK</li>
                                                {/if}
                                                {#if availableResponses.includes('NIE')}
                                                    <li on:click={(event) => { event.stopPropagation(); selectFilter('response', 'false'); }}>NIE</li>
                                                {/if}
                                                {#each [5, 4, 3, 2, 1] as stars}
                                                    {#if availableResponses.includes(stars)}
                                                        <li on:click={(event) => { event.stopPropagation(); selectFilter('response', stars); }}>
                                                            <Star rating={stars} />
                                                        </li>
                                                    {/if}
                                                {/each}
                                            </ul>
                                        </div>
                                    {/if}
                                </th>
                            </tr>
                        </thead>
                        <tbody>
                            {#each displayedAnswers as answer}
                                <tr>
                                    <td>{answer.incidentNumber}</td>
                                    <td>{answer.date}</td>
                                    <td>{answer.ehelpdesk}</td>
                                    <td>{answer.user}</td>
                                    <td>{answer.serviceman}</td>
                                    <td>
                                        {#if answer.value === 'true'}
                                            TAK
                                        {:else if answer.value === 'false'}
                                            NIE
                                        {:else if answer.value >= 1 && answer.value <= 5}
                                            <Star rating={answer.value} />
                                        {:else}
                                            {answer.value}
                                        {/if}
                                    </td>
                                </tr>
                            {/each}
                        </tbody>
                    </table>
                </div>
                <div class="pagination">
                    <button on:click={prevPage} disabled={currentPage === 1}>Poprzednia</button>

                    <ul class="page-list">
                        {#if currentPage > 2}
                            <li class="page-item" on:click={() => goToPage(1)}>1</li>
                            {#if currentPage > 3}
                                <li class="page-item">...</li>
                            {/if}
                        {/if}

                        {#each [-1, 0, 1] as offset}
                            {#if currentPage + offset > 0 && currentPage + offset <= totalPagesCount}
                            <li
                                class:selected={(currentPage === currentPage + offset)}
                                class="page-item"
                                on:click={() => goToPage(currentPage + offset)}>
                                {currentPage + offset}
                            </li>
                            {/if}
                        {/each}

                        {#if currentPage < totalPagesCount - 1}
                            {#if currentPage < totalPagesCount - 2}
                                <li class="page-item">...</li>
                            {/if}
                            <li class="page-item" on:click={() => goToPage(totalPagesCount)}>{totalPagesCount}</li>
                        {/if}
                    </ul>

                    <button on:click={nextPage} disabled={currentPage === totalPagesCount}>Następna</button>
                </div>

            {/if}
        {/if}
    </main>

    <main class="main2">
        <h2>Statystyki</h2>
        <div>
            <p>Liczba wysłanych formularzy: {sentForms}</p>
            <p>Liczba uzupełnionych: {filledForms} | {filledPercentage}%</p>
    
            {#if question && question.questionType === 'STARS'}
                {#if selectedEHelpDesk || selectedUser}
                    <p>Średnia ocena wszystkich po przefiltrowaniu: {averageRating}</p>
                    <h3>Ranking pracowników po przefiltrowaniu</h3>
                {:else}
                    <p>Średnia ocena wszystkich pracowników: {averageRating}</p>
                    <h3>Ranking pracowników</h3>
                {/if}
                <div class="scrollable-table-container table-container-main2">
                    <table>
                        <thead>
                            <tr>
                                <th>Lp</th>
                                <th>Pracownik</th>
                                <th>Średnia ocena</th>
                                <th>Ilość odpowiedzi</th>
                            </tr>
                        </thead>
                        <tbody>
                            {#each employeeStarRanking as { serviceMan, averageRating, count }, index}
                                <tr>
                                    <td>{index + 1}</td>
                                    <td>{serviceMan}</td>
                                    <td>{averageRating}</td>
                                    <td>{count}</td>
                                </tr>
                            {/each}
                        </tbody>
                    </table>
                </div>
            {/if}
    
            {#if question && question.questionType === 'RADIO'}
            {#if selectedEHelpDesk || selectedUser}
                {#if showTrueAnswers}
                    <p>Ilość odpowiedzi TAK wszystkich po przefiltrowaniu: {allAnswerAmount} | {percentageRadio}%</p>
                    <h3>Ranking pracowników po przefiltrowaniu ze względu na TAK</h3>
                {:else}
                    <p>Ilość odpowiedzi NIE wszystkich po przefiltrowaniu: {noAnswerAmount} | {percentageNoRadio}%</p>
                    <h3>Ranking pracowników ze względu na NIE</h3>
                {/if}
            {:else}
                {#if showTrueAnswers}
                    <p>Ilość odpowiedzi TAK wszystkich pracowników: {allAnswerAmount} | {percentageRadio}%</p>
                    <h3>Ranking pracowników ze względu na TAK</h3>
                {:else}
                    <p>Ilość odpowiedzi NIE wszystkich pracowników: {noAnswerAmount} | {percentageNoRadio}%</p>
                    <h3>Ranking pracowników ze względu na NIE</h3>
                {/if}
            {/if}
            <div class="scrollable-table-container table-container-main2">
                <table>
                    <thead>
                        <tr>
                            <th>Miejsce</th>
                            <th>Pracownik</th>
                            {#if showTrueAnswers}
                            <th>Procent na TAK</th>
                            {:else}
                            <th>Procent na NIE</th>
                            {/if}
                            <th>Ilość wszystkich odpowiedzi</th>
                        </tr>
                    </thead>
                    <tbody>
                        {#each employeeRadioRanking as { serviceMan, percentage, totalCount }, index}
                            <tr>
                                <td>{index + 1}</td>
                                <td>{serviceMan}</td>
                                <td>{percentage}</td>
                                <td>{totalCount}</td>
                            </tr>
                        {/each}
                    </tbody>
                </table>
            </div>
            <button on:click={changeLeadingAnswer}>
                ZMIEŃ NA {showTrueAnswers ? 'NIE' : 'TAK'}
            </button>
        {/if}
        </div>
    </main>
    
    <main class="main3">
        <h2>Wykres wszystkich odpowiedzi w wybranych datach</h2>
        {#if message === ""}
            {#if selectedStartDate != '' && selectedEndDate != ''}
                <canvas bind:this={canvas}></canvas>
            {/if}
        {/if}
    </main>
</div>

<style>
    .scrollable-table-container {
        height: 540px;
        overflow-y: auto;
    }

    .table-container-main1 {
        max-height: 550px;
    }

    .table-container-main2 {
        max-height: 150px;
    }

    .scrollable-table-container th {
        position: sticky;
        top: 0;
        z-index: 2;
    }

    .main-container {
        display: grid;
        grid-template-columns: 1fr 1fr;
        grid-template-rows: auto auto;
        gap: 10px;
        max-width: 1700px;
        margin: auto;
    }

    .main1 {
        grid-column: 1 / 2;
        grid-row: 1 / 3;
        max-height: 1125px;
    }

    .main2 {
        grid-column: 2 / 3;
        grid-row: 1 / 2;
    }

    .main3 {
        grid-column: 2 / 3;
        grid-row: 2 / 3;
        position: relative;
        overflow-y: visible;
    }

    canvas {
        width: 95% !important;
        height: 85% !important;
        max-width: 95%;
        max-height: 85%;
    }

    main {
        max-width: none;
        margin: 0;
        width: 100%;
        overflow-y: auto;
        padding-right: 15px;
        padding-top: 5px;
        padding-bottom: 5px;
    }

    .upperButtons {
        width: auto;
    }

    .pagination {
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer; 
        flex-wrap: wrap;
    }

    .page-list {
        list-style: none;
        display: flex;
        gap: 5px;
        margin: 0 15px;
        padding: 0;
        flex-wrap: wrap;
    }

    h2 {
        margin-bottom: 0px;
    }

    .page-list li.selected {
        font-weight: bold;
    }

    .page-item {
        background-color: transparent;
        padding: 1em;
        border-radius: 5px;
        border: none;
        margin-bottom: 0;
        font-size: large;
        margin-top: 1em;
        cursor: pointer;
    }

    .date-filter {
        margin: 15px 0;
        display: flex;
        gap: 15px;
        align-items: center;
        flex-wrap: wrap;
    }

    .date-filter input[type="date"] {
        padding: 10px;
        border: 1px solid #ccc;
        border-radius: 5px;
        font-size: 16px;
        width: 100%;
        max-width: 200px;
    }

    @media (max-width: 1600px) {
        .date-filter {
            flex-direction: column;
            align-items: flex-start;
        }

        .upperButtons {
            width: 100%;
        }
    }

    @media (max-width: 1200px) {
        .main-container {
            grid-template-columns: 1fr;
            grid-template-rows: auto;
        }

        .main1, .main2, .main3 {
            grid-column: 1 / -1;
            grid-row: auto;
        }
    }

    @media (max-width: 768px) {
        .pagination {
            flex-direction: column;
        }

        .upperButtons {
            width: 100%;
        }

        .page-item {
            font-size: medium;
            padding: 0.5em;
        }

        .date-filter {
            flex-direction: column;
            align-items: flex-start;
            gap: 10px;
        }

        .date-filter input[type="date"] {
            width: 100%;
        }
    }
    
    @media (max-width: 480px) {
        main > * {
            display: none !important;
        }

        main::before {
            content: "Zbyt mała rozdzielczość ekranu";
            display: block;
            color: var(--primary-color);
            font-size: 1.5em;
            text-align: center;
            padding: 20px;
            margin-top: 50%;
            transform: translateY(-50%);
        }
    }
</style>
