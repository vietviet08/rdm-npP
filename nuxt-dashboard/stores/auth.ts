import { defineStore } from "pinia";

export interface User {
    id: number;
    username: string;
    email: string;
    role: "admin" | "operator" | "viewer";
    isActive: boolean;
    createdAt: string;
    updatedAt: string;
    lastLogin: string | null;
}

export interface LoginResponse {
    token: string;
    type: string;
    user: User;
}

export const useAuthStore = defineStore("auth", {
    state: () => ({
        user: null as User | null,
        token: null as string | null,
        isAuthenticated: false,
    }),

    getters: {
        isAdmin: (state) => state.user?.role === "admin",
        isOperator: (state) => state.user?.role === "operator",
        isViewer: (state) => state.user?.role === "viewer",
    },

    actions: {
        setAuth(token: string, user: User) {
            this.token = token;
            this.user = user;
            this.isAuthenticated = true;

            // Store in localStorage for persistence
            if (process.client) {
                localStorage.setItem("auth_token", token);
                localStorage.setItem("auth_user", JSON.stringify(user));
            }
        },

        clearAuth() {
            this.token = null;
            this.user = null;
            this.isAuthenticated = false;

            // Clear localStorage
            if (process.client) {
                localStorage.removeItem("auth_token");
                localStorage.removeItem("auth_user");
            }
        },

        loadFromStorage() {
            if (process.client) {
                const token = localStorage.getItem("auth_token");
                const userStr = localStorage.getItem("auth_user");

                if (token && userStr) {
                    try {
                        const user = JSON.parse(userStr);
                        this.setAuth(token, user);
                    } catch (e) {
                        console.error("Failed to parse stored user data", e);
                        this.clearAuth();
                    }
                }
            }
        },

        async login(username: string, password: string) {
            const api = useApi();

            try {
                const response = await api.post<LoginResponse>(
                    "/auth/login",
                    {
                        username,
                        password,
                    },
                    { requireAuth: false }
                );

                this.setAuth(response.token, response.user);
                return response;
            } catch (error: any) {
                console.error("Login failed:", error);
                throw error;
            }
        },

        async logout() {
            const api = useApi();

            try {
                await api.post("/auth/logout");
            } catch (error) {
                console.error("Logout failed:", error);
            } finally {
                this.clearAuth();
            }
        },

        async fetchCurrentUser() {
            const api = useApi();

            try {
                const user = await api.get<User>("/auth/me");
                if (this.token) {
                    this.user = user;
                    if (process.client) {
                        localStorage.setItem("auth_user", JSON.stringify(user));
                    }
                }
                return user;
            } catch (error) {
                console.error("Failed to fetch current user:", error);
                this.clearAuth();
                throw error;
            }
        },
    },
});
