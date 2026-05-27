import type { NotificationType, UserType } from './auth'

export interface TeamSummary {
  id: number
  name: string
  fifaCode: string
  flagUrl: string
}

export interface StadiumSummary {
  id: number
  name: string
  city: string
}

export interface CitySummary {
  id: number
  name: string
  country: string
  iataCode: string
}

export interface PreferencesResponse {
  id: number
  favNations: TeamSummary[]
  favStadiums: StadiumSummary[]
  favNotifications: NotificationType[]
  favCities: CitySummary[] | null
}

export interface UserProfile {
  id: number
  firstName: string
  lastName: string
  userName: string
  email: string
  registerDate: string | null
  userType: UserType
  preferences: PreferencesResponse | null
  notificationsEnabled: boolean | null
}

export interface UserUpdateRequest {
  firstName: string
  lastName: string
  userName: string
  email: string
}

export interface ChangePasswordRequest {
  currentPassword: string
  newPassword: string
  confirmPassword: string
}
