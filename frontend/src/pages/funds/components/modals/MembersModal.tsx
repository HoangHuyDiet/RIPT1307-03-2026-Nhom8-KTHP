import React from 'react';
import { Modal, Avatar, Space, Tag, Button } from 'antd';
import { DeleteOutlined } from '@ant-design/icons';
import { GroupFund, FundMember } from '../../types';

interface MembersModalProps {
  isOpen: boolean;
  onClose: () => void;
  selectedGroup: GroupFund | null;
  onRequestRemove: (member: FundMember, index: number) => void;
}

export default function MembersModal({ isOpen, onClose, selectedGroup, onRequestRemove }: MembersModalProps) {
  const membersList = selectedGroup?.members || [];

  return (
    <Modal
      title={<span style={{ fontWeight: 700, fontSize: '16px' }}>Thành viên trong quỹ ({selectedGroup?.membersCount})</span>}
      open={isOpen}
      onCancel={onClose}
      footer={[
        <Button key="close" type="primary" onClick={onClose} style={{ borderRadius: 16 }}>
          Đóng
        </Button>
      ]}
      destroyOnClose
      centered
      width={380}
    >
      <div style={{ maxHeight: '350px', overflowY: 'auto', marginTop: 12 }}>
        {membersList.map((member, idx) => {
          const fullName = member.name;
          const isOwner = member.role === 'OWNER';
          const email = member.email;
          const avatar = member.avatar;
          
          return (
            <div 
              key={idx} 
              style={{ 
                display: 'flex', 
                alignItems: 'center', 
                justifyContent: 'space-between', 
                padding: '12px 0',
                borderBottom: idx < membersList.length - 1 ? '1px solid #f1f3f4' : 'none'
              }}
            >
              <Space size="middle">
                <Avatar src={avatar} size="large" />
                <div>
                  <div style={{ fontWeight: 600, color: '#202124', fontSize: '13px' }}>
                    {fullName.replace(' (Trưởng quỹ)', '')}
                  </div>
                  <div style={{ fontSize: '11px', color: '#8c98a5' }}>
                    {isOwner ? 'Trưởng quỹ' : email}
                  </div>
                </div>
              </Space>
              
              {isOwner ? (
                <Tag color="blue" style={{ borderRadius: 10, margin: 0 }}>Chủ phòng</Tag>
              ) : (
                <Button 
                  danger 
                  type="text" 
                  icon={<DeleteOutlined />} 
                  style={{ padding: 4 }}
                  onClick={() => onRequestRemove(member, idx)}
                  title="Yêu cầu xóa thành viên"
                />
              )}
            </div>
          );
        })}
      </div>
    </Modal>
  );
}
