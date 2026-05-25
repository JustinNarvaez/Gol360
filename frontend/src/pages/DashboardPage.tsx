import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { getAllMatches } from '../services/matchService'
import { STATUS_LABELS } from '../types/matches'
import type { Match } from '../types/matches'

function formatMatchDate(iso: string): string {
  return new Date(iso).toLocaleDateString('es-ES', { day: '2-digit', month: 'short' })
}

export default function DashboardPage() {
  const { user } = useAuth()
  const [upcoming, setUpcoming] = useState<Match[]>([])

  useEffect(() => {
    getAllMatches()
      .then((all) => {
        setUpcoming(
          all.filter((m) => m.status === 'SCHEDULED' || m.status === 'IN_PROGRESS').slice(0, 3)
        )
      })
      .catch(() => {})
  }, [])

  return (
    <div className="dashboard-page">
      <div className="dashboard-hero">
        <h1 className="dashboard-greeting">
          Hola, <span>{user?.userName}</span>
        </h1>
        <p className="dashboard-subtitle">Copa Mundial FIFA 2026 — Tu panel de control</p>
      </div>

      <div className="dashboard-modules">
        <Link to="/matches" className="module-card">
          <span className="module-icon">⚽</span>
          <span className="module-title">Partidos</span>
          <span className="module-desc">Calendario y resultados</span>
        </Link>
        <div className="module-card disabled">
          <span className="module-icon">👤</span>
          <span className="module-title">Perfil</span>
          <span className="module-desc">Próximamente</span>
        </div>
        <div className="module-card disabled">
          <span className="module-icon">🏆</span>
          <span className="module-title">Pollas</span>
          <span className="module-desc">Próximamente</span>
        </div>
        <div className="module-card disabled">
          <span className="module-icon">📚</span>
          <span className="module-title">Álbum</span>
          <span className="module-desc">Próximamente</span>
        </div>
      </div>

      {upcoming.length > 0 && (
        <div className="dashboard-section">
          <div className="section-header">
            <h2 className="section-title">Próximos Partidos</h2>
            <Link to="/matches" className="section-link">Ver todos →</Link>
          </div>
          <div className="upcoming-matches">
            {upcoming.map((m) => (
              <Link key={m.id} to={`/matches/${m.id}`} className="upcoming-card">
                <div className="upcoming-teams">
                  <span className="upcoming-team">
                    <img
                      src={m.homeTeamFlag}
                      alt={m.homeTeam}
                      className="upcoming-flag"
                      onError={(e) => { (e.target as HTMLImageElement).style.display = 'none' }}
                    />
                    {m.homeTeamCode}
                  </span>
                  <span className="upcoming-sep">vs</span>
                  <span className="upcoming-team">
                    {m.awayTeamCode}
                    <img
                      src={m.awayTeamFlag}
                      alt={m.awayTeam}
                      className="upcoming-flag"
                      onError={(e) => { (e.target as HTMLImageElement).style.display = 'none' }}
                    />
                  </span>
                </div>
                <div className="upcoming-meta">
                  <span>{formatMatchDate(m.dateTime)}</span>
                  <span className={`match-status-badge status-${m.status.toLowerCase()}`} style={{ fontSize: '0.65rem', padding: '2px 7px' }}>
                    {m.status === 'IN_PROGRESS' && <span className="live-dot" />}
                    {STATUS_LABELS[m.status]}
                  </span>
                </div>
              </Link>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
