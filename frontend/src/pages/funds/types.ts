export interface FundMember {
  name: string;
  email: string;
  avatar: string;
  role: string;
}

export interface GroupFund {
  id: number;
  name: string;
  target: number;
  balance: number;
  membersCount: number;
  status: 'active' | 'settled';
  themeColor: string;
  members: FundMember[];
}
