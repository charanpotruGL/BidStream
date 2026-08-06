// ============================================================================
// BidStream TypeScript Types & Interfaces
// ============================================================================

/**
 * Authentication & User Types
 */
export interface AuthCredentials {
  usernameOrEmail: string;
  password: string;
}

export interface RegisterPayload {
  username: string;
  fullName: string;
  email: string;
  role: UserRole;
  password: string;
}

export enum UserRole {
  USER = "USER",
  SELLER = "SELLER",
  ADMIN = "ADMIN",
}

export interface User {
  id: number;
  username: string;
  email: string;
  fullName: string;
  role: UserRole;
  active: boolean;
  createdAt: string;
}

export interface AuthResponse {
  token: string;
  tokenType: "Bearer";
  userId: number;
  username: string;
  email: string;
  role: UserRole;
  expiresIn: number;
}

/**
 * Auction Types
 */
export enum AuctionStatus {
  PENDING = "PENDING",
  ACTIVE = "ACTIVE",
  CLOSED = "CLOSED",
  EXPIRED = "EXPIRED",
}

export interface Auction {
  id: number;
  title: string;
  description: string;
  sellerId: number;
  startingPrice: number;
  currentPrice: number;
  highestBidId: number | null;
  highestBidderId: number | null;
  startTime: string; // ISO 8601
  endTime: string; // ISO 8601
  status: AuctionStatus;
  createdAt: string;
}

export interface CreateAuctionPayload {
  title: string;
  description: string;
  startingPrice: number;
  sellerId: number;
  startTime: string;
  endTime: string;
}

export interface UpdateAuctionPayload {
  title?: string;
  description?: string;
  startingPrice?: number;
}

export interface PaginatedResponse<T> {
  content: T[];
  empty: boolean;
  first: boolean;
  last: boolean;
  number: number;
  numberOfElements: number;
  pageable: Pageable;
  size: number;
  sort: Sort;
  totalElements: number;
  totalPages: number;
}

export interface Pageable {
  offset: number;
  pageNumber: number;
  pageSize: number;
  paged: boolean;
  sort: Sort;
  unpaged: boolean;
}

export interface Sort {
  empty: boolean;
  sorted: boolean;
  unsorted: boolean;
}

export interface AuctionQueryParams {
  page?: number;
  size?: number;
  sort?: string;
  direction?: "asc" | "desc";
}

/**
 * Bid Types
 */
export enum BidStatus {
  PLACED = "PLACED",
  OUTBID = "OUTBID",
}

export interface Bid {
  id: number;
  auctionId: number;
  bidderId: number;
  amount: number;
  status: BidStatus;
  createdAt: string;
}

export interface PlaceBidPayload {
  amount: number;
  bidderId: number;
  auctionId: number;
}

/**
 * Notification Types
 */
export enum NotificationType {
  AUCTION_CREATED = "AUCTION_CREATED",
  AUCTION_STARTED = "AUCTION_STARTED",
  AUCTION_CLOSED = "AUCTION_CLOSED",
  BID_PLACED = "BID_PLACED",
  BID_OUTBID = "BID_OUTBID",
  INFO = "INFO",
}

export interface Notification {
  id: number;
  userId: number;
  notificationType: NotificationType;
  title: string;
  message: string;
  read: boolean;
  createdAt: string;
}

export interface CreateNotificationPayload {
  userId: number;
  notificationType: NotificationType;
  title: string;
  message: string;
}

/**
 * Error Response Type
 */
export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  fieldErrors: Record<string, string> | null;
}

/**
 * Request/Response Interceptor Types
 */
export interface RequestConfig {
  headers: {
    Authorization?: string;
    "Content-Type"?: string;
    [key: string]: string | undefined;
  };
}
