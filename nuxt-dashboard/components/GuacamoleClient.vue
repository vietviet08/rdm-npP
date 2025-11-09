<template>
    <div class="relative w-full h-full bg-gray-900">
        <!-- Loading State -->
        <div
            v-if="loading"
            class="absolute inset-0 flex items-center justify-center bg-gray-900"
        >
            <div class="text-center">
                <div
                    class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-white mb-4"
                ></div>
                <p class="text-white text-sm">Connecting to device...</p>
            </div>
        </div>

        <!-- Error State -->
        <div
            v-else-if="error"
            class="absolute inset-0 flex items-center justify-center bg-gray-900"
        >
            <div class="text-center max-w-md px-4">
                <div class="text-red-500 text-4xl mb-4">⚠️</div>
                <h3 class="text-white text-lg font-semibold mb-2">
                    Connection Error
                </h3>
                <p class="text-gray-300 text-sm mb-4">{{ error }}</p>
                <button
                    @click="$emit('retry')"
                    class="px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 text-sm"
                >
                    Retry
                </button>
            </div>
        </div>

        <!-- Guacamole iframe -->
        <iframe
            v-else
            ref="guacamoleFrame"
            :src="connectionUrl"
            class="w-full h-full border-0"
            @load="onFrameLoad"
            @error="onFrameError"
        ></iframe>

        <!-- Connection Controls -->
        <div
            v-if="!loading && !error"
            class="absolute top-4 right-4 flex gap-2"
        >
            <button
                @click="toggleFullscreen"
                class="px-3 py-2 bg-gray-800 bg-opacity-75 text-white rounded-md hover:bg-opacity-100 text-sm transition-all"
                :title="isFullscreen ? 'Exit Fullscreen' : 'Enter Fullscreen'"
            >
                <svg
                    v-if="!isFullscreen"
                    class="w-5 h-5"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                >
                    <path
                        stroke-linecap="round"
                        stroke-linejoin="round"
                        stroke-width="2"
                        d="M4 8V4m0 0h4M4 4l5 5m11-1V4m0 0h-4m4 0l-5 5M4 16v4m0 0h4m-4 0l5-5m11 5l-5-5m5 5v-4m0 4h-4"
                    />
                </svg>
                <svg
                    v-else
                    class="w-5 h-5"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                >
                    <path
                        stroke-linecap="round"
                        stroke-linejoin="round"
                        stroke-width="2"
                        d="M6 18L18 6M6 6l12 12"
                    />
                </svg>
            </button>
            <button
                @click="$emit('disconnect')"
                class="px-3 py-2 bg-red-600 bg-opacity-75 text-white rounded-md hover:bg-opacity-100 text-sm transition-all"
                title="Disconnect"
            >
                <svg
                    class="w-5 h-5"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                >
                    <path
                        stroke-linecap="round"
                        stroke-linejoin="round"
                        stroke-width="2"
                        d="M6 18L18 6M6 6l12 12"
                    />
                </svg>
            </button>
        </div>
    </div>
</template>

<script setup lang="ts">
interface Props {
    connectionUrl: string;
}

const props = defineProps<Props>();

const emit = defineEmits<{
    disconnect: [];
    retry: [];
    loaded: [];
    error: [message: string];
}>();

const guacamoleFrame = ref<HTMLIFrameElement | null>(null);
const loading = ref(true);
const error = ref<string | null>(null);
const isFullscreen = ref(false);
const connectionTimeout = ref<NodeJS.Timeout | null>(null);
const loadTimeout = ref<NodeJS.Timeout | null>(null);

// Connection timeout (30 seconds)
const CONNECTION_TIMEOUT_MS = 30000;

const onFrameLoad = () => {
    if (loadTimeout.value) {
        clearTimeout(loadTimeout.value);
        loadTimeout.value = null;
    }

    // Wait a bit to check if connection is actually established
    // Guacamole may load but connection might fail
    setTimeout(() => {
        loading.value = false;
        error.value = null;
        emit("loaded");

        // Set connection timeout to detect disconnections
        connectionTimeout.value = setTimeout(() => {
            // Connection seems to be inactive, but don't show error yet
            // This is just for monitoring
        }, CONNECTION_TIMEOUT_MS);
    }, 1000);
};

const onFrameError = () => {
    if (loadTimeout.value) {
        clearTimeout(loadTimeout.value);
        loadTimeout.value = null;
    }
    loading.value = false;
    error.value =
        "Failed to load connection. Please check your network and try again.";
    emit("error", error.value);
};

// Watch for connection URL changes
watch(
    () => props.connectionUrl,
    () => {
        if (props.connectionUrl) {
            loading.value = true;
            error.value = null;

            // Set load timeout
            if (loadTimeout.value) {
                clearTimeout(loadTimeout.value);
            }

            loadTimeout.value = setTimeout(() => {
                if (loading.value) {
                    loading.value = false;
                    error.value =
                        "Connection timeout. The device may be unreachable or the connection is taking too long.";
                    emit("error", error.value);
                }
            }, CONNECTION_TIMEOUT_MS);
        }
    },
    { immediate: true }
);

const toggleFullscreen = () => {
    if (!process.client) return;

    if (!isFullscreen.value) {
        // Enter fullscreen
        const elem = guacamoleFrame.value?.parentElement;
        if (elem) {
            if (elem.requestFullscreen) {
                elem.requestFullscreen();
            } else if ((elem as any).webkitRequestFullscreen) {
                (elem as any).webkitRequestFullscreen();
            } else if ((elem as any).msRequestFullscreen) {
                (elem as any).msRequestFullscreen();
            }
        }
        isFullscreen.value = true;
    } else {
        // Exit fullscreen
        if (document.exitFullscreen) {
            document.exitFullscreen();
        } else if ((document as any).webkitExitFullscreen) {
            (document as any).webkitExitFullscreen();
        } else if ((document as any).msExitFullscreen) {
            (document as any).msExitFullscreen();
        }
        isFullscreen.value = false;
    }
};

// Listen for fullscreen changes
onMounted(() => {
    if (process.client) {
        document.addEventListener("fullscreenchange", () => {
            isFullscreen.value = !!document.fullscreenElement;
        });
        document.addEventListener("webkitfullscreenchange", () => {
            isFullscreen.value = !!(document as any).webkitFullscreenElement;
        });
        document.addEventListener("msfullscreenchange", () => {
            isFullscreen.value = !!(document as any).msFullscreenElement;
        });

        // Set initial load timeout
        if (props.connectionUrl) {
            loadTimeout.value = setTimeout(() => {
                if (loading.value) {
                    loading.value = false;
                    error.value =
                        "Connection timeout. The device may be unreachable or the connection is taking too long.";
                    emit("error", error.value);
                }
            }, CONNECTION_TIMEOUT_MS);
        }
    }
});

// Cleanup on unmount
onBeforeUnmount(() => {
    if (loadTimeout.value) {
        clearTimeout(loadTimeout.value);
    }
    if (connectionTimeout.value) {
        clearTimeout(connectionTimeout.value);
    }
});
</script>
