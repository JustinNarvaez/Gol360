import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import Navbar from '../components/Navbar'

const mockNavigate = vi.fn()

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')
  return { ...(actual as object), useNavigate: () => mockNavigate }
})

vi.mock('../context/AuthContext', () => ({
  useAuth: vi.fn(),
}))

import { useAuth } from '../context/AuthContext'
const mockUseAuth = useAuth as ReturnType<typeof vi.fn>

const MOCK_USER = { id: 1, userName: 'testuser', email: 'test@gol360.com', userType: 'LOCAL_FAN', token: 'jwt' }

beforeEach(() => {
  mockNavigate.mockClear()
  mockUseAuth.mockReturnValue({
    user: MOCK_USER,
    isAuthenticated: true,
    logoutUser: vi.fn().mockResolvedValue(undefined),
    loginUser: vi.fn(),
  })
})

describe('Navbar', () => {
  it('renders the GOL360 logo', () => {
    render(<MemoryRouter><Navbar /></MemoryRouter>)
    expect(screen.getByText('GOL', { exact: false })).toBeInTheDocument()
  })

  it('renders the logged-in username', () => {
    render(<MemoryRouter><Navbar /></MemoryRouter>)
    expect(screen.getByText('testuser')).toBeInTheDocument()
  })

  it('renders navigation links for Inicio, Partidos and Perfil', () => {
    render(<MemoryRouter><Navbar /></MemoryRouter>)
    expect(screen.getByRole('link', { name: 'Inicio' })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Partidos' })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Perfil' })).toBeInTheDocument()
  })

  it('calls logoutUser and navigates to /login when clicking Salir', async () => {
    const user = userEvent.setup()
    const logoutUser = vi.fn().mockResolvedValue(undefined)
    mockUseAuth.mockReturnValue({ user: MOCK_USER, isAuthenticated: true, logoutUser, loginUser: vi.fn() })
    render(<MemoryRouter><Navbar /></MemoryRouter>)

    await user.click(screen.getByRole('button', { name: 'Salir' }))

    expect(logoutUser).toHaveBeenCalled()
    expect(mockNavigate).toHaveBeenCalledWith('/login', { replace: true })
  })
})
