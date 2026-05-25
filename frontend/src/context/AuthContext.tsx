import { createContext, useCallback, useContext, useState, type ReactNode } from 'react'
import type { AuthUser } from '../types/auth'
import { logout as apiLogout } from '../services/authService'

const AUTH_STORAGE_KEY = 'gol360_auth'

interface AuthState {
  user: AuthUser | null
  isAuthenticated: boolean
}

interface AuthContextValue extends AuthState {
  loginUser: (user: AuthUser) => void
  logoutUser: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | null>(null)

function loadStoredUser(): AuthUser | null {
  try {
    const stored = localStorage.getItem(AUTH_STORAGE_KEY)
    return stored ? (JSON.parse(stored) as AuthUser) : null
  } catch {
    return null
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<AuthState>(() => {
    const user = loadStoredUser()
    return { user, isAuthenticated: user !== null }
  })

  const loginUser = useCallback((user: AuthUser) => {
    localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(user))
    setState({ user, isAuthenticated: true })
  }, [])

  const logoutUser = useCallback(async () => {
    try {
      await apiLogout()
    } catch {
      // intentional: local state clears regardless of server response
    }
    localStorage.removeItem(AUTH_STORAGE_KEY)
    setState({ user: null, isAuthenticated: false })
  }, [])

  return (
    <AuthContext.Provider value={{ ...state, loginUser, logoutUser }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider')
  return ctx
}
