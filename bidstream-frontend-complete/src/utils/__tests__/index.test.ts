import { afterEach, describe, expect, it, vi } from 'vitest'
import {
  calculatePercentageChange,
  debounce,
  formatCurrency,
  formatDateTime,
  getInitials,
  getTimeRemaining,
  isValidEmail,
  throttle,
  truncate,
  validatePasswordStrength,
} from '../index'

afterEach(() => {
  vi.useRealTimers()
})

describe('formatCurrency', () => {
  it('formats whole dollars with two decimals', () => {
    expect(formatCurrency(100)).toBe('$100.00')
  })

  it('formats decimal amounts', () => {
    expect(formatCurrency(1234.5)).toBe('$1,234.50')
  })

  it('formats zero', () => {
    expect(formatCurrency(0)).toBe('$0.00')
  })
})

describe('formatDateTime', () => {
  it('formats an ISO date into a readable local string', () => {
    const formatted = formatDateTime('2026-01-15T10:30:00Z')
    expect(formatted).toContain('Jan')
    expect(formatted).toContain('2026')
  })
})

describe('getTimeRemaining', () => {
  it('returns "Ended" for a past date', () => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2026-01-01T00:00:00Z'))
    expect(getTimeRemaining(new Date('2025-12-31T23:59:59Z'))).toBe('Ended')
  })

  it('returns "Ended" for the current instant', () => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2026-01-01T00:00:00Z'))
    expect(getTimeRemaining(new Date('2026-01-01T00:00:00Z'))).toBe('Ended')
  })

  it('formats seconds for sub-minute durations', () => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2026-01-01T00:00:00Z'))
    expect(getTimeRemaining(new Date('2026-01-01T00:00:10Z'))).toBe('10s')
  })

  it('formats minutes and seconds', () => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2026-01-01T00:00:00Z'))
    expect(getTimeRemaining(new Date('2026-01-01T00:01:05Z'))).toBe('1m 5s')
  })

  it('formats hours and minutes', () => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2026-01-01T00:00:00Z'))
    expect(getTimeRemaining(new Date('2026-01-01T01:30:00Z'))).toBe('1h 30m')
  })

  it('formats days and hours', () => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2026-01-01T00:00:00Z'))
    expect(getTimeRemaining(new Date('2026-01-03T05:00:00Z'))).toBe('2d 5h')
  })
})

describe('calculatePercentageChange', () => {
  it('computes positive percentage change', () => {
    expect(calculatePercentageChange(100, 150)).toBe(50)
  })

  it('computes negative percentage change', () => {
    expect(calculatePercentageChange(200, 150)).toBe(-25)
  })

  it('returns 0 when original is 0', () => {
    expect(calculatePercentageChange(0, 50)).toBe(0)
  })
})

describe('isValidEmail', () => {
  it('accepts valid emails', () => {
    expect(isValidEmail('user@example.com')).toBe(true)
    expect(isValidEmail('a.b+tag@sub.domain.co')).toBe(true)
  })

  it('rejects invalid emails', () => {
    expect(isValidEmail('not-an-email')).toBe(false)
    expect(isValidEmail('missing@domain')).toBe(false)
    expect(isValidEmail('')).toBe(false)
  })
})

describe('validatePasswordStrength', () => {
  it('flags a short password as weak with feedback', () => {
    const result = validatePasswordStrength('Aa1')
    expect(result.isStrong).toBe(false)
    expect(result.feedback).toContain('At least 8 characters')
  })

  it('requires uppercase, lowercase, and a number', () => {
    expect(validatePasswordStrength('abcdefgh1').isStrong).toBe(false)
    expect(validatePasswordStrength('ABCDEFGH1').isStrong).toBe(false)
    expect(validatePasswordStrength('Abcdefgh').isStrong).toBe(false)
  })

  it('accepts a strong password', () => {
    const result = validatePasswordStrength('StrongPass1')
    expect(result.isStrong).toBe(true)
    expect(result.feedback).toHaveLength(0)
  })
})

describe('debounce', () => {
  it('coalesces rapid calls into a single invocation', () => {
    vi.useFakeTimers()
    const fn = vi.fn()
    const debounced = debounce(fn, 100)

    debounced()
    debounced()
    debounced()
    vi.advanceTimersByTime(99)
    expect(fn).not.toHaveBeenCalled()
    vi.advanceTimersByTime(1)
    expect(fn).toHaveBeenCalledTimes(1)
  })

  it('forwards arguments to the wrapped function', () => {
    vi.useFakeTimers()
    const fn = vi.fn()
    const debounced = debounce(fn, 100)

    debounced('a', 1)
    vi.advanceTimersByTime(100)
    expect(fn).toHaveBeenCalledWith('a', 1)
  })
})

describe('throttle', () => {
  it('invokes immediately then blocks until the limit elapses', () => {
    vi.useFakeTimers()
    const fn = vi.fn()
    const throttled = throttle(fn, 100)

    throttled()
    throttled()
    expect(fn).toHaveBeenCalledTimes(1)

    vi.advanceTimersByTime(100)
    throttled()
    expect(fn).toHaveBeenCalledTimes(2)
  })
})

describe('getInitials', () => {
  it('takes the first letters of the first two words', () => {
    expect(getInitials('Alice Seller')).toBe('AS')
  })

  it('handles a single word', () => {
    expect(getInitials('Alice')).toBe('A')
  })

  it('handles empty input', () => {
    expect(getInitials('')).toBe('')
  })
})

describe('truncate', () => {
  it('keeps short text unchanged', () => {
    expect(truncate('short', 10)).toBe('short')
  })

  it('appends an ellipsis to long text', () => {
    expect(truncate('hello world', 5)).toBe('hello...')
  })
})
