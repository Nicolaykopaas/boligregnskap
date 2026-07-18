package no.nicolay.boligregnskap.migration;

import no.nicolay.boligregnskap.model.Eiendom;
import no.nicolay.boligregnskap.repository.EiendomRepository;
import no.nicolay.boligregnskap.repository.TransactionRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Én gang, idempotent: transaksjoner fra før eiendom-konseptet ble innført
 * (eiendom_id = NULL) flyttes til en standard-eiendom, slik at ingen
 * historiske regnskapsdata blir hengende uten en eiendom å vises under.
 */
@Component
public class EiendomMigrationRunner implements ApplicationRunner {

    private static final String DEFAULT_ADRESSE = "Ikke oppgitt (migrert automatisk)";

    private final TransactionRepository transactionRepository;
    private final EiendomRepository eiendomRepository;

    public EiendomMigrationRunner(TransactionRepository transactionRepository, EiendomRepository eiendomRepository) {
        this.transactionRepository = transactionRepository;
        this.eiendomRepository = eiendomRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!transactionRepository.existsByEiendomIsNull()) {
            return;
        }
        Eiendom standard = eiendomRepository.findByAdresse(DEFAULT_ADRESSE)
                .orElseGet(() -> eiendomRepository.save(new Eiendom(DEFAULT_ADRESSE)));
        transactionRepository.findAllByEiendomIsNull()
                .forEach(t -> t.setEiendom(standard));
    }
}
