package no.nicolay.boligregnskap.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import no.nicolay.boligregnskap.model.Eiendom;
import no.nicolay.boligregnskap.util.SkatteUtil;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Estimerer forventet leieinntekt basert på SSBs leiemarkedsundersøkelse
 * (PxWebApi, tabell 09895 - https://data.ssb.no/api/v0/no/table/09895).
 * Gir et regional-/nasjonalt snittestimat, ikke et adressespesifikt tall -
 * dette må kommuniseres til brukeren via "forbehold"-feltet i resultatet.
 */
@Service
public class LeieestimatService {

    private static final String TABELL_URL = "https://data.ssb.no/api/v0/no/table/09895";

    /**
     * Kommunenummer -> Soner2-kode i SSB-tabellen. Verifisert direkte mot
     * Kartverkets adresse-API i juli 2026: Oslo=0301, Bærum=3201, Bergen=4601,
     * Trondheim=5001, Stavanger=1103. Norge har hatt kommunereformer i 2020 og
     * 2024 - verifiser disse kodene på nytt hvis leieestimatet plutselig
     * begynner å treffe "Hele landet" for adresser som burde matche en sone.
     */
    private static final Map<String, String> SONENAVN = Map.of(
            "00", "Hele landet",
            "01", "Oslo og Bærum",
            "02", "Akershus (utenom Bærum)",
            "03", "Bergen",
            "04", "Trondheim",
            "05", "Stavanger"
    );

    private final RestClient restClient;

    public LeieestimatService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    public Map<String, Object> beregn(Eiendom eiendom) {
        if (eiendom.getArealKvm() == null || eiendom.getArealKvm().signum() <= 0
                || eiendom.getAntallRom() == null || eiendom.getAntallRom() <= 0) {
            return feil("Eiendommen mangler areal og/eller antall rom - kan ikke beregne leieestimat.");
        }

        String sone = finnSone(eiendom.getKommunenummer());
        String antRomKode = eiendom.getAntallRom() >= 5 ? "5+" : String.valueOf(eiendom.getAntallRom());

        JsonNode data;
        try {
            data = hentFraSsb(sone, antRomKode);
        } catch (RestClientException e) {
            return feil("Kunne ikke hente leiepris-statistikk fra SSB akkurat nå. Prøv igjen senere.");
        }

        BigDecimal kvmPris = lesVerdi(data);
        if (kvmPris == null) {
            return feil("SSB har ikke tilgjengelig statistikk for denne sonen/romstørrelsen (for få observasjoner).");
        }

        String aar = lesAar(data);

        BigDecimal bruttoAarlig = kvmPris.multiply(eiendom.getArealKvm()).setScale(0, RoundingMode.HALF_UP);
        BigDecimal driftskostnaderAarlig = eiendom.getFelleskostnaderPerMaaned() != null
                ? eiendom.getFelleskostnaderPerMaaned().multiply(BigDecimal.valueOf(12))
                : BigDecimal.ZERO;
        BigDecimal overskuddFoerSkatt = bruttoAarlig.subtract(driftskostnaderAarlig);
        BigDecimal skatt = SkatteUtil.estimertSkatt(overskuddFoerSkatt);
        BigDecimal nettoAarlig = overskuddFoerSkatt.subtract(skatt);

        Map<String, Object> resultat = new LinkedHashMap<>();
        resultat.put("sone", sone);
        resultat.put("sonenavn", SONENAVN.getOrDefault(sone, "Ukjent"));
        resultat.put("aar", aar);
        resultat.put("kvmPris", kvmPris);
        resultat.put("bruttoAarligLeie", bruttoAarlig);
        resultat.put("bruttoManedligLeie", bruttoAarlig.divide(BigDecimal.valueOf(12), 0, RoundingMode.HALF_UP));
        resultat.put("driftskostnaderAarlig", driftskostnaderAarlig);
        resultat.put("estimertSkatt", skatt);
        resultat.put("nettoAarligLeie", nettoAarlig);
        resultat.put("nettoManedligLeie", nettoAarlig.divide(BigDecimal.valueOf(12), 0, RoundingMode.HALF_UP));
        resultat.put("forbehold", "Grovt estimat basert på SSBs leiemarkedsundersøkelse for sonen \""
                + SONENAVN.getOrDefault(sone, sone) + "\" (" + aar + "), ikke adressespesifikt. "
                + "Skatt er et forenklet anslag på 22 % av positivt overskudd.");
        return resultat;
    }

    private String finnSone(String kommunenummer) {
        if (kommunenummer == null) {
            return "00";
        }
        return switch (kommunenummer) {
            case "0301", "3201" -> "01"; // Oslo, Bærum
            case "4601" -> "03"; // Bergen
            case "5001" -> "04"; // Trondheim
            case "1103" -> "05"; // Stavanger
            default -> kommunenummer.startsWith("32") ? "02" : "00"; // Akershus utenom Bærum / ellers hele landet
        };
    }

    private JsonNode hentFraSsb(String sone, String antRomKode) {
        ObjectNode body = JsonNodeFactory.instance.objectNode();
        ArrayNode query = body.putArray("query");
        query.add(dimensjon("Soner2", "item", sone));
        query.add(dimensjon("AntRom", "item", antRomKode));
        query.add(dimensjon("ContentsCode", "item", "Husleiear"));
        query.add(dimensjon("Tid", "top", "1"));
        body.putObject("response").put("format", "json-stat2");

        return restClient.post()
                .uri(TABELL_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(JsonNode.class);
    }

    private ObjectNode dimensjon(String kode, String filter, String verdi) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("code", kode);
        ObjectNode selection = node.putObject("selection");
        selection.put("filter", filter);
        selection.putArray("values").add(verdi);
        return node;
    }

    private BigDecimal lesVerdi(JsonNode data) {
        if (data == null) {
            return null;
        }
        JsonNode value = data.path("value");
        if (!value.isArray() || value.isEmpty()) {
            return null;
        }
        JsonNode forste = value.get(0);
        if (forste == null || forste.isNull()) {
            return null;
        }
        return new BigDecimal(forste.asText());
    }

    private String lesAar(JsonNode data) {
        JsonNode tid = data.path("dimension").path("Tid").path("category").path("label");
        Iterator<String> aartall = tid.fieldNames();
        return aartall.hasNext() ? aartall.next() : "ukjent år";
    }

    private Map<String, Object> feil(String melding) {
        return Map.of("feil", melding);
    }
}
