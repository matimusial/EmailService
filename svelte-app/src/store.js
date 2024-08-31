import { writable } from 'svelte/store';

export const user = writable(null);
export const backendBaseUrl = 'http://localhost:8081';
