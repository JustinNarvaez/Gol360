import { describe, it, expect, vi, beforeEach } from 'vitest'
import { getUserById, updateUser, changePassword } from '../services/userService'
import api from '../services/api'

vi.mock('../services/api', () => ({
  default: {
    get: vi.fn(),
    put: vi.fn(),
    interceptors: { request: { use: vi.fn() } },
  },
}))

const mockApi = api as unknown as {
  get: ReturnType<typeof vi.fn>
  put: ReturnType<typeof vi.fn>
}

const PROFILE = {
  id: 1,
  firstName: 'Juan',
  lastName: 'García',
  userName: 'juangarcia',
  email: 'juan@gol360.com',
  registerDate: '2026-01-01',
  userType: 'LOCAL_FAN' as const,
  preferences: null,
  notificationsEnabled: null,
}

beforeEach(() => vi.clearAllMocks())

describe('userService', () => {
  it('getUserById calls GET /users/:id and returns the profile', async () => {
    mockApi.get.mockResolvedValueOnce({ data: PROFILE })
    const result = await getUserById(1)
    expect(mockApi.get).toHaveBeenCalledWith('/users/1')
    expect(result).toEqual(PROFILE)
  })

  it('updateUser calls PUT /users/:id with the update payload', async () => {
    const update = { firstName: 'Juan', lastName: 'García', userName: 'juanv2', email: 'juan@gol360.com' }
    mockApi.put.mockResolvedValueOnce({ data: { ...PROFILE, ...update } })
    const result = await updateUser(1, update)
    expect(mockApi.put).toHaveBeenCalledWith('/users/1', update)
    expect(result.userName).toBe('juanv2')
  })

  it('changePassword calls PUT /users/:id/password with the password payload', async () => {
    const pwdReq = { currentPassword: 'old123', newPassword: 'new123', confirmPassword: 'new123' }
    mockApi.put.mockResolvedValueOnce({ data: 'Contraseña actualizada' })
    const result = await changePassword(1, pwdReq)
    expect(mockApi.put).toHaveBeenCalledWith('/users/1/password', pwdReq)
    expect(result).toBe('Contraseña actualizada')
  })

  it('getUserById propagates 404 errors', async () => {
    mockApi.get.mockRejectedValueOnce({ response: { status: 404 } })
    await expect(getUserById(999)).rejects.toMatchObject({ response: { status: 404 } })
  })

  it('updateUser propagates server errors', async () => {
    mockApi.put.mockRejectedValueOnce({ response: { status: 409 } })
    await expect(updateUser(1, { firstName: 'x', lastName: 'y', userName: 'z', email: 'e@e.com' }))
      .rejects.toMatchObject({ response: { status: 409 } })
  })
})
