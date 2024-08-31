<script>
    import { navigate } from 'svelte-routing';
    import { onMount } from 'svelte';
    import { backendBaseUrl } from '../../../../store';

    let password = '';
    let conPassword = '';
    let errors = {};
    let isSubmitting = false;
    let statusMessage = '';

    let pincode = '';
    let email = '';

    const verifyReset = async () => {

        const urlParams = new URLSearchParams(window.location.search);
        pincode = urlParams.get('pincode');
        email = urlParams.get('email');

        if (!pincode || !email) {
            statusMessage = "Błędny adres URL";
            return;
        }

        try {
            const response = await fetch(`${backendBaseUrl}/api/admin/verify-reset/${pincode}/${email}`, {
                method: 'GET',
            });

            if (!response.ok) {
                statusMessage = await response.text();
            }
        } catch (error) {
            statusMessage = "Wystąpił błąd połączenia. Spróbuj ponownie później";
        }
    }


    onMount(verifyReset);

    const resetPassword = async () => {
        isSubmitting = true;
        errors = {};

        console.log(`${backendBaseUrl}/api/admin/reset-password/${pincode}/${email}`);

        try {
            const response = await fetch(`${backendBaseUrl}/api/admin/reset-password/${pincode}/${email}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json' 
                },
                body: JSON.stringify({
                    password: password,
                    conPassword: conPassword
                })
            });

            if (!response.ok) {
                errors = await response.json();
                isSubmitting = false;
            } else {
                errors = {};
                navigate('/admin/login');
            }
        } catch (error) {
            alert('Błąd połączenia. Spróbuj ponownie później');
            isSubmitting = false;
        }
    }
</script>

<main>
    <h1>Ustaw nowe hasło</h1>

    {#if statusMessage}
        <div class="status-message">{statusMessage}</div>
    {:else}
        <div class="container">
            <form class="form" on:submit|preventDefault={resetPassword}>
                <div>
                    <label for="password">Nowe hasło:</label>
                    <input type="password" id="password" bind:value={password} required />
                    {#if errors.password}
                        <div class="error">{errors.password}</div>
                    {/if}
                </div>
                <br/>

                <div>
                    <label for="conPassword">Potwierdź nowe hasło:</label>
                    <input type="password" id="conPassword" bind:value={conPassword} required />
                    {#if errors.conPassword}
                        <div class="error">{errors.conPassword}</div>
                    {/if}
                </div>
                <br/>
                <div class="password-toggle">
                    <input type="checkbox" id="showPassword" on:click={() => {
                        const passwordField = document.getElementById('password');
                        const conPasswordField = document.getElementById('conPassword');
                        passwordField.type = passwordField.type === 'password' ? 'text' : 'password';
                        conPasswordField.type = conPasswordField.type === 'password' ? 'text' : 'password';
                    }}>
                    <label for="showPassword">Pokaż hasło</label>
                </div>

                <button type="submit" disabled={isSubmitting}>Zresetuj hasło</button>

                {#if errors.general}
                    <div class="error">{errors.general}</div>
                {/if}
            </form>
        </div>
    {/if}
</main>

<style>
    .error {
        color: red;
        text-align: center;
    }

    .status-message {
        text-align: center;
        color: green;
    }
</style>
