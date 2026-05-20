import { type FormEvent, useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { register } from '../services/authService'
import type { AuthUser, NotificationType, UserType } from '../types/auth'

/* ── Static seed data (matches backend data.sql insertion order) ── */
const TEAMS = [
  { id: 1, name: 'Colombia', flag: 'https://flagcdn.com/co.svg' },
  { id: 2, name: 'Brasil', flag: 'https://flagcdn.com/br.svg' },
  { id: 3, name: 'Argentina', flag: 'https://flagcdn.com/ar.svg' },
  { id: 4, name: 'Francia', flag: 'https://flagcdn.com/fr.svg' },
  { id: 5, name: 'España', flag: 'https://flagcdn.com/es.svg' },
  { id: 6, name: 'Alemania', flag: 'https://flagcdn.com/de.svg' },
  { id: 7, name: 'Inglaterra', flag: 'https://flagcdn.com/gb-eng.svg' },
  { id: 8, name: 'Portugal', flag: 'https://flagcdn.com/pt.svg' },
  { id: 9, name: 'México', flag: 'https://flagcdn.com/mx.svg' },
  { id: 10, name: 'USA', flag: 'https://flagcdn.com/us.svg' },
  { id: 11, name: 'Canadá', flag: 'https://flagcdn.com/ca.svg' },
  { id: 12, name: 'Marruecos', flag: 'https://flagcdn.com/ma.svg' },
  { id: 13, name: 'Japón', flag: 'https://flagcdn.com/jp.svg' },
  { id: 14, name: 'Senegal', flag: 'https://flagcdn.com/sn.svg' },
  { id: 15, name: 'Uruguay', flag: 'https://flagcdn.com/uy.svg' },
  { id: 16, name: 'Ecuador', flag: 'https://flagcdn.com/ec.svg' },
]

const STADIUMS = [
  { id: 1, name: 'MetLife Stadium', city: 'Nueva Jersey' },
  { id: 2, name: 'SoFi Stadium', city: 'Los Angeles' },
  { id: 3, name: 'AT&T Stadium', city: 'Dallas' },
  { id: 4, name: "Levi's Stadium", city: 'San Francisco' },
  { id: 5, name: 'Estadio Azteca', city: 'Ciudad de México' },
  { id: 6, name: 'Estadio Akron', city: 'Guadalajara' },
  { id: 7, name: 'Estadio BBVA', city: 'Monterrey' },
  { id: 8, name: 'BMO Field', city: 'Toronto' },
  { id: 9, name: 'BC Place', city: 'Vancouver' },
  { id: 10, name: 'Hard Rock Stadium', city: 'Miami' },
  { id: 11, name: 'Lincoln Financial', city: 'Filadelfia' },
  { id: 12, name: 'Gillette Stadium', city: 'Boston' },
]

const CITIES = [
  { id: 1, name: 'Nueva York' },
  { id: 2, name: 'Los Angeles' },
  { id: 3, name: 'Dallas' },
  { id: 4, name: 'Miami' },
  { id: 5, name: 'San Francisco' },
  { id: 6, name: 'Boston' },
  { id: 7, name: 'Filadelfia' },
  { id: 8, name: 'Seattle' },
  { id: 9, name: 'Ciudad de México' },
  { id: 10, name: 'Guadalajara' },
  { id: 11, name: 'Monterrey' },
  { id: 12, name: 'Toronto' },
  { id: 13, name: 'Vancouver' },
]

const NOTIFICATIONS: { value: NotificationType; label: string; icon: string }[] = [
  { value: 'GOAL', label: 'Gol', icon: '⚽' },
  { value: 'MATCH_START', label: 'Inicio', icon: '🏁' },
  { value: 'MATCH_END', label: 'Final', icon: '🔔' },
  { value: 'YELLOW_CARD', label: 'Amarilla', icon: '🟨' },
  { value: 'RED_CARD', label: 'Roja', icon: '🟥' },
  { value: 'SUBSTITUTION', label: 'Cambio', icon: '🔄' },
]

/* ── Step 1 form state ──────────────────────────────────────────── */
interface Step1Form {
  firstName: string
  lastName: string
  userName: string
  email: string
  password: string
  confirmPassword: string
  userType: UserType
}

interface Step1Errors {
  firstName?: string
  lastName?: string
  userName?: string
  email?: string
  password?: string
  confirmPassword?: string
}

function validateStep1(f: Step1Form): Step1Errors {
  const e: Step1Errors = {}
  if (!f.firstName.trim()) e.firstName = 'Requerido'
  if (!f.lastName.trim()) e.lastName = 'Requerido'
  if (!f.userName.trim()) e.userName = 'Requerido'
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(f.email)) e.email = 'Email inválido'
  if (f.password.length < 8) e.password = 'Mínimo 8 caracteres'
  if (f.password !== f.confirmPassword) e.confirmPassword = 'Las contraseñas no coinciden'
  return e
}

/* ── Toggle helper ──────────────────────────────────────────────── */
function toggle<T>(arr: T[], item: T): T[] {
  return arr.includes(item) ? arr.filter((x) => x !== item) : [...arr, item]
}

export default function RegisterPage() {
  const { isAuthenticated, loginUser } = useAuth()
  const navigate = useNavigate()

  const [step, setStep] = useState<1 | 2>(1)

  const [step1, setStep1] = useState<Step1Form>({
    firstName: '',
    lastName: '',
    userName: '',
    email: '',
    password: '',
    confirmPassword: '',
    userType: 'LOCAL_FAN',
  })
  const [step1Errors, setStep1Errors] = useState<Step1Errors>({})

  const [favNationIds, setFavNationIds] = useState<number[]>([])
  const [favStadiumIds, setFavStadiumIds] = useState<number[]>([])
  const [favNotifications, setFavNotifications] = useState<NotificationType[]>([])
  const [favCityIds, setFavCityIds] = useState<number[]>([])

  const [serverError, setServerError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  if (isAuthenticated) return <Navigate to="/dashboard" replace />

  function handleStep1Change(e: React.ChangeEvent<HTMLInputElement>) {
    const { name, value } = e.target
    setStep1((prev) => ({ ...prev, [name]: value }))
    setStep1Errors((prev) => ({ ...prev, [name]: undefined }))
  }

  function goToStep2(e: FormEvent) {
    e.preventDefault()
    const errors = validateStep1(step1)
    if (Object.keys(errors).length > 0) {
      setStep1Errors(errors)
      return
    }
    setStep(2)
    setServerError(null)
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    if (favNationIds.length === 0 || favStadiumIds.length === 0 || favNotifications.length === 0) {
      setServerError('Selecciona al menos 1 equipo, 1 estadio y 1 notificación.')
      return
    }

    setLoading(true)
    setServerError(null)
    try {
      const response = await register({
        firstName: step1.firstName,
        lastName: step1.lastName,
        userName: step1.userName,
        email: step1.email,
        password: step1.password,
        userType: step1.userType,
        preferences: {
          favNationIds,
          favStadiumIds,
          favNotifications,
          ...(step1.userType === 'VISIT_FAN' && { favCityIds }),
        },
      })
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
      const axiosErr = err as { response?: { status?: number; data?: { message?: string } } }
      if (axiosErr.response?.status === 409) {
        setServerError('El email o nombre de usuario ya está registrado.')
      } else if (axiosErr.response?.data?.message) {
        setServerError(axiosErr.response.data.message)
      } else {
        setServerError('Error al crear la cuenta. Intenta de nuevo.')
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="auth-page">
      <div className={`auth-card${step === 2 ? ' wide' : ''}`}>
        {/* Logo */}
        <div className="logo-section">
          <h1 className="logo">GOL360</h1>
          <p className="logo-subtitle">Mundial FIFA 2026</p>
        </div>

        {/* Step indicator */}
        <div className="step-indicator" aria-label="Paso del formulario">
          <div className={`step-dot ${step >= 1 ? 'active' : ''} ${step > 1 ? 'done' : ''}`}>
            {step > 1 ? '✓' : '1'}
          </div>
          <div className={`step-line ${step > 1 ? 'done' : ''}`} />
          <div className={`step-dot ${step === 2 ? 'active' : ''}`}>2</div>
        </div>

        {/* Error banner */}
        {serverError && <div className="error-banner" role="alert">{serverError}</div>}

        {/* ── STEP 1: Basic info ──────────────────────────────────── */}
        {step === 1 && (
          <form onSubmit={goToStep2} noValidate>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0 12px' }}>
              <div className="form-group">
                <label className="form-label" htmlFor="firstName">Nombre</label>
                <input
                  id="firstName" name="firstName" type="text"
                  placeholder="Juan"
                  className={`form-input${step1Errors.firstName ? ' error' : ''}`}
                  value={step1.firstName} onChange={handleStep1Change}
                />
                {step1Errors.firstName && <p className="field-error">{step1Errors.firstName}</p>}
              </div>
              <div className="form-group">
                <label className="form-label" htmlFor="lastName">Apellido</label>
                <input
                  id="lastName" name="lastName" type="text"
                  placeholder="García"
                  className={`form-input${step1Errors.lastName ? ' error' : ''}`}
                  value={step1.lastName} onChange={handleStep1Change}
                />
                {step1Errors.lastName && <p className="field-error">{step1Errors.lastName}</p>}
              </div>
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="userName">Nombre de usuario</label>
              <input
                id="userName" name="userName" type="text"
                autoComplete="username"
                placeholder="juangarcia10"
                className={`form-input${step1Errors.userName ? ' error' : ''}`}
                value={step1.userName} onChange={handleStep1Change}
              />
              {step1Errors.userName && <p className="field-error">{step1Errors.userName}</p>}
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="email">Email</label>
              <input
                id="email" name="email" type="email"
                autoComplete="email"
                placeholder="tu@email.com"
                className={`form-input${step1Errors.email ? ' error' : ''}`}
                value={step1.email} onChange={handleStep1Change}
              />
              {step1Errors.email && <p className="field-error">{step1Errors.email}</p>}
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0 12px' }}>
              <div className="form-group">
                <label className="form-label" htmlFor="password">Contraseña</label>
                <input
                  id="password" name="password" type="password"
                  autoComplete="new-password"
                  placeholder="••••••••"
                  className={`form-input${step1Errors.password ? ' error' : ''}`}
                  value={step1.password} onChange={handleStep1Change}
                />
                {step1Errors.password && <p className="field-error">{step1Errors.password}</p>}
              </div>
              <div className="form-group">
                <label className="form-label" htmlFor="confirmPassword">Confirmar</label>
                <input
                  id="confirmPassword" name="confirmPassword" type="password"
                  autoComplete="new-password"
                  placeholder="••••••••"
                  className={`form-input${step1Errors.confirmPassword ? ' error' : ''}`}
                  value={step1.confirmPassword} onChange={handleStep1Change}
                />
                {step1Errors.confirmPassword && <p className="field-error">{step1Errors.confirmPassword}</p>}
              </div>
            </div>

            <div className="form-group">
              <p className="form-label">Tipo de fan</p>
              <div className="user-type-group">
                <button
                  type="button"
                  className={`user-type-option${step1.userType === 'LOCAL_FAN' ? ' selected' : ''}`}
                  onClick={() => setStep1((p) => ({ ...p, userType: 'LOCAL_FAN' }))}
                >
                  <span className="user-type-icon">🏠</span>
                  <span className="user-type-label">Fan Local</span>
                </button>
                <button
                  type="button"
                  className={`user-type-option${step1.userType === 'VISIT_FAN' ? ' selected' : ''}`}
                  onClick={() => setStep1((p) => ({ ...p, userType: 'VISIT_FAN' }))}
                >
                  <span className="user-type-icon">✈️</span>
                  <span className="user-type-label">Fan Viajero</span>
                </button>
              </div>
            </div>

            <button type="submit" className="btn-primary">
              Siguiente →
            </button>

            <div className="auth-footer">
              ¿Ya tienes cuenta?{' '}
              <Link to="/login" className="auth-link">Iniciar sesión</Link>
            </div>
          </form>
        )}

        {/* ── STEP 2: Preferences ─────────────────────────────────── */}
        {step === 2 && (
          <form onSubmit={handleSubmit} noValidate>
            <div className="pref-panel">
              {/* Teams */}
              <p className="pref-section-title">
                Equipos favoritos
                {favNationIds.length > 0 && (
                  <span className="selection-badge">{favNationIds.length}</span>
                )}
              </p>
              <div className="teams-grid">
                {TEAMS.map((team) => (
                  <button
                    key={team.id}
                    type="button"
                    className={`team-card${favNationIds.includes(team.id) ? ' selected' : ''}`}
                    onClick={() => setFavNationIds((prev) => toggle(prev, team.id))}
                  >
                    <img
                      src={team.flag}
                      alt={team.name}
                      className="team-flag"
                      loading="lazy"
                    />
                    <span className="team-name">{team.name}</span>
                  </button>
                ))}
              </div>

              <div className="form-divider" />

              {/* Stadiums */}
              <p className="pref-section-title">
                Estadios favoritos
                {favStadiumIds.length > 0 && (
                  <span className="selection-badge">{favStadiumIds.length}</span>
                )}
              </p>
              <div className="stadiums-grid">
                {STADIUMS.map((s) => (
                  <button
                    key={s.id}
                    type="button"
                    className={`stadium-card${favStadiumIds.includes(s.id) ? ' selected' : ''}`}
                    onClick={() => setFavStadiumIds((prev) => toggle(prev, s.id))}
                  >
                    <p className="stadium-card-name">{s.name}</p>
                    <p className="stadium-card-city">{s.city}</p>
                  </button>
                ))}
              </div>

              <div className="form-divider" />

              {/* Notifications */}
              <p className="pref-section-title">
                Notificaciones
                {favNotifications.length > 0 && (
                  <span className="selection-badge">{favNotifications.length}</span>
                )}
              </p>
              <div className="notif-grid">
                {NOTIFICATIONS.map((n) => (
                  <button
                    key={n.value}
                    type="button"
                    className={`notif-pill${favNotifications.includes(n.value) ? ' selected' : ''}`}
                    onClick={() =>
                      setFavNotifications((prev) => toggle(prev, n.value))
                    }
                  >
                    <span>{n.icon}</span>
                    <span>{n.label}</span>
                  </button>
                ))}
              </div>

              {/* Cities — only for VISIT_FAN */}
              {step1.userType === 'VISIT_FAN' && (
                <>
                  <div className="form-divider" />
                  <p className="pref-section-title">
                    Ciudades de interés
                    {favCityIds.length > 0 && (
                      <span className="selection-badge">{favCityIds.length}</span>
                    )}
                  </p>
                  <div className="cities-grid">
                    {CITIES.map((c) => (
                      <button
                        key={c.id}
                        type="button"
                        className={`city-pill${favCityIds.includes(c.id) ? ' selected' : ''}`}
                        onClick={() => setFavCityIds((prev) => toggle(prev, c.id))}
                      >
                        {c.name}
                      </button>
                    ))}
                  </div>
                </>
              )}
            </div>

            <div className="step-nav">
              <button
                type="button"
                className="btn-secondary"
                onClick={() => { setStep(1); setServerError(null) }}
                disabled={loading}
              >
                ← Atrás
              </button>
              <button type="submit" className="btn-primary" disabled={loading}>
                {loading ? 'Creando cuenta...' : 'Crear cuenta'}
              </button>
            </div>
          </form>
        )}
      </div>
    </main>
  )
}
