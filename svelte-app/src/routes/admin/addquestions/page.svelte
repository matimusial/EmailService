<script>
    import { onMount } from 'svelte';
    import { backendBaseUrl } from '../../../store';

    let newQuestions = [];
    let numberOfQuestionsToAdd = 0;
    let latestVersion = 0;

    const fetchLatestVersion = async () => {
        try {
            const response = await fetch(`${backendBaseUrl}/api/admin/questions`, {
                method: 'GET',
                credentials: 'include'
            });
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }

            const data = await response.json();
            const questions = data.questions;

            latestVersion = Math.max(...questions.map(q => q.version), 0);
        } catch (error) {
            console.error('Error fetching data:', error);
        }
    };

    onMount(fetchLatestVersion);

    const addQuestions = async () => {
        if (!validateQuestions()) {
            return;
        }

        if (!confirm('Czy na pewno chcesz dodać nowy zestaw pytań?')) {
            return;
        }

        try {
            const newVersion = latestVersion + 1;
            const questionsWithVersion = newQuestions.map(q => ({...q, version: newVersion}));

            const response = await fetch(`${backendBaseUrl}/api/admin/submitQuestions`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                credentials: 'include',
                body: JSON.stringify({questions: questionsWithVersion})
            });

            if (!response.ok) {
                if (response.status === 409) {
                    alert('Wystąpił konflikt przy zapisie pytań. Spróbuj ponownie.');
                } else {
                    alert(`Wystąpił nieoczekiwany błąd: ${response.status}`);
                    throw new Error('Network response was not ok');
                }
            } else {
                alert('Pytania zostały pomyślnie dodane!');
                await fetchLatestVersion();
                newQuestions = [];
                latestVersion = newVersion;
            }

        } catch (error) {
            console.error('Error adding questions:', error);
            alert('Wystąpił problem podczas dodawania pytań. Spróbuj ponownie później');
        }
    };

    const isQuestionValid = (questionText) => {
        const validEndings = ['?', '!', '.', ';', ':'];
        const lastChar = questionText.trim().slice(-1);
        return validEndings.includes(lastChar);
    };

    const hasDuplicateQuestions = (questionsArray) => {
        const seenQuestions = new Set();
        for (const question of questionsArray) {
            const normalizedQuestion = question.questionText.trim().toLowerCase();
            if (seenQuestions.has(normalizedQuestion)) {
                return true;
            }
            seenQuestions.add(normalizedQuestion);
        }
        return false;
    };

    const validateQuestions = () => {
        if (newQuestions.length === 0) {
            alert('Proszę dodać przynajmniej jedno pytanie.');
            return false;
        }

        for (let i = 0; i < newQuestions.length; i++) {
            if (!isQuestionValid(newQuestions[i].questionText)) {
                alert(`Pytanie ${i + 1} nie kończy się odpowiednim znakiem interpunkcyjnym.`);
                return false;
            }
        }

        if (hasDuplicateQuestions(newQuestions)) {
            alert('W zestawie znajdują się identyczne pytania. Proszę je zmienić.');
            return false;
        }
        return true;
    };

    const handleNumberOfQuestionsChange = (event) => {
        numberOfQuestionsToAdd = parseInt(event.target.value, 10);
        newQuestions = Array.from({length: numberOfQuestionsToAdd}, () => ({
            version: '',
            questionText: '',
            questionType: 'STARS'
        }));
    };
</script>


<main>
    <h1>Dodawanie pytań</h1>

    <div>
        <form class="wide-form">
            <div>
                <label for="numberOfQuestions">Ilość nowych pytań:</label>
                <input type="number" id="numberOfQuestions" min="1" bind:value={numberOfQuestionsToAdd}
                       on:input={handleNumberOfQuestionsChange}/>
            </div>
            {#each newQuestions as newQuestion, index}
                <div>
                    <label>Treść Pytania {index + 1}:</label>
                    <input type="text" bind:value={newQuestion.questionText} placeholder="Wpisz treść pytania"/>

                    <label>Typ Odpowiedzi {index + 1}:</label>
                    <select bind:value={newQuestion.questionType}>
                        <option value="STARS">STARS</option>
                        <option value="RADIO">RADIO</option>
                    </select>
                </div>
            {/each}
            <button type="button" on:click={addQuestions}>Dodaj</button>
        </form>
    </div>
</main>

<style>
    .wide-form {
        width: 100%;
        max-width: 800px;
        margin-bottom: 20px;
    }
</style>
