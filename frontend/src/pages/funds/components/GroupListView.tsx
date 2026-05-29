import React, { useState } from 'react';
import { Row, Col, Card, Typography, Space, Button, Progress, Avatar, Input, Tag } from 'antd';
import { PlusOutlined, SearchOutlined, WalletOutlined, ArrowUpOutlined, ArrowDownOutlined, ClockCircleOutlined, UserAddOutlined, DeleteOutlined, SmileOutlined } from '@ant-design/icons';
import styles from '../index.less';
import { GroupFund } from '../types';
import { formatVND } from '@/utils/format';
import CreateFundModal from './modals/CreateFundModal';

const { Title, Text } = Typography;

interface GroupListViewProps {
  groups: GroupFund[];
  activities: any[];
  onSelectGroup: (group: GroupFund) => void;
  onFundCreated: (newFund: GroupFund, newActivity: any) => void;
}

export default function GroupListView({ groups, activities, onSelectGroup, onFundCreated }: GroupListViewProps) {
  const [search, setSearch] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);

  const filteredGroups = groups.filter(g => 
    g.name.toLowerCase().includes(search.toLowerCase())
  );

  const renderGroupCard = (group: GroupFund) => {
    const percent = group.target > 0 ? Math.round((group.balance / group.target) * 100) : 0;
    return (
      <Col xs={24} sm={12} key={group.id}>
        <Card 
          bordered={false} 
          className={styles.groupCard}
          onClick={() => onSelectGroup(group)}
          style={{ cursor: 'pointer' }}
        >
          <div className={styles.cardTop}>
            <Tag color="default" className={styles.memberTag}>{group.membersCount} Thành viên</Tag>
          </div>

          <div className={styles.cardMid}>
            <div className={styles.groupName}>{group.name}</div>
            <div className={styles.groupAmount}>
              <span className={styles.groupBalance}>{formatVND(group.balance)}</span>
              <span className={styles.groupTarget}> / {formatVND(group.target)}</span>
            </div>
          </div>

          <div className={styles.cardBottom}>
            <Progress 
              percent={percent > 100 ? 100 : percent} 
              format={() => ''}
              showInfo={false} 
              strokeWidth={6} 
              strokeColor={group.themeColor}
            />
            
            <div className={styles.cardFooter}>
              <Avatar.Group maxCount={3} size="small" className={styles.avatarStack}>
                {group.members.map((member, idx) => (
                  <Avatar key={idx} src={member.avatar} />
                ))}
              </Avatar.Group>
              <span className={styles.percentText}>{percent}%</span>
            </div>
          </div>
        </Card>
      </Col>
    );
  };

  return (
    <div className={styles.container}>
      <div className={styles.headerRow}>
        <div className={styles.searchWrapper}>
          <Input
            prefix={<SearchOutlined style={{ color: '#8c8c8c' }} />}
            placeholder="Tìm kiếm quỹ..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className={styles.searchBar}
            allowClear
          />
        </div>
      </div>

      <div className={styles.sectionHeader}>
        <Title level={3} className={styles.sectionTitle}>Quản Lý Quỹ Nhóm</Title>
        <Button 
          type="primary" 
          icon={<PlusOutlined />} 
          onClick={() => setIsModalOpen(true)}
          className={styles.createBtn}
        >
          Tạo Quỹ Nhóm
        </Button>
      </div>

      <Row gutter={[24, 24]}>
        <Col xs={24} lg={17}>
          <Space direction="vertical" size="large" style={{ width: '100%' }}>


            <div className={styles.activeGroupsWrapper}>
              <div className={styles.subHeader}>
                <Title level={4} className={styles.subTitle}>Danh Sách Quỹ Nhóm</Title>
              </div>

              <Row gutter={[16, 16]}>
                {filteredGroups.length === 0 ? (
                  <Col span={24}>
                    <Card bordered={false} className={styles.emptyCard}>
                      <SmileOutlined style={{ fontSize: 40, color: '#bfbfbf' }} />
                      <div style={{ marginTop: 12, color: '#8c8c8c' }}>Không có quỹ nhóm nào phù hợp</div>
                    </Card>
                  </Col>
                ) : (
                  filteredGroups.map(renderGroupCard)
                )}
              </Row>
            </div>
          </Space>
        </Col>

        <Col xs={24} lg={7}>
          <Card bordered={false} className={styles.logCard}>
            <div className={styles.logHeader}>
              <Space>
                <ClockCircleOutlined className={styles.clockIcon} />
                <Text strong style={{ fontSize: '15px', color: '#1a1d20' }}>Nhật ký hoạt động</Text>
              </Space>
            </div>

            <div className={styles.logList}>
              {activities.map(activity => (
                <div className={styles.logItem} key={activity.id}>
                  <div className={styles.logIconWrapper} style={{ backgroundColor: `${activity.color}15`, color: activity.color }}>
                    {activity.type === 'join' ? <UserAddOutlined /> : activity.type === 'delete' ? <DeleteOutlined /> : <PlusOutlined />}
                  </div>
                  <div className={styles.logContent}>
                    <div className={styles.logTextItem}>{activity.text}</div>
                    <div className={styles.logTime}>{activity.time}</div>
                  </div>
                </div>
              ))}
            </div>
          </Card>
        </Col>
      </Row>

      <CreateFundModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSuccess={onFundCreated}
      />
    </div>
  );
}
