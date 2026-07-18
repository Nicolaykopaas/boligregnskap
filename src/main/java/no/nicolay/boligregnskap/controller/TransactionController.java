package no.nicolay.boligregnskap.controller;

import no.nicolay.boligregnskap.model.Transaction;
import no.nicolay.boligregnskap.model.TransactionType;
import no.nicolay.boligregnskap.repository.EiendomRepository;
import no.nicolay.boligregnskap.repository.TransactionRepository;
import no.nicolay.boligregnskap.util.SkatteUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionRepository repository;
    private final EiendomRepository eiendomRepository;

    public TransactionController(TransactionRepository repository, EiendomRepository eiendomRepository) {
        this.repository = repository;
        this.eiendomRepository = eiendomRepository;
    }

    @GetMapping
    public List<Transaction> all(@RequestParam(required = false) Long eiendomId) {
        return eiendomId != null
                ? repository.findAllByEiendom_IdOrderByDateDesc(eiendomId)
                : repository.findAllByOrderByDateDesc();
    }

    @PostMapping
    public ResponseEntity<Transaction> create(@RequestBody Transaction transaction,
                                               @RequestParam(required = false) Long eiendomId) {
        if (eiendomId != null) {
            var eiendom = eiendomRepository.findById(eiendomId);
            if (eiendom.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            // NB: bruk findById (ikke getReferenceById) - getReferenceById gir en Hibernate-proxy
            // som Jackson ikke klarer å serialisere når vi returnerer transaksjonen i responsen.
            transaction.setEiendom(eiendom.get());
        }
        return ResponseEntity.ok(repository.save(transaction));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /** Kommende forfallsdatoer, til påminnelser. */
    @GetMapping("/upcoming")
    public List<Transaction> upcoming(@RequestParam(required = false) Long eiendomId) {
        return eiendomId != null
                ? repository.findByEiendom_IdAndDueDateIsNotNullAndDueDateAfterOrderByDueDateAsc(eiendomId, LocalDate.now())
                : repository.findByDueDateIsNotNullAndDueDateAfterOrderByDueDateAsc(LocalDate.now());
    }

    /** Årsrapport: sum inntekter, sum utgifter, overskudd og grovt skatteestimat. */
    @GetMapping("/report/{year}")
    public Map<String, Object> report(@PathVariable int year, @RequestParam(required = false) Long eiendomId) {
        List<Transaction> all = eiendomId != null
                ? repository.findAllByEiendom_IdOrderByDateDesc(eiendomId)
                : repository.findAll();

        BigDecimal income = all.stream()
                .filter(t -> t.getType() == TransactionType.INNTEKT)
                .filter(t -> t.getDate().getYear() == year)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal expenses = all.stream()
                .filter(t -> t.getType() == TransactionType.UTGIFT)
                .filter(t -> t.getDate().getYear() == year)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal profit = income.subtract(expenses);
        BigDecimal tax = SkatteUtil.estimertSkatt(profit);

        return Map.of(
                "year", year,
                "income", income,
                "expenses", expenses,
                "profit", profit,
                "estimatedTax", tax
        );
    }
}
