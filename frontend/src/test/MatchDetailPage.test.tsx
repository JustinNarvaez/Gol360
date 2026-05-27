import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import MatchDetailPage from '../pages/MatchDetailPage'

vi.mock('../services/matchService', () => ({
  getMatchById: vi.fn(),
}))

import { getMatchById } from '../services/matchService'
const mockGetMatchById = getMatchById as ReturnType<typeof vi.fn>

const FINISHED_MATCH = {
  id: 1,
  dateTime: '2026-06-11T18:00:00',
  roundName: 'Group A - Matchday 1',
  phase: 'GROUP' as const,
  status: 'FINISHED' as const,
  homeTeam: 'Colombia',
  homeTeamCode: 'COL',
  homeTeamFlag: '',
  awayTeam: 'Brasil',
  awayTeamCode: 'BRA',
  awayTeamFlag: '',
  homeScore: 2,
  awayScore: 1,
  result: '2-1',
  stadium: 'MetLife Stadium',
  refreshed: true,
}

function renderDetailPage(matchId = '1') {
  render(
    <MemoryRouter initialEntries={[`/matches/${matchId}`]}>
      <Routes>
        <Route path="/matches/:id" element={<MatchDetailPage />} />
      </Routes>
    </MemoryRouter>
  )
}

beforeEach(() => vi.clearAllMocks())

describe('MatchDetailPage', () => {
  it('shows a loading spinner initially', () => {
    mockGetMatchById.mockReturnValueOnce(new Promise(() => {}))
    renderDetailPage()
    expect(screen.getByText('Cargando partido...')).toBeInTheDocument()
  })

  it('calls getMatchById with the numeric id from the URL', async () => {
    mockGetMatchById.mockResolvedValueOnce(FINISHED_MATCH)
    renderDetailPage('42')
    await waitFor(() => expect(mockGetMatchById).toHaveBeenCalledWith(42))
  })

  it('displays team names and stadium after loading', async () => {
    mockGetMatchById.mockResolvedValueOnce(FINISHED_MATCH)
    renderDetailPage()

    await waitFor(() => {
      expect(screen.getByText('Colombia')).toBeInTheDocument()
      expect(screen.getByText('Brasil')).toBeInTheDocument()
      expect(screen.getByText('MetLife Stadium')).toBeInTheDocument()
    })
  })

  it('shows the score for a finished match', async () => {
    mockGetMatchById.mockResolvedValueOnce(FINISHED_MATCH)
    renderDetailPage()

    await waitFor(() => {
      const scoreNums = screen.getAllByText(/^[012]$/)
      expect(scoreNums.length).toBeGreaterThanOrEqual(2)
    })
  })

  it('shows VS for a scheduled match instead of a score', async () => {
    const scheduled = { ...FINISHED_MATCH, status: 'SCHEDULED' as const, homeScore: null, awayScore: null, result: null }
    mockGetMatchById.mockResolvedValueOnce(scheduled)
    renderDetailPage()

    await waitFor(() => {
      expect(screen.getByText('VS')).toBeInTheDocument()
    })
  })

  it('shows result text when available', async () => {
    mockGetMatchById.mockResolvedValueOnce(FINISHED_MATCH)
    renderDetailPage()

    await waitFor(() => {
      expect(screen.getByText(/Resultado: 2-1/)).toBeInTheDocument()
    })
  })

  it('shows error message when the API call fails', async () => {
    mockGetMatchById.mockRejectedValueOnce(new Error('Not found'))
    renderDetailPage()

    await waitFor(() => {
      expect(screen.getByText('No se encontró el partido.')).toBeInTheDocument()
    })
  })

  it('shows a back link to the matches list', async () => {
    mockGetMatchById.mockResolvedValueOnce(FINISHED_MATCH)
    renderDetailPage()

    await waitFor(() => {
      expect(screen.getByRole('link', { name: /Volver a Partidos/i })).toBeInTheDocument()
    })
  })
})
