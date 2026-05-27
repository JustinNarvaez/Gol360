import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import DashboardPage from '../pages/DashboardPage'

vi.mock('../context/AuthContext', () => ({
  useAuth: vi.fn(),
}))

vi.mock('../services/matchService', () => ({
  getAllMatches: vi.fn(),
}))

import { useAuth } from '../context/AuthContext'
import { getAllMatches } from '../services/matchService'

const mockUseAuth = useAuth as ReturnType<typeof vi.fn>
const mockGetAllMatches = getAllMatches as ReturnType<typeof vi.fn>

const MOCK_USER = { id: 1, userName: 'testuser', email: 'test@gol360.com', userType: 'LOCAL_FAN', token: 'jwt' }

const SCHEDULED_MATCH = {
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
}

beforeEach(() => {
  vi.clearAllMocks()
  mockUseAuth.mockReturnValue({ user: MOCK_USER, isAuthenticated: true, loginUser: vi.fn(), logoutUser: vi.fn() })
})

describe('DashboardPage', () => {
  it('shows the greeting with the logged-in username', async () => {
    mockGetAllMatches.mockResolvedValueOnce([])
    render(<MemoryRouter><DashboardPage /></MemoryRouter>)
    expect(screen.getByText('testuser')).toBeInTheDocument()
  })

  it('renders all four module cards', async () => {
    mockGetAllMatches.mockResolvedValueOnce([])
    render(<MemoryRouter><DashboardPage /></MemoryRouter>)
    expect(screen.getByText('Partidos')).toBeInTheDocument()
    expect(screen.getByText('Perfil')).toBeInTheDocument()
    expect(screen.getByText('Pollas')).toBeInTheDocument()
    expect(screen.getByText('Álbum')).toBeInTheDocument()
  })

  it('Partidos and Perfil cards are navigation links', async () => {
    mockGetAllMatches.mockResolvedValueOnce([])
    render(<MemoryRouter><DashboardPage /></MemoryRouter>)
    expect(screen.getByRole('link', { name: /Partidos/i })).toHaveAttribute('href', '/matches')
    expect(screen.getByRole('link', { name: /Perfil/i })).toHaveAttribute('href', '/profile')
  })

  it('shows upcoming scheduled and in-progress matches', async () => {
    mockGetAllMatches.mockResolvedValueOnce([SCHEDULED_MATCH])
    render(<MemoryRouter><DashboardPage /></MemoryRouter>)

    await waitFor(() => {
      expect(screen.getByText('COL')).toBeInTheDocument()
      expect(screen.getByText('BRA')).toBeInTheDocument()
    })
  })

  it('limits upcoming matches to 3', async () => {
    const many = Array.from({ length: 5 }, (_, i) => ({
      ...SCHEDULED_MATCH,
      id: i + 1,
      homeTeamCode: `H${i}`,
      awayTeamCode: `A${i}`,
    }))
    mockGetAllMatches.mockResolvedValueOnce(many)
    render(<MemoryRouter><DashboardPage /></MemoryRouter>)

    await waitFor(() => expect(screen.getByText('H0')).toBeInTheDocument())

    const cards = screen.getAllByText(/vs/i)
    expect(cards).toHaveLength(3)
  })

  it('does not render the upcoming section when no live or scheduled matches exist', async () => {
    const finished = { ...SCHEDULED_MATCH, status: 'FINISHED', homeScore: 1, awayScore: 0 }
    mockGetAllMatches.mockResolvedValueOnce([finished])
    render(<MemoryRouter><DashboardPage /></MemoryRouter>)

    await waitFor(() => {
      expect(screen.queryByText('Próximos Partidos')).not.toBeInTheDocument()
    })
  })

  it('silently ignores errors loading matches', async () => {
    mockGetAllMatches.mockRejectedValueOnce(new Error('Network'))
    render(<MemoryRouter><DashboardPage /></MemoryRouter>)

    await waitFor(() => {
      expect(screen.queryByText('Próximos Partidos')).not.toBeInTheDocument()
    })
    expect(screen.getByText('testuser')).toBeInTheDocument()
  })
})
