package no.nicolay.boligregnskap.controller;

import no.nicolay.boligregnskap.model.Eiendom;
import no.nicolay.boligregnskap.repository.EiendomRepository;
import no.nicolay.boligregnskap.repository.TransactionRepository;
import no.nicolay.boligregnskap.service.AdresseService;
import no.nicolay.boligregnskap.service.LeieestimatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/eiendommer")
public class EiendomController {

    private final EiendomRepository repository;
    private final TransactionRepository transactionRepository;
    private final AdresseService adresseService;
    private final LeieestimatService leieestimatService;

    public EiendomController(EiendomRepository repository,
                              TransactionRepository transactionRepository,
                              AdresseService adresseService,
                              LeieestimatService leieestimatService) {
        this.repository = repository;
        this.transactionRepository = transactionRepository;
        this.adresseService = adresseService;
        this.leieestimatService = leieestimatService;
    }

    @GetMapping
    public List<Eiendom> all() {
        return repository.findAll();
    }

    @PostMapping
    public Eiendom create(@RequestBody Eiendom eiendom) {
        adresseService.slaOppOgFyllInn(eiendom);
        return repository.save(eiendom);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        if (transactionRepository.existsByEiendom_Id(id)) {
            return ResponseEntity.status(409).build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/leieestimat")
    public ResponseEntity<Map<String, Object>> leieestimat(@PathVariable Long id) {
        return repository.findById(id)
                .map(eiendom -> {
                    Map<String, Object> resultat = leieestimatService.beregn(eiendom);
                    return resultat.containsKey("feil")
                            ? ResponseEntity.unprocessableEntity().body(resultat)
                            : ResponseEntity.ok(resultat);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
