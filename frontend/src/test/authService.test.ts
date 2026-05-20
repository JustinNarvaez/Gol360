import { describe, it, expect, vi, beforeEach } from 'vitest'
import { login, register, logout } from '../services/authService'
import api from '../services/api'

vi.mock('../services/api', () => ({
  default: {
    post: vi.fn(),
    interceptors: { request: { use: vi.fn() } },
  },
}))

const mockApi = api as unknown as { post: ReturnType<typeof vi.fn> }

const AUTH_RESPONSE = {
  token: 'fake-jwt',
  tokenType: 'Bearer',
  id: 1,
  userName: 'testuser',
  email: 'test@gol360.com',
  userType: 'LOCAL_FAN' as const,
}

beforeEach(() => {
  vi.clearAllMocks()
})

describe('authService.login', () => {
  it('calls POST /auth/login with credentials', async () => {
    mockApi.post.mockResolvedValueOnce({ data: AUTH_RESPONSE })
    const result = await login({ identifier: 'testuser', password: 'password123' })
    expect(mockApi.post).toHaveBeenCalledWith('/auth/login', {
      identifier: 'testuser',
      password: 'password123',
    })
    expect(result).toEqual(AUTH_RESPONSE)
  })

  it('propagates server errors', async () => {
    mockApi.post.mockRejectedValueOnce({ response: { status: 401 } })
    await expect(login({ identifier: 'bad', password: 'bad' })).rejects.toMatchObject({
      response: { status: 401 },
    })
  })
})

describe('authService.register', () => {
  it('calls POST /auth/register with full payload', async () => {
    mockApi.post.mockResolvedValueOnce({ data: AUTH_RESPONSE })
    const payload = {
      firstName: 'Juan',
      lastName: 'García',
      userName: 'testuser',
      email: 'test@gol360.com',
      password: 'password123',
      userType: 'LOCAL_FAN' as const,
      preferences: {
        favNationIds: [1, 2],
        favStadiumIds: [1],
        favNotifications: ['GOAL' as const],
      },
    }
    const result = await register(payload)
    expect(mockApi.post).toHaveBeenCalledWith('/auth/register', payload)
    expect(result.userName).toBe('testuser')
  })
})

describe('authService.logout', () => {
  it('calls POST /auth/logout', async () => {
    mockApi.post.mockResolvedValueOnce({ data: 'Sesion cerrada exitosamente' })
    await logout()
    expect(mockApi.post).toHaveBeenCalledWith('/auth/logout')
  })
})
