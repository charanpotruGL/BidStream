import { AuctionStatus } from '../../types'
import type { BadgeTone } from './Badge'

export const auctionStatusTone = (status: AuctionStatus): BadgeTone => {
  switch (status) {
    case AuctionStatus.ACTIVE:
      return 'emerald'
    case AuctionStatus.PENDING:
      return 'sky'
    case AuctionStatus.CLOSED:
      return 'slate'
    case AuctionStatus.EXPIRED:
      return 'rose'
    default:
      return 'slate'
  }
}

export const auctionStatusLabel = (status: AuctionStatus): string => {
  switch (status) {
    case AuctionStatus.ACTIVE:
      return 'LIVE'
    case AuctionStatus.PENDING:
      return 'PENDING'
    case AuctionStatus.CLOSED:
      return 'CLOSED'
    case AuctionStatus.EXPIRED:
      return 'EXPIRED'
    default:
      return 'UNKNOWN'
  }
}
