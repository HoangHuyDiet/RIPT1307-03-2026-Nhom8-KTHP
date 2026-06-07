import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { Alert, Button, Card, Col, Input, Modal, QRCode, Result, Row, Space, Tag, Typography, message } from 'antd';
import {
  CheckOutlined,
  CrownOutlined,
  GiftOutlined,
  RobotOutlined,
  SafetyCertificateOutlined,
  ThunderboltOutlined,
} from '@ant-design/icons';
import { useLocation } from 'umi';
import styles from './index.less';
import api from '../../utils/api';
import { useAuthStore } from '@/store/useAuthStore';

const { Title, Text, Paragraph } = Typography;

const BASE_MONTHLY_PRICE = 59000;
const FIRST_TIME_COUPON = 'WELCOME50';

type BillingPlan = {
  id: string;
  planCode: string;
  name: string;
  months: number;
  price: number;
  badge?: string;
  subtitle: string;
  highlight?: boolean;
};

const paidPlans: BillingPlan[] = [
  {
    id: 'monthly',
    planCode: 'PRO_MONTHLY',
    name: 'Pro 1 tháng',
    months: 1,
    price: BASE_MONTHLY_PRICE,
    subtitle: 'Phù hợp để trải nghiệm trợ lý AI tài chính',
  },
  {
    id: 'semiannual',
    planCode: 'PRO_SEMIANNUAL',
    name: 'Pro 6 tháng',
    months: 6,
    price: 299000,
    badge: 'Phổ biến nhất',
    subtitle: 'Cân bằng giữa chi phí và thời gian sử dụng',
    highlight: true,
  },
  {
    id: 'annual',
    planCode: 'PRO_ANNUAL',
    name: 'Pro 1 năm',
    months: 12,
    price: 499000,
    badge: 'Tiết kiệm nhất',
    subtitle: 'Tối ưu cho người dùng lâu dài',
  },
];

const proBenefits = [
  'Chatbot AI phân tích thu chi cá nhân',
  'Gợi ý tài chính trên Dashboard',
  'Kho kiến thức RAG có nguồn tham khảo',
  'Tóm tắt tình hình tài chính theo tháng',
  'Ưu tiên hỗ trợ bởi chuyên gia tư vấn',
];

const freeBenefits = [
  'Quản lý giao dịch',
  'Mục tiêu tiết kiệm',
  'Quỹ cá nhân và quỹ nhóm',
  'Thông báo cơ bản',
];

type CheckoutData = {
  orderCode: number;
  status: 'PENDING' | 'PAID' | 'CANCELLED' | 'EXPIRED' | 'FAILED';
  planCode: string;
  planName: string;
  amount: number;
  description: string;
  checkoutUrl?: string;
  qrCode?: string;
  paymentLinkId?: string;
};

type CurrentSubscription = {
  status: string;
  planCode: string;
  planName: string;
  active: boolean;
  startedAt?: string;
  expiredAt?: string;
};

function formatPrice(value: number) {
  return `${value.toLocaleString('vi-VN')} đ`;
}

function calculateSavingPercent(plan: BillingPlan) {
  const original = plan.months * BASE_MONTHLY_PRICE;
  return Math.max(0, Math.round((1 - plan.price / original) * 100));
}

export default function PricingPage() {
  const [coupon, setCoupon] = useState(FIRST_TIME_COUPON);
  const [loadingPlanId, setLoadingPlanId] = useState<string | null>(null);
  const [checkout, setCheckout] = useState<CheckoutData | null>(null);
  const [checkoutOpen, setCheckoutOpen] = useState(false);
  const [currentSub, setCurrentSub] = useState<CurrentSubscription | null>(null);
  const normalizedCoupon = coupon.trim().toUpperCase();
  const couponApplied = normalizedCoupon === FIRST_TIME_COUPON;
  const location = useLocation();
  const { roles, setAuth, token, user } = useAuthStore();

  const isPro = currentSub?.active === true;

  const fetchSubscription = useCallback(async () => {
    try {
      const response = await api.get('/subscriptions/me');
      const sub = response.data.data as CurrentSubscription;
      setCurrentSub(sub);
      return sub;
    } catch {
      return null;
    }
  }, []);

  const refreshRoles = useCallback(async () => {
    try {
      const response = await api.get('/auth/me');
      const data = response.data?.data || response.data;
      const newRoles = data.roles || [];
      const newToken = data.token || token;
      if (newToken && newRoles.length > 0) {
        setAuth(newToken, user, newRoles);
      }
    } catch {
    }
  }, [token, user, setAuth]);

  useEffect(() => {
    fetchSubscription();
  }, [fetchSubscription]);

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    if (params.get('payment') === 'success') {
      let attempts = 0;
      const maxAttempts = 10;
      const timer = window.setInterval(async () => {
        attempts++;
        const sub = await fetchSubscription();
        if (sub?.active || attempts >= maxAttempts) {
          window.clearInterval(timer);
          if (sub?.active) {
            message.success('🎉 Thanh toán thành công! Gói Pro đã được kích hoạt.');
            refreshRoles();
          }
        }
      }, 3000);

      return () => window.clearInterval(timer);
    }
  }, [location.search, fetchSubscription, refreshRoles]);

  const planViewModels = useMemo(() => {
    return paidPlans.map((plan) => {
      const finalPrice = couponApplied ? Math.round(plan.price * 0.5) : plan.price;
      return {
        ...plan,
        finalPrice,
        monthlyEquivalent: Math.round(finalPrice / plan.months),
        savingPercent: calculateSavingPercent(plan),
      };
    });
  }, [couponApplied]);

  useEffect(() => {
    if (!checkoutOpen || !checkout || checkout.status !== 'PENDING') {
      return;
    }

    const timer = window.setInterval(async () => {
      try {
        const response = await api.get(`/subscriptions/orders/${checkout.orderCode}`);
        const nextCheckout = response.data.data as CheckoutData;
        setCheckout(nextCheckout);
        if (nextCheckout.status === 'PAID') {
          message.success('🎉 Thanh toán thành công! Gói Pro đã được kích hoạt.');
          window.clearInterval(timer);
          fetchSubscription();
          refreshRoles();
        }
      } catch (error) {
      }
    }, 4000);

    return () => window.clearInterval(timer);
  }, [checkoutOpen, checkout?.orderCode, checkout?.status, fetchSubscription, refreshRoles]);

  const handleSelectPlan = async (plan: BillingPlan) => {
    setLoadingPlanId(plan.id);
    try {
      const response = await api.post('/subscriptions/checkout', {
        planCode: plan.planCode,
        couponCode: couponApplied ? normalizedCoupon : undefined,
      });
      setCheckout(response.data.data);
      setCheckoutOpen(true);
    } catch (error: any) {
      const errorMsg = error.response?.data?.message || 'Không thể tạo QR thanh toán PayOS.';
      message.error(errorMsg);
    } finally {
      setLoadingPlanId(null);
    }
  };

  const isActivePlan = (planCode: string) => {
    return isPro && currentSub?.planCode === planCode;
  };

  return (
    <div className={styles.pricingPage}>
      {isPro && (
        <div className={styles.proActiveBanner}>
          <CrownOutlined style={{ fontSize: 20, color: '#f59e0b' }} />
          <div>
            <Text strong style={{ color: '#92400e' }}>Bạn đang sử dụng gói {currentSub?.planName}</Text>
            {currentSub?.expiredAt && (
              <Text style={{ color: '#b45309', fontSize: 13, marginLeft: 8 }}>
                · Hết hạn: {new Date(currentSub.expiredAt).toLocaleDateString('vi-VN')}
              </Text>
            )}
          </div>
        </div>
      )}

      <section className={styles.hero}>
        <Tag color="blue" className={styles.heroTag}>
          <CrownOutlined /> Smart Finance Pro
        </Tag>
        <Title className={styles.title}>Nâng cấp trải nghiệm tài chính với AI</Title>
        <Paragraph className={styles.subtitle}>
          Mở khóa trợ lý AI, gợi ý chi tiêu thông minh, phân tích tài chính theo tháng và hỗ trợ tư vấn ưu tiên.
        </Paragraph>

        {!isPro && (
          <div className={styles.couponBox}>
            <div>
              <Text strong>
                <GiftOutlined /> Ưu đãi lần đầu
              </Text>
              <Paragraph className={styles.couponHint}>
                Nhập mã <b>{FIRST_TIME_COUPON}</b> để giảm 50% cho lần đăng ký đầu tiên.
              </Paragraph>
            </div>
            <Input
              value={coupon}
              onChange={(event) => setCoupon(event.target.value)}
              className={styles.couponInput}
              placeholder="Nhập mã ưu đãi"
            />
          </div>
        )}
      </section>

      <Row gutter={[24, 24]} className={styles.planGrid}>
        <Col xs={24} md={12} xl={6}>
          <Card className={`${styles.planCard} ${styles.freeCard}`} bordered={false}>
            <Title level={3}>Gói thường</Title>
            <Text type="secondary">Dành cho quản lý tài chính cơ bản</Text>
            <div className={styles.price}>
              0 <span>đ</span>
            </div>
            <Space direction="vertical" size="middle" className={styles.features}>
              {freeBenefits.map((benefit) => (
                <Text key={benefit}>
                  <CheckOutlined /> {benefit}
                </Text>
              ))}
            </Space>
            <Button block disabled className={styles.disabledButton}>
              {isPro ? 'Gói cơ bản' : 'Đang sử dụng'}
            </Button>
          </Card>
        </Col>

        {planViewModels.map((plan) => {
          const active = isActivePlan(plan.planCode);
          return (
            <Col xs={24} md={12} xl={6} key={plan.id}>
              <Card
                className={`${styles.planCard} ${plan.highlight ? styles.highlightCard : ''} ${active ? styles.activeCard : ''}`}
                bordered={false}
              >
                {active && (
                  <Tag className={styles.activeBadge}>
                    <CrownOutlined /> Đang sử dụng
                  </Tag>
                )}
                {!active && plan.badge && <Tag className={styles.badge}>{plan.badge}</Tag>}
                <Title level={3}>{plan.name}</Title>
                <Text type="secondary">{plan.subtitle}</Text>

                <div className={styles.priceBlock}>
                  {couponApplied && !isPro && (
                    <Text delete type="secondary" className={styles.oldPrice}>
                      {formatPrice(plan.price)}
                    </Text>
                  )}
                  <div className={styles.price}>{formatPrice(isPro ? plan.price : plan.finalPrice)}</div>
                  <Text className={styles.monthlyPrice}>
                    ~ {formatPrice(Math.round((isPro ? plan.price : plan.finalPrice) / plan.months))}/tháng
                  </Text>
                  {plan.savingPercent > 0 && (
                    <Tag color="green">Tiết kiệm {plan.savingPercent}%</Tag>
                  )}
                </div>

                <Space direction="vertical" size="middle" className={styles.features}>
                  {proBenefits.map((benefit) => (
                    <Text key={benefit}>
                      <CheckOutlined /> {benefit}
                    </Text>
                  ))}
                </Space>

                {active ? (
                  <Button block disabled className={styles.activeButton}>
                    <CrownOutlined /> Đang sử dụng
                  </Button>
                ) : (
                  <Button
                    block
                    type={plan.highlight ? 'primary' : 'default'}
                    className={plan.highlight ? styles.primaryButton : styles.darkButton}
                    onClick={() => handleSelectPlan(plan)}
                    loading={loadingPlanId === plan.id}
                  >
                    {isPro ? 'Gia hạn thêm' : 'Chọn gói này'}
                  </Button>
                )}
              </Card>
            </Col>
          );
        })}
      </Row>

      <section className={styles.footerNote}>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <RobotOutlined style={{ color: '#1a73e8', fontSize: '16px' }} />
            <Text style={{ margin: 0 }}>AI chỉ đưa ra thông tin tham khảo</Text>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <SafetyCertificateOutlined style={{ color: '#10b981', fontSize: '16px' }} />
            <Text style={{ margin: 0 }}>Dữ liệu cá nhân được kiểm soát theo tài khoản</Text>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <ThunderboltOutlined style={{ color: '#f59e0b', fontSize: '16px' }} />
            <Text style={{ margin: 0 }}>Thanh toán tự động qua PayOS và kích hoạt Pro sau khi ngân hàng xác nhận</Text>
          </div>
        </div>
      </section>


      <Modal
        title="Thanh toán gói Pro qua PayOS"
        open={checkoutOpen}
        onCancel={() => setCheckoutOpen(false)}
        footer={[
          <Button key="close" onClick={() => setCheckoutOpen(false)}>
            Đóng
          </Button>,
          <Button
            key="checkout"
            type="primary"
            href={checkout?.checkoutUrl}
            target="_blank"
            disabled={!checkout?.checkoutUrl || checkout.status === 'PAID'}
          >
            Mở trang thanh toán
          </Button>,
        ]}
        width={520}
      >
        {checkout && (
          <div className={styles.checkoutModal}>
            {checkout.status === 'PAID' ? (
              <Result
                status="success"
                title="Thanh toán thành công!"
                subTitle="Gói Pro đã được kích hoạt. Bạn đã có quyền truy cập tất cả tính năng AI."
              />
            ) : (
              <>
                <Alert
                  type="info"
                  showIcon
                  message="Đang chờ thanh toán"
                  description="Quét QR hoặc mở trang thanh toán PayOS. Hệ thống sẽ tự cập nhật khi ngân hàng xác nhận giao dịch."
                />

                <div className={styles.qrBox}>
                  <QRCode value={checkout.qrCode || checkout.checkoutUrl || String(checkout.orderCode)} size={220} />
                </div>
              </>
            )}

            <div className={styles.checkoutInfo}>
              <div>
                <Text type="secondary">Gói</Text>
                <Text strong>{checkout.planName}</Text>
              </div>
              <div>
                <Text type="secondary">Số tiền</Text>
                <Text strong>{formatPrice(Number(checkout.amount))}</Text>
              </div>
              <div>
                <Text type="secondary">Nội dung</Text>
                <Text code copyable>{checkout.description}</Text>
              </div>
              <div>
                <Text type="secondary">Mã đơn</Text>
                <Text code copyable>{checkout.orderCode}</Text>
              </div>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}
