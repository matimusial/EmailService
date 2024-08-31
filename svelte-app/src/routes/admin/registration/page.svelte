<script>
    import { backendBaseUrl } from '../../../store';

    let username = '';
    let email = '';
    let password = '';
    let conPassword = '';
    let firstName = '';
    let errors = {};
    let registrationSuccess = false;
    let isSubmitting = false;

    const registration = async () => {
        isSubmitting = true;
        errors = {};

        const admin = { username, email, password, conPassword, firstName };
        try {
            const response = await fetch(`${backendBaseUrl}/api/admin/registration`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(admin)
            });

            if (!response.ok) {
                errors = await response.json();
                isSubmitting = false;
                if (response.status === 500) {
                    alert('Błąd serwera. Spróbuj ponownie później');
                }
            } else {
                errors = {};
                registrationSuccess = true;
            }
        } catch (error) {
            console.error('Error registering admin :', error);
            alert('Błąd połączenia. Spróbuj ponownie później');
            isSubmitting = false;
        }
    };
</script>

<main>
    <h1>Rejestracja</h1>

    {#if registrationSuccess}
        <div class="success">
            <p>Rejestracja pomyślnie ukończona! Kliknij w aktywacyjny link wysłany na Twój email, aby potwierdzić konto.
                Link jest ważny 24 godziny. Bez aktywacji nie będziesz mógł(a) zalogować się do systemu.</p>
        </div>
    {:else}
        <div class="container">
            <form class="form" on:submit|preventDefault={registration}>
                <div>
                    <label for="firstName">Imię:</label>
                    <input type="text" id="firstName" bind:value={firstName} required maxlength="50"/>
                    {#if errors.firstName}
                        <div class="error">{errors.firstName}</div>
                    {/if}
                </div>
                <br/>

                <div>
                    <label for="username">Nazwa użytkownika:</label>
                    <input type="text" id="username" bind:value={username} required maxlength="20"/>
                    {#if errors.username}
                        <div class="error">{errors.username}</div>
                    {/if}
                </div>
                <br/>

                <div>
                    <label for="email">Email:</label>
                    <input type="email" id="email" bind:value={email} required/>
                    {#if errors.email}
                        <div class="error">{errors.email}</div>
                    {/if}
                </div>
                <br/>

                <div>
                    <label for="password">Hasło:</label>
                    <input type="password" id="password" bind:value={password} required maxlength="20"/>
                    {#if errors.password}
                        <div class="error">{errors.password}</div>
                    {/if}
                </div>
                <br/>

                <div>
                    <label for="conPassword">Potwierdź hasło:</label>
                    <input type="password" id="conPassword" bind:value={conPassword} required maxlength="20"/>
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
                <br/>

                <button type="submit" disabled={isSubmitting}>Zarejestruj się</button>
            </form>

            {#if errors.general}
                <div class="error">{errors.general}</div>
            {/if}
        </div>
    {/if}
</main>

<style>
    .error {
        color: red;
        text-align: center;
    }

    .success {
        color: green;
        text-align: center;
    }

    button[disabled] {
        background-color: #ccc;
        cursor: not-allowed;
    }
</style>
