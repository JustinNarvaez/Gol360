import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { render, screen, act } from '@testing-library/react'
import { AuthProvider, useAuth } from '../context/AuthContext'

vi.mock('../services/authService', () => ({
  logout: vi.fn().mockResolvedValue('ok'),
}))

import { logout as mockLogoutFn } from '../services/authService'
const mockLogout = mockLogoutFn as ReturnType<typeof vi.fn>

const AUTH_STORAGE_KEY = 'gol360_auth'

const MOCK_USER = {
  id: 1,
  userName: 'testuser',
  email: 'test@gol360.com',
  userType: 'LOCAL_FAN' as const,
  token: 'fake-jwt',
}

function StatusConsumer() {
  const { user, isAuthenticated } = useAuth()
  return (
    <div>
      <span data-testid="auth">{isAuthenticated ? 'yes' : 'no'}</span>
      <span data-testid="user">{user?.userName ?? 'none'}</span>
    </div>
  )
}

function LoginConsumer() {
  const { loginUser, isAuthenticated } = useAuth()
  return (
    <div>
      <span data-testid="auth">{isAuthenticated ? 'yes' : 'no'}</span>
      <button onClick={() => loginUser(MOCK_USER)}>login</button>
    </div>
  )
}

function LogoutConsumer() {
  const { logoutUser, isAuthenticated } = useAuth()
  return (
    <div>
      <span data-testid="auth">{isAuthenticated ? 'yes' : 'no'}</span>
      <button onClick={() => void logoutUser()}>logout</button>
    </div>
  )
}

beforeEach(() => {
  localStorage.clear()
  vi.clearAllMocks()
})

afterEach(() => {
  localStorage.clear()
})

describe('AuthContext', () => {
  it('starts unauthenticated when localStorage is empty', () => {
    render(<AuthProvider><StatusConsumer /></AuthProvider>)
    expect(screen.getByTestId('auth')).toHaveTextContent('no')
    expect(screen.getByTestId('user')).toHaveTextContent('none')
  })

  it('restores the session from localStorage on initialization', () => {
    localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(MOCK_USER))
    render(<AuthProvider><StatusConsumer /></AuthProvider>)
    expect(screen.getByTestId('auth')).toHaveTextContent('yes')
    expect(screen.getByTestId('user')).toHaveTextContent('testuser')
  })

  it('handles malformed localStorage data gracefully', () => {
    localStorage.setItem(AUTH_STORAGE_KEY, 'not-valid-json')
    render(<AuthProvider><StatusConsumer /></AuthProvider>)
    expect(screen.getByTestId('auth')).toHaveTextContent('no')
  })

  it('loginUser persists the user to localStorage and sets isAuthenticated', async () => {
    render(<AuthProvider><LoginConsumer /></AuthProvider>)
    expect(screen.getByTestId('auth')).toHaveTextContent('no')

    await act(async () => {
      screen.getByRole('button', { name: 'login' }).click()
    })

    expect(screen.getByTestId('auth')).toHaveTextContent('yes')
    const stored = JSON.parse(localStorage.getItem(AUTH_STORAGE_KEY)!)
    expect(stored.userName).toBe('testuser')
    expect(stored.token).toBe('fake-jwt')
  })

  it('logoutUser calls the API, removes localStorage entry, and clears auth state', async () => {
    localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(MOCK_USER))
    render(<AuthProvider><LogoutConsumer /></AuthProvider>)
    expect(screen.getByTestId('auth')).toHaveTextContent('yes')

    await act(async () => {
      screen.getByRole('button', { name: 'logout' }).click()
    })

    expect(screen.getByTestId('auth')).toHaveTextContent('no')
    expect(localStorage.getItem(AUTH_STORAGE_KEY)).toBeNull()
    expect(mockLogout).toHaveBeenCalled()
  })

  it('logoutUser clears local state even when the API call fails', async () => {
    mockLogout.mockRejectedValueOnce(new Error('Network'))
    localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(MOCK_USER))
    render(<AuthProvider><LogoutConsumer /></AuthProvider>)

    await act(async () => {
      screen.getByRole('button', { name: 'logout' }).click()
    })

    expect(screen.getByTestId('auth')).toHaveTextContent('no')
    expect(localStorage.getItem(AUTH_STORAGE_KEY)).toBeNull()
  })

  it('useAuth throws when rendered outside AuthProvider', () => {
    const spy = vi.spyOn(console, 'error').mockImplementation(() => {})
    expect(() => render(<StatusConsumer />)).toThrow('useAuth must be used inside AuthProvider')
    spy.mockRestore()
  })
})
