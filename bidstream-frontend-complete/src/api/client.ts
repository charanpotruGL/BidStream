import axios, { AxiosInstance, AxiosError } from "axios";
import {
  AuthCredentials,
  AuthResponse,
  Auction,
  AuctionQueryParams,
  Bid,
  CreateAuctionPayload,
  CreateNotificationPayload,
  ErrorResponse,
  Notification,
  PaginatedResponse,
  PlaceBidPayload,
  RegisterPayload,
  UpdateAuctionPayload,
  User,
} from "../types";

// In development, Vite proxies /api -> http://localhost:8080 (see vite.config.ts).
// In production, set VITE_API_URL to the gateway origin (or serve behind the same origin).
const BASE_URL = (import.meta.env.VITE_API_URL as string) || "";

export class APIClient {
  private client: AxiosInstance;
  private tokenKey = "bidstream_token";
  private userIdKey = "bidstream_user_id";

  constructor() {
    this.client = axios.create({
      baseURL: BASE_URL,
      timeout: 15000,
      headers: {
        "Content-Type": "application/json",
      },
    });

    this.setupInterceptors();
  }

  private setupInterceptors(): void {
    this.client.interceptors.request.use(
      (config) => {
        const token = this.getToken();
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
      },
      (error) => Promise.reject(error)
    );

    this.client.interceptors.response.use(
      (response) => response,
      (error: AxiosError<ErrorResponse>) => {
        if (
          error.response?.status === 401 &&
          window.location.pathname !== "/login"
        ) {
          this.clearToken();
          this.clearUserId();
          window.location.href = "/login";
        }
        return Promise.reject(error);
      }
    );
  }

  public setToken(token: string): void {
    localStorage.setItem(this.tokenKey, token);
  }

  public getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  public clearToken(): void {
    localStorage.removeItem(this.tokenKey);
  }

  public setUserId(id: number): void {
    localStorage.setItem(this.userIdKey, String(id));
  }

  public getUserId(): number | null {
    const raw = localStorage.getItem(this.userIdKey);
    return raw ? Number(raw) : null;
  }

  public clearUserId(): void {
    localStorage.removeItem(this.userIdKey);
  }

  /** Normalize Axios errors into a readable message + field errors. */
  public getErrorMessage(error: unknown): string {
    const axiosError = error as AxiosError<ErrorResponse>;
    const data = axiosError?.response?.data;
    if (data) {
      if (data.message) return data.message;
      if (data.fieldErrors) {
        return Object.values(data.fieldErrors).join(", ");
      }
    }
    if (axiosError?.message && axiosError.message !== "Request failed with status code 400") {
      return axiosError.message;
    }
    return "Something went wrong. Please try again.";
  }

  public async register(payload: RegisterPayload): Promise<AuthResponse> {
    const response = await this.client.post<AuthResponse>("/api/auth/register", payload);
    return response.data;
  }

  public async login(credentials: AuthCredentials): Promise<AuthResponse> {
    const response = await this.client.post<AuthResponse>("/api/auth/login", credentials);
    return response.data;
  }

  public async getUserById(id: number): Promise<User> {
    const response = await this.client.get<User>(`/api/users/${id}`);
    return response.data;
  }

  public async getUserByUsername(username: string): Promise<User> {
    const response = await this.client.get<User>(`/api/users/username/${username}`);
    return response.data;
  }

  public async createAuction(payload: CreateAuctionPayload): Promise<Auction> {
    const response = await this.client.post<Auction>("/api/auctions", payload);
    return response.data;
  }

  public async getAuctions(params?: AuctionQueryParams): Promise<PaginatedResponse<Auction>> {
    const response = await this.client.get<PaginatedResponse<Auction>>("/api/auctions", { params });
    return response.data;
  }

  public async getAuctionById(id: number): Promise<Auction> {
    const response = await this.client.get<Auction>(`/api/auctions/${id}`);
    return response.data;
  }

  public async getAuctionsByStatus(status: string): Promise<PaginatedResponse<Auction>> {
    const response = await this.client.get<PaginatedResponse<Auction>>(`/api/auctions/status/${status}`);
    return response.data;
  }

  public async getAuctionsBySeller(sellerId: number): Promise<Auction[]> {
    const response = await this.client.get<Auction[]>(`/api/auctions/seller/${sellerId}`);
    return response.data;
  }

  public async getActiveAuctionCount(): Promise<number> {
    const response = await this.client.get<number>("/api/auctions/stats/active-count");
    return response.data;
  }

  public async updateAuction(id: number, payload: UpdateAuctionPayload): Promise<Auction> {
    const response = await this.client.put<Auction>(`/api/auctions/${id}`, payload);
    return response.data;
  }

  public async startAuction(id: number): Promise<Auction> {
    const response = await this.client.post<Auction>(`/api/auctions/${id}/start`);
    return response.data;
  }

  public async closeAuction(id: number): Promise<Auction> {
    const response = await this.client.post<Auction>(`/api/auctions/${id}/close`);
    return response.data;
  }

  public async deleteAuction(id: number): Promise<void> {
    await this.client.delete(`/api/auctions/${id}`);
  }

  public async placeBid(payload: PlaceBidPayload): Promise<Bid> {
    const response = await this.client.post<Bid>("/api/bids", payload);
    return response.data;
  }

  public async getBidById(id: number): Promise<Bid> {
    const response = await this.client.get<Bid>(`/api/bids/${id}`);
    return response.data;
  }

  public async getBidsByAuction(auctionId: number): Promise<Bid[]> {
    const response = await this.client.get<Bid[]>(`/api/bids/auction/${auctionId}`);
    return response.data;
  }

  public async getHighestBid(auctionId: number): Promise<Bid> {
    const response = await this.client.get<Bid>(`/api/bids/auction/${auctionId}/highest`);
    return response.data;
  }

  public async getBidsByBidder(bidderId: number): Promise<Bid[]> {
    const response = await this.client.get<Bid[]>(`/api/bids/bidder/${bidderId}`);
    return response.data;
  }

  public async createNotification(payload: CreateNotificationPayload): Promise<Notification> {
    const response = await this.client.post<Notification>("/api/notifications", payload);
    return response.data;
  }

  public async getNotificationById(id: number): Promise<Notification> {
    const response = await this.client.get<Notification>(`/api/notifications/${id}`);
    return response.data;
  }

  public async getMyNotifications(): Promise<Notification[]> {
    const response = await this.client.get<Notification[]>("/api/notifications/user/me");
    return response.data;
  }

  public async getUnreadNotifications(): Promise<Notification[]> {
    const response = await this.client.get<Notification[]>("/api/notifications/user/me/unread");
    return response.data;
  }

  public async getUnreadNotificationCount(): Promise<number> {
    const response = await this.client.get<number>("/api/notifications/user/me/count");
    return response.data;
  }

  public async markNotificationAsRead(id: number): Promise<Notification> {
    const response = await this.client.put<Notification>(`/api/notifications/${id}/read`);
    return response.data;
  }

  public async markAllNotificationsAsRead(): Promise<void> {
    await this.client.put("/api/notifications/user/me/read-all");
  }

  public async deleteNotification(id: number): Promise<void> {
    await this.client.delete(`/api/notifications/${id}`);
  }
}

export const apiClient = new APIClient();
