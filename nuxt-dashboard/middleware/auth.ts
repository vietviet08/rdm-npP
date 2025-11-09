export default defineNuxtRouteMiddleware((to, from) => {
    // On server-side, allow all routes to be rendered
    // Client-side middleware will handle redirects
    if (process.server) {
        return;
    }

    // Client-side authentication checks only

    // Public routes that don't require authentication
    const publicRoutes = ["/login"];
    const isPublicRoute = publicRoutes.includes(to.path) || to.path === "/";

    // Get auth store (should be initialized by plugin)
    let authStore;
    try {
        authStore = useAuthStore();
    } catch (error) {
        // If store is not available, allow public routes only
        if (isPublicRoute) {
            return;
        }
        // For protected routes, redirect to login
        return navigateTo("/login");
    }

    // Ensure auth is loaded from storage
    if (!authStore.isAuthenticated) {
        authStore.loadFromStorage();
    }

    const isAuthenticated = authStore.isAuthenticated;

    // Handle root path redirect
    if (to.path === "/") {
        if (isAuthenticated) {
            return navigateTo("/dashboard");
        } else {
            return navigateTo("/login");
        }
    }

    // If accessing login page while authenticated, redirect to dashboard
    if (to.path === "/login" && isAuthenticated) {
        return navigateTo("/dashboard");
    }

    // Allow public routes
    if (isPublicRoute) {
        return;
    }

    // For protected routes, require authentication
    if (!isAuthenticated) {
        return navigateTo("/login");
    }
});
