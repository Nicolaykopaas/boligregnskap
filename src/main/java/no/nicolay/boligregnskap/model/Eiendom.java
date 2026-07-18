package no.nicolay.boligregnskap.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * En bolig/adresse man fører regnskap for eller vurderer å leie ut.
 * Feltene speiler det man typisk fyller inn på en boligannonse.
 */
@Entity
public class Eiendom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String adresse;

    private String postnummer;
    private String poststed;
    private String kommune;
    private String kommunenummer;

    @Enumerated(EnumType.STRING)
    private Boligtype boligtype;

    @Column(nullable = false)
    private BigDecimal arealKvm;

    @Column(nullable = false)
    private Integer antallRom;

    private Integer antallSoverom;
    private Integer byggeaar;
    private Integer etasje;
    private BigDecimal prisantydning;
    private BigDecimal felleskostnaderPerMaaned;
    private String notat;

    public Eiendom() {
    }

    /** Brukt av migreringsjobben til å opprette en standard-eiendom for gamle transaksjoner. */
    public Eiendom(String adresse) {
        this.adresse = adresse;
        this.arealKvm = BigDecimal.ZERO;
        this.antallRom = 0;
    }

    public Long getId() {
        return id;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getPostnummer() {
        return postnummer;
    }

    public void setPostnummer(String postnummer) {
        this.postnummer = postnummer;
    }

    public String getPoststed() {
        return poststed;
    }

    public void setPoststed(String poststed) {
        this.poststed = poststed;
    }

    public String getKommune() {
        return kommune;
    }

    public void setKommune(String kommune) {
        this.kommune = kommune;
    }

    public String getKommunenummer() {
        return kommunenummer;
    }

    public void setKommunenummer(String kommunenummer) {
        this.kommunenummer = kommunenummer;
    }

    public Boligtype getBoligtype() {
        return boligtype;
    }

    public void setBoligtype(Boligtype boligtype) {
        this.boligtype = boligtype;
    }

    public BigDecimal getArealKvm() {
        return arealKvm;
    }

    public void setArealKvm(BigDecimal arealKvm) {
        this.arealKvm = arealKvm;
    }

    public Integer getAntallRom() {
        return antallRom;
    }

    public void setAntallRom(Integer antallRom) {
        this.antallRom = antallRom;
    }

    public Integer getAntallSoverom() {
        return antallSoverom;
    }

    public void setAntallSoverom(Integer antallSoverom) {
        this.antallSoverom = antallSoverom;
    }

    public Integer getByggeaar() {
        return byggeaar;
    }

    public void setByggeaar(Integer byggeaar) {
        this.byggeaar = byggeaar;
    }

    public Integer getEtasje() {
        return etasje;
    }

    public void setEtasje(Integer etasje) {
        this.etasje = etasje;
    }

    public BigDecimal getPrisantydning() {
        return prisantydning;
    }

    public void setPrisantydning(BigDecimal prisantydning) {
        this.prisantydning = prisantydning;
    }

    public BigDecimal getFelleskostnaderPerMaaned() {
        return felleskostnaderPerMaaned;
    }

    public void setFelleskostnaderPerMaaned(BigDecimal felleskostnaderPerMaaned) {
        this.felleskostnaderPerMaaned = felleskostnaderPerMaaned;
    }

    public String getNotat() {
        return notat;
    }

    public void setNotat(String notat) {
        this.notat = notat;
    }
}
