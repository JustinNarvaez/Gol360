import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Navbar() {
  const { user, logoutUser } = useAuth()
  const navigate = useNavigate()

  async function handleLogout() {
    await logoutUser()
    navigate('/login', { replace: true })
  }

  return (
    <nav className="navbar">
      <Link to="/dashboard" className="navbar-logo">
        GOL<span>360</span>
      </Link>

      <div className="navbar-links">
        <NavLink
          to="/dashboard"
          end
          className={({ isActive }) => `navbar-link${isActive ? ' active' : ''}`}
        >
          Inicio
        </NavLink>
        <NavLink
          to="/matches"
          className={({ isActive }) => `navbar-link${isActive ? ' active' : ''}`}
        >
          Partidos
        </NavLink>
      </div>

      <div className="navbar-user">
        <span className="navbar-username">{user?.userName}</span>
        <button className="btn-nav-logout" onClick={handleLogout}>
          Salir
        </button>
      </div>
    </nav>
  )
}
