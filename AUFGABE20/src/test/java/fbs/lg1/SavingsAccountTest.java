package fbs.lg1;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SavingsAccountTest {

    private static BigDecimal eur(String value) {
        return new BigDecimal(value);
    }

    @Test
    void testInit() {
        SavingsAccount account = new SavingsAccount("Marcel Schachner", eur("100"));
        assertThat(account.ownerName()).isEqualTo("Marcel Schachner");
        assertThat(account.balance()).isEqualByComparingTo("100");
    }

    @Test
    void testNegativeInitialBalance() {
        SavingsAccount account = new SavingsAccount("Marcel Schachner", eur("-50"));
        assertThat(account.balance()).isEqualByComparingTo("0");
    }

    @Test
    void testInitInvalid() {
        assertThatThrownBy(() -> new SavingsAccount(null, eur("10")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SavingsAccount("", eur("10")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SavingsAccount("   ", eur("10")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SavingsAccount("Marcel Schachner", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testUniqueAccountNumberAndIban() {
        SavingsAccount first = new SavingsAccount("Marcel Schachner", BigDecimal.ZERO);
        SavingsAccount second = new SavingsAccount("Marcel Ferdinand Schachner", BigDecimal.ZERO);
        assertThat(first.accountNumber()).isNotEqualTo(second.accountNumber());
        assertThat(first.iban()).isNotEqualTo(second.iban());
    }

    @Test
    void testDeposit() {
        SavingsAccount account = new SavingsAccount("Marcel Schachner", eur("100"));
        account.deposit(eur("50"));
        assertThat(account.balance()).isEqualByComparingTo("150");
    }

    @Test
    void testDepositCentsExact() {
        SavingsAccount account = new SavingsAccount("Marcel Schachner", BigDecimal.ZERO);
        account.deposit(eur("0.10"));
        account.deposit(eur("0.20"));
        assertThat(account.balance()).isEqualByComparingTo("0.30");
    }

    @Test
    void testDepositInvalid() {
        SavingsAccount account = new SavingsAccount("Marcel Schachner", eur("100"));
        assertThatThrownBy(() -> account.deposit(BigDecimal.ZERO)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> account.deposit(eur("-10"))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> account.deposit(null)).isInstanceOf(IllegalArgumentException.class);
        assertThat(account.balance()).isEqualByComparingTo("100");
    }

    @Test
    void testWithdraw() {
        SavingsAccount account = new SavingsAccount("Marcel Schachner", eur("100"));
        account.withdraw(eur("100"));
        assertThat(account.balance()).isEqualByComparingTo("0");
    }

    @Test
    void testWithdrawInvalid() {
        SavingsAccount account = new SavingsAccount("Marcel Schachner", eur("100"));
        assertThatThrownBy(() -> account.withdraw(BigDecimal.ZERO)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> account.withdraw(eur("-10"))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> account.withdraw(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> account.withdraw(eur("100.01"))).isInstanceOf(IllegalStateException.class);
        assertThat(account.balance()).isEqualByComparingTo("100");
    }

    @Test
    void testTransfer() {
        SavingsAccount source = new SavingsAccount("Marcel Schachner", eur("100"));
        SavingsAccount target = new SavingsAccount("Marcel Ferdinand Schachner", BigDecimal.ZERO);
        source.transferTo(target, eur("30"));
        assertThat(source.balance()).isEqualByComparingTo("70");
        assertThat(target.balance()).isEqualByComparingTo("30");
    }

    @Test
    void testTransferKeepsSum() {
        SavingsAccount source = new SavingsAccount("Marcel Schachner", eur("100.55"));
        SavingsAccount target = new SavingsAccount("Marcel Ferdinand Schachner", eur("20.10"));
        BigDecimal sumBefore = source.balance().add(target.balance());
        source.transferTo(target, eur("33.33"));
        assertThat(source.balance().add(target.balance())).isEqualByComparingTo(sumBefore);
    }

    @Test
    void testTransferWholeBalance() {
        SavingsAccount source = new SavingsAccount("Marcel Schachner", eur("100"));
        SavingsAccount target = new SavingsAccount("Marcel Ferdinand Schachner", BigDecimal.ZERO);
        source.transferTo(target, eur("100"));
        assertThat(source.balance()).isEqualByComparingTo("0");
        assertThat(target.balance()).isEqualByComparingTo("100");
    }

    @Test
    void testTransferInvalid() {
        SavingsAccount source = new SavingsAccount("Marcel Schachner", eur("100"));
        SavingsAccount target = new SavingsAccount("Marcel Ferdinand Schachner", BigDecimal.ZERO);
        assertThatThrownBy(() -> source.transferTo(null, eur("10"))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> source.transferTo(source, eur("10"))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> source.transferTo(target, BigDecimal.ZERO)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> source.transferTo(target, eur("-10"))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> source.transferTo(target, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> source.transferTo(target, eur("100.01"))).isInstanceOf(IllegalStateException.class);
        assertThat(source.balance()).isEqualByComparingTo("100");
        assertThat(target.balance()).isEqualByComparingTo("0");
    }

    @Test
    void testChangeOwnerName() {
        SavingsAccount account = new SavingsAccount("Marcel Schachner", BigDecimal.ZERO);
        account.changeOwnerName("Anna Beispiel");
        assertThat(account.ownerName()).isEqualTo("Anna Beispiel");
    }

    @Test
    void testChangeOwnerNameInvalid() {
        SavingsAccount account = new SavingsAccount("Marcel Schachner", BigDecimal.ZERO);
        assertThatThrownBy(() -> account.changeOwnerName("")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> account.changeOwnerName("   ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> account.changeOwnerName(null)).isInstanceOf(IllegalArgumentException.class);
        assertThat(account.ownerName()).isEqualTo("Marcel Schachner");
    }
}
