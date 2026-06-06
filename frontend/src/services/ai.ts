import api from '@/utils/api';

export interface AiAvailability {
  aiEnabled: boolean;
  aiAvailable: boolean;
  ragEnabled: boolean;
  ragAvailable: boolean;
  rebuildStatus?: string;
  knowledgeBaseHash?: string;
  proRequired?: boolean;
  aiAccessible?: boolean;
  allowedRoles?: string[];
  errorCode?: string;
  reason?: string;
}

export interface AiInsight {
  month: string;
  summary: string;
  detailedAdvice?: string;
  fromCache: boolean;
  generatedAt?: string;
  errorCode?: string;
}

export interface AiCitation {
  title?: string;
  sourceKey?: string;
  sourceUrl?: string;
  score?: number;
}

export interface AiChatResponse {
  sessionId?: string;
  reply: string;
  citations?: AiCitation[];
  aiEnabled: boolean;
  ragAvailable: boolean;
  errorCode?: string;
  disclaimer?: string;
}

export async function getAiStatus() {
  const res = await api.get<AiAvailability>('/ai/status');
  return res.data;
}

export async function getAiInsight(month?: string, forceRefresh = false) {
  const res = await api.get<AiInsight>('/ai/insight', {
    params: {
      ...(month ? { month } : {}),
      ...(forceRefresh ? { forceRefresh: true } : {}),
    },
  });
  return res.data;
}

export async function sendAiChat(message: string, sessionId?: string) {
  const res = await api.post<AiChatResponse>('/ai/chat', {
    message,
    sessionId,
  });
  return res.data;
}
