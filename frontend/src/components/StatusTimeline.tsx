import React from 'react';
import { Check, Clock, Truck, ShieldCheck, XCircle, AlertCircle } from 'lucide-react';
import type { RequestStatus } from '../types';

interface StatusTimelineProps {
  status: RequestStatus;
}

export const StatusTimeline: React.FC<StatusTimelineProps> = ({ status }) => {
  if (status === 'CANCELLED' || status === 'REJECTED' || status === 'UNFULFILLED') {
    return (
      <div className="flex items-center gap-2 px-3 py-2 bg-rose-50 border border-rose-200 rounded-lg text-rose-800 text-xs">
        <XCircle className="w-4 h-4 text-rose-600 shrink-0" />
        <span className="font-semibold">Request Terminated: {status}</span>
      </div>
    );
  }

  const steps = [
    { key: 'SUBMITTED', label: 'Submitted', icon: Clock },
    { key: 'MATCHING', label: 'Matching', icon: AlertCircle },
    { key: 'AWAITING_BANK_RESPONSES', label: 'Bank Responses', icon: Clock },
    { key: 'ACCEPTED', label: 'Accepted & Reserved', icon: ShieldCheck },
    { key: 'IN_TRANSFER', label: 'In Transit', icon: Truck },
    { key: 'FULFILLED', label: 'Fulfilled', icon: Check },
  ];

  const getStepIndex = (st: RequestStatus) => {
    switch (st) {
      case 'DRAFT': return 0;
      case 'SUBMITTED': return 1;
      case 'MATCHING': return 2;
      case 'AWAITING_BANK_RESPONSES': return 3;
      case 'PARTIALLY_FULFILLED': return 3;
      case 'ACCEPTED': return 4;
      case 'IN_TRANSFER': return 5;
      case 'FULFILLED': return 6;
      default: return 1;
    }
  };

  const currentIndex = getStepIndex(status);

  return (
    <div className="w-full py-2">
      <div className="flex items-center justify-between relative">
        <div className="absolute left-0 top-1/2 -translate-y-1/2 h-0.5 w-full bg-slate-200 -z-0" />
        <div
          className="absolute left-0 top-1/2 -translate-y-1/2 h-0.5 bg-rose-600 transition-all duration-500 -z-0"
          style={{ width: `${Math.min(100, ((currentIndex - 1) / (steps.length - 1)) * 100)}%` }}
        />

        {steps.map((s, idx) => {
          const stepNum = idx + 1;
          const isDone = currentIndex > stepNum;
          const isCurrent = currentIndex === stepNum;
          const Icon = s.icon;

          return (
            <div key={s.key} className="flex flex-col items-center relative z-10">
              <div
                className={`w-7 h-7 rounded-full flex items-center justify-center text-xs font-semibold transition-all ${
                  isDone
                    ? 'bg-rose-600 text-white ring-4 ring-rose-100'
                    : isCurrent
                    ? 'bg-white text-rose-600 border-2 border-rose-600 ring-4 ring-rose-100 animate-pulse'
                    : 'bg-white text-slate-400 border border-slate-300'
                }`}
              >
                {isDone ? <Check className="w-3.5 h-3.5" /> : <Icon className="w-3.5 h-3.5" />}
              </div>
              <span className={`text-[10px] mt-1 hidden sm:block whitespace-nowrap font-medium ${
                isCurrent ? 'text-rose-600 font-bold' : isDone ? 'text-slate-800' : 'text-slate-400'
              }`}>
                {s.label}
              </span>
            </div>
          );
        })}
      </div>
    </div>
  );
};
