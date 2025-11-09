export default defineNuxtPlugin(() => {
    // Initialize auth store on client-side
    const authStore = useAuthStore();

    // Load auth from localStorage on app initialization
    if (process.client) {
        authStore.loadFromStorage();
    }
});
