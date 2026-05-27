import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import RegisterPage from '../pages/RegisterPage'

const mockNavigate = vi.fn()

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')
  return { ...(actual as object), useNavigate: () => mockNavigate }
})

vi.mock('../context/AuthContext', () => ({
  useAuth: vi.fn(),
}))

vi.mock('../services/authService', () => ({
  register: vi.fn(),
}))

import { useAuth } from '../context/AuthContext'
import { register } from '../services/authService'

const mockUseAuth = useAuth as ReturnType<typeof vi.fn>
const mockRegister = register as ReturnType<typeof vi.fn>

const AUTH_RESPONSE = {
  token: 'fake-jwt',
  tokenType: 'Bearer',
  id: 2,
  userName: 'newuser',
  email: 'new@gol360.com',
  userType: 'LOCAL_FAN' as const,
}

function setup() {
  const loginUser = vi.fn()
  mockUseAuth.mockReturnValue({ isAuthenticated: false, loginUser, user: null, logoutUser: vi.fn() })
  render(<MemoryRouter><RegisterPage /></MemoryRouter>)
  return { loginUser }
}

async function fillStep1(user: ReturnType<typeof userEvent.setup>) {
  await user.type(screen.getByLabelText('Nombre'), 'Juan')
  await user.type(screen.getByLabelText('Apellido'), 'García')
  await user.type(screen.getByLabelText('Nombre de usuario'), 'newuser')
  await user.type(screen.getByLabelText('Email'), 'new@gol360.com')
  await user.type(screen.getByLabelText('Contraseña'), 'password123')
  await user.type(screen.getByLabelText('Confirmar'), 'password123')
  await user.click(screen.getByRole('button', { name: /Siguiente/i }))
}

beforeEach(() => {
  vi.clearAllMocks()
  mockNavigate.mockClear()
})

describe('RegisterPage — step 1', () => {
  it('renders the step 1 form fields', () => {
    setup()
    expect(screen.getByLabelText('Nombre')).toBeInTheDocument()
    expect(screen.getByLabelText('Apellido')).toBeInTheDocument()
    expect(screen.getByLabelText('Nombre de usuario')).toBeInTheDocument()
    expect(screen.getByLabelText('Email')).toBeInTheDocument()
    expect(screen.getByLabelText('Contraseña')).toBeInTheDocument()
    expect(screen.getByLabelText('Confirmar')).toBeInTheDocument()
  })

  it('shows fan type selector buttons', () => {
    setup()
    expect(screen.getByText('Fan Local')).toBeInTheDocument()
    expect(screen.getByText('Fan Viajero')).toBeInTheDocument()
  })

  it('shows validation errors when step 1 is submitted empty', async () => {
    const user = userEvent.setup()
    setup()
    await user.click(screen.getByRole('button', { name: /Siguiente/i }))
    expect(await screen.findAllByText('Requerido')).toHaveLength(3)
    expect(screen.getByText('Email inválido')).toBeInTheDocument()
    expect(screen.getByText('Mínimo 8 caracteres')).toBeInTheDocument()
  })

  it('shows password mismatch error', async () => {
    const user = userEvent.setup()
    setup()
    await user.type(screen.getByLabelText('Contraseña'), 'password123')
    await user.type(screen.getByLabelText('Confirmar'), 'different')
    await user.click(screen.getByRole('button', { name: /Siguiente/i }))
    expect(await screen.findByText('Las contraseñas no coinciden')).toBeInTheDocument()
  })

  it('redirects to /dashboard when user is already authenticated', () => {
    mockUseAuth.mockReturnValue({ isAuthenticated: true, user: null, loginUser: vi.fn(), logoutUser: vi.fn() })
    render(<MemoryRouter><RegisterPage /></MemoryRouter>)
    expect(screen.queryByLabelText('Nombre')).not.toBeInTheDocument()
  })
})

describe('RegisterPage — step 2', () => {
  it('advances to step 2 with valid step 1 data', async () => {
    const user = userEvent.setup()
    setup()
    await fillStep1(user)
    await waitFor(() => {
      expect(screen.getByText('Equipos favoritos')).toBeInTheDocument()
      expect(screen.getByText('Estadios favoritos')).toBeInTheDocument()
      expect(screen.getByText('Notificaciones')).toBeInTheDocument()
    })
  })

  it('renders team, stadium and notification options', async () => {
    const user = userEvent.setup()
    setup()
    await fillStep1(user)
    await waitFor(() => expect(screen.getByText('Colombia')).toBeInTheDocument())
    expect(screen.getByText('MetLife Stadium')).toBeInTheDocument()
    expect(screen.getByText('Gol')).toBeInTheDocument()
  })

  it('shows error when submitting without selecting preferences', async () => {
    const user = userEvent.setup()
    setup()
    await fillStep1(user)
    await waitFor(() => expect(screen.getByText('Equipos favoritos')).toBeInTheDocument())
    await user.click(screen.getByRole('button', { name: /Crear cuenta/i }))
    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Selecciona al menos 1 equipo, 1 estadio y 1 notificación'
    )
  })

  it('calls register and navigates to /dashboard on successful submit', async () => {
    const user = userEvent.setup()
    const { loginUser } = setup()
    mockRegister.mockResolvedValueOnce(AUTH_RESPONSE)

    await fillStep1(user)
    await waitFor(() => expect(screen.getByText('Colombia')).toBeInTheDocument())

    await user.click(screen.getByText('Colombia'))
    await user.click(screen.getByText('MetLife Stadium'))
    await user.click(screen.getByText('Gol'))

    await user.click(screen.getByRole('button', { name: /Crear cuenta/i }))

    await waitFor(() => {
      expect(mockRegister).toHaveBeenCalled()
      expect(loginUser).toHaveBeenCalled()
      expect(mockNavigate).toHaveBeenCalledWith('/dashboard', { replace: true })
    })
  })

  it('shows cities section only for VISIT_FAN type', async () => {
    const user = userEvent.setup()
    setup()

    await user.click(screen.getByText('Fan Viajero'))
    await fillStep1(user)

    await waitFor(() => expect(screen.getByText('Ciudades de interés')).toBeInTheDocument())
  })

  it('can go back to step 1 from step 2', async () => {
    const user = userEvent.setup()
    setup()
    await fillStep1(user)
    await waitFor(() => expect(screen.getByText('Equipos favoritos')).toBeInTheDocument())

    await user.click(screen.getByRole('button', { name: /Atrás/i }))

    expect(screen.getByLabelText('Nombre')).toBeInTheDocument()
  })
})
