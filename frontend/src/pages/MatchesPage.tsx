import { useEffect, useMemo, useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import { getAllMatches, loadMatchesFromApi } from '../services/matchService'

// Evita que dos montajes simultáneos lancen la carga al mismo tiempo
let loadInProgress = false
import { PHASE_LABELS, PHASE_ORDER, STATUS_LABELS } from '../types/matches'
import type { Match, MatchStatus, TournamentPhase } from '../types/matches'

type StatusFilter = 'ALL' | MatchStatus

const STATUS_FILTERS: { value: StatusFilter; label: string }[] = [
  { value: 'ALL', label: 'Todos' },
  { value: 'IN_PROGRESS', label: 'En Vivo' },
  { value: 'SCHEDULED', label: 'Programados' },
  { value: 'FINISHED', label: 'Finalizados' },
]

function formatDateTime(isoString: string): { date: string; time: string } {
  const d = new Date(isoString)
  return {
    date: d.toLocaleDateString('es-ES', { day: '2-digit', month: 'short', year: 'numeric' }),
    time: d.toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' }),
  }
}

function MatchCard({ match }: { match: Match }) {
  const { date, time } = formatDateTime(match.dateTime)
  const isLive = match.status === 'IN_PROGRESS'
  const isPlayed = match.status === 'FINISHED' || isLive

  return (
    <Link to={`/matches/${match.id}`} className="match-card">
      <div className="match-card-header">
        <span className="match-phase-badge">{PHASE_LABELS[match.phase]}</span>
        <span className={`match-status-badge status-${match.status.toLowerCase()}`}>
          {isLive && <span className="live-dot" />}
          {STATUS_LABELS[match.status]}
        </span>
      </div>

      <div className="match-teams">
        <div className="match-team">
          <img
            src={match.homeTeamFlag}
            alt={match.homeTeam}
            className="match-flag"
            loading="lazy"
            onError={(e) => { (e.target as HTMLImageElement).style.display = 'none' }}
          />
          <span className="match-team-name">{match.homeTeam}</span>
        </div>

        <div className="match-score">
          {isPlayed ? (
            <>
              <span className="score-num">{match.homeScore ?? 0}</span>
              <span className="score-sep">-</span>
              <span className="score-num">{match.awayScore ?? 0}</span>
            </>
          ) : (
            <span className="score-vs">VS</span>
          )}
        </div>

        <div className="match-team">
          <img
            src={match.awayTeamFlag}
            alt={match.awayTeam}
            className="match-flag"
            loading="lazy"
            onError={(e) => { (e.target as HTMLImageElement).style.display = 'none' }}
          />
          <span className="match-team-name">{match.awayTeam}</span>
        </div>
      </div>

      <div className="match-card-footer">
        <span className="match-meta-item">📅 {date} &mdash; {time}</span>
        <span className="match-meta-item">🏟 {match.stadium}</span>
        {match.roundName && (
          <span className="match-meta-item">📋 {match.roundName}</span>
        )}
      </div>
    </Link>
  )
}

export default function MatchesPage() {
  const [matches, setMatches] = useState<Match[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('ALL')
  const [phaseFilter, setPhaseFilter] = useState<TournamentPhase | 'ALL'>('ALL')

  const hasLoaded = useRef(false)

  async function fetchMatches() {
    setLoading(true)
    setError(null)
    try {
      const data = await getAllMatches()
      if (data.length === 0 && !loadInProgress) {
        loadInProgress = true
        try {
          await loadMatchesFromApi()
        } finally {
          loadInProgress = false
        }
        const loaded = await getAllMatches()
        setMatches(loaded)
      } else {
        setMatches(data)
      }
    } catch {
      setError('No se pudieron cargar los partidos. Verifica la conexión con el servidor.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (!hasLoaded.current) {
      hasLoaded.current = true
      fetchMatches()
    }
  }, [])

  const phases = useMemo<TournamentPhase[]>(
    () =>
      Array.from(new Set(matches.map((m) => m.phase))).sort(
        (a, b) => PHASE_ORDER[a] - PHASE_ORDER[b]
      ),
    [matches]
  )

  const filtered = useMemo(() => {
    return matches.filter((m) => {
      const statusOk = statusFilter === 'ALL' || m.status === statusFilter
      const phaseOk = phaseFilter === 'ALL' || m.phase === phaseFilter
      return statusOk && phaseOk
    })
  }, [matches, statusFilter, phaseFilter])

  const liveCount = useMemo(() => matches.filter((m) => m.status === 'IN_PROGRESS').length, [matches])

  return (
    <div className="matches-page">
      <div className="matches-header">
        <h1 className="page-title">Partidos</h1>
        <p className="page-subtitle">
          Copa Mundial FIFA 2026
          {!loading && matches.length > 0 && ` · ${matches.length} partidos`}
          {liveCount > 0 && (
            <span className="live-count-badge">
              <span className="live-dot" />
              {liveCount} en vivo
            </span>
          )}
        </p>
      </div>

      <div className="matches-filters">
        <div className="status-tabs">
          {STATUS_FILTERS.map((f) => (
            <button
              key={f.value}
              className={`status-tab${statusFilter === f.value ? ' active' : ''}`}
              onClick={() => setStatusFilter(f.value)}
            >
              {f.value === 'IN_PROGRESS' && <span className="live-dot" />}
              {f.label}
            </button>
          ))}
        </div>

        {phases.length > 0 && (
          <select
            className="phase-select"
            value={phaseFilter}
            onChange={(e) => setPhaseFilter(e.target.value as TournamentPhase | 'ALL')}
            aria-label="Filtrar por fase"
          >
            <option value="ALL">Todas las fases</option>
            {phases.map((p) => (
              <option key={p} value={p}>
                {PHASE_LABELS[p]}
              </option>
            ))}
          </select>
        )}
      </div>

      {loading && (
        <div className="matches-state">
          <div className="spinner" />
          <p>Cargando partidos...</p>
        </div>
      )}

      {!loading && error && (
        <div className="matches-state error">
          <p>{error}</p>
          <button
            className="btn-secondary"
            style={{ width: 'auto', padding: '10px 20px', marginTop: 0 }}
            onClick={fetchMatches}
          >
            Reintentar
          </button>
        </div>
      )}

      {!loading && !error && filtered.length === 0 && (
        <div className="matches-state">
          <p>No hay partidos para los filtros seleccionados.</p>
          <button
            className="btn-secondary"
            style={{ width: 'auto', padding: '8px 18px', marginTop: 0 }}
            onClick={() => { setStatusFilter('ALL'); setPhaseFilter('ALL') }}
          >
            Limpiar filtros
          </button>
        </div>
      )}

      {!loading && !error && filtered.length > 0 && (
        <div className="matches-grid">
          {filtered.map((m) => (
            <MatchCard key={m.id} match={m} />
          ))}
        </div>
      )}
    </div>
  )
}
