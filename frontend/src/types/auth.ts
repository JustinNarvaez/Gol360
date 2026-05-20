export type UserType = 'LOCAL_FAN' | 'VISIT_FAN' | 'OPERATOR' | 'SUPPORT' | 'COMPLIANCE'

export type NotificationType =
  | 'GOAL'
  | 'YELLOW_CARD'
  | 'RED_CARD'
  | 'MATCH_START'
  | 'MATCH_END'
  | 'SUBSTITUTION'

export interface LoginRequest {
  identifier: string
  password: string
}

export interface PreferencesRequest {
  favNationIds: number[]
  favStadiumIds: number[]
  favNotifications: NotificationType[]
  favCityIds?: number[]
}

export interface RegisterRequest {
  firstName: string
  lastName: string
  userName: string
  email: string
  password: string
  userType: UserType
  preferences: PreferencesRequest
}

export interface AuthResponse {
  token: string | null
  tokenType: string
  id: number
  userName: string
  email: string
  userType: UserType
}

export interface AuthUser {
  id: number
  userName: string
  email: string
  userType: UserType
  token: string | null
}
