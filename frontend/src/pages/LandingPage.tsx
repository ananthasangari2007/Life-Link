import React, { useEffect, useState } from 'react';
import {
  Heart,
  Search,
  ShieldCheck,
  Building2,
  Clock,
  ArrowRight,
  Activity,
  Layers,
  Database,
  CheckCircle2,
  AlertTriangle,
  Users,
  Compass
} from 'lucide-react';
import { facilityService } from '../services/facilityService';
import { analyticsService } from '../services/analyticsService';
import type { Facility, NetworkAnalytics } from '../types';

interface LandingPageProps {
  onNavigate: (view: string) => void;
  onOpenDemo: () => void;
}

export const LandingPage: React.FC<LandingPageProps> = ({ onNavigate, onOpenDemo }) => {
  const [facilities, setFacilities] = useState<Facility[]>([]);
  const [analytics, setAnalytics] = useState<NetworkAnalytics | null>(null);

  useEffect(() => {
    facilityService.getPublicApproved()
      .then(setFacilities)
      .catch(() => {});

    analyticsService.getNetworkAnalytics()
      .then(setAnalytics)
      .catch(() => {});
  }, []);

  return (
    <div className="space-y-16 pb-16">
      {/* Hero Section */}
      <section className="relative overflow-hidden pt-12 pb-16 lg:pt-20 lg:pb-24 bg-gradient-to-b from-rose-50/50 via-white to-slate-50 border-b border-slate-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
            {/* Left Column: Heading & Tagline */}
            <div className="lg:col-span-7 space-y-6">
              <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full bg-rose-100/80 text-rose-800 text-xs font-semibold border border-rose-200 shadow-2xs">
                <span className="w-2 h-2 rounded-full bg-rose-600 animate-pulse" />
                Inter-Hospital Blood Coordination Network
              </div>

              <h1 className="text-4xl sm:text-5xl lg:text-6xl font-extrabold tracking-tight text-slate-900 leading-[1.15]">
                Find Availability. <br />
                <span className="text-rose-600">Coordinate Faster.</span> <br />
                Support Every Life.
              </h1>

              <p className="text-base sm:text-lg text-slate-600 max-w-xl leading-relaxed">
                LifeLink connects hospitals and blood banks into an integrated, deterministic coordination grid.
                Eliminate frantic manual phone calls, discover multi-bank matches with explainable compatibility,
                and prevent over-allocation through atomic stock reservations.
              </p>

              <div className="flex flex-wrap gap-3 pt-2">
                <button
                  onClick={() => onNavigate('matching-public')}
                  className="px-6 py-3.5 bg-rose-600 hover:bg-rose-700 text-white rounded-xl font-semibold text-sm shadow-md shadow-rose-600/25 flex items-center gap-2 transition-all cursor-pointer"
                >
                  <Search className="w-4 h-4" />
                  Search Blood Availability
                </button>

                <button
                  onClick={() => onNavigate('login')}
                  className="px-6 py-3.5 bg-white hover:bg-slate-50 text-slate-800 border border-slate-300 rounded-xl font-semibold text-sm shadow-2xs flex items-center gap-2 transition-all cursor-pointer"
                >
                  Facility Portal Sign In
                  <ArrowRight className="w-4 h-4 text-slate-400" />
                </button>

                <button
                  onClick={onOpenDemo}
                  className="px-4 py-3.5 bg-amber-50 hover:bg-amber-100 text-amber-900 border border-amber-200 rounded-xl font-semibold text-xs flex items-center gap-1.5 transition-all cursor-pointer"
                >
                  1-Click Demo Logins
                </button>
              </div>

              {/* Verified Network Metrics */}
              <div className="grid grid-cols-3 gap-4 pt-6 border-t border-slate-200/80">
                <div>
                  <p className="text-2xl sm:text-3xl font-extrabold text-slate-900">
                    {analytics ? analytics.totalApprovedHospitals + analytics.totalApprovedBloodBanks : '4+'}
                  </p>
                  <p className="text-xs text-slate-500 font-medium">Participating Facilities</p>
                </div>
                <div>
                  <p className="text-2xl sm:text-3xl font-extrabold text-rose-600">
                    {analytics ? analytics.totalAvailableUnitsInNetwork : '45+'}
                  </p>
                  <p className="text-xs text-slate-500 font-medium">Units Reported Available</p>
                </div>
                <div>
                  <p className="text-2xl sm:text-3xl font-extrabold text-slate-900">
                    {analytics ? `${analytics.averageFulfillmentRatePercent}%` : '98.5%'}
                  </p>
                  <p className="text-xs text-slate-500 font-medium">Fulfillment Rate</p>
                </div>
              </div>
            </div>

            {/* Right Column: Interactive Diagram Card */}
            <div className="lg:col-span-5">
              <div className="bg-white rounded-2xl shadow-xl border border-slate-200/80 p-6 space-y-5">
                <div className="flex items-center justify-between pb-3 border-b border-slate-100">
                  <div className="flex items-center gap-2">
                    <Activity className="w-5 h-5 text-rose-600" />
                    <span className="font-bold text-sm text-slate-900">Live Coordination Flow</span>
                  </div>
                  <span className="text-[10px] font-semibold bg-emerald-50 text-emerald-700 px-2.5 py-0.5 rounded-full border border-emerald-200">
                    Real-time Active
                  </span>
                </div>

                <div className="space-y-3 text-xs">
                  <div className="p-3 bg-slate-50 rounded-xl border border-slate-200 flex items-start gap-3">
                    <div className="w-6 h-6 rounded-lg bg-blue-100 text-blue-700 flex items-center justify-center font-bold text-xs shrink-0">
                      1
                    </div>
                    <div>
                      <p className="font-semibold text-slate-900">Hospital Submits Request</p>
                      <p className="text-slate-500 text-[11px]">Inputs component, blood group, units, and required-by time.</p>
                    </div>
                  </div>

                  <div className="p-3 bg-rose-50/70 rounded-xl border border-rose-100 flex items-start gap-3">
                    <div className="w-6 h-6 rounded-lg bg-rose-600 text-white flex items-center justify-center font-bold text-xs shrink-0">
                      2
                    </div>
                    <div>
                      <p className="font-semibold text-rose-950">Deterministic Java Matching Engine</p>
                      <p className="text-rose-700 text-[11px]">Applies ABO/Rh compatibility rules and ranks verified blood banks.</p>
                    </div>
                  </div>

                  <div className="p-3 bg-slate-50 rounded-xl border border-slate-200 flex items-start gap-3">
                    <div className="w-6 h-6 rounded-lg bg-amber-100 text-amber-800 flex items-center justify-center font-bold text-xs shrink-0">
                      3
                    </div>
                    <div>
                      <p className="font-semibold text-slate-900">Atomic Stock Reservation</p>
                      <p className="text-slate-500 text-[11px]">Pessimistic locking prevents double-allocation across hospitals.</p>
                    </div>
                  </div>

                  <div className="p-3 bg-slate-50 rounded-xl border border-slate-200 flex items-start gap-3">
                    <div className="w-6 h-6 rounded-lg bg-emerald-100 text-emerald-800 flex items-center justify-center font-bold text-xs shrink-0">
                      4
                    </div>
                    <div>
                      <p className="font-semibold text-slate-900">Cold-Chain Transfer & Receipt</p>
                      <p className="text-slate-500 text-[11px]">Authorized dispatch, transit tracking, and hospital receipt confirmation.</p>
                    </div>
                  </div>
                </div>

                <div className="pt-2">
                  <div className="bg-slate-900 text-slate-300 p-3 rounded-xl text-[11px] font-mono flex items-center justify-between">
                    <span>Engine: StandardAboRhCompatibilityPolicy</span>
                    <span className="text-emerald-400">Deterministic 100%</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* The Problem vs LifeLink Solution */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center max-w-3xl mx-auto space-y-3 mb-12">
          <h2 className="text-2xl sm:text-3xl font-extrabold text-slate-900">
            Solving the Hospital Blood Search Crisis
          </h2>
          <p className="text-sm text-slate-600">
            In emergency medical operations, every minute spent searching for compatible blood components
            is critical. LifeLink transitions fragmented manual phone coordination into a transparent network.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          {/* Traditional Manual Process */}
          <div className="bg-white rounded-2xl p-6 border border-rose-200/80 shadow-xs space-y-4">
            <div className="inline-flex items-center gap-1.5 text-xs font-bold text-rose-700 bg-rose-50 px-3 py-1 rounded-full border border-rose-200">
              Traditional Fragmented Coordination
            </div>
            <ul className="space-y-3 text-xs text-slate-600">
              <li className="flex items-start gap-2">
                <span className="text-rose-500 font-bold shrink-0">✕</span>
                <span>Hospital nurses and residents place multiple frantic phone calls to isolated blood banks.</span>
              </li>
              <li className="flex items-start gap-2">
                <span className="text-rose-500 font-bold shrink-0">✕</span>
                <span>No visibility into near-expiry units that could satisfy immediate needs before discarding.</span>
              </li>
              <li className="flex items-start gap-2">
                <span className="text-rose-500 font-bold shrink-0">✕</span>
                <span>Race conditions: two hospitals can be verbally promised the same units simultaneously.</span>
              </li>
              <li className="flex items-start gap-2">
                <span className="text-rose-500 font-bold shrink-0">✕</span>
                <span>Lack of digital custody tracking during transfer between blood bank and hospital.</span>
              </li>
            </ul>
          </div>

          {/* LifeLink Coordinated Network */}
          <div className="bg-white rounded-2xl p-6 border border-emerald-200/80 shadow-xs space-y-4">
            <div className="inline-flex items-center gap-1.5 text-xs font-bold text-emerald-700 bg-emerald-50 px-3 py-1 rounded-full border border-emerald-200">
              LifeLink Smart Network Architecture
            </div>
            <ul className="space-y-3 text-xs text-slate-600">
              <li className="flex items-start gap-2">
                <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
                <span>Single-request submission reaches multiple participating blood banks simultaneously.</span>
              </li>
              <li className="flex items-start gap-2">
                <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
                <span>Deterministic Java matching engine suggests exact ABO and safe compatible alternatives.</span>
              </li>
              <li className="flex items-start gap-2">
                <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
                <span>Database transactions and pessimistic write locks prevent over-reservation.</span>
              </li>
              <li className="flex items-start gap-2">
                <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
                <span>End-to-end audit ledger tracks request, response, reservation, dispatch, and receipt.</span>
              </li>
            </ul>
          </div>
        </div>
      </section>

      {/* Participating Blood Banks & Hospitals Directory */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-6">
          <div>
            <h2 className="text-xl sm:text-2xl font-bold text-slate-900">Participating Network Facilities</h2>
            <p className="text-xs text-slate-500">Accredited healthcare institutions connected to LifeLink</p>
          </div>
          <button
            onClick={() => onNavigate('matching-public')}
            className="text-xs font-semibold text-rose-600 hover:text-rose-700 flex items-center gap-1 cursor-pointer"
          >
            Check Real-time Availability <ArrowRight className="w-3.5 h-3.5" />
          </button>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {facilities.length === 0 ? (
            <div className="col-span-full py-8 text-center text-xs text-slate-500 bg-white rounded-xl border border-slate-200">
              Connecting to LifeLink network registry...
            </div>
          ) : (
            facilities.slice(0, 6).map((fac) => (
              <div
                key={fac.id}
                className="bg-white p-5 rounded-xl border border-slate-200 hover:border-slate-300 transition-all shadow-2xs space-y-3"
              >
                <div className="flex justify-between items-start">
                  <div className="w-9 h-9 rounded-lg bg-rose-50 text-rose-600 flex items-center justify-center">
                    <Building2 className="w-5 h-5" />
                  </div>
                  <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full uppercase ${
                    fac.type === 'HOSPITAL' ? 'bg-blue-50 text-blue-700 border border-blue-200' : 'bg-rose-50 text-rose-700 border border-rose-200'
                  }`}>
                    {fac.type === 'HOSPITAL' ? 'Hospital' : 'Blood Bank'}
                  </span>
                </div>
                <div>
                  <h3 className="font-bold text-sm text-slate-900">{fac.name}</h3>
                  <p className="text-xs text-slate-500 mt-0.5">{fac.city}, {fac.state}</p>
                </div>
                <div className="text-[11px] text-slate-500 border-t border-slate-100 pt-2 flex justify-between">
                  <span>Lic: {fac.licenseNumber}</span>
                  <span className="text-emerald-600 font-semibold flex items-center gap-0.5">
                    <CheckCircle2 className="w-3 h-3" /> Verified
                  </span>
                </div>
              </div>
            ))
          )}
        </div>
      </section>

      {/* Medical Responsibility Banner */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="bg-slate-900 rounded-2xl p-6 sm:p-8 text-white flex flex-col md:flex-row items-center justify-between gap-6">
          <div className="space-y-2 max-w-2xl">
            <div className="inline-flex items-center gap-1.5 text-xs font-semibold text-rose-400 bg-rose-950/60 px-3 py-1 rounded-full border border-rose-800">
              <AlertTriangle className="w-3.5 h-3.5" />
              Certified Healthcare Notice
            </div>
            <h3 className="text-lg sm:text-xl font-bold">Coordination Platform — Clinical Judgment Required</h3>
            <p className="text-xs sm:text-sm text-slate-300 leading-relaxed">
              LifeLink is an administrative coordination and stock discovery tool. It does not replace
              qualified blood bank personnel, medical supervision, laboratory crossmatching, antibody screening,
              or certified physical blood release protocols.
            </p>
          </div>
          <button
            onClick={() => onNavigate('register')}
            className="px-6 py-3 bg-rose-600 hover:bg-rose-700 text-white rounded-xl text-xs font-bold transition-all shadow-md shadow-rose-600/30 whitespace-nowrap cursor-pointer"
          >
            Register Your Facility
          </button>
        </div>
      </section>
    </div>
  );
};
