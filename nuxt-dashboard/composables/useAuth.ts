export const useAuth = () => {
    // Only access store on client-side to avoid SSR issues
    if (process.server) {
        // Return a minimal client on server-side
        return {
            user: computed(() => null),
            isAuthenticated: computed(() => false),
            isAdmin: computed(() => false),
            isOperator: computed(() => false),
            isViewer: computed(() => false),
            token: computed(() => null),
            login: async () => ({
                success: false,
                error: "Server-side not supported",
            }),
            logout: async () => {},
            requireAuth: () => false,
            requireRole: () => false,
        };
    }

    const authStore = useAuthStore();
    const router = useRouter();

    // Load auth from storage on client side
    if (!authStore.isAuthenticated) {
        authStore.loadFromStorage();
    }

    const login = async (username: string, password: string) => {
        try {
            await authStore.login(username, password);
            return { success: true };
        } catch (error: any) {
            return {
                success: false,
                error: error.message || "Login failed",
            };
        }
    };

    const logout = async () => {
        await authStore.logout();
        await router.push("/login");
    };

    const requireAuth = () => {
        if (!authStore.isAuthenticated) {
            router.push("/login");
            return false;
        }
        return true;
    };

    const requireRole = (role: "admin" | "operator" | "viewer") => {
        if (!authStore.isAuthenticated) {
            router.push("/login");
            return false;
        }

        const roleHierarchy = {
            admin: ["admin", "operator", "viewer"],
            operator: ["operator", "viewer"],
            viewer: ["viewer"],
        };

        const userRole = authStore.user?.role;
        if (!userRole || !roleHierarchy[userRole].includes(role)) {
            router.push("/dashboard");
            return false;
        }

        return true;
    };

    return {
        user: computed(() => authStore.user),
        isAuthenticated: computed(() => authStore.isAuthenticated),
        isAdmin: computed(() => authStore.isAdmin),
        isOperator: computed(() => authStore.isOperator),
        isViewer: computed(() => authStore.isViewer),
        token: computed(() => authStore.token),
        login,
        logout,
        requireAuth,
        requireRole,
    };
};
