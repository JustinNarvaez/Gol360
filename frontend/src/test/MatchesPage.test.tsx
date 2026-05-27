import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import MatchesPage from '../pages/MatchesPage'
import type { Match } from '../types/matches'

vi.mock('../services/matchService', () => ({
  getAllMatches: vi.fn(),
  loadMatchesFromApi: vi.fn(),
}))

import { getAllMatches, loadMatchesFromApi } from '../services/matchService'
const mockGetAllMatches = getAllMatches as ReturnType<typeof vi.fn>
const mockLoadMatchesFromApi = loadMatchesFromApi as ReturnType<typeof vi.fn>

function makeMatch(overrides: Partial<Match> = {}): Match {
  return {
    id: 1,
    dateTime: '2026-06-11T18:00:00',
    roundName: 'Group A - Matchday 1',
    phase: 'GROUP',
    status: 'SCHEDULED',
    homeTeam: 'Colombia',
    homeTeamCode: 'COL',
    homeTeamFlag: '',
    awayTeam: 'Brasil',
    awayTeamCode: 'BRA',
    awayTeamFlag: '',
    homeScore: null,
    awayScore: null,
    result: null,
    stadium: 'MetLife Stadium',
    refreshed: false,
    ...overrides,
  }
}

beforeEach(() => {
  vi.clearAllMocks()
  mockLoadMatchesFromApi.mockResolvedValue('ok')
})

describe('MatchesPage', () => {
  it('shows a loading spinner while fetching', () => {
    mockGetAllMatches.mockReturnValueOnce(new Promise(() => {}))
    render(<MemoryRouter><MatchesPage /></MemoryRouter>)
    expect(screen.getByText('Cargando partidos...')).toBeInTheDocument()
  })

  it('renders match cards after loading', async () => {
    mockGetAllMatches.mockResolvedValueOnce([makeMatch()])
    render(<MemoryRouter><MatchesPage /></MemoryRouter>)

    await waitFor(() => {
      expect(screen.getByText('Colombia')).toBeInTheDocument()
      expect(screen.getByText('Brasil')).toBeInTheDocument()
    })
  })

  it('shows total match count in the subtitle', async () => {
    mockGetAllMatches.mockResolvedValueOnce([makeMatch({ id: 1 }), makeMatch({ id: 2 })])
    render(<MemoryRouter><MatchesPage /></MemoryRouter>)

    await waitFor(() => {
      expect(screen.getByText(/2 partidos/)).toBeInTheDocument()
    })
  })

  it('shows an error message when the API call fails', async () => {
    mockGetAllMatches.mockRejectedValueOnce(new Error('Network error'))
    render(<MemoryRouter><MatchesPage /></MemoryRouter>)

    await waitFor(() => {
      expect(screen.getByText(/No se pudieron cargar los partidos/)).toBeInTheDocument()
    })
  })

  it('shows a retry button on error', async () => {
    mockGetAllMatches.mockRejectedValueOnce(new Error('fail'))
    render(<MemoryRouter><MatchesPage /></MemoryRouter>)

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Reintentar' })).toBeInTheDocument()
    })
  })

  it('filters matches by team name search (case-insensitive)', async () => {
    const user = userEvent.setup()
    mockGetAllMatches.mockResolvedValueOnce([
      makeMatch({ id: 1, homeTeam: 'Colombia', homeTeamCode: 'COL', awayTeam: 'Brasil', awayTeamCode: 'BRA' }),
      makeMatch({ id: 2, homeTeam: 'Francia', homeTeamCode: 'FRA', awayTeam: 'Alemania', awayTeamCode: 'GER' }),
    ])
    render(<MemoryRouter><MatchesPage /></MemoryRouter>)
    await waitFor(() => expect(screen.getByText('Colombia')).toBeInTheDocument())

    await user.type(screen.getByPlaceholderText('Buscar equipo...'), 'col')

    await waitFor(() => {
      expect(screen.getByText('Colombia')).toBeInTheDocument()
      expect(screen.queryByText('Francia')).not.toBeInTheDocument()
    })
  })

  it('filters matches by status tab', async () => {
    const user = userEvent.setup()
    mockGetAllMatches.mockResolvedValueOnce([
      makeMatch({ id: 1, status: 'SCHEDULED', homeTeam: 'Colombia', awayTeam: 'Brasil' }),
      makeMatch({ id: 2, status: 'FINISHED', homeTeam: 'Francia', awayTeam: 'España', homeScore: 1, awayScore: 0 }),
    ])
    render(<MemoryRouter><MatchesPage /></MemoryRouter>)
    await waitFor(() => expect(screen.getByText('Colombia')).toBeInTheDocument())

    await user.click(screen.getByRole('button', { name: 'Finalizados' }))

    await waitFor(() => {
      expect(screen.queryByText('Colombia')).not.toBeInTheDocument()
      expect(screen.getByText('Francia')).toBeInTheDocument()
    })
  })

  it('shows the "no matches" state when filters yield no results', async () => {
    const user = userEvent.setup()
    mockGetAllMatches.mockResolvedValueOnce([makeMatch()])
    render(<MemoryRouter><MatchesPage /></MemoryRouter>)
    await waitFor(() => expect(screen.getByText('Colombia')).toBeInTheDocument())

    await user.type(screen.getByPlaceholderText('Buscar equipo...'), 'xyz_nomatch')

    await waitFor(() => {
      expect(screen.getByText(/No hay partidos para los filtros seleccionados/)).toBeInTheDocument()
    })
  })

  it('shows a "clear filters" button when filters are active and no results', async () => {
    const user = userEvent.setup()
    mockGetAllMatches.mockResolvedValueOnce([makeMatch()])
    render(<MemoryRouter><MatchesPage /></MemoryRouter>)
    await waitFor(() => expect(screen.getByText('Colombia')).toBeInTheDocument())

    await user.type(screen.getByPlaceholderText('Buscar equipo...'), 'xyz_nomatch')

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Limpiar filtros' })).toBeInTheDocument()
    })
  })

  it('clears all filters when clicking "Limpiar filtros"', async () => {
    const user = userEvent.setup()
    mockGetAllMatches.mockResolvedValueOnce([makeMatch()])
    render(<MemoryRouter><MatchesPage /></MemoryRouter>)
    await waitFor(() => expect(screen.getByText('Colombia')).toBeInTheDocument())

    await user.type(screen.getByPlaceholderText('Buscar equipo...'), 'xyz')
    await waitFor(() => expect(screen.getByRole('button', { name: 'Limpiar filtros' })).toBeInTheDocument())

    await user.click(screen.getByRole('button', { name: 'Limpiar filtros' }))

    await waitFor(() => {
      expect(screen.getByText('Colombia')).toBeInTheDocument()
    })
  })
})
