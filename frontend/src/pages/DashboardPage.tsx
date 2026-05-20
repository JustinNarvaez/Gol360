import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function DashboardPage() {
  const { user, logoutUser } = useAuth()
  const navigate = useNavigate()

  async function handleLogout() {
    await logoutUser()
    navigate('/login', { replace: true })
  }

  return (
    <main className="dashboard-page">
      <div className="stadium-bg" aria-hidden />
      <h1 className="dashboard-greeting">
        Bienvenido, <span>{user?.userName}</span>
      </h1>
      <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
        Panel principal — próximamente más funcionalidades
      </p>
      <button
        className="btn-primary"
        style={{ maxWidth: 200, marginTop: 12 }}
        onClick={handleLogout}
      >
        Cerrar sesión
      </button>
    </main>
  )
}
