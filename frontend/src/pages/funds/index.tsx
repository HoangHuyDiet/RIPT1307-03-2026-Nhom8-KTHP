import React, { useState, useEffect } from 'react';
import { useLocation, history } from 'umi';
import request from '@/utils/request';
import { useAuthStore } from '@/store/useAuthStore';
import { GroupFund } from './types';
import GroupListView from './components/GroupListView';
import GroupDetailView from './components/GroupDetailView';

export default function FundManagement() {
  const [groups, setGroups] = useState<GroupFund[]>([]);
  const [activities, setActivities] = useState<any[]>([]);
  const [selectedGroup, setSelectedGroup] = useState<GroupFund | null>(null);

  const user = useAuthStore(state => state.user);
  const location = useLocation();
  const queryParams = new URLSearchParams(location.search);
  const fundId = queryParams.get('id');

  useEffect(() => {
    if (fundId && groups.length > 0) {
      const group = groups.find(g => String(g.id) === fundId);
      if (group) {
        setSelectedGroup(group);
      }
    }
  }, [fundId, groups]);

  useEffect(() => {
    fetchData();
    
    window.addEventListener('transaction-approved', fetchData);
    return () => {
      window.removeEventListener('transaction-approved', fetchData);
    };
  }, [user]);

  useEffect(() => {
    if (selectedGroup) {
      const updatedGroup = groups.find(g => g.id === selectedGroup.id);
      if (updatedGroup) {
        setSelectedGroup(prev => {
          if (JSON.stringify(prev) !== JSON.stringify(updatedGroup)) {
            return updatedGroup;
          }
          return prev;
        });
      }
    }
  }, [groups]);

  const fetchData = async () => {
    try {
      const [fundsData, activitiesData] = await Promise.all([
        request.get('/funds/list', { params: { email: user?.email } }),
        request.get('/funds/activities', { params: { email: user?.email } })
      ]);

      if (fundsData.success) setGroups(fundsData.data);
      if (activitiesData.success) setActivities(activitiesData.data);
    } catch (error) {
      console.error('Lỗi khi tải dữ liệu trang quỹ nhóm', error);
    }
  };

  const handleFundCreated = (newFund: GroupFund, newActivity: any) => {
    setGroups([...groups, newFund]);
    setActivities([newActivity, ...activities]);
  };

  const handleLeaveGroup = (groupId: number) => {
    setGroups(groups.filter(g => g.id !== groupId));
    setSelectedGroup(null);
    history.push('/funds');
    fetchData();
  };

  const handleDeleteGroup = (groupId: number) => {
    setGroups(groups.filter(g => g.id !== groupId));
    setSelectedGroup(null);
    history.push('/funds');
    fetchData();
  };

  const handleRenameGroup = (groupId: number, newName: string) => {
    setGroups(groups.map(g => g.id === groupId ? { ...g, name: newName } : g));
    if (selectedGroup && selectedGroup.id === groupId) {
      setSelectedGroup({ ...selectedGroup, name: newName });
    }
  };

  if (selectedGroup) {
    return (
      <GroupDetailView 
        group={selectedGroup} 
        onBack={() => {
          setSelectedGroup(null);
          history.push('/funds');
        }}
        onLeaveGroup={handleLeaveGroup}
        onRenameGroup={handleRenameGroup}
        onDeleteGroup={handleDeleteGroup}
      />
    );
  }

  return (
    <GroupListView 
      groups={groups} 
      activities={activities} 
      onSelectGroup={setSelectedGroup} 
      onFundCreated={handleFundCreated}
    />
  );
}
