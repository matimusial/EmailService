<script>
    import { writable } from "svelte/store";
    import { backendBaseUrl } from '../../../../store';

    let email = '';
    let errorMessage = writable('');
    let successMessage = writable('');
    let formVisible = writable(true);
    let isSubmitting = writable(false);

    const sendResetLink = async () => {
        errorMessage.set('');
        successMessage.set('');
        isSubmitting.set(true);

        try {
            const response = await fetch(`${backendBaseUrl}/api/admin/forgot-password`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({email})
            });

            const data = await response.text();

            if (!response.ok) {
                errorMessage.set(data);
                isSubmitting.set(false);
            } else {
                formVisible.set(false);
                successMessage.set(data);
            }
        } catch (error) {
            console.error('Error submitting form:', error);
            alert('Wystąpił błąd. Spróbuj ponownie później');
            isSubmitting.set(false);
        }
    }
</script>


<main>
    <h1>Zapomniałeś hasła?</h1>

    <div class="container">
        {#if $formVisible}
            <form class="form" on:submit|preventDefault={sendResetLink}>
                <div>
                    <label for="email">Podaj swój adres e-mail:</label>
                    <input type="email" id="email" bind:value={email} required/>
                </div>
                <br/>

                <button type="submit" disabled={$isSubmitting}>Wyślij link resetujący</button>

                {#if $errorMessage}
                    <div class="error">{$errorMessage}</div>
                {/if}
            </form>
        {:else}
            <div class="success">{$successMessage}</div>
        {/if}
    </div>
</main>

<style>
    button[disabled] {
        background-color: #ccc;
        cursor: not-allowed;
    }

    .error {
        color: red;
        margin-top: 2%;
        text-align: center;
    }

    .success {
        color: green;
        margin-top: 2%;
        text-align: center;
    }
</style>
