<template>
    <div
        class="min-h-screen bg-gray-50 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8"
    >
        <div class="max-w-md w-full space-y-8">
            <div>
                <h2
                    class="mt-6 text-center text-3xl font-extrabold text-gray-900"
                >
                    Sign in to RDM Platform
                </h2>
                <p class="mt-2 text-center text-sm text-gray-600">
                    Remote Desktop Management Platform
                </p>
            </div>
            <form class="mt-8 space-y-6" @submit.prevent="handleLogin">
                <div class="rounded-md shadow-sm -space-y-px">
                    <div>
                        <label for="username" class="sr-only">Username</label>
                        <input
                            id="username"
                            v-model="form.username"
                            name="username"
                            type="text"
                            required
                            class="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-t-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm"
                            placeholder="Username"
                        />
                    </div>
                    <div>
                        <label for="password" class="sr-only">Password</label>
                        <input
                            id="password"
                            v-model="form.password"
                            name="password"
                            type="password"
                            required
                            class="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-b-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm"
                            placeholder="Password"
                        />
                    </div>
                </div>

                <div v-if="error" class="rounded-md bg-red-50 p-4">
                    <div class="flex">
                        <div class="ml-3">
                            <h3 class="text-sm font-medium text-red-800">
                                {{ error }}
                            </h3>
                        </div>
                    </div>
                </div>

                <div>
                    <button
                        type="submit"
                        :disabled="loading"
                        class="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                        <span v-if="loading">Signing in...</span>
                        <span v-else>Sign in</span>
                    </button>
                </div>
            </form>
        </div>
    </div>
</template>

<script setup lang="ts">
definePageMeta({
    layout: false,
    middleware: [],
});

const { login } = useAuth();
const router = useRouter();

const form = reactive({
    username: "",
    password: "",
});

const loading = ref(false);
const error = ref("");

const handleLogin = async () => {
    loading.value = true;
    error.value = "";

    try {
        const result = await login(form.username, form.password);
        if (result.success) {
            await router.push("/dashboard");
        } else {
            error.value = result.error || "Login failed";
        }
    } catch (err: any) {
        error.value = err.message || "An error occurred during login";
    } finally {
        loading.value = false;
    }
};
</script>
