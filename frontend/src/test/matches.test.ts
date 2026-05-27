import { describe, it, expect } from 'vitest'
import { PHASE_LABELS, PHASE_ORDER, STATUS_LABELS } from '../types/matches'
import type { TournamentPhase, MatchStatus } from '../types/matches'

describe('PHASE_LABELS', () => {
  it('has a human-readable label for every tournament phase', () => {
    const phases: TournamentPhase[] = ['GROUP', 'ROUND_OF_16', 'QUARTER_FINAL', 'SEMI_FINAL', 'THIRD_PLACE', 'FINAL']
    phases.forEach((phase) => {
      expect(PHASE_LABELS[phase]).toBeTruthy()
    })
  })

  it('returns correct Spanish label for GROUP', () => {
    expect(PHASE_LABELS.GROUP).toBe('Fase de Grupos')
  })

  it('returns correct Spanish label for FINAL', () => {
    expect(PHASE_LABELS.FINAL).toBe('Final')
  })
})

describe('PHASE_ORDER', () => {
  it('GROUP has the lowest order number', () => {
    const orders = Object.values(PHASE_ORDER)
    expect(PHASE_ORDER.GROUP).toBe(Math.min(...orders))
  })

  it('FINAL has the highest order number', () => {
    const orders = Object.values(PHASE_ORDER)
    expect(PHASE_ORDER.FINAL).toBe(Math.max(...orders))
  })

  it('phases sort from group stage to final', () => {
    expect(PHASE_ORDER.GROUP).toBeLessThan(PHASE_ORDER.ROUND_OF_16)
    expect(PHASE_ORDER.ROUND_OF_16).toBeLessThan(PHASE_ORDER.QUARTER_FINAL)
    expect(PHASE_ORDER.QUARTER_FINAL).toBeLessThan(PHASE_ORDER.SEMI_FINAL)
    expect(PHASE_ORDER.SEMI_FINAL).toBeLessThan(PHASE_ORDER.FINAL)
  })
})

describe('STATUS_LABELS', () => {
  it('has a label for every match status', () => {
    const statuses: MatchStatus[] = ['SCHEDULED', 'IN_PROGRESS', 'FINISHED']
    statuses.forEach((status) => {
      expect(STATUS_LABELS[status]).toBeTruthy()
    })
  })

  it('IN_PROGRESS maps to a live indicator label', () => {
    expect(STATUS_LABELS.IN_PROGRESS).toBe('En Vivo')
  })
})
