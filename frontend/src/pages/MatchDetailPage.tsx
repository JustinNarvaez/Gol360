import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { getMatchById } from '../services/matchService'
import { PHASE_LABELS, STATUS_LABELS } from '../types/matches'
import type { Match } from '../types/matches'

function formatFullDateTime(isoString: string): string {
  return new Date(isoString).toLocaleString('es-ES', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

export default function MatchDetailPage() {
  const { id } = useParams<{ id: string }>()
  const [match, setMatch] = useState<Match | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!id) return
    getMatchById(Number(id))
      .then(setMatch)
      .catch(() => setError('No se encontró el partido.'))
      .finally(() => setLoading(false))
  }, [id])

  if (loading) {
    return (
      <div className="detail-page">
        <div className="matches-state">
          <div className="spinner" />
          <p>Cargando partido...</p>
        </div>
      </div>
    )
  }

  if (error || !match) {
    return (
      <div className="detail-page">
        <div className="matches-state error">
          <p>{error ?? 'Partido no encontrado.'}</p>
          <Link to="/matches" className="btn-back" style={{ marginBottom: 0 }}>
            ← Volver a Partidos
          </Link>
        </div>
      </div>
    )
  }

  const isLive = match.status === 'IN_PROGRESS'
  const isPlayed = match.status === 'FINISHED' || isLive

  return (
    <div className="detail-page">
      <Link to="/matches" className="btn-back">
        ← Volver a Partidos
      </Link>

      <div className="detail-card">
        <div className="detail-header">
          <span className="match-phase-badge">{PHASE_LABELS[match.phase]}</span>
          <span className={`match-status-badge status-${match.status.toLowerCase()}`}>
            {isLive && <span className="live-dot" />}
            {STATUS_LABELS[match.status]}
          </span>
        </div>

        {match.roundName && (
          <p className="detail-round">{match.roundName}</p>
        )}

        <div className="detail-matchup">
          <div className="detail-team">
            <img
              src={match.homeTeamFlag}
              alt={match.homeTeam}
              className="detail-flag"
              onError={(e) => { (e.target as HTMLImageElement).style.display = 'none' }}
            />
            <span className="detail-team-name">{match.homeTeam}</span>
            <span className="detail-team-code">{match.homeTeamCode}</span>
          </div>

          <div className="detail-score">
            {isPlayed ? (
              <>
                <span className="detail-score-num">{match.homeScore ?? 0}</span>
                <span className="detail-score-sep">-</span>
                <span className="detail-score-num">{match.awayScore ?? 0}</span>
              </>
            ) : (
              <span className="detail-score-vs">VS</span>
            )}
          </div>

          <div className="detail-team">
            <img
              src={match.awayTeamFlag}
              alt={match.awayTeam}
              className="detail-flag"
              onError={(e) => { (e.target as HTMLImageElement).style.display = 'none' }}
            />
            <span className="detail-team-name">{match.awayTeam}</span>
            <span className="detail-team-code">{match.awayTeamCode}</span>
          </div>
        </div>

        <div className="detail-info">
          <div className="detail-info-row">
            <span className="detail-info-icon">📅</span>
            <span className="detail-info-text">{formatFullDateTime(match.dateTime)}</span>
          </div>
          <div className="detail-info-row">
            <span className="detail-info-icon">🏟</span>
            <span className="detail-info-text">{match.stadium}</span>
          </div>
          {match.result && (
            <div className="detail-info-row">
              <span className="detail-info-icon">🏆</span>
              <span className="detail-info-text">Resultado: {match.result}</span>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
