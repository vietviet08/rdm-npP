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
                                class="border-indigo-500 text-gray-900 inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium"
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
                <div
                    class="border-4 border-dashed border-gray-200 rounded-lg p-8"
                >
                    <h2 class="text-2xl font-bold text-gray-900 mb-4">
                        Welcome to RDM Platform
                    </h2>
                    <p class="text-gray-600 mb-6">
                        Remote Desktop Management Platform - Phase 2
                        Implementation
                    </p>

                    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                        <div class="bg-white overflow-hidden shadow rounded-lg">
                            <div class="p-5">
                                <div class="flex items-center">
                                    <div class="flex-shrink-0">
                                        <svg
                                            class="h-6 w-6 text-gray-400"
                                            fill="none"
                                            viewBox="0 0 24 24"
                                            stroke="currentColor"
                                        >
                                            <path
                                                stroke-linecap="round"
                                                stroke-linejoin="round"
                                                stroke-width="2"
                                                d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"
                                            />
                                        </svg>
                                    </div>
                                    <div class="ml-5 w-0 flex-1">
                                        <dl>
                                            <dt
                                                class="text-sm font-medium text-gray-500 truncate"
                                            >
                                                Authentication
                                            </dt>
                                            <dd
                                                class="text-lg font-medium text-gray-900"
                                            >
                                                ✅ Complete
                                            </dd>
                                        </dl>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="bg-white overflow-hidden shadow rounded-lg">
                            <div class="p-5">
                                <div class="flex items-center">
                                    <div class="flex-shrink-0">
                                        <svg
                                            class="h-6 w-6 text-gray-400"
                                            fill="none"
                                            viewBox="0 0 24 24"
                                            stroke="currentColor"
                                        >
                                            <path
                                                stroke-linecap="round"
                                                stroke-linejoin="round"
                                                stroke-width="2"
                                                d="M9 3v2m6-2v2M9 19v2m6-2v2M5 9H3m2 6H3m18-6h-2m2 6h-2M7 19h10a2 2 0 002-2V7a2 2 0 00-2-2H7a2 2 0 00-2 2v10a2 2 0 002 2zM9 9h6v6H9V9z"
                                            />
                                        </svg>
                                    </div>
                                    <div class="ml-5 w-0 flex-1">
                                        <dl>
                                            <dt
                                                class="text-sm font-medium text-gray-500 truncate"
                                            >
                                                Device Management
                                            </dt>
                                            <dd
                                                class="text-lg font-medium text-gray-900"
                                            >
                                                ✅ Complete
                                            </dd>
                                        </dl>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="bg-white overflow-hidden shadow rounded-lg">
                            <div class="p-5">
                                <div class="flex items-center">
                                    <div class="flex-shrink-0">
                                        <svg
                                            class="h-6 w-6 text-gray-400"
                                            fill="none"
                                            viewBox="0 0 24 24"
                                            stroke="currentColor"
                                        >
                                            <path
                                                stroke-linecap="round"
                                                stroke-linejoin="round"
                                                stroke-width="2"
                                                d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"
                                            />
                                        </svg>
                                    </div>
                                    <div class="ml-5 w-0 flex-1">
                                        <dl>
                                            <dt
                                                class="text-sm font-medium text-gray-500 truncate"
                                            >
                                                User Management
                                            </dt>
                                            <dd
                                                class="text-lg font-medium text-gray-900"
                                            >
                                                ✅ Complete
                                            </dd>
                                        </dl>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </main>
    </div>
</template>

<script setup lang="ts">
definePageMeta({
    middleware: "auth",
});

const router = useRouter();

// Initialize reactive refs for user data
const user = ref<any>(null);
const isAdmin = ref(false);

// Initialize auth on client-side only
onMounted(() => {
    if (process.client) {
        const authStore = useAuthStore();

        // Load auth from storage
        authStore.loadFromStorage();

        // Check authentication
        if (!authStore.isAuthenticated) {
            router.push("/login");
            return;
        }

        // Set initial values
        user.value = authStore.user;
        isAdmin.value = authStore.isAdmin;

        // Watch for changes in store
        watch(
            () => authStore.user,
            (newUser) => {
                user.value = newUser;
                isAdmin.value = authStore.isAdmin;
            },
            { immediate: true, deep: true }
        );
    }
});

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
