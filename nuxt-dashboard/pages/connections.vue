<template>
    <div class="min-h-screen bg-gray-50">
        <!-- Navigation -->
        <nav class="bg-white shadow-sm">
            <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div class="flex justify-between h-16">
                    <div class="flex">
                        <div class="flex-shrink-0 flex items-center">
                            <h1 class="text-xl font-bold text-gray-900">
                                RDM Platform
                            </h1>
                        </div>
                        <div class="hidden sm:ml-6 sm:flex sm:space-x-8">
                            <NuxtLink
                                to="/dashboard"
                                class="border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700 inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium"
                            >
                                Dashboard
                            </NuxtLink>
                            <NuxtLink
                                to="/devices"
                                class="border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700 inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium"
                            >
                                Devices
                            </NuxtLink>
                            <NuxtLink
                                v-if="user && isAdmin"
                                to="/users"
                                class="border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700 inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium"
                            >
                                Users
                            </NuxtLink>
                            <NuxtLink
                                to="/connections"
                                class="border-indigo-500 text-gray-900 inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium"
                            >
                                Connections
                            </NuxtLink>
                        </div>
                    </div>
                    <div class="flex items-center">
                        <div class="flex-shrink-0" v-if="user">
                            <span class="text-sm text-gray-700 mr-4">{{
                                user.username
                            }}</span>
                            <span
                                class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-indigo-100 text-indigo-800"
                            >
                                {{ user.role }}
                            </span>
                        </div>
                        <button
                            v-if="user"
                            @click="handleLogout"
                            class="ml-4 inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700"
                        >
                            Logout
                        </button>
                    </div>
                </div>
            </div>
        </nav>

        <!-- Main Content -->
        <main class="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
            <div class="px-4 py-6 sm:px-0">
                <!-- Header -->
                <div class="mb-6">
                    <h2 class="text-2xl font-bold text-gray-900">
                        Connection History
                    </h2>
                    <p class="mt-1 text-sm text-gray-600">
                        View your connection history and logs
                    </p>
                </div>

                <!-- Filters -->
                <div class="bg-white shadow rounded-lg p-4 mb-6">
                    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                        <div>
                            <label
                                for="status-filter"
                                class="block text-sm font-medium text-gray-700 mb-1"
                            >
                                Status
                            </label>
                            <select
                                id="status-filter"
                                v-model="filters.status"
                                class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
                                @change="loadConnections"
                            >
                                <option value="">All</option>
                                <option value="success">Success</option>
                                <option value="failed">Failed</option>
                                <option value="timeout">Timeout</option>
                            </select>
                        </div>
                        <div>
                            <label
                                for="protocol-filter"
                                class="block text-sm font-medium text-gray-700 mb-1"
                            >
                                Protocol
                            </label>
                            <select
                                id="protocol-filter"
                                v-model="filters.protocol"
                                class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
                                @change="loadConnections"
                            >
                                <option value="">All</option>
                                <option value="rdp">RDP</option>
                                <option value="vnc">VNC</option>
                                <option value="ssh">SSH</option>
                            </select>
                        </div>
                        <div class="flex items-end">
                            <button
                                @click="resetFilters"
                                class="w-full px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50"
                            >
                                Reset Filters
                            </button>
                        </div>
                    </div>
                </div>

                <!-- Loading State -->
                <div v-if="loading" class="text-center py-12">
                    <div
                        class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600"
                    ></div>
                    <p class="mt-2 text-sm text-gray-600">
                        Loading connections...
                    </p>
                </div>

                <!-- Error State -->
                <div
                    v-else-if="error"
                    class="bg-red-50 border border-red-200 rounded-lg p-4 mb-6"
                >
                    <p class="text-sm text-red-800">{{ error }}</p>
                </div>

                <!-- Connections Table -->
                <div v-else class="bg-white shadow rounded-lg overflow-hidden">
                    <table class="min-w-full divide-y divide-gray-200">
                        <thead class="bg-gray-50">
                            <tr>
                                <th
                                    class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                                >
                                    Device
                                </th>
                                <th
                                    class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                                >
                                    Protocol
                                </th>
                                <th
                                    class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                                >
                                    Start Time
                                </th>
                                <th
                                    class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                                >
                                    Duration
                                </th>
                                <th
                                    class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                                >
                                    Status
                                </th>
                            </tr>
                        </thead>
                        <tbody class="bg-white divide-y divide-gray-200">
                            <tr
                                v-for="log in connections"
                                :key="log.id"
                                class="hover:bg-gray-50"
                            >
                                <td class="px-6 py-4 whitespace-nowrap">
                                    <div
                                        class="text-sm font-medium text-gray-900"
                                    >
                                        {{ log.deviceName || "Unknown Device" }}
                                    </div>
                                    <div
                                        v-if="log.deviceHost"
                                        class="text-sm text-gray-500"
                                    >
                                        {{ log.deviceHost }}
                                    </div>
                                </td>
                                <td class="px-6 py-4 whitespace-nowrap">
                                    <span
                                        v-if="log.protocol"
                                        class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
                                        :class="{
                                            'bg-blue-100 text-blue-800':
                                                log.protocol === 'rdp',
                                            'bg-green-100 text-green-800':
                                                log.protocol === 'vnc',
                                            'bg-purple-100 text-purple-800':
                                                log.protocol === 'ssh',
                                        }"
                                    >
                                        {{ log.protocol.toUpperCase() }}
                                    </span>
                                </td>
                                <td
                                    class="px-6 py-4 whitespace-nowrap text-sm text-gray-900"
                                >
                                    <div>
                                        {{ formatDate(log.connectionStart) }}
                                    </div>
                                    <div
                                        v-if="log.connectionEnd"
                                        class="text-xs text-gray-500"
                                    >
                                        Ended:
                                        {{ formatDate(log.connectionEnd) }}
                                    </div>
                                </td>
                                <td
                                    class="px-6 py-4 whitespace-nowrap text-sm text-gray-900"
                                >
                                    {{ formatDuration(log.duration) }}
                                    <div
                                        v-if="!log.connectionEnd"
                                        class="text-xs text-gray-500 italic"
                                    >
                                        Active
                                    </div>
                                </td>
                                <td class="px-6 py-4 whitespace-nowrap">
                                    <span
                                        class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
                                        :class="{
                                            'bg-green-100 text-green-800':
                                                log.status === 'success',
                                            'bg-red-100 text-red-800':
                                                log.status === 'failed',
                                            'bg-yellow-100 text-yellow-800':
                                                log.status === 'timeout',
                                        }"
                                    >
                                        {{ log.status }}
                                    </span>
                                </td>
                            </tr>
                            <tr v-if="connections.length === 0">
                                <td
                                    colspan="5"
                                    class="px-6 py-4 text-center text-sm text-gray-500"
                                >
                                    No connection history found
                                </td>
                            </tr>
                        </tbody>
                    </table>

                    <!-- Pagination -->
                    <div
                        v-if="totalPages > 1"
                        class="bg-white px-4 py-3 flex items-center justify-between border-t border-gray-200 sm:px-6"
                    >
                        <div class="flex-1 flex justify-between sm:hidden">
                            <button
                                @click="goToPage(currentPage - 1)"
                                :disabled="currentPage === 0"
                                class="relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                                Previous
                            </button>
                            <button
                                @click="goToPage(currentPage + 1)"
                                :disabled="currentPage >= totalPages - 1"
                                class="ml-3 relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                                Next
                            </button>
                        </div>
                        <div
                            class="hidden sm:flex-1 sm:flex sm:items-center sm:justify-between"
                        >
                            <div>
                                <p class="text-sm text-gray-700">
                                    Showing
                                    <span class="font-medium">{{
                                        currentPage * pageSize + 1
                                    }}</span>
                                    to
                                    <span class="font-medium">{{
                                        Math.min(
                                            (currentPage + 1) * pageSize,
                                            totalElements
                                        )
                                    }}</span>
                                    of
                                    <span class="font-medium">{{
                                        totalElements
                                    }}</span>
                                    results
                                </p>
                            </div>
                            <div>
                                <nav
                                    class="relative z-0 inline-flex rounded-md shadow-sm -space-x-px"
                                    aria-label="Pagination"
                                >
                                    <button
                                        @click="goToPage(currentPage - 1)"
                                        :disabled="currentPage === 0"
                                        class="relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                                    >
                                        Previous
                                    </button>
                                    <button
                                        v-for="page in visiblePages"
                                        :key="page"
                                        @click="goToPage(page)"
                                        :class="[
                                            'relative inline-flex items-center px-4 py-2 border text-sm font-medium',
                                            page === currentPage
                                                ? 'z-10 bg-indigo-50 border-indigo-500 text-indigo-600'
                                                : 'bg-white border-gray-300 text-gray-500 hover:bg-gray-50',
                                        ]"
                                    >
                                        {{ page + 1 }}
                                    </button>
                                    <button
                                        @click="goToPage(currentPage + 1)"
                                        :disabled="
                                            currentPage >= totalPages - 1
                                        "
                                        class="relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                                    >
                                        Next
                                    </button>
                                </nav>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </main>
    </div>
</template>

<script setup lang="ts">
import { useConnection } from "~/composables/useConnection";

definePageMeta({
    middleware: "auth",
});

const router = useRouter();
const { getConnectionLogs } = useConnection();

// State
const connections = ref<any[]>([]);
const loading = ref(false);
const error = ref<string | null>(null);
const currentPage = ref(0);
const pageSize = ref(20);
const totalElements = ref(0);
const totalPages = ref(0);

const filters = ref({
    status: "",
    protocol: "",
});

// User state
const user = ref<any>(null);
const isAdmin = ref(false);

// Initialize auth on client-side only
onMounted(() => {
    if (process.client) {
        const authStore = useAuthStore();
        authStore.loadFromStorage();

        if (!authStore.isAuthenticated) {
            router.push("/login");
            return;
        }

        user.value = authStore.user;
        isAdmin.value = authStore.isAdmin;

        watch(
            () => authStore.user,
            (newUser) => {
                user.value = newUser;
                isAdmin.value = authStore.isAdmin;
            },
            { immediate: true, deep: true }
        );
    }

    loadConnections();
});

// Load connections
const loadConnections = async () => {
    loading.value = true;
    error.value = null;

    try {
        const response = await getConnectionLogs(
            currentPage.value,
            pageSize.value
        );

        // Apply client-side filtering if needed
        let filteredConnections = response.content;

        if (filters.value.status) {
            filteredConnections = filteredConnections.filter(
                (log) => log.status === filters.value.status
            );
        }

        if (filters.value.protocol) {
            filteredConnections = filteredConnections.filter(
                (log) => log.protocol === filters.value.protocol
            );
        }

        connections.value = filteredConnections;
        totalElements.value = response.totalElements;
        totalPages.value = response.totalPages;
    } catch (err: any) {
        error.value = err.message || "Failed to load connections";
        console.error("Error loading connections:", err);
    } finally {
        loading.value = false;
    }
};

// Reset filters
const resetFilters = () => {
    filters.value = {
        status: "",
        protocol: "",
    };
    currentPage.value = 0;
    loadConnections();
};

// Pagination
const goToPage = (page: number) => {
    if (page >= 0 && page < totalPages.value) {
        currentPage.value = page;
        loadConnections();
    }
};

// Calculate visible pages for pagination
const visiblePages = computed(() => {
    const pages: number[] = [];
    const maxVisible = 5;
    let start = Math.max(0, currentPage.value - Math.floor(maxVisible / 2));
    let end = Math.min(totalPages.value, start + maxVisible);

    if (end - start < maxVisible) {
        start = Math.max(0, end - maxVisible);
    }

    for (let i = start; i < end; i++) {
        pages.push(i);
    }
    return pages;
});

// Format date
const formatDate = (dateString: string) => {
    if (!dateString) return "N/A";
    const date = new Date(dateString);
    return date.toLocaleString();
};

// Format duration
const formatDuration = (seconds: number | null) => {
    if (seconds === null || seconds === undefined) return "N/A";

    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    const secs = seconds % 60;

    if (hours > 0) {
        return `${hours}h ${minutes}m ${secs}s`;
    } else if (minutes > 0) {
        return `${minutes}m ${secs}s`;
    } else {
        return `${secs}s`;
    }
};

// Logout
const handleLogout = async () => {
    if (process.client) {
        try {
            const authStore = useAuthStore();
            await authStore.logout();
            await router.push("/login");
        } catch (error) {
            console.error("Logout error:", error);
        }
    }
};
</script>
