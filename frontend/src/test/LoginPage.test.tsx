import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import LoginPage from '../pages/LoginPage'

const mockNavigate = vi.fn()

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')
  return { ...(actual as object), useNavigate: () => mockNavigate }
})

vi.mock('../context/AuthContext', () => ({
  useAuth: vi.fn(),
}))

vi.mock('../services/authService', () => ({
  login: vi.fn(),
}))

import { useAuth } from '../context/AuthContext'
import { login } from '../services/authService'

const mockUseAuth = useAuth as ReturnType<typeof vi.fn>
const mockLogin = login as ReturnType<typeof vi.fn>

const AUTH_RESPONSE = {
  token: 'fake-jwt',
  tokenType: 'Bearer',
  id: 1,
  userName: 'testuser',
  email: 'test@gol360.com',
  userType: 'LOCAL_FAN' as const,
}

function setup(isAuthenticated = false) {
  const loginUser = vi.fn()
  mockUseAuth.mockReturnValue({ isAuthenticated, loginUser, user: null, logoutUser: vi.fn() })
  render(<MemoryRouter><LoginPage /></MemoryRouter>)
  return { loginUser }
}

beforeEach(() => {
  vi.clearAllMocks()
  mockNavigate.mockClear()
})

describe('LoginPage', () => {
  it('renders the login form with identifier and password fields', () => {
    setup()
    expect(screen.getByLabelText('Email o Usuario')).toBeInTheDocument()
    expect(screen.getByLabelText('Contraseña')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Iniciar Sesión' })).toBeInTheDocument()
  })

  it('shows a link to the register page', () => {
    setup()
    expect(screen.getByRole('link', { name: 'Crear cuenta' })).toBeInTheDocument()
  })

  it('redirects to /dashboard when the user is already authenticated', () => {
    setup(true)
    expect(screen.queryByLabelText('Email o Usuario')).not.toBeInTheDocument()
  })

  it('shows field errors when submitting an empty form', async () => {
    const user = userEvent.setup()
    setup()
    await user.click(screen.getByRole('button', { name: 'Iniciar Sesión' }))
    expect(await screen.findByText('Ingresa tu email o usuario')).toBeInTheDocument()
    expect(screen.getByText('Ingresa tu contraseña')).toBeInTheDocument()
    expect(mockLogin).not.toHaveBeenCalled()
  })

  it('calls login with identifier and password on valid submission', async () => {
    const user = userEvent.setup()
    const { loginUser } = setup()
    mockLogin.mockResolvedValueOnce(AUTH_RESPONSE)

    await user.type(screen.getByLabelText('Email o Usuario'), 'testuser')
    await user.type(screen.getByLabelText('Contraseña'), 'password123')
    await user.click(screen.getByRole('button', { name: 'Iniciar Sesión' }))

    await waitFor(() => {
      expect(mockLogin).toHaveBeenCalledWith({ identifier: 'testuser', password: 'password123' })
      expect(loginUser).toHaveBeenCalledWith({
        id: 1,
        userName: 'testuser',
        email: 'test@gol360.com',
        userType: 'LOCAL_FAN',
        token: 'fake-jwt',
      })
      expect(mockNavigate).toHaveBeenCalledWith('/dashboard', { replace: true })
    })
  })

  it('shows an error message on 401 response', async () => {
    const user = userEvent.setup()
    setup()
    mockLogin.mockRejectedValueOnce({ response: { status: 401 } })

    await user.type(screen.getByLabelText('Email o Usuario'), 'bad')
    await user.type(screen.getByLabelText('Contraseña'), 'bad')
    await user.click(screen.getByRole('button', { name: 'Iniciar Sesión' }))

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Usuario o contraseña incorrectos'
    )
  })

  it('shows a connection error when there is no HTTP response', async () => {
    const user = userEvent.setup()
    setup()
    mockLogin.mockRejectedValueOnce({})

    await user.type(screen.getByLabelText('Email o Usuario'), 'user')
    await user.type(screen.getByLabelText('Contraseña'), 'pass')
    await user.click(screen.getByRole('button', { name: 'Iniciar Sesión' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('No se pudo conectar')
  })

  it('disables the submit button while the request is in flight', async () => {
    const user = userEvent.setup()
    setup()
    let resolve: (v: unknown) => void
    mockLogin.mockReturnValueOnce(new Promise((r) => { resolve = r }))

    await user.type(screen.getByLabelText('Email o Usuario'), 'user')
    await user.type(screen.getByLabelText('Contraseña'), 'pass')

    const submitPromise = user.click(screen.getByRole('button', { name: 'Iniciar Sesión' }))

    expect(await screen.findByRole('button', { name: 'Iniciando sesión...' })).toBeDisabled()

    resolve!(AUTH_RESPONSE)
    await submitPromise
  })
})
