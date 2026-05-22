import { Outlet } from 'umi';
import styles from './index.less';

export default function AuthLayout() {
  return (
    <div className={styles.container}>
      <div className={styles.formWrapper}>
        <Outlet /> 
      </div>
    </div>
  );
}
