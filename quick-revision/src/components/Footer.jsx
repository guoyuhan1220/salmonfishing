import styles from './Footer.module.css'

function Footer() {
  const currentYear = new Date().getFullYear()
  
  return (
    <footer className={styles.footer}>
      <div className={styles.container}>
        <p>© {currentYear} Quick Revision. All rights reserved.</p>
      </div>
    </footer>
  )
}

export default Footer