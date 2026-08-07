import { describe, expect, it, vi } from 'vitest'
import { fireEvent, render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { Auction, AuctionStatus } from '../../../types'
import { AuctionCard } from '../AuctionCard'

const baseAuction: Auction = {
  id: 1,
  title: 'Vintage Camera',
  description: 'A well-preserved vintage camera.',
  sellerId: 10,
  startingPrice: 100,
  currentPrice: 150,
  highestBidId: 7,
  highestBidderId: 5,
  startTime: '2026-01-01T00:00:00Z',
  endTime: '2026-01-10T00:00:00Z',
  status: AuctionStatus.ACTIVE,
  createdAt: '2026-01-01T00:00:00Z',
}

const renderCard = (auction: Auction, onQuickBid?: (amount: number) => void) =>
  render(
    <MemoryRouter>
      <AuctionCard auction={auction} onQuickBid={onQuickBid} />
    </MemoryRouter>
  )

describe('AuctionCard', () => {
  it('renders the auction title, description, and prices', () => {
    renderCard(baseAuction)
    expect(screen.getByText('Vintage Camera')).toBeInTheDocument()
    expect(screen.getByText('A well-preserved vintage camera.')).toBeInTheDocument()
    expect(screen.getByText('$150.00')).toBeInTheDocument()
    expect(screen.getByText('$100.00')).toBeInTheDocument()
  })

  it('shows the LIVE badge and countdown for an active auction', () => {
    renderCard(baseAuction)
    expect(screen.getByText('LIVE')).toBeInTheDocument()
    expect(screen.getByText('left')).toBeInTheDocument()
  })

  it('renders quick-bid buttons and triggers the callback for active auctions', () => {
    const onQuickBid = vi.fn()
    renderCard(baseAuction, onQuickBid)

    fireEvent.click(screen.getByRole('button', { name: 'Place bid at +10% above current price' }))
    expect(onQuickBid).toHaveBeenCalledWith(150 * 1.1)
  })

  it('shows the ended panel instead of bidding actions for a closed auction', () => {
    renderCard({ ...baseAuction, status: AuctionStatus.CLOSED }, vi.fn())
    expect(screen.getByText('Auction ended')).toBeInTheDocument()
    expect(screen.getByText('Won by bidder #5')).toBeInTheDocument()
    expect(screen.getByText('CLOSED')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Place bid at +10% above current price' })).not.toBeInTheDocument()
  })

  it('hides the bidding panel for an expired auction', () => {
    renderCard({ ...baseAuction, status: AuctionStatus.EXPIRED }, vi.fn())
    expect(screen.getByText('EXPIRED')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Place bid at +10% above current price' })).not.toBeInTheDocument()
  })

  it('shows the start date for a pending auction', () => {
    renderCard({ ...baseAuction, status: AuctionStatus.PENDING })
    expect(screen.getByText('PENDING')).toBeInTheDocument()
    expect(screen.getByText(/Starts/)).toBeInTheDocument()
    expect(screen.queryByText('left')).not.toBeInTheDocument()
  })
})
