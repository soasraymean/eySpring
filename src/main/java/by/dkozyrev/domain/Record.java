package by.dkozyrev.domain;

//Class represents a single DB record
public class Record {
    private Long id;
    private String bankNumber;
    private Double incomingSaldoActive;
    private Double incomingSaldoPassive;
    private Double debet;
    private Double credit;
    private Double outcomingSaldoActive;
    private Double outcomingSaldoPassive;

    public Record() {
    }

    public Long getId() {
        return id;
    }

    public String getBankNumber() {
        return bankNumber;
    }

    public void setBankNumber(String bankNumber) {
        this.bankNumber = bankNumber;
    }

    public Double getIncomingSaldoActive() {
        return incomingSaldoActive;
    }

    public void setIncomingSaldoActive(Double incomingSaldoActive) {
        this.incomingSaldoActive = incomingSaldoActive;
    }

    @Override
    public String toString() {
        return "Record{" +
                "id=" + id +
                ", bankNumber='" + bankNumber + '\'' +
                ", incomingSaldoActive=" + incomingSaldoActive +
                ", incomingSaldoPassive=" + incomingSaldoPassive +
                ", debet=" + debet +
                ", credit=" + credit +
                ", outcomingSaldoActive=" + outcomingSaldoActive +
                ", outcomingSaldoPassive=" + outcomingSaldoPassive +
                '}';
    }

    public Double getIncomingSaldoPassive() {
        return incomingSaldoPassive;
    }

    public void setIncomingSaldoPassive(Double incomingSaldoPassive) {
        this.incomingSaldoPassive = incomingSaldoPassive;
    }

    public Double getDebet() {
        return debet;
    }

    public void setDebet(Double debet) {
        this.debet = debet;
    }

    public Double getCredit() {
        return credit;
    }

    public void setCredit(Double credit) {
        this.credit = credit;
    }

    public Double getOutcomingSaldoActive() {
        return outcomingSaldoActive;
    }

    public void setOutcomingSaldoActive(Double outcomingSaldoActive) {
        this.outcomingSaldoActive = outcomingSaldoActive;
    }

    public Double getOutcomingSaldoPassive() {
        return outcomingSaldoPassive;
    }

    public void setOutcomingSaldoPassive(Double outcomingSaldoPassive) {
        this.outcomingSaldoPassive = outcomingSaldoPassive;
    }
}
