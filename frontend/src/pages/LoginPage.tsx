import { type FormEvent, useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { login } from '../services/authService'
import type { AuthUser } from '../types/auth'

interface FormState {
  identifier: string
  password: string
}

interface FormErrors {
  identifier?: string
  password?: string
}

function validate(form: FormState): FormErrors {
  const errors: FormErrors = {}
  if (!form.identifier.trim()) errors.identifier = 'Ingresa tu email o usuario'
  if (!form.password) errors.password = 'Ingresa tu contraseña'
  return errors
}

export default function LoginPage() {
  const { isAuthenticated, loginUser } = useAuth()
  const navigate = useNavigate()

  const [form, setForm] = useState<FormState>({ identifier: '', password: '' })
  const [errors, setErrors] = useState<FormErrors>({})
  const [serverError, setServerError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  if (isAuthenticated) return <Navigate to="/dashboard" replace />

  function handleChange(e: React.ChangeEvent<HTMLInputElement>) {
    const { name, value } = e.target
    setForm((prev) => ({ ...prev, [name]: value }))
    setErrors((prev) => ({ ...prev, [name]: undefined }))
    setServerError(null)
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    const validationErrors = validate(form)
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors)
      return
    }

    setLoading(true)
    setServerError(null)
    try {
      const response = await login({ identifier: form.identifier, password: form.password })
      const user: AuthUser = {
        id: response.id,
        userName: response.userName,
        email: response.email,
        userType: response.userType,
        token: response.token,
      }
      loginUser(user)
      navigate('/dashboard', { replace: true })
    } catch (err: unknown) {
      const axiosErr = err as { response?: { status?: number; data?: unknown } }
      if (axiosErr.response?.status === 401 || axiosErr.response?.status === 403) {
        setServerError('Credenciales incorrectas. Verifica tu email/usuario y contraseña.')
      } else if (axiosErr.response?.status === 404) {
        setServerError('Usuario no encontrado.')
      } else {
        setServerError('Error al conectar con el servidor. Intenta de nuevo.')
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="auth-page">
      <div className="auth-card">
        {/* Logo */}
        <div className="logo-section">
          <h1 className="logo">GOL360</h1>
          <p className="logo-subtitle">Mundial FIFA 2026</p>
        </div>

        {/* Error banner */}
        {serverError && <div className="error-banner" role="alert">{serverError}</div>}

        <form onSubmit={handleSubmit} noValidate>
          <div className="form-group">
            <label className="form-label" htmlFor="identifier">
              Email o Usuario
            </label>
            <input
              id="identifier"
              name="identifier"
              type="text"
              autoComplete="username"
              placeholder="tu@email.com"
              className={`form-input${errors.identifier ? ' error' : ''}`}
              value={form.identifier}
              onChange={handleChange}
              disabled={loading}
            />
            {errors.identifier && (
              <p className="field-error" role="alert">{errors.identifier}</p>
            )}
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="password">
              Contraseña
            </label>
            <input
              id="password"
              name="password"
              type="password"
              autoComplete="current-password"
              placeholder="••••••••"
              className={`form-input${errors.password ? ' error' : ''}`}
              value={form.password}
              onChange={handleChange}
              disabled={loading}
            />
            {errors.password && (
              <p className="field-error" role="alert">{errors.password}</p>
            )}
          </div>

          <button type="submit" className="btn-primary" disabled={loading}>
            {loading ? 'Iniciando sesión...' : 'Iniciar Sesión'}
          </button>
        </form>

        <div className="auth-footer">
          ¿No tienes cuenta?{' '}
          <Link to="/register" className="auth-link">
            Crear cuenta
          </Link>
        </div>
      </div>
    </main>
  )
}
