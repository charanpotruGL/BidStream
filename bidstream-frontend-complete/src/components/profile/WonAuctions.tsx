import React from 'react'
import { useQuery } from '@tanstack/react-query'
import { motion } from 'framer-motion'
import { Trophy, Eye } from 'lucide-react'
import { apiClient } from '../../api/client'
import { BidStatus } from '../../types'
import { formatCurrency, formatDateTime } from '../../utils'
import { Panel } from '../ui/Panel'
import { Badge } from '../ui/Badge'
import { Button } from '../ui/Button'
import { Skeleton } from '../ui/Skeleton'
import { EmptyState } from '../ui/EmptyState'

// ============================================================================
// Won Auctions - auctions a bidder has won (bids marked WINNING on close)
// ============================================================================

interface WonAuctionsProps {
  bidderId: number
}

export const WonAuctions: React.FC<WonAuctionsProps> = ({ bidderId }) => {
  const { data: auctions = [], isLoading } = useQuery({
    queryKey: ['won-auctions', bidderId],
    queryFn: async () => {
      const bids = await apiClient.getBidsByBidder(bidderId)
      const winningBids = bids.filter((b) => b.status === BidStatus.WINNING)

      const results = await Promise.allSettled(
        winningBids.map((b) => apiClient.getAuctionById(b.auctionId))
      )

      return results
        .filter(
          (r): r is PromiseFulfilledResult<Awaited<ReturnType<typeof apiClient.getAuctionById>>> =>
            r.status === 'fulfilled'
        )
        .map((r) => r.value)
        .sort((a, b) => b.endTime.localeCompare(a.endTime))
    },
    enabled: bidderId > 0,
  })

  return (
    <section className="space-y-5" aria-labelledby="won-auctions-heading">
      <div className="flex items-center gap-2.5">
        <span className="flex h-8 w-8 items-center justify-center rounded-lg border border-amber-500/25 bg-amber-500/10 text-amber-300">
          <Trophy className="h-4 w-4" aria-hidden="true" />
        </span>
        <h2 id="won-auctions-heading" className="font-display text-xl font-semibold text-white">
          Auctions Won
        </h2>
      </div>

      {isLoading ? (
        <div className="grid grid-cols-1 gap-5 md:grid-cols-2" role="status" aria-label="Loading won auctions">
          {[1, 2, 3, 4].map((i) => (
            <Skeleton key={i} className="h-36" />
          ))}
        </div>
      ) : auctions.length === 0 ? (
        <EmptyState
          icon={<Trophy className="h-8 w-8" aria-hidden="true" />}
          title="No auctions won yet"
          description="Bid on live auctions and outlast the competition to take home your first win."
          action={
            <Button to="/auctions">
              <Eye className="h-4 w-4" aria-hidden="true" />
              Browse Auctions
            </Button>
          }
        />
      ) : (
        <div className="grid grid-cols-1 gap-5 md:grid-cols-2">
          {auctions.map((auction, index) => (
            <motion.div
              key={auction.id}
              initial={{ opacity: 0, y: 16 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: Math.min(index * 0.05, 0.4) }}
            >
              <Panel hover className="flex h-full flex-col gap-4 p-6">
                <div className="flex items-start justify-between gap-3">
                  <div className="min-w-0">
                    <h3 className="line-clamp-1 font-display text-lg font-semibold text-white">
                      {auction.title}
                    </h3>
                    <p className="mt-0.5 line-clamp-1 text-sm text-slate-400">
                      {auction.description}
                    </p>
                  </div>
                  <Badge tone="emerald" className="shrink-0">
                    Won
                  </Badge>
                </div>

                <div className="grid grid-cols-2 gap-4 rounded-xl border border-slate-700/70 bg-slate-900/40 px-4 py-3">
                  <div>
                    <p className="text-[11px] font-medium uppercase tracking-wider text-slate-400">
                      Winning Bid
                    </p>
                    <p className="mt-0.5 truncate text-sm font-bold text-emerald-300">
                      {formatCurrency(auction.currentPrice)}
                    </p>
                  </div>
                  <div>
                    <p className="text-[11px] font-medium uppercase tracking-wider text-slate-400">
                      Ended
                    </p>
                    <p className="mt-0.5 truncate text-xs font-semibold text-slate-300">
                      {formatDateTime(auction.endTime)}
                    </p>
                  </div>
                </div>

                <Button to={`/auction/${auction.id}`} variant="outline" size="sm" className="mt-auto">
                  <Eye className="h-3.5 w-3.5" aria-hidden="true" />
                  View Auction
                </Button>
              </Panel>
            </motion.div>
          ))}
        </div>
      )}
    </section>
  )
}
