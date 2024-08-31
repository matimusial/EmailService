<script>
    import { onMount } from 'svelte';
    import { backendBaseUrl } from '../../../store';

    let ehelpdesks = [];
    let newEhelpdesk = '';

    const fetchEhelpdesks = async () => {
        try {
            const response = await fetch(`${backendBaseUrl}/api/admin/ehelpdesks`, {
                method: 'GET',
                credentials: 'include'
            });
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            ehelpdesks = await response.json();
        } catch (error) {
            console.error('Error fetching ehelpdesks:', error);
        }
    };

    onMount(fetchEhelpdesks);

    const addEhelpdesk = async () => {
        if (newEhelpdesk.trim() === '') {
            alert('Proszę podać nazwę eHelpdesku');
            return;
        }

        const confirmed = confirm(`Czy na pewno chcesz dodać "${newEhelpdesk}" jako nowy eHelpdesk do bazy?`);
        if (!confirmed) {
            return;
        }

        try {
            const response = await fetch(`${backendBaseUrl}/api/admin/submitEhelpdesk`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                credentials: 'include',
                body: JSON.stringify({ name: newEhelpdesk })
            });

            if (!response.ok) {
                if (response.status === 409) {
                    alert('Ehelpdesk o tej nazwie już istnieje. Wybierz inną nazwę');
                } else {
                    alert(`Wystąpił nieoczekiwany błąd: ${response.status}`);
                    throw new Error('Network response was not ok');
                }
            } else {
                await fetchEhelpdesks();
                newEhelpdesk = '';
            }
        } catch (error) {
            console.error('Error adding ehelpdesk:', error);
            alert('Wystąpił problem podczas dodawania eHelpdesk. Spróbuj ponownie później');
        }
    };
</script>

<main>
    <h1>eHelpDesk</h1>

    <div class="container">
        <form class="form">
            <div>
                <label for="newEhelpdesk">Nowy eHelpDesk:</label>
                <input type="text" id="newEhelpdesk" bind:value={newEhelpdesk} />
            </div>
            <button type="button" on:click={addEhelpdesk}>Dodaj</button>
        </form>

        <table>
            <thead>
            <tr>
                <th>ID</th>
                <th>Nazwa</th>
            </tr>
            </thead>
            <tbody>
            {#each ehelpdesks as ehelpdesk}
                <tr>
                    <td>{ehelpdesk.id}</td>
                    <td>{ehelpdesk.name}</td>
                </tr>
            {/each}
            </tbody>
        </table>
    </div>
</main>

<style>
    .container {
        display: flex;
        flex-direction: column;
        gap: 20px;
        align-items: center;
    }

    .form {
        width: 50%;
    }

    table {
        width: 80%;
    }
</style>
