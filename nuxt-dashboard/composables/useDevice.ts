import { useApi } from "./useApi";
import type { Device } from "~/types";

export interface DeviceListResponse {
    content: Device[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
}

export const useDevice = () => {
    const api = useApi();

    /**
     * Get all devices with pagination and filtering
     */
    const getDevices = async (
        page: number = 0,
        size: number = 20,
        name?: string,
        protocol?: "rdp" | "vnc" | "ssh",
        status?: "online" | "offline" | "unknown"
    ): Promise<DeviceListResponse> => {
        try {
            const params: Record<string, any> = {
                page,
                size,
            };

            if (name) params.name = name;
            if (protocol) params.protocol = protocol;
            if (status) params.status = status;

            const response = await api.get<DeviceListResponse>("/devices", {
                params,
            });
            return response;
        } catch (error: any) {
            console.error("Failed to get devices:", error);
            throw new Error(error.message || "Failed to get devices");
        }
    };

    /**
     * Get a device by ID
     */
    const getDevice = async (id: number): Promise<Device> => {
        try {
            const response = await api.get<Device>(`/devices/${id}`);
            return response;
        } catch (error: any) {
            console.error("Failed to get device:", error);
            throw new Error(error.message || "Failed to get device");
        }
    };

    return {
        getDevices,
        getDevice,
    };
};
