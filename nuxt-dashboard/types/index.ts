// Type definitions for the application

export type UserRole = 'admin' | 'operator' | 'viewer'
export type DeviceProtocol = 'rdp' | 'vnc' | 'ssh'
export type DeviceStatus = 'online' | 'offline' | 'unknown'
export type PermissionType = 'read' | 'write' | 'control' | 'view'
export type ConnectionStatus = 'success' | 'failed' | 'timeout'
export type AuditAction = 'create' | 'update' | 'delete' | 'connect' | 'login' | 'logout'

export interface User {
  id: number
  username: string
  email: string
  password_hash: string
  role: UserRole
  created_at: string
  updated_at: string
  last_login: string | null
  is_active: boolean
}

export interface Device {
  id: number
  name: string
  description: string | null
  host: string
  port: number
  protocol: DeviceProtocol
  username: string | null
  password_encrypted: string | null
  private_key: string | null
  guacamole_conn_id: string | null
  status: DeviceStatus
  tags: string[]
  created_at: string
  updated_at: string
  created_by: number | null
  is_active: boolean
}

export interface UserDevice {
  user_id: number
  device_id: number
  permission: PermissionType
  granted_at: string
  granted_by: number | null
}

export interface UserGroup {
  id: number
  name: string
  description: string | null
  created_at: string
}

export interface GroupMember {
  group_id: number
  user_id: number
  joined_at: string
}

export interface GroupDevice {
  group_id: number
  device_id: number
  permission: PermissionType
  granted_at: string
}

export interface ConnectionLog {
  id: number
  user_id: number
  device_id: number
  connection_start: string
  connection_end: string | null
  duration: number | null
  status: ConnectionStatus
  ip_address: string | null
  user_agent: string | null
}

export interface AuditLog {
  id: number
  user_id: number | null
  action: AuditAction
  resource_type: string
  resource_id: number | null
  details: Record<string, any>
  ip_address: string | null
  timestamp: string
}

export interface LoginCredentials {
  username: string
  password: string
}

export interface JWTPayload {
  userId: number
  username: string
  role: UserRole
  iat?: number
  exp?: number
}

