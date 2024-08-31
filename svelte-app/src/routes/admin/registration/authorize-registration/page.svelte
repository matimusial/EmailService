<script>
    import { onMount } from 'svelte';
    import { Link } from 'svelte-routing';
    import { backendBaseUrl } from '../../../../store';

    let pincode = '';
    let statusMessage = '';
    let showLoginLink = false;

    const authorizeRegistration = async () => {
        try {
            const response = await fetch(`${backendBaseUrl}/api/admin/authorize-registration/${pincode}`, {
                method: 'GET',
            });
            const data = await response.text();

            if (response.ok) {
                showLoginLink = true;
            }
            statusMessage = data;
        } catch (error) {
            console.error('Error fetching data:', error);
        }
    };

    onMount(() => {
        const urlParams = new URLSearchParams(window.location.search);
        pincode = urlParams.get('pincode');

        if (pincode) {
            authorizeRegistration();
        } else {
            statusMessage = 'Brak pincode w URL';
        }
    });
</script>

<main>
    <div class="message">
        <p>{statusMessage}</p>
        <div class="links">
            {#if showLoginLink}
                <Link to="/admin/login">Przejdź do logowania</Link>
            {:else}
                <Link to="/admin/registration">Zarejestruj się ponownie</Link>
            {/if}
        </div>
    </div>
</main>

<style>
    p {
        text-align: center;
    }
    .links {
        text-align: center;
        margin-top: 20px;
    }
</style>
