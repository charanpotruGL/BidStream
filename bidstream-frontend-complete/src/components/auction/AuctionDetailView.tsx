import React, { useState, useEffect } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { motion, AnimatePresence } from 'framer-motion'
import { Clock, AlertTriangle, CheckCircle2, Activity, Gavel } from 'lucide-react'
import { AuctionStatus, Bid, BidStatus } from '../../types'
import { apiClient } from '../../api/client'
import { useAuthStore } from '../../store'
import { useCountdown } from '../../hooks/useCountdown'
import { formatCurrency } from '../../utils'
import { toast } from 'sonner'
import { Panel } from '../ui/Panel'
import { Button } from '../ui/Button'
import { Input } from '../ui/Input'
import { Badge } from '../ui/Badge'
import { Spinner } from '../ui/Spinner'
import { EmptyState } from '../ui/EmptyState'
import { UserName, UserAvatar } from '../ui/UserName'
import { auctionStatusTone, auctionStatusLabel } from '../ui/auctionStatus'
import { cn } from '../../lib/cn'

// ============================================================================
// Auction Detail View Component
// ============================================================================

interface AuctionDetailViewProps {
  auctionId: number
}

export const AuctionDetailView: React.FC<AuctionDetailViewProps> = ({
  auctionId,
}) => {
  const [bidAmount, setBidAmount] = useState<number>(0)
  const { user } = useAuthStore()
  const queryClient = useQueryClient()

  // Fetch auction details
  const { data: auction, isLoading: auctionLoading } = useQuery({
    queryKey: ['auction', auctionId],
    queryFn: () => apiClient.getAuctionById(auctionId),
    refetchInterval: 5000, // Poll every 5 seconds
  })

  // Fetch bid history. Sorted ascending by id, so the last element is the
  // leading bid — no separate highest-bid request needed.
  const { data: bids = [] as Bid[], isLoading: bidsLoading } = useQuery({
    queryKey: ['bids', auctionId],
    queryFn: () => apiClient.getBidsByAuction(auctionId),
    refetchInterval: 3000, // Poll every 3 seconds for live updates
  })

  const leadingBid = bids.length > 0 ? bids[bids.length - 1] : null
  const timeRemaining = useCountdown(auction?.endTime, {
    disabled: auction?.status !== AuctionStatus.ACTIVE,
  })

  // Place bid mutation
  const { mutate: placeBid, isPending: isBidding } = useMutation({
    mutationFn: (amount: number) =>
      apiClient.placeBid({
        amount,
        bidderId: user!.id,
        auctionId,
      }),
    onSuccess: (bid) => {
      toast.success(`Bid placed successfully for ${formatCurrency(bid.amount)}`)
      setBidAmount(0)
      queryClient.invalidateQueries({ queryKey: ['auction', auctionId] })
      queryClient.invalidateQueries({ queryKey: ['bids', auctionId] })
      queryClient.invalidateQueries({ queryKey: ['auctions'] })
    },
    onError: (error: any) => {
      const message =
        error.response?.data?.message ||
        error.response?.data?.fieldErrors?.amount ||
        'Failed to place bid'
      toast.error(message)
    },
  })

  // Initialize bid amount
  useEffect(() => {
    if (leadingBid) {
      setBidAmount(leadingBid.amount + 1)
    } else if (auction) {
      setBidAmount(auction.startingPrice + 1)
    }
  }, [leadingBid, auction])

  if (auctionLoading) {
    return (
      <div className="flex h-96 items-center justify-center">
        <Spinner size="lg" label="Loading auction" />
      </div>
    )
  }

  if (!auction) {
    return (
      <EmptyState
        icon={<AlertTriangle className="h-8 w-8" aria-hidden="true" />}
        title="Auction not found"
        description="This auction may have been removed or never existed."
      />
    )
  }

  const isActive = auction.status === AuctionStatus.ACTIVE
  const ended = !isActive || new Date(auction.endTime).getTime() <= Date.now()
  const userBids = bids.filter((b) => b.bidderId === user?.id) || []
  const userIsOutbid =
    userBids.length > 0 && userBids[userBids.length - 1]?.status === BidStatus.OUTBID

  const handlePlaceBid = () => {
    if (ended) {
      toast.error('This auction has ended')
      return
    }
    if (bidAmount <= (leadingBid?.amount || auction.currentPrice)) {
      toast.error('Bid must be higher than current highest bid')
      return
    }
    placeBid(bidAmount)
  }

  return (
    <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
      {/* Main Content */}
      <div className="space-y-6 lg:col-span-2">
        {/* Auction Header */}
        <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }}>
          <Panel className="relative overflow-hidden p-6 sm:p-8">
            <div
              className="pointer-events-none absolute inset-0 bg-brand-gradient-soft"
              aria-hidden="true"
            />
            <div className="relative space-y-6">
              <div className="flex flex-wrap items-start justify-between gap-4">
                <div className="min-w-0">
                  <h1 className="font-display text-2xl font-semibold tracking-tight text-white sm:text-3xl md:text-4xl">
                    {auction.title}
                  </h1>
                  <p className="mt-2 max-w-2xl text-sm leading-relaxed text-slate-400 sm:text-base">
                    {auction.description}
                  </p>
                </div>
                <Badge
                  tone={auctionStatusTone(auction.status)}
                  dot={isActive}
                  pulse={isActive}
                  className="shrink-0"
                >
                  {auctionStatusLabel(auction.status)}
                </Badge>
              </div>

              {/* Price Section */}
              <div className="grid grid-cols-3 gap-4 border-t border-slate-700/70 pt-6">
                <div>
                  <p className="text-[11px] font-medium uppercase tracking-wider text-slate-400">
                    Starting Price
                  </p>
                  <p className="mt-1 text-base font-bold text-slate-300 sm:text-lg">
                    {formatCurrency(auction.startingPrice)}
                  </p>
                </div>
                <div>
                  <p className="text-[11px] font-medium uppercase tracking-wider text-slate-400">
                    Current Price
                  </p>
                  <p className="mt-1 text-base font-bold text-white sm:text-lg">
                    {formatCurrency(auction.currentPrice)}
                  </p>
                </div>
                <div>
                  <p className="text-[11px] font-medium uppercase tracking-wider text-slate-400">
                    Time Remaining
                  </p>
                  <motion.p
                    animate={isActive ? { opacity: [1, 0.7, 1] } : undefined}
                    transition={{ duration: 2, repeat: Infinity }}
                    className="tabular mt-1 flex items-center gap-1.5 text-base font-bold text-amber-300 sm:text-lg"
                  >
                    <Clock className="h-4 w-4 shrink-0" aria-hidden="true" />
                    <span>{timeRemaining}</span>
                  </motion.p>
                </div>
              </div>
            </div>
          </Panel>
        </motion.div>

        {/* User Alert - Outbid */}
        <AnimatePresence>
          {userIsOutbid && (
            <motion.div
              initial={{ opacity: 0, y: -10 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -10 }}
              className="flex items-start gap-3 rounded-2xl border border-rose-500/30 bg-rose-500/10 p-4"
              role="alert"
            >
              <AlertTriangle className="mt-0.5 h-5 w-5 flex-shrink-0 text-rose-400" aria-hidden="true" />
              <div>
                <p className="font-semibold text-rose-200">You&rsquo;ve been outbid!</p>
                <p className="mt-0.5 text-sm text-rose-300/80">
                  Place a higher bid to stay in the running.
                </p>
              </div>
            </motion.div>
          )}
        </AnimatePresence>

        {/* Bid History Timeline */}
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.08 }}
        >
          <Panel className="p-6">
            <div className="mb-5 flex items-center gap-2.5">
              <span className="flex h-8 w-8 items-center justify-center rounded-lg border border-brand-500/25 bg-brand-500/10 text-brand-300">
                <Activity className="h-4 w-4" aria-hidden="true" />
              </span>
              <h2 className="font-display text-xl font-semibold text-white">
                Live Bid Activity
              </h2>
            </div>

            <div className="max-h-96 space-y-1 overflow-y-auto pr-1">
              {bidsLoading ? (
                <div className="flex items-center justify-center py-10">
                  <Spinner label="Loading bids" />
                </div>
              ) : bids.length === 0 ? (
                <div className="flex flex-col items-center py-10 text-center">
                  <Gavel className="mb-3 h-10 w-10 text-slate-600" aria-hidden="true" />
                  <p className="text-slate-400">No bids yet. Be the first to bid!</p>
                </div>
              ) : (
                [...bids].reverse().map((bid, index) => {
                  const isLast = index === bids.length - 1
                  const isMine = bid.bidderId === user?.id
                  const isLeading = index === 0
                  return (
                    <div key={bid.id} className="relative flex gap-4 pb-4">
                      {!isLast && (
                        <span
                          className="absolute bottom-0 left-[19px] top-10 w-px bg-slate-700/70"
                          aria-hidden="true"
                        />
                      )}
                      {isMine ? (
                        <span
                          className={cn(
                            'relative z-10 flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-full border text-xs font-bold',
                            'border-brand-500/50 bg-brand-gradient text-brand-950'
                          )}
                        >
                          You
                        </span>
                      ) : (
                        <UserAvatar userId={bid.bidderId} />
                      )}
                      <div
                        className={cn(
                          'flex flex-1 items-center justify-between gap-3 rounded-xl border px-4 py-3',
                          isLeading
                            ? 'border-brand-500/40 bg-brand-500/5'
                            : 'border-slate-700/70 bg-slate-900/40',
                          bid.status === BidStatus.OUTBID && 'border-slate-700 bg-slate-900/30'
                        )}
                      >
                        <div className="min-w-0">
                          <p className="truncate text-sm font-semibold text-white">
                            <UserName userId={bid.bidderId} />
                            {isMine && (
                              <span className="ml-2 rounded-full bg-brand-500/15 px-2 py-0.5 text-[11px] font-semibold text-brand-300">
                                You
                              </span>
                            )}
                          </p>
                          <p className="mt-0.5 text-xs text-slate-400">
                            {new Date(bid.createdAt).toLocaleTimeString()}
                          </p>
                        </div>
                        <div className="text-right">
                          <p className="tabular text-sm font-bold text-white">
                            {formatCurrency(bid.amount)}
                          </p>
                          {bid.status === BidStatus.OUTBID ? (
                            <p className="text-[11px] font-medium text-rose-400">Outbid</p>
                          ) : isLeading ? (
                            <p className="text-[11px] font-medium text-emerald-400">Leading</p>
                          ) : null}
                        </div>
                      </div>
                    </div>
                  )
                })
              )}
            </div>
          </Panel>
        </motion.div>
      </div>

      {/* Sidebar - Bidding Panel */}
      <div className="lg:col-span-1">
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.12 }}
          className="lg:sticky lg:top-20"
        >
          <Panel className="space-y-5 p-6">
            <h3 className="font-display text-lg font-semibold text-white">
              Place Your Bid
            </h3>

            {!ended ? (
              <>
                <div className="rounded-xl border border-slate-700/70 bg-slate-900/50 p-4">
                  <p className="text-[11px] font-medium uppercase tracking-wider text-slate-400">
                    Minimum bid
                  </p>
                  <p className="mt-1 text-xl font-bold text-white">
                    {formatCurrency((leadingBid?.amount || auction.currentPrice) + 1)}
                  </p>
                </div>

                <Input
                  type="number"
                  label="Your bid (USD)"
                  value={bidAmount || ''}
                  onChange={(e) => setBidAmount(parseFloat(e.target.value))}
                  placeholder="Enter bid amount"
                />

                <Button
                  onClick={handlePlaceBid}
                  disabled={isBidding}
                  loading={isBidding}
                  size="lg"
                  fullWidth
                >
                  {isBidding ? 'Placing Bid...' : 'Place Bid'}
                </Button>

                <p className="text-center text-xs leading-relaxed text-slate-400">
                  You&rsquo;ll win if no higher bid is placed before the auction ends.
                </p>
              </>
            ) : (
              <div className="flex flex-col items-center py-8 text-center">
                <CheckCircle2 className="mb-3 h-10 w-10 text-slate-500" aria-hidden="true" />
                <p className="text-slate-400">
                  {auction.status === AuctionStatus.PENDING
                    ? 'Auction not started yet'
                    : 'Auction has ended'}
                </p>
                {ended && auction.status !== AuctionStatus.PENDING && (
                  <div className="mt-4 w-full rounded-xl border border-brand-500/25 bg-brand-500/10 p-4">
                    <p className="text-xs font-semibold uppercase tracking-wider text-brand-300">
                      Winner
                    </p>
                    {auction.highestBidderId ? (
                      <>
                        <p className="mt-1 text-lg font-bold text-white">
                          <UserName userId={auction.highestBidderId} />
                        </p>
                        <p className="mt-0.5 text-sm text-slate-400">
                          Won at {formatCurrency(auction.currentPrice)}
                        </p>
                      </>
                    ) : (
                      <p className="mt-1 text-sm text-slate-400">
                        No winning bid was placed
                      </p>
                    )}
                  </div>
                )}
              </div>
            )}
          </Panel>
        </motion.div>
      </div>
    </div>
  )
}
