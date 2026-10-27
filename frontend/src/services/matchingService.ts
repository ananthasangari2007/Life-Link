import { api } from './api';
import type { FindMatchesRequest, MatchResult } from '../types';

export const matchingService = {
  findMatches: (request: FindMatchesRequest) =>
    api.post<MatchResult>('/matching/find-matches', request),
};
