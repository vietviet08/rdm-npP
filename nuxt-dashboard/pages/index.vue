<template>
    <div class="min-h-screen flex items-center justify-center bg-gray-50">
        <div class="text-center">
            <div
                class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600"
            ></div>
            <p class="mt-4 text-gray-600">Loading...</p>
        </div>
    </div>
</template>

<script setup lang="ts">
// Root page - redirects based on auth status
definePageMeta({
    middleware: "auth",
});
</script>
