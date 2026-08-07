import { afterEach, describe, expect, it, vi } from 'vitest'
import { renderHook, act } from '@testing-library/react'
import { useCountdown } from '../useCountdown'

afterEach(() => {
  vi.useRealTimers()
})

describe('useCountdown', () => {
  it('returns the formatted time remaining', () => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2026-01-01T00:00:00Z'))
    const { result } = renderHook(() => useCountdown('2026-01-01T00:01:05Z'))
    expect(result.current).toBe('1m 5s')
  })

  it('returns an empty string when no end time is provided', () => {
    const { result } = renderHook(() => useCountdown(undefined))
    expect(result.current).toBe('')
  })

  it('does not tick while disabled', () => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2026-01-01T00:00:00Z'))
    const { result } = renderHook(() =>
      useCountdown('2026-01-01T00:01:05Z', { disabled: true })
    )
    act(() => {
      vi.advanceTimersByTime(2000)
    })
    expect(result.current).toBe('1m 5s')
  })

  it('ticks down every second while enabled', () => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2026-01-01T00:00:00Z'))
    const { result } = renderHook(() => useCountdown('2026-01-01T00:01:05Z'))
    act(() => {
      vi.advanceTimersByTime(1000)
    })
    expect(result.current).toBe('1m 4s')
  })

  it('throttles to 30s when more than 10 minutes remain', () => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2026-01-01T00:00:00Z'))
    const { result } = renderHook(() => useCountdown('2026-01-01T00:11:00Z'))
    act(() => {
      vi.advanceTimersByTime(1000)
    })
    // No tick after 1s — the 30s interval has not fired yet.
    expect(result.current).toBe('11m 0s')
    act(() => {
      vi.advanceTimersByTime(29000)
    })
    expect(result.current).toBe('10m 30s')
  })
})
