import { Outlet } from 'umi';
import styles from './index.less';

export default function AuthLayout() {
  return (
    <div className={styles.container}>
      {/* Khu vực thẻ Form màu trắng nằm chính giữa màn hình */}
      <div className={styles.formWrapper}>
        <Outlet /> 
      </div>
    </div>
  );
}
