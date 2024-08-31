<script>
    import { navigate } from 'svelte-routing';
    import { writable } from 'svelte/store';
    import { user } from '../../../store';
    import { getCurrentUser } from '../../../exportFunctions';
    import { Link } from 'svelte-routing';
    import { onMount } from 'svelte';

    import { backendBaseUrl } from '../../../store';

    onMount(async () => {
        await getCurrentUser();
    });

    let username = '';
    let password = '';
    let errorMessage = writable('');

    const login = async () => {
        try {
            const response = await fetch(`${backendBaseUrl}/api/admin/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: new URLSearchParams({username, password}),
                credentials: 'include'
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Logowanie nieudane');
            }

            user.set({username});
            navigate('/admin/questionsandanswers');
        } catch (error) {
            errorMessage.set(error.message);
        }
    }

</script>

<main>
    <h1>Logowanie</h1>

    <div class="container">

        <form class="form" on:submit|preventDefault={login}>
            <div>
                <label for="username">Nazwa użytkownika:</label>
                <input type="text" id="username" bind:value={username} required/>
            </div>
            <br/>

            <div>
                <label for="password">Hasło:</label>
                <input type="password" id="password" bind:value={password} required/>
            </div>
            <br/>

            <div class="password-toggle">
                <input type="checkbox" id="showPassword" on:click={() => {
                    const passwordField = document.getElementById('password');
                    passwordField.type = passwordField.type === 'password' ? 'text' : 'password';
                }}>
                <label for="showPassword">Pokaż hasło</label>
            </div>

            {#if $errorMessage}
                <div class="error">{$errorMessage}</div>
            {/if}
            <br/>

            <button type="submit">Zaloguj się</button>
        </form>

        <div class="links">
            <p>Nie masz konta?
                <Link to="/admin/registration">Zarejestruj się</Link>
            </p>
            <p>
                <Link to="/admin/login/forgot-password">Zapomniałeś hasła?</Link>
            </p>
        </div>
    </div>
</main>
