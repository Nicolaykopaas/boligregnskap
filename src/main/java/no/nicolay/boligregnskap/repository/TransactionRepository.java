package no.nicolay.boligregnskap.repository;

import no.nicolay.boligregnskap.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByDueDateIsNotNullAndDueDateAfterOrderByDueDateAsc(LocalDate date);

    List<Transaction> findAllByOrderByDateDesc();

    List<Transaction> findAllByEiendom_IdOrderByDateDesc(Long eiendomId);

    List<Transaction> findByEiendom_IdAndDueDateIsNotNullAndDueDateAfterOrderByDueDateAsc(Long eiendomId, LocalDate date);

    List<Transaction> findAllByEiendomIsNull();

    boolean existsByEiendomIsNull();

    boolean existsByEiendom_Id(Long eiendomId);
}
