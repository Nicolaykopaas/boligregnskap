package no.nicolay.boligregnskap.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * En enkelt transaksjon knyttet til boligen: enten en leieinntekt
 * eller en utgift (felleskostnader, vedlikehold, forsikring osv.).
 */
@Entity
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    /** Fritekst-kategori, f.eks. "Leie", "Felleskostnader", "Vedlikehold". */
    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate date;

    /** Valgfri forfallsdato, brukes til påminnelser om kommende betalinger. */
    private LocalDate dueDate;

    private String note;

    @ManyToOne
    @JoinColumn(name = "eiendom_id")
    private Eiendom eiendom;

    public Transaction() {
    }

    public Transaction(TransactionType type, String category, BigDecimal amount,
                       LocalDate date, LocalDate dueDate, String note) {
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.dueDate = dueDate;
        this.note = note;
    }

    public Long getId() {
        return id;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Eiendom getEiendom() {
        return eiendom;
    }

    public void setEiendom(Eiendom eiendom) {
        this.eiendom = eiendom;
    }
}
