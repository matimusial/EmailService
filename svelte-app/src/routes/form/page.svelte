<script>
    import { onMount } from 'svelte';
    import StarRating from './StarRating.svelte';
    import { backendBaseUrl } from '../../store';

    let username = '';
    let reportNumber = '';
    let topic = '';
    let technician = '';
    let questions = [];
    let answers = {};
    let statusMessage = '';
    let hash = '';

    const fetchFormData = async () => {
        try {
            const response = await fetch(`${backendBaseUrl}/api/form/${hash}`, {
                method: 'GET',
            });
            const data = await response.json();

            if (!response.ok) {
                statusMessage = data.message;
            }
            else if (![200, 404, 409].includes(response.status)) {
                throw new Error('Network response was not ok');
            }
            else {
                reportNumber = data.incidentNumber;
                topic = data.topic;
                technician = data.technician;
                username = data.firstName;
                questions = data.questions;
            }

        } catch (error) {
            console.error('Error fetching form data:', error);
            statusMessage = 'Błąd podczas pobierania danych';
        }
    };

    onMount(async () => {
        const urlParams = new URLSearchParams(window.location.search);
        hash = urlParams.get('hash');

        if (hash) {
            await fetchFormData();
        } else {
            statusMessage = 'Błędny adres url';
        }
    });

    const validateForm = () => {
        for (let question of questions) {
            if (!answers[question.id]) {
                alert('Proszę odpowiedzieć na wszystkie pytania');
                return false;
            }
        }
        return true;
    };

    const handleSubmit = async () => {
        if (!validateForm()) return;

        try {
            const response = await fetch(`${backendBaseUrl}/api/form/submit`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    hash: hash,
                    answers: answers
                })
            }); 

            if (!response.ok) {
                statusMessage = 'Błąd podczas przesyłania formularza'
                throw new Error('Network response was not ok');
            }
            else{
                statusMessage = await response.text();
            }

        } catch (error) {
            console.error('Error submitting form:', error);
            statusMessage = 'Błąd podczas przesyłania formularza';
        }
    };
</script>

<main>
    <div style="text-align:center;">
        <img src="btc_big.png" alt="btc logo" />
        <h1>Formularz oceny</h1>
        
    </div>
    
    <div class="centered hidden" class:hidden={statusMessage}>
        
        <h2>{username}!</h2>
    </div>
    <p class="centered hidden" class:hidden={statusMessage}>Dziękujemy za poświęcenie czasu na wypełnienie formularza. Twoja opinia jest dla nas bardzo ważna.</p>
    
    {#if statusMessage}
        <p class="centered">{statusMessage}</p>
    {:else}
        <table>
            <thead>
                <tr>
                    <th>Nr zgłoszenia</th>
                    <th>Temat</th>
                    <th>Serwisant</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td><p>{reportNumber}</p></td>
                    <td><p>{topic}</p></td>
                    <td><p>{technician}</p></td>
                </tr>
            </tbody>
        </table>

        {#each questions as question}
        <div class="question-block">
            <p>{question.text}</p>
            {#if question.type === 'STARS'}
                <StarRating bind:rating={answers[question.id]} ratingId={question.id} />
            {:else if question.type === 'RADIO'}
                <div class="radio-group">
                    <input type="radio" id={`timelinessYes-${question.id}`} name={`question-${question.id}`} value="true" bind:group={answers[question.id]} />
                    <label for={`timelinessYes-${question.id}`}>Tak</label>
                    <input type="radio" id={`timelinessNo-${question.id}`} name={`question-${question.id}`} value="false" bind:group={answers[question.id]} />
                    <label for={`timelinessNo-${question.id}`}>Nie</label>
                </div>
            {/if}
        </div>
        {/each}
        
        <button on:click={handleSubmit} aria-label="Wyślij formularz">Wyślij</button>
    {/if}
</main>

<style>
    img{
        margin-bottom: 0;
    }
    .hidden { 
        display: none; 
    }
    
    .centered { 
        text-align: center; 
    }

    .radio-group {
        display: flex;
        flex-direction: row;
        justify-content: flex-start;
        margin-bottom: 1em;
        align-items: center;
    }

    main {
        margin-top: 2%;
    }
</style>
