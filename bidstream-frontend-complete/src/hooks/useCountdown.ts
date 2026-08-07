import { useEffect, useMemo, useState } from 'react'
import { getTimeRemaining } from '../utils'

const THROTTLE_THRESHOLD_MS = 10 * 60 * 1000

interface UseCountdownOptions {
  disabled?: boolean
}

/**
 * Shared countdown hook that re-renders on a ticking interval and degrades the
 * tick rate once more than 10 minutes remain (30s instead of 1s). Only runs the
 * timer when `disabled` is false, so a single interval instance can serve many
 * cards on the same screen.
 */
export function useCountdown(
  endTime?: string | null,
  { disabled = false }: UseCountdownOptions = {}
): string {
  const [now, setNow] = useState(() => Date.now())

  const intervalMs = useMemo(() => {
    if (!endTime) return 1000
    const remaining = new Date(endTime).getTime() - Date.now()
    return remaining > THROTTLE_THRESHOLD_MS ? 30000 : 1000
  }, [endTime])

  useEffect(() => {
    if (disabled || !endTime) return
    setNow(Date.now())
    const id = setInterval(() => setNow(Date.now()), intervalMs)
    return () => clearInterval(id)
  }, [disabled, endTime, intervalMs])

  return endTime ? getTimeRemaining(new Date(endTime), new Date(now)) : ''
}
