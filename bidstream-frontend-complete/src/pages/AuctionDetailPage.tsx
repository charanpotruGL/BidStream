import React from 'react'
import { useParams } from 'react-router-dom'
import { ArrowLeft } from 'lucide-react'
import { AuctionDetailView } from '../components/auction/AuctionDetailView'
import { Button } from '../components/ui/Button'
import { EmptyState } from '../components/ui/EmptyState'

export const AuctionDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>()
  const auctionId = Number(id)

  if (!auctionId) {
    return (
      <EmptyState
        icon={<ArrowLeft className="h-8 w-8" aria-hidden="true" />}
        title="Invalid auction id"
        description="The auction you're looking for doesn't exist."
        action={
          <Button to="/auctions" variant="outline">
            Back to Auctions
          </Button>
        }
      />
    )
  }

  return (
    <div className="flex flex-col gap-6">
      <Button to="/auctions" variant="ghost" size="sm" className="self-start">
        <ArrowLeft className="h-4 w-4" aria-hidden="true" />
        Back to Auctions
      </Button>
      <AuctionDetailView auctionId={auctionId} />
    </div>
  )
}
