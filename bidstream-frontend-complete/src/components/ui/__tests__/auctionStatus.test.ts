import { describe, expect, it } from 'vitest'
import { AuctionStatus } from '../../../types'
import { auctionStatusLabel, auctionStatusTone } from '../auctionStatus'

describe('auctionStatusTone', () => {
  it('maps ACTIVE to emerald', () => {
    expect(auctionStatusTone(AuctionStatus.ACTIVE)).toBe('emerald')
  })

  it('maps PENDING to sky', () => {
    expect(auctionStatusTone(AuctionStatus.PENDING)).toBe('sky')
  })

  it('maps CLOSED to slate', () => {
    expect(auctionStatusTone(AuctionStatus.CLOSED)).toBe('slate')
  })

  it('maps EXPIRED to rose', () => {
    expect(auctionStatusTone(AuctionStatus.EXPIRED)).toBe('rose')
  })

  it('falls back to slate for unknown statuses', () => {
    expect(auctionStatusTone('UNKNOWN' as AuctionStatus)).toBe('slate')
  })
})

describe('auctionStatusLabel', () => {
  it('labels each known status', () => {
    expect(auctionStatusLabel(AuctionStatus.ACTIVE)).toBe('LIVE')
    expect(auctionStatusLabel(AuctionStatus.PENDING)).toBe('PENDING')
    expect(auctionStatusLabel(AuctionStatus.CLOSED)).toBe('CLOSED')
    expect(auctionStatusLabel(AuctionStatus.EXPIRED)).toBe('EXPIRED')
  })

  it('falls back to UNKNOWN for unknown statuses', () => {
    expect(auctionStatusLabel('UNKNOWN' as AuctionStatus)).toBe('UNKNOWN')
  })
})
