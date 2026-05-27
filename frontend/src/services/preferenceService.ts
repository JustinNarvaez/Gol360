import api from './api'
import type { PreferencesResponse } from '../types/user'
import type { PreferencesRequest } from '../types/auth'

export const getPreferences = (userId: number): Promise<PreferencesResponse> =>
  api.get<PreferencesResponse>(`/preferences/${userId}`).then((r) => r.data)

export const updatePreferences = (userId: number, data: PreferencesRequest): Promise<PreferencesResponse> =>
  api.put<PreferencesResponse>(`/preferences/${userId}`, data).then((r) => r.data)
