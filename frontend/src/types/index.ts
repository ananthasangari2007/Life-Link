export type UserRole = 'HOSPITAL_USER' | 'BLOOD_BANK_USER' | 'NETWORK_ADMIN' | 'AUDITOR';

export type BloodGroup =
  | 'A_POSITIVE'
  | 'A_NEGATIVE'
  | 'B_POSITIVE'
  | 'B_NEGATIVE'
  | 'AB_POSITIVE'
  | 'AB_NEGATIVE'
  | 'O_POSITIVE'
  | 'O_NEGATIVE';

export type ComponentType =
  | 'WHOLE_BLOOD'
  | 'RED_BLOOD_CELLS'
  | 'PLATELETS'
  | 'FRESH_FROZEN_PLASMA'
  | 'CRYOPRECIPITATE';

export type RequestUrgency = 'ROUTINE' | 'URGENT' | 'CRITICAL';

export type RequestStatus =
  | 'DRAFT'
  | 'SUBMITTED'
  | 'MATCHING'
  | 'AWAITING_BANK_RESPONSES'
  | 'PARTIALLY_FULFILLED'
  | 'ACCEPTED'
  | 'IN_TRANSFER'
  | 'FULFILLED'
  | 'UNFULFILLED'
  | 'CANCELLED'
  | 'REJECTED';

export type BankResponseStatus = 'PENDING' | 'ACCEPTED' | 'PARTIALLY_ACCEPTED' | 'REJECTED' | 'WITHDRAWN';

export type ReservationStatus = 'ACTIVE' | 'CONFIRMED' | 'EXPIRED' | 'RELEASED' | 'DISPATCHED';

export type TransferStatus = 'PREPARED' | 'DISPATCHED' | 'IN_TRANSIT' | 'RECEIVED' | 'CANCELLED';

export type FacilityType = 'HOSPITAL' | 'BLOOD_BANK';
export type FacilityStatus = 'PENDING' | 'APPROVED' | 'SUSPENDED' | 'REJECTED';

export interface User {
  id: number;
  email: string;
  fullName: string;
  phone?: string;
  role: UserRole;
  facilityId?: number;
  facilityName?: string;
  facilityType?: FacilityType;
  designation?: string;
  active: boolean;
}

export interface AuthResponse {
  token: string;
  refreshToken: string;
  tokenType: string;
  user: User;
}

export interface Facility {
  id: number;
  name: string;
  licenseNumber: string;
  type: FacilityType;
  status: FacilityStatus;
  address: string;
  city: string;
  state: string;
  pincode: string;
  contactPhone: string;
  contactEmail: string;
  latitude?: number;
  longitude?: number;
  operatingHours?: string;
  emergencyContact?: string;
  approvalNotes?: string;
}

export interface BloodInventoryBatch {
  id: number;
  facilityId: number;
  facilityName: string;
  batchNumber: string;
  bloodGroup: BloodGroup;
  bloodGroupDisplay: string;
  componentType: ComponentType;
  componentTypeDisplay: string;
  quantity: number;
  reservedQuantity: number;
  dispatchedQuantity: number;
  availableQuantity: number;
  collectionDate?: string;
  expiryDate: string;
  daysUntilExpiry: number;
  status: 'AVAILABLE' | 'QUARANTINED' | 'EXPIRED' | 'DEPLETED';
  storageLocation?: string;
  notes?: string;
  nearExpiry: boolean;
  expired: boolean;
}

export interface InventorySummary {
  totalUnits: number;
  availableUnits: number;
  reservedUnits: number;
  dispatchedUnits: number;
  nearExpiryUnits: number;
  quarantinedUnits: number;
  stockByBloodGroup: Record<string, number>;
  stockByComponent: Record<string, number>;
}

export interface FindMatchesRequest {
  bloodGroup: BloodGroup;
  componentType: ComponentType;
  unitsRequested: number;
  hospitalLocation?: string;
  requiredBy?: string;
  urgency?: RequestUrgency;
}

export interface CandidateFacilityMatch {
  facilityId: number;
  facilityName: string;
  address: string;
  city: string;
  contactPhone: string;
  contactEmail: string;
  bloodGroup: BloodGroup;
  bloodGroupDisplay: string;
  availableUnits: number;
  compatibilityScore: number;
  compatibilityExplanation: string;
  canFulfillEntirely: boolean;
  availableBatches: BloodInventoryBatch[];
}

export interface PlanSegment {
  facilityId: number;
  facilityName: string;
  city: string;
  bloodGroup: BloodGroup;
  bloodGroupDisplay: string;
  unitsAllocated: number;
  reason: string;
}

export interface FulfillmentPlan {
  planType: 'SINGLE_BANK' | 'MULTI_BANK_SPLIT';
  title: string;
  summary: string;
  totalUnitsProposed: number;
  completesRequest: boolean;
  segments: PlanSegment[];
}

export interface MatchResult {
  requestedGroup: BloodGroup;
  requestedGroupDisplay: string;
  componentType: ComponentType;
  componentTypeDisplay: string;
  unitsRequested: number;
  totalAvailableInNetwork: number;
  fullMatchFound: boolean;
  candidateFacilities: CandidateFacilityMatch[];
  suggestedPlans: FulfillmentPlan[];
  medicalNotice: string;
}

export interface RequestResponseItem {
  id: number;
  bloodBankId: number;
  bloodBankName: string;
  status: BankResponseStatus;
  offeredUnits: number;
  notes?: string;
  respondedByUserId?: number;
  responderName?: string;
  respondedAt?: string;
  createdAt: string;
}

export interface BloodRequest {
  id: number;
  requestReference: string;
  hospitalId: number;
  hospitalName: string;
  requesterUserId: number;
  requesterName: string;
  bloodGroup: BloodGroup;
  bloodGroupDisplay: string;
  componentType: ComponentType;
  componentTypeDisplay: string;
  unitsRequested: number;
  unitsFulfilled: number;
  urgency: RequestUrgency;
  status: RequestStatus;
  requiredBy: string;
  patientReferenceCode?: string;
  clinicalNotes?: string;
  createdAt: string;
  updatedAt: string;
  responses: RequestResponseItem[];
}

export interface StockReservation {
  id: number;
  reservationReference: string;
  requestId: number;
  requestReference: string;
  bloodBankId: number;
  bloodBankName: string;
  batchId: number;
  batchNumber: string;
  bloodGroupDisplay: string;
  componentTypeDisplay: string;
  unitsReserved: number;
  status: ReservationStatus;
  expiresAt: string;
  expired: boolean;
  createdAt: string;
}

export interface BloodTransfer {
  id: number;
  transferReference: string;
  requestId: number;
  requestReference: string;
  reservationId: number;
  sourceFacilityId: number;
  sourceFacilityName: string;
  destinationFacilityId: number;
  destinationFacilityName: string;
  unitsTransferred: number;
  status: TransferStatus;
  dispatcherUserId?: number;
  dispatcherName?: string;
  receiverUserId?: number;
  receiverName?: string;
  courierDetails?: string;
  temperatureLogSummary?: string;
  dispatchedAt?: string;
  receivedAt?: string;
  notes?: string;
  createdAt: string;
}

export interface AppNotification {
  id: number;
  title: string;
  message: string;
  type: 'INFO' | 'URGENT' | 'ALERT' | 'CONFIRMATION';
  actionUrl?: string;
  isRead: boolean;
  createdAt: string;
}

export interface NetworkAnalytics {
  totalApprovedHospitals: number;
  totalApprovedBloodBanks: number;
  totalAvailableUnitsInNetwork: number;
  totalReservedUnitsInNetwork: number;
  totalRequestsCreated: number;
  totalRequestsFulfilled: number;
  activeTransfersCount: number;
  averageFulfillmentRatePercent: number;
  stockByBloodGroup: Record<string, number>;
  stockByComponent: Record<string, number>;
}

export interface ShortageMatrixItem {
  bloodGroup: string;
  componentType: string;
  availableUnits: number;
  minimumSafetyThreshold: number;
  shortageSeverity: 'NORMAL' | 'LOW' | 'CRITICAL';
}

export interface AuditLog {
  id: number;
  userId?: number;
  userEmail?: string;
  userRole?: string;
  facilityId?: number;
  action: string;
  entityType: string;
  entityId?: number;
  details?: string;
  ipAddress?: string;
  timestamp: string;
}
