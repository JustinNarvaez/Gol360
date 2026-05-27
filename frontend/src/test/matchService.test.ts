import { describe, it, expect, vi, beforeEach } from 'vitest'
import {
  getAllMatches,
  getMatchById,
  getScheduledMatches,
  getFinishedMatches,
  getMatchesByPhase,
  getMatchesByTeam,
  getMatchesByRound,
  loadMatchesFromApi,
} from '../services/matchService'
import api from '../services/api'

vi.mock('../services/api', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    interceptors: { request: { use: vi.fn() } },
  },
}))

const mockApi = api as unknown as {
  get: ReturnType<typeof vi.fn>
  post: ReturnType<typeof vi.fn>
}

const MATCH = {
  id: 1,
  dateTime: '2026-06-11T18:00:00',
  roundName: 'Group A - Matchday 1',
  phase: 'GROUP' as const,
  status: 'SCHEDULED' as const,
  homeTeam: 'Colombia',
  homeTeamCode: 'COL',
  homeTeamFlag: 'https://flagcdn.com/co.svg',
  awayTeam: 'Brasil',
  awayTeamCode: 'BRA',
  awayTeamFlag: 'https://flagcdn.com/br.svg',
  homeScore: null,
  awayScore: null,
  result: null,
  stadium: 'MetLife Stadium',
  refreshed: false,
}

beforeEach(() => vi.clearAllMocks())

describe('matchService', () => {
  it('getAllMatches calls GET /matches and returns the list', async () => {
    mockApi.get.mockResolvedValueOnce({ data: [MATCH] })
    const result = await getAllMatches()
    expect(mockApi.get).toHaveBeenCalledWith('/matches')
    expect(result).toEqual([MATCH])
  })

  it('getMatchById calls GET /matches/:id', async () => {
    mockApi.get.mockResolvedValueOnce({ data: MATCH })
    const result = await getMatchById(1)
    expect(mockApi.get).toHaveBeenCalledWith('/matches/1')
    expect(result).toEqual(MATCH)
  })

  it('getScheduledMatches calls GET /matches/scheduled', async () => {
    mockApi.get.mockResolvedValueOnce({ data: [MATCH] })
    await getScheduledMatches()
    expect(mockApi.get).toHaveBeenCalledWith('/matches/scheduled')
  })

  it('getFinishedMatches calls GET /matches/finished', async () => {
    mockApi.get.mockResolvedValueOnce({ data: [] })
    await getFinishedMatches()
    expect(mockApi.get).toHaveBeenCalledWith('/matches/finished')
  })

  it('getMatchesByPhase calls GET /matches/phase/:phase', async () => {
    mockApi.get.mockResolvedValueOnce({ data: [MATCH] })
    await getMatchesByPhase('GROUP')
    expect(mockApi.get).toHaveBeenCalledWith('/matches/phase/GROUP')
  })

  it('getMatchesByTeam uppercases the code before calling the API', async () => {
    mockApi.get.mockResolvedValueOnce({ data: [MATCH] })
    await getMatchesByTeam('col')
    expect(mockApi.get).toHaveBeenCalledWith('/matches/team/COL')
  })

  it('getMatchesByRound encodes the round name in the URL', async () => {
    const round = 'Group A - Matchday 1'
    mockApi.get.mockResolvedValueOnce({ data: [MATCH] })
    await getMatchesByRound(round)
    expect(mockApi.get).toHaveBeenCalledWith(`/matches/round/${encodeURIComponent(round)}`)
  })

  it('loadMatchesFromApi calls POST /matches/load and returns the message', async () => {
    mockApi.post.mockResolvedValueOnce({ data: 'Partidos cargados' })
    const result = await loadMatchesFromApi()
    expect(mockApi.post).toHaveBeenCalledWith('/matches/load')
    expect(result).toBe('Partidos cargados')
  })

  it('getAllMatches propagates errors', async () => {
    mockApi.get.mockRejectedValueOnce(new Error('Network Error'))
    await expect(getAllMatches()).rejects.toThrow('Network Error')
  })

  it('getMatchById propagates 404 errors', async () => {
    mockApi.get.mockRejectedValueOnce({ response: { status: 404 } })
    await expect(getMatchById(999)).rejects.toMatchObject({ response: { status: 404 } })
  })
})
