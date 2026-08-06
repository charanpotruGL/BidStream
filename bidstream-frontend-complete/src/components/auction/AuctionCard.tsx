import React, { useState, useEffect } from 'react'
import { motion } from 'framer-motion'
import { Clock, Gavel, Eye } from 'lucide-react'
import { Link } from 'react-router-dom'
import { Auction, AuctionStatus } from '../../types'
import { formatCurrency, getTimeRemaining } from '../../utils'
import { Badge } from '../ui/Badge'
import { Button } from '../ui/Button'
import { auctionStatusTone, auctionStatusLabel } from '../ui/auctionStatus'
import { cn } from '../../lib/cn'

// ============================================================================
// Auction Card Component
// ============================================================================

interface AuctionCardProps {
  auction: Auction
  onQuickBid?: (amount: number) => void
  isLoading?: boolean
}

export const AuctionCard: React.FC<AuctionCardProps> = ({
  auction,
  onQuickBid,
  isLoading = false,
}) => {
  const [timeRemaining, setTimeRemaining] = useState<string>('')
  const [percentageIncrease, setPercentageIncrease] = useState<number>(0)

  useEffect(() => {
    const updateCountdown = () => {
      const remaining = getTimeRemaining(new Date(auction.endTime))
      setTimeRemaining(remaining)
    }

    updateCountdown()
    const interval = setInterval(updateCountdown, 1000)
    return () => clearInterval(interval)
  }, [auction.endTime])

  useEffect(() => {
    if (auction.startingPrice > 0) {
      const increase =
        ((auction.currentPrice - auction.startingPrice) / auction.startingPrice) *
        100
      setPercentageIncrease(increase)
    }
  }, [auction.currentPrice, auction.startingPrice])

  const isActive = auction.status === AuctionStatus.ACTIVE

  return (
    <motion.div
      whileHover={{ y: -5 }}
      transition={{ type: 'spring', stiffness: 300, damping: 24 }}
      className="group flex h-full flex-col overflow-hidden rounded-2xl border border-slate-700/70 bg-slate-800/60 shadow-card backdrop-blur-sm transition-colors duration-300 hover:border-slate-600"
    >
      {/* Header visual */}
      <Link
        to={`/auction/${auction.id}`}
        className="relative block h-44 overflow-hidden bg-slate-900"
        aria-label={auction.title}
      >
        <div className="absolute inset-0 bg-brand-gradient-soft" aria-hidden="true" />
        <div
          className="absolute inset-0 bg-[radial-gradient(circle_at_30%_20%,rgba(216,158,48,0.16),transparent_55%)]"
          aria-hidden="true"
        />
        <div className="absolute inset-0 flex items-center justify-center">
          <Gavel
            className="h-14 w-14 text-brand-400/25 transition-transform duration-300 group-hover:scale-110 group-hover:text-brand-400/40"
            aria-hidden="true"
          />
        </div>

        {/* Status Badge */}
        <div className="absolute left-4 top-4">
          <Badge
            tone={auctionStatusTone(auction.status)}
            dot={isActive}
            pulse={isActive}
          >
            {auctionStatusLabel(auction.status)}
          </Badge>
        </div>

        {/* Overlay gradient */}
        <div className="absolute inset-x-0 bottom-0 h-20 bg-gradient-to-t from-slate-800/90 to-transparent" />
      </Link>

      {/* Content */}
      <div className="flex flex-1 flex-col gap-4 p-5">
        <div>
          <h3 className="line-clamp-1 font-display text-lg font-semibold text-white transition-colors group-hover:text-brand-200">
            <Link to={`/auction/${auction.id}`}>{auction.title}</Link>
          </h3>
          <p className="mt-1 line-clamp-2 text-sm leading-relaxed text-slate-400">
            {auction.description}
          </p>
        </div>

        {/* Prices */}
        <div className="mt-auto space-y-3 border-t border-slate-700/70 pt-4">
          <div className="flex items-end justify-between">
            <div>
              <p className="text-[11px] font-medium uppercase tracking-wider text-slate-500">
                Current Price
              </p>
              <p className="text-2xl font-bold tracking-tight text-white">
                {formatCurrency(auction.currentPrice)}
              </p>
            </div>
            {percentageIncrease > 0 && (
              <span className="rounded-full bg-emerald-500/15 px-2 py-0.5 text-xs font-semibold text-emerald-300">
                +{percentageIncrease.toFixed(1)}%
              </span>
            )}
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <p className="text-[11px] font-medium uppercase tracking-wider text-slate-500">
                Starting
              </p>
              <p className="text-sm font-semibold text-slate-300">
                {formatCurrency(auction.startingPrice)}
              </p>
            </div>
            <div>
              <p className="text-[11px] font-medium uppercase tracking-wider text-slate-500">
                Bids
              </p>
              <p className="text-sm font-semibold text-slate-300">
                {auction.highestBidderId ? '1+' : '0'}
              </p>
            </div>
          </div>
        </div>

        {/* Time remaining */}
        {isActive && (
          <div className="flex items-center gap-2 rounded-xl border border-amber-500/25 bg-amber-500/10 px-3 py-2">
            <Clock className="h-4 w-4 text-amber-300" aria-hidden="true" />
            <p className="tabular text-sm font-semibold text-amber-200">
              {timeRemaining}
            </p>
            <span className="ml-auto text-xs text-amber-300/70">left</span>
          </div>
        )}

        {/* Quick Bid Actions */}
        {isActive && onQuickBid && (
          <div className="space-y-2 pt-1">
            <div className="grid grid-cols-3 gap-2">
              {[
                { label: '+5%', amount: auction.currentPrice * 1.05 },
                { label: '+10%', amount: auction.currentPrice * 1.1 },
                { label: '+20%', amount: auction.currentPrice * 1.2 },
              ].map(({ label, amount }) => (
                <button
                  key={label}
                  onClick={() => onQuickBid(amount)}
                  disabled={isLoading}
                  aria-label={`Place bid at ${label} above current price`}
                  className="h-9 rounded-lg border border-slate-700 bg-slate-900/70 text-xs font-semibold text-slate-200 transition-colors hover:border-brand-500/50 hover:text-brand-300 disabled:pointer-events-none disabled:opacity-50"
                >
                  {label}
                </button>
              ))}
            </div>
            <Button to={`/auction/${auction.id}`} size="sm" fullWidth>
              <Eye className="h-4 w-4" aria-hidden="true" />
              View &amp; Bid
            </Button>
          </div>
        )}

        {/* Closed state */}
        {auction.status === AuctionStatus.CLOSED && (
          <div className="rounded-xl border border-slate-700 bg-slate-900/50 px-4 py-3 text-center">
            <p className="text-sm text-slate-300">Auction ended</p>
            {auction.highestBidderId && (
              <p className="mt-0.5 text-xs text-slate-500">
                Won by bidder #{auction.highestBidderId}
              </p>
            )}
          </div>
        )}

        {/* Pending state */}
        {auction.status === AuctionStatus.PENDING && (
          <div className={cn('rounded-xl border border-slate-700 bg-slate-900/50 px-4 py-3 text-center')}>
            <p className="text-sm text-slate-300">
              Starts {new Date(auction.startTime).toLocaleDateString()}
            </p>
          </div>
        )}
      </div>
    </motion.div>
  )
}
