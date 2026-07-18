package no.nicolay.boligregnskap.repository;

import no.nicolay.boligregnskap.model.Eiendom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EiendomRepository extends JpaRepository<Eiendom, Long> {

    Optional<Eiendom> findByAdresse(String adresse);
}
