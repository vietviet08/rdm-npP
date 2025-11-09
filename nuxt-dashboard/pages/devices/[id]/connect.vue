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
                                class="border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700 inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium"
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
                <!-- Device Info -->
                <div class="bg-white shadow rounded-lg p-4 mb-4">
                    <div class="flex items-center justify-between">
                        <div>
                            <h2 class="text-xl font-semibold text-gray-900">
                                {{ device?.name || "Loading..." }}
                            </h2>
                            <p v-if="device" class="text-sm text-gray-600 mt-1">
                                {{ device.host }}:{{ device.port }} •
                                {{ device.protocol.toUpperCase() }}
                            </p>
                        </div>
                        <button
                            @click="goBack"
                            class="px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 bg-white hover:bg-gray-50"
                        >
                            Back to Devices
                        </button>
                    </div>
                </div>

                <!-- Connection Status -->
                <div v-if="connectionStatus" class="mb-4">
                    <div
                        class="bg-white shadow rounded-lg p-4"
                        :class="{
                            'border-l-4 border-green-500':
                                connectionStatus === 'connected',
                            'border-l-4 border-yellow-500':
                                connectionStatus === 'connecting',
                            'border-l-4 border-red-500':
                                connectionStatus === 'error',
                        }"
                    >
                        <div class="flex items-center">
                            <div
                                class="flex-shrink-0"
                                :class="{
                                    'text-green-500':
                                        connectionStatus === 'connected',
                                    'text-yellow-500':
                                        connectionStatus === 'connecting',
                                    'text-red-500':
                                        connectionStatus === 'error',
                                }"
                            >
                                <svg
                                    v-if="connectionStatus === 'connected'"
                                    class="h-5 w-5"
                                    fill="currentColor"
                                    viewBox="0 0 20 20"
                                >
                                    <path
                                        fill-rule="evenodd"
                                        d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                                        clip-rule="evenodd"
                                    />
                                </svg>
                                <svg
                                    v-else-if="
                                        connectionStatus === 'connecting'
                                    "
                                    class="h-5 w-5 animate-spin"
                                    fill="none"
                                    viewBox="0 0 24 24"
                                    stroke="currentColor"
                                >
                                    <path
                                        stroke-linecap="round"
                                        stroke-linejoin="round"
                                        stroke-width="2"
                                        d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
                                    />
                                </svg>
                                <svg
                                    v-else
                                    class="h-5 w-5"
                                    fill="currentColor"
                                    viewBox="0 0 20 20"
                                >
                                    <path
                                        fill-rule="evenodd"
                                        d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
                                        clip-rule="evenodd"
                                    />
                                </svg>
                            </div>
                            <div class="ml-3">
                                <p class="text-sm font-medium text-gray-900">
                                    <span
                                        v-if="connectionStatus === 'connected'"
                                        >Connected</span
                                    >
                                    <span
                                        v-else-if="
                                            connectionStatus === 'connecting'
                                        "
                                        >Connecting...</span
                                    >
                                    <span v-else>Connection Error</span>
                                </p>
                                <p
                                    v-if="errorMessage"
                                    class="text-sm text-gray-500 mt-1"
                                >
                                    {{ errorMessage }}
                                </p>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Guacamole Client -->
                <div
                    class="bg-white shadow rounded-lg overflow-hidden"
                    style="height: calc(100vh - 300px); min-height: 600px"
                >
                    <GuacamoleClient
                        v-if="connectionUrl"
                        :connection-url="connectionUrl"
                        @disconnect="handleDisconnect"
                        @retry="handleRetry"
                        @loaded="onConnectionLoaded"
                        @error="onConnectionError"
                    />
                    <div v-else class="flex items-center justify-center h-full">
                        <div class="text-center">
                            <div
                                class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600 mb-4"
                            ></div>
                            <p class="text-gray-600">
                                Initializing connection...
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </main>
    </div>
</template>

<script setup lang="ts">
import { useConnection } from "~/composables/useConnection";
import { useDevice } from "~/composables/useDevice";

definePageMeta({
    middleware: "auth",
});

const route = useRoute();
const router = useRouter();
const deviceId = computed(() => parseInt(route.params.id as string));
const isViewOnly = computed(() => route.query.view === "true");

const { getDevice } = useDevice();
const { initiateConnection, endConnection } = useConnection();

// State
const device = ref<any>(null);
const connectionUrl = ref<string | null>(null);
const connectionLogId = ref<number | null>(null);
const connectionStatus = ref<"connecting" | "connected" | "error" | null>(null);
const errorMessage = ref<string | null>(null);
const loading = ref(true);

// User state
const user = ref<any>(null);
const isAdmin = ref(false);

// Initialize
onMounted(async () => {
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

    await loadDevice();
    await startConnection();
});

// Load device info
const loadDevice = async () => {
    try {
        device.value = await getDevice(deviceId.value);
    } catch (err: any) {
        errorMessage.value = err.message || "Failed to load device information";
        connectionStatus.value = "error";
        console.error("Error loading device:", err);
    }
};

// Start connection
const startConnection = async () => {
    try {
        loading.value = true;
        connectionStatus.value = "connecting";
        errorMessage.value = null;

        const response = await initiateConnection(deviceId.value);
        connectionUrl.value = response.connectionUrl;
        connectionLogId.value = response.connectionLogId;
    } catch (err: any) {
        errorMessage.value = err.message || "Failed to initiate connection";
        connectionStatus.value = "error";
        console.error("Error initiating connection:", err);
    } finally {
        loading.value = false;
    }
};

// Connection event handlers
const onConnectionLoaded = () => {
    connectionStatus.value = "connected";
    errorMessage.value = null;
};

const onConnectionError = (message: string) => {
    connectionStatus.value = "error";
    errorMessage.value = message;
};

const handleRetry = async () => {
    await startConnection();
};

const handleDisconnect = async () => {
    if (connectionLogId.value) {
        try {
            await endConnection(connectionLogId.value, "success");
        } catch (err) {
            console.error("Error ending connection:", err);
        }
    }
    router.push("/devices");
};

// Cleanup on unmount
onBeforeUnmount(async () => {
    if (connectionLogId.value) {
        try {
            await endConnection(connectionLogId.value, "success");
        } catch (err) {
            console.error("Error ending connection on unmount:", err);
        }
    }
});

// Navigation
const goBack = () => {
    router.push("/devices");
};

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
