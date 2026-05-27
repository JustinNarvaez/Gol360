import api from './api'
import type { UserProfile, UserUpdateRequest, ChangePasswordRequest } from '../types/user'

export const getUserById = (id: number): Promise<UserProfile> =>
  api.get<UserProfile>(`/users/${id}`).then((r) => r.data)

export const updateUser = (id: number, data: UserUpdateRequest): Promise<UserProfile> =>
  api.put<UserProfile>(`/users/${id}`, data).then((r) => r.data)

export const changePassword = (id: number, data: ChangePasswordRequest): Promise<string> =>
  api.put<string>(`/users/${id}/password`, data).then((r) => r.data)
