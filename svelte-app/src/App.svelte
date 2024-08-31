<script>
    import { Router, Route, Link, navigate } from 'svelte-routing';
    import { onMount } from 'svelte';
    import { user, backendBaseUrl } from './store';
    import { getCurrentUser } from './exportFunctions';
    import { get } from 'svelte/store';

    import ehelpdesk from './routes/admin/ehelpdesk/page.svelte';
    import addquestions from './routes/admin/addquestions/page.svelte';
    import questionsandanswers from './routes/admin/questionsandanswers/page.svelte';
    import question from './routes/admin/question/page.svelte';
    import info from './routes/admin/info/page.svelte';

    import admin from './routes/admin/page.svelte';
    import form from './routes/form/page.svelte';
    import login from './routes/admin/login/page.svelte';
    import registration from './routes/admin/registration/page.svelte';
    import forgotpassword from './routes/admin/login/forgot-password/page.svelte';
    import resetpassword from './routes/admin/login/reset-password/page.svelte';
    import authorizeregistration from './routes/admin/registration/authorize-registration/page.svelte';

    const securedRoutes = {
        '/admin/ehelpdesk': ehelpdesk,
        '/admin/addquestions': addquestions,
        '/admin/questionsandanswers': questionsandanswers,
        '/admin/question': question,
        '/admin/info': info,
    };

    const freeRoutes = {
        '/admin': admin,
        '/form': form,
        '/admin/login': login,
        '/admin/registration': registration,
        '/admin/login/forgot-password': forgotpassword,
        '/admin/login/reset-password': resetpassword,
        '/admin/registration/authorize-registration': authorizeregistration,
    };

    const routes = {...securedRoutes, ...freeRoutes};

    let currentRoute = window.location.pathname;
    let shouldDisplayNav = currentRoute !== '/form' && Object.keys(routes).includes(currentRoute);


    onMount(async () => {
        window.addEventListener('popstate', handleRouteChange);
        await getCurrentUser();
        await handleRouteChange();

        return () => {
            window.removeEventListener('popstate', handleRouteChange);
        };
    });

    async function handleLogout(event) {
        event.preventDefault();
        try {
            const response = await fetch(`${backendBaseUrl}/api/admin/logout`, {
                method: 'POST',
                credentials: 'include'
            });

            if (response.ok) {
                user.set(null);
                navigate('/admin/login');
            } else {
                alert('Wylogowanie nie powiodło się');
            }
        } catch (error) {
            alert('Wylogowanie nie powiodło się: ' + error.message);
        }
    }

    async function handleDeleteAccount(event) {
        event.preventDefault();
        const confirmDelete = confirm("Czy na pewno chcesz usunąć swoje konto? Tego działania nie można cofnąć");

        if (confirmDelete) {
            try {
                const response = await fetch(`${backendBaseUrl}/api/admin/deleteAccount`, {
                    method: 'DELETE',
                    credentials: 'include'
                });

                if (response.ok) {
                    user.set(null);
                    alert('Twoje konto zostało pomyślnie usunięte');
                    await handleLogout(event);
                } else {
                    alert('Usunięcie konta nie powiodło się. Spróbuj ponownie później');
                }
            } catch (error) {
                alert('Usunięcie konta nie powiodło się: ' + error.message);
            }
        }
    }

    const handleRouteChange = async () => {
        currentRoute = window.location.pathname;
        await getCurrentUser();
        const currentUser = get(user);

        if (currentRoute === '/form') {
            return;
        }
        
        if (currentUser == null) {
            if (Object.keys(securedRoutes).includes(currentRoute)) {
                navigate('/admin/login');
            }
        } else {
            if (Object.keys(freeRoutes).includes(currentRoute)) {
                navigate('/admin');
            }
        }
    };
</script>

<Router>
    {#if shouldDisplayNav}
        <nav>
            {#if $user}
                <Link to="/admin/questionsandanswers">Pytania i odpowiedzi</Link>
                <Link to="/admin/addquestions">Dodaj pytania</Link>
                <Link to="/admin/ehelpdesk">eHelpDesk</Link>
                <Link to="/admin/info">Info</Link>
                <Link on:click={handleLogout} class="logout-button">Wyloguj</Link>
                <Link on:click={handleDeleteAccount} class="delete-account-button">Usuń konto</Link>
            {:else}
                <Link to="/admin/login">Zaloguj</Link>
                <Link to="/admin/registration">Zarejestruj</Link>
            {/if}
        </nav>
    {/if}

    <div>
        {#each Object.entries(routes) as [path, component]}
            <Route path={path} component={component}/>
        {/each}
    </div>
</Router>
