import React, { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { motion } from 'framer-motion'
import { Plus, Play, Square, Trash2, PackageOpen, Eye, Gavel } from 'lucide-react'
import { CreateAuctionModal } from '../components/auction/CreateAuctionModal'
import { apiClient } from '../api/client'
import { useAuthStore } from '../store'
import { AuctionStatus } from '../types'
import { formatCurrency, formatDateTime } from '../utils'
import { toast } from 'sonner'
import { PageHeader } from '../components/ui/PageHeader'
import { Button } from '../components/ui/Button'
import { Panel } from '../components/ui/Panel'
import { Skeleton } from '../components/ui/Skeleton'
import { EmptyState } from '../components/ui/EmptyState'
import { Badge } from '../components/ui/Badge'
import { auctionStatusTone, auctionStatusLabel } from '../components/ui/auctionStatus'

// ============================================================================
// Seller Hub - Manage & Monitor Auctions
// ============================================================================

export const SellerHubPage: React.FC = () => {
  const { user } = useAuthStore()
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false)
  const queryClient = useQueryClient()

  const userId = user?.id

  const { data: auctions = [], isLoading } = useQuery({
    queryKey: ['my-auctions', userId],
    queryFn: () => apiClient.getAuctionsBySeller(userId!),
    enabled: !!userId,
    refetchInterval: 15000,
  })

  const refresh = () => {
    queryClient.invalidateQueries({ queryKey: ['my-auctions', userId] })
    queryClient.invalidateQueries({ queryKey: ['auctions'] })
  }

  const startMutation = useMutation({
    mutationFn: (id: number) => apiClient.startAuction(id),
    onSuccess: () => {
      toast.success('Auction started')
      refresh()
    },
    onError: (e: any) => toast.error(apiClient.getErrorMessage(e)),
  })

  const closeMutation = useMutation({
    mutationFn: (id: number) => apiClient.closeAuction(id),
    onSuccess: () => {
      toast.success('Auction closed')
      refresh()
    },
    onError: (e: any) => toast.error(apiClient.getErrorMessage(e)),
  })

  const deleteMutation = useMutation({
    mutationFn: (id: number) => apiClient.deleteAuction(id),
    onSuccess: () => {
      toast.success('Auction deleted')
      refresh()
    },
    onError: (e: any) => toast.error(apiClient.getErrorMessage(e)),
  })

  return (
    <div className="flex flex-col gap-8">
      <PageHeader
        eyebrow="Seller Studio"
        title="Seller Hub"
        description="Create, manage and monitor your auctions"
        actions={
          <Button onClick={() => setIsCreateModalOpen(true)}>
            <Plus className="h-4 w-4" aria-hidden="true" />
            Create Auction
          </Button>
        }
      />

      {isLoading ? (
        <div className="grid grid-cols-1 gap-5 md:grid-cols-2" role="status" aria-label="Loading your auctions">
          {[1, 2, 3, 4].map((i) => (
            <Skeleton key={i} className="h-48" />
          ))}
        </div>
      ) : auctions.length === 0 ? (
        <EmptyState
          icon={<PackageOpen className="h-8 w-8" aria-hidden="true" />}
          title="No auctions yet"
          description="Create your first auction to start selling."
          action={
            <Button onClick={() => setIsCreateModalOpen(true)}>
              <Gavel className="h-4 w-4" aria-hidden="true" />
              Create Your First Auction
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
              <Panel hover className="flex h-full flex-col gap-5 p-6">
                <div className="flex items-start justify-between gap-3">
                  <div className="min-w-0">
                    <h3 className="line-clamp-1 font-display text-lg font-semibold text-white">
                      {auction.title}
                    </h3>
                    <p className="mt-0.5 line-clamp-1 text-sm text-slate-400">
                      {auction.description}
                    </p>
                  </div>
                  <Badge
                    tone={auctionStatusTone(auction.status)}
                    dot={auction.status === AuctionStatus.ACTIVE}
                    pulse={auction.status === AuctionStatus.ACTIVE}
                    className="shrink-0"
                  >
                    {auctionStatusLabel(auction.status)}
                  </Badge>
                </div>

                <div className="grid grid-cols-3 gap-4 rounded-xl border border-slate-700/70 bg-slate-900/40 px-4 py-3">
                  <div>
                    <p className="text-[11px] font-medium uppercase tracking-wider text-slate-400">
                      Current
                    </p>
                    <p className="mt-0.5 truncate text-sm font-bold text-white">
                      {formatCurrency(auction.currentPrice)}
                    </p>
                  </div>
                  <div>
                    <p className="text-[11px] font-medium uppercase tracking-wider text-slate-400">
                      Bids
                    </p>
                    <p className="mt-0.5 text-sm font-bold text-white">
                      {auction.highestBidderId ? '1+' : '0'}
                    </p>
                  </div>
                  <div>
                    <p className="text-[11px] font-medium uppercase tracking-wider text-slate-400">
                      Ends
                    </p>
                    <p className="mt-0.5 truncate text-xs font-semibold text-slate-300">
                      {formatDateTime(auction.endTime)}
                    </p>
                  </div>
                </div>

                <div className="mt-auto flex flex-wrap gap-2">
                  {auction.status === AuctionStatus.PENDING && (
                    <Button
                      variant="secondary"
                      size="sm"
                      onClick={() => startMutation.mutate(auction.id)}
                      disabled={startMutation.isPending}
                      className="!border-emerald-500/40 !text-emerald-300 hover:!bg-emerald-500/10"
                    >
                      <Play className="h-3.5 w-3.5" aria-hidden="true" />
                      Start
                    </Button>
                  )}
                  {auction.status === AuctionStatus.ACTIVE && (
                    <Button
                      variant="secondary"
                      size="sm"
                      onClick={() => closeMutation.mutate(auction.id)}
                      disabled={closeMutation.isPending}
                      className="!border-amber-500/40 !text-amber-300 hover:!bg-amber-500/10"
                    >
                      <Square className="h-3.5 w-3.5" aria-hidden="true" />
                      Close
                    </Button>
                  )}
                  <Button to={`/auction/${auction.id}`} variant="outline" size="sm">
                    <Eye className="h-3.5 w-3.5" aria-hidden="true" />
                    View
                  </Button>
                  <button
                    onClick={() => deleteMutation.mutate(auction.id)}
                    disabled={deleteMutation.isPending}
                    aria-label={`Delete auction ${auction.title}`}
                    className="ml-auto flex h-9 w-9 items-center justify-center rounded-lg border border-rose-500/30 bg-rose-500/10 text-rose-300 transition-colors hover:bg-rose-500/20 hover:text-rose-200 disabled:pointer-events-none disabled:opacity-50"
                  >
                    <Trash2 className="h-4 w-4" aria-hidden="true" />
                  </button>
                </div>
              </Panel>
            </motion.div>
          ))}
        </div>
      )}

      <CreateAuctionModal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
      />
    </div>
  )
}
