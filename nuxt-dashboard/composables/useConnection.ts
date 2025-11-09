import { useApi } from "./useApi";

export interface ConnectionInitiateResponse {
    connectionUrl: string;
    connectionLogId: number;
    guacamoleConnId: string;
    deviceName: string;
    protocol: string;
}

export interface ConnectionLog {
    id: number;
    userId: number;
    username: string | null;
    deviceId: number;
    deviceName: string | null;
    deviceHost: string | null;
    protocol: string | null;
    connectionStart: string;
    connectionEnd: string | null;
    duration: number | null;
    status: "success" | "failed" | "timeout";
    ipAddress: string | null;
    userAgent: string | null;
}

export interface EndConnectionRequest {
    status?: "success" | "failed" | "timeout";
}

export const useConnection = () => {
    const api = useApi();

    /**
     * Initiate a connection to a device
     */
    const initiateConnection = async (
        deviceId: number
    ): Promise<ConnectionInitiateResponse> => {
        try {
            const response = await api.post<ConnectionInitiateResponse>(
                `/connections/${deviceId}/initiate`
            );
            return response;
        } catch (error: any) {
            console.error("Failed to initiate connection:", error);
            throw new Error(error.message || "Failed to initiate connection");
        }
    };

    /**
     * End a connection
     */
    const endConnection = async (
        connectionLogId: number,
        status?: "success" | "failed" | "timeout"
    ): Promise<void> => {
        try {
            await api.post(`/connections/${connectionLogId}/end`, {
                status: status || "success",
            });
        } catch (error: any) {
            console.error("Failed to end connection:", error);
            throw new Error(error.message || "Failed to end connection");
        }
    };

    /**
     * Get connection logs for the current user
     */
    const getConnectionLogs = async (
        page: number = 0,
        size: number = 20
    ): Promise<{
        content: ConnectionLog[];
        totalElements: number;
        totalPages: number;
        number: number;
        size: number;
    }> => {
        try {
            const response = await api.get<{
                content: ConnectionLog[];
                totalElements: number;
                totalPages: number;
                number: number;
                size: number;
            }>(`/connections`, {
                params: {
                    page,
                    size,
                },
            });
            return response;
        } catch (error: any) {
            console.error("Failed to get connection logs:", error);
            throw new Error(error.message || "Failed to get connection logs");
        }
    };

    /**
     * Get connection logs for a specific device
     */
    const getDeviceConnectionLogs = async (
        deviceId: number,
        page: number = 0,
        size: number = 20
    ): Promise<{
        content: ConnectionLog[];
        totalElements: number;
        totalPages: number;
        number: number;
        size: number;
    }> => {
        try {
            const response = await api.get<{
                content: ConnectionLog[];
                totalElements: number;
                totalPages: number;
                number: number;
                size: number;
            }>(`/connections/device/${deviceId}/logs`, {
                params: {
                    page,
                    size,
                },
            });
            return response;
        } catch (error: any) {
            console.error("Failed to get device connection logs:", error);
            throw new Error(
                error.message || "Failed to get device connection logs"
            );
        }
    };

    return {
        initiateConnection,
        endConnection,
        getConnectionLogs,
        getDeviceConnectionLogs,
    };
};
