package no.nicolay.boligregnskap.service;

import com.fasterxml.jackson.databind.JsonNode;
import no.nicolay.boligregnskap.model.Eiendom;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Geokoder en adresse mot Kartverkets åpne, nøkkelfrie adresse-API
 * (https://ws.geonorge.no/adresser/v1/) for å finne kommune/postnummer.
 */
@Service
public class AdresseService {

    private static final String SOK_URL = "https://ws.geonorge.no/adresser/v1/sok";

    private final RestClient restClient;

    public AdresseService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(4000);
        factory.setReadTimeout(4000);
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    /** Best-effort: fyller inn kommune/postnummer-felter på eiendommen. Feiler stille hvis oppslaget ikke lykkes. */
    public void slaOppOgFyllInn(Eiendom eiendom) {
        if (eiendom.getAdresse() == null || eiendom.getAdresse().isBlank()) {
            return;
        }
        try {
            // Viktig: bruk uriBuilder (queryParam) fremfor å legge en selv-encodet streng inn i
            // .uri(String) - RestClient tolker en String som en URI-mal og encoder den på nytt,
            // som dobbelt-encoder (%25...) og gir null treff hos Kartverket.
            JsonNode response = restClient.get()
                    .uri(SOK_URL + "?sok={sok}&treffPerSide=1", eiendom.getAdresse())
                    .retrieve()
                    .body(JsonNode.class);
            if (response == null) {
                return;
            }
            JsonNode treff = response.path("adresser");
            if (!treff.isArray() || treff.isEmpty()) {
                return;
            }
            JsonNode adresse = treff.get(0);
            eiendom.setKommune(tekst(adresse, "kommunenavn"));
            eiendom.setKommunenummer(tekst(adresse, "kommunenummer"));
            eiendom.setPostnummer(tekst(adresse, "postnummer"));
            eiendom.setPoststed(tekst(adresse, "poststed"));
        } catch (RestClientException e) {
            // Geokoding er best-effort - eiendommen lagres uten kommune-felter hvis oppslaget feiler.
        }
    }

    private String tekst(JsonNode node, String felt) {
        JsonNode verdi = node.get(felt);
        return (verdi == null || verdi.isNull()) ? null : verdi.asText();
    }
}
