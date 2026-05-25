export type TournamentPhase =
  | 'GROUP'
  | 'ROUND_OF_16'
  | 'QUARTER_FINAL'
  | 'SEMI_FINAL'
  | 'THIRD_PLACE'
  | 'FINAL'

export type MatchStatus = 'SCHEDULED' | 'IN_PROGRESS' | 'FINISHED'

export interface Match {
  id: number
  dateTime: string
  roundName: string
  phase: TournamentPhase
  status: MatchStatus
  homeTeam: string
  homeTeamCode: string
  homeTeamFlag: string
  awayTeam: string
  awayTeamCode: string
  awayTeamFlag: string
  homeScore: number | null
  awayScore: number | null
  result: string | null
  stadium: string
  refreshed: boolean
}

export const PHASE_LABELS: Record<TournamentPhase, string> = {
  GROUP: 'Fase de Grupos',
  ROUND_OF_16: 'Octavos de Final',
  QUARTER_FINAL: 'Cuartos de Final',
  SEMI_FINAL: 'Semifinal',
  THIRD_PLACE: 'Tercer Puesto',
  FINAL: 'Final',
}

export const STATUS_LABELS: Record<MatchStatus, string> = {
  SCHEDULED: 'Programado',
  IN_PROGRESS: 'En Vivo',
  FINISHED: 'Finalizado',
}

export const PHASE_ORDER: Record<TournamentPhase, number> = {
  GROUP: 1,
  ROUND_OF_16: 2,
  QUARTER_FINAL: 3,
  SEMI_FINAL: 4,
  THIRD_PLACE: 5,
  FINAL: 6,
}
