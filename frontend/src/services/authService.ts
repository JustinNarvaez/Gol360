import api from './api'
import type { AuthResponse, LoginRequest, RegisterRequest } from '../types/auth'

export const login = async (data: LoginRequest): Promise<AuthResponse> => {
  const response = await api.post<AuthResponse>('/auth/login', data)
  return response.data
}

export const register = async (data: RegisterRequest): Promise<AuthResponse> => {
  const response = await api.post<AuthResponse>('/auth/register', data)
  return response.data
}

export const logout = async (): Promise<void> => {
  await api.post('/auth/logout')
}
