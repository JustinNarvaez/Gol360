import { describe, it, expect, vi, beforeEach } from 'vitest'
import { getPreferences, updatePreferences } from '../services/preferenceService'
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

const PREFS_RESPONSE = {
  id: 1,
  favNations: [{ id: 1, name: 'Colombia', fifaCode: 'COL', flagUrl: 'https://flagcdn.com/co.svg' }],
  favStadiums: [{ id: 1, name: 'MetLife Stadium', city: 'Nueva Jersey' }],
  favNotifications: ['GOAL' as const],
  favCities: null,
}

beforeEach(() => vi.clearAllMocks())

describe('preferenceService', () => {
  it('getPreferences calls GET /preferences/:userId', async () => {
    mockApi.get.mockResolvedValueOnce({ data: PREFS_RESPONSE })
    const result = await getPreferences(1)
    expect(mockApi.get).toHaveBeenCalledWith('/preferences/1')
    expect(result).toEqual(PREFS_RESPONSE)
  })

  it('updatePreferences calls PUT /preferences/:userId with the payload', async () => {
    const req = {
      favNationIds: [1, 3],
      favStadiumIds: [2],
      favNotifications: ['GOAL' as const, 'MATCH_START' as const],
    }
    mockApi.put.mockResolvedValueOnce({ data: PREFS_RESPONSE })
    const result = await updatePreferences(1, req)
    expect(mockApi.put).toHaveBeenCalledWith('/preferences/1', req)
    expect(result).toEqual(PREFS_RESPONSE)
  })

  it('getPreferences propagates errors', async () => {
    mockApi.get.mockRejectedValueOnce({ response: { status: 404 } })
    await expect(getPreferences(999)).rejects.toMatchObject({ response: { status: 404 } })
  })
})
