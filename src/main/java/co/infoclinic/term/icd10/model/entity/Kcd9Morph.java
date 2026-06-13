package co.infoclinic.term.icd10.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(schema = "icd10", name = "KCD9_MORPH")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Kcd9Morph {

    @Id
    @Column
    private String code;

    @Column
    private String koreanLabel;

    @Column
    private String englishLabel;
}
