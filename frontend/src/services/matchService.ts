import api from './api'
import type { Match, TournamentPhase } from '../types/matches'

export const getAllMatches = (): Promise<Match[]> =>
  api.get<Match[]>('/matches').then((r) => r.data)

export const getMatchById = (id: number): Promise<Match> =>
  api.get<Match>(`/matches/${id}`).then((r) => r.data)

export const getScheduledMatches = (): Promise<Match[]> =>
  api.get<Match[]>('/matches/scheduled').then((r) => r.data)

export const getFinishedMatches = (): Promise<Match[]> =>
  api.get<Match[]>('/matches/finished').then((r) => r.data)

export const getMatchesByPhase = (phase: TournamentPhase): Promise<Match[]> =>
  api.get<Match[]>(`/matches/phase/${phase}`).then((r) => r.data)

export const getMatchesByTeam = (fifaCode: string): Promise<Match[]> =>
  api.get<Match[]>(`/matches/team/${fifaCode.toUpperCase()}`).then((r) => r.data)

export const getMatchesByRound = (round: string): Promise<Match[]> =>
  api.get<Match[]>(`/matches/round/${encodeURIComponent(round)}`).then((r) => r.data)

export const loadMatchesFromApi = (): Promise<string> =>
  api.post<string>('/matches/load').then((r) => r.data)
