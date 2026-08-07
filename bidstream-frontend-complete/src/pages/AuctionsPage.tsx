import React, { useState, useMemo } from 'react'
import { useQuery } from '@tanstack/react-query'
import { motion } from 'framer-motion'
import { Grid, List, Plus, AlertTriangle, PackageOpen, ChevronLeft, ChevronRight, SearchX } from 'lucide-react'
import { Link, useSearchParams } from 'react-router-dom'
import { AuctionCard } from '../components/auction/AuctionCard'
import { CreateAuctionModal } from '../components/auction/CreateAuctionModal'
import { apiClient } from '../api/client'
import { useAuthStore } from '../store'
import { AuctionStatus, AuctionQueryParams } from '../types'
import { formatCurrency } from '../utils'
import { toast } from 'sonner'
import { PageHeader } from '../components/ui/PageHeader'
import { Select } from '../components/ui/Select'
import { Button } from '../components/ui/Button'
import { Panel } from '../components/ui/Panel'
import { Skeleton } from '../components/ui/Skeleton'
import { EmptyState } from '../components/ui/EmptyState'
import { Badge } from '../components/ui/Badge'
import { auctionStatusTone, auctionStatusLabel } from '../components/ui/auctionStatus'
import { cn } from '../lib/cn'

// ============================================================================
// Auctions Page - Main Marketplace View
// ============================================================================

type SortMode = 'price' | 'time-remaining' | 'newest'

export const AuctionsPage: React.FC = () => {
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid')
  const [statusFilter, setStatusFilter] = useState<AuctionStatus | 'ALL'>('ALL')
  const [sortBy, setSortBy] = useState<SortMode>('price')
  const [currentPage, setCurrentPage] = useState(0)
  const [pageSize, setPageSize] = useState(12)
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false)
  const [searchParams, setSearchParams] = useSearchParams()
  const { user } = useAuthStore()

  const searchTerm = (searchParams.get('q') ?? '').trim().toLowerCase()

  const queryParams: AuctionQueryParams = useMemo(() => {
    const params: AuctionQueryParams = {
      page: currentPage,
      size: pageSize,
    }

    if (sortBy === 'price') {
      params.sort = 'currentPrice'
      params.direction = 'desc'
    } else if (sortBy === 'time-remaining') {
      params.sort = 'endTime'
      params.direction = 'asc'
    } else if (sortBy === 'newest') {
      params.sort = 'createdAt'
      params.direction = 'desc'
    }

    return params
  }, [currentPage, pageSize, sortBy])

  const { data: auctionData, isLoading, error } = useQuery({
    queryKey: ['auctions', queryParams, statusFilter],
    queryFn: async () => {
      if (statusFilter === 'ALL') {
        return await apiClient.getAuctions(queryParams)
      }
      return await apiClient.getAuctionsByStatus(statusFilter, {
        page: currentPage,
        size: pageSize,
      })
    },
    staleTime: 30000,
    gcTime: 5 * 60 * 1000,
  })

  // The status endpoint returns unsorted pages, so sort all views client-side
  // for consistent ordering regardless of filter.
  const auctions = useMemo(() => {
    const list = auctionData?.content ?? []
    const filtered = searchTerm
      ? list.filter(
          (a) =>
            a.title.toLowerCase().includes(searchTerm) ||
            a.description.toLowerCase().includes(searchTerm)
        )
      : list

    const sorted = [...filtered]
    if (sortBy === 'price') {
      sorted.sort((a, b) => b.currentPrice - a.currentPrice)
    } else if (sortBy === 'time-remaining') {
      sorted.sort(
        (a, b) => new Date(a.endTime).getTime() - new Date(b.endTime).getTime()
      )
    } else {
      sorted.sort(
        (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
      )
    }
    return sorted
  }, [auctionData, sortBy, searchTerm])

  const clearSearch = () => {
    searchParams.delete('q')
    setSearchParams(searchParams, { replace: true })
  }

  const handleQuickBid = async (auctionId: number, amount: number) => {
    if (!user) {
      toast.error('Please login to place a bid')
      return
    }

    try {
      await apiClient.placeBid({
        amount,
        bidderId: user.id,
        auctionId,
      })
      toast.success(`Bid placed for $${amount.toFixed(2)}`)
    } catch (err) {
      toast.error(apiClient.getErrorMessage(err))
    }
  }

  const canCreate = user?.role === 'SELLER' || user?.role === 'ADMIN'
  const totalPages = auctionData?.totalPages ?? 0
  const hasSearch = searchTerm.length > 0

  return (
    <div className="flex flex-col gap-8">
      <PageHeader
        eyebrow="Marketplace"
        title="Live Auctions"
        description="Discover and bid on exclusive items in real-time"
        actions={
          canCreate ? (
            <Button onClick={() => setIsCreateModalOpen(true)}>
              <Plus className="h-4 w-4" aria-hidden="true" />
              Create Auction
            </Button>
          ) : undefined
        }
      />

      {/* Filters */}
      <Panel className="p-4 sm:p-5">
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <Select
            label="Status"
            value={statusFilter}
            onChange={(e) => {
              setStatusFilter(e.target.value as AuctionStatus | 'ALL')
              setCurrentPage(0)
            }}
          >
            <option value="ALL">All Auctions</option>
            <option value={AuctionStatus.ACTIVE}>Active</option>
            <option value={AuctionStatus.PENDING}>Pending</option>
            <option value={AuctionStatus.CLOSED}>Closed</option>
          </Select>

          <Select
            label="Sort by"
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value as typeof sortBy)}
          >
            <option value="price">Price: High to Low</option>
            <option value="time-remaining">Ending Soonest</option>
            <option value="newest">Newest First</option>
          </Select>

          <Select
            label="Per page"
            value={pageSize}
            onChange={(e) => {
              setPageSize(parseInt(e.target.value))
              setCurrentPage(0)
            }}
          >
            <option value={12}>12 items</option>
            <option value={24}>24 items</option>
            <option value={36}>36 items</option>
          </Select>

          <div>
            <span className="mb-1.5 block text-sm font-medium text-slate-300">View</span>
            <div className="flex gap-1 rounded-xl border border-slate-700 bg-slate-900/70 p-1">
              <button
                onClick={() => setViewMode('grid')}
                aria-label="Grid view"
                aria-pressed={viewMode === 'grid'}
                className={cn(
                  'flex h-9 flex-1 items-center justify-center gap-2 rounded-lg text-sm font-medium transition-all',
                  viewMode === 'grid'
                    ? 'bg-brand-500 text-brand-950 shadow'
                    : 'text-slate-400 hover:text-white'
                )}
              >
                <Grid className="h-4 w-4" aria-hidden="true" />
                Grid
              </button>
              <button
                onClick={() => setViewMode('list')}
                aria-label="List view"
                aria-pressed={viewMode === 'list'}
                className={cn(
                  'flex h-9 flex-1 items-center justify-center gap-2 rounded-lg text-sm font-medium transition-all',
                  viewMode === 'list'
                    ? 'bg-brand-500 text-brand-950 shadow'
                    : 'text-slate-400 hover:text-white'
                )}
              >
                <List className="h-4 w-4" aria-hidden="true" />
                List
              </button>
            </div>
          </div>
        </div>
      </Panel>

      {/* Content */}
      {isLoading && (
        <div
          className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4"
          role="status"
          aria-label="Loading auctions"
        >
          {Array.from({ length: Math.min(pageSize, 8) }).map((_, i) => (
            <Skeleton key={i} className="h-[380px]" />
          ))}
        </div>
      )}

      {error && (
        <EmptyState
          icon={<AlertTriangle className="h-8 w-8" aria-hidden="true" />}
          title="Failed to load auctions"
          description="We couldn't reach the marketplace right now. Please try again."
        />
      )}

      {!isLoading && !error && auctions.length === 0 && !hasSearch && (
        <EmptyState
          icon={<PackageOpen className="h-8 w-8" aria-hidden="true" />}
          title="No auctions found"
          description="Try adjusting your filters or check back later."
        />
      )}

      {!isLoading && !error && auctions.length === 0 && hasSearch && (
        <EmptyState
          icon={<SearchX className="h-8 w-8" aria-hidden="true" />}
          title="No matching auctions"
          description={`Nothing found for "${searchTerm}". Try a different search.`}
          action={
            <Button variant="outline" onClick={clearSearch}>
              Clear search
            </Button>
          }
        />
      )}

      {!isLoading && !error && auctions.length > 0 && viewMode === 'grid' && (
        <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
          {auctions.map((auction, index) => (
            <motion.div
              key={auction.id}
              initial={{ opacity: 0, y: 16 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: Math.min(index * 0.04, 0.4) }}
              className="h-full"
            >
              <AuctionCard
                auction={auction}
                onQuickBid={(amount) => handleQuickBid(auction.id, amount)}
              />
            </motion.div>
          ))}
        </div>
      )}

      {!isLoading && !error && auctions.length > 0 && viewMode === 'list' && (
        <div className="space-y-3">
          {auctions.map((auction) => (
            <motion.div
              key={auction.id}
              initial={{ opacity: 0, x: -12 }}
              animate={{ opacity: 1, x: 0 }}
              className="group flex items-center gap-4 rounded-2xl border border-slate-700/70 bg-slate-800/60 p-4 transition-all hover:border-slate-600 hover:bg-slate-800"
            >
              <Link
                to={`/auction/${auction.id}`}
                className="flex h-20 w-20 flex-shrink-0 items-center justify-center rounded-xl border border-slate-700 bg-slate-900"
                aria-label={auction.title}
              >
                <span className="text-2xl font-bold text-brand-500/60">
                  {auction.title.charAt(0).toUpperCase()}
                </span>
              </Link>
              <div className="min-w-0 flex-1">
                <div className="flex items-center gap-2">
                  <Link
                    to={`/auction/${auction.id}`}
                    className="line-clamp-1 font-display text-lg font-semibold text-white transition-colors group-hover:text-brand-200"
                  >
                    {auction.title}
                  </Link>
                  <Badge tone={auctionStatusTone(auction.status)} dot={auction.status === AuctionStatus.ACTIVE} pulse={auction.status === AuctionStatus.ACTIVE}>
                    {auctionStatusLabel(auction.status)}
                  </Badge>
                </div>
                <p className="mt-0.5 line-clamp-1 text-sm text-slate-400">
                  {auction.description}
                </p>
                <p className="mt-1.5 text-sm font-semibold text-white">
                  {formatCurrency(auction.currentPrice)}
                </p>
              </div>
              <Button to={`/auction/${auction.id}`} variant="outline" size="sm" className="flex-shrink-0">
                View
              </Button>
            </motion.div>
          ))}
        </div>
      )}

      {/* Pagination */}
      {!isLoading && !error && totalPages > 1 && (
        <div className="flex items-center justify-center gap-4">
          <Button
            variant="secondary"
            size="sm"
            onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
            disabled={currentPage === 0}
          >
            <ChevronLeft className="h-4 w-4" aria-hidden="true" />
            Previous
          </Button>

          <div className="flex items-center gap-1.5">
            {Array.from({ length: totalPages }).map((_, i) => (
              <button
                key={i}
                onClick={() => setCurrentPage(i)}
                aria-label={`Page ${i + 1}`}
                aria-current={currentPage === i ? 'page' : undefined}
                className={cn(
                  'flex h-9 w-9 items-center justify-center rounded-lg text-sm font-medium transition-all',
                  currentPage === i
                    ? 'bg-brand-500 text-brand-950 shadow'
                    : 'text-slate-400 hover:bg-slate-800 hover:text-white'
                )}
              >
                {i + 1}
              </button>
            ))}
          </div>

          <Button
            variant="secondary"
            size="sm"
            onClick={() => setCurrentPage(Math.min(totalPages - 1, currentPage + 1))}
            disabled={currentPage === totalPages - 1}
          >
            Next
            <ChevronRight className="h-4 w-4" aria-hidden="true" />
          </Button>
        </div>
      )}

      <CreateAuctionModal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
      />
    </div>
  )
}
