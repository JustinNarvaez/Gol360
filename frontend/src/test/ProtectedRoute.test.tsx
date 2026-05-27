import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter, Routes, Route } from 'react-router-dom'
import ProtectedRoute from '../components/ProtectedRoute'

vi.mock('../context/AuthContext', () => ({
  useAuth: vi.fn(),
}))

import { useAuth } from '../context/AuthContext'
const mockUseAuth = useAuth as ReturnType<typeof vi.fn>

describe('ProtectedRoute', () => {
  it('renders children when the user is authenticated', () => {
    mockUseAuth.mockReturnValue({ isAuthenticated: true, user: null, loginUser: vi.fn(), logoutUser: vi.fn() })

    render(
      <MemoryRouter>
        <ProtectedRoute>
          <div>Protected content</div>
        </ProtectedRoute>
      </MemoryRouter>
    )

    expect(screen.getByText('Protected content')).toBeInTheDocument()
  })

  it('redirects to /login when the user is not authenticated', () => {
    mockUseAuth.mockReturnValue({ isAuthenticated: false, user: null, loginUser: vi.fn(), logoutUser: vi.fn() })

    render(
      <MemoryRouter initialEntries={['/protected']}>
        <Routes>
          <Route path="/login" element={<div>Login page</div>} />
          <Route
            path="/protected"
            element={
              <ProtectedRoute>
                <div>Protected content</div>
              </ProtectedRoute>
            }
          />
        </Routes>
      </MemoryRouter>
    )

    expect(screen.queryByText('Protected content')).not.toBeInTheDocument()
    expect(screen.getByText('Login page')).toBeInTheDocument()
  })
})
