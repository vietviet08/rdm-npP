export const useApi = () => {
    const config = useRuntimeConfig();

    const apiUrl = config.public.apiUrl || "/api";

    const apiFetch = async <T>(
        endpoint: string,
        options: {
            method?: string;
            body?: any;
            headers?: Record<string, string>;
            requireAuth?: boolean;
        } = {}
    ): Promise<T> => {
        const {
            method = "GET",
            body,
            headers = {},
            requireAuth = true,
        } = options;

        const requestHeaders: Record<string, string> = {
            "Content-Type": "application/json",
            ...headers,
        };

        // Add auth token if required and available
        if (requireAuth && process.client) {
            try {
                const authStore = useAuthStore();
                if (authStore && authStore.token) {
                    requestHeaders[
                        "Authorization"
                    ] = `Bearer ${authStore.token}`;
                }
            } catch (e) {
                // Auth store not available
            }
        }

        try {
            const fullUrl = `${apiUrl}${endpoint}`;

            const response = await fetch(fullUrl, {
                method,
                headers: requestHeaders,
                body: body ? JSON.stringify(body) : undefined,
            });

            if (!response.ok) {
                const error = await response
                    .json()
                    .catch(() => ({ message: response.statusText }));
                throw new ApiError(
                    error.message || "Request failed",
                    response.status,
                    error
                );
            }

            // Handle empty responses
            const contentType = response.headers.get("content-type");
            if (contentType && contentType.includes("application/json")) {
                return await response.json();
            }

            return null as T;
        } catch (error) {
            if (error instanceof ApiError) {
                throw error;
            }

            // Handle network errors
            if (
                error instanceof TypeError &&
                error.message === "Failed to fetch"
            ) {
                throw new ApiError(
                    "Network error: Cannot connect to server",
                    0,
                    error
                );
            }

            throw new ApiError(
                error instanceof Error ? error.message : "Unknown error",
                0,
                error
            );
        }
    };

    return {
        apiUrl,
        get: <T>(
            endpoint: string,
            options?: Omit<Parameters<typeof apiFetch>[1], "method">
        ) => apiFetch<T>(endpoint, { ...options, method: "GET" }),

        post: <T>(
            endpoint: string,
            body?: any,
            options?: Omit<Parameters<typeof apiFetch>[1], "method" | "body">
        ) => apiFetch<T>(endpoint, { ...options, method: "POST", body }),

        put: <T>(
            endpoint: string,
            body?: any,
            options?: Omit<Parameters<typeof apiFetch>[1], "method" | "body">
        ) => apiFetch<T>(endpoint, { ...options, method: "PUT", body }),

        delete: <T>(
            endpoint: string,
            options?: Omit<Parameters<typeof apiFetch>[1], "method">
        ) => apiFetch<T>(endpoint, { ...options, method: "DELETE" }),

        patch: <T>(
            endpoint: string,
            body?: any,
            options?: Omit<Parameters<typeof apiFetch>[1], "method" | "body">
        ) => apiFetch<T>(endpoint, { ...options, method: "PATCH", body }),
    };
};

export class ApiError extends Error {
    constructor(message: string, public status: number, public data?: any) {
        super(message);
        this.name = "ApiError";
    }
}
