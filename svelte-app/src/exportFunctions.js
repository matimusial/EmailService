import { user, backendBaseUrl } from './store.js';

export async function getCurrentUser() {
    try {
        const response = await fetch(`${backendBaseUrl}/api/admin/me`, {
            method: 'GET',
            credentials: 'include'
        });

        if (response.ok) {
            const data = await response.text();
            if (data != 'anonymousUser'){
                user.set(data);
            } 
        } else {
            user.set(null);
        }
    } catch (error) {
        console.error('Error fetching user:', error);
        user.set(null);
    }
}
