<script>
    import { onMount, onDestroy } from 'svelte';
    import { Link } from 'svelte-routing';
    import { backendBaseUrl } from '../../../store';

    let questions = [];
    let selectedVersion = '';
    let showVersionFilter = false;
    let sentForms = 0;
    let filledForms = 0;
    let filledPercentage = 0;

    const fetchData = async () => {
        try {
            const response = await fetch(`${backendBaseUrl}/api/admin/questions`, {
                method: 'GET',
                credentials: 'include'
            });
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            const data = await response.json();

            questions = data.questions || [];
            sentForms = data.sent || 0;
            filledForms = data.filled || 0;

            filledPercentage = sentForms > 0 ? Math.round((filledForms / sentForms) * 100) : 0;
        } catch (error) {
            console.error('Error fetching data:', error);
        }
    };

    onMount(fetchData);

    const getQuestionUrl = (id) => `/admin/question/?id=${id}`;

    const toggleVersionFilter = (event) => {
        event.stopPropagation();
        showVersionFilter = !showVersionFilter;
    };

    const selectVersion = (version) => {
        selectedVersion = version;
        showVersionFilter = false;
    };

    const handleClickOutside = (event) => {
        if (showVersionFilter && !event.target.closest('.version-filter-popup') && !event.target.closest('th')) {
            showVersionFilter = false;
        }
    };

    onMount(() => {
        document.addEventListener('click', handleClickOutside);
    });

    onDestroy(() => {
        document.removeEventListener('click', handleClickOutside);
    });

</script>

<main>
    <h1>Pytania i odpowiedzi</h1>

    <div>
        <p>Liczba wysłanych formularzy: {sentForms}</p>
        <p>Liczba uzupełnionych: {filledForms} | {filledPercentage}%</p>
    </div>

    <table>
        <thead>
            <tr>
                <th on:click={toggleVersionFilter} style="cursor: pointer; position: relative;">
                    Wersja zestawu
                    {#if showVersionFilter}
                        <div class="version-filter-popup">
                            <ul>
                                <li on:click={(event) => { event.stopPropagation(); selectVersion(''); }}>Wszystkie</li>
                                {#each Array.from(new Set(questions.map(q => q.version))) as version}
                                    <li on:click={(event) => { event.stopPropagation(); selectVersion(version); }}>{version}</li>
                                {/each}
                            </ul>
                        </div>
                    {/if}
                </th>
                <th>Tekst</th>
                <th>Typ odpowiedzi</th>
                <th>Akcja</th>
            </tr>
        </thead>
        <tbody>
            {#each questions.filter(q => selectedVersion === '' || q.version == selectedVersion) as question}
                <tr>
                    <td>{question.version}</td>
                    <td>{question.questionText}</td>
                    <td>{question.questionType}</td>
                    <td>
                        <Link to={getQuestionUrl(question.questionID)}>
                            Zobacz odpowiedzi
                        </Link>
                    </td>
                </tr>
            {/each}
        </tbody>
    </table>
</main>
